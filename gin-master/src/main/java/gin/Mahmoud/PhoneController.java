package gin.Mahmoud;

/**
 * Created by Mahmoud-Uni on 13/01/2017.
 */

public class PhoneController {

    String deviceId = "";

    
    Utils utils = new Utils();
    public static boolean isPrintSteps= false;
    CommandLineClass commandLineClass;
    int portId = 0;

    public PhoneController(String deviceId, int portId)
    {
        this.deviceId = deviceId;
        this.portId = portId;

        commandLineClass = new CommandLineClass(deviceId, portId);
    }
    public void forceStop(String packageName)
    {
        commandLineClass.runCommandAsSuper("am force-stop "+packageName);
    }
    public void forceStopBatteryMonitor()
    {
        commandLineClass.runCommandAsSuper("am force-stop com.example.mahmoud.batterymonitor");
    }


    public void startBatteryMonitor()
    {
        commandLineClass.runCommandAsSuper("am start -n com.example.mahmoud.batterymonitor/com.example.mahmoud.batterymonitor.Activities.MainActivity");
    }

    public void startApp(String packageName, String mainActivityPath)
    {
        commandLineClass.runCommandAsSuper("am start -n "+packageName+"/"+packageName.concat(".".concat(mainActivityPath)));
    }



    public void turnScreenOff() {
        
        
        //commandLineClass.runCommandAsSuper(new String[]{"input keyevent 26"});
        utils.log(commandLineClass.runCommandAsSuper("input keyevent 26"),isPrintSteps);
    }

    public void turnScreenOn() {

        utils.log(commandLineClass.runCommandAsSuper("input keyevent 26"),isPrintSteps);
        commandLineClass.runCommandAsSuper("input keyevent 82");
        commandLineClass.runCommandAsSuper("input keyevent 82");
    }

    public  void changeWifiState(boolean state) {

       
    }

    public  void turnAirPlaneModeOn() {

        commandLineClass.runCommandAsSuper("settings put global airplane_mode_on 1");
        commandLineClass.runCommandAsSuper("am broadcast -a android.intent.action.AIRPLANE_MODE");
    }
    public  void turnAirPlaneModeOff() {
        
        commandLineClass.runCommandAsSuper("settings put global airplane_mode_on 0");
        commandLineClass.runCommandAsSuper("am broadcast -a android.intent.action.AIRPLANE_MODE");
    }

    /**
     * @deprecated this method not working as it needs some permissions that are not granted to 3rd party apps
     */
    public static void changeCellularInternetConnectionState(boolean state) {
        
    }

    public String enableMobileData(boolean enable)
    {

        
        String opt = "";
        if(enable)
            opt = "enable";
        else
            opt = "disable";
        String command = "svc data "+opt;
        
        
        return commandLineClass.runCommandAsSuper(command);
    }
    public boolean checkMobileDataState()
    {
        try
        {
            
        }
        catch (Exception ex)
        {
            
        }

        return false;
    }


    public  String getNetworkInfo()
    {
        return "";
    }
    public  boolean checkWifiConnected() {

        return false;
    }
    public  boolean checkMobileConnected() {

        return false;
    }
    public  boolean checkInternetConnection()
    {

        return false;
    }

    public  void changeBluetoothState(boolean state) {

    }
    public void setDozeMode(boolean state) {

        
        if(state)
            commandLineClass.runCommandAsSuper("shell dumpsys deviceidle disable");
        else
            commandLineClass.runCommandAsSuper("shell dumpsys deviceidle enable");
    }

    public  void changeGPSState(boolean state) {
        //Intent intent = new Intent("android.location.GPS_ENABLED_CHANGE");
        //intent.putExtra("enabled", state);
        
        if(state) {
            commandLineClass.runCommandAsSuper("settings put secure location_providers_allowed +network");
            commandLineClass.runCommandAsSuper("settings put secure location_providers_allowed +gps");
        }
        else {
            commandLineClass.runCommandAsSuper("settings put secure location_providers_allowed -gps");
            commandLineClass.runCommandAsSuper("settings put secure location_providers_allowed -network");
            commandLineClass.runCommandAsSuper("settings put secure location_providers_allowed -gps");
        }
    }

    public void changeNFCState(boolean state)
    {
        
        if(state)
            commandLineClass.runCommandAsSuper("service call nfc 6");
        else
            commandLineClass.runCommandAsSuper("service call nfc 5");
    }

}

