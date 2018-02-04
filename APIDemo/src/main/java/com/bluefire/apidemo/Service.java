package com.bluefire.apidemo;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.os.Process;
import android.util.Log;

import com.bluefire.api.BlueFire;
import com.bluefire.api.CANBusSpeeds;
import com.bluefire.api.ConnectionStates;
import com.bluefire.api.Const;
import com.bluefire.api.RetrievalMethods;
import com.bluefire.api.Truck;

import java.util.LinkedList;
import java.util.Queue;

public class Service
{
    private BlueFire blueFire;

    private ServiceThread serviceThread;

    private boolean serviceIsRunning;

    // Android process pid for the activity or service that instantiates the API.
    private int servicePid;
    private boolean killService= false;

    private ConnectionStates connectionState = ConnectionStates.NotConnected;
    private String connectionMessage = "";

    private Queue<Message> EventsQueue = new LinkedList<Message>();
    private ReceiveEventsThreading ReceiveEventsThread;

    private boolean isKeyOn;

    private boolean isConnecting;
    private boolean isConnected;

    private boolean IsRetrievingEngineVIN;

    private RetrievalMethods retrievalMethod;
    private int retrievalInterval;

    // Application settings
    private boolean appUseBLE;
    private boolean appUseBT21;

    private boolean appIgnoreJ1939;
    private boolean appIgnoreJ1708;

    private int appLedBrightness;

    private int appDiscoveryTimeOut;
    private int appMaxConnectAttempts;
    private int appMaxReconnectAttempts;
    private int appBluetoothRecycleAttempt;
    private int appBleDisconnectWaitTime;

    private String appDeviceId = "";
    private String appAdapterId = "";
    private boolean appConnectToLastAdapter;

    private String appUserName = "";
    private String appPassword = "";
    private boolean appSecureDevice = false;
    private boolean appSecureAdapter = false;

    private boolean appOptimizeDataRetrieval = false;

    private Context serviceContext;

    public Service(Context context)
    {
        serviceContext = context; // the API requires a context

        // Set to kill the service when the user exits.
        // Note, this is recommended as it will ensure that all API resources such as
        // Bluetooth (BLE GATT) are released.
        //killService = true;
        killService = false; // for testing
        servicePid = Process.myPid();

        // Set app variables
        appUseBLE = true;
        appUseBT21 = false;

        appIgnoreJ1939 = false;
        appIgnoreJ1708 = true;

        appLedBrightness = 100;

        appConnectToLastAdapter = false;

        appOptimizeDataRetrieval = true;

        // Setup to receive API events
        ReceiveEventsThread = new ReceiveEventsThreading();
        ReceiveEventsThread.start();
    }

    public void startService()
    {
        // Initiate the API
        blueFire = new BlueFire(serviceContext, eventHandler);

        appDiscoveryTimeOut = blueFire.DiscoveryTimeoutDefault;
        appMaxConnectAttempts = blueFire.MaxConnectAttemptsDefault;
        appMaxReconnectAttempts = blueFire.MaxReconnectAttemptsDefault;
        appBluetoothRecycleAttempt = blueFire.BluetoothRecycleAttemptDefault;
        appBleDisconnectWaitTime = blueFire.BleDisconnectWaitTimeDefault;

        // Simulate a service
        serviceIsRunning = true;

        serviceThread = new ServiceThread();
        serviceThread.start();
    }

    public void stopService()
    {
        serviceIsRunning = false;

        // Clear previous data from the CAN Filter
        blueFire.StopDataRetrieval();

        disconnectAdapter();

        blueFire.Dispose();

        // Kill the service to ensure all resources (like BLE) are released.
        // Note, this will close the BLE GATT connection if for some reason
        // Android is keeping it open.
        // Note, the app will be killed but most likely Android will restart it
        // so it will show up under Settings/Apps but all the App and API resources
        // will be stopped and restarted.
        if (killService)
            Process.killProcess(servicePid);
    }

    private class ServiceThread extends Thread
    {
        public void run()
        {
            // Connect to the adapter
            connectAdapter();
        }
    }

