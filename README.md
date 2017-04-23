# BlueFire-API-for-Android
Android API for direct connection to the BlueFire J1939/J1708 Bluetooth Data Adapters. Documentation is available upon request from [BlueFire Support](mailto:support@bluefire-llc.com).

If you just want to download the .apk file for checking out the Demo app, you can find the .apk file at [this link.](https://github.com/BlueFire-LLC/BlueFire-API-for-Android-Eclipse/tree/master/bin)

Version 1:<ul>
	<li>Initial version.
</ul>
	
Version 2:<ul>
	<li>Code updates.
</ul>
	
Version 3:<ul>
	<li>Handles Adapter looping errors.
</ul>
	
Version 4:<ul>
	<li>Added Adapter Name and Password retrieval and update.
	<li>Connects to any adapter that starts with the Adapter Name.
	<li>Reboots the adapter on receiving adapter errors (firmware version 2.12).
	<li>Supports Adapter Firmware 2.13.
</ul>
	
Version 5:<ul>
	<li>Added Adapter Name update to Main page.
	<li>Added Truck Make, Model, Serial No, and Unit No to Next Pages.
	<li>Added App Settings class for BlueFire settings.
	<li>Added rebooting the adapter when disconnecting.
	<li>Added LastConnectedId and ConnectToLastAdapter Bluetooth settings.
	<li>Added Incompatible version check.</li>
	<ul>
		<li>Adapter Firmware 2.7 and less</li>
		<li>Adapter Firmware 3.0 and greater</li>
	</ul>
</ul>
	
Version 6:<ul>
	<li>Created an API document. Contact BlueFire Support for a copy.
	<li>Removed exposure to the Comm, J1939, and J1587 classes, and, moved all properties and methods to the Adapter class.
	<li>Added option to set the Interval on retrieving truck data (default is on change of data). This is useful when the data is coming in too fast (ie. RPM) and you want to slow it down.
	<li>Added SendPGN method and PGNData property for sending non-API defined PGNs.
	<li>Added sample code for SendPGN and MonitorPGN.
	<li>Added commons-codec-1.10.jar to the project libs folder. This is only required for the API Demo app.
	<li>Added a projects docs folder that contains the commons javadoc files. You must set the Javadoc Location project property to point to this folder.
</ul>
	
Version 7:<ul>
	<li>GetVehicleData is now threaded for performance.
	<li>Compatible with Adapter Firmware 3.x.
</ul>
	
Version 8:<ul>
	<li>Fixed retrieving J1939 Component Id (Make, Model, Serial No, Unit No).
	<li>Added retrieving J1708 VIN, Component Id and Faults.
	<li>Added additional exception handling.
</ul>
	
Version 9:<ul>
	<li>Added additional exception handling.
	<li>Added additional debug logging to the Demo app (Main).
</ul>
	
Version 10:<ul>
	<li>Added J1708 filtering.
	<li>Truck numerical data is initialized to -1.
	<li>Demo App (Main) shows NA if truck data is negative (no data).
</ul>
	
Version 11:<ul>
	<li>Renamed FWVersion property to FirmwareVersion.
	<li>Renamed HWVersion property to HardwareVersion.
	<li>J1587 filtering caters for a 0 value where appropriate.
</ul>
	
Version 12:<ul>
	<li>Supports Adapter Firmwares 3.4+.
	<li>Added User Name and Password authentication.
	<li>Added updating adapter data (led brightness) while offline to the Demo app (Main).
</ul>
	
Version 13:<ul>
	<li>Added Key State to API Demo (Key On/Off).
	<li>Renamed source folder and apk from "bluefire.apidemo" to "com.bluefire.apidemo".
</ul>
	
Version 14:<ul>
	<li>Minor improvements to the Bluetooth Discovery process.
	<li>Changed SetMaxConnectRetrys default to 10 retries.
	<li>Changed SetDiscoveryTimeOut default to 30 seconds.
</ul>
	
Version 15:<ul>
	<li>Supports Adapter Firmware 3.7.
	<li>GetVehicleData, GetFuelData, and GetEngineHours retrieves data more accurately (firmware 3.6 and lower work, but better performance with firmware 3.7). 
</ul>
	
Version 16:<ul>
	<li>When J1939 and J1708 data are retrieved, J1939 data will take precedence over J1708 data. 
</ul>
	
Version 17:<ul>
	<li>Supports Adapter Firmware 3.8+.
	<li>Added Get/Set PerformanceMode that will improve the retrieval of PGNs that have an interval of one second. 
	<li>Minor improvement to the retrieval of vehicle data (vin, make, model, serial no). 
</ul>

Version 18:<ul>
	<li>App heartbeat will be sent to the adapter every second. 
</ul>

Version 19:<ul>
	<li>Supports the BLE (Bluetooth Low Energy) adapters (see limitations below).
	<li>The API will automatically find and select the correct adapter (BLE or BT21 (Bluetooth Classic)).
	<li>Added properties UseBLE, UseBT21 that will improve connection speed if set.
	<li>Added properties IsUsingBLE, IsUsingBT21 that will be set according to the type of Bluetooth connected to. 
	<li>Added property MinInterval that defaults to 500 ms for BLE adapters. 
	<li>Supports the new 500K CAN adapter (green Deutsch connector). 
	<li>Removed the Settings class from the Demo app.
	<li>No longer compatible with Adapter firmware 2.x.
	<li>Requires Android 4+ for Bluetooth Classic adapter and 5+ for BLE adapter.
	<li>Supports the Android Studio IDE.
</ul>

Version 19.1:<ul>
	<li>Patch for discovering an adapter using Bluetooth Classic and Android 6.0+.
</ul>

Version 20.0:<ul>
	<li>Renamed the Adapter class to the BlueFire class to avoid confusion with the Android Adapter widget.
	<li>Added an Adapter BT2.1 and a BLE checkbox that will select the appropriate Adapter type. Leaving both unchecked will cause the API to auto select the Adapter type.
	<li>Added GetTransmissionGears method that will retrieve the current and selected gears from the transmission ECM if the data is available.
</ul>

Version 20.1:<ul>
	<li>Removed IsUsingBT21 and IsUsingBLE properties in lieu of using properties UseBT21 and UseBLE which if not set will be set automatically by the API.
	<li>The Demo App's Disconnect button will be shown immediately after connecting to allow for disconnecting while the API is attempting to discover an Adapter.
</ul>

Version 20.2:<ul>
	<li>The API will only raise Connection State 'Reconnected' when the Adapter is reconnected. Connection State 'AdapterConnected' will only be raised upon initial connection.
	<li>The Demo App has been modified to reflect the above 'Reconnected' Connection State.
	<li>Added API method ResetAdapter which will reset the Adapter to factory settings.
	<li>The Demo App will edit for invalid hex characters in Send PGN Data.
	<li>Fixed API fatal exception in SendPGN when sending data.
</ul>

Version 21.0:<ul>
	<li>Added optional Source and MID parameters to the GetVehicleInfo method.
    <li>Added optional Source and MID parameters to the GetFaults method.
    <li>Removed property IsFaultDataChanged.
    <li>Added Truck property IsJ1587Fault.
    <li>All methods take Source, PGN, and MID as integers.
    <li>Demo app shows Fault source.
</ul>

Version 22.0:<ul>
	<li>Added support for Adapter Firmware 3.10 and ELD Recording (see API 22.0 documentation).
    <li>Changed and renamed many properties and methods. Refer to the API documentation for all the changes.
    <li>Backward compatible with Adapter Firmware 3.7 to 3.9 (no ELD Recording).
    <li>No additional functionality except for features in Firmware 3.10 and ELD Recording.
    <li>Added Adapter Id security.
    <li>Fixed BLE issues however using the BLE Adapter requires Android 6+.
    <li>New Demo App that supports ELD Recording.
    <li>Demo App request location permissions if connecting to a BLE adapter.
    <li>API and Demo App are compiled with minimum Android version of 23.
    <li>Demo App no longer references commons-codec-1.10.
</ul>
	
Version 22.1:<ul>
	<li>Changed ConnectionStates, SleepModes, and ELD.RecordIds to be exposed outside of the BlueFire class.
	<li>Internal changes.
</ul>
	
Version 22.2:<ul>
	<li>Compatible with Adapter Firmware Beta 3.10.3 (required).
	<li>Removed duplicate ELD records.
	<li>Better re-connection while ELD recording.
</ul>
	
Version 22.3:<ul>
	<li>Compatible with Adapter Firmware Beta 3.10.5 (required).
	<li>Better time sync with the Adapter.
</ul>

	
