package com.bluefire.apidemo;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.os.Handler;
import android.os.Message;

import com.bluefire.api.BFComm;
import com.bluefire.api.BlueFire;
import com.bluefire.api.ConnectionStates;
import com.bluefire.api.Const;
import com.bluefire.api.Helper;

import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;

public class Comm extends BFComm
{
    //region Declaratives

    // Android specific
    protected Context Context;
    protected Handler ConnectionHandler;

    protected String DeviceName = "";
    protected String DeviceAddress = "";

    protected String BFDeviceName = "";

    private boolean IsClearingInputPackets;
    private Queue<byte[]> InputPackets = new LinkedList<byte[]>();;

    private boolean IsQueueingData;
    protected boolean IsSendingData;
    private boolean IsClearingSendBuffer;
    private Queue<byte[]> SendBuffer = new LinkedList<byte[]>();

    private Timer DisconnectTimer;
    private int WaitForDisconnectTime = 8 * Const.OneSecond;

    private String CommMessage = "";
    protected ConnectionStates ConnectionState = ConnectionStates.NA;
    private ConnectionStates CurrentState = ConnectionStates.NotConnected; // needs to be NotConnected

    private Queue<Message> StateQueue = new LinkedList<Message>();
    private UpdateStateThreading UpdateStateThread;

    protected BluetoothAdapter BTAdapter;

    protected BlueFire blueFire;

    //endregion

    //region Valid Adapter

    protected Boolean IsValidAdapter()
    {
        return IsValidAdapter(DeviceName, DeviceAddress);
    }

    protected boolean IsValidAdapter(String BTDeviceName, String BTDeviceAddress)
    {
        // Check for a last connected device
        if (IsLastConnectedAdapterSet() && !BTDeviceAddress.equals("") && BTDeviceAddress.equals(blueFire.AdapterId()))
            return true;

            // Check for not finding a last connected device
        else if (IsLastConnectedAdapterSet())
            return false;

            // Check for a BlueFire device
        else if (BTDeviceName.equals(BFDeviceName))
            return true;

        return false;
    }

    //endregion

    //region Last Connected Adapter

    // IsLastConnectedId Property
    protected boolean IsLastConnectedAdapter()
    {
        return IsLastConnectedAdapter(DeviceAddress);
    }

    // IsLastConnectedId Property
    protected boolean IsLastConnectedAdapter(String BTDeviceAddress)
    {
        return ((blueFire.ConnectToLastAdapter() || blueFire.SecureAdapter()) && BTDeviceAddress.equals(blueFire.AdapterId()));
    }

    protected boolean IsLastConnectedAdapterSet()
    {
        return ((blueFire.ConnectToLastAdapter() || blueFire.SecureAdapter()) && !blueFire.AdapterId().equals(""));
    }

    //endregion

    //region Timeout

    private boolean AdapterIsBusy;
    private boolean IsDataTimedOut = false;
    private long PrevDataTime = Helper.GetDateTimeNowMs();

    private void ResetTimeOut()
    {
        IsDataTimedOut = false;

        PrevDataTime = Helper.GetDateTimeNowMs();
    }

    protected boolean CheckDataTimeOut()
    {
        // No data, check for timeout
        // Note, only timeout once a cycle
        if (!IsDataTimedOut)
        {
            // Check if the adapter is busy
            if (blueFire.BusyWaitTime > 0)
            {
                if (!AdapterIsBusy)
                {
                    AdapterIsBusy = true;
                    ResetTimeOut();
                }
            }
            else
            {
                if (AdapterIsBusy)
                {
                    AdapterIsBusy = false;
                    ResetTimeOut();
                }
            }

            // Check for a data timeout
            if (Helper.TimeDiff(PrevDataTime) > blueFire.DataTimeoutInterval)
            {
                IsDataTimedOut = true;
                return true;
            }
        }
        return false;
    }

    //endregion

    //region Constructor

    public Comm(Context context, Handler connectionHandler, BlueFire blueFire)
    {
    	Context = context;

    	ConnectionHandler = connectionHandler;

    	this.blueFire = blueFire;

        UpdateStateThread = new UpdateStateThreading();
        UpdateStateThread.start();
    }