    // Connect
    public void connectAdapter()
    {
        try
        {
            isConnecting = true;
            isConnected = false;

            connectionState = ConnectionStates.NA;

            logNotifications("Connecting...");

            // Initialize adapter properties (in case they were changed)
            initializeAdapter();

            // Note, this is a blocking call and must run in it's own thread.
            blueFire.Connect();
        }
        catch (Exception ex) {}
    }

    private void initializeAdapter()
    {
        // Set Bluetooth adapter type
        blueFire.UseBLE = appUseBLE;
        blueFire.UseBT21 = appUseBT21;

        // Set to ignore data bus settings
        blueFire.SetIgnoreJ1939(appIgnoreJ1939);
        blueFire.SetIgnoreJ1708(appIgnoreJ1708);

        // Set the BLE Disconnect Wait Timeout.
        // Note, in order for BLE to release the connection to the adapter and allow reconnects
        // or subsequent connects, it must be completely closed. Unfortunately Android does not
        // have a way to detect this other than waiting a set amount of time after disconnecting
        // from the adapter. This wait time can vary with the Android version and the make and
        // model of the mobile device. The default is 2 seconds. If your app experiences numerous
        // unable to connect and BlueFire LE fails to show up under Bluetooth settings, try increasing
        // this value.
        blueFire.SetBleDisconnectWaitTime(appBleDisconnectWaitTime);

        // Set the Bluetooth discovery timeout.
        // Note, depending on the number of Bluetooth devices present on the mobile device,
        // discovery could take a long time.
        // Note, if this is set to a high value, the app needs to provide the user with the
        // capability of canceling the discovery.
        blueFire.SetDiscoveryTimeout(appDiscoveryTimeOut);

        // Set number of Bluetooth connection attempts.
        // Note, if the mobile device does not connect, try setting this to a value that
        // allows for a consistent connection. If you're using multiple adapters and have
        // connection problems, un-pair all devices before connecting.
        blueFire.SetMaxConnectAttempts(appMaxConnectAttempts);
        blueFire.SetMaxReconnectAttempts(appMaxReconnectAttempts);
        blueFire.SetBluetoothRecycleAttempt(appBluetoothRecycleAttempt);

        // Set the device and adapter ids
        blueFire.SetDeviceId(appDeviceId);
        blueFire.SetAdapterId(appAdapterId);

        // Set the connect to last adapter setting
        blueFire.SetConnectToLastAdapter(appConnectToLastAdapter);

        // Set the adapter security parameters
        blueFire.SetSecurity(appSecureDevice, appSecureAdapter, appUserName, appPassword);

        // Set to optimize data retrieval
        blueFire.SetOptimizeDataRetrieval(appOptimizeDataRetrieval);
    }

    private void disconnectAdapter()
    {
        try
        {
            // Note, with Firmware 3.11 there is no need to wait for the adapter
            // to disconnect.
            boolean WaitForDisconnect = isConnected; // just for code clarity
            blueFire.Disconnect(WaitForDisconnect);
        }
        catch(Exception e) {}
    }

    private void adapterDisconnected()
    {
        logNotifications("Adapter disconnected.");

        adapterNotConnected();
    }

    private void adapterNotConnected()
    {
        logNotifications("Adapter not connected.");

        isConnected = false;
        isConnecting = false;

        logStatus();
    }

    private void adapterReconnecting()
    {
        logNotifications("Adapter re-connecting.");

        isConnected = false;
        isConnecting = true;
    }

    private void adapterReconnected()
    {
        logNotifications("Adapter re-connected.");

        adapterConnected();
    }

    private void adapterNotReconnected()
    {
        logNotifications("Adapter not re-connected.");

        adapterNotConnected();
    }

    private void adapterNotAuthenticated()
    {
        logNotifications("Adapter not authenticated.");

        adapterNotConnected();
    }

    private void adapterConnected()
    {
        logNotifications("Adapter connected.");

        isConnected = true;
        isConnecting = false;

        // Get adapter data
        getAdapterData();
    }

