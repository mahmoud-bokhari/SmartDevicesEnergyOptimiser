package Mahmoud;



import java.io.File;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

/**
 * Created by Mahmoud-Uni on 1/21/2019.
 */
public class Device {



    public String deviceId="";
    public String deviceName="";
    public int portId=0;
    public float testLimit=0f;
    public String OS="PC";
    public int currentEvaluationNumber = 0; // this to be used for naming the log file pulled from the phone. It gets incremented by 1 after every successful pull.
    public int currentBatteryLevel = 100;
    public String NOISE_HANDLING = Experiment.NOISE_HANDLING_DUMMY_LOOP;

    public static boolean isRunInVivo = false;

    
    String msg = "";

    public static Utils utils = new Utils();
    String FILE_NAME_SUFFIX = "";

    ArrayList<String> goodPatchs = new ArrayList<>();
    ArrayList<String> patchStats = new ArrayList<>();
    public static boolean isPrintSteps = true;
    public static boolean isDebug = false;


    public static int[] N6_N_ave={7,8,9,8,5};
    public static int[] N6_M_ave={15,16,11,12,8};

    public static int[] N9_N_ave={7,8,9,8,5};
    public static int[] N9_M_ave={15,16,11,12,8};

    public static int[] moto_N_ave={61,53,51,48,47};
    public static int[] moto_M_ave={18,24,21,24,26};


    public static int[] moto_M_median={6, 9, 8 ,13,9};
    public static int[] moto_N_median={86,76,78, 94,69};



    //public static int[] N6_N_med={3,4,5,5,3};
    //public static int[] N6_M_med={10,7,8,6,6};


    public Device()
    {

    }
    public Device(String deviceName, int portId, boolean invivo, float testLimit, String noise)
    {

        this.deviceName = deviceName;

        this.portId = portId;
        this.testLimit = testLimit;
        isRunInVivo = invivo;
        NOISE_HANDLING = noise;
        deviceId = utils.runScript("e:/Program Files/Git/bin/bash.exe","getDeviceID.bsh", new String[]{deviceName});
        if(isRunInVivo) {
            currentBatteryLevel = getBatteryLevel(); // TODO: 4/1/2019   assuming the device is fully charged otherwise recharging takes in place.
            setOS();
        }

    }


    private void setOS()
    {
        CommandLineClass commandLineClass = new CommandLineClass(deviceId,portId);
        OS = commandLineClass.runCommand("getprop ro.build.version.release");
        if(OS.contains("7")) OS = "N";
        else if (OS.contains("6")) OS = "M";
        else if (OS.contains("8")) OS = "O";
        else
        {
            utils.log("OS/device not found exiting ...",isDebug);
            System.exit(10);
        }
    }

    public int getBatteryLevel()
    {
        // the following cases are a fix for when the device returns empty String for battery level!!!

        utils.log("device Name: "+ deviceName, isDebug);
        utils.log("device id: "+ deviceId, isDebug);

        //String result = utils.readFile(deviceName + File.separatorChar+"batteryLevel.txt");

        //if (result == null)
        //{
        CommandLineClass commandLineClass = new CommandLineClass(deviceId, portId);
        int batteryLevel = commandLineClass.getBatteryLevel();
        msg = "battery level: "+batteryLevel;
        utils.log(msg,isPrintSteps);
        if(batteryLevel > -1)
            return batteryLevel;
        String battery = commandLineClass.runCommandAsSuper("cat sys/class/power_supply/battery/capacity");
        msg = "battery level: "+battery;
        utils.log(msg,isPrintSteps);
        if(utils.isNumeric(battery) && Integer.parseInt(battery) >  -1)
            return Integer.parseInt(battery);

        battery = commandLineClass.runCommandAsSuper("cat sys/class/power_supply/max170xx_battery/capacity").trim();

        msg = "battery level: "+battery;
        utils.log(msg,isPrintSteps);
        if(utils.isNumeric(battery) && Integer.parseInt(battery) >  -1)
            return Integer.parseInt(battery);

        return  batteryLevel; // can't find another source to report the battery from and there's a problem with the battery level -1
        //}
        //if (result.isEmpty()){
        //    CommandLineClass commandLineClass = new CommandLineClass(deviceId);
        //    int batteryLevel = commandLineClass.getBatteryLevel();
        //    msg = "battery level: "+batteryLevel;
        //    utils.log(msg,isPrintSteps);
        //    if(batteryLevel >  -1)
        //        return batteryLevel;
        //    String battery = commandLineClass.runCommandAsSuper(new String[]{"cat sys/class/power_supply/battery/capacity"});
        //    msg = "battery level: "+battery;
        //    utils.log(msg,isPrintSteps);
        //    if(utils.isNumeric(battery) && Integer.parseInt(battery) >  -1) return batteryLevel;
//
        //    battery = commandLineClass.runCommandAsSuper(new String[]{"cat sys/class/power_supply/max170xx_battery/capacity"});
//
        //    msg = "battery level: "+battery;
        //    utils.log(msg,isPrintSteps);
        //    if(utils.isNumeric(battery) && Integer.parseInt(battery) >  -1) return batteryLevel;
//
        //    return -1;
        //}
        //result = result.substring(result.indexOf(":") + 1).trim();
        //utils.log("battery level: " + result, isPrintSteps);
        //if(utils.isNumeric(result))
        //    return Integer.parseInt(result);
        //return -1;
    }
    public void updateCurrentBatteryLevel()
    {
        currentBatteryLevel = getBatteryLevel();
        //currentBatteryLevel =- 20;
    }