    //endregion

    //region Bluetooth

    private boolean EnableBluetooth()
    {
    	try
    	{
	        // Initializes a Bluetooth adapter.
	    	BTAdapter = BluetoothAdapter.getDefaultAdapter();
	
	        if (BTAdapter == null) 
	             return false;
	        
	        if (!BTAdapter.isEnabled())
	    		return TurnBluetoothOn();
	
	        return true;
	    }
	    catch (Exception ex)
	    {
	        RaiseSystemError("CommBLE EnableBluetooth", ex);
	        return false;
	    }
    }

    protected boolean TurnBluetoothOn()
	{
		try 
		{
			if (BTAdapter == null)
				return false;
			
			// Bluetooth is not on, turn it on
			BTAdapter.enable();

            // Wait for Bluetooth to turn on
            int WaitTime = 3 * Const.OneSecond;
            while (!BTAdapter.isEnabled())
            {
                Helper.Sleep(100);
                WaitTime -= 100;
                if (WaitTime <= 0)
                    break;
            }
            if (!BTAdapter.isEnabled())
                return false;

            Helper.Sleep(100); // allow bluetooth to initialize itself
            
            return true;
			
		} catch (Exception ex) 
		{
			RaiseSystemError ("CommBT2.TurnBluetoothOn", ex);
			return false;
		}
	}

    protected void TurnBluetoothOff()
    {
        if (BTAdapter == null)
            return;

        BTAdapter.disable();

        // Wait for Bluetooth to turn off
        int WaitTime = 3 * Const.OneSecond;
        while (BTAdapter.isEnabled())
        {
            Helper.Sleep(100);
            WaitTime -= 100;
            if (WaitTime <= 0)
                break;
        }

        Helper.Sleep(100); // allow bluetooth to initialize itself
    }

    private boolean RecycleBluetooth()
    {
        TurnBluetoothOff();

        if (!TurnBluetoothOn())
            return false;
        else
            Helper.Sleep(3 * Const.OneSecond); // give Bluetooth time to find the adapter

        return true;
    }

    //endregion

    //region Initialization

    // Override in CommBLE/CommBT2
    protected void Initialize()
    {
    }

    private void InitializeData(boolean isReconnecting)
    {
        ClearSendBuffer();
        ClearInputPackets();

        PreviousId = "";

		IsConnected = false;
        IsSendingData = false;

        blueFire.BusyWaitTime = 0;

        if (!isReconnecting)
        {
            IsReconnecting = false;
            ReconnectAttempt = 0;
        }
     }

    private void InitializeState()
    {
        StateQueue.clear();
    }

    private void InitializeComm()
    {
        UpdateState(ConnectionStates.Initializing);

        Initialize();

        UpdateState(ConnectionStates.Initialized);
    }

    //endregion

    //region Connection

    // Connect method
    @Override
    public boolean Connect()
    {
        return Connect(false);
    }
    private boolean Connect(boolean isReconnecting)
    {
        InitializeData(isReconnecting);

        if (!isReconnecting)
            InitializeState();

        InitializeComm();

        if (!isReconnecting)
            UpdateState(ConnectionStates.Connecting);
        else
            UpdateState(ConnectionStates.Reconnecting);

        if (!StartConnection()) // this will block
            AdapterNotConnected();

        return (ConnectionState == ConnectionStates.Connected);
    }

    // Override in CommBLE/CommBT2
    protected boolean StartConnection()
	{
        return InitializeConnection();
    }

    protected boolean InitializeConnection()
    {
        IsConnected = false;
        IsConnecting = true;

        ClearInputPackets();

        if (!EnableBluetooth())
            return false;

        return true;
    }

    // Override in CommBLE/CommBT2
    protected void AdapterConnected()
    {
    	IsConnecting = false;
    	IsConnected = true;

    	AdapterId = DeviceAddress;

        ResetTimeOut();

        // Check for reconnecting
        if (IsReconnecting)
            AdapterReconnected();
        else
       	    UpdateState(ConnectionStates.Connected);
    }

