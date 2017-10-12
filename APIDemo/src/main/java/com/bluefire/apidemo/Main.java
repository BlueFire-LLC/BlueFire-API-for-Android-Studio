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
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bluefire.api.BlueFire;
import com.bluefire.api.ConnectionStates;
import com.bluefire.api.Const;
import com.bluefire.api.RecordIds;
import com.bluefire.api.RecordingModes;
import com.bluefire.api.RetrievalMethods;
import com.bluefire.api.SleepModes;
import com.bluefire.api.Truck;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;

public class Main extends Activity
{
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
    private EditText textPGNData;

    private TextView textNotifications;

    private CheckBox checkUseBT21;
    private CheckBox checkUseBLE;
    private CheckBox checkUseJ1939;
    private CheckBox checkUseJ1708;
    private CheckBox checkSecureDevice;
    private CheckBox checkSecureAdapter;
    private CheckBox checkConnectLastAdapter;

    private Button buttonConnect;
    private Button buttonNextFault;
    private Button buttonResetFault;
    private Button buttonUpdate;
    private Button buttonSendMonitor;
    private Button buttonTruckData;
    private Button buttonELDData;
    private Button buttonTest;
    private Button buttonStartService;
    private Button buttonStopService;

    // Truck Layout
    private RelativeLayout layoutTruck;

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
    private boolean isConnected;
    private boolean isConnectButton;

    private int pgn;
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
    private static final int maxGroupNo = 8;

    private boolean IsRetrievingVINID;

    private RetrievalMethods retrievalMethod;
    private int retrievalInterval;

    private static final int myCustomRecordId1 = 1;
    private static final int myCustomRecordId2 = 2;

    private ConnectAdapterThread connectThread;

    private GetTruckInfoThread getTruckInfoThread;

    private boolean isKeyOn;

    private boolean secureDevice = false;
    private boolean secureAdapter = false;

    private ConnectionStates connectionState = ConnectionStates.NotConnected;

    // BlueFire adapter and service
    private BlueFire blueFire;
    private Service demoService;

    private boolean isStartingService;

    // BlueFire App settings\

    private SharedPreferences settings;
    private SharedPreferences.Editor settingsSave;

    private boolean appUseBLE = false;
    private boolean appUseBT21 = false;

    private int appLedBrightness = 100;
    private SleepModes appSleepMode = SleepModes.NoSleep;
    private boolean appPerformanceMode = false;
    public int appMinInterval;

    private boolean appIgnoreJ1939 = false;
    private boolean appIgnoreJ1708 = false;

    private String appDeviceId = "";
    private String appAdapterId = "";
    private boolean appConnectToLastAdapter = false;

    private String appUserName = "";
    private String appPassword = "";
    private boolean appSecureDevice = false;
    private boolean appSecureAdapter = false;

    public int appDiscoveryTimeOut = 5 * Const.OneSecond;
    public int appMaxConnectAttempts = 5;
    public int appMaxReconnectAttempts = 5;
    public int appMaxBluetoothRecycleAttempt = 2;

    private boolean appOptimizeDataRetrieval = false;

    // ELD settings
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

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        logNotifications("API Demo started.");

        blueFire = new BlueFire(this, eventHandler);

        this.setTitle("API Demo v-" + blueFire.APIVersion());

        // Set to use an insecure connection.
        // Note, there are other Android devices that require this other than just ICS (4.0.x).
        if (android.os.Build.VERSION.RELEASE.startsWith("4.0."))
            blueFire.SetUseInsecureConnection(true);
        else
            blueFire.SetUseInsecureConnection(false);

        // Establish settings persistent storage
        settings = this.getPreferences(Context.MODE_PRIVATE);
        settingsSave = settings.edit();

        // Get application settings
        getSettings();

        // Initialize adapter properties
        initializeAdapter();

