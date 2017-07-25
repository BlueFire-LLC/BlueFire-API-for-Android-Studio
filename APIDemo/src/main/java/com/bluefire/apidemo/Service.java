package com.bluefire.apidemo;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.bluefire.api.BlueFire;
import com.bluefire.api.Truck;
import com.bluefire.api.Const;
import com.bluefire.api.SleepModes;
import com.bluefire.api.ConnectionStates;
import com.bluefire.api.RetrievalMethods;
import com.bluefire.api.RecordIds; // for ELD
import com.bluefire.api.RecordingModes; // for ELD

public class Service
{
    private BlueFire blueFire;

    private ServiceThread serviceThread;

    private boolean serviceIsRunning;

    private ConnectionStates connectionState = ConnectionStates.NotConnected;

    private boolean isCANAvailable;

    private boolean isConnecting;
    private boolean isConnected;

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

    private String appAdapterId = "";
    private boolean appConnectToLastAdapter;

    public Service(Context serviceContext)
    {
        blueFire = new BlueFire(serviceContext, eventHandler);

        // Set app variables
        appUseBLE = true;
        appUseBT21 = false;

        appIgnoreJ1939 = false;
        appIgnoreJ1708 = true;

        appDiscoveryTimeOut = 10 * Const.OneSecond;

        appMaxConnectAttempts = 10;
        appMaxReconnectAttempts = 5;

        appLedBrightness = 100;

        appConnectToLastAdapter = false;
    }

    public void startService()
    {
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
    }

    private class ServiceThread extends Thread
    {
        public void run()
        {
            // Connect to the adapter
            connectAdapter();
        }
    }

    private void initializeAdapter()
    {
        // Set Bluetooth adapter type
        blueFire.UseBLE = appUseBLE;
        blueFire.UseBT21 = appUseBT21;

        // Set to ignore data bus settings
        blueFire.SetIgnoreJ1939(appIgnoreJ1939);
        blueFire.SetIgnoreJ1708(appIgnoreJ1708);

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

        // Set the Bluetooth adapter id and the 'connect to last adapter' setting
        blueFire.SetAdapterId(appAdapterId);
        blueFire.SetConnectToLastAdapter(appConnectToLastAdapter);
    }

    // Connect
    public void connectAdapter()
    {
        try
        {
            logNotifications("Connecting...");

            // Initialize adapter properties (in case they were changed)
            initializeAdapter();

            isConnecting = true;
            isConnected = false;

            // Note, this is a blocking call and must run in it's own thread.
            blueFire.Connect();
        }
        catch (Exception ex) {}
    }

    private void disconnectAdapter()
    {
        try
        {
            // Note, with Firmware 3.11 there is no need to wait for the adapter
            // to disconnect.
            boolean WaitForDisconnect = false; // just for code clarity
            blueFire.Disconnect(WaitForDisconnect);
        }
        catch(Exception e) {}
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

        // Set the adapter led brightness
        blueFire.SetLedBrightness(appLedBrightness);

        // Get the adapter id
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

        // Set the retrieval method and interval.
        // Note, this is here for demo-ing the different methods.
        //retrievalMethod = RetrievalMethods.OnChange; // default
        //retrievalMethod = RetrievalMethods.Synchronized;
        retrievalMethod = RetrievalMethods.OnInterval;

        // Set the retrieval interval if not using the RetrievalMethod.
        // Note, only required if RetrievalMethod is OnInterval, default is MinInterval
        retrievalInterval = blueFire.MinInterval(); // or any interval you need

        // Request data from the adapter
        // Note, be careful not to request too much data at one time otherwise you
        // run the risk of filling up the CAN Filter buffer. You can experiement with
        // combining data retrievals to determine how much you can request before filling
        // the CAN Filter buffer (you get an error if you do).
        blueFire.GetEngineData1(retrievalMethod, retrievalInterval); // RPM, Percent Torque, Driver Torque, Torque Mode
        blueFire.GetEngineData2(retrievalMethod, retrievalInterval); // Percent Load, Accelerator Pedal Position
        blueFire.GetEngineData3(retrievalMethod, retrievalInterval); // Vehicle Speed, Max Set Speed, Brake Switch, Clutch Switch, Park Brake Switch, Cruise Control Settings and Switches
    }