    // Override in CommBLE/CommBT2
    protected void AdapterNotConnected()
    {
    	try
    	{
	    	IsConnecting = false;
	    	IsConnected = false;

            Disconnect();

            if (IsReconnecting)
                Reconnect();
            else
                UpdateState(ConnectionStates.NotConnected);
    	}
		catch (Exception ex){}
	}

	//endregion

    //region Reconnection

    public boolean Reconnect()
    {
        // Check for no reconnect attempts or non-compatible adapter
        if (blueFire.MaxReconnectAttempts() == 0 || !blueFire.IsCompatible())
        {
            StopReconnection(true);
            return false;
        }

        // Check for exceeding reconnect attempts
        if (ReconnectAttempt >= blueFire.MaxReconnectAttempts())
        {
            AdapterNotReconnected();
            return false;
        }

        // Set for reconnecting
        IsReconnecting = true;

        ReconnectAttempt++;

//		android.util.Log.d("BlueFire", "Reconnect, Attempt=" + ReconnectAttempts);

        // Check for recycling Bluetooth.
        // Note, must be before Dispose.
        if (ReconnectAttempt == (blueFire.BluetoothRecycleAttempt()))
        {
            if (!RecycleBluetooth())
                ReconnectAttempt = blueFire.MaxReconnectAttempts();
        }

        // Try to reconnect
        return Connect(IsReconnecting);
    }

    public void StopReconnection(boolean DisconnectAdapter)
    {
        IsReconnecting = false;

        if (DisconnectAdapter)
            Disconnect();
    }

    private void AdapterReconnected()
    {
        StopReconnection(false);

        IsReconnecting = false;

        UpdateState(ConnectionStates.Reconnected);
    }

    private void AdapterNotReconnected()
    {
        StopReconnection(true);

        ConnectionStates State;

        if (IsConnecting)
            State = ConnectionStates.NotConnected;
        else
            State = ConnectionStates.NotReconnected;

        AdapterNotConnected();

        UpdateState(State);
    }

    //endregion

    //region Disconnection

    // Disconnect Method
    // Override in CommBLE/CommBT2
    protected boolean Disconnect()
    {
        return Disconnect(false);
    }
    @Override
    public boolean Disconnect(boolean WaitForDisconnect)
    {
        try
        {
            if (ConnectionState == ConnectionStates.Disconnecting || ConnectionState == ConnectionStates.Disconnected)
                return false;

            InitializeData(IsReconnecting);

            return true;
        }
        catch (Exception ex)
        {
            RaiseSystemError("Comm.Disconnect", ex);
            return false;
        }
    }

    protected void CheckWaitForDisconnect(boolean WaitForDisconnect)
    {
        if (WaitForDisconnect)
        {
            DisconnectTimer = new Timer();
            DisconnectTimer.schedule(new DisconnectTimedOut(), WaitForDisconnectTime, Long.MAX_VALUE);
        }
        else
            UpdateState(ConnectionStates.Disconnected);
    }

    private class DisconnectTimedOut extends TimerTask
    {
        @Override
        public void run()
        {
            UpdateState(ConnectionStates.Disconnected);
        }
    };

    //endregion

    //region Get Data

    @Override
    public boolean IsDataAvailable()
	{
	    if (InputPackets.size() > 0)
	        return true;

        if (CheckDataTimeOut())
            RaiseDataTimeout();

	    return false;
	}

    // Get message data from the adapter
    // The data stream looks like:
    //      Length (2) - Length of Requested Data, not Checksum
    //      Length Checksum (1)
    //      Message Data (n)
    //      Data Checksum (1)
    //
    @Override
    public byte[] GetData()
    {
        try
        {
            if (!IsDataAvailable())
                return new byte[0];

            byte[] DataBuffer = null;
            try
            {
               DataBuffer = (byte[]) InputPackets.poll();
            }
            catch (Exception ex){ }

            if (DataBuffer == null) // just in case ...
                return new byte[0];

            // Check for an invalid length and ignore the packet
            if (!IsValidDataLength(DataBuffer))
                return new byte[0];
            
            // Check for a checksum error and ignore the packet
            byte[] MessageData = GetMessageData(DataBuffer);
            if (MessageData == null)
                return new byte[0];

            // Return the message data
            return MessageData;
        }
        catch (Exception ex)
        {
            RaiseDataError("Comm.GetData", ex);
            return null;
        }
    }

