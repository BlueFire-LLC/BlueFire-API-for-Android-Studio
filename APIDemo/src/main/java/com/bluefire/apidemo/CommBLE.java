package com.bluefire.apidemo;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.bluetooth.le.ScanSettings.Builder;
import android.content.Context;
import android.os.Handler;

import com.bluefire.api.BFCommBLE;
import com.bluefire.api.BlueFire;
import com.bluefire.api.ConnectionStates;
import com.bluefire.api.Const;
import com.bluefire.api.Helper;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import static android.bluetooth.le.ScanSettings.CALLBACK_TYPE_ALL_MATCHES;
import static android.bluetooth.le.ScanSettings.SCAN_MODE_BALANCED;

public class CommBLE extends Comm
{
	//region Declaratives

	private BluetoothLeScanner BTScanner;

	private BluetoothDevice BTDevice;
	private BluetoothGatt BLEGatt;

	private int CurrentState = BluetoothProfile.STATE_DISCONNECTED;

	private int GattStatus = -1;

	private final int GattStatus_LossPower = 8; // not defined in GattStatus

	protected BluetoothGattCharacteristic J1939DataCharacteristic;
	protected BluetoothGattCharacteristic J1708DataCharacteristic;

	protected BluetoothGattCharacteristic ClientPacket1Characteristic;
	protected BluetoothGattCharacteristic ClientPacket2Characteristic;
	protected BluetoothGattCharacteristic ClientPacket3Characteristic;

	private BluetoothGattCharacteristic ClientCharacteristic;

	private BluetoothGattService GenericAccess;
	private BluetoothGattService GenericAttribute;

	private BluetoothGattService AdapterService;
	private BluetoothGattService ClientService;

	private ArrayList<String> IsCharacteristicNotification = new ArrayList<String>();

	protected boolean IsWritingData;
	private int WriteTimeout;
	private int WriteTimeoutMax = Const.OneSecond;

	protected boolean IsUpdatingDescriptor;
	private int UpdateDescriptorTimeout;
	private final int UpdateDescriptorTimeoutMax = 500; // ms

	private Timer NotificationsTimer;
	protected final int NotificationsInterval = 100; // ms

	// BLE Scanning

	protected boolean IsScanning;

	protected boolean IsWaitingForAdapter;
	protected boolean IsAdapterConnected;

	private final int NoSignal = -128;
	private int RawSignalStrength = NoSignal;

	private List<String> DeviceAddresses = new ArrayList<String>();

	private boolean IsAdvertisementTimedOut;

	private Timer AdvertisementTimer;
	private Timer DiscoveryTimer;

	private boolean WaitForDisconnect;
	private DisconnectThreading DisconnectThread;

	//endregion

	//region Constructor

	public CommBLE(Context context, Handler connectionHandler, BlueFire blueFire)
	{
		super(context, connectionHandler, blueFire);

		BFDeviceName = BFCommBLE.AdapterName;
	}

	//endregion

	//region Initialize

	@Override
	protected void Initialize()
	{
		super.Initialize();

		IsScanning = false;
		IsAdapterConnected = false;
	}

	//endregion

	//region Connection

	@Override
	protected boolean StartConnection()
	{
		if (!super.StartConnection())
			return false;

		try
		{
			// Check for the ConnectToLastAdapter set by the app
			if (IsLastConnectedAdapterSet())
            {
                if (GetRemoteDevice(blueFire.AdapterId()))
				{
					AdapterConnected("last/secured", blueFire.AdapterId());
					return true;
				}
				// Did not find last connected adapter.
				// Note, it is possible to not find the last connected adapter even though it is plugged in
				// so we have to allow for this and drop into the scan.
				RaiseNotification("CommBLE", "Failed to find or connect to last/secured adapter '" + blueFire.AdapterId() + "'");
			}
			else // Check for a previous adapter
			{
				if (!PreviousId.equals(""))
				{
                    if (GetRemoteDevice(PreviousId))
                    {
						AdapterConnected("previous", PreviousId);
                        return true;
                    }
					RaiseNotification("CommBLE", "Failed to find or connect to previous adapter '" + PreviousId + "'. Scanning for new adapter." + GetConnectionData());
				}
			}

			// Did not find an adapter, re-initialize the connection for scanning
			if (!InitializeConnection())
				return false;

			// Scan for an adapter
			if (!ScanForDevice())
			{
				RaiseNotification("CommBLE", "Failed to find an adapter." + GetConnectionData());
				return false;
			}

			// Found an adapter, attempt to connect to it
			StartDiscoveryTimer();

			if (!ConnectGatt(BTDevice))
				return false;

            if (WaitForConnection()) // this will block
			{
				AdapterConnected("scanned", DeviceAddress);
				return true;
			}
            else
			{
				RaiseNotification("CommBLE", "Failed to connect to scanned adapter '" + DeviceAddress + "'" + GetConnectionData());
				return false;
			}

		} catch (Exception ex)
		{
			RaiseSystemError("CommBLE.StartConnection", ex);
			return false;
		}
	}