    // Start retrieving data after connecting to the adapter
    private void getAdapterData()
    {
        // Check for an incompatible version.
        if (!blueFire.IsCompatible())
        {
            logNotifications("Incompatible Adapter.");

            disconnectAdapter();
            return;
        }

        // Get the adapter id
        appDeviceId = blueFire.DeviceId();
        appAdapterId = blueFire.AdapterId();

        // Get any adapter messages
        blueFire.GetMessages();

        // Start retrieving truck data
        getTruckData();
    }

    private void getTruckData()
    {
        // Clear previous data from the CAN Filter
        blueFire.StopDataRetrieval();

        if (Truck.EngineVIN == Const.NA)
        {
            IsRetrievingEngineVIN = true;
            blueFire.GetEngineVIN();
        }

        retrievalMethod = RetrievalMethods.OnChange; // do not use OnInterval with this many data requests
        retrievalInterval = blueFire.MinInterval(); // should be MinInterval or greater with this many requests
        int hoursInterval = 30 * Const.OneSecond; // hours only change every 3 minutes

        // Request data from the adapter.
        // Note, be careful not to request too much data at one time otherwise you run the risk of filling up
        // the CAN Filter buffer. You can experiment with combining data retrievals to determine how much you can
        // request before filling the CAN Filter buffer (you get an error if you do).

        blueFire.GetEngineData1(retrievalMethod, retrievalInterval); // RPM, Percent Torque, Driver Torque, Torque Mode
        blueFire.GetEngineData2(retrievalMethod, retrievalInterval); // Percent Load, Accelerator Pedal Position
        blueFire.GetEngineData3(retrievalMethod, retrievalInterval); // Vehicle Speed, Max Set Speed, Brake Switch, Clutch Switch, Park Brake Switch, Cruise Control Settings and Switches
        blueFire.GetOdometer(retrievalMethod, retrievalInterval); // Distance and Odometer
        blueFire.GetEngineHours(retrievalMethod, hoursInterval); // Total Engine Hours, Total Idle Hours
        blueFire.GetBrakeData(retrievalMethod, retrievalInterval); // Application Pressure, Primary Pressure, Secondary Pressure
        blueFire.GetBatteryVoltage(retrievalMethod, retrievalInterval); // Battery Voltage
        blueFire.GetFuelData(retrievalMethod, retrievalInterval); // Fuel Levels, Fuel Used, Idle Fuel Used, Fuel Rate, Instant Fuel Economy, Avg Fuel Economy, Throttle Position
        blueFire.GetTemps(retrievalMethod, retrievalInterval); // Oil Temp, Coolant Temp, Intake Manifold Temperature
        blueFire.GetPressures(retrievalMethod, retrievalInterval); // Oil Pressure, Coolant Pressure, Intake Manifold(Boost) Pressure
        blueFire.GetCoolantLevel(retrievalMethod, retrievalInterval); // Coolant Level
    }

    private int TimeToWrite = 9999;

    private void checkTruckData()
    {
        // Check the data you requested to see which one changed that triggered the DataAvailable
        // event. If you're not concerned with data throughput for processing the data, you can just
        // process all the data whether it changed or not.

        if (IsRetrievingEngineVIN && Truck.EngineVIN != Const.NA)
        {
            IsRetrievingEngineVIN = false;
            blueFire.StopRetrievingEngineVIN();
            logNotifications("Engine VIN=" + Truck.EngineVIN);
        }

        if (TimeToWrite > 50)
        {
            TimeToWrite = 0;
            logNotifications("RPM=" + Truck.RPM);
        }
        else
            TimeToWrite ++;

//        if (Truck.RPM > 0)
//            logNotifications("RPM=" + Truck.RPM);
//        logNotifications("PctLoad=" + Truck.PctLoad);
//        logNotifications("Speed=" + Truck.Speed);
    }

    private void checkKeyState()
    {
        boolean keyIsOn = blueFire.IsKeyOn();

        if (isKeyOn != keyIsOn)
        {
            if (keyIsOn)
                logNotifications("Key is On");
            else
                logNotifications("Key is Off");

            // Double check key change by retrieving IsCANAvailable and IsJ1708Available.
            // Note, only do this on change of state, not constantly.
            blueFire.GetKeyState();

            isKeyOn = keyIsOn;
        }
    }

