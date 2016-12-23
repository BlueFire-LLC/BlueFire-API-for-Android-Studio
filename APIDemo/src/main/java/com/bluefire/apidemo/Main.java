//package com.bluefire.api;
package com.bluefire.apidemo;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.bluefire.api.BlueFire;
import com.bluefire.api.Const;
import com.bluefire.api.Helper;
import com.bluefire.api.Truck;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.StringUtils;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class Main extends Activity 
{
    // Form controls	
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
	
	private TextView textStatus;    
	private TextView textKeyState;    
	private TextView textFaultCode;
	private EditText textLedBrightness;
	private EditText textUserName;
	private EditText textPassword;
	private EditText textPGN;
	private EditText textPGNData;
	private CheckBox checkBT21;
	private CheckBox checkBLE;
	private CheckBox checkJ1939;
	private CheckBox checkJ1708;
	private Button buttonConnect;
	private Button buttonReset;
	private Button buttonUpdate;
	private Button buttonSendMonitor;
	private TextView textHeartbeat;
    
    private boolean isConnecting;
    private boolean isConnected;
	private boolean IsConnectButton;
 	
    private int pgn;
    private boolean isSendingPGN;
    private boolean isMonitoringPGN;

	private int faultIndex;
	
	private int groupNo;
	private static final int maxGroupNo = 6;
    
    private Timer connectTimer;
    
    private boolean isCANAvailable;
    
    private BlueFire.ConnectionStates ConnectionState = BlueFire.ConnectionStates.NotConnected;

    // BlueFire adapter
    private BlueFire blueFire;
    
    // BlueFire App settings
    private boolean appUseBLE = false;
    private boolean appUseBT21 = false;

    private int appLedBrightness = 100;
    private BlueFire.SleepModes appSleepMode = BlueFire.SleepModes.NoSleep;
    private boolean appPerformanceMode = false;
    public int appMinInterval;

    private boolean appIgnoreJ1939 = false;
    private boolean appIgnoreJ1708 = false;
    
    private String appLastConnectedId = "";
    private boolean appConnectToLastAdapter = false;
    
    private String appUserName = "";
    private String appPassword = "";
    
    public int appDiscoveryTimeOut = 10 * Const.OneSecond;
    public int appMaxConnectRetrys = 10;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.main);
		
		LogNotifications("API Demo started.");
		
		blueFire = new BlueFire(this, adapterHandler);
		
		this.setTitle("API Demo v-" + blueFire.GetAPIVersion());

        // Set to use an insecure connection.
        // Note, there are other Android devices that require this other than just ICS (4.0.x).
        if (android.os.Build.VERSION.RELEASE.startsWith("4.0."))
            blueFire.SetUseInsecureConnection(true);
        else
            blueFire.SetUseInsecureConnection(false);   	
 
        // Application settings for the BlueFire
        appUseBLE = false; // default
        appUseBT21 = true; // default
		appUserName = ""; // default
		appPassword = ""; // default
        appLedBrightness = 100; // default
        appSleepMode = BlueFire.SleepModes.NoSleep; // default
        appIgnoreJ1939 = false;
        appIgnoreJ1708 = true; // set to ignore for testing performance
        appPerformanceMode = false; // default
        appMinInterval = 0; // default
        appConnectToLastAdapter = false; // default
        appDiscoveryTimeOut = 10 * Const.OneSecond; // default
        appMaxConnectRetrys = 10; // default

        // Initialize adapter properties
        initializeAdapter();
        
        // Initialize the app startup form
        initializeForm();
	}
	
	private void initializeAdapter()
	{
		// Set Bluetooth adapter type
		blueFire.UseBLE = appUseBLE;
		blueFire.UseBT21 = appUseBT21;
		
         // Set the user name and password
		blueFire.SetSecurity(appUserName, appPassword);
        
        // Set the adapter led brightness
		blueFire.SetLedBrightness(appLedBrightness);
        
        // Set the performance mode
		blueFire.SetPerformanceMode(appPerformanceMode);
	       
        // Set the minimum interval
		blueFire.MinInterval = appMinInterval;				
       
        // Set the Bluetooth discovery timeout.
        // Note, depending on the number of Bluetooth devices present on the mobile device,
        // discovery could take a long time.
        // Note, if this is set to a high value, the app needs to provide the user with the
        // capability of canceling the discovery.
        blueFire.SetDiscoveryTimeOut(appDiscoveryTimeOut);
        
        // Set number of Bluetooth connection attempts.
        // Note, if the mobile device does not connect, try setting this to a value that 
        // allows for a consistent connection. If you're using multiple adapters and have 
        // connection problems, un-pair all devices before connecting.
        blueFire.SetMaxConnectRetrys(appMaxConnectRetrys);
        
        // Get the Bluetooth last connection id and the connect to last adapter setting
        blueFire.SetLastConnectedId(appLastConnectedId);
        blueFire.SetConnectToLastAdapter(appConnectToLastAdapter);
		
		// Set to ignore data bus settings
		blueFire.SetIgnoreJ1939(appIgnoreJ1939);
		blueFire.SetIgnoreJ1708(appIgnoreJ1708);
	}

	private void initializeForm()
	{
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
        
        textStatus = (TextView) findViewById(R.id.textStatus);
        textKeyState = (TextView) findViewById(R.id.textKeyState);
        textFaultCode = (TextView) findViewById(R.id.textFaultCode);
        textLedBrightness = (EditText) findViewById(R.id.textLedBrightness);
        textUserName = (EditText) findViewById(R.id.textUserName);
        textPassword = (EditText) findViewById(R.id.textPassword);
        textPGN = (EditText) findViewById(R.id.textPGN);
        textPGNData = (EditText) findViewById(R.id.textPGNData);

		checkBT21 = (CheckBox) findViewById(R.id.checkBT21);
		checkBLE = (CheckBox) findViewById(R.id.checkBLE);
		checkJ1939 = (CheckBox) findViewById(R.id.checkJ1939);
        checkJ1708 = (CheckBox) findViewById(R.id.checkJ1708);
        buttonConnect = (Button) findViewById(R.id.buttonConnect);
        buttonReset = (Button) findViewById(R.id.buttonReset);
        buttonUpdate = (Button) findViewById(R.id.buttonUpdate);
        buttonSendMonitor = (Button) findViewById(R.id.buttonSendMonitor);
        textHeartbeat = (TextView) findViewById(R.id.textHeartbeat);
        
        clearForm();

		ShowConnectButton();

        buttonReset.setEnabled(false);
        buttonSendMonitor.setEnabled(false);
        textStatus.setText("Not Connected");
    	
        buttonConnect.setFocusable(true);
        buttonConnect.setFocusableInTouchMode(true);
        buttonConnect.requestFocus();
	}
	
	private void clearForm()
	{
		ShowTruckText();
		
		// Clear form
		ShowTruckText();

		textFaultCode.setText("NA");
		textLedBrightness.setText("0");
		textUserName.setText("");
		textPassword.setText("");
		textHeartbeat.setText("0");

		faultIndex = -1;
		
    	// Show user settings
		checkBT21.setChecked(appUseBT21);
		checkBLE.setChecked(appUseBLE);
		textUserName.setText(appUserName);
        textPassword.setText(appPassword);
        textLedBrightness.setText(String.valueOf(appLedBrightness));
		checkJ1939.setChecked(!appIgnoreJ1939); // checkJ1939 is the opposite of ignoreJ1939
		checkJ1708.setChecked(!appIgnoreJ1708); // checkJ1708 is the opposite of ignoreJ1708
	}

	// Connect button
	public void onConnectClick(View view) 
	{
        if (IsConnectButton)
        {
        	clearForm();
    		
    		isConnecting = true;
    		isConnected = false;
    		
			ConnectionState = BlueFire.ConnectionStates.NA;
            textStatus.setText("Connecting...");

			checkBT21.setEnabled(false);
			checkBLE.setEnabled(false);

			checkJ1939.setEnabled(false);
			checkJ1708.setEnabled(false);

			ShowDisconnectButton();

            buttonReset.setEnabled(false);
            buttonUpdate.setEnabled(false);
            buttonSendMonitor.setEnabled(false);
            
            connectTimer = new Timer();
            connectTimer.schedule(new ConnectAdapter(), 1, Long.MAX_VALUE);
       }
        else
        	DisconnectAdapter();
	}

	private void ShowConnectButton()
	{
		IsConnectButton = true;
		buttonConnect.setText("Connect");
		buttonConnect.setEnabled(true);
	}

	private void ShowDisconnectButton()
	{
		IsConnectButton = false;
		buttonConnect.setText("Disconnect");
		buttonConnect.setEnabled(true);
	}
	private class ConnectAdapter extends TimerTask
	{
        @Override
        public void run()
        {
            // Initialize adapter properties (in case they were changed)
        	initializeAdapter();
        	
			blueFire.Connect(); // this is a blocking call
			connectTimer.cancel();
        }
	};

	private void DisconnectAdapter()
	{
		try
		{
	        buttonConnect.setEnabled(false);
	        buttonReset.setEnabled(false);
            buttonUpdate.setEnabled(true);
	        buttonSendMonitor.setEnabled(false);
	        
	        blueFire.Disconnect(true);
		}
		catch(Exception e) {}
	}

	private void AdapterConnected()
	{
		LogNotifications("Adapter connected.");
		
	   	isConnected = true;
	   	isConnecting = false;

		buttonConnect.setEnabled(true);
        buttonUpdate.setEnabled(true);
        buttonSendMonitor.setEnabled(true);
		if (faultIndex >= 0)
			buttonReset.setEnabled(true);

		buttonConnect.requestFocus();
    	
		// Get the Bluetooth connected id
		appLastConnectedId = blueFire.GetLastConnectedId();
        
		getData();
	}

	private void AdapterDisconnected()
	{
		LogNotifications("Adapter disconnected.");

		AdapterNotConnected();
	}

	private void AdapterNotConnected()
	{
		LogNotifications("Adapter not connected.");
		
	   	isConnected = false;
	   	isConnecting = false;

		ShowConnectButton();

		checkBT21.setEnabled(true);
		checkBLE.setEnabled(true);

		checkJ1939.setEnabled(true);
        checkJ1708.setEnabled(true);
        
        buttonUpdate.setEnabled(true);
        buttonSendMonitor.setEnabled(false);
    	
    	buttonConnect.requestFocus();
       
        ShowStatus();
	}

    private void AdapterReconnecting()
    {
		LogNotifications("Adapter re-connecting.");
		
    	isConnected = false;
		isConnecting = true;
    	
        buttonConnect.setEnabled(false);
        buttonUpdate.setEnabled(false);
        buttonSendMonitor.setEnabled(false);

        LogNotifications("App reconnecting to the Adapter. Reason is " + blueFire.ReconnectReason + ".");
        
		Toast.makeText(this, "Lost connection to the Adapter.", Toast.LENGTH_LONG).show();
         
    	Toast.makeText(this, "Attempting to reconnect.", Toast.LENGTH_LONG).show();
    }
 
    private void AdapterReconnected()
    {
		LogNotifications("Adapter re-connected.");

		AdapterConnected();
 
        Toast.makeText(this, "Adapter reconnected.", Toast.LENGTH_LONG).show();
    }

    private void AdapterNotReconnected()
    {
		LogNotifications("Adapter not re-connected.");
		
        AdapterNotConnected();
        
        Toast.makeText(this, "The Adapter did not reconnect.", Toast.LENGTH_LONG).show();
    }

	// Next Group click
    public void onNextGroupClick(View view)
    {
    	groupNo++;
    	if (groupNo > maxGroupNo)
    		groupNo = 0;
    	
    	ShowTruckText();    	
    }

	public void onBT21Click(View view)
	{
		// Set to ignore J1939 (opposite of checkJ1939)
		appUseBT21 = checkBT21.isChecked();

		if (appUseBT21)
		{
			appUseBLE = false;
			checkBLE.setChecked(false);
		}
	}

	public void onBLEClick(View view)
	{
		// Set to ignore J1939 (opposite of checkJ1939)
		appUseBLE = checkBLE.isChecked();

		if (appUseBLE)
		{
			appUseBT21 = false;
			checkBT21.setChecked(false);
		}
	}

	// Fault Text click
	public void onFaultClick(View view) 
	{	
		ShowFault();
	}
   
	// Reset button
	public void onResetClick(View view) 
	{	
		blueFire.ResetFaults();
	}
	
	// Update button
	public void onUpdateClick(View view) 
	{
		// Edit LED Brightness
		int ledBrightnessText = -1;
		try
		{
			ledBrightnessText = Integer.parseInt(textLedBrightness.getText().toString().trim());
		}
		catch(Exception e){}
		
		if (ledBrightnessText < 1 || ledBrightnessText > 100)
		{
            Toast.makeText(this, "Led Brightness must be between 1 and 100", Toast.LENGTH_LONG).show();
            return;
		}
		
		// Edit User Name
		String userNameText = textUserName.getText().toString().trim();
		if (userNameText.length() > 20)
		{
            Toast.makeText(this, "User Name must be less than 21 characters.", Toast.LENGTH_LONG).show();
            return;
		}
		
		// Edit Password
		String passwordText = textPassword.getText().toString().trim();
		if (passwordText.length() > 12)
		{
            Toast.makeText(this, "Password must be less than 13 characters.", Toast.LENGTH_LONG).show();
            return;
		}
		
		// Check for a change of led brightness
		if (ledBrightnessText != appLedBrightness)
		{
			appLedBrightness = ledBrightnessText;
			
			blueFire.SetLedBrightness(appLedBrightness);
			
	        Toast.makeText(this, "LED Brightness updated.", Toast.LENGTH_SHORT).show();
		}
		
		// Check for a change of user name or password
		if (!appUserName.equals(userNameText) || !appPassword.equals(passwordText))
		{
			appUserName = userNameText;
			appPassword = passwordText;
			
			blueFire.UpdateSecurity(appUserName, appPassword);
			
			Toast.makeText(this, "User Name and Password have been updated.", Toast.LENGTH_SHORT).show();
		}
		
		buttonConnect.requestFocus();
	}
	
	// Send/Monitor button
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
				pgnBytes = Hex.decodeHex(pgnData.toCharArray());
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

	public void onJ1939Click(View view)
	{
		// Set to ignore J1939 (opposite of checkJ1939)
		appIgnoreJ1939 = !checkJ1939.isChecked();
		
		// Update BlueFire
		blueFire.SetIgnoreJ1939(appIgnoreJ1939);
	}

	public void onJ1708Click(View view)
	{
		// Set to ignore J708 (opposite of checkJ1708)
		appIgnoreJ1708 = !checkJ1708.isChecked();
		
		// Update BlueFire
		blueFire.SetIgnoreJ1708(appIgnoreJ1708);
	}
	
    // Data Changed Handler from the BlueFire BlueFire
	private final Handler adapterHandler = new Handler() 
	{
		@Override
		@SuppressLint("HandlerLeak")
		public void handleMessage(Message msg) 
		{
			try 
			{
				ShowStatus();
				switch (blueFire.ConnectionState)
				{
					case NotConnected:
						if (isConnecting || isConnected)
							AdapterNotConnected();
						break;
						
					case Connecting:
						if (blueFire.IsReconnecting)
							if (!isConnecting)
								AdapterReconnecting();
						break;
						
					case Discovering:
						// Status only
						break;
						
					case Connected:
						// Status only
						break;
						
					case AdapterConnected:
						if (!isConnected)
		                	AdapterConnected();
						break;
						
					case Disconnecting:
						// Status only
						break;
					
					case Disconnected:
						if (isConnecting || isConnected)
							AdapterDisconnected();
						break;
						
					case Reconnecting:
						if (!isConnecting)
							AdapterReconnecting();
						break;
						
					case Reconnected:
						if (isConnecting)
							AdapterReconnected();
						break;
						
					case NotReconnected:
						if (isConnecting)
							AdapterNotReconnected();
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
							AdapterNotConnected();
							ShowMessage("Adapter Connection", "The Adapter Timed Out.");
						}
						break;
						
					case SystemError:
						if (isConnecting || isConnected)
						{
							blueFire.Disconnect();
							AdapterNotConnected();
							ShowSystemError();
						}
						break;
						
					case DataChanged:
						if (isConnected)
							ShowData();
				}
				
				// Check reset button enable
				if (!isConnected)
		          	buttonReset.setEnabled(false); // because it's enabled in ShowData
			} 
			catch (Exception e) {} 
		}
	};
	
    // Start retrieving data after connecting to the adapter
    private void getData()
    {
		// Check for API setting the adapter type
		appUseBT21 = blueFire.UseBT21;
		appUseBLE = blueFire.UseBLE;
		checkBT21.setChecked(appUseBT21);
		checkBLE.setChecked(appUseBLE);

		// Note, version has already been retrieved.
		// Check for an incompatible version.
    	if (blueFire.IsVersionIncompatible)
    	{
            Toast.makeText(this, "The Adapter is not compatible with this API.", Toast.LENGTH_LONG).show();
            DisconnectAdapter();
            return;
    	}

    	// Check authentication
        if (!blueFire.IsAuthenticated)
        {
            Toast.makeText(this, "Your User Name and Password do not match the Adapter's User Name and Password.", Toast.LENGTH_LONG).show();
            DisconnectAdapter();
            return;
        }
        
       	blueFire.GetSleepMode(); // BlueFire Sleep Mode
      	blueFire.GetLedBrightness(); // BlueFire LED Brightness
       	blueFire.GetMessages(); // Any BlueFire Error Messages
     	
      	blueFire.GetVehicleData(); // VIN, Make, Model, Serial no
      	
     	blueFire.GetEngineData1(); // RPM, Percent Torque, Driver Torque, Torque Mode
     	blueFire.GetEngineData2(); // Percent Load, Accelerator Pedal Position
     	blueFire.GetEngineData3(); // Vehicle Speed, Max Set Speed, Brake Switch, Clutch Switch, Park Brake Switch, Cruise Control Settings and Switches
     	
      	blueFire.GetTemps(); // Oil Temp, Coolant Temp, Intake Manifold Temperature
      	blueFire.GetOdometer(); // Odometer (Engine Distance)
      	blueFire.GetFuelData(); // Fuel Used, Idle Fuel Used, Fuel Rate, Instant Fuel Economy, Avg Fuel Economy, Throttle Position
      	blueFire.GetBrakeData(); // Application Pressure, Primary Pressure, Secondary Pressure
      	blueFire.GetPressures(); // Oil Pressure, Coolant Pressure, Intake Manifold(Boost) Pressure
      	blueFire.GetEngineHours(); // Total Engine Hours, Total Idle Hours
      	blueFire.GetCoolantLevel(); // Coolant Level
      	blueFire.GetBatteryVoltage(); // Battery Voltage

		blueFire.GetTransmissionGears(); // Selected and Current Gears
      	
      	blueFire.GetFaults(); // Any Engine Faults
    }

	private void ShowStatus()
	{
		// Check for a change of the connection state
		if (ConnectionState != blueFire.ConnectionState)
		{
			ConnectionState = blueFire.ConnectionState;
			textStatus.setText(ConnectionState.toString());
		}
		
        // Show any error message from the adapter
    	if (blueFire.NotificationMessage != "")
    	{
    		LogNotifications(blueFire.NotificationMessage);
    		blueFire.NotificationMessage = "";
    	}
	}
	
    private void LogNotifications(String Notification)
    {
 		Log.d("BlueFire", Notification);
    }

    private void CheckKeyState()
    {
		if (isCANAvailable != blueFire.IsCANAvailable)
		{
			isCANAvailable = blueFire.IsCANAvailable;
			if (isCANAvailable)
				textKeyState.setText("Key On");
			else
				textKeyState.setText("Key Off");
		}

    }
    
	private void ShowData()
	{ 
		// Check the key state
		CheckKeyState();

        // Show truck data
        if (blueFire.IsTruckDataChanged)
        {
        	ShowTruckData();
        }
       
        if (blueFire.IsFaultDataChanged)
        {
	        if (Truck.GetFaultCount() == 0)
	        {
				faultIndex = -1; // reset to show fault
	        	textFaultCode.setText("");
	          	buttonReset.setEnabled(false);
	        }
	        else
	        {
	        	if (faultIndex < 0) // show first fault only once.
	        	{
	        		faultIndex = 0;
		        	ShowFault();
		          	buttonReset.setEnabled(true);
	        	}
	        }
        }
		
		// Check for user changed adapter data while offline
		if (appLedBrightness != blueFire.LedBrightness)
			blueFire.SetLedBrightness(appLedBrightness);
        
		if (appPerformanceMode != blueFire.PerformanceMode)
			blueFire.SetPerformanceMode(appPerformanceMode);
        
		// Check if adapter overrode user input
		if (!appIgnoreJ1939 != blueFire.GetIgnoreJ1939()) 
		{
			appIgnoreJ1939 = blueFire.GetIgnoreJ1939();
			checkJ1939.setChecked(!appIgnoreJ1939); // checkJ1939 is the opposite of ignoreJ1939
		}
		if (!appIgnoreJ1708 != blueFire.GetIgnoreJ1708()) 
		{
			appIgnoreJ1708 = blueFire.GetIgnoreJ1708();
			checkJ1708.setChecked(!appIgnoreJ1708); // checkJ1708 is the opposite of ignoreJ1708
		}

		// Check for SendPGN response
		if ((isSendingPGN || isMonitoringPGN) && blueFire.PGNData.PGN == pgn) 
		{
			isSendingPGN = false; // only show sending data once
			textPGNData.setText(new String(Hex.encodeHex(blueFire.PGNData.Data)).toUpperCase());
		}
 
		// Show heartbeat
		textHeartbeat.setText(String.valueOf(blueFire.HeartbeatCount));
	}
	
	private void ShowTruckText()
	{
        switch (groupNo)
        {
			case 0:
				textView1.setText("RPM");
				textView2.setText("Speed");
				textView3.setText("Max Speed");
				textView4.setText("HiRes Max");
				textView5.setText("Accel Pedal");
				textView6.setText("Throttle Pos");
				textView7.setText("VIN");
				break;

			case 1:
				textView1.setText("Distance");
				textView2.setText("Odometer");
				textView3.setText("Total Hours");
				textView4.setText("Idle Hours");
				textView5.setText("Brake Pres");
				textView6.setText("Brake Air");
				textView7.setText("Make");
				break;

			case 2:
				textView1.setText("Fuel Rate");
				textView2.setText("Fuel Used");
				textView3.setText("HiRes Fuel");
				textView4.setText("Idle Fuel Used");
				textView5.setText("Avg Fuel Econ");
				textView6.setText("Inst Fuel Econ");
				textView7.setText("Model");
				break;

			case 3:
				textView1.setText("Pct Load");
				textView2.setText("Pct Torque");
				textView3.setText("Driver Torque");
				textView4.setText("Torque Mode");
				textView5.setText("Intake Temp");
				textView6.setText("Intake Pres");
				textView7.setText("Serial No");
				break;

			case 4:
				textView1.setText("Oil Temp");
				textView2.setText("Oil Pressure");
				textView3.setText("Coolant Temp");
				textView4.setText("Coolant Level");
				textView5.setText("Coolant Pres");
				textView6.setText("Battery Volts");
				textView7.setText("Unit No");
				break;

			case 5:
				textView1.setText("Brake Switch");
				textView2.setText("Clutch Switch");
				textView3.setText("Park Switch");
				textView4.setText("Cruise Switch");
				textView5.setText("Cruise Speed");
				textView6.setText("Cruise State");
				textView7.setText("");
				break;

			case 6:
				textView1.setText("Current Gear");
				textView2.setText("Selected Gear");
				textView3.setText("");
				textView4.setText("");
				textView5.setText("");
				textView6.setText("");
				textView7.setText("");
				break;

        }
		ShowTruckData();
	}

	private void ShowTruckData()
	{
        switch (groupNo)
        {
			case 0:
				dataView1.setText(formatInt(Truck.RPM));
				dataView2.setText(formatFloat(Truck.Speed * Const.KphToMph,0));
				dataView3.setText(formatFloat(Truck.MaxSpeed * Const.KphToMph,0));
				dataView4.setText(formatFloat(Truck.HiResMaxSpeed * Const.KphToMph,0));
				dataView5.setText(formatFloat(Truck.AccelPedal,2));
				dataView6.setText(formatFloat(Truck.ThrottlePos,2));
				dataView7.setText(Truck.VIN);
				break;

			case 1:
				dataView1.setText(formatFloat(Truck.Distance * Const.KmToMiles,0));
				dataView2.setText(formatFloat(Truck.Odometer * Const.MetersToMiles,0)); // HiRes Distance
				dataView3.setText(formatFloat(Truck.TotalHours,2));
				dataView4.setText(formatFloat(Truck.IdleHours,2));
				dataView5.setText(formatFloat(Truck.BrakeAppPressure * Const.kPaToPSI,2));
				dataView6.setText(formatFloat(Truck.Brake1AirPressure * Const.kPaToPSI,2));
				dataView7.setText(Truck.Make);
				break;

			case 2:
				dataView1.setText(formatFloat(Truck.FuelRate * Const.LphToGalPHr,2));
				dataView2.setText(formatFloat(Truck.FuelUsed * Const.LitersToGal,2));
				dataView3.setText(formatFloat(Truck.HiResFuelUsed * Const.LitersToGal,2));
				dataView4.setText(formatFloat(Truck.IdleFuelUsed * Const.LitersToGal,2));
				dataView5.setText(formatFloat(Truck.AvgFuelEcon * Const.KplToMpg,2));
				dataView6.setText(formatFloat(Truck.InstFuelEcon * Const.KplToMpg,2));
				dataView7.setText(Truck.Model);
				break;

			case 3:
				dataView1.setText(formatInt(Truck.PctLoad));
				dataView2.setText(formatInt(Truck.PctTorque));
				dataView3.setText(formatInt(Truck.DrvPctTorque));
				dataView4.setText(String.valueOf(Truck.TorqueMode));
				dataView5.setText(formatFloat(Helper.CelciusToFarenheit(Truck.IntakeTemp),2));
				dataView6.setText(formatFloat(Truck.IntakePressure * Const.kPaToPSI,2));
				dataView7.setText(Truck.SerialNo);
				break;

			case 4:
				dataView1.setText(formatFloat(Helper.CelciusToFarenheit(Truck.OilTemp),2));
				dataView2.setText(formatFloat(Truck.OilPressure * Const.kPaToPSI,2));
				dataView3.setText(formatFloat(Helper.CelciusToFarenheit(Truck.CoolantTemp),2));
				dataView4.setText(formatFloat(Truck.CoolantLevel,2));
				dataView5.setText(formatFloat(Truck.CoolantPressure * Const.kPaToPSI,2));
				dataView6.setText(formatFloat(Truck.BatteryPotential,2));
				dataView7.setText(Truck.UnitNo);
				break;

			case 5:
				dataView1.setText(String.valueOf(Truck.BrakeSwitch));
				dataView2.setText(String.valueOf(Truck.ClutchSwitch));
				dataView3.setText(String.valueOf(Truck.ParkBrakeSwitch));
				dataView4.setText(String.valueOf(Truck.CruiseOnOff));
				dataView5.setText(formatFloat(Truck.CruiseSetSpeed * Const.KphToMph,0));
				dataView6.setText(String.valueOf(Truck.CruiseState));
				dataView7.setText("");
				break;

			case 6:
				dataView1.setText(formatInt(Truck.CurrentGear));
				dataView2.setText(formatInt(Truck.SelectedGear));
				dataView3.setText("");
				dataView4.setText("");
				dataView5.setText("");
				dataView6.setText("");
				dataView7.setText("");
				break;
		}
	}

	private void ShowFault()
	{
		// Show the fault at the specified index. Note, faultIndex is relative to 0.
    	String FaultCode = String.valueOf(Truck.GetFaultSPN(faultIndex)) + " - " + String.valueOf(Truck.GetFaultFMI(faultIndex));
    	textFaultCode.setText(FaultCode);
    	
    	// Set to show next fault
    	faultIndex += 1;
    	if (faultIndex == Truck.GetFaultCount()) // wrap to the beginning
    		faultIndex = 0;
	}

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
		
		String formatString = "#";
		if(precision > 0)
			formatString += "." + StringUtils.repeat("#", precision);
		
        return String.valueOf(NumberFormat.getNumberInstance(Locale.US).format(Double.valueOf(new DecimalFormat(formatString).format(data))));
	}

    private void ShowSystemError()
    {
		LogNotifications("System Error.");
		
		ShowMessage("System Error", "See System Log");
    }
    
	private void ShowMessage(String title, String message)
	{
		AlertDialog.Builder alert = new AlertDialog.Builder(this);
		alert.setTitle(title);
		alert.setMessage(message);
		alert.show();
	}

	@Override
	public void onBackPressed()
    {
		super.onBackPressed();
		try 
		{
			blueFire.Disconnect();
		}
		catch (Exception e) {} 
    }

	@Override
	protected void onDestroy()
    {
		super.onDestroy();
		
		blueFire.Dispose();
    }

}

