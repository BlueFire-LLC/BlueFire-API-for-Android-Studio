package com.bluefire.apidemo;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;

import com.bluefire.api.BFCommBT2;
import com.bluefire.api.BlueFire;
import com.bluefire.api.ConnectionStates;
import com.bluefire.api.Helper;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

public class CommBT2 extends Comm
{
	//region Declaratives

	private BluetoothSocket Socket = null;
    
    private InputStream InStream = null;
    private OutputStream OutStream = null;
	
	private boolean BTpairing = false;
	private boolean BTreceiving = false;
	private boolean BTpairingError = false;
	private boolean BTfoundDevice = false;
    private boolean BTreceiverUnregistered = false;

	private Timer DiscoveryTimer;
	
	// Get Data

	private int DataByte;
	private int BufferIndex;
	private byte[] StreamBuffer = new byte[BFCommBT2.MaxMessageSize]; // length bytes + max message data + checksum

	private int DataLength;
	private int MessageLength;

	//endregion

	//region Constructor

	public CommBT2(Context context, Handler connectionHandler, BlueFire blueFire)
	{
		super(context, connectionHandler, blueFire);

        BFDeviceName = BFCommBT2.AdapterName;
	}

	//endregion

	//region Initialize

    @Override
	protected void Initialize()
    {
		super.Initialize();

		BTpairing = false;
        BTreceiving = false;
        BTfoundDevice = false;
        BTpairingError = false;
        BTreceiverUnregistered = false;

        BTreceiving = true; // ignore discovery timeout
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
            // Check for a paired last connected device
            if (IsLastConnectedDevice())
                return true;

            // Check for not finding a last connected device
            if (IsLastConnectedAdapterSet())
                return false;

            // Check for a paired BlueFire device
            if (IsBlueFireDevice())
                return true;
			
			// Last device is not connected, start the discovery process.
			// Note, this is faster than trying to connect to each paired device.
			if (ConnectionState == ConnectionStates.Connecting || ConnectionState == ConnectionStates.Reconnecting)
				StartDiscovery();
			
			return true;

		} catch (Exception ex) 
		{
			RaiseSystemError ("CommBT2.StartConnection", ex);
			return false;
		}
	}

    private Boolean IsLastConnectedDevice()
    {
        // Get all paired devices
		Set<BluetoothDevice> BTpairedDevices = BTAdapter.getBondedDevices();

        // Check for last connected device
		for (BluetoothDevice BTdevice : BTpairedDevices) 
			 if (IsValidAdapter(BTdevice.getName(), BTdevice.getAddress()))
				return ConnectDevice(BTdevice); // try to connect to the adapter

        return false;
    }

    private Boolean IsBlueFireDevice()
    {
		// Get all paired devices
		Set<BluetoothDevice> BTpairedDevices = BTAdapter.getBondedDevices();

        // Try to connect to the adapter
		for (BluetoothDevice BTdevice : BTpairedDevices) 
			if (BTdevice.getName().equals(BFDeviceName))
				if (ConnectDevice(BTdevice)) // try to connect to the adapter and retry
					return true;

        return false;
    }

	// Connect to the Bluetooth device and Pair if needed
	private boolean ConnectDevice(BluetoothDevice BTdevice) 
	{
		try 
		{
			BTpairing = true; // ignore discovery timeout just in case pairing is needed
			
			// Get a BluetoothSocket to connect with the given BluetoothDevice
			try {
				if (Socket != null)
				{
					Socket.close();
					InStream = null;
					OutStream = null;
				}
				
                if (blueFire.UseInsecureConnection())
                    Socket = BTdevice.createInsecureRfcommSocketToServiceRecord(BFCommBT2.BT_UUID);
                else
				    Socket = BTdevice.createRfcommSocketToServiceRecord(BFCommBT2.BT_UUID);
			} 
			catch (Exception e)
			{
		    	BTpairing = false; 
				return false;
			}
			// Cancel discovery because it will slow down the connection
			if (BTAdapter.isDiscovering())
				BTAdapter.cancelDiscovery();

            // Connect the device through the socket. This will block until it succeeds or throws an exception.
            // Note, This will initiate pairing if needed.
            while (!Socket.isConnected())
            {
				Helper.Sleep(100);
                try
                {
					// Note, Socket.Connect can take from 1 to 3 seconds.
					// Have witnessed retries of more than 20.
					if (Socket != null)
                    	Socket.connect();
				}
                catch (Exception ex2)
                {
					BTpairing = false;
					return false;
                }
				if (Socket == null) // disconnected
				{
					BTpairing = false;
					return false;
				}
            }
   			InStream = Socket.getInputStream();
			OutStream = Socket.getOutputStream();

			DeviceName = BTdevice.getName();
			DeviceAddress = BTdevice.getAddress();

	    	BTpairing = false;

			AdapterConnected();
	
			return true; // connected
		} 
		catch (Exception ex) 
		{
			RaiseSystemError ("CommBT2.ConnectDevice", ex);
			BTpairingError = true;
			return false;
		}
	}

	//endregion

	//region Discovery

	private void StartDiscovery()
	{
		UpdateState(ConnectionStates.Discovering);

		// Only allow discovery a set amount of time to find the adapter
		DiscoveryTimer = new Timer();
		DiscoveryTimer.schedule(new DiscoveryTimedOut(), blueFire.DiscoveryTimeout(), blueFire.DiscoveryTimeout());

		BTpairing = false;
		BTreceiving = true;
		BTpairingError = false;
		BTreceiverUnregistered = false;

		IntentFilter ActionFoundFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
		ActionFoundFilter.addAction(BluetoothDevice.ACTION_NAME_CHANGED);
		Context.registerReceiver(DiscoveryReceiver, ActionFoundFilter);

		// Start discovery.
		// Note, discovery of the device will be handled in the Broadcast Receiver.
		BTAdapter.startDiscovery();

		// Block until discovery finishes
		while (ConnectionState == ConnectionStates.Discovering)
			Helper.Sleep(100);
	}

	private class DiscoveryTimedOut extends TimerTask
	{
        @Override
        public void run()
        {
            // Check for BT receiving
            if (BTreceiving)
            {
            	BTreceiving = false;
                return;
            }
            // Check for BT pairing
            if (BTpairing)
            {
            	BTpairing = false;
                return;
            }

            // Stop the discovery timer (but don't nuke it)
			if (DiscoveryTimer != null) // just to be safe ...
				DiscoveryTimer.cancel();

            // Unregister the discovery receiver
			CancelDiscovery();

            // Did not find an adapter, display a message to the user.
            // Note, all messages to the user must be done here in this timer thread instead of in
            // the receiver.
     		if (!BTpairingError)
				AdapterNotConnected();
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
		catch (Exception ex){}
	}

	// Return from Bluetooth discovery broadcasts

    private void CancelDiscovery()
    {
		BTAdapter.cancelDiscovery();
		Helper.Sleep(100); // give time for all receiver events to fire

		while (BTAdapter.isDiscovering())
			Helper.Sleep(100); // give time for all receiver events to fire

		UnregisterReceiver();
     }

	//endregion

	//region Broadcast Receiver

	protected final BroadcastReceiver DiscoveryReceiver = new BroadcastReceiver() {

		public void onReceive(Context context, Intent intent) {

			try
			{
				if (BTreceiverUnregistered)
					return;

				String action = intent.getAction();

				// Check for discovery finding a device.
				// Note, if there are multiple devices, discovery will find the one that is connected.
				if ((action.equals(BluetoothDevice.ACTION_FOUND) || action.equals(BluetoothDevice.ACTION_NAME_CHANGED)) && !BTfoundDevice)
				{
					BTreceiving = true; // ignore discovery timeout

					// Get the Bluetooth device
					BluetoothDevice BTdevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

					if (BTdevice == null)
						return;

					String BTName = BTdevice.getName();
					if (BTName == null)
						return;

					// Check for a BlueFire adapter
					if (BTName.equals(BFDeviceName))
					{
						// Yes is is, stop the receiver and try to connect to it
						BTfoundDevice = true;

						CancelDiscovery();

						ConnectDevice(BTdevice);

	                    if (!IsConnected) // needed for bug when reconnecting with no pairing
	                        ConnectDevice(BTdevice);

	                    BTfoundDevice = false;
					}
				}
			}
			catch (Exception ex)
			{
				CancelDiscovery();
				RaiseSystemError ("CommBT2.BroadcastReceiver", ex);
				BTpairingError = true;
			}
		}
	};

	private void UnregisterReceiver()
	{
		// This is the only way to handle receiver already unregistered
   		try
   		{
		    BTreceiverUnregistered = true;
		    Context.unregisterReceiver(DiscoveryReceiver);
		}
   		catch(Exception e) {}
	}

	//endregion

	//region Adapter Connected

	@Override
	protected void AdapterConnected()
	{
		StopDiscoveryTimer();

		super.AdapterConnected();
	}

	//endregion

	//region Is Data Available

	@Override
	public boolean IsDataAvailable()
    {
        try
        {
            // Check for any queued packets
            if (super.IsDataAvailable())
                return true;

	        // Check for disconnection
	        if (InStream == null)
	            return false;

	        // Check for any input packets
	        if (InStream.available() == 0)
	            return false;

	        byte[] DataBuffer = GetPacketData();
	        if (DataBuffer.length == 0)
	            return false;

	        // Add input packet to the queue
			ReceiveAdapterData(DataBuffer);

	        return true;
		}
		catch (Exception ex)
		{
			RaiseSystemError ("CommBT2.IsDataAvailable", ex);
			return false;
		}
    }

	//endregion

	//region Get Packet Data

	// Get message data from the adapter
	// The data stream looks like:
	// Length (2) - Length of Requested Data, not Checksum
	// Length Check (1)
	// Message Data (n)
	// Data Checksum (1)

	private byte[] GetPacketData()
	{
        DataLength = 0;
        BufferIndex = 0;
        MessageLength = -1;
        byte[] MessageData;

        try
        {
            // Wait until an entire message is received from the adapter
            while (true)
            {
                if (InStream == null) // disconnected
                    return new byte[0];

                if (InStream.available() > 0)
                    DataByte = InStream.read();
                else
                    DataByte = -1;

                if (DataByte < 0)
                    if (CheckDataTimeOut())
                    {
						RaiseDataTimeout();
                        return new byte[0];
                    }
                    else
                        continue;

                // Get the data byte
                StreamBuffer[BufferIndex] = (byte)DataByte;

                // Check for receiving the length bytes
                if (BufferIndex == (BFCommBT2.NofLengthBytes - 1))
                {
                    DataLength = GetDataLength(StreamBuffer);
                    if (DataLength == 0)
                        return new byte[0];
                    MessageLength = BFCommBT2.NofLengthBytes + DataLength + 1; // length bytes + message data + checksum
                }

                // Check for receiving the entire message
                else if (BufferIndex == (MessageLength - 1))
                {
                    MessageData = new byte[MessageLength];
                    System.arraycopy(StreamBuffer, 0,MessageData, 0, MessageLength);
                    return MessageData;
                }

                BufferIndex++;
            }
        }
        catch (Exception ex)
        {
            if (InStream == null) // disconnected
                return new byte[0];
            else
            {
    			RaiseSystemError ("CommBT2.GetPacketData", ex);
                return new byte[0];
            }
        }
	}

	//endregion

	//region Send Adapter Data

	@Override
	protected boolean SendAdapterData(byte[] DataBuffer, int DataLength) throws Exception
    {
        return SendAdapterData(DataBuffer, DataLength, true);
    }

    // Send Client Data to the BlueFire
    private boolean SendAdapterData(byte[] DataBuffer, int DataLength, boolean AddPrefix) throws Exception
    {
        try
        {
            if (OutStream == null || DataBuffer.length == 0)
                return false;

            super.SendAdapterData(DataBuffer, DataLength);

            // Add message prefix and suffix
            if (AddPrefix)
            {
                byte[] SendBuffer = new byte[BFCommBT2.MessagePrefix.length + DataLength + BFCommBT2.MessageSuffix.length];
                System.arraycopy(BFCommBT2.MessagePrefix, 0, SendBuffer, 0, BFCommBT2.MessagePrefix.length);
                System.arraycopy(DataBuffer, 0, SendBuffer, BFCommBT2.MessagePrefix.length, DataBuffer.length);
                System.arraycopy(BFCommBT2.MessageSuffix, 0, SendBuffer, BFCommBT2.MessagePrefix.length + DataLength, BFCommBT2.MessageSuffix.length);
                // Send the data
                OutStream.write(SendBuffer, 0, SendBuffer.length);
            }
            else // Send the data without a prefix and suffix
                OutStream.write(DataBuffer, 0, DataLength);

            return true;
        }
        catch (IOException ex) { } // This occurs when going out of BT range
        catch (Exception ex)
        {
			RaiseSystemError("CommBT2.SendClientData", ex);
        }
        return false;
    }

	//region Receive Adapter Data

	@Override
	protected void ReceiveAdapterData(byte[] DataBuffer)
	{
		super.ReceiveAdapterData(DataBuffer);
	}

	//endregion

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
        if (!super.Disconnect(WaitForDisconnect))
        	return false;

		UpdateState(ConnectionStates.Disconnecting);

        if (BTAdapter == null)
        {
        	AdapterNotConnected();
            return false;
        }

		try
		{
			Context.unregisterReceiver(DiscoveryReceiver);
		} 
		catch (Exception e) {}
		
		try 
		{
			if (DiscoveryTimer != null)
			{
				DiscoveryTimer.cancel();
				DiscoveryTimer = null;
			}

			if (InStream != null)
			{
				InStream.close();
				InStream = null;
			}
			if (OutStream != null)
			{
				OutStream.close();
				OutStream = null;
			}
			if (Socket != null)
			{
				Socket.close();
				Socket = null;
			}
			
            // Unregister the discovery receiver
			CancelDiscovery();

			CheckWaitForDisconnect(WaitForDisconnect);
			
			return true;
		} 
		catch (Exception ex) 
		{ 
			RaiseSystemError("CommBT2.Disconnect", ex);
			return false;
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