	private String GetConnectionData()
	{
		return (Const.CrLf + "ConnectToLastAdapter=" + blueFire.ConnectToLastAdapter() + ", SecureAdapter=" + blueFire.SecureAdapter() + ", AdapterId='" + blueFire.AdapterId() + "'" + ", ConnectionState=" + this.ConnectionState + ", GattStatus=" + GattStatus);
	}

	private boolean GetRemoteDevice(String AdapterId)
    {
		// Attempt to connect directly to the adapter without scanning
        BTDevice = BTAdapter.getRemoteDevice(AdapterId);

		// Check if did not find an adapter.
		// Note, it is possible to not find an adapter even though it is plugged in
		// and the adapter id is correct.
		if (BTDevice.getName() == null)
            return false;

		// Found a device (real or cached)
		// Note, if the mobile device has previously connected to an adapter it will
		// be cached and the device returned even if it's not present.
        DeviceName = BTDevice.getName();
        DeviceAddress = BTDevice.getAddress();

		// Attempt to connect to it.
		StartDiscoveryTimer();

        if (!ConnectGatt(BTDevice))
        	return false;

        if (WaitForConnection()) // this will block
			return true;

		// Failed to connect.
		// Note, the discovery timer will disconnect Gatt.
		return false;
    }

	//endregion

	//region Discovery

	private void StartDiscoveryTimer()
	{
		// Note, do not set the State to Discovering because BLE doesn't really doesn't have a discovery,
		// we just use this for a connection timeout.
		// UpdateState(ConnectionStates.Discovering);

		DiscoveryTimer = new Timer();
		DiscoveryTimer.schedule(new DiscoveryTimedOut(), blueFire.DiscoveryTimeout(), Long.MAX_VALUE);
	}

	private class DiscoveryTimedOut extends TimerTask
	{
		@Override
		public void run()
		{
			try
			{
				// Stop the discovery timer (but don't nuke it)
				DiscoveryTimer.cancel();

				// Check if still scanning.
				// Note, if an adapter is found the scan will be stopped
				if (IsScanning)
					StopScan();

				Disconnect();

				AdapterNotConnected();

			} catch (Exception ex)
			{
			}
		}
	};

	private void StopDiscoveryTimer()
	{
		try
		{
			if (DiscoveryTimer != null)
			{
				DiscoveryTimer.cancel();
				DiscoveryTimer.purge();
				DiscoveryTimer = null;
			}
		}
		catch(Exception ex){}
	}

	//endregion

	//region Scanning

	private boolean ScanForDevice()
	{
		StartScan();

		WaitForAdapter();

		StopScan();

		if (DeviceName == "")
			return false;

		// Check for not last adapter id
		if (!IsValidAdapter())
			return false;

		return true;
	}

	public boolean WaitForAdapter()
	{
		while (IsWaitingForAdapter && !IsAdvertisementTimedOut)
			Helper.Sleep(10);

		StopAdvertisementTimer();

		return !IsAdvertisementTimedOut;
	}

	protected boolean WaitForConnection()
	{
		while (!IsAdapterConnected && (ConnectionState == ConnectionStates.Connecting ||ConnectionState == ConnectionStates.Reconnecting))
			Helper.Sleep(100);

		StopDiscoveryTimer();

		return IsAdapterConnected;
	}