        // Initialize the app startup form
        initializeForm();
    }

    private void getSettings()
    {
        // Get the application settings
        // Note, these should be retrieved from persistent storage.
        appUseBLE = settings.getBoolean("UseBLE", true);
        appUseBT21 = settings.getBoolean("UseBT21", false);
        appIgnoreJ1939 = settings.getBoolean("IgnoreJ1939", false);
        appIgnoreJ1708 = settings.getBoolean("IgnoreJ1708", true);
        appPerformanceMode = settings.getBoolean("IsPerformanceModeOn", false);
        appSecureDevice = settings.getBoolean("SecureDevice", false);
        appSecureAdapter = settings.getBoolean("SecureAdapter", false);
        appConnectToLastAdapter = settings.getBoolean("ConnectToLastAdapter", false);
        appDeviceId = settings.getString("DeviceId", "");
        appAdapterId = settings.getString("AdapterId", "");
        appUserName = settings.getString("UserName", "");
        appPassword = settings.getString("Password", "");
        appLedBrightness = settings.getInt("LedBrightness", 100);
        appMinInterval = settings.getInt("MinInterval", 0);
        appDiscoveryTimeOut = settings.getInt("_DiscoveryTimeout", 5 * Const.OneSecond);
        appMaxConnectAttempts = settings.getInt("MaxConnectAttempts", 5);
        appMaxReconnectAttempts = settings.getInt("MaxReconnectAttempts", 5);
        appMaxBluetoothRecycleAttempt = settings.getInt("MaxBluetoothRecycleAttempt",2);
        appOptimizeDataRetrieval = settings.getBoolean("appOptimizeDataRetrieval", true);

        // Get ELD settings
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

    private void saveSettings()
    {
        // Save the application settings.
        settingsSave.putBoolean("UseBLE", appUseBLE);
        settingsSave.putBoolean("UseBT21", appUseBT21);
        settingsSave.putBoolean("IgnoreJ1939", appIgnoreJ1939);
        settingsSave.putBoolean("IgnoreJ1708", appIgnoreJ1708);
        settingsSave.putBoolean("IsPerformanceModeOn", appPerformanceMode);
        settingsSave.putBoolean("SecureDevice", appSecureDevice);
        settingsSave.putBoolean("SecureAdapter", appSecureAdapter);
        settingsSave.putBoolean("ConnectToLastAdapter", appConnectToLastAdapter);
        settingsSave.putString("DeviceId", appDeviceId);
        settingsSave.putString("AdapterId", appAdapterId);
        settingsSave.putString("UserName", appUserName);
        settingsSave.putString("Password", appPassword);
        settingsSave.putInt("LedBrightness", appLedBrightness);
        settingsSave.putInt("MinInterval", appMinInterval);
        settingsSave.putInt("_DiscoveryTimeout", appDiscoveryTimeOut);
        settingsSave.putInt("MaxConnectAttempts", appMaxConnectAttempts);
        settingsSave.putInt("MaxReconnectAttempts", appMaxReconnectAttempts);
        settingsSave.putInt("MaxBluetoothRecycleAttempt", appMaxBluetoothRecycleAttempt);
        settingsSave.putBoolean("appOptimizeDataRetrieval", appOptimizeDataRetrieval);

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

    private void initializeAdapter()
    {
        // Set Bluetooth adapter type
        blueFire.UseBLE = appUseBLE;
        blueFire.UseBT21 = appUseBT21;

        // Set to ignore data bus settings
        blueFire.SetIgnoreJ1939(appIgnoreJ1939);
        blueFire.SetIgnoreJ1708(appIgnoreJ1708);

        // Set the minimum interval
        blueFire.SetMinInterval(appMinInterval);

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
        // Note: Bluetooth Classic (UseBT21) uses Com sockets and they can block for a
        // considerably amount of time depending on the OEM device. It is therefore recommended
        // that you adjust the MaxConnectAttempts, MaxReconnectAttempts, and the DiscoveryTimeout
        // to compensate for this duration.
        blueFire.SetMaxConnectAttempts(appMaxConnectAttempts);
        blueFire.SetMaxReconnectAttempts(appMaxReconnectAttempts);
        blueFire.SetBluetoothRecycleAttempt(appMaxBluetoothRecycleAttempt);

        // Set the device and adapter ids
        blueFire.SetDeviceId(appDeviceId);
        blueFire.SetAdapterId(appAdapterId);

        // Set the connect to last adapter setting
        blueFire.SetConnectToLastAdapter(appConnectToLastAdapter);

        // Set the adapter security parameters
        blueFire.SetSecurity(appSecureDevice, appSecureAdapter, appUserName, appPassword);

        // Set to optimize data retrieval
        blueFire.SetOptimizeDataRetrieval(appOptimizeDataRetrieval);

        // Set streaming and recording mode
        blueFire.ELD.SetStreaming(appStreamingELD);
        blueFire.ELD.SetRecordingMode(RecordingModes.forValue(appRecordingMode));
    }

    private void initializeForm()
    {
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
        textPGNData = (EditText) findViewById(R.id.textPGNData);

        textNotifications = (TextView) findViewById(R.id.textNotifications);

        checkUseBT21 = (CheckBox) findViewById(R.id.checkUseBT21);
        checkUseBLE = (CheckBox) findViewById(R.id.checkUseBLE);
        checkUseJ1939 = (CheckBox) findViewById(R.id.checkUseJ1939);
        checkUseJ1708 = (CheckBox) findViewById(R.id.checkUseJ1708);
        checkSecureDevice = (CheckBox) findViewById(R.id.checkSecureDevice);
        checkSecureAdapter = (CheckBox) findViewById(R.id.checkSecureAdapter);
        checkConnectLastAdapter = (CheckBox) findViewById(R.id.checkConnectLastAdapter);

        buttonConnect = (Button) findViewById(R.id.buttonConnect);
        buttonNextFault = (Button) findViewById(R.id.buttonNextFault);
        buttonResetFault = (Button) findViewById(R.id.buttonResetFault);
        buttonUpdate = (Button) findViewById(R.id.buttonUpdate);
        buttonSendMonitor = (Button) findViewById(R.id.buttonSendMonitor);
        buttonTruckData = (Button) findViewById(R.id.buttonTruckData);
        buttonELDData = (Button) findViewById(R.id.buttonELDData);
        buttonTest = (Button) findViewById(R.id.buttonTest);
        buttonStartService = (Button) findViewById(R.id.buttonStartService);
        buttonStopService = (Button) findViewById(R.id.buttonStopService);

        // Truck layout
        layoutTruck = (RelativeLayout) findViewById(R.id.layoutTruck);

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

        buttonSendMonitor.setEnabled(false);

        buttonNextFault.setVisibility(View.INVISIBLE);
        buttonResetFault.setVisibility(View.INVISIBLE);

        buttonTruckData.setEnabled(false);
        buttonELDData.setEnabled(false);
        buttonTest.setEnabled(false);

        textStatus.setText("Not Connected");

        buttonConnect.setFocusable(true);
        buttonConnect.setFocusableInTouchMode(true);
        buttonConnect.requestFocus();
    }

    private void clearForm()
    {
        // Disable adapter parameters
        enableAdapterParms(false);

        textHeartbeat.setText("0");

        faultIndex = -1;

        isRetrievingFaults = false;

        // Show user settings
        checkUseBT21.setChecked(appUseBT21);
        checkUseBLE.setChecked(appUseBLE);
        checkUseJ1939.setChecked(!appIgnoreJ1939); // checkUseJ1939 is the opposite of ignoreJ1939
        checkUseJ1708.setChecked(!appIgnoreJ1708); // checkUseJ1708 is the opposite of ignoreJ1708
        checkSecureDevice.setChecked(appSecureDevice);
        checkSecureAdapter.setChecked(appSecureAdapter);
        checkConnectLastAdapter.setChecked(appConnectToLastAdapter);

        textUserName.setText(appUserName);
        textPassword.setText(appPassword);
        textLedBrightness.setText(String.valueOf(appLedBrightness));

        textNotifications.setText("");

        // ELD
        setELDParms();
    }

    private void enableAdapterParms(boolean isEnable)
    {
        // Enable/Disable Adapter page parameters

        checkSecureDevice.setEnabled(isEnable);
        checkSecureAdapter.setEnabled(isEnable);

        textPGN.setEnabled(isEnable);
        textPGNData.setEnabled(isEnable);
    }

    // Connect Button
    public void onConnectClick(View view)
    {
        try
        {
            if (isConnectButton)
            {
                if (!EditLEDBrightness())
                    return;

                clearForm();

                isConnecting = true;
                isConnected = false;

                connectionState = ConnectionStates.NA;
                textStatus.setText("Connecting...");

                checkUseBT21.setEnabled(false);
                checkUseBLE.setEnabled(false);

                checkUseJ1939.setEnabled(false);
                checkUseJ1708.setEnabled(false);

                showDisconnectButton();

                enableAdapterParms(false);

                buttonUpdate.setEnabled(false);
                buttonSendMonitor.setEnabled(false);

                buttonNextFault.setVisibility(View.INVISIBLE);
                buttonResetFault.setVisibility(View.INVISIBLE);

                checkBluetoothPermissions();
            } else
            {
                Thread.sleep(500); // allow eld to stop before disconnecting

                disconnectAdapter();
            }
        } catch (Exception ex)
        {
        }
    }

    // Start Service Button
    public void onStartServiceClick(View view)
    {
        isStartingService = true;

        enableAdapterParms(false);

        buttonStartService.setEnabled(false);
        buttonStopService.setEnabled(true);

        buttonConnect.setEnabled(false);
        buttonUpdate.setEnabled(false);
        buttonSendMonitor.setEnabled(false);

        buttonTruckData.setEnabled(false);
        buttonELDData.setEnabled(false);
        buttonTest.setEnabled(false);

        checkBluetoothPermissions();
    }

    // Stop Service Button
    public void onStopServiceClick(View view)
    {
        if (demoService == null)
            return;

        demoService.stopService();

        enableAdapterParms(false);

        buttonStartService.setEnabled(true);
        buttonStopService.setEnabled(false);

        buttonConnect.setEnabled(true);
        buttonUpdate.setEnabled(true);
        buttonSendMonitor.setEnabled(true);

        buttonTruckData.setEnabled(true);
        buttonELDData.setEnabled(true);
        buttonTest.setEnabled(true);
    }

    private void checkBluetoothPermissions()
    {
        // BLE adapters require Android 6.0 and the user must accept location access permission
        if (appUseBLE)
        {
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
        // Bluetooth Classic adapter is good to go
        else
            startConnection();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults)
    {
        // Check for user granting permission.
        // Note, iff request is cancelled, the result arrays are empty.

        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
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

    // Start Service
    public void StartService()
    {
        if (demoService == null)
            demoService = new Service(this);

        demoService.startService();
    }

    private class ConnectAdapterThread extends Thread
    {
        public void run()
        {
            // Initialize adapter properties (in case they were changed)
            initializeAdapter();

            // Connect to the adapter.
            // Note, this is a blocking call and must run in it's own thread.
            blueFire.Connect();
        }
    }

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

    private void disconnectAdapter()
    {
        try
        {
            enableAdapterParms(false);

            buttonConnect.setEnabled(false);
            buttonUpdate.setEnabled(false);
            buttonSendMonitor.setEnabled(false);

            buttonNextFault.setVisibility(View.INVISIBLE);
            buttonResetFault.setVisibility(View.INVISIBLE);

            buttonTruckData.setEnabled(false);
            buttonELDData.setEnabled(false);
            buttonTest.setEnabled(false);

            buttonStartService.setEnabled(false);
            buttonStopService.setEnabled(false);

            // Wait for the adapter to disconnect so that the Connect button
            // is not displayed too prematurely.
            boolean WaitForDisconnect = isConnected; // just for code clarity
            blueFire.Disconnect(WaitForDisconnect);

        } catch (Exception e)
        {
        }
    }

    private void j1708Restarting()
    {
        // Re-retrieve truck data
        if (isTesting)
            StartTest();
        else
            getTruckData();
    }

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

        // Set to receive notifications from the adapter.
        // Note, this should only be used during testing.
        blueFire.SetNotificationsOn(true);

        // Get the device and adapter ids
        appDeviceId = blueFire.DeviceId();
        appAdapterId = blueFire.AdapterId();

        // Check for API setting the adapter data
        appUseBT21 = blueFire.UseBT21;
        appUseBLE = blueFire.UseBLE;

        // Save any changed data from the API
        saveSettings();

        checkUseBT21.setChecked(appUseBT21);
        checkUseBLE.setChecked(appUseBLE);

        // Set the adapter led brightness
        blueFire.SetLedBrightness(appLedBrightness);

        // Set the performance mode
        blueFire.SetPerformanceModeOn(appPerformanceMode);

        // Get adapter data
        blueFire.GetMessages();
    }

    private void adapterNotAuthenticated()
    {
        logNotifications("Adapter not authenticated.");

        adapterNotConnected();

        Toast.makeText(this, "You are not authorized to access this adapter. Check for the correct adapter, the 'connect to last adapter' setting, or your user name and password.", Toast.LENGTH_LONG).show();
    }

    private void adapterConnected()
    {
        isConnected = true;
        isConnecting = false;

        // Enable adapter parameters
        enableAdapterParms(true);

        // Enable buttons
        showDisconnectButton();
        buttonUpdate.setEnabled(true);
        buttonSendMonitor.setEnabled(true);

        buttonNextFault.setVisibility(View.INVISIBLE);
        buttonResetFault.setVisibility(View.INVISIBLE);

        buttonTruckData.setEnabled(true);
        buttonELDData.setEnabled(true);
        buttonTest.setEnabled(true);

        buttonConnect.requestFocus();

        // Connect to ELD
        blueFire.ELD.Connect();

        // Get adapter data
        getAdapterData();

        if (isTesting)
            StartTest();

        String Message = "Adapter connected.";
        logNotifications(Message);
        Toast.makeText(this, Message, Toast.LENGTH_LONG).show();
    }

    private void adapterDisconnected()
    {
        adapterNotConnected(false);

        String Message = "Adapter disconnected.";
        logNotifications(Message);
        Toast.makeText(this, Message, Toast.LENGTH_LONG).show();
    }

    private void adapterNotConnected()
    {
        adapterNotConnected(true);
    }
    private void adapterNotConnected(boolean logMessage)
    {
        isConnected = false;
        isConnecting = false;

        showConnectButton();

        enableAdapterParms(false);

        checkUseBT21.setEnabled(true);
        checkUseBLE.setEnabled(true);

        checkUseJ1939.setEnabled(true);
        checkUseJ1708.setEnabled(true);

        buttonUpdate.setEnabled(true);
        buttonSendMonitor.setEnabled(false);

        buttonConnect.requestFocus();

        showStatus();

        if (logMessage)
        {
            String Message = "Adapter not connected.";
            logNotifications(Message);
            Toast.makeText(this, Message, Toast.LENGTH_LONG).show();
        }
    }

    private void adapterReconnecting()
    {
        isConnected = false;
        isConnecting = true;

        enableAdapterParms(false);

        buttonConnect.setEnabled(true);
        buttonUpdate.setEnabled(false);
        buttonSendMonitor.setEnabled(false);

        logAPINotifications();

        String Message = "Connecting the Adapter.";
        logNotifications(Message);
        Toast.makeText(this, Message, Toast.LENGTH_SHORT).show();
    }

    private void adapterReconnected()
    {
        logNotifications("Adapter reconnected.");

        adapterConnected();
    }

    private void adapterNotReconnected()
    {
        adapterNotConnected(false);

        String Message = "Adapter connection failed.";
        logNotifications(Message);
        Toast.makeText(this, Message, Toast.LENGTH_LONG).show();
    }

    // BT21 Checkbox
    public void onUseBT21Check(View view)
    {
        appUseBT21 = checkUseBT21.isChecked();

        if (appUseBT21)
        {
            appUseBLE = false;
            checkUseBLE.setChecked(false);
        }
    }

    // BLE Checkbox
    public void onUseBLECheck(View view)
    {
        appUseBLE = checkUseBLE.isChecked();

        if (appUseBLE)
        {

            if (appUseBLE)


                appUseBT21 = false;
            checkUseBT21.setChecked(false);
        }
    }

    private boolean EditLEDBrightness()
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

        return true;
    }

    // Connect to Last Adapter Checkbox
    public void onConnectLastAdapterCheck(View view)
    {
        appConnectToLastAdapter = checkConnectLastAdapter.isChecked();

        if (appConnectToLastAdapter)
            blueFire.SetAdapterId(appAdapterId);
        else
            blueFire.SetAdapterId("");
    }

    // Secure Device Checkbox
    public void onSecureDeviceCheck(View view)
    {
        secureDevice = checkSecureDevice.isChecked();
    }

    // Secure Adapter Checkbox
    public void onSecureAdapterCheck(View view)
    {
        secureAdapter = checkSecureAdapter.isChecked();
    }

    // J1939 Checkbox
    public void onUseJ1939Check(View view)
    {
        // Set to ignore J1939 (opposite of checkUseJ1939)
        appIgnoreJ1939 = !checkUseJ1939.isChecked();

        // Update BlueFire
        blueFire.SetIgnoreJ1939(appIgnoreJ1939);
    }

    // J1708 Checkbox
    public void onUseJ1708Check(View view)
    {
        // Set to ignore J708 (opposite of checkUseJ1708)
        appIgnoreJ1708 = !checkUseJ1708.isChecked();

        // Update BlueFire
        blueFire.SetIgnoreJ1708(appIgnoreJ1708);
    }

    // Fault Button
    public void onNextFaultClick(View view)
    {
        showNextFault();
    }

    // Reset Button
    public void onResetFaultClick(View view)
    {
        blueFire.ResetFaults();
    }

    // Update Button
    public void onUpdateClick(View view)
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

            if (!blueFire.UpdateSecurity(appSecureDevice, appSecureAdapter, appUserName, appPassword))
                Toast.makeText(this, "Security parameters have not been updated.", Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(this, "Security parameters have been updated.", Toast.LENGTH_SHORT).show();
        }

        buttonConnect.requestFocus();
    }

    // Send/Monitor Button
    public void onSendMonitorClick(View view)
    {
        isSendingPGN = false;
        isMonitoringPGN = false;

        // Get PGN
        pgn = -1;
        try
        {
            pgn = Integer.parseInt("0"+textPGN.getText().toString().trim());
        }
        catch(Exception e){}

        if (pgn < 0)
        {
            Toast.makeText(this, "PGN must be numeric.", Toast.LENGTH_LONG).show();
            return;
        }

        // Ignore if no PGN
        if (pgn == 0)
            return;

        // Get PGN Data
        byte[] pgnBytes = new byte[8];

        String pgnData = textPGNData.getText().toString().trim();

        if (pgnData.length() == 0) // Monitor a PGN
        {
            int source = 0; // engine
            isMonitoringPGN = true;
            blueFire.MonitorPGN(source, pgn);
        }
        else // Send a PGN
        {
            // Edit the PGN Data to be 16 hex characters (8 bytes)
            if (pgnData.length() != 16)
            {
                Toast.makeText(this, "PGN Data must be 16 hex characters (8 bytes).", Toast.LENGTH_LONG).show();
                return;
            }

            // Convert the PGN Data hex string to bytes
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
            blueFire.SendPGN(pgn,  pgnBytes);
        }
    }

    // Next Group Button
    public void onNextGroupClick(View view)
    {
        groupNo++;
        if (groupNo > maxGroupNo)
            groupNo = 0;

        getTruckData();
    }

    // Previous Group Button
    public void onPreviousGroupClick(View view)
    {
        groupNo--;
        if (groupNo < 0)
            groupNo = maxGroupNo;

        getTruckData();
    }

    // Truck Button
    public void onTruckDataClick(View view)
    {
        if (layoutTruck.getVisibility() == View.INVISIBLE)
        {
            layoutAdapter.setVisibility(View.INVISIBLE);
            layoutELD.setVisibility(View.INVISIBLE);
            layoutTruck.setVisibility(View.VISIBLE);

            startTruckData();
        }
        else
        {
            layoutTruck.setVisibility(View.INVISIBLE);
            layoutAdapter.setVisibility(View.VISIBLE);

            stopTruckData();
        }
    }

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

        StartTest();
    }

    private void StartTest()
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

        // Start monitoring all other truck data
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

    private void checkKeyState()
    {
        boolean keyIsOn = (isConnected && (blueFire.IsCANAvailable() || blueFire.IsJ1708Available()));

        if (isKeyOn != keyIsOn)
        {
            if (keyIsOn)
                textKeyState.setText("Key On");
            else
                textKeyState.setText("Key Off");

            isKeyOn = keyIsOn;
        }
    }

    private void showData()
    {
        // Show hardware and firmware versions
        textHardware.setText(blueFire.HardwareVersion());
        textFirmware.setText(blueFire.FirmwareVersion());

        // Check the key state
        checkKeyState();

        // Show truck data
        if (blueFire.IsTruckDataChanged())
            showTruckData();

        // Show ELD data
        if ( blueFire.ELD.IsDataRetrieved())
            showELDData();

        // Check for user changed adapter data while offline
        if (appLedBrightness != blueFire.LedBrightness())
            blueFire.SetLedBrightness(appLedBrightness);

        if (appPerformanceMode != blueFire.IsPerformanceModeOn())
            blueFire.SetPerformanceModeOn(appPerformanceMode);

        // Check if adapter overrode user input
        if (!appIgnoreJ1939 != blueFire.IgnoreJ1939())
        {
            appIgnoreJ1939 = blueFire.IgnoreJ1939();
            checkUseJ1939.setChecked(!appIgnoreJ1939); // checkUseJ1939 is the opposite of ignoreJ1939
        }
        if (!appIgnoreJ1708 != blueFire.IgnoreJ1708())
        {
            appIgnoreJ1708 = blueFire.IgnoreJ1708();
            checkUseJ1708.setChecked(!appIgnoreJ1708); // checkUseJ1708 is the opposite of ignoreJ1708
        }

        // Check for SendPGN response
        if ((isSendingPGN || isMonitoringPGN) && blueFire.PGNData.PGN == pgn)
        {
            isSendingPGN = false; // only show sending data once
            textPGNData.setText(bytesToHexString(blueFire.PGNData.Data).toUpperCase()); //TODO Fix Hex
        }
    }

    private void showHeartbeat()
    {
        textHeartbeat.setText(String.valueOf(blueFire.HeartbeatCount()));
    }
    private void startTruckData()
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
        getTruckData();
    }

    private void stopTruckData()
    {
        if (!blueFire.IsConnected())
            return;

        blueFire.StopDataRetrieval();
    }

    private void clearAdapterData()
    {
        testStarted = false;

        IsRetrievingVINID = false;
        isRetrievingFaults = false;

        blueFire.StopDataRetrieval();
    }

    private class GetTruckInfoThread extends Thread
    {
        public void run()
        {
            // Get the Engine VIN and Component Id
            final int nofRetries = 3;

            int retries = nofRetries;
            while (retries > 0)
            {
                blueFire.GetEngineVIN(RetrievalMethods.Synchronized);
                if (Truck.EngineVIN != Const.NA)
                    break;
                retries--;
            }

            retries = nofRetries;
            while (retries > 0)
            {
                blueFire.GetEngineId(RetrievalMethods.Synchronized);
                if (Truck.EngineMake != Const.NA)
                    break;
                retries--;
            }

            retries = nofRetries;
            while (retries > 0)
            {
                blueFire.GetTruckVIN(RetrievalMethods.Synchronized);
                if (Truck.VIN != Const.NA)
                    break;
                retries--;
            }

            retries = nofRetries;
            while (retries > 0)
            {
                blueFire.GetTruckId(RetrievalMethods.Synchronized);
                if (Truck.Make != Const.NA)
                    break;
                retries--;
            }
        }
    }

    private void getTruckData()
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
        retrievalMethod = RetrievalMethods.OnChange; // default
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
                    StartTest(); // restart testing

                if (!isTesting)
                {
                    clearAdapterData();
                    blueFire.GetEngineData3(retrievalMethod, retrievalInterval); // Vehicle Speed, Max Set Speed, Brake Switch, Clutch Switch, Park Brake Switch, Cruise Control Settings and Switches
                }
                break;

            case 6:
                textView1.setText("Engine VIN");
                textView2.setText("Make");
                textView3.setText("Model");
                textView4.setText("Serial No");
                textView5.setText("Unit No");
                textView6.setText("");
                textView7.setText("");

                if (!IsRetrievingVINID)
                {
                    clearAdapterData();

                    IsRetrievingVINID = true;

                    getTruckInfoThread = new GetTruckInfoThread();
                    getTruckInfoThread.start();
                }

                break;

            case 7:
                textView1.setText("Truck VIN");
                textView2.setText("Make");
                textView3.setText("Model");
                textView4.setText("Serial No");
                textView5.setText("Unit No");
                textView6.setText("");
                textView7.setText("");

                break;

            case 8:
                textView1.setText("Source");
                textView2.setText("SPN");
                textView3.setText("FMI");
                textView4.setText("Occurrences");
                textView5.setText("Conversion");
                textView6.setText("");
                textView7.setText("");

                if (isTesting && !testStarted)
                    StartTest(); // restart testing

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

    private void showTruckData()
    {
        switch (groupNo)
        {
            case 0:
                dataView1.setText(formatInt(Truck.RPM));
                dataView2.setText(formatFloat(Truck.Speed * Const.KphToMph,2));
                dataView3.setText(formatFloat(Truck.AccelPedal,2));
                dataView4.setText(formatInt(Truck.PctLoad));
                dataView5.setText(formatInt(Truck.PctTorque));
                dataView6.setText(formatInt(Truck.DrvPctTorque));
                dataView7.setText(String.valueOf(Truck.TorqueMode));

                break;

            case 1:
                dataView1.setText(formatFloat(Truck.Distance * Const.MetersToMiles,2)); // hi-res or converted lo-res
                dataView2.setText(formatFloat(Truck.HiResDistance * Const.MetersToMiles,2));
                dataView3.setText(formatFloat(Truck.LoResDistance * Const.KmToMiles,2));
                dataView4.setText("");
                dataView5.setText(formatFloat(Truck.Odometer * Const.MetersToMiles,2)); // hi-res or converted lo-res
                dataView6.setText(formatFloat(Truck.HiResOdometer * Const.MetersToMiles,2));
                dataView7.setText(formatFloat(Truck.LoResOdometer * Const.KmToMiles,2));

                break;

            case 2:
                dataView1.setText(formatFloat(Truck.TotalHours,3));
                dataView2.setText(formatFloat(Truck.IdleHours,3));
                dataView3.setText(formatFloat(Truck.BrakeAppPressure * Const.kPaToPSI,2));
                dataView4.setText(formatFloat(Truck.Brake1AirPressure * Const.kPaToPSI,2));
                dataView5.setText(formatInt(Truck.CurrentGear));
                dataView6.setText(formatInt(Truck.SelectedGear));
                dataView7.setText(formatFloat(Truck.BatteryPotential,2));

                break;

            case 3:
                dataView1.setText(formatFloat(Truck.FuelRate * Const.LphToGalPHr,2));
                dataView2.setText(formatFloat(Truck.FuelUsed * Const.LitersToGal,3));
                dataView3.setText(formatFloat(Truck.HiResFuelUsed * Const.LitersToGal,3));
                dataView4.setText(formatFloat(Truck.IdleFuelUsed * Const.LitersToGal,3));
                dataView5.setText(formatFloat(Truck.AvgFuelEcon * Const.KplToMpg,2));
                dataView6.setText(formatFloat(Truck.InstFuelEcon * Const.KplToMpg,2));
                dataView7.setText(formatFloat(Truck.ThrottlePos,2));

                break;

            case 4:
                dataView1.setText(formatFloat(celciusToFarenheit(Truck.OilTemp),2));
                dataView2.setText(formatFloat(Truck.OilPressure * Const.kPaToPSI,2));
                dataView3.setText(formatFloat(celciusToFarenheit(Truck.IntakeTemp),2));
                dataView4.setText(formatFloat(Truck.IntakePressure * Const.kPaToPSI,2));
                dataView5.setText(formatFloat(celciusToFarenheit(Truck.CoolantTemp),2));
                dataView6.setText(formatFloat(Truck.CoolantPressure * Const.kPaToPSI,2));
                dataView7.setText(formatFloat(Truck.CoolantLevel,2));

                break;

            case 5:
                dataView1.setText(String.valueOf(Truck.BrakeSwitch));
                dataView2.setText(String.valueOf(Truck.ClutchSwitch));
                dataView3.setText(String.valueOf(Truck.ParkBrakeSwitch));
                dataView4.setText(String.valueOf(Truck.CruiseOnOff));
                dataView5.setText(String.valueOf(Truck.CruiseState));
                dataView6.setText(formatFloat(Truck.CruiseSetSpeed * Const.KphToMph,0));
                float MaxSpeed = Truck.MaxSpeed;
                if (Truck.HiResMaxSpeed > 0)
                    MaxSpeed = Truck.HiResMaxSpeed;
                dataView7.setText(formatFloat(MaxSpeed * Const.KphToMph,0));

                break;

            case 6:
                dataView1.setText(Truck.EngineVIN);
                dataView2.setText(Truck.EngineMake);
                dataView3.setText(Truck.EngineModel);
                dataView4.setText(Truck.EngineSerialNo);
                dataView5.setText(Truck.EngineUnitNo);
                dataView6.setText("");

                // Waiting on either VIN or ID
                if (Truck.EngineVIN == Const.NA && Truck.EngineMake == Const.NA)
                    textView7.setText("Retrieving Engine VIN ...");

                // Waiting just for VIN
                else if (Truck.EngineVIN == Const.NA)
                    textView7.setText("Retrieving Engine VIN ...");

                // Waiting just for ID
                else if (Truck.EngineMake == Const.NA)
                    textView7.setText("Retrieving Engine Id ...");

                // Retrieved VIN and ID
                else
                    textView7.setText("");

                // Stop retrieving the data when all have been retrieved.
                // Note, because the VIN and ID are requested on interval, they should be stopped
                // when all have been retrieved.
                if (Truck.EngineVIN != Const.NA)
                    blueFire.StopRetrievingEngineVIN();
                if (Truck.EngineMake != Const.NA )
                    blueFire.StopRetrievingEngineId();

                if (Truck.EngineVIN != Const.NA && Truck.EngineMake != Const.NA && Truck.VIN != Const.NA && Truck.Make != Const.NA)
                    blueFire.StopDataRetrieval();

                break;

            case 7:
                dataView1.setText(Truck.VIN);
                dataView2.setText(Truck.Make);
                dataView3.setText(Truck.Model);
                dataView4.setText(Truck.SerialNo);
                dataView5.setText(Truck.UnitNo);
                dataView6.setText("");

                // Waiting on either VIN or ID
                if (Truck.VIN == Const.NA && Truck.Make == Const.NA)
                    textView7.setText("Retrieving Truck VIN ...");

                    // Waiting just for VIN
                else if (Truck.VIN == Const.NA)
                    textView7.setText("Retrieving Truck VIN ...");

                    // Waiting just for ID
                else if (Truck.Make == Const.NA)
                    textView7.setText("Retrieving Truck Id ...");

                    // Retrieved VIN and ID
                else
                    textView7.setText("");

                // Stop retrieving the data when all have been retrieved.
                // Note, because the VIN and ID are requested on interval, they should be stopped
                // when all have been retrieved.
                if (Truck.VIN != Const.NA)
                    blueFire.StopRetrievingTruckVIN();
                if (Truck.Make != Const.NA )
                    blueFire.StopRetrievingTruckId();

                break;

            case 8:
                if (Truck.GetFaultCount() == 0)
                {
                    faultCount = 0;
                    faultIndex = -1; // reset to show fault
                    buttonNextFault.setEnabled(false);
                    buttonResetFault.setEnabled(false);
                }
                else // faults found
                {
                    if (Truck.GetFaultCount() != faultCount) // additional faults
                    {
                        faultCount = Truck.GetFaultCount();
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
                    faultSource = String.valueOf(Truck.GetFaultSource(faultIndex));
                    faultSPN = String.valueOf(Truck.GetFaultSPN(faultIndex));
                    faultFMI = String.valueOf(Truck.GetFaultFMI(faultIndex));
                    faultOccurrence = String.valueOf(Truck.GetFaultOccurrence(faultIndex));
                    faultConversion = String.valueOf(Truck.GetFaultConversion(faultIndex));
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
        // Check for a change of the connection state
        if (connectionState != blueFire.ConnectionState)
        {
            connectionState = blueFire.ConnectionState;
            textStatus.setText(connectionState.toString());
        }

        // Show any error messages from the adapter
        logAPINotifications();
    }

    // *********************************** ELD *******************************************

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
            layoutTruck.setVisibility(View.INVISIBLE);
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

        SetStreamingText();
    }

    // Record Connected Checkbox
    public void onRecordingConnectedCheck(View view)
    {
        if (!blueFire.IsConnected())
            return;

        blueFire.ELD.SetRecordingMode(checkRecordingConnected.isChecked(), checkRecordingDisconnected.isChecked());

        SetStreamingText();
    }

    // Record Connected Checkbox
    public void onRecordingDisconnectedCheck(View view)
    {
        if (!blueFire.IsConnected())
            return;

        blueFire.ELD.SetRecordingMode(checkRecordingConnected.isChecked(), checkRecordingDisconnected.isChecked());

        SetStreamingText();
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

        Toast.makeText(this, "The ELD data was deleted.", Toast.LENGTH_LONG).show();
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

        SetStreamingText();
    }

    private void SetStreamingText()
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

    private void showSystemError()
    {
        logAPINotifications();

        showMessage("System Error", "See System Log for details.");
    }

    private void showMessage(String title, String message)
    {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle(title);
        alert.setMessage(message);
        alert.show();
    }

    private String logAPINotifications()
    {
        String Message = blueFire.NotificationMessage();

        if (!Message.equals(""))
        {
            if (!blueFire.NotificationLocation().equals(""))
                Message = blueFire.NotificationLocation() + " - " + Message;

            blueFire.ClearNotificationMessage();

            logNotifications(Message, true);
        }

        String AdapterMessage = logAdapterMessages();
        if (!AdapterMessage.equals(""))
            Message += Const.CrLf + AdapterMessage;

        return Message;
    }

    private String logAdapterMessages()
    {
        String Message = blueFire.Message();

        if (!Message.equals(""))
        {
            blueFire.ClearMessages();

            logNotifications(Message, true);
        }
        return Message;
    }
    private void logNotifications(String notification)
    {
        logNotifications(notification, false);
    }

    private void logNotifications(String notification, boolean showMessage)
    {
        Log.d("BlueFire", notification);

        if (textNotifications == null)
            return;

        String Message;
        if (showMessage)
        {
            Message = textNotifications.getText().toString().trim();
            if (!Message.equals(""))
                Message += Const.CrLf;

            Message += notification;

            textNotifications.setText(Message);
        }
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
                switch (blueFire.ConnectionState)
                {
                    case NotConnected:
                        if (isConnecting || isConnected) // only show once
                            adapterNotConnected();
                        break;

                    case Connecting:
                        if (blueFire.IsReconnecting())
                            if (!isConnecting) // only show once
                                adapterReconnecting();
                        break;

                    case Discovering:
                        // Status only
                        break;

                    case AdapterConnected:
                        // Status only
                        break;

                    case Authenticated:
                        if (!isConnected) // only show once
                            adapterConnected();
                        break;

                    case NotAuthenticated:
                        adapterNotAuthenticated();
                        break;

                    case Disconnecting:
                        // Status only
                        break;

                    case Disconnected:
                        if (isConnecting || isConnected) // only show once
                            adapterDisconnected();
                        break;

                    case Reconnecting:
                        adapterReconnecting();
                        break;

                    case Reconnected:
                        if (isConnecting) // only show once
                            adapterReconnected();
                        break;

                    case NotReconnected:
                        if (isConnecting) // only show once
                            adapterNotReconnected();
                        break;

                    case J1708Restarting:
                        j1708Restarting();
                        break;

                    case Notification:
                        logAPINotifications();
                        break;

                    case AdapterMessage:
                        logAdapterMessages();
                        break;

                    case CANFilterFull:
                        showMessage("Adapter Data Retrieval", "The CAN Filter is Full. Some data will not be retrieved.");
                        break;

                    case DataError:
                        // Ignore, handled by Reconnecting
                        break;

                    case CommTimeout:
                    case ConnectTimeout:
                    case AdapterTimeout:
                        if (isConnecting || isConnected) // only show once
                        {
                            blueFire.Disconnect();
                            adapterNotConnected();
                            showMessage("Adapter Connection", "The Adapter Timed Out.");
                        }
                        break;

                    case SystemError:
                        if (isConnecting || isConnected) // only show once
                        {
                            blueFire.Disconnect();
                            adapterNotConnected();
                        }
                        showSystemError();
                        break;

                    case DataChanged:
                        showData();
                        break;

                    case Heartbeat:
                        showHeartbeat();
                        return; // do not show heartbeat status
                }

                showStatus();

            }
            catch (Exception e) {}
        }
    };

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
        String hexString = ("0000000000000000" + new BigInteger(1, hexBytes).toString(16));

        return hexString.substring(hexString.length()-16);
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

    @Override
    public void onBackPressed()
    {
        try
        {
            if (layoutTruck.getVisibility() == View.VISIBLE)
            {
                saveSettings();

                layoutTruck.setVisibility(View.INVISIBLE);
                layoutAdapter.setVisibility(View.VISIBLE);

                return;
            }
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

            saveSettings();

            super.onBackPressed();

            blueFire.Disconnect();
        }
        catch (Exception e) {}
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();

        saveSettings();

        blueFire.Dispose();
    }

}