    // Get the data length from the adapter message
    protected int GetDataLength(byte[] DataBuffer)
    {
        try
        {
            if (DataBuffer.length < 3)
                return -1;

            // Get and check the message length
        	int Length1 = DataBuffer[0];
            int Length2 = DataBuffer[1];
            int Length3 = DataBuffer[2];

            // Check the length bytes
            if (Length3 != (byte)(Length1 ^ Length2))
            {
                // Don't log this error because it can occur quite often when the data rate is high
                //Helper.LogMessage("Comm.GetDataLength - Packet Data Length Error - Invalid Length");
                return -1;
            }

            // Get the message data length
            int DataLength = (Length1 << 8) | Length2;
            if (DataLength < 0)
            {
                // Don't log this error because it can occur quite often when the data rate is high
                //Helper.LogMessage("Comm.GetDataLength - Packet Data Length Error - Negative Length");
                return -1;
            }

            return DataLength;
        }
        catch (Exception ex)
        {
            RaiseDataError("Comm.GetDataLength", ex);
            return -1;
        }
    }

    // Get the data length from the adapter message
    protected boolean IsValidDataLength(byte[] DataBuffer)
    {
        try
        {
            // Get the message data length
        	int DataLength = GetDataLength(DataBuffer);
            if (DataLength < 0)
                return false; 

            // Validate the data length.
            // StartIndex(3) + Data Length + Checksum(1) must be <= to DataBuffer Length
            if ((StartIndex + DataLength + 1) > DataBuffer.length)
            {
                // Don't log this error because it can occur quite often when the data rate is high
                //Helper.LogMessage("Comm.IsValidDataLength - Packet Data Length Error - Length Too Long, DataLength=" + DataLength + ", BufferLength=" + DataBuffer.Length);
                return false;
            }

            return true;
        }
        catch (Exception ex)
        {
            RaiseDataError("Comm.IsValidDataLength", ex);
            return false;
        }
    }

    // Get the message data from the adapter message
    private byte[] GetMessageData(byte[] DataBuffer)
    {
        try
        {
            // Get the data length
            int DataLength = GetDataLength(DataBuffer);

            // Validate checksum on the message data
            byte Checksum = Helper.CalcChecksum(DataBuffer, StartIndex, DataLength);
            if (Checksum != DataBuffer[StartIndex + DataLength])
            {
                return null;
            }

            // Return the message data (excluding the checksum)
            byte[] MessageData = new byte[DataLength];
            System.arraycopy(DataBuffer, StartIndex, MessageData, 0, DataLength);

            return MessageData;
        }
        catch (Exception ex)
        {
             RaiseDataError("Comm.GetMessageData", ex);
             return null;
        }
    }

    //endregion

    //region Send Adapter Data

    @Override
    public void SendData(byte[] DataBuffer)
    {
        int DataIndex = 0;
        int DataLength;
        byte[] SendBuffer;

        try
        {
            if (DataBuffer.length == 0)
                return;

            // Get the length of data to send
            DataLength = DataBuffer.length;

            // Build the data stream to send the data.
            // Include space for Length and Checksum.
            SendBuffer = new byte[2 + DataLength + 1]; // Length Bytes + Data + CheckSum

            // Include the data length in the send buffer.
            // Start calculating the checksum.
            byte[] LengthBytes = Helper.Short2Bytes(DataLength);
            SendBuffer[DataIndex] = LengthBytes[0];
            SendBuffer[DataIndex + 1] = LengthBytes[1];
            int Checksum = Helper.JavaUnsignedByte(SendBuffer[DataIndex]) + Helper.JavaUnsignedByte(SendBuffer[DataIndex + 1]); // checksum
            DataIndex += 2;

            // Include the data in the send buffer
            for (int i = 0; i < DataLength; i++)
            {
                SendBuffer[DataIndex] = DataBuffer[i];
                Checksum += Helper.JavaUnsignedByte(DataBuffer[i]);
                DataIndex++;
            }

            // Calculate the Checksum and store it at the end of the send buffer
            SendBuffer[DataIndex] = (byte)(Helper.GenerateChecksum(Checksum));

            // Send the request for data
            AddSendBuffer(SendBuffer);
        }
        catch (Exception ex)
        {
            ClearSendBuffer();
            RaiseDataError("Comm.SendPID", ex);
           return;
        }
    }