	// Method to scan BLE Devices. The status of the scan will be detected in the BluetoothAdapter.LeScanCallback
	private void StartScan()
	{
		try
		{
			IsScanning = true;

			RawSignalStrength = NoSignal;

			DeviceName = "";
			DeviceAddress = "";
			DeviceAddresses.clear();

            StartAdvertisementTimer(blueFire.AdvertisementTimeout());

			if (BTScanner == null)
				BTScanner = BTAdapter.getBluetoothLeScanner();

			Builder SettingsBuilder = new Builder();
			SettingsBuilder.setCallbackType(CALLBACK_TYPE_ALL_MATCHES);
			SettingsBuilder.setScanMode(SCAN_MODE_BALANCED);
			ScanSettings Settings = SettingsBuilder.build();

			List<ScanFilter> Filters = new ArrayList<ScanFilter>();
			ScanFilter.Builder FilterBuilder = new ScanFilter.Builder();
			FilterBuilder.setDeviceName(BFCommBLE.AdapterName);
			ScanFilter Filter = FilterBuilder.build();
			Filters.add(Filter);

			BTScanner.startScan(Filters, Settings, BLEScanCallback);

		} catch (Exception ex)
		{
			IsScanning = false;
			RaiseSystemError("CommBLE StartScan", ex);
		}
	}

	private void StopScan()
	{
		try
		{
			if (!IsScanning)
				return;

			IsScanning = false;

			StopAdvertisementTimer();

			if (!BTAdapter.isEnabled())
				return;

			if (BTScanner != null)
				BTScanner.stopScan(BLEScanCallback);

		} catch (Exception ex)
		{
			RaiseSystemError("CommBLE StopScan", ex);
		}
	}

	private ScanCallback BLEScanCallback = new ScanCallback()
	{
		// Call back for BLE Scan.
		// This call back is called when a BLE device is found near by.

		@Override
		public void onScanResult(int CallbackType, ScanResult Result)
		{
			try
			{
				// Ignore if scanning has been stopped
				if (!IsScanning)
					return;

				if (!IsWaitingForAdapter)
					return;

				BluetoothDevice AdDevice = Result.getDevice();

				String AdDeviceName = AdDevice.getName() + ""; // in case it is null;
				String AdDeviceAddress = AdDevice.getAddress();
				int AdSignalStrength = Result.getRssi();

				// Ignore duplicate adapter events
				if (DeviceAddresses.contains(AdDeviceAddress))
					return;
				else
					DeviceAddresses.add(AdDeviceAddress);

				// Ignore if not a valid BlueFire adapter.
				// Note, this will check for a BlueFire adapter that is the correct adapter
				// if the last connected adapter is set, or a any BlueFire adapter.
				if (!IsValidAdapter(AdDeviceName, AdDeviceAddress))
					return;

				// Check for finding a last connected adapter
				if (IsLastConnectedAdapter(AdDeviceAddress))
				{
					BTDevice = AdDevice;
					DeviceName = AdDeviceName;
					DeviceAddress = AdDeviceAddress;
					IsWaitingForAdapter = false;
					return;
				}

				// Keep looking for an adapter with the best signal.
				// Note, this will continue until the WaitingForAdapter times out.
				if (AdSignalStrength > RawSignalStrength)
				{
					BTDevice = AdDevice;
					DeviceName = AdDeviceName;
					DeviceAddress = AdDeviceAddress;
					RawSignalStrength = AdSignalStrength;
				}

				// Keep waiting for another adapter
				StartAdvertisementTimer(Const.OneSecond);

			} catch (Exception ex)
			{
				RaiseSystemError("CommBLE OnLeScan", ex);
			}
		}

		@Override
		public void onScanFailed(int ErrorCode)
		{
			RaiseSystemError("CommBLE onScanFailed","BLEScanCallback Failed, ErrorCode=" + ErrorCode);
		}
	};

	//endregion

	//region Advertising

	private void StartAdvertisementTimer(int TimeoutInterval)
	{
		IsWaitingForAdapter = true;
		IsAdvertisementTimedOut = false;

		AdvertisementTimer = new Timer();
		AdvertisementTimer.schedule(new AdvertisementTimedOut(), TimeoutInterval, Long.MAX_VALUE);
	}

	private void StopAdvertisementTimer()
	{
        IsWaitingForAdapter = false;
        try
        {
            if (AdvertisementTimer != null)
            {
                AdvertisementTimer.cancel();
                AdvertisementTimer.purge();
                AdvertisementTimer = null;
            }
        }
        catch(Exception ex){}
	}

	private class AdvertisementTimedOut extends TimerTask
	{
		@Override
		public void run()
		{
			try
			{
				// Stop the discovery timer (but don't nuke it)
				StopAdvertisementTimer();
				//AdvertisementTimer.cancel();

				IsAdvertisementTimedOut = true;

				if (IsScanning)
					StopScan();

			} catch (Exception ex)
			{
			}
		}
	};

	//endregion

	//region GATT Connection