    private void checkTruckData()
    {
        // Check the data you requested to see which one changed that triggered the DataAvailable
        // event. If you're not concerned with data throughput for processing the data, you can just
        // process all the data whether it changed or not.

        logNotifications("RPM=" + Truck.RPM);
        logNotifications("PctLoad=" + Truck.PctLoad);
        logNotifications("Speed=" + Truck.Speed);
    }

    private void checkKeyState()
    {
        if (isCANAvailable != blueFire.IsCANAvailable())
        {
            isCANAvailable = blueFire.IsCANAvailable();
            if (isCANAvailable)
                logNotifications("Key is On");
            else
                logNotifications("Key is Off");
        }
    }

    private void adapterNotAuthenticated()
    {
        logNotifications("Adapter not authenticated.");

        adapterNotConnected();
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

        logNotifications("App reconnecting to the Adapter. Reason is " + blueFire.ReconnectReason() + ".");
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

    // BlueFire Event Handler
    private final Handler eventHandler = new Handler()
    {
        @Override
        @SuppressLint("HandlerLeak")
        public void handleMessage(Message msg)
        {
            try
            {
                logStatus();

                switch (blueFire.ConnectionState)
                {
                    case NotConnected:
                        if (isConnecting || isConnected)
                            adapterNotConnected();
                        break;

                    case Connecting:
                        if (blueFire.IsReconnecting())
                            if (!isConnecting)
                                adapterReconnecting();
                        break;

                    case Discovering:
                        // Status only
                        break;

                    case Connected:
                        // Status only
                        break;

                    case AdapterConnected:
                        // Status only
                        break;

                    case Authenticated:
                        if (!isConnected)
                            adapterConnected();
                        break;

                    case NotAuthenticated:
                        adapterNotAuthenticated();
                        break;

                    case Disconnecting:
                        // Status only
                        break;

                    case Disconnected:
                        if (isConnecting || isConnected)
                            adapterDisconnected();
                        break;

                    case Reconnecting:
                        if (!isConnecting)
                            adapterReconnecting();
                        break;

                    case Reconnected:
                        if (isConnecting)
                            adapterReconnected();
                        break;

                    case NotReconnected:
                        if (isConnecting)
                            adapterNotReconnected();
                        break;

                    case CANFilterFull:
                        logNotifications("The CAN Filter is Full. Some data will not be retrieved.");
                        break;

                    case DataError:
                        // Ignore, handled by Reconnecting
                        break;

                    case CommTimeout:
                    case ConnectTimeout:
                    case AdapterTimeout:
                        if (isConnecting || isConnected)
                        {
                            blueFire.Disconnect();
                            adapterNotConnected();
                            logNotifications("The Adapter has Timed Out.");
                        }
                        break;

                    case SystemError:
                        if (isConnecting || isConnected)
                        {
                            blueFire.Disconnect();
                            adapterNotConnected();
                            logSystemError();
                        }
                        break;

                    case DataChanged:
                        if (isConnected)
                            checkTruckData();
                }

            }
            catch (Exception e) {}
        }
    };

    private void logStatus()
    {
        // Check for a change of the connection state
        if (connectionState != blueFire.ConnectionState)
        {
            connectionState = blueFire.ConnectionState;
            logNotifications(connectionState.toString());
        }

        // Show any error message from the adapter
        if (!blueFire.NotificationMessage().equals(""))
        {
            logNotifications(blueFire.NotificationMessage());
            blueFire.ClearNotificationMessage();
        }
    }

    private void logSystemError()
    {
        logNotifications("System Error.");
    }

    private void logNotifications(String Notification)
    {
        Log.d("BlueFire", Notification);
    }

}