    protected void AddSendBuffer(byte[] DataBuffer)
    {
        try
        {
            while (IsSendingData || IsClearingSendBuffer)
                Helper.Sleep(10);

            if (SendBuffer == null) // Comm null while waiting for IsSendingData
                return;

            IsQueueingData = true;

            SendBuffer.add(DataBuffer);
        }
        catch (Exception ex)
        {
            int BufferCount = SendBuffer.size();
            ClearSendBuffer();
            RaiseDataError("Comm.AddSendBuffer, Buffer Size=" + BufferCount, ex);
        }

        IsQueueingData = false;
    }

    private void ClearSendBuffer()
    {
        IsClearingSendBuffer = true;

        try
        {
            SendBuffer.clear();
        }
        catch (Exception ex) { } // Android can throw an exception

        IsClearingSendBuffer = false;
    }

    // Check if there is data in the buffer and send it
    @Override
    public void CheckSendBuffer()
    {
		try
		{
	        if (!IsConnected)
	            return;
	
	        // Ignore if writing data to the buffer
	        if (IsSendingData)
	            return;
	
	        if (IsClearingSendBuffer)
	            return;
	
	        if (blueFire.BusyWaitTime > 0)
	            return;
	
	        if (SendBuffer.isEmpty())
	            return;

            while (IsQueueingData)
                Helper.Sleep(10);

            if (SendBuffer == null) // Comm null while waiting for IsQueueingData
                return;

            IsSendingData = true;

	        byte[] DataBuffer = (byte[])SendBuffer.poll();
	        
           // Ignore if not connected or no data
           if (!IsConnected || DataBuffer == null || DataBuffer.length == 0)
           {
               IsSendingData = false;
               return;
           }

            SendAdapterData(DataBuffer, DataBuffer.length);
       }
        catch (IOException ex) { } // This occurs when going out of BT range
        catch (Exception ex)
       {
           RaiseDataError("Comm.CheckSendBuffer", ex);
       }

       IsSendingData = false;
    }

    // Send Adapter Data Method
    protected boolean SendAdapterData(byte[] DataBuffer, int DataLength) throws Exception
    {
        if (!IsConnected)
            return false;
        
        return true;
    }

    //endregion

    //region Receive Adapter Data

    // Queue the input data for processing.
    // The data stream looks like:
    // Length (2) - Length of Requested Data, not Checksum
    // Length Checksum (1)
    // Message Data (n)
    // Data Checksum (1)
    protected void ReceiveAdapterData(byte[] DataBuffer)
    {
        try
        {
            if (DataBuffer.length == 0) // should not occur but just in case ...
                return;

            // Reset data timeout
            ResetTimeOut();

            blueFire.BusyWaitTime = 0;

            // Queue the data for processing later
            while (IsClearingInputPackets)
                Helper.Sleep(10);

            if (InputPackets == null) // Comm null while waiting for IsSendingData
                return;

            // Limit number of input packets
            if (InputPackets.size() > blueFire.MaxCommBuffer())
                ClearInputPackets();

            try
            {
                InputPackets.add(DataBuffer);
            }
            catch (Exception ex) {}
        }
        catch (Exception ex)
        {
            ClearInputPackets();
        }
    }

    protected void ClearInputPackets()
    {
        IsClearingInputPackets = true;

        try
        {
            InputPackets.clear();
        }
        catch (Exception ex){ } // Android can throw an exception

        IsClearingInputPackets = false;
    }

    //endregion

    //region Raise Events

    protected void RaiseNotification(String Location, String Message)
    {
        RaiseNotification(Location, Message, false);
    }
    protected void RaiseNotification(String Location, String Message, boolean Force)
    {
        if (!Location.equals(""))
            Message = Location + " - " + Message;

        if (!blueFire.NotificationsOn() && !Force)
            return;

        // Save previous connection state
        ConnectionStates CurrentState = ConnectionState;

        // Notify the app
        UpdateState(ConnectionStates.Notification, Message);

        // Restore connection state
        ConnectionState = CurrentState;
    }