    public void prepareForExperiment()
    {
        utils.log("preparing the device ...",isPrintSteps);
        PhoneController phoneController = new PhoneController(deviceId, portId);
        phoneController.turnAirPlaneModeOn();
        phoneController.turnScreenOff();
        phoneController.turnScreenOn();
        phoneController.setDozeMode(false);
        phoneController.changeGPSState(false);
        phoneController.forceStopBatteryMonitor();
        phoneController.startBatteryMonitor();
        phoneController.changeWifiState(false);
        utils.log("the device is prepared",isPrintSteps);
    }
    public void prepareForRun()
    {
        utils.log("preparing the device ...",isPrintSteps);
        PhoneController phoneController = new PhoneController(deviceId, portId);
        phoneController.turnAirPlaneModeOn();

        phoneController.setDozeMode(false);
        phoneController.changeGPSState(false);
        phoneController.changeWifiState(false);
        utils.log("the device is prepared",isPrintSteps);
    }


    /**
     * uninstall the app and then install it.
     *
     * */
    public void deploy(boolean isPrintSteps, String packageName, String apkLocation)  {

        // Configure the compiler
        //utils.log("Now compiling");
        boolean success = false;
        CommandLineClass commandLineClass = new CommandLineClass(deviceId, portId);
        utils.log("now trying to deploy the app", this.isPrintSteps);
        try {
            //utils.log(commandLineClass.runAdbCommand("uninstall com.example.mahmoud.modifiedrebound"),isPrintGradleOutput);
            utils.log(commandLineClass.runAdbCommand("uninstall "+packageName),isPrintSteps);
            //utils.log(commandLineClass.runAdbCommand("-d install gin-master\\reboundPC\\app-debug.apk"),isPrintGradleOutput);
            utils.log(commandLineClass.runAdbCommand("-d install "+apkLocation),isPrintSteps);
            utils.log(commandLineClass.runCommandAsSuper("pm grant "+packageName+" android.permission.WRITE_EXTERNAL_STORAGE"),isPrintSteps);
            utils.log(commandLineClass.runCommandAsSuper("pm grant "+packageName+" android.permission.READ_EXTERNAL_STORAGE"),isPrintSteps);
        }
        catch (Exception e)
        {
            utils.log("Exception occurred while deploying the app", true);
            e.printStackTrace();
            System.exit(2);
        }
        utils.log("deploying the app is finished", this.isPrintSteps);
        //utils.log("compiling finished");
    }

    public void deploy(boolean isPrintSteps,  String configFilePath)  {

        CommandLineClass commandLineClass = new CommandLineClass(deviceId, portId);
        utils.log("now trying to deploy the config file", this.isPrintSteps);
        try {
            utils.log(commandLineClass.runAdbCommand("push "+configFilePath+" sdcard/define/"),isPrintSteps);
        }
        catch (Exception e)
        {
            utils.log("Exception occurred while deploying the config file", true);
            e.printStackTrace();
            System.exit(2);
        }
        utils.log("deploying the config file is finished", this.isPrintSteps);
        //utils.log("compiling finished");
    }

