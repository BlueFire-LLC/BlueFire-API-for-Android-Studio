package com.bluefire.apidemo;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Process;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.core.app.ActivityCompat;

import com.bluefire.api.BlueFire;
import com.bluefire.api.CANBusSpeeds;
import com.bluefire.api.ConnectionStates;
import com.bluefire.api.Const;
import com.bluefire.api.HardwareTypes;
import com.bluefire.api.J1939;
import com.bluefire.api.RecordIds;
import com.bluefire.api.RecordingModes;
import com.bluefire.api.RetrievalMethods;
import com.bluefire.api.SleepModes;
import com.bluefire.api.Vehicle;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.LinkedList;
import java.util.Queue;

public class Main extends Activity
{
    //region Declaratives

    private ScrollView scrollViewDemo;

    // Adapter Layout
    private RelativeLayout layoutAdapter;

    private TextView textStatus;
    private TextView textHardware;
    private TextView textFirmware;
    private TextView textKeyState;
    private TextView textHeartbeat;

    private EditText textLedBrightness;
    private EditText textUserName;
    private EditText textPassword;
    private EditText textPGN;
    private EditText textSource;
    private EditText textInterval;
    private EditText textPGNData;

    private TextView textNotifications;
    private TextView textResponseSource;
    private TextView textResponseData;

    private CheckBox checkUseBT21;
    private CheckBox checkUseBLE;
    private CheckBox checkRequest;
    private CheckBox checkBAMRTS;
    private CheckBox checkUseJ1939;
    private CheckBox checkUseJ1708;
    private CheckBox checkUseOBD2;
    private CheckBox checkSecureDevice;
    private CheckBox checkSecureAdapter;
    private CheckBox checkSendAllPackets;
    private CheckBox checkConnectLastAdapter;

    private Button buttonConnect;
    private Button buttonNextFault;
    private Button buttonResetFault;
    private Button buttonUpdate;
    private Button buttonSend;
    private Button buttonMonitor;
    private Button buttonVehicleData;
    private Button buttonELDData;
    private Button buttonTest;
    private Button buttonStartService;
    private Button buttonStopService;

    // Vehicle Layout
    private RelativeLayout layoutVehicle;

    private TextView textView1;
    private TextView textView2;
    private TextView textView3;
    private TextView textView4;
    private TextView textView5;
    private TextView textView6;
    private TextView textView7;

    private TextView dataView1;
    private TextView dataView2;
    private TextView dataView3;
    private TextView dataView4;
    private TextView dataView5;
    private TextView dataView6;
    private TextView dataView7;

    // ELD Layout
    private RelativeLayout layoutELD;

    private EditText textDriverId;
    private EditText textELDInterval;
    private EditText textIFTAInterval;
    private EditText textStatsInterval;

    private CheckBox checkAlignELD;
    private CheckBox checkAlignIFTA;
    private CheckBox checkAlignStats;
    private CheckBox checkRecordIFTA;
    private CheckBox checkRecordStats;
    private CheckBox checkSecureELD;
    private CheckBox checkAccessSecured;
    private CheckBox checkStreamingELD;
    private CheckBox checkRecordingConnected;
    private CheckBox checkRecordingDisconnected;

    private TextView textStreamingELD;

    private Button buttonStartELD;
    private Button buttonUploadELD;
    private Button buttonDeleteELD;

    private TextView textRemaining;
    private TextView textRecordNo;
    private TextView textRecordId;
    private TextView textTimeUTC;
    private TextView textTimeLocal;
    private TextView labelVIN;
    private TextView textVIN;
    private TextView textDistance;
    private TextView textOdometer;
    private TextView textTotalHours;
    private TextView textIdleHours;
    private TextView textTotalFuel;
    private TextView textIdleFuel;
    private TextView textLatitude;
    private TextView textLongitude;

    // App variables
    private boolean isConnecting;
    private boolean isReconnecting;
    private boolean isConnected;
    private boolean isConnectButton;

    private int monitorPGN;
    private int monitorPGN2;
    private int monitorSource;
    private int monitorInterval;
    private byte[] monitorPGNData;
    private boolean monitorRequest;
    private boolean monitorBAMRTS;
    private boolean isSendingPGN;
    private boolean isMonitoringPGN;

    private int faultCount;
    private int faultIndex;
    private boolean isRetrievingFaults;

    private String faultSource = "";
    private String faultSPN = "";
    private String faultFMI = "";
    private String faultOccurrence = "";
    private String faultConversion = "";

    private int groupNo;
    private static final int maxGroupNo = 9;

    private boolean IsRetrievingVINID;

    private RetrievalMethods retrievalMethod;
    private int retrievalInterval;

    private static final int myCustomRecordId1 = 1;
    private static final int myCustomRecordId2 = 2;

    private ConnectAdapterThread connectThread;

    private GetVehicleInfoThread getVehicleInfoThread;

    private boolean isKeyOn;

    private boolean secureDevice = false;
    private boolean secureAdapter = false;

    private ConnectionStates connectionState = ConnectionStates.NA;
    private String connectionMessage = "";

    private Queue<Message> EventsQueue = new LinkedList<Message>();
    private ReceiveEventsThreading ReceiveEventsThread;

    // BlueFire adapter and service
    private BlueFire blueFire;
    private Service demoService;

    private boolean isStartingService;

    // Android process pid for the activity or service that instantiates the API.
    private int appPid;
    private boolean killApp = false;

    // BlueFire App settings\

    private boolean ignoreSettings = false;

    private SharedPreferences settings;
    private SharedPreferences.Editor settingsSave;

    private HardwareTypes appHardwareType = HardwareTypes.HW_Unknown;

    private boolean appUseBLE = false;
    private boolean appUseBT21 = false;

    private int appLedBrightness = 100;
    private SleepModes appSleepMode = SleepModes.NoSleep;
    private boolean appPerformanceMode = false;
    private boolean appDisconnectedReboot = false;
    private int appDisconnectedRebootInterval = 60; // minutes
    public int appMinInterval;
    public boolean appSendAllPackets = false;

    private boolean appIgnoreJ1939 = false;
    private boolean appIgnoreJ1708 = false;
    private boolean appIgnoreOBD2 = false;

    private String appDeviceId = "";
    private String appAdapterId = "";
    private boolean appConnectToLastAdapter = false;

    private String appUserName = "";
    private String appPassword = "";
    private boolean appSecureDevice = false;
    private boolean appSecureAdapter = false;

    private int appDiscoveryTimeout;
    private int appMaxConnectAttempts;
    private int appMaxReconnectAttempts;
    private int appBluetoothRecycleAttempt;
    private int appBleDisconnectWaitTime;

    private boolean appOptimizeDataRetrieval = false;

    // ELD settings
    public boolean appIgnoreELD = true;
    public boolean appELDStarted = false;
    public boolean appSecureELD = false;
    public boolean appRecordIFTA = false;
    public boolean appRecordStats = false;
    public boolean appAlignELD = false;
    public boolean appAlignIFTA = false;
    public boolean appAlignStats = false;
    public boolean appStreamingELD = false;
    public int appRecordingMode = RecordingModes.RecordNever.getValue();
    public String appDriverId = "";
    public String appELDAdapterId = "";
    public float appELDInterval = 60; // minutes;
    public float appIFTAInterval = 1; // minutes;
    public float appStatsInterval = 60; // minutes;
    public RecordingModes RecordingMode;

    private boolean isUploading;
    private int currentRecordNo = -1;

    private Context context;

    //endregion

    //region Constructor

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        // Set to kill the app when the user exits.
        // Note, this is recommended as it will ensure that all API resources such as
        // Bluetooth (BLE GATT) are released.
        killApp = true;
        appPid = Process.myPid();

        logNotifications("BlueFire API Demo started.");

        context = this;

        blueFire = new BlueFire(context, eventHandler);

        this.setTitle("BlueFire API Demo v-" + blueFire.APIVersion());

        // Set to use an insecure connection.
        // Note, there are other Android devices that require this other than just ICS (4.0.x).
        if (android.os.Build.VERSION.RELEASE.startsWith("4.0."))
            blueFire.SetUseInsecureConnection(true);
        else
            blueFire.SetUseInsecureConnection(false);

        // Establish settings persistent storage
        try
        {
            settings = this.getPreferences(Context.MODE_PRIVATE);
            settingsSave = settings.edit();

            // Get application settings
            getSettings();
        }
        catch (Exception ex)
        {
            ignoreSettings = true;
        }

        // Initialize adapter properties
        initializeAdapter();

        // Initialize settings that are not in the form for the
        // user to change.
        initializeSettings();

        // Initialize the app startup form
        initializeForm();