    // Raise Data Timeout
    protected void RaiseDataTimeout()
    {
        UpdateState(ConnectionStates.DataTimeout);

        Reconnect();
    }

    // Raise Data Error
    protected void RaiseDataError(String Message)
    {
        RaiseDataError(Message, null);

        Reconnect();
    }

    private void RaiseDataError(String Message, Exception ex)
    {
        Message = Helper.GetErrorMessage("", Message, ex);

        UpdateState(ConnectionStates.DataError, Message);

        Reconnect();
    }

    // Raise System Error
    protected void RaiseSystemError(String Location, String Message)
    {
        RaiseSystemError(Location, Message, null);
    }

    protected void RaiseSystemError(String Location, Exception ex)
	{
		RaiseSystemError(Location, "", ex);
	}

    protected void RaiseSystemError(String Location, String Message, Exception ex)
	{
        Message = Helper.GetErrorMessage(Location, Message, ex);

        UpdateState(ConnectionStates.SystemError, Message);
	}

	//endregion

    //region Update State

    protected void UpdateState(ConnectionStates State)
    {
        UpdateState(State, "");
    }
    private void UpdateState(ConnectionStates State, String Message)
    {
        //Helper.LogMessage("Comm UpdateState, Queued State=" + State);

        Message HandleMessage = new Message();
        HandleMessage.what = State.ordinal();
        HandleMessage.obj = Message;

        ConnectionState = ConnectionStates.values()[HandleMessage.what];
        CommMessage = (String)HandleMessage.obj;

        StateQueue.add(HandleMessage);
    }

    private class UpdateStateThreading extends Thread
    {
        public void run()
        {
            try
            {
                int Retry = blueFire.EventHandlerRetry;

                while (true)
                {
                    if (!StateQueue.isEmpty())
                    {
                        //                    if (StateQueue.size() > 1)
                        //                        Helper.LogMessage("Comm UpdateStateThreading, StateQueue Size=" + StateQueue.size());

                        Message HandleMessage = StateQueue.peek(); // get message
                        if (HandleMessage != null)
                        {
                            ConnectionStates CommState = ConnectionStates.values()[HandleMessage.what];
                            String CommMessage = (String) HandleMessage.obj;

                            //Helper.LogMessage("Comm UpdateStateThreading, Sending State=" + ConnectionState);

                            ConnectionHandler.obtainMessage(CommState.ordinal(), CommMessage).sendToTarget();

                            boolean MessageReceived = !ConnectionHandler.hasMessages(CommState.ordinal(), CommMessage);
                            if (!MessageReceived) // check if message received
                            {
                                int Delay = 0;
                                while (Delay < blueFire.EventHandlerDelay && !MessageReceived)
                                {
                                    Helper.Sleep(10);
                                    Delay += 10;
                                    MessageReceived = !ConnectionHandler.hasMessages(CommState.ordinal(), CommMessage);
                                }
                                if (!MessageReceived)
                                {
                                    Helper.LogMessage("Comm UpdateStateThreading, ***** Missing State=" + CommState + " *****");
                                    ConnectionHandler.removeMessages(CommState.ordinal(), CommMessage); // remove sent message
                                    Retry -= blueFire.EventHandlerDelay;
                                    if (Retry <= 0)
                                    {
                                        Helper.LogMessage("Comm UpdateStateThreading, ***** Ignoring State=" + CommState + " *****");
                                        Retry = blueFire.EventHandlerRetry;
                                        MessageReceived  = true; // set ignored message as received so it can be removed from the queue
                                    }
                                }
                            }
                            if (MessageReceived)
                                StateQueue.poll(); // remove message
                        }
                    }
                    Helper.Sleep(1); // allow other threads to execute
                }
            }
            catch (Exception ex)
            {
                RaiseDataError("Comm.UpdateStateThreading", ex);
            }
        }
    }

    //endregion

    //region Dispose

    @Override
    public void Dispose()
	{
        try
        {
            Disconnect();
        }
        catch (Exception ex)
        {
        }
	}

    //endregion
}