	protected boolean ConnectGatt(BluetoothDevice Device)
	{
		try
		{
			// Get the connection status of the device
			if (CurrentState != BluetoothProfile.STATE_DISCONNECTED)
				DisconnectGatt();

			CurrentState = BluetoothProfile.STATE_DISCONNECTED;

//			android.util.Log.d("BlueFire", "CommBLE ConnectGatt, Connecting Gatt");

			BLEGatt = Device.connectGatt(Context, true, BLEGattCallback);

			return true;

		} catch (Exception ex)
		{
			if (ex.getMessage().contains("null object reference"))
				return false;

			RaiseSystemError("CommBLE ConnectGatt", ex);
			return false;
		}
	}

	//endregion

	//region GATT Callback

	private BluetoothGattCallback BLEGattCallback = new BluetoothGattCallback()
	{
		// Check if the adapter connection changed (eg. connected)
		@Override
		public void onConnectionStateChange(BluetoothGatt Gatt, int Status, int NewState)
		{
			if (Gatt == null || BLEGatt == null)
				return;

			GattStatus = Status;

			try
			{
				switch (Status)
				{
					case BluetoothGatt.GATT_FAILURE:
					case BluetoothGatt.GATT_INSUFFICIENT_AUTHENTICATION:
					case BluetoothGatt.GATT_INSUFFICIENT_ENCRYPTION:
					case BluetoothGatt.GATT_INVALID_ATTRIBUTE_LENGTH:
					case BluetoothGatt.GATT_INVALID_OFFSET:
					case BluetoothGatt.GATT_READ_NOT_PERMITTED:
					case BluetoothGatt.GATT_REQUEST_NOT_SUPPORTED:
					case BluetoothGatt.GATT_WRITE_NOT_PERMITTED:
						StopScan();
						RaiseDataError("CommBLE onConnectionStateChange, Gatt Status=" + Status);
						return;
					case 61:
						StopScan();
						RaiseDataError("CommBLE onConnectionStateChange, Gatt Status=" + Status);
						return;
				}

				if (NewState == CurrentState) // no change
					return;

				CurrentState = NewState;

//				android.util.Log.d("BlueFire", "CommBLE BLEGattCallback, New State=" + NewState + ", Current State=" + CurrentState);

				switch (NewState)
				{
					case BluetoothProfile.STATE_CONNECTING:
						break;

					case BluetoothProfile.STATE_CONNECTED:
						StopScan();

						if (IsConnecting)
							BLEGatt.discoverServices();
						break;

					case BluetoothProfile.STATE_DISCONNECTING:
					    if (IsConnecting || IsConnected)
						    AdapterNotConnected();
						break;

					case BluetoothProfile.STATE_DISCONNECTED:
//						android.util.Log.d("BlueFire", "CommBLE BLEGattCallback, Gatt Disconnected, closing Gatt, State=" + NewState + ", Current State=" + CurrentState);

						// Close the Gatt connection.
						// Note, this must be before AdapterNotConnected.
						CloseGatt(false);

						// Check for notifying the app
						if (IsConnecting || IsConnected)
							AdapterNotConnected();
						break;
				}
			} catch (Exception ex)
			{
				RaiseSystemError("CommBLE OnConnectionStateChange", ex);
			}
		}

        // Check for discovering the adapter
        @Override
		public void onServicesDiscovered(BluetoothGatt Gatt, int Status)
        {
            if (Status != BluetoothGatt.GATT_SUCCESS)
            {
            	RaiseDataError("CommBLE OnServicesDiscovered, Gatt Status=" + Status);
                return;
            }

            // Get the Gatt services and characteristics
            AdapterService = null;
            ClientService = null;

            GetGattServices();
        }

        @Override
		public void onCharacteristicChanged(BluetoothGatt Gatt, BluetoothGattCharacteristic Characteristic)
        {
            // GATT Characteristic data changed
            String UUID = Characteristic.getUuid().toString().toUpperCase();

            //Helper.DebugPrint("CommBLE OnCharacteristicChanged - UUID=" + UUID);

            if (UUID.equals(BFCommBLE.J1939DataCharacteristicUUID) ||
                UUID.equals(BFCommBLE.J1708DataCharacteristicUUID) ||
                UUID.equals(BFCommBLE.ClientPacket1CharacteristicUUID) ||
                UUID.equals(BFCommBLE.ClientPacket2CharacteristicUUID) ||
                UUID.equals(BFCommBLE.ClientPacket3CharacteristicUUID))
                // Characteristic data changed, queue it for processing
				ReceiveAdapterData(Characteristic.getValue());
        }

        @Override
		public void onCharacteristicWrite(BluetoothGatt Gatt, BluetoothGattCharacteristic Characteristic, int Status)
        {
            IsWritingData = false;

            //Helper.DebugPrint("onCharacteristicWrite - IsWritingData="+IsWritingData);
        }

		@Override
		public void onDescriptorWrite(BluetoothGatt Gatt, BluetoothGattDescriptor Descriptor, int Status)
		{
			// Helper.DebugPrint("CommBLE OnDescriptorWrite - Descriptor=" + Descriptor.Characteristic.Uuid.ToString().ToUpper() + ", Status=" + Status.ToString());

			IsUpdatingDescriptor = false;
		}
	};