        // Setup to receive API events
        ReceiveEventsThread = new ReceiveEventsThreading();
        ReceiveEventsThread.start();
    }

    //endregion

    //region Initialize Settings

    private void initializeSettings()
    {
        if (ignoreSettings)
            return;

        // Initialize settings that are not in the form for the
        // user to change.

        // Set the sleep mode
        appSleepMode = SleepModes.NoSleep;
        blueFire.SetSleepMode(appSleepMode);

        // Test setting connect attempts
        //appMaxConnectAttempts = 1; // default is blueFire.MaxConnectAttemptsDefault (5)
        //appMaxReconnectAttempts = 0; // default is blueFire.MaxReconnectAttemptsDefault (5)

        // Set the performance mode
        appPerformanceMode = false;
        blueFire.SetPerformanceModeOn(appPerformanceMode);

        // Set the disconnect reboot option
        appDisconnectedReboot = true;
        appDisconnectedRebootInterval = 60; // minutes
        blueFire.SetDisconnectedReboot(appDisconnectedReboot, appDisconnectedRebootInterval);

        // Set Optimize Data Retrieval
        appOptimizeDataRetrieval = true;

        // Save settings
        saveSettings();
    }

    //endregion

    //region Get Settings

    private void getSettings()
    {
        if (ignoreSettings)
            return;

        // Get the application settings
        // Note, these should be retrieved from persistent storage.
        appUseBLE = settings.getBoolean("UseBLE", true);
        appUseBT21 = settings.getBoolean("UseBT21", false);
        appIgnoreJ1939 = settings.getBoolean("IgnoreJ1939", false);
        appIgnoreJ1708 = settings.getBoolean("IgnoreJ1708", true);
        appIgnoreOBD2 = settings.getBoolean("IgnoreOBD2", true);
        appLedBrightness = settings.getInt("LedBrightness", 100);
        appPerformanceMode = settings.getBoolean("PerformanceMode", false);
        appDisconnectedReboot = settings.getBoolean("DisconnectedReboot", false);
        appDisconnectedRebootInterval = settings.getInt("DisconnectedRebootInterval", blueFire.DisconnectedRebootIntervalDefault);
        appSendAllPackets = settings.getBoolean("SendAllPackets", false);
        appSecureDevice = settings.getBoolean("SecureDevice", false);
        appSecureAdapter = settings.getBoolean("SecureAdapter", false);
        appConnectToLastAdapter = settings.getBoolean("ConnectToLastAdapter", false);
        appDeviceId = settings.getString("DeviceId", "");
        appAdapterId = settings.getString("AdapterId", "");
        appUserName = settings.getString("UserName", "");
        appPassword = settings.getString("Password", "");
        appMinInterval = settings.getInt("MinInterval", blueFire.MinIntervalDefault);
        appDiscoveryTimeout = settings.getInt("DiscoveryTimeout", blueFire.DiscoveryTimeoutDefault);
        appMaxConnectAttempts = settings.getInt("MaxConnectAttempts", blueFire.MaxConnectAttemptsDefault);
        appMaxReconnectAttempts = settings.getInt("MaxReconnectAttempts", blueFire.MaxReconnectAttemptsDefault);
        appBluetoothRecycleAttempt = settings.getInt("BluetoothRecycleAttempt", blueFire.BluetoothRecycleAttemptDefault);
        appBleDisconnectWaitTime = settings.getInt("BleDisconnectWaitTime", blueFire.BleDisconnectWaitTimeDefault);
        appOptimizeDataRetrieval = settings.getBoolean("OptimizeDataRetrieval", true);

        // Get ELD settings
        appIgnoreELD = settings.getBoolean("IgnoreELD", true);
        appELDStarted = settings.getBoolean("ELDStarted", false);
        appSecureELD = settings.getBoolean("SecureELD", false);
        appRecordIFTA = settings.getBoolean("RecordIFTA", false);
        appRecordStats = settings.getBoolean("RecordStats", false);
        appAlignELD = settings.getBoolean("AlignELD", false);
        appAlignIFTA = settings.getBoolean("AlignIFTA", false);
        appAlignStats = settings.getBoolean("AlignStats", false);
        appStreamingELD = settings.getBoolean("StreamingELD", false);
        appRecordingMode = settings.getInt("RecordingMode", RecordingModes.RecordNever.getValue());
        appDriverId = settings.getString("DriverId", "");
        appELDAdapterId = settings.getString("ELDAdapterId", "");
        appELDInterval = settings.getFloat("ELDInterval", 60); // minutes;
        appIFTAInterval = settings.getFloat("IFTAInterval", 1); // minutes;
        appStatsInterval = settings.getFloat("StatsInterval", 60); // minutes;
    }

    //endregion

    //region Save Settings

    private void saveSettings()
    {
        // Save the application settings.
        settingsSave.putBoolean("UseBLE", appUseBLE);
        settingsSave.putBoolean("UseBT21", appUseBT21);
        settingsSave.putBoolean("IgnoreJ1939", appIgnoreJ1939);
        settingsSave.putBoolean("IgnoreJ1708", appIgnoreJ1708);
        settingsSave.putBoolean("IgnoreELD", appIgnoreELD);
        settingsSave.putBoolean("PerformanceMode", appPerformanceMode);
        settingsSave.putBoolean("DisconnectedReboot", appDisconnectedReboot);
        settingsSave.putInt("DisconnectedRebootInterval", appDisconnectedRebootInterval);
        settingsSave.putBoolean("SendAllPackets", appSendAllPackets);
        settingsSave.putBoolean("SecureDevice", appSecureDevice);
        settingsSave.putBoolean("SecureAdapter", appSecureAdapter);
        settingsSave.putBoolean("ConnectToLastAdapter", appConnectToLastAdapter);
        settingsSave.putString("DeviceId", appDeviceId);
        settingsSave.putString("AdapterId", appAdapterId);
        settingsSave.putString("UserName", appUserName);
        settingsSave.putString("Password", appPassword);
        settingsSave.putInt("LedBrightness", appLedBrightness);
        settingsSave.putInt("MinInterval", appMinInterval);
        settingsSave.putInt("DiscoveryTimeout", appDiscoveryTimeout);
        settingsSave.putInt("MaxConnectAttempts", appMaxConnectAttempts);
        settingsSave.putInt("MaxReconnectAttempts", appMaxReconnectAttempts);
        settingsSave.putInt("BluetoothRecycleAttempt", appBluetoothRecycleAttempt);
        settingsSave.putInt("BleDisconnectWaitTime", appBleDisconnectWaitTime);
        settingsSave.putBoolean("OptimizeDataRetrieval", appOptimizeDataRetrieval);

        settingsSave.putBoolean("IgnoreOBD2", appIgnoreOBD2);
        settingsSave.putBoolean("ELDStarted", appELDStarted);
        settingsSave.putBoolean("SecureELD", appSecureELD);
        settingsSave.putBoolean("RecordIFTA", appRecordIFTA);
        settingsSave.putBoolean("RecordStats", appRecordStats);
        settingsSave.putBoolean("AlignELD", appAlignELD);
        settingsSave.putBoolean("AlignIFTA", appAlignIFTA);
        settingsSave.putBoolean("AlignStats", appAlignStats);
        settingsSave.putBoolean("StreamingELD", appStreamingELD);
        settingsSave.putInt("RecordingMode", appRecordingMode);
        settingsSave.putString("DriverId", appDriverId);
        settingsSave.putString("ELDAdapterId", appELDAdapterId);
        settingsSave.putFloat("ELDInterval", appELDInterval); // minutes;
        settingsSave.putFloat("IFTAInterval", appIFTAInterval); // minutes;
        settingsSave.putFloat("StatsInterval", appStatsInterval); // minutes;

        settingsSave.commit();
    }

    private void resetSettings()
    {
        if (ignoreSettings)
            return;

        settingsSave.clear();
        settingsSave.commit();
    }

    //endregion

    //region Initialize Adapter

    private void initializeAdapter()
    {
        // Set Bluetooth adapter type
        blueFire.UseBLE = appUseBLE;
        blueFire.UseBT21 = appUseBT21;

        // Set to receive notifications from the adapter.
        // Note, this should only be used during testing.
        blueFire.SetNotificationsOn(false);

        // Set to ignore data bus settings
        blueFire.SetIgnoreJ1939(appIgnoreJ1939);
        blueFire.SetIgnoreJ1708(appIgnoreJ1708);
        blueFire.SetIgnoreOBD2(appIgnoreOBD2);

        // Set the minimum interval
        blueFire.SetMinInterval(appMinInterval);

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
        blueFire.SetDiscoveryTimeout(appDiscoveryTimeout);

        // Set number of Bluetooth connection attempts.
        // Note, if the mobile device does not connect, try setting this to a value that
        // allows for a consistent connection. If you're using multiple adapters and have
        // connection problems, un-pair all devices before connecting.
        // Note: Bluetooth Classic (UseBT21) uses Com sockets and they can block for a
        // considerably amount of time depending on the OEM device. It is therefore recommended
        // that you adjust the MaxConnectAttempts, MaxReconnectAttempts, and the DiscoveryTimeout
        // to compensate for this duration.
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

        // Set the send all packets option
        blueFire.SetSendAllPackets(appSendAllPackets);

        // Set streaming and recording mode
        if (!blueFire.IgnoreELD())
        {
            blueFire.ELD.SetStreaming(appStreamingELD);
            blueFire.ELD.SetRecordingMode(RecordingModes.forValue(appRecordingMode));
        }
    }

    //endregion

    //region Initialize Form

    private void initializeForm()
    {
        scrollViewDemo = (ScrollView) findViewById(R.id.scrollViewDemo);

        // Adapter layout
        layoutAdapter = (RelativeLayout) findViewById(R.id.layoutAdapter);

        textStatus = (TextView) findViewById(R.id.textStatus);
        textHardware = (TextView) findViewById(R.id.textHardware);
        textFirmware = (TextView) findViewById(R.id.textFirmware);
        textKeyState = (TextView) findViewById(R.id.textKeyState);
        textHeartbeat = (TextView) findViewById(R.id.textHeartbeat);

        textLedBrightness = (EditText) findViewById(R.id.textLedBrightness);
        textUserName = (EditText) findViewById(R.id.textUserName);
        textPassword = (EditText) findViewById(R.id.textPassword);
        textPGN = (EditText) findViewById(R.id.textPGN);
        textSource = (EditText) findViewById(R.id.textSource);
        textInterval = (EditText) findViewById(R.id.textInterval);
        textPGNData = (EditText) findViewById(R.id.textPGNData);

        textNotifications = (TextView) findViewById(R.id.textNotifications);
        textResponseSource = (TextView) findViewById(R.id.textResponseSource);
        textResponseData = (TextView) findViewById(R.id.textResponseData);

        checkUseBT21 = (CheckBox) findViewById(R.id.checkUseBT21);
        checkUseBLE = (CheckBox) findViewById(R.id.checkUseBLE);
        checkUseJ1939 = (CheckBox) findViewById(R.id.checkUseJ1939);
        checkUseJ1708 = (CheckBox) findViewById(R.id.checkUseJ1708);
        checkUseOBD2 = (CheckBox) findViewById(R.id.checkUseOBD2);
        checkRequest = (CheckBox) findViewById(R.id.checkRequest);
        checkBAMRTS = (CheckBox) findViewById(R.id.checkBAMRTS);
        checkSecureDevice = (CheckBox) findViewById(R.id.checkSecureDevice);
        checkSecureAdapter = (CheckBox) findViewById(R.id.checkSecureAdapter);
        checkSendAllPackets = (CheckBox) findViewById(R.id.checkSendAllPackets);
        checkConnectLastAdapter = (CheckBox) findViewById(R.id.checkConnectLastAdapter);

        buttonConnect = (Button) findViewById(R.id.buttonConnect);
        buttonNextFault = (Button) findViewById(R.id.buttonNextFault);
        buttonResetFault = (Button) findViewById(R.id.buttonResetFault);
        buttonUpdate = (Button) findViewById(R.id.buttonUpdate);
        buttonSend = (Button) findViewById(R.id.buttonSend);
        buttonMonitor = (Button) findViewById(R.id.buttonMonitor);
        buttonVehicleData = (Button) findViewById(R.id.buttonVehicleData);
        buttonELDData = (Button) findViewById(R.id.buttonELDData);
        buttonTest = (Button) findViewById(R.id.buttonTest);
        buttonStartService = (Button) findViewById(R.id.buttonStartService);
        buttonStopService = (Button) findViewById(R.id.buttonStopService);

        // Vehicle layout
        layoutVehicle = (RelativeLayout) findViewById(R.id.layoutVehicle);

        textView1 = (TextView) findViewById(R.id.textView1);
        textView2 = (TextView) findViewById(R.id.textView2);
        textView3 = (TextView) findViewById(R.id.textView3);
        textView4 = (TextView) findViewById(R.id.textView4);
        textView5 = (TextView) findViewById(R.id.textView5);
        textView6 = (TextView) findViewById(R.id.textView6);
        textView7 = (TextView) findViewById(R.id.textView7);

        dataView1 = (TextView) findViewById(R.id.dataView1);
        dataView2 = (TextView) findViewById(R.id.dataView2);
        dataView3 = (TextView) findViewById(R.id.dataView3);
        dataView4 = (TextView) findViewById(R.id.dataView4);
        dataView5 = (TextView) findViewById(R.id.dataView5);
        dataView6 = (TextView) findViewById(R.id.dataView6);
        dataView7 = (TextView) findViewById(R.id.dataView7);

        // ELD layout
        layoutELD = (RelativeLayout) findViewById(R.id.layoutELD);

        textDriverId = (EditText) findViewById(R.id.textDriverId);
        textELDInterval = (EditText) findViewById(R.id.textELDInterval);
        textIFTAInterval = (EditText) findViewById(R.id.textIFTAInterval);
        textStatsInterval = (EditText) findViewById(R.id.textStatsInterval);

        checkAlignELD = (CheckBox) findViewById(R.id.checkAlignELD);
        checkAlignIFTA = (CheckBox) findViewById(R.id.checkAlignIFTA);
        checkAlignStats = (CheckBox) findViewById(R.id.checkAlignStats);
        checkRecordIFTA = (CheckBox) findViewById(R.id.checkRecordIFTA);
        checkRecordStats = (CheckBox) findViewById(R.id.checkRecordStats);
        checkSecureELD = (CheckBox) findViewById(R.id.checkSecureELD);
        checkAccessSecured = (CheckBox) findViewById(R.id.checkAccessSecured);
        checkStreamingELD = (CheckBox) findViewById(R.id.checkStreamingELD);
        checkRecordingConnected = (CheckBox) findViewById(R.id.checkRecordingConnected);
        checkRecordingDisconnected = (CheckBox) findViewById(R.id.checkRecordingDisconnected);

        textStreamingELD = (TextView) findViewById(R.id.textStreamingELD);

        buttonStartELD = (Button) findViewById(R.id.buttonStartELD);
        buttonUploadELD = (Button) findViewById(R.id.buttonUploadELD);
        buttonDeleteELD = (Button) findViewById(R.id.buttonDeleteELD);

        textRemaining = (TextView) findViewById(R.id.textRemaining);
        textRecordNo = (TextView) findViewById(R.id.textRecordNo);
        textRecordId = (TextView) findViewById(R.id.textRecordId);
        textTimeUTC = (TextView) findViewById(R.id.textTimeUTC);
        textTimeLocal = (TextView) findViewById(R.id.textTimeLocal);
        labelVIN = (TextView) findViewById(R.id.labelVIN);
        textVIN = (TextView) findViewById(R.id.textVIN);
        textDistance = (TextView) findViewById(R.id.textDistance);
        textOdometer = (TextView) findViewById(R.id.textOdometer);
        textTotalHours = (TextView) findViewById(R.id.textTotalHours);
        textIdleHours = (TextView) findViewById(R.id.textIdleHours);
        textTotalFuel = (TextView) findViewById(R.id.textTotalFuel);
        textIdleFuel = (TextView) findViewById(R.id.textIdleFuel);
        textLatitude = (TextView) findViewById(R.id.textLatitude);
        textLongitude = (TextView) findViewById(R.id.textLongitude);

        // Clear the form
        clearForm();

        showConnectButton();

        buttonNextFault.setVisibility(View.INVISIBLE);
        buttonResetFault.setVisibility(View.INVISIBLE);

        connectionState = ConnectionStates.NA;
        showStatus();

        buttonConnect.setFocusable(true);
        buttonConnect.setFocusableInTouchMode(true);
        buttonConnect.requestFocus();
    }

    //endregion

    //region Clear Form

    private void clearForm()
    {
        // Disable adapter parameters
        enableAdapterParms();

        textHeartbeat.setText("0");

        faultIndex = -1;

        isRetrievingFaults = false;

        // Show user settings

        if (!blueFire.IsBLESupported())
        {
            checkUseBLE.setChecked(false);
            checkUseBT21.setChecked(true);
        }
        else
        {
            checkUseBLE.setChecked(appUseBLE);
            checkUseBT21.setChecked(appUseBT21);
        }

        checkUseJ1939.setChecked(!appIgnoreJ1939); // checkUseJ1939 is the opposite of ignoreJ1939
        checkUseJ1708.setChecked(!appIgnoreJ1708); // checkUseJ1708 is the opposite of ignoreJ1708
        checkUseOBD2.setChecked(!appIgnoreOBD2); // checkUseJ1708 is the opposite of ignoreJ1708

        checkSecureDevice.setChecked(appSecureDevice);
        checkSecureAdapter.setChecked(appSecureAdapter);
        checkSendAllPackets.setChecked(appSendAllPackets);
        checkConnectLastAdapter.setChecked(appConnectToLastAdapter);

        textUserName.setText(appUserName);
        textPassword.setText(appPassword);
        textLedBrightness.setText(String.valueOf(appLedBrightness));

        textNotifications.setText("");

        // ELD
        if (!blueFire.IgnoreELD())
            setELDParms();
    }

    //endregion

    //region Enable Adapter Parameters

    private void enableAdapterParms()
    {
        // Enable/Disable Adapter page parameters

        // Enable only if not connecting or connected
        boolean isNotConnecting = (!isConnecting && !isConnected);

        checkUseBLE.setEnabled(isNotConnecting);
        checkUseBT21.setEnabled(isNotConnecting);

        checkUseJ1939.setEnabled(isNotConnecting);
        checkUseJ1708.setEnabled(isNotConnecting);
        checkUseOBD2.setEnabled(isNotConnecting);

        checkConnectLastAdapter.setEnabled(isNotConnecting);

        // Enable only if connected

        buttonVehicleData.setEnabled(isConnected);
        buttonTest.setEnabled(isConnected);

        buttonELDData.setEnabled(isConnected && !blueFire.IgnoreELD());

        textPGN.setEnabled(isConnected);
        textPGNData.setEnabled(isConnected);
        textSource.setEnabled(isConnected);
        checkRequest.setEnabled(isConnected);
        checkBAMRTS.setEnabled(isConnected);
        buttonSend.setEnabled(isConnected);
        buttonMonitor.setEnabled(isConnected);
    }

    //endregion

    //region Connect Button

    public void onConnectClick(View view)
    {
        try
        {
            if (isConnectButton)
            {
                if (!editLEDBrightness())
                    return;

                clearForm();

                isConnecting = true;
                isConnected = false;
                isReconnecting = false;

                checkUseBT21.setEnabled(false);
                checkUseBLE.setEnabled(false);

                checkUseJ1939.setEnabled(false);
                checkUseJ1708.setEnabled(false);
                checkUseOBD2.setEnabled(false);

                showDisconnectButton();

                enableAdapterParms();

                buttonNextFault.setVisibility(View.INVISIBLE);
                buttonResetFault.setVisibility(View.INVISIBLE);

                if (appUseBT21)
                    startConnection();
                else
                    checkBluetoothPermissions();
            }
            else // disconnect button
            {
                Thread.sleep(500); // allow eld to stop before disconnecting

                disconnectAdapter();
            }
        } catch (Exception ex)
        {
        }
    }

    //endregion

    //region Start Service Button

    public void onStartServiceClick(View view)
    {
        isStartingService = true;

        enableAdapterParms();

        buttonStartService.setEnabled(false);
        buttonStopService.setEnabled(true);

        buttonConnect.setEnabled(false);

        if (appUseBT21)
            startConnection();
        else
            checkBluetoothPermissions();
    }

    //endregion

    //region Stop Service Button

    public void onStopServiceClick(View view)
    {
        if (demoService == null)
            return;

        demoService.stopService();

        enableAdapterParms();

        buttonStartService.setEnabled(true);
        buttonStopService.setEnabled(false);

        buttonConnect.setEnabled(true);
    }

    //endregion

    //region Check Bluetooth Permissions

    private void checkBluetoothPermissions()
    {
        // BLE adapters require Android 6.0 and the user must accept location access permission
        // Check for Android 6 or higher
        if (blueFire.AndroidVersion()[0] < 6)
        {
            adapterDisconnected();

            Toast.makeText(this, "BLE Adapters require Android 6+.", Toast.LENGTH_LONG).show();
            return;
        }

        // Check and request access permission for BLE
        // Note, ActivityCompat.shouldShowRequestPermissionRationale doesn't always work so we don't use it here.
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults)
    {
        int MinSDKVersion = 0;
        try
        {
            MinSDKVersion = this.getApplicationInfo().minSdkVersion;
        }
        catch (Error e){}

        // Check for user granting permission.
        // Note, if request is cancelled, the result arrays are empty.
        // Note, only minSDKVersion 23 and above requires the user to grant location permission.

        if (MinSDKVersion < 23 || (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED))
        {
            // Ensure location services is turned on
            if (isLocationEnabled())
                startConnection();
            else
            {
                adapterDisconnected();

                Toast.makeText(this, "You need to turn on Location to use the BLE Adapter.", Toast.LENGTH_LONG).show();
            }
        }
        // User refused the permission request, do not allow connection.
        else
        {
            adapterDisconnected();

            Toast.makeText(this, "You need to allow Location Access to use the BLE Adapter.", Toast.LENGTH_LONG).show();
        }
    }

    private boolean isLocationEnabled()
    {
        LocationManager lm = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        return lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    //endregion

    //region Start Connection

    private void startConnection()
    {
        if (isStartingService)
        {
            isStartingService = false;
            StartService();
        }
        else
        {
            // Connect to the adapter via a thread so that the blocking
            // connection will run in it's own thread and allow the app
            // to show status.
            connectThread = new ConnectAdapterThread();
            connectThread.start();
        }
    }

    //endregion

    //region Start Service

    public void StartService()
    {
        if (demoService == null)
            demoService = new Service(this);

        demoService.startService();
    }

    //endregion

    //region Connect Adapter

    private class ConnectAdapterThread extends Thread
    {
        public void run()
        {
            // Initialize adapter properties (in case they were changed)
            initializeAdapter();

            // Instantiate BLE or BT21 Comm
            instantiateComm();

            // Connect to the adapter.
            // Note, this is a blocking call and must run in it's own thread.
            blueFire.Connect();
        }
    }

    private void instantiateComm()
    {
        // Check for user changing adapter types
        if (blueFire.Comm != null)
        {
            if ((appUseBLE && (blueFire.Comm instanceof CommBT2)) || (appUseBT21 && (blueFire.Comm instanceof CommBLE)))
            {
                disposeComm(); // dispose previous instance

                blueFire.SetDiscoveryTimeout(0);
                blueFire.SetAdvertisementTimeout(0);
            }
        }

        // Must set Comm to null for BT21
        if (appUseBT21 && blueFire.Comm != null)
            disposeComm();

        // Check for a BLE or BT21 adapter
        if (blueFire.Comm == null)
        {
             if (appUseBLE)
                blueFire.Comm = new CommBLE(context, eventHandler, blueFire);
            else
                blueFire.Comm = new CommBT2(context, eventHandler, blueFire);
        }
    }

    private void disposeComm()
    {
        try
        {
            if (blueFire.Comm == null)
                return;

            blueFire.Comm.Dispose();
            blueFire.Comm = null;

        } catch (Exception ex){}
    }

    //endregion

    //region Show Connect Button

    private void showConnectButton()
    {
        checkKeyState();

        isConnectButton = true;
        buttonConnect.setText("Connect");
        buttonConnect.setEnabled(true);

        buttonStartService.setEnabled(true);
        buttonStopService.setEnabled(false);
    }

    private void showDisconnectButton()
    {
        isConnectButton = false;
        buttonConnect.setText("Disconnect");
        buttonConnect.setEnabled(true);

        buttonStartService.setEnabled(false);
    }

    //endregion

    //region Adapter Connections

    private void disconnectAdapter()
    {
        try
        {
            enableAdapterParms();

            buttonConnect.setEnabled(false);

            buttonSend.setEnabled(false);
            buttonMonitor.setEnabled(false);

            buttonNextFault.setVisibility(View.INVISIBLE);
            buttonResetFault.setVisibility(View.INVISIBLE);

            buttonStartService.setEnabled(false);
            buttonStopService.setEnabled(false);

            // Stop any monitoring
            if (isSendingPGN || isMonitoringPGN)
                stopSendMonitoring();

            // Stop any reconnection
            blueFire.Comm.StopReconnection(false);

            // Wait for the adapter to disconnect so that the Connect button
            // is not displayed too prematurely.
            boolean WaitForDisconnect = isConnected; // just for code clarity
            blueFire.Disconnect(WaitForDisconnect);

        } catch (Exception e)
        {
        }
    }

    private void adapterDisconnected()
    {
        adapterNotConnected(false);

        String Message = "Adapter disconnected.";
        logNotifications(Message);
        Toast.makeText(this, Message, Toast.LENGTH_SHORT).show();
    }

    private void adapterNotConnected()
    {
        adapterNotConnected(true);
    }
    private void adapterNotConnected(boolean logMessage)
    {
        isConnected = false;
        isConnecting = false;
        isReconnecting = false;

        showConnectButton();

        enableAdapterParms();

        checkUseBT21.setEnabled(true);
        checkUseBLE.setEnabled(true);

        checkUseJ1939.setEnabled(true);
        checkUseJ1708.setEnabled(true);
        checkUseOBD2.setEnabled(true);

        buttonConnect.requestFocus();

        showStatus();

        if (logMessage)
        {
            String Message = "Adapter not connected.";
            logNotifications(Message);
            Toast.makeText(this, Message, Toast.LENGTH_SHORT).show();
        }
    }

    private void adapterReconnecting()
    {
        isConnected = false;
        isConnecting = true;
        isReconnecting = true;

        enableAdapterParms();

        showDisconnectButton();

        String Message = "Reconnecting to the Adapter.";
        logNotifications(Message);
        Toast.makeText(this, Message, Toast.LENGTH_SHORT).show();
    }

    private void adapterReconnected()
    {
        isReconnecting = false;

        logNotifications("Adapter is reconnected and re-retrieving data.");

        // Start re-retrieving Vehicle data after reconnection
        if (layoutVehicle.getVisibility() == View.VISIBLE)
            getVehicleData();
    }

    private void adapterNotReconnected()
    {
        adapterNotConnected(false);

        String Message = "Adapter did not reconnect.";
        logNotifications(Message);
        Toast.makeText(this, Message, Toast.LENGTH_SHORT).show();
    }

    private void adapterNotAuthenticated()
    {
        logNotifications("Adapter is not authenticated.");

        adapterNotConnected();

        Toast.makeText(this, "You are not authorized to access this adapter. Check for the correct adapter, the 'Connect to Last Adapter' setting, or your 'User Name and Password'.", Toast.LENGTH_LONG).show();
    }

    private void adapterConnected()
    {
        isConnected = true;
        isConnecting = false;

        // Enable Use J1939/J1708
        checkUseJ1939.setEnabled(true);
        checkUseJ1708.setEnabled(true);

        // Enable adapter parameters
        enableAdapterParms();

        // Enable buttons
        showDisconnectButton();

        buttonNextFault.setVisibility(View.INVISIBLE);
        buttonResetFault.setVisibility(View.INVISIBLE);

        buttonVehicleData.setEnabled(true);
        buttonTest.setEnabled(true);

        buttonELDData.setEnabled(!blueFire.IgnoreELD());

        buttonConnect.requestFocus();

        // Connect to ELD
        if (!blueFire.IgnoreELD())
            blueFire.ELD.Connect();

        // Get adapter data
        getAdapterData();

        if (isTesting)
            startTest();

        String Message = "Adapter is connected.";
        logNotifications(Message);
        Toast.makeText(this, Message, Toast.LENGTH_SHORT).show();
    }

    //endregion

    //region Starting CAN

    private void startingCAN()
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
        logNotifications(Message, true);

        // Key is on so double check the key state
        checkKeyState();

        // Re-retrieve Vehicle data
        reRetrieveVehicleData();
    }

    private void j1708Restarting()
    {
        // Re-retrieve Vehicle data
        reRetrieveVehicleData();
    }

    //endregion

    //region Get Adapter Data

    // Start retrieving data after connecting to the adapter
    private void getAdapterData()
    {
        // Check for an incompatible version.
        if (!blueFire.IsCompatible())
        {
            logNotifications("Incompatible Adapter.");

            Toast.makeText(this, "The Adapter is not compatible with this API.", Toast.LENGTH_LONG).show();
            disconnectAdapter();
            return;
        }

        // Get the adapter hardware type
        appHardwareType = blueFire.HardwareType();

        // Get the device and adapter ids
        appDeviceId = blueFire.DeviceId();
        appAdapterId = blueFire.AdapterId();

        // Check for API setting the adapter data
        appUseBLE = blueFire.UseBLE;
        appUseBT21 = blueFire.UseBT21;

        appIgnoreJ1939 = blueFire.IgnoreJ1939();
        appIgnoreJ1708 = blueFire.IgnoreJ1708();
        appIgnoreOBD2 = blueFire.IgnoreOBD2();

        // Save any changed data from the API
        saveSettings();

        // Update UseBLE and UseBT21 checkboxes
        checkUseBLE.setChecked(appUseBLE);
        checkUseBT21.setChecked(appUseBT21);

        // Update J1939 and J1708 checkboxes
        checkUseJ1939.setChecked(!appIgnoreJ1939);
        checkUseJ1708.setChecked(!appIgnoreJ1708);
        checkUseOBD2.setChecked(!appIgnoreOBD2);

        // Show hardware and firmware versions
        textHardware.setText(blueFire.HardwareVersion());
        textFirmware.setText(blueFire.FirmwareVersion());

        // Check the key state (key on/off)
        checkKeyState();
    }

    private void reRetrieveVehicleData()
    {
        if (isTesting)
            startTest();

        else if (layoutVehicle.getVisibility() == View.VISIBLE)
            getVehicleData();
    }

    //endregion

    //region BLE Checkbox

    public void onUseBLECheck(View view)
    {
        appUseBLE = checkUseBLE.isChecked();

        appUseBT21 = !appUseBLE;
        checkUseBT21.setChecked(appUseBT21);

        // Update api
        blueFire.UseBLE = appUseBLE;

        saveSettings();
    }

    //endregion

    //region BT21 Checkbox

    public void onUseBT21Check(View view)
    {
        appUseBT21 = checkUseBT21.isChecked();

        appUseBLE = !appUseBT21;
        checkUseBLE.setChecked(appUseBLE);

        // Update api
        blueFire.UseBT21 = appUseBT21;

        saveSettings();
    }

    //endregion

    //region LED Brightness

    private boolean editLEDBrightness()
    {
        // Edit LED Brightness
        int ledBrightness = -1;
        try
        {
            ledBrightness = Integer.parseInt(textLedBrightness.getText().toString().trim());
        }
        catch(Exception e){}

        if (ledBrightness < 1 || ledBrightness > 100)
        {
            Toast.makeText(this, "Led Brightness must be between 1 and 100", Toast.LENGTH_LONG).show();
            return false;
        }

        appLedBrightness = ledBrightness;
        saveSettings();

        blueFire.SetLedBrightness(appLedBrightness);

        return true;
    }

    //endregion

    //region Send All Packets Checkbox

    public void onSendAllPacketsCheck(View view)
    {
        appSendAllPackets = checkSendAllPackets.isChecked();
        saveSettings();

        blueFire.SetSendAllPackets(appSendAllPackets);
    }

    //endregion

    //region Connect to Last Adapter Checkbox

    public void onConnectLastAdapterCheck(View view)
    {
        appConnectToLastAdapter = checkConnectLastAdapter.isChecked();

        if (appConnectToLastAdapter)
            blueFire.SetAdapterId(appAdapterId);

        blueFire.SetConnectToLastAdapter(appConnectToLastAdapter);

        saveSettings();
    }

    //endregion

    //region Secure Device Checkbox

    public void onSecureDeviceCheck(View view)
    {
        secureDevice = checkSecureDevice.isChecked();

        updateSecurity();
    }

    // Secure Adapter Checkbox
    public void onSecureAdapterCheck(View view)
    {
        secureAdapter = checkSecureAdapter.isChecked();

        if (updateSecurity())
        {
            blueFire.SetAdapterId(appAdapterId);
            saveSettings();
        }
    }

    //endregion

    //region J1939 Checkbox

    public void onUseJ1939Check(View view)
    {
        // Set to ignore J1939 (opposite of checkUseJ1939)
        appIgnoreJ1939 = !checkUseJ1939.isChecked();

        if (!appIgnoreJ1939)
        {
            appIgnoreOBD2 = true;
            checkUseOBD2.setChecked(!appIgnoreOBD2); // opposite
        }
        else
        {
            appIgnoreJ1708 = false;
            checkUseJ1708.setChecked(!appIgnoreJ1708); // opposite
        }

        // Update api
        blueFire.SetIgnoreJ1939(appIgnoreJ1939);
        blueFire.SetIgnoreJ1708(appIgnoreJ1708);
        blueFire.SetIgnoreOBD2(appIgnoreOBD2);

        saveSettings();
    }

    //endregion

    //region J1708 Checkbox

    public void onUseJ1708Check(View view)
    {
        // Set to ignore J708 (opposite of checkUseJ1708)
        appIgnoreJ1708 = !checkUseJ1708.isChecked();

        if (!appIgnoreJ1708)
        {
            appIgnoreOBD2 = true;
            checkUseOBD2.setChecked(!appIgnoreOBD2); // opposite
        }
        else
        {
            appIgnoreJ1939 = false;
            checkUseJ1939.setChecked(!appIgnoreJ1939); // opposite
        }

        // Update api
        blueFire.SetIgnoreJ1939(appIgnoreJ1939);
        blueFire.SetIgnoreJ1708(appIgnoreJ1708);
        blueFire.SetIgnoreOBD2(appIgnoreOBD2);

        saveSettings();
    }

    //endregion

    //region OBD2 Checkbox
    public void onUseOBD2Check(View view)
    {
        // Set to ignore J708 (opposite of checkUseJ1708)
        appIgnoreOBD2 = !checkUseOBD2.isChecked();

        if (!appIgnoreOBD2)
        {
            appIgnoreJ1939 = true;
            appIgnoreJ1708 = true;
        }
        else
        {
            appIgnoreJ1939 = false;
            appIgnoreJ1708 = true;
        }
        checkUseJ1939.setChecked(!appIgnoreJ1939); // opposite
        checkUseJ1708.setChecked(!appIgnoreJ1708); // opposite

        // Update api
        blueFire.SetIgnoreJ1939(appIgnoreJ1939);
        blueFire.SetIgnoreJ1708(appIgnoreJ1708);
        blueFire.SetIgnoreOBD2(appIgnoreOBD2);

        saveSettings();
    }

    //endregion

    //region Fault Button

    public void onNextFaultClick(View view)
    {
        showNextFault();
    }

    //endregion

    //region Reset Button

    public void onResetFaultClick(View view)
    {
        blueFire.ResetFaults();
    }

    //endregion

    //region Reset Settings Button

    public void onResetSettingsClick(View view)
    {
        resetSettings();

        getSettings();

        initializeAdapter();

        clearForm();

        buttonConnect.requestFocus();

        Toast.makeText(this, "App settings have been reset.", Toast.LENGTH_SHORT).show();
    }

    //endregion

    //region Update Button

    public void onUpdateClick(View view)
    {
        // Update username and password
        updateSecurity();

        buttonConnect.requestFocus();
    }

    private boolean updateSecurity()
    {
        // Get username and password
        String userNameText = textUserName.getText().toString().trim();
        String passwordText = textPassword.getText().toString().trim();

        // Check for a change of security
        if (secureDevice != appSecureDevice || secureAdapter != appSecureAdapter || !appUserName.equals(userNameText) || !appPassword.equals(passwordText))
        {
            appSecureDevice = secureDevice;
            appSecureAdapter = secureAdapter;
            appUserName = userNameText;
            appPassword = passwordText;
            saveSettings();

            blueFire.SetSecurity(appSecureDevice, appSecureAdapter, appUserName, appPassword);
            return true;
        }
        return false;
    }

    //endregion

    //region Monitor Button

    public void onMonitorClick(View view)
    {
        if (isMonitoringPGN)
        {
            stopSendMonitoring();
            return;
        }

        // Edit Send/Monitoring data
        if (!editSendMonitoring())
            return;

        // Get the 'To' Source.
        // Note, when monitoring a PGN this is the source you are requesting the data from.
        // If the source is Global then any ECM that is programmed to respond will.
        monitorSource = J1939.Sources.Engine.getValue(); // default to engine
        try
        {
            monitorSource = Integer.parseInt("0"+textSource.getText().toString().trim());
        }
        catch(Exception e){}

        if (monitorSource < 0 || monitorSource > 255)
        {
            Toast.makeText(this, "Source must be numeric and less than 256.", Toast.LENGTH_LONG).show();
            return;
        }

        // Get On Request and BAM/RTS.
        // Note, this must match the the SAE specification of the PGN.
        monitorRequest = checkRequest.isChecked();
        monitorBAMRTS = checkBAMRTS.isChecked();

        disableSendMonitoring();

        buttonSend.setEnabled(false);
        buttonMonitor.setText("Stop");

        // Clear notification area
        textNotifications.setText("");

        // Check for monitoring multiple PGNs.
        // Note, in lieu of a MultipleMonitor button we just use the PGN and if none, then
        // we simulate monitoring multiple PGNs.
        if (monitorPGN == 0)
        {
            monitorMultiplePGNs();
            return;
        }

        // Start monitoring the PGN
        isMonitoringPGN = true;

        if (monitorBAMRTS)
            blueFire.RequestPGN(monitorSource, monitorPGN, monitorRequest, monitorBAMRTS);
        else
            blueFire.StartMonitoringPGN(monitorSource, monitorPGN, monitorInterval, monitorRequest, monitorBAMRTS);

        textResponseSource.setText("Waiting for data ...");
    }

    //endregion

    //region Send Button

    public void onSendClick(View view)
    {
        if (isSendingPGN)
        {
            stopSendMonitoring();
            return;
        }

        // Edit Send/Monitoring data
        if (!editSendMonitoring())
            return;

        // Get PGN Data
        String pgnData = textPGNData.getText().toString().trim();

        // Edit the PGN Data to be 16 hex characters (8 bytes)
        if (pgnData.length() != 16)
        {
            Toast.makeText(this, "PGN Data must be 16 hex characters (8 bytes).", Toast.LENGTH_LONG).show();
            return;
        }

        // Get the 'From' Source.
        // Note, when sending a PGN the BlueFire API should be the source otherwise
        // you are spoofing another ECM.
        monitorSource = J1939.Sources.BlueFire.getValue(); // default to BlueFire
        try
        {
            monitorSource = Integer.parseInt("0"+textSource.getText().toString().trim());
        }
        catch(Exception e){}

        if (monitorSource < 0 || monitorSource > 255)
        {
            Toast.makeText(this, "Source must be numeric and less than 256.", Toast.LENGTH_LONG).show();
            return;
        }

        disableSendMonitoring();

        buttonSend.setText("Stop");
        buttonMonitor.setEnabled(false);

        // Convert the PGN Data hex string to bytes
        byte[] pgnBytes = new byte[8];

        try
        {
            pgnBytes = hexStringToBytes(pgnData);

        } catch (Exception e)
        {
            Toast.makeText(this, "PGN Data must be 16 hex characters (8 bytes).", Toast.LENGTH_LONG).show();
            return;
        }

        // Send the PGN
        isSendingPGN = true;
        blueFire.SendPGN(monitorSource, monitorPGN, monitorInterval, pgnBytes);
    }

    private boolean editSendMonitoring()
    {
        // Clear response data
        textResponseSource.setText("");
        textResponseData.setText("");

        // Get the interval
        monitorInterval = -1;
        try
        {
            monitorInterval = Integer.parseInt("0"+textInterval.getText().toString().trim());
        }
        catch(Exception e){}

        if (monitorInterval < 0)
        {
            Toast.makeText(this, "Interval must be numeric.", Toast.LENGTH_LONG).show();
            return false;
        }
        if (monitorInterval == 0)
            monitorInterval = 5 * Const.OneSecond;

        // Get PGN
        monitorPGN = -1; // required
        try
        {
            monitorPGN = Integer.parseInt("0"+textPGN.getText().toString().trim());
        }
        catch(Exception e){}

        if (monitorPGN < 0)
        {
            Toast.makeText(this, "PGN must be numeric.", Toast.LENGTH_LONG).show();
            return false;
        }

        return true;
    }

    private void disableSendMonitoring()
    {
        // Disable send/monitoring data
        textPGN.setEnabled(false);
        textPGNData.setEnabled(false);
        textSource.setEnabled(false);
        textInterval.setEnabled(false);
        checkRequest.setEnabled(false);
        checkBAMRTS.setEnabled(false);
    }

    //endregion

    //region Test Monitor Button

    public void onTestMonitorClick(View view)
    {
        monitorMultiplePGNs();
    }

    private void monitorMultiplePGNs()
    {
        // Simulate monitoring multiple PGNs
        isMonitoringPGN = true;

        // First PGN, a non-request PGN
        monitorSource = J1939.Sources.Engine.getValue();
        monitorPGN = J1939.PGNs.EEC1.getValue();
        blueFire.StartMonitoringPGN(monitorSource, monitorPGN);

        // Second PGN, an on-request PGN
        monitorPGN2 = J1939.PGNs.EngineHoursRevolutions.getValue();
        monitorRequest = true;
        blueFire.StartMonitoringPGN(monitorSource, monitorPGN2, monitorRequest);
    }

    private void stopSendMonitoring()
    {
        // Stop PGN monitoring.
        // Note, this will also stop any Sending PGNs.
        blueFire.StopMonitoringPGN(monitorSource, monitorPGN, monitorRequest);

        isSendingPGN = false;
        isMonitoringPGN = false;

        buttonSend.setText("Send");
        buttonMonitor.setText("Monitor");
        buttonSend.setEnabled(true);
        buttonMonitor.setEnabled(true);

        // Enable PGN monitoring data
        textPGN.setEnabled(true);
        textPGNData.setEnabled(true);
        textSource.setEnabled(true);
        textInterval.setEnabled(true);
        checkRequest.setEnabled(true);
        checkBAMRTS.setEnabled(true);
    }

    //endregion

    //region Next/Previous Buttons

    public void onNextGroupClick(View view)
    {
        groupNo++;
        if (groupNo > maxGroupNo)
            groupNo = 0;

        getVehicleData();
    }

     public void onPreviousGroupClick(View view)
    {
        groupNo--;
        if (groupNo < 0)
            groupNo = maxGroupNo;

        getVehicleData();
    }

    //endregion

    //region Vehicle Button

    public void onVehicleDataClick(View view)
    {
        if (layoutVehicle.getVisibility() == View.INVISIBLE)
        {
            layoutAdapter.setVisibility(View.INVISIBLE);
            layoutELD.setVisibility(View.INVISIBLE);
            layoutVehicle.setVisibility(View.VISIBLE);

            scrollViewDemo.scrollTo(0,0);

            startVehicleData();
        }
        else
        {
            layoutVehicle.setVisibility(View.INVISIBLE);
            layoutAdapter.setVisibility(View.VISIBLE);

            stopVehicleData();
        }
    }

    //endregion

    //region Testing

    private boolean isTesting;
    private boolean testStarted;

    // Test Button
    public void onTestClick(View view)
    {
        // Clear previous data from the CAN Filter
        blueFire.StopDataRetrieval();

        if (isTesting)
        {
            isTesting = false;
            buttonTest.setText(("Start Testing"));
            return;
        }
        buttonTest.setText(("Stop Testing"));

        isTesting = true;
        logNotifications("Test started");

        startTest();
    }

    private void startTest()
    {
        clearAdapterData();

        retrievalMethod = RetrievalMethods.OnChange; // do not use OnInterval with this many data requests
        retrievalInterval = blueFire.MinInterval(); // should be MinInterval or greater with this many requests
        int hoursInterval = 30 * Const.OneSecond; // hours only change every 3 minutes

        // Request data from the adapter.
        // Note, be careful not to request too much data at one time otherwise you run the risk of filling up
        // the CAN Filter buffer. You can experiment with combining data retrievals to determine how much you can
        // request before filling the CAN Filter buffer (you get an error if you do).

        // Start monitoring for faults.
        // Note, this clears the CAN Filter so it must be before any other requests for data.
        blueFire.GetFaults();

        // Start monitoring all other Vehicle data
        blueFire.GetEngineData1(retrievalMethod, retrievalInterval); // RPM, Percent Torque, Driver Torque, Torque Mode
        blueFire.GetEngineData2(retrievalMethod, retrievalInterval); // Percent Load, Accelerator Pedal Position
        blueFire.GetEngineData3(retrievalMethod, retrievalInterval); // Vehicle Speed, Max Set Speed, Brake Switch, Clutch Switch, Park Brake Switch, Cruise Control Settings and Switches
        blueFire.GetOdometer(retrievalMethod, retrievalInterval); // Distance and Odometer
        blueFire.GetEngineHours(retrievalMethod, hoursInterval); // Total Engine Hours, Total Idle Hours
        blueFire.GetBrakeData(retrievalMethod, retrievalInterval); // Application Pressure, Primary Pressure, Secondary Pressure
        blueFire.GetBatteryVoltage(retrievalMethod, retrievalInterval); // Battery Voltage
        blueFire.GetFuelData(retrievalMethod, retrievalInterval); // Fuel Used, Idle Fuel Used, Fuel Rate, Instant Fuel Economy, Avg Fuel Economy, Throttle Position
        blueFire.GetTemps(retrievalMethod, retrievalInterval); // Oil Temp, Coolant Temp, Intake Manifold Temperature
        blueFire.GetPressures(retrievalMethod, retrievalInterval); // Oil Pressure, Coolant Pressure, Intake Manifold(Boost) Pressure
        blueFire.GetCoolantLevel(retrievalMethod, retrievalInterval); // Coolant Level
        blueFire.GetTransmissionGears(retrievalMethod, retrievalInterval); // Selected and Current Gears (not available in J1708)

        testStarted = true;
    }

    //endregion

    //region Check Key State

    private void checkKeyState()
    {
        boolean keyIsOn = blueFire.IsKeyOn();

        if (isKeyOn != keyIsOn)
        {
            if (keyIsOn)
                textKeyState.setText("Key On");
            else
                textKeyState.setText("Key Off");

            // Double check key change by retrieving IsCANAvailable and IsJ1708Available.
            // Note, only do this on change of state, not constantly.
            blueFire.GetKeyState();

            isKeyOn = keyIsOn;
        }
    }

    //endregion

    //region Show Data

    private void showData()
    {
        // Check the key state (key on/off)
        checkKeyState();

        // Show Vehicle data
        if (blueFire.IsVehicleDataChanged())
            showVehicleData();

        // Show ELD data
        if (!blueFire.IgnoreELD())
        {
            if (blueFire.ELD.IsDataRetrieved())
                showELDData();
        }

        // Check for Monitoring a PGN(s)
        if (isMonitoringPGN && (blueFire.PGNData.PGN == monitorPGN || blueFire.PGNData.PGN == monitorPGN2))
        {
            showMonitorData();
            if (monitorBAMRTS)
                stopSendMonitoring();
        }

        // Check for Sending a PGN
        if (isSendingPGN && blueFire.PGNData.PGN == monitorPGN)
            showSendPGNData();
    }

    private void showSendPGNData()
    {
        textPGNData.setText(bytesToHexString(blueFire.PGNData.Data).toUpperCase());
    }

    private void showMonitorData()
    {
        textPGN.setText(Integer.toString(blueFire.PGNData.PGN));

        textResponseSource.setText(Integer.toString(blueFire.PGNData.Source));

        String PGNDataText = bytesToHexString(blueFire.PGNData.Data).toUpperCase();

        textResponseData.setText(PGNDataText);

        //logNotifications("showMonitorData - PGN=" + blueFire.PGNData.PGN + ", Source=" + blueFire.PGNData.Source + ", Data=" + PGNDataText);
    }

    private void showHeartbeat()
    {
        // Check the key state (key on/off)
        checkKeyState();

        textHeartbeat.setText(String.valueOf(blueFire.HeartbeatCount()));
    }

    //endregion

    //region Clear Adapter Data
    private void clearAdapterData()
    {
        testStarted = false;

        IsRetrievingVINID = false;
        isRetrievingFaults = false;

        if (getVehicleInfoThread != null)
            getVehicleInfoThread.interrupt();

        stopSendMonitoring();

        blueFire.StopDataRetrieval();
    }

    //endregion

    //region Get Vehicle Info

    private class GetVehicleInfoThread extends Thread
    {
        public void run()
        {
            // Get the Engine VIN and Component Id
            final int nofRetries = 3;

            int retries = nofRetries;
            while (retries > 0)
            {
                blueFire.GetEngineVIN(RetrievalMethods.Synchronized);
                if (Vehicle.EngineVIN != Const.NA)
                    break;
                retries--;
                threadSleep(1); // allow other threads to execute
            }

            retries = nofRetries;
            while (retries > 0)
            {
                blueFire.GetEngineId(RetrievalMethods.Synchronized);
                if (Vehicle.EngineMake != Const.NA)
                    break;
                retries--;
                threadSleep(1); // allow other threads to execute
            }

            retries = nofRetries;
            while (retries > 0)
            {
                blueFire.GetVehicleVIN(RetrievalMethods.Synchronized);
                if (Vehicle.VIN != Const.NA)
                    break;
                retries--;
                threadSleep(1); // allow other threads to execute
            }

            retries = nofRetries;
            while (retries > 0)
            {
                blueFire.GetVehicleId(RetrievalMethods.Synchronized);
                if (Vehicle.Make != Const.NA)
                    break;
                retries--;
                threadSleep(1); // allow other threads to execute
            }
        }
    }

    //endregion

    //region Get Vehicle Data

    private void startVehicleData()
    {
        if (!blueFire.IsConnected())
            return;

        // Set data retrieval method for testing.
        // Note, this can be Synchronized or OnChange.
        //retrievalMethod = OnChange;
        //retrievalMethod = Synchronized;

        // Or set the retrieval interval if not using the RetrievalMethod.
        retrievalInterval = blueFire.MinInterval(); // or any interval you need

        groupNo = 0;
        getVehicleData();
    }

    private void stopVehicleData()
    {
        if (!blueFire.IsConnected())
            return;

        if (getVehicleInfoThread != null)
            getVehicleInfoThread.interrupt();

        blueFire.StopDataRetrieval();
    }

    private void getVehicleData()
    {
        dataView1.setText("");
        dataView2.setText("");
        dataView3.setText("");
        dataView4.setText("");
        dataView5.setText("");
        dataView6.setText("");
        dataView7.setText("");

        buttonNextFault.setVisibility(View.INVISIBLE);
        buttonResetFault.setVisibility(View.INVISIBLE);

        // Set the retrieval method and interval.
        // Note, this is here for demo-ing the different methods.
        retrievalMethod = RetrievalMethods.OnChange;
        retrievalInterval = blueFire.MinInterval(); // default, only required if RetrievalMethod is OnInterval

        switch (groupNo)
        {
            case 0:
                textView1.setText("RPM");
                textView2.setText("Speed");
                textView3.setText("Accel Pedal");
                textView4.setText("Pct Load");
                textView5.setText("Pct Torque");
                textView6.setText("Driver Torque");
                textView7.setText("Torque Mode");

                //blueFire.GetEngineData1(); // default OnChange
                //blueFire.GetEngineData1(retrievalMethod.OnInterval); // default MinInterval
                //blueFire.GetEngineData1(retrievalMethod.Synchronized); // blocks until data is retrieved
                if (!isTesting)
                {
                    clearAdapterData();
                    blueFire.GetEngineData1(retrievalMethod, retrievalInterval); // RPM, Percent Torque, Driver Torque, Torque Mode
                    blueFire.GetEngineData2(retrievalMethod, retrievalInterval); // Percent Load, Accelerator Pedal Position
                    blueFire.GetEngineData3(retrievalMethod, retrievalInterval); // Vehicle Speed, Max Set Speed, Brake Switch, Clutch Switch, Park Brake Switch, Cruise Control Settings and Switches
                }
                break;

            case 1:
                textView1.setText("Distance");
                textView2.setText("     Hi-Res");
                textView3.setText("     Lo-Res");
                textView4.setText("");
                textView5.setText("Odometer");
                textView6.setText("     Hi-Res");
                textView7.setText("     Lo-Res");

                if (!isTesting)
                {
                    clearAdapterData();
                    blueFire.GetOdometer(retrievalMethod, retrievalInterval); // Distance and Odometer
                }
                break;

            case 2:
                textView1.setText("Total Hours");
                textView2.setText("Idle Hours");
                textView3.setText("Brake Pres");
                textView4.setText("Brake Air");
                textView5.setText("Current Gear");
                textView6.setText("Selected Gear");
                textView7.setText("Battery Volts");

                if (!isTesting)
                {
                    clearAdapterData();
                    blueFire.GetEngineHours(retrievalMethod, retrievalInterval); // Total Engine Hours, Total Idle Hours
                    blueFire.GetBrakeData(retrievalMethod, retrievalInterval); // Application Pressure, Primary Pressure, Secondary Pressure
                    blueFire.GetTransmissionGears(retrievalMethod, retrievalInterval); // Selected and Current Gears
                    blueFire.GetBatteryVoltage(retrievalMethod, retrievalInterval); // Battery Voltage
                }
                break;

            case 3:
                textView1.setText("Fuel Rate");
                textView2.setText("Fuel Used");
                textView3.setText("HiRes Fuel");
                textView4.setText("Idle Fuel Used");
                textView5.setText("Avg Fuel Econ");
                textView6.setText("Inst Fuel Econ");
                textView7.setText("Throttle Pos");

                if (!isTesting)
                {
                    clearAdapterData();
                    blueFire.GetFuelData(retrievalMethod, retrievalInterval); // Fuel Levels, Fuel Used, Idle Fuel Used, Fuel Rate, Instant Fuel Economy, Avg Fuel Economy, Throttle Position
                }
                break;

            case 4:
                textView1.setText("Oil Temp");
                textView2.setText("Oil Pressure");
                textView3.setText("Intake Temp");
                textView4.setText("Intake Pres");
                textView5.setText("Coolant Temp");
                textView6.setText("Coolant Pres");
                textView7.setText("Coolant Level");

                if (!isTesting)
                {
                    clearAdapterData();
                    blueFire.GetTemps(retrievalMethod, retrievalInterval); // Oil Temp, Coolant Temp, Transmission Temp, Intake Manifold Temperature
                    blueFire.GetPressures(retrievalMethod, retrievalInterval); // Oil Pressure, Coolant Pressure, Intake Manifold(Boost) Pressure
                    blueFire.GetCoolantLevel(retrievalMethod, retrievalInterval); // Coolant Level
                }
                break;

            case 5:
                textView1.setText("Brake Switch");
                textView2.setText("Clutch Switch");
                textView3.setText("Park Switch");
                textView4.setText("Cruise Switch");
                textView5.setText("Cruise State");
                textView6.setText("Cruise Speed");
                textView7.setText("Max Speed");

                if (isTesting && !testStarted)
                    startTest(); // restart testing

                if (!isTesting)
                {
                    clearAdapterData();
                    blueFire.GetEngineData3(retrievalMethod, retrievalInterval); // Vehicle Speed, Max Set Speed, Brake Switch, Clutch Switch, Park Brake Switch, Cruise Control Settings and Switches
                }
                break;

            case 6:
                textView1.setText("RPM");
                textView2.setText("Speed");
                textView3.setText("Distance");
                textView4.setText("Odometer");
                textView5.setText("Total Hours");
                textView6.setText("");
                textView7.setText("");

                if (!isTesting)
                {
                    clearAdapterData();
                    blueFire.GetELDData(); // RPM, Speed, Distance/Odometer, Total Hours
                }

                break;

            case 7:
                textView1.setText("Engine VIN");
                textView2.setText("Make");
                textView3.setText("Model");
                textView4.setText("Serial No");
                textView5.setText("Unit No");
                textView6.setText("");
                textView7.setText("");

//                if (!appIgnoreOBD2)
//                {
//                    clearAdapterData();
//                    blueFire.GetEngineVIN();
//                }
//                else
                {
                    if (!IsRetrievingVINID)
                    {
                        clearAdapterData();

                        IsRetrievingVINID = true;

                        getVehicleInfoThread = new GetVehicleInfoThread();
                        getVehicleInfoThread.start();
                    }
                }

                break;

            case 8:
                textView1.setText("Vehicle VIN");
                textView2.setText("Make");
                textView3.setText("Model");
                textView4.setText("Serial No");
                textView5.setText("Unit No");
                textView6.setText("");
                textView7.setText("");

                break;

            case 9:
                textView1.setText("Source");
                textView2.setText("SPN");
                textView3.setText("FMI");
                textView4.setText("Occurrences");
                textView5.setText("Conversion");
                textView6.setText("");
                textView7.setText("");

                if (isTesting && !testStarted)
                    startTest(); // restart testing

                if (!isTesting && !isRetrievingFaults)
                {
                    isRetrievingFaults = true;

                    buttonNextFault.setVisibility(View.VISIBLE);
                    buttonResetFault.setVisibility(View.VISIBLE);

                    blueFire.GetFaults(); // Engine Faults
                    //blueFire.GetFaults(11, 128); // Brakes Faults
                    //blueFire.GetFaults(90, 0); // Proprietary faults
                }

                break;
        }
    }

    //endregion

    //region Show Vehicle Data

    private void showVehicleData()
    {
        switch (groupNo)
        {
            case 0:
                dataView1.setText(formatInt(Vehicle.RPM));
                dataView2.setText(formatFloat(Vehicle.Speed * Const.KphToMph,2));
                dataView3.setText(formatFloat(Vehicle.AccelPedal,2));
                dataView4.setText(formatInt(Vehicle.PctLoad));
                dataView5.setText(formatInt(Vehicle.PctTorque));
                dataView6.setText(formatInt(Vehicle.DrvPctTorque));
                dataView7.setText(String.valueOf(Vehicle.TorqueMode));

                break;

            case 1:
                dataView1.setText(formatFloat(Vehicle.Distance * Const.MetersToMiles,2)); // hi-res or converted lo-res
                dataView2.setText(formatFloat(Vehicle.HiResDistance * Const.MetersToMiles,2));
                dataView3.setText(formatFloat(Vehicle.LoResDistance * Const.KmToMiles,2));
                dataView4.setText("");
                dataView5.setText(formatFloat(Vehicle.Odometer * Const.MetersToMiles,2)); // hi-res or converted lo-res
                dataView6.setText(formatFloat(Vehicle.HiResOdometer * Const.MetersToMiles,2));
                dataView7.setText(formatFloat(Vehicle.LoResOdometer * Const.KmToMiles,2));

                break;

            case 2:
                dataView1.setText(formatFloat(Vehicle.TotalHours,3));
                dataView2.setText(formatFloat(Vehicle.IdleHours,3));
                dataView3.setText(formatFloat(Vehicle.BrakeAppPressure * Const.kPaToPSI,2));
                dataView4.setText(formatFloat(Vehicle.Brake1AirPressure * Const.kPaToPSI,2));
                dataView5.setText(formatInt(Vehicle.CurrentGear));
                dataView6.setText(formatInt(Vehicle.SelectedGear));
                dataView7.setText(formatFloat(Vehicle.BatteryPotential,2));

                break;

            case 3:
                dataView1.setText(formatFloat(Vehicle.FuelRate * Const.LphToGalPHr,2));
                dataView2.setText(formatFloat(Vehicle.FuelUsed * Const.LitersToGal,3));
                dataView3.setText(formatFloat(Vehicle.HiResFuelUsed * Const.LitersToGal,3));
                dataView4.setText(formatFloat(Vehicle.IdleFuelUsed * Const.LitersToGal,3));
                dataView5.setText(formatFloat(Vehicle.AvgFuelEcon * Const.KplToMpg,2));
                dataView6.setText(formatFloat(Vehicle.InstFuelEcon * Const.KplToMpg,2));
                dataView7.setText(formatFloat(Vehicle.ThrottlePos,2));

                break;

            case 4:
                dataView1.setText(formatFloat(celciusToFarenheit(Vehicle.OilTemp),2));
                dataView2.setText(formatFloat(Vehicle.OilPressure * Const.kPaToPSI,2));
                dataView3.setText(formatFloat(celciusToFarenheit(Vehicle.IntakeTemp),2));
                dataView4.setText(formatFloat(Vehicle.IntakePressure * Const.kPaToPSI,2));
                dataView5.setText(formatFloat(celciusToFarenheit(Vehicle.CoolantTemp),2));
                dataView6.setText(formatFloat(Vehicle.CoolantPressure * Const.kPaToPSI,2));
                dataView7.setText(formatFloat(Vehicle.CoolantLevel,2));

                break;

            case 5:
                dataView1.setText(String.valueOf(Vehicle.BrakeSwitch));
                dataView2.setText(String.valueOf(Vehicle.ClutchSwitch));
                dataView3.setText(String.valueOf(Vehicle.ParkBrakeSwitch));
                dataView4.setText(String.valueOf(Vehicle.CruiseOnOff));
                dataView5.setText(String.valueOf(Vehicle.CruiseState));
                dataView6.setText(formatFloat(Vehicle.CruiseSetSpeed * Const.KphToMph,0));
                float MaxSpeed = Vehicle.MaxSpeed;
                if (Vehicle.HiResMaxSpeed > 0)
                    MaxSpeed = Vehicle.HiResMaxSpeed;
                dataView7.setText(formatFloat(MaxSpeed * Const.KphToMph,0));

                break;

            case 6:
                dataView1.setText(formatInt(Vehicle.RPM));
                dataView2.setText(formatFloat(Vehicle.Speed * Const.KphToMph,2));
                dataView3.setText(formatFloat(Vehicle.Distance * Const.MetersToMiles,2)); // hi-res or converted lo-res
                dataView4.setText(formatFloat(Vehicle.Odometer * Const.MetersToMiles,2)); // hi-res or converted lo-res
                dataView5.setText(formatFloat(Vehicle.TotalHours,3));
                dataView6.setText("");
                dataView7.setText("");

                break;

            case 7:
                dataView1.setText(Vehicle.EngineVIN);
                dataView2.setText(Vehicle.EngineMake);
                dataView3.setText(Vehicle.EngineModel);
                dataView4.setText(Vehicle.EngineSerialNo);
                dataView5.setText(Vehicle.EngineUnitNo);
                dataView6.setText("");

                // Waiting on either VIN or ID
                if (Vehicle.EngineVIN == Const.NA && Vehicle.EngineMake == Const.NA)
                    textView7.setText("Retrieving Engine VIN ...");

                // Waiting just for VIN
                else if (Vehicle.EngineVIN == Const.NA)
                    textView7.setText("Retrieving Engine VIN ...");

                // Waiting just for ID
                else if (Vehicle.EngineMake == Const.NA)
                    textView7.setText("Retrieving Engine Id ...");

                // Retrieved VIN and ID
                else
                    textView7.setText("");

                // Stop retrieving the data when all have been retrieved.
                // Note, because the VIN and ID are requested on interval, they should be stopped
                // when all have been retrieved.
                if (Vehicle.EngineVIN != Const.NA)
                    blueFire.StopRetrievingEngineVIN();
                if (Vehicle.EngineMake != Const.NA )
                    blueFire.StopRetrievingEngineId();

                if (Vehicle.EngineVIN != Const.NA && Vehicle.EngineMake != Const.NA && Vehicle.VIN != Const.NA && Vehicle.Make != Const.NA)
                    blueFire.StopDataRetrieval();

                break;

            case 8:
                dataView1.setText(Vehicle.VIN);
                dataView2.setText(Vehicle.Make);
                dataView3.setText(Vehicle.Model);
                dataView4.setText(Vehicle.SerialNo);
                dataView5.setText(Vehicle.UnitNo);
                dataView6.setText("");

                // Waiting on either VIN or ID
                if (Vehicle.VIN == Const.NA && Vehicle.Make == Const.NA)
                    textView7.setText("Retrieving Vehicle VIN ...");

                    // Waiting just for VIN
                else if (Vehicle.VIN == Const.NA)
                    textView7.setText("Retrieving Vehicle VIN ...");

                    // Waiting just for ID
                else if (Vehicle.Make == Const.NA)
                    textView7.setText("Retrieving Vehicle Id ...");

                    // Retrieved VIN and ID
                else
                    textView7.setText("");

                // Stop retrieving the data when all have been retrieved.
                // Note, because the VIN and ID are requested on interval, they should be stopped
                // when all have been retrieved.
                if (Vehicle.VIN != Const.NA)
                    blueFire.StopRetrievingVehicleVIN();
                if (Vehicle.Make != Const.NA )
                    blueFire.StopRetrievingVehicleId();

                break;

            case 9:
                if (Vehicle.GetFaultCount() == 0)
                {
                    faultCount = 0;
                    faultIndex = -1; // reset to show fault
                    buttonNextFault.setEnabled(false);
                    buttonResetFault.setEnabled(false);
                }
                else // faults found
                {
                    if (Vehicle.GetFaultCount() != faultCount) // additional faults
                    {
                        faultCount = Vehicle.GetFaultCount();
                        faultIndex = 0; // show first fault
                        if (faultCount > 1)
                            buttonNextFault.setEnabled(true);
                        buttonResetFault.setEnabled(true);
                    }
                }
                if (faultIndex < 0)
                {
                    faultSource = "";
                    faultSPN = "";
                    faultFMI = "";
                    faultOccurrence = "";
                    faultConversion = "";
                }
                else
                {
                    faultSource = String.valueOf(Vehicle.GetFaultSource(faultIndex));
                    faultSPN = String.valueOf(Vehicle.GetFaultSPN(faultIndex));
                    faultFMI = String.valueOf(Vehicle.GetFaultFMI(faultIndex));
                    faultOccurrence = String.valueOf(Vehicle.GetFaultOccurrence(faultIndex));
                    faultConversion = String.valueOf(Vehicle.GetFaultConversion(faultIndex));
                }

                dataView1.setText(faultSource);
                dataView2.setText(faultSPN);
                dataView3.setText(faultFMI);
                dataView4.setText(faultOccurrence);
                dataView5.setText(faultConversion);
                dataView6.setText("");
                dataView7.setText("");

                break;
        }
    }

    private void showNextFault()
    {
        // Set to show next fault
        faultIndex += 1;
        if (faultIndex == faultCount) // wrap to the beginning
            faultIndex = 0;
    }

    private void showStatus()
    {
        textStatus.setText(connectionState.toString());
    }

    //endregion

    //region ELD

    // *********************************** ELD *******************************************

    public void connectedELD()
    {
        // Check if ELD page is visible and refresh it
        if (layoutELD.getVisibility() != View.INVISIBLE)
        {
            showELDPage();

            if (blueFire.ELD.IsStarted())
                blueFire.ELD.StartStreaming();
        }
    }

    // ELD Button
    public void onELDDataClick(View view)
    {
        if (!blueFire.ELD.IsCompatible())
        {
            Toast.makeText(this, "The Adapter is not compatible with ELD Recording.", Toast.LENGTH_LONG).show();
            return;
        }

        if (!blueFire.IsConnected())
        {
            Toast.makeText(this, "The Adapter must be connected for ELD Recording.", Toast.LENGTH_LONG).show();
            return;
        }

        if (layoutELD.getVisibility() == View.INVISIBLE)
        {
            layoutAdapter.setVisibility(View.INVISIBLE);
            layoutVehicle.setVisibility(View.INVISIBLE);
            layoutELD.setVisibility(View.VISIBLE);

            showELDPage();
        }
        else
        {
            layoutELD.setVisibility(View.INVISIBLE);
            layoutAdapter.setVisibility(View.VISIBLE);
        }
    }

    // Align ELD Checkbox
    public void onAlignELDCheck(View view)
    {
        // Edit for valid hour alignment
        editELDInterval();
    }

    // Record IFTA Checkbox
    public void onRecordIFTACheck(View view)
    {
        appRecordIFTA = checkRecordIFTA.isChecked();
    }

    // Align IFTA Checkbox
    public void onAlignIFTACheck(View view)
    {
        // Edit for valid hour alignment
        editIFTAInterval();
    }

    // Record Stats Checkbox
    public void onRecordStatsCheck(View view)
    {
        appRecordStats = checkRecordStats.isChecked();
    }

    // Align Stats Checkbox
    public void onAlignStatsCheck(View view)
    {
        // Edit for valid hour alignment
        editStatsInterval();
    }

    // Secure ELD Checkbox
    public void onSecureELDCheck(View view)
    {
        if (!blueFire.IsConnected())
            return;

        appSecureELD = checkSecureELD.isChecked();

        blueFire.ELD.SetSecured(appSecureELD);
    }

    // Streaning ELD Checkbox
    public void onStreamingELDCheck(View view)
    {
        if (!blueFire.IsConnected())
            return;

        appStreamingELD = checkStreamingELD.isChecked();

        blueFire.ELD.SetStreaming(appStreamingELD);

        setELDStreamingText();
    }

    // Record Connected Checkbox
    public void onRecordingConnectedCheck(View view)
    {
        if (!blueFire.IsConnected())
            return;

        blueFire.ELD.SetRecordingMode(checkRecordingConnected.isChecked(), checkRecordingDisconnected.isChecked());

        setELDStreamingText();
    }

    // Record Connected Checkbox
    public void onRecordingDisconnectedCheck(View view)
    {
        if (!blueFire.IsConnected())
            return;

        blueFire.ELD.SetRecordingMode(checkRecordingConnected.isChecked(), checkRecordingDisconnected.isChecked());

        setELDStreamingText();
    }

    // ELD Start Button
    public void onStartELDClick(View view)
    {
        if (blueFire.ELD.IsStarted())
            stopELD();
        else
            startELD();
    }

    // ELD Upload Button
    public void onUploadELDClick(View view)
    {
        // Disable buttons
        buttonStartELD.setEnabled(false);
        buttonUploadELD.setEnabled(false);
        buttonDeleteELD.setEnabled(false);

        // Start the upload
        isUploading = true;

        blueFire.ELD.StartUpload();

        blueFire.ELD.GetRecord(1);
    }

    // ELD Delete Button
    public void onDeleteELDClick(View view)
    {
        // Disable buttons
        buttonStartELD.setEnabled(false);
        buttonUploadELD.setEnabled(false);
        buttonDeleteELD.setEnabled(false);

        // Delete all adapter recorded records
        blueFire.ELD.DeleteRecords(blueFire.ELD.CurrentRecordNo());

        // Clear the ELD data from the page
        clearELDData();

        // Enable start button
        buttonStartELD.setEnabled(true);

        Toast.makeText(this, "ELD data has been deleted.", Toast.LENGTH_LONG).show();
    }

    private void showELDPage()
    {
        // Initialize ELD parameters
        if (!blueFire.ELD.IsStarted())
        {
            getELDParms();
            setELDParms();
        }

        // Show ELD memory
        showELDRemaining();

        // Set start button
        buttonStartELD.setEnabled(true);

        if (blueFire.ELD.IsStarted())
            buttonStartELD.setText("Stop ELD");
        else
            buttonStartELD.setText("Start ELD");

        // Check for third party securing access
        if (blueFire.ELD.IsAccessSecured())
        {
            // Disable ELD parameters
            enableELDParms(false);

            checkSecureELD.setChecked(false);
            checkAccessSecured.setChecked(true);

            // Disable all buttons
            buttonStartELD.setEnabled(false);
            buttonUploadELD.setEnabled(false);
            buttonDeleteELD.setEnabled(false);
        }
        else // open access
        {
            // Enable ELD parameters
            enableELDParms(true);

            // Check for any records to upload or delete
            if (blueFire.ELD.CurrentRecordNo() > 0)
            {
                buttonUploadELD.setEnabled(true);
                buttonDeleteELD.setEnabled(true);
            }
            else
            {
                buttonUploadELD.setEnabled(false);
                buttonDeleteELD.setEnabled(false);
            }
        }

        // Get current record
        blueFire.ELD.GetRecord(blueFire.ELD.CurrentRecordNo());
    }

    private void startELD()
    {
        if (!blueFire.IsConnected())
        {
            Toast.makeText(this, "The adapter is not connected.", Toast.LENGTH_LONG).show();
            return;
        }

        if (!blueFire.ELD.IsStreaming() && blueFire.ELD.RecordingMode() == RecordingModes.RecordNever)
        {
            Toast.makeText(this, "Please select either streaming or recording data.", Toast.LENGTH_LONG).show();
            return;
        }
        // if not started, edit ELD parameters
        if (!blueFire.ELD.IsStarted())
            if (!editELDParms())
                return;

        // Set Start/Stop button
        buttonStartELD.setText("Stop ELD");

        // Disable ELD parameters
        enableELDParms(false);

        // Disable upload and delete buttons
        buttonUploadELD.setEnabled(false);
        buttonDeleteELD.setEnabled(false);

        // Start recording
        if (!blueFire.ELD.IsStarted())
        {
            // Set the time in the adapter.
            // Note, must do this here so the custom record will have the correct date.
            // If no custom record is to be sent, StartRecording will also set the time.
            blueFire.SetTime();

            // Send a custom record(like app started recording)
            if (!blueFire.ELD.IsRecordingDisconnected())
                sendCustomELDRecord(myCustomRecordId1);

            blueFire.ELD.StartRecording();
        }

        // And start streaming
        blueFire.ELD.StartStreaming();
    }

    private void stopELD()
    {
        if (!blueFire.IsConnected())
            return;

        // Set Start/Stop button
        buttonStartELD.setText("Start ELD");

        // Enable ELD parameters
        enableELDParms(true);

        // Enable upload and delete buttons
        if (blueFire.ELD.RecordNo() > 0)
        {
            if (blueFire.ELD.IsRecordingLocally())
                buttonUploadELD.setText("Save Records");
            else
                buttonUploadELD.setText("Upload Data");

            buttonUploadELD.setEnabled(true);
            buttonDeleteELD.setEnabled(true);
        }

        // Send a custom record (like app stopped recording)
        sendCustomELDRecord(myCustomRecordId2);

        // Stop streaming
        blueFire.ELD.StopStreaming();

        // Stop recording
        blueFire.ELD.StopRecording();
    }

    private void showELDData()
    {
        if (blueFire.ELD.CurrentRecordNo() > 0 || blueFire.ELD.IsRecordingLocally())
            if (blueFire.ELD.RecordNo() > 0 && blueFire.ELD.RecordNo() != currentRecordNo)
            {
                // Only show the record once
                currentRecordNo = blueFire.ELD.RecordNo();

                // Show the ELD record
                showELDRecord(currentRecordNo);

                // Check for recording locally
                if (blueFire.ELD.IsRecordingLocally() && !isUploading)
                {
                    if (blueFire.ELD.IsStarted() || blueFire.ELD.LocalRecordNo() > 0)
                    {
                        blueFire.ELD.SetLocalRecordNo(blueFire.ELD.RecordNo());

                        // Write local ELD record
                        writeELDRecord();
                    }
                }
                else // recording or uploading from the adapter
                {
                    // Check for uploading records
                    if (isUploading)
                        uploadELD();
                }
            }

    }

    private void showELDRecord(int RecordNo)
    {
        // Show remaining memory
        showELDRemaining();

        // Show ELD Records

        clearELDData();

        RecordIds RecordId = RecordIds.forValue(blueFire.ELD.RecordId());

        textRecordNo.setText(String.valueOf(RecordNo));
        textRecordId.setText(RecordId.toString());

        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd yyyy HH:mm:ss");
        textTimeUTC.setText(dateFormat.format(blueFire.ELD.Date()));
        textTimeLocal.setText(dateFormat.format(blueFire.ELD.LocalDate()));

        if (RecordId == RecordIds.VIN)
        {
            labelVIN.setText("VIN");
            textVIN.setText(blueFire.ELD.VIN());
        }
        else if(RecordId == RecordIds.DriverId)
        {
            labelVIN.setText("Driver");
            textVIN.setText(blueFire.ELD.DriverId);
        }
        else
        {
            labelVIN.setText("VIN");
            if (blueFire.ELD.VIN().equals(Const.NA))
                textVIN.setText("");
            else
                textVIN.setText(blueFire.ELD.VIN());
        }

        switch (RecordId)
        {
            case IFTA:
                textDistance.setText(formatFloat(blueFire.ELD.Distance(), 0));
                textOdometer.setText(formatFloat(blueFire.ELD.Odometer(),0));
                textTotalFuel.setText(formatFloat(blueFire.ELD.TotalFuel(),2));
                textLatitude.setText(formatDecimal(blueFire.ELD.Latitude(), 7));
                textLongitude.setText(formatDecimal(blueFire.ELD.Longitude(), 7));
                break;

            case Stats:
                textDistance.setText(formatFloat(blueFire.ELD.Distance(), 0));
                textTotalHours.setText(formatFloat(blueFire.ELD.TotalHours(),2));
                textIdleHours.setText(formatFloat(blueFire.ELD.IdleHours(),2));
                textTotalFuel.setText(formatFloat(blueFire.ELD.TotalFuel(),2));
                textIdleFuel.setText(formatFloat(blueFire.ELD.IdleFuel(),2));
                break;

            case Custom:
                textRecordId.setText("Custom (" + blueFire.ELD.RecordId() + ")");
                break;

            default: // ELD
                textDistance.setText(formatFloat(blueFire.ELD.Distance(), 0));
                textOdometer.setText(formatFloat(blueFire.ELD.Odometer(),0));
                textTotalHours.setText(formatFloat(blueFire.ELD.TotalHours(),2));
                textLatitude.setText(formatDecimal(blueFire.ELD.Latitude(), 7));
                textLongitude.setText(formatDecimal(blueFire.ELD.Longitude(), 7));
                break;
        }
    }

    private void showELDRemaining()
    {
        // Show remaining memory
        textRemaining.setText(formatFloat(blueFire.ELD.RemainingPercent(),2) + "% (" + formatFloat(blueFire.ELD.RemainingTime(),2) + " hrs)");
    }

    private void clearELDData()
    {
        textRecordNo.setText("");
        textRecordId.setText("");
        textTimeUTC.setText("");
        textTimeLocal.setText("");
        textVIN.setText("");

        textDistance.setText("");
        textOdometer.setText("");
        textTotalHours.setText("");
        textIdleHours.setText("");
        textTotalFuel.setText("");
        textIdleFuel.setText("");
        textLatitude.setText("");
        textLongitude.setText("");
    }

    private void getELDParms()
    {
        // Set ELD recording parameters from the adapter

        // Note, DriverId is not persistent in the adapter

        appELDInterval = blueFire.ELD.ELDInterval;
        appAlignELD = blueFire.ELD.AlignELD;

        appRecordIFTA = blueFire.ELD.RecordIFTA;
        appIFTAInterval = blueFire.ELD.IFTAInterval;
        appAlignIFTA = blueFire.ELD.AlignIFTA;

        appRecordStats = blueFire.ELD.RecordStats;
        appStatsInterval = blueFire.ELD.StatsInterval;
        appAlignStats = blueFire.ELD.AlignStats;

        appSecureELD = blueFire.ELD.IsSecured();

        appStreamingELD = blueFire.ELD.IsStreaming();
        appRecordingMode = blueFire.ELD.RecordingMode().getValue();
    }

    private void setELDParms()
    {
        // Set ELD page parameters
        textDriverId.setText(appDriverId);

        checkAlignELD.setChecked(appAlignELD);
        textELDInterval.setText(String.valueOf(appELDInterval));

        checkRecordIFTA.setChecked(appRecordIFTA);
        textIFTAInterval.setText(String.valueOf(appIFTAInterval));
        checkAlignIFTA.setChecked(appAlignIFTA);

        checkRecordStats.setChecked(appRecordStats);
        textStatsInterval.setText(String.valueOf(appStatsInterval));
        checkAlignStats.setChecked(appAlignStats);

        checkSecureELD.setChecked(appSecureELD);

        checkStreamingELD.setChecked(appStreamingELD);
        checkRecordingConnected.setChecked(blueFire.ELD.IsRecordingConnected()); // comes from appRecordingMode
        checkRecordingDisconnected.setChecked(blueFire.ELD.IsRecordingDisconnected()); // comes from appRecordingMode

        setELDStreamingText();
    }

    private void setELDStreamingText()
    {
        // Streaming text
        if (blueFire.ELD.IsStreaming())
        {
            if (blueFire.ELD.IsRecordingConnected())
                textStreamingELD.setText("Stream locally");
            else
                textStreamingELD.setText("Stream and record locally");
        }
    }

    private boolean editELDParms()
    {
        // Edit driver id
        String driverId = textDriverId.getText().toString().trim();
        if (driverId.length() > blueFire.ELD.CustomDataLength)
        {
            Toast.makeText(this, "The Driver Id must be less than " + (blueFire.ELD.CustomDataLength + 1) + ".", Toast.LENGTH_LONG).show();
            return false;
        }
        appDriverId = driverId;

        // Edit intervals and alignments
        if (!editELDInterval())
            return false;

        if (!editIFTAInterval())
            return false;

        if (!editStatsInterval())
            return false;

        // Set ELD parameters
        blueFire.ELD.DriverId = appDriverId; // not persistent by adapter

        blueFire.ELD.ELDInterval = appELDInterval;
        blueFire.ELD.AlignELD = appAlignELD;

        blueFire.ELD.RecordIFTA = appRecordIFTA;
        blueFire.ELD.IFTAInterval = appIFTAInterval;
        blueFire.ELD.AlignIFTA = appAlignIFTA;

        blueFire.ELD.RecordStats = appRecordStats;
        blueFire.ELD.StatsInterval = appStatsInterval;
        blueFire.ELD.AlignStats = appAlignStats;

        return true;
    }

    private void enableELDParms(boolean isEnable)
    {
        // Enable/Disable ELD page parameters
        textDriverId.setEnabled(isEnable);

        checkAlignELD.setEnabled(isEnable);
        textELDInterval.setEnabled(isEnable);

        checkRecordIFTA.setEnabled(isEnable);
        textIFTAInterval.setEnabled(isEnable);
        checkAlignIFTA.setEnabled(isEnable);

        checkRecordStats.setEnabled(isEnable);
        textStatsInterval.setEnabled(isEnable);
        checkAlignStats.setEnabled(isEnable);

        checkSecureELD.setEnabled(isEnable);

        checkStreamingELD.setEnabled(isEnable);
        checkRecordingConnected.setEnabled(isEnable);
        checkRecordingDisconnected.setEnabled(isEnable);
    }

    private void sendCustomELDRecord(int myCustomRecordId)
    {
        // Set the custom record id
        int customId = RecordIds.Custom.getValue() + myCustomRecordId;

        // Set the data to whatever you want
        byte[] customData = new byte[blueFire.ELD.CustomDataLength];
        customData[0] = 1;
        customData[1] = 2;

        // Send the custom record to the adapter
        blueFire.ELD.WriteRecord(customId, customData);
    }

    private boolean editInterval(String intervalText, boolean align)
    {
        float interval = -1;
        try
        {
            interval = Float.parseFloat(intervalText.trim());
        }

        catch(Exception e){}

        if (interval <= 0)
        {
            Toast.makeText(this, "Interval must be greater than 0.", Toast.LENGTH_LONG).show();
            return false;
        }

        if (align)
        {
            if (!blueFire.ELD.IsHourAligned(interval))
            {
                Toast.makeText(this, "Interval cannot be aligned.", Toast.LENGTH_LONG).show();
                return false;
            }
        }

        return true;
    }

    private boolean editELDInterval()
    {
        // Edit ELD interval and alignment
        String  intervalText = textELDInterval.getText().toString().trim();
        boolean align = checkAlignELD.isChecked();

        if (!editInterval(intervalText, align))
            return false;

        appELDInterval = Float.parseFloat(intervalText);
        appAlignELD = align;

        return true;
    }

    private boolean editIFTAInterval()
    {
        // Edit IFTA interval and alignment
        String intervalText = textIFTAInterval.getText().toString().trim();
        boolean align = checkAlignIFTA.isChecked();

        if (!editInterval(intervalText, align))
            return false;

        appIFTAInterval = Float.parseFloat(intervalText);
        appAlignIFTA = align;

        return true;
    }

    private boolean editStatsInterval()
    {
        // Edit Stats interval and alignment
        String intervalText = textStatsInterval.getText().toString().trim();
        boolean align = checkAlignStats.isChecked();

        if (!editInterval(intervalText, align))
            return false;

        appStatsInterval = Float.parseFloat(intervalText);
        appAlignStats = align;

        return true;
    }

    private void uploadELD()
    {
        // Record the ELD record someplace
        writeELDRecord();

        // Check for more records
        if (currentRecordNo < blueFire.ELD.CurrentRecordNo())
            blueFire.ELD.GetRecord(currentRecordNo + 1);

            // No more records, done uploading
        else
        {
            // Stop the upload
            isUploading = false;

            blueFire.ELD.StopUpload();

            // Enable buttons
            buttonStartELD.setEnabled(true);
            buttonUploadELD.setEnabled(true);
            buttonDeleteELD.setEnabled(true);

            if (blueFire.ELD.IsRecordingLocally())
                Toast.makeText(this, "The ELD records were saved.", Toast.LENGTH_LONG).show();
            else
                Toast.makeText(this, "The ELD Upload is completed.", Toast.LENGTH_LONG).show();
        }
    }

    private void writeELDRecord()
    {
        // Do something with the record data
        Log.d("Upload", String.valueOf(blueFire.ELD.RecordNo()) + "," + String.valueOf(blueFire.ELD.RecordId()));
    }

    // *********************************** End ELD *******************************************

    //endregion

    //region Formatting

    private String formatInt(int data)
    {
        if (data < 0)
            return "NA";
        else
            return String.valueOf(data);
    }

    private String formatFloat(float data, int precision)
    {
        if (data < 0)
            return "NA";

        return formatDecimal(data, precision);
    }

    private String formatDecimal(double data, int precision)
    {
        BigDecimal bd = new BigDecimal(data);
        bd = bd.setScale(precision, RoundingMode.HALF_UP);
        return String.valueOf(bd.floatValue());
    }

    //endregion

    //region Show Message

    private void showMessage(String title, String message)
    {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle(title);
        alert.setMessage(message);
        alert.show();
    }

    //endregion

    //region Log Notifications

    private void logNotifications(String notification)
    {
        logNotifications(notification, false);
    }

    private void logNotifications(String message, boolean showMessage)
    {
        if (message.equals(""))
            return;

        Log.d("BlueFire", message);

        if (textNotifications == null)
            return;

        String textNotification;
        if (showMessage)
        {
            textNotification = textNotifications.getText().toString().trim();
            if (!textNotification.equals(""))
                textNotification += Const.CrLf;

            textNotification += message;

            textNotifications.setText(textNotification);
        }
    }

    //endregion

    //region Event Handler

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

    // BlueFire Event Handler Thread
    private class ReceiveEventsThreading extends Thread
    {
        public void run()
        {
            while (true)
            {
                if (!EventsQueue.isEmpty())
                {
                    final Message handleMessage = EventsQueue.poll();
                    if (handleMessage != null)
                    {
                        runOnUiThread(new Runnable()
                        {
                            public void run()
                            {
                                processEvent(handleMessage);
                            }
                        });
                    }
                }
                threadSleep(1); // allow other threads to execute
            }
        }
    }

    private void processEvent(Message msg)
    {
        try
        {
            connectionState = ConnectionStates.values()[msg.what];
            connectionMessage = (String)msg.obj;

            //android.util.Log.d("BlueFire", "Main ProcessEvent, State=" + connectionState);

            switch (connectionState)
            {
                case Initializing:
                case Initialized:
                case Discovering:
                case Disconnecting:
                case Connected:
                    // Status only
                    break;

                case Connecting:
                    if (!blueFire.Comm.IsReconnecting)
                        logNotifications("Connection Attempt " + blueFire.ConnectAttempts(), true);
                    break;

                case IsReady:
                    adapterConnected();
                    break;

                case NotAuthenticated:
                    adapterNotAuthenticated();
                    break;

                case Disconnected:
                    adapterDisconnected();
                    break;

                case Reconnecting:
                    logNotifications("Reconnect Attempt " + blueFire.Comm.ReconnectAttempt, true);
                    adapterReconnecting();
                    break;

                case Reconnected:
                    adapterReconnected();
                    break;

                case NotReconnected:
                    adapterNotReconnected();
                    break;

                case CANStarting:
                    startingCAN();
                    break;

                case J1708Restarting:
                    j1708Restarting();
                    break;

                case ELDConnected:
                    connectedELD();
                    break;

                case NotConnected:
                    adapterNotConnected();
                    break;

                case DataChanged:
                    showData();
                    break;

                case Heartbeat:
                    showHeartbeat();
                    return; // do not show status

                case CANFilterFull:
                    logNotifications("The CAN Filter is Full", true);
                    showMessage("Adapter Data Retrieval", "The CAN Filter is Full. Some data will not be retrieved.");
                    break;

                case Notification:
                    logNotifications("API Notification - " + connectionMessage, true);
                    return; // do not show status

                case AdapterMessage:
                    logNotifications("Adapter Message - " + connectionMessage, true);
                    return; // do not show status

                case AdapterReboot:
                    if (appMaxReconnectAttempts == 0) // no reconnect attempt
                        adapterNotConnected();
                    else
                        blueFire.Comm.Reconnect();

                    logNotifications("Adapter Rebooting - " + connectionMessage, true);
                    return; // do not show status

                case DataError:
                    if (appMaxReconnectAttempts == 0) // no reconnect attempt
                        adapterNotConnected();
                    logNotifications("Adapter Data Error - " + connectionMessage, true);
                    return; // do not show status

                case DataTimeout:
                    if (appMaxReconnectAttempts == 0) // no reconnect attempt
                        adapterNotConnected();
                    logNotifications("Adapter Data Timeout - Lost connection with the Adapter", true);
                    return; // do not show status

                case BluetoothTimeout:
                    adapterNotConnected();
                    logNotifications("API Bluetooth Timeout - Unable to connect to Bluetooth." , true);
                    break;

                case AdapterTimeout:
                    adapterNotConnected();
                    logNotifications("Adapter Connection Timeout - Bluetooth unable to connect to the Adapter." , true);
                    break;

                case SystemError:
                    adapterNotConnected();
                    logNotifications("API System Error - " + connectionMessage , true);
                    showMessage("API System Error", "See the log for details.");
                    break;
            }

            showStatus();
        }
        catch (Exception e) {}
    }

    //endregion

    //region Conversions

    private byte[] hexStringToBytes(String hexString)
    {
        byte[] hexBytes = new byte[8];

        byte[] StringBytes = new BigInteger(hexString,16).toByteArray();

        int index = 7;
        for (int i = StringBytes.length-1; i >= 0; i--)
        {
            hexBytes[index] = StringBytes[i];
            if (index == 0)
                break;
            index--;
        }

        return hexBytes;
    }

    private String bytesToHexString(byte[] hexBytes)
    {
        // Convert hex bytes to a hex string.
        // Note, this will pad < 8 bytes with hex 0.

        String hexString = ("0000000000000000" + new BigInteger(1, hexBytes).toString(16));

        int startIndex = hexString.length()-16;
        if (startIndex > 16)
            startIndex = 16;

        return hexString.substring(startIndex);
    }

    private float celciusToFarenheit(float temp)
    {
        if (temp < 0)
            return -1;
        else
            return (temp * 1.8F + 32F);
    }

    private float farenheitToCelcius(float temp)
    {
        if (temp < 0)
            return -1;
        else
            return ((temp -32) / 1.8F);
    }

    //endregion

    //region Thread Sleep

    private void threadSleep(int Interval)
    {
        try
        {
            Thread.sleep(Interval);
        }
        catch(Exception ex) {}
    }

    //endregion

    //region Back Button

    @Override
    public void onBackPressed()
    {
        try
        {
            // Check if leaving the Vehicle page
            if (layoutVehicle.getVisibility() == View.VISIBLE)
            {
                saveSettings();

                layoutVehicle.setVisibility(View.INVISIBLE);
                layoutAdapter.setVisibility(View.VISIBLE);

                stopVehicleData();

                return;
            }
            // Check for leaving the ELD page
            if (layoutELD.getVisibility() == View.VISIBLE)
            {
                if (!editELDParms())
                    return;

                getELDParms();
                saveSettings();

                layoutELD.setVisibility(View.INVISIBLE);
                layoutAdapter.setVisibility(View.VISIBLE);

                return;
            }

            // Exiting the app, save settings
            saveSettings();

            // Notify Android
            super.onBackPressed();

            // Disconnect the adapter
            blueFire.Disconnect();

            // Kill the app to ensure all resources (like BLE) are released.
            // Note, this will close the BLE GATT connection if for some reason
            // Android is keeping it open.
            // Note, the app will be killed but most likely Android will restart it
            // so it will show up under Settings/Apps but all the App and API resources
            // will be stopped and restarted.
            if (killApp)
                Process.killProcess(appPid);
        }
        catch (Exception e) {}
    }

    //endregion

    //region Destroy

    @Override
    protected void onDestroy()
    {
        super.onDestroy();

        saveSettings();

        blueFire.Dispose();
    }

    //endregion

}