    public void runInstrumentationTestInVivo(int runs)
    {
        String packageName = "com.example.mahmoud.modifiedrebound";
        String intentAction = "com.example.mahmoud.batterymonitor.externalMeterExperiment";
        String mainActivity = ".MainActivity";
        int reportCre = 1;
        int reps = 1;
        boolean logging = true;
        boolean keepConnection = false;


        ServerSocket serverSocket = null;
        Socket clientSocket = null;

        CommandLineClass commandLineClass = new CommandLineClass(deviceId, portId);
        int port = 0;

        try {
            long testingTime = (runs * reps * (long)testLimit + ((runs+reps==2)?0:60)) * 1000;
            serverSocket = new ServerSocket(0); //0 61954 61955  61954 62056
            serverSocket.setReuseAddress(true);

            port = serverSocket.getLocalPort();
            utils.log("listening on port: "+port,isPrintSteps);

            String command = "am broadcast -a "+intentAction+" --es event "+Experiment.OPTIMISATION_EXPERIMENT+
                    " --ef "+Experiment.MEASUREMENT_INTERVAL+" 0.25 --ei "+Experiment.RUNS+" "+runs+" --ei "+Experiment.REPETITIONS+" "+reps+
                    " --ef "+Experiment.TIMEOUT+" "+testLimit+" --ez "+Experiment.isDoSum+" true --ez " + Experiment.isRestAllowed + " false --ez "+
                    Experiment.VALIDATION_EXPERIMENT+" true --ez "+ Experiment.DETAILED_LOG+" "+logging+ " --ei SERVER_SOCKET_PORT "+port+
                    " --ez disableRecharging true";
            utils.log(command, isPrintSteps);
            utils.log(commandLineClass.runCommand(command),isDebug);
            //if(true) System.exit(-1);
            serverSocket.setSoTimeout((int)testingTime+10000);
            if(!keepConnection) // Mahmoud: this is used when we're optimising using a model and don't need to disconnect the devices.
                commandLineClass.usbPortClose();
            Thread.sleep(5 * 1000); // five more seconds for waiting

            utils.log("Start waiting for "+testingTime+" ms"+" from "+(new SimpleDateFormat("yyyy-MM-dd HH-mm-ss").format(System.currentTimeMillis())),isPrintSteps);

            //Main.log("Ending testing (waiting for 5 more seconds)");
            //Thread.sleep(5 * 1000); // five more seconds


            clientSocket = serverSocket.accept();//Thread.sleep(testingTime);
            utils.log(">======incoming on=======> " + serverSocket.getLocalPort(),isPrintSteps);

            clientSocket.close();
            serverSocket.close();

        }
        catch (Exception e)
        {
            utils.log("Exception occurred ", true);
            utils.log(e.getMessage(), isPrintSteps);

            utils.log(e.getMessage(),isPrintSteps);
            e.printStackTrace();
        }
        finally {

            try {
                if (serverSocket != null) {
                    if (!serverSocket.isClosed())
                        serverSocket.close();
                    clientSocket = null;

                }
                if (clientSocket != null) clientSocket = null;

                Thread.sleep(3000);

                utils.log("operation finished!", isPrintSteps);

                if (!keepConnection) // Mahmoud: this to be used when we're optimising using a model and don't need to disconnect the devices.
                    commandLineClass.usbPortOpen();

            } catch (Exception e) {
                utils.log(e.getMessage(),true);
                e.printStackTrace();
            }

        }

    }
    /**
     * This method is used when evaluating many solutions in one experiment. It will save each solution's logs in a dir from 1->n.
     * @param location the source where the files will be pulled from.
     * @param destination the folder in which the logs will be saved in. This must be a directory NOT a File
     *
     * */
    public boolean pullExperimentsLogs(String location, String destination)
    {
        try {
            utils.log("pulling experiment logs to "+destination, isDebug);
            File file = new File(destination);

            if(file.isDirectory())
            {
                pullFromDevice(location,destination.concat("\\").concat((++currentEvaluationNumber)+""));
                utils.log("pulling is successful ", isDebug);
                return true;
            }
            utils.log("pulling is not successful ", isDebug);
            return false;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            utils.log("pulling is not successful ", true);
            utils.log(e.getMessage(),isDebug);
            return false;
        }
    }