	//endregion

	//region GATT Services

	private void GetGattServices()
	{
		try
		{
			List<BluetoothGattService> GattServices = (List<BluetoothGattService>) BLEGatt.getServices();

			if (GattServices == null)
				return;

			// Loops through available GATT Services.
			for (BluetoothGattService GattService : GattServices)
			{
				String UUID = GattService.getUuid().toString().toUpperCase();

				if (UUID.equals(BFCommBLE.GenericAccessServiceUUID))
					GenericAccess = GattService;

				else if (UUID.equals(BFCommBLE.GenericAttributeServiceUUID))
					GenericAttribute = GattService;

				else if (UUID.equals(BFCommBLE.AdapterServiceUUID))
				{
					AdapterService = GattService;

					for (int i = 0; i < AdapterService.getCharacteristics().size(); i++) // count should be 2
					{
						BluetoothGattCharacteristic ServiceCharacteristic = AdapterService.getCharacteristics().get(i);
						String AdapterCharacteristicValue = ServiceCharacteristic.getUuid().toString().toUpperCase();

						if (AdapterCharacteristicValue.equals(BFCommBLE.J1939DataCharacteristicUUID))
							J1939DataCharacteristic = ServiceCharacteristic;

						else if (AdapterCharacteristicValue.equals(BFCommBLE.J1708DataCharacteristicUUID))
							J1708DataCharacteristic = ServiceCharacteristic;
					}
				}

				else if (UUID.equals(BFCommBLE.ClientServiceUUID))
				{
					ClientService = GattService;

					for (int i = 0; i < ClientService.getCharacteristics().size(); i++) // count should be 3
					{
						BluetoothGattCharacteristic ServiceCharacteristic = ClientService.getCharacteristics().get(i);
						String AdapterCharacteristicValue = ServiceCharacteristic.getUuid().toString().toUpperCase();

						if (AdapterCharacteristicValue.equals(BFCommBLE.ClientPacket1CharacteristicUUID))
							ClientPacket1Characteristic = ServiceCharacteristic;

						else if (AdapterCharacteristicValue.equals(BFCommBLE.ClientPacket2CharacteristicUUID))
							ClientPacket2Characteristic = ServiceCharacteristic;

						else if (AdapterCharacteristicValue.equals(BFCommBLE.ClientPacket3CharacteristicUUID))
							ClientPacket3Characteristic = ServiceCharacteristic;
					}
				}
			}

			if (AdapterService == null || ClientService == null)
			{
				String Message = "";

				if (AdapterService == null && ClientService == null)
					Message = "BlueFire and Client Services.";

				else if (AdapterService == null)
					Message = "BlueFire Service.";

				else if (ClientService == null)
					Message += "Client Service.";

				RaiseDataError("CommBLE GetGattServices, Failed to Discover " + Message);
				return;
			}

			// All characteristics have been discovered, enable notifications
			StartNotifications();
		} catch (Exception ex)
		{
			RaiseSystemError("CommBLE GetGattServices", ex);
		}
	}

	//endregion

	//region GATT Notifications

	protected void StartNotifications()
	{
		// Must start notifications from a timer to execute outside of Gatt event
		NotificationsTimer = new Timer();
		NotificationsTimer.schedule(new NotificationsTimedOut(), NotificationsInterval, NotificationsInterval);
	}

	// This is a one time shot
	private class NotificationsTimedOut extends TimerTask
	{
		@Override
		public void run()
		{
			StopNotificationsTimer();

			if (StartAllNotifications())
				// All characteristics notifications have been enabled, adapter is connected
				IsAdapterConnected = true;
		}
	};