    private void CANStarting()
    {
        // Get the CAN bus speed
        CANBusSpeeds CANBusSpeed = blueFire.CANBusSpeed();

        String Message;
        if (blueFire.IsOBD2())
            Message = "OBD2";
        else
            Message = "J1939";
        Message += " is starting, CAN bus speed is ";

        switch (CANBusSpeed)
        {
            case K250:
                Message += "250K.";
                break;
            case K500:
                Message += "500K.";
                break;
            default:
                Message += "unknown.";
                break;
        }
        logNotifications(Message);

        // Key is on so double check the key state
        checkKeyState();

        // Re-retrieve truck data
        getTruckData();
    }

    private void j1708Restarting()
    {
        // Re-retrieve truck data
        getTruckData();
    }

    private void processEvent(Message msg)
    {
        try
        {
            connectionState = ConnectionStates.values()[msg.arg1];
            connectionMessage = (String)msg.obj;

            logStatus();

            switch (connectionState)
            {
                case Connecting:
                case Discovering:
                case Disconnecting:
                    break;

                case Connected:
                    adapterConnected();
                    break;

                case NotAuthenticated:
                    adapterNotAuthenticated();
                    break;

                case Disconnected:
                    adapterDisconnected();
                    break;

                case Reconnecting:
                    adapterReconnecting();
                    break;

                case Reconnected:
                    adapterReconnected();
                    break;

                case NotReconnected:
                    adapterNotReconnected();
                    break;

                case CANStarting:
                    CANStarting();
                    break;

                case J1708Restarting:
                    j1708Restarting();
                    break;

                case NotConnected:
                    adapterNotConnected();
                    break;

                case CANFilterFull:
                    logNotifications("The CAN Filter is Full. Some data will not be retrieved.");
                    break;

                case DataError:
                    logNotifications("Adapter Data Error. " + connectionMessage);
                    break;

                case Notification:
                    logNotifications("API notification. " + connectionMessage);
                    break;

                case AdapterMessage:
                    logNotifications("Adapter message. " + connectionMessage);
                    break;

                case AdapterReboot:
                    logNotifications("Adapter Rebooting - " + connectionMessage);
                    break;

                case DataTimeout:
                    logNotifications("Adapter Data Timeout - Lost connection with the Adapter");
                    break;

                case BluetoothTimeout:
                    adapterNotConnected();
                    logNotifications("Adapter Bluetooth Timeout - Unable to connect to Bluetooth.");
                    break;

                case AdapterTimeout:
                    adapterNotConnected();
                    logNotifications("Adapter Connection Timeout - Bluetooth unable to connect to the Adapter.");
                    break;

                case SystemError:
                    adapterNotConnected();
                    logNotifications("API System error. " + connectionMessage);
                    break;

                case DataChanged:
                    checkTruckData();
                    break;
            }

        }
        catch (Exception e) {}
    }

    // BlueFire Event Handler Thread
    private class ReceiveEventsThreading extends Thread
    {
        public void run()
        {
            while (true)
            {
                if (!EventsQueue.isEmpty())
                {
                    Message handleMessage = EventsQueue.poll();
                    if (handleMessage != null)
                        processEvent(handleMessage);
                }
                threadSleep(1); // allow other threads to execute
            }
        }
    }

    // BlueFire Event Handler
    private Handler eventHandler = new Handler()
    {
        @Override
        @SuppressLint("HandlerLeak")
        public void handleMessage(Message msg)
        {
            Message handleMessage = new Message();
            handleMessage.what = msg.what;
            handleMessage.obj = msg.obj;

            EventsQueue.add(handleMessage);
        }
    };

    private void logStatus()
    {
        logNotifications(connectionState.toString());
    }

    private void logNotifications(String message)
    {
        if (!message.equals(""))
            Log.d("BlueFire", message);
    }

    private void threadSleep(int Interval)
    {
        try
        {
            Thread.sleep(Interval);
        }
        catch(Exception ex) {}
    }

}
