# BlueFire-API-for-Android
Android API for direct connection to the BlueFire J1939/J1708 Bluetooth Data Adapters.

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
	<li>Requires Adapter Firmware Beta 3.10.3 for ELD functionality.
	<li>Removed duplicate ELD records.
	<li>Better re-connection while ELD recording.
</ul>
	
Version 22.3:<ul>
	<li>Compatible with Adapter Firmware Beta 3.10.5 for ELD functionality.
	<li>Better time sync with the Adapter.
</ul>
	
Version 22.4:<ul>
	<li>Requires Adapter Firmware Beta 3.10.5 for ELD functionality.
	<li>Added getEngineVIN method.
	<li>Added synchronization to Truck Data methods.
	<li>Added retrievalMethod parameter to Truck Data methods.
	<li>Demo app only retrieves truck data when navigating to the specific data page.
	<li>API documentation has been updated to reflect the above changes.
</ul>
	
Version 22.5:<ul>
	<li>Requires Adapter Firmware Beta 3.10.6 for ELD functionality.
	<li>Fixed J1708 data retrieval.
	<li>Added property AndroidVersion.
    <li>Added property SyncTimeout.
    <li>Added ConnectionState CANFilterFull.
    <li>Renamed property DiscoveryTimeOut to DiscoveryTimeout.
    <li>Renamed property MaxConnectRetrys to MaxConnectAttempts.
    <li>Added method ELD.SetStreaming.
    <li>Added property ELD.LocalRecordNo.
    <li>Added ELD Enum RecordingModes.
    <li>Added property ELD.RecordingMode and method SetRecordingMode.
    <li>Added property ELD.IsRecordingLocally.
    <li>Added property ELD.IsRecordingConnected.
    <li>Added property ELD.IsRecordingDisconnected.
    <li>Included the API documentation in the GitHub repository.
    <li>Added to the API documentation Appendix instructions for manually resetting the Adapter.
</ul>
	
Version 22.6:<ul>
	<li>Requires Adapter Firmware Beta 3.10.8 for ELD functionality.
	<li>Removed VIN from GetVehicleData method.
	<li>Added Boolean return value to Truck Data methods for synchronized calls.
	<li>Added synchronized VIN retrieval to Demo app.
	<li>ELD rules are sent to the Adapter from the API.
	<li>The Adapter will reboot after ELD StopEngine if the API is not connected.
</ul>
	
Version 22.7:<ul>
	<li>Requires Adapter Firmware 3.10 for ELD functionality.
	<li>Fixed issue with retrieving VIN and Vehicle Data in the Demo app.
</ul>	

Version 22.8:<ul>
	<li>Fixed compatability with Firmware 3.9 and below.
	<li>API will generate and update the Adapter Serial Number if it is missing. This can occur if the adapter firmware is flashed over the top of an older incompatible firmware.
</ul>

Version 22.9:<ul>
	<li>Requires Adapter Firmware 3.11. Note, Firmware 3.10 is broken and should be updated to 3.11.
    <li>Compatable with Firmware 3.9 and below.
	<li>API method GetEngineVIN automatically sets the sync timeout if RetrievalMethod Synchronized is specified. The default sync timeout is 2 seconds.
	<li>The call to VIN retrieval SetSyncTimeout in the Demo app has been commented out to allow the API method to set the default.
	<li>Faults have been moved to their own page in the Demo app in order to allow faults to be retrieved by themselves (recommended).
	<li>VIN and Truck data (Component Id) retrieval have been improved. It is recommended to retrieve VIN and Truck data before retrieving any other data.
</ul>

Version 22.10:<ul>
	<li>The API now disconnects properly from the Adapter while ELD is recording.
    <li>The Demo app disconnectAdapter method WaitForDisconnect parameter is set to false for Adapter Firmware 3.11.
</ul>

Version 22.11:<ul>
	<li>The SetDiscoveryTimeOut method is renamed to SetDiscoveryTimeout (TimeOut to Timeout).
	<li>Added SetAdvertisementTimeout method for use in very crowded BLE areas (like trade shows).
    <li>For BLE adapters, if the ConnectToLastAdapter and SetSecurity(SecureAdapter) are not set, the API will connect to the adapter with the strongest signal.
</ul>

Version 22.12:<ul>
	<li>Added method GetDistance which is the same as GetOdometer (GetOdometer actually calls GetDistance).
	<li>Added properties Truck.HiResDistance, LoResDistance, HiResOdometer, and LoResOdometer.
    <li>Truck.Odometer now returns the OEM distance (previously it returned Engine distance).
    <li>Truck.Odometer will return -1 if the OEM distance is not available (e.g. Volvo trucks).
    <li>Truck.Distance and Truck.Odometer returns the hi-resolution value unless it is not available in which case it returns the lo-resolution value.
    <li>Note that hi-resolution distance is at a 1 second ECM refresh rate while lo-resolution is at a 100 ms ECM refresh rate.
    <li>Modified the Demo app to reflect the above changes.
</ul>

Version 22.13:<ul>
	<li>Added a Service class that emulates using the API with an Android service.
	<li>Added a Start and Stop Service buttons to the Demo app.
</ul>

Version 23.0:<ul>
	<li>GetVehicleData is renamed to GetEngineId.
	<li>Added methods GetVIN and GetComponentId for retrieving non-engine ECM data.
	<li>Added synchronized calling to GetEngineVIN, GetVIN, GetEngineId, and GetComponentId.
	<li>The SetSyncTimeout method is replaced with the SyncTimeout passed as the Interval parameter along with the Synchronized Retrieval Method parameter.
	<li>Fixed the OnChange Retrieval Method that caused issues with the Adapter.
	<li>Added a Notification Connection State that will return any API notifications.
	<li>Added SetHeartbeatOn method that will turn the Adapter heartbeat on/off. Use with caution.
	<li>Added SetNotificationsOn method that will turn Adapter notifications on/off.
	<li>Change the Engine VIN/Id page in the Demo app to show using synchronous retrieval.
	<li>Added a VIN/ComponentId page to the Demo app that shows using asynchronous retrieval.
	<li>Added a Test All button to the Demo app that retrieves all the data at once to test loading the connection.
	<li>Improved connection reliability with beta Firmware 3.12.x.
</ul>

Version 23.1:<ul>
	<li>Added property OptimizeDataRetrieval that optimizes retrieval of data when the same data is available from both J1939 and J1708 ECMs. Recommended.
	<li>Better J1708 data retrieval with Firmware 3.12.
	<li>Not selecting a connection type (BLE or BT21) will auto connect properly.
	<li>Works on Android 7.1 devices that use Android 6 BLE firmware.
	<li>No longer supports Android 4+ and Android 5+.
	<li>
	<li>
	<li>
	<li>Improved connection and data reliability with Firmware 3.12.
</ul>