	private void StopNotificationsTimer()
	{
        try
        {
            if (NotificationsTimer != null)
            {
                NotificationsTimer.cancel();
                NotificationsTimer.purge();
                NotificationsTimer = null;
            }
        }
        catch(Exception ex){}
	}

	private boolean StartAllNotifications()
	{
		IsCharacteristicNotification.clear();

		if (!StartStopNotifications(AdapterService, true))
			return false;

		if (!StartStopNotifications(ClientService, true))
			return false;

		return true;
	}

	private boolean StopNotifications()
	{
		if (!StartStopNotifications(AdapterService, false))
			return false;

		if (!StartStopNotifications(ClientService, false))
			return false;

		return true;
	}

	protected boolean StartAdapterNotifications()
	{
		return StartStopNotifications(AdapterService, true);
	}

	protected boolean StopAdapterNotifications()
	{
		return StartStopNotifications(AdapterService, false);
	}

	private boolean StartStopNotifications(BluetoothGattService Service, boolean Start)
	{
		try
		{
//			if (BTDevice == null || BLEGatt == null || Service == null || Service.getCharacteristics() == null)
//				return false;

			if (CurrentState != BluetoothProfile.STATE_CONNECTED)
				return false;

			int CharacteristicsCount = Service.getCharacteristics().size();

			for (int i = 0; i < CharacteristicsCount; i++)
			{
//				if (Service == null)
//					return false;

				BluetoothGattCharacteristic ServiceCharacteristic = Service.getCharacteristics().get(i);

//				if (Service == null || Service.getCharacteristics() == null || ServiceCharacteristic == null)
//					return false;

				if (Start)
				{
					if (!StartNotification(ServiceCharacteristic))
						return false;
				}
				else
				{
					if (!StopNotification(ServiceCharacteristic))
						return false;
				}

				WaitForDescriptorUpdate();
			}

			return true;

		} catch (Exception ex)
		{
			if (ex.getMessage().contains("null object reference"))
				return false;

			RaiseSystemError("CommBLE StartStopNotifications - Start=" + Start, ex);
			return false;
		}
	}