    public void rechargeTill(int batteryLevel)
    {
        try {
            if(isRunInVivo)
            {
                int counter = 0;
                int limit = 2;
                int bootCounter = 0;
                long sleepFor = 5 * 60 * 1000;

                updateCurrentBatteryLevel();
                int previousLevel = currentBatteryLevel;
                
                CommandLineClass commandLineClass = new CommandLineClass(deviceId,portId);
                while( currentBatteryLevel < batteryLevel)
                {
                    String simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss").format(System.currentTimeMillis());
                    utils.log("Charging ..."+
                            "time: "+simpleDateFormat, isPrintSteps);
                    Thread.sleep(sleepFor);

                    if(bootCounter > 2) // this means the device has been rebooted 2 times and the battery level hasn't changed.
                    {
                        utils.log("There's a problem in the device, it is not recharging and" +
                                " it has been rebooted, the experiment will proceed..."+
                                "time: "+simpleDateFormat, isPrintSteps);
                        break;
                    }
                    updateCurrentBatteryLevel();
                    //System.out.println(" BATTERY "+currentBatteryLevel);
                    if(previousLevel == currentBatteryLevel)
                    {
                        counter++;
                        utils.log("battery level didn't change!"+
                                "time: "+simpleDateFormat, isPrintSteps);
                    }
                    else
                        bootCounter = 0;

                    if(counter > limit)
                    {
                        utils.log("The device was plugged in for (mins): "+(sleepFor/60/1000), isPrintSteps);
                        utils.log("reboot and recharge again"+
                                "time: "+simpleDateFormat, isPrintSteps);
                        commandLineClass.runAdbCommand("reboot");
                        Thread.sleep(sleepFor/2); // this is an additional recharging period for rebooting.
                        bootCounter++;
                        counter = 0;
                    }
                    previousLevel = currentBatteryLevel;
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            utils.log("exception: "+e.getMessage(), true);
        }
    }

    public void pullFromDevice(String location, String destination)
    {

        try {
            File file = new File(destination);
            if(!file.exists())
                file.mkdirs();
            String command = " pull " + location + " " + destination;
            CommandLineClass commandLineClass = new CommandLineClass(deviceId, portId);
            commandLineClass.runAdbCommand(command);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            utils.log("could not pull file(s) "+ e.getMessage(), true);
        }
    }

    public void remove(String location)
    {
        utils.log("removing "+location, isDebug);
        String command = " rm -r "+location;
        CommandLineClass commandLineClass = new CommandLineClass(deviceId, portId);
        commandLineClass.runCommand(command);
        utils.log("removing finished", isDebug);
    }
    public void createFile(String location)
    {
        utils.log("creating "+location, isDebug);
        String command = "mkdir "+location;
        CommandLineClass commandLineClass = new CommandLineClass(deviceId, portId);
        commandLineClass.runCommand(command);
        utils.log("creating finished", isDebug);
    }

    public void pushToDevice(String location, String destination)
    {
        String command = " push "+location+" "+destination;
        CommandLineClass commandLineClass = new CommandLineClass(deviceId, portId);
        utils.log(command,isDebug);
        utils.log(commandLineClass.runAdbCommand(command),isDebug);
    }

    public void enableRecharging(boolean enable)
    {
        /*CommandLineClass commandLineClass = new CommandLineClass(deviceId, portId);
        commandLineClass.setRecharging(enable);*/
    }




    private String resample(String patch, int sampleSize)
    {
        Double []evaluation = new Double[1];//runBusyLoopExperiment(patch.duration /*+ baseDuration*/,sampleSize);
        Double sum = 0d;

        //this is the correct implementation of static:
        for(int i=0; i<evaluation.length;i++){
            //patch.addSample(evaluation[i]);
            sum+=evaluation[i];
        }

        //patch.fitness = sum/(double)sampleSize;

        return patch;
    }

    @Override
    public String toString()
    {
        return deviceName+" on port "+portId;
    }

    public int getMinimalSetSize()
    {
        if (NOISE_HANDLING.contains("dummyLoop")) return 1;
        int[] temp;
        if(deviceName.contains("nexus6")) {
            if (OS.contains("M"))
                temp = N6_M_ave;
            else temp = N6_N_ave;
        }
        else if(deviceName.contains("nexus9"))
        {
            if (OS.contains("M"))
                temp = N9_M_ave;
            else temp = N9_N_ave;
        }
        else
        {
            if (OS.contains("M"))
                temp = N9_M_ave;
            else temp = N9_N_ave;
        }

        if(currentBatteryLevel>80) return temp[0];
        else if(currentBatteryLevel>60) return temp[1];
        else if(currentBatteryLevel>40) return temp[2];
        else if(currentBatteryLevel>20) return temp[3];
        else return temp[4];
    }
}
