# BlueFire-API-for-Android
Android API for direct connection to the BlueFire J1939/J1708 Bluetooth Data Adapters. Documentation is available upon request from [BlueFire Support](mailto:support@bluefire-llc.com).

If you just want to download the .apk file for checking out the Demo app, you can find the .apk file at [this link.](https://github.com/BlueFire-LLC/BlueFire-API-for-Android-Studio/tree/master/APIDemo/build/outputs/apk)

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
	<li>Fixed crash in SendPGN.
	<li>The Demo App's will edit for invalid hex characters in Send PGN Data.
</ul>
	
Limitations:<ul>
	<li>VIN and Component Id will be retrieved only from the Engine ECU.
	<li>Only Engine Active faults are retrieved.
	<li>Only J1939 or J1708 faults can be retrieved (not J1939 and J1708 faults).
	<li>BLE adapters cannot reliable retrieve data faster than 500 ms (eg. 10, 50, 100 ms).
	<li>The API may become unstable after long periods of BLE adapter connection.
	<li>The API may lock up if the BLE adapter is power cycled too quickly (such as a power blip).
	<li>Note, these BLE limitations may be fixed by Google in later versions of Android.
</ul>
	
	