	protected boolean StartNotification(BluetoothGattCharacteristic ServiceCharacteristic)
	{
		try
		{
//			if (BTDevice == null || BLEGatt == null || ServiceCharacteristic == null)
//				return false;

			String CharacteristicUUID = ServiceCharacteristic.getUuid().toString().toUpperCase();

			IsCharacteristicNotification.remove(CharacteristicUUID);

//			if (BLEGatt == null || ServiceCharacteristic == null)
//				return false;

			BLEGatt.setCharacteristicNotification(ServiceCharacteristic, true);

//			if (BLEGatt == null || ServiceCharacteristic == null)
//				return false;

			BluetoothGattDescriptor Descriptor = ServiceCharacteristic.getDescriptor(UUID.fromString(BFCommBLE.ClientCharacteristicConfigUUID));

//			if (Descriptor == null)
//				return false;

			Descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);

//			if (BLEGatt == null)
//				return false;

			BLEGatt.writeDescriptor(Descriptor);

			IsCharacteristicNotification.add(CharacteristicUUID);

			return true;

		} catch (Exception ex)
		{
			if (ex.getMessage().contains("null object reference"))
				return false;

			RaiseSystemError("CommBLE.StartNotification", ex);
			return false;
		}
	}

	private boolean StopNotification(BluetoothGattCharacteristic ServiceCharacteristic)
	{
		try
		{
//			if (BTDevice == null || BLEGatt == null || ServiceCharacteristic == null)
//				return false;

			String CharacteristicUUID = ServiceCharacteristic.getUuid().toString().toUpperCase();

			if (!IsCharacteristicNotification.contains(CharacteristicUUID))
				return false;

//			if (BLEGatt == null || ServiceCharacteristic == null)
//				return false;

			BLEGatt.setCharacteristicNotification(ServiceCharacteristic, false);

//			if (BLEGatt == null || ServiceCharacteristic == null)
//				return false;

			BluetoothGattDescriptor Descriptor = ServiceCharacteristic.getDescriptor(UUID.fromString(BFCommBLE.ClientCharacteristicConfigUUID));

//			if (Descriptor == null)
//				return false;

			Descriptor.setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);

//			if (BLEGatt == null)
//				return false;

			BLEGatt.writeDescriptor(Descriptor);

			IsCharacteristicNotification.remove(CharacteristicUUID);

			return true;

		} catch (Exception ex)
		{
			if (ex.getMessage().contains("null object reference"))
				return false;

			RaiseSystemError("CommBLE.StopNotification", ex);
			return false;
		}
	}

	private void WaitForDescriptorUpdate()
	{
		IsUpdatingDescriptor = true;

		UpdateDescriptorTimeout = 0;
		while (IsUpdatingDescriptor && UpdateDescriptorTimeout < UpdateDescriptorTimeoutMax)
		{
			Helper.Sleep(10);
			UpdateDescriptorTimeout += 10;
		}
	}

	//endregion

	//region GATT Disconnection

	private boolean IsClosingGatt;
	private boolean IsDisconnectingGatt;

	private void DisconnectGatt()
	{
		try
		{
			if (BLEGatt == null)
				return;

			if (IsDisconnectingGatt)
				return;

			if (IsClosingGatt)
				return;

			IsDisconnectingGatt = true;

			// Disconnect the Gatt connection.
//			android.util.Log.d("BlueFire", "CommBLE DisconnectGatt, Disconnecting Gatt");
			BLEGatt.disconnect();

			// Must give Bluetooth time to close the Gatt connection or be reconnected.
			// Note, the Gatt connection is closed in the callback.
			int Timeout = blueFire.BleDisconnectWaitTime();
			while (true)
			{
				// Check if Gatt has been closed
				if (BLEGatt == null)
					break;

				// Check if Gatt has been reconnected
				if (CurrentState == BluetoothProfile.STATE_CONNECTED)
					break;

				// Check for timeout waiting for Gatt to close or be reconnected
				if (Timeout <= 0)
				{
//					android.util.Log.d("BlueFire", "CommBLE DisconnectGatt, Timeout waiting for GATT to close, State=" + CurrentState);
					CloseGatt(true);
					break;
				}
				Helper.Sleep(100);
				Timeout-= 100;
			}

			IsDisconnectingGatt = false;

		} catch (Exception ex)
		{
			RaiseSystemError("CommBLE DisconnectGatt", ex);

			IsDisconnectingGatt = false;
		}
	}

	private void CloseGatt(boolean WaitForClose)
	{
		try
		{
			if (BLEGatt == null)
				return;

			if (IsClosingGatt)
				return;

			IsClosingGatt = true;

			// Close the Gatt connection.
//			android.util.Log.d("BlueFire", "CommBLE CloseGatt, Closing Gatt, WaitForClose=" + WaitForClose);
			BLEGatt.close();

			// Give Bluetooth time to fully close the Gatt connection
			if (WaitForClose)
				Helper.Sleep(blueFire.BleDisconnectWaitTime());

			BLEGatt = null; // not a good idea in case the wait time is not enough

		} catch (Exception ex)
		{
			RaiseSystemError("CommBLE CloseGatt", ex);
		}

		IsClosingGatt = false;
	}

	//endregion

	//region Adapter Connected

	@Override
	protected void AdapterConnected()
	{
		StopDiscoveryTimer();

		super.AdapterConnected();
	}

	private void AdapterConnected(String Text, String DeviceAddress)
	{
		RaiseNotification("CommBLE", "Connected to " + Text + " adapter '" + DeviceAddress + "'");

		AdapterConnected();
	}

	@Override
	protected void AdapterNotConnected()
	{
		try
		{
			super.AdapterNotConnected();

		} catch (Exception ex)
		{
		}
	}

	//endregion

	//region Send Adapter Data

	@Override
	protected boolean SendAdapterData(byte[] DataBuffer, int DataLength)
	{
		try
		{
			if (!IsAdapterConnected)
				return false;

			super.SendAdapterData(DataBuffer, DataLength);

			byte PacketNo;
			int PacketLength;

			if (DataLength <= BFCommBLE.ClientPacket1Length)
			{
				PacketNo = BFCommBLE.ClientPacket1;
				PacketLength = BFCommBLE.ClientPacket1Length;
			}
			else if (DataLength <= BFCommBLE.ClientPacket2Length)
			{
				PacketNo = BFCommBLE.ClientPacket2;
				PacketLength = BFCommBLE.ClientPacket2Length;
			}
			else if (DataLength <= BFCommBLE.ClientPacket3Length)
			{
				PacketNo = BFCommBLE.ClientPacket3;
				PacketLength = BFCommBLE.ClientPacket3Length;
			}
			else
			{
				RaiseSystemError("CommBLE.SendClientData", new Exception("Invalid Client Data Length=" + DataLength));
				return false;
			}

			// Copy the data allowing room for the packet no.
			byte[] PacketData = new byte[PacketLength + 1]; // add a byte for the Packet No

			PacketData[0] = PacketNo;
			System.arraycopy(DataBuffer, 0, PacketData, 1, DataLength);

			IsWritingData = true;

			// Send the data to the adapter using the correct characteristic
			if (!WriteData(PacketData))
			{
				IsWritingData = false;
				return false;
			}

			// Wait for data to be sent
			if (!WaitForWrittenData())
			{
				IsWritingData = false;
				if (IsAdapterConnected && IsConnected)
					RaiseNotification("CommBLE.SendClientData", "BLE Write Timeout");

				return false;
			}

			IsWritingData = false;

			return true;
		}
		catch (Exception ex)
		{
			RaiseSystemError("CommBLE.SendClientData", ex);
			return false;
		}
	}

	// Write data to the adapter
	private boolean WriteData(byte[] PacketData)
	{
		try
		{
//			if (BLEGatt == null)
//				return false;

			// Send the data to the adapter using the correct characteristic
			switch (PacketData[0])
			{
				case BFCommBLE.ClientPacket1:
					ClientCharacteristic = ClientPacket1Characteristic;
					break;

				case BFCommBLE.ClientPacket2:
					ClientCharacteristic = ClientPacket2Characteristic;
					break;

				case BFCommBLE.ClientPacket3:
					ClientCharacteristic = ClientPacket3Characteristic;
					break;

				default:
					return false;
			}

//			if (ClientCharacteristic == null)
//				return false;

			ClientCharacteristic.setValue(PacketData);

			BLEGatt.writeCharacteristic(ClientCharacteristic);

			return true;
		}
		catch (Exception ex)
		{
			if (ex.getMessage().contains("null object reference"))
				return false;

			RaiseSystemError("CommBLE.WriteData", ex);
			return false;
		}
	}

	private boolean WaitForWrittenData()
	{
		WriteTimeout = 0;

		// Wait for data to be written
		while (IsWritingData && IsAdapterConnected && WriteTimeout < WriteTimeoutMax)
		{
			Helper.Sleep(10);
			WriteTimeout += 10;
		}

		// Check for a timeout writing data
		if (WriteTimeout >= WriteTimeoutMax)
			return false;

		// No timeout
		return true;
	}

	//endregion

	//region Receive Adapter Data

	@Override
	protected void ReceiveAdapterData(byte[] DataBuffer)
	{
		super.ReceiveAdapterData(DataBuffer);
	}

	//endregion

	//region Disconnect

	@Override
	protected boolean Disconnect()
	{
		return Disconnect(false);
	}

	@Override
	public boolean Disconnect(boolean WaitForDisconnect)
	{
		// Note, it is critical that Gatt is disconnected because if it is not the
		// adapter will not advertise itself.

		try
		{
			if (!super.Disconnect(WaitForDisconnect))
			{
				DisconnectGatt();
				return false;
			}

			IsAdapterConnected = false;

			if (ConnectionState == ConnectionStates.Disconnecting || this.ConnectionState == ConnectionStates.Disconnected)
			{
				DisconnectGatt();
				return false;
			}

			UpdateState(ConnectionStates.Disconnecting);

			if (BTAdapter == null)
			{
				DisconnectGatt();
				AdapterNotConnected();
				return false;
			}

			// Disconnect Bluetooth
			// Note, this must be done in a thread to allow the Disconnecting state
			// to be processed by the event handlers.

			this.WaitForDisconnect = WaitForDisconnect;
			DisconnectThread = new DisconnectThreading();
			DisconnectThread.start();

			return true;

		} catch (Exception ex)
		{
			DisconnectGatt();
			RaiseSystemError("CommBLE.Disconnect", ex);
			return false;
		}
	}

	private class DisconnectThreading extends Thread
	{
		public void run()
		{
			try
			{
				// Stop scanning
				StopScan();

				// Stop discovering
				StopDiscoveryTimer();

				// Stop adapter sending notifications
				StopNotifications();

				ClearInputPackets();

				// Disconnect Gatt
				DisconnectGatt();

				if (BTDevice != null)
					BTDevice = null;

				CheckWaitForDisconnect(WaitForDisconnect);

			} catch (Exception ex)
			{
				DisconnectGatt();
				RaiseSystemError("CommBLE.DisconnectThreading", ex);
			}
		}
	}

	//endregion

	//region Dispose

	@Override
	public void Dispose()
	{
		super.Dispose();
	}

	//endregion
}
