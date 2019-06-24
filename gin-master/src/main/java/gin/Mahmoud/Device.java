package gin.Mahmoud;


import gin.StructuralTunningExperiment;
import gin.LocalSearch;
import gin.Patch;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.stat.inference.MannWhitneyUTest;

import java.io.File;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

/**
 * Created by Mahmoud-Uni on 1/21/2019.
 */
public class Device {
    public static final String isDoSum = "isDoSum";
    public static final String isDoAverage = "isDoAverage";
    public static final String isDoMedian = "isDoMedian";
    public static final String isRestAllowed = "isRestAllowed";
    public static final String RestDuration = "RestDuration";
    public static final String OPTIMISATION_EXPERIMENT = "OPTIMISATION_EXPERIMENT";
    public static final String VALIDATION_EXPERIMENT = "VALIDATION_EXPERIMENT";
    public static final String CPU_SCREEN_EXPERIMENT = "CPU_SCREEN_EXPERIMENT";
    public static final String MAIN_MEMORY_EXPERIMENT = "MAIN_MEMORY_EXPERIMENT";

    public static final String EXPERIMENT_DURATION = "EXPERIMENT_DURATION";
    public static final String MEASUREMENT_INTERVAL = "MEASUREMENT_INTERVAL";
    public static final String EXTERNAL_METER_EXPERIMENT = "EXTERNAL_METER_EXPERIMENT";
    public static final String REPETITIONS = "REPETITIONS";
    public static final String RUNS = "RUNS";
    public static final String TIMEOUT = "TIMEOUT";
    public static final String DETAILED_LOG = "DETAILED_LOG";


    public String deviceId="";
    public String deviceName="";
    public int portId=0;
    public String OS="PC";
    public int directoryNumber = 0; // this to be used for naming the log file pulled from the phone. It gets incremented by 1 after every successful pull.
    public int currentBatteryLevel = 100;

    public static boolean isRunInVivo = false;
    
    public int INITIAL_SAMPLE_SIZE=0;
    int RESAMPLING_TYPE = 0;
    
    String msg = "";

    public static Utils utils = new Utils();
    String FILE_NAME_SUFFIX = "";

    ArrayList<String> goodPatchs = new ArrayList<>();
    ArrayList<String> patchStats = new ArrayList<>();
    public static boolean isPrintSteps = true;
    public static boolean isDebug = false;

    public static int[] N6_N_ave={7,8,9,8,5};
    public static int[] N6_M_ave={15,16,11,12,8};

    public Patch currentBestPatch;

    //public static int[] N6_N_med={3,4,5,5,3};
    //public static int[] N6_M_med={10,7,8,6,6};


    public Device()
    {

    }
    public Device(String deviceName, int portId, boolean invivo)
    {

        this.deviceName = deviceName;

        this.portId = portId;
        isRunInVivo = invivo;

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
        if(utils.isNumeric(battery) && Integer.parseInt(battery) >  -1) return batteryLevel;

        battery = commandLineClass.runCommandAsSuper("cat sys/class/power_supply/max170xx_battery/capacity");

        msg = "battery level: "+battery;
        utils.log(msg,isPrintSteps);
        if(utils.isNumeric(battery) && Integer.parseInt(battery) >  -1) return batteryLevel;

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

    public void prepare()
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
            utils.log("Exception occurred while deploying the app", this.isPrintSteps);
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
            utils.log("Exception occurred while deploying the config file", this.isPrintSteps);
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
        float testLimit = 16f;

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

            String command = "am broadcast -a "+intentAction+" --es event "+OPTIMISATION_EXPERIMENT+
                    " --ef "+MEASUREMENT_INTERVAL+" 0.25 --ei "+RUNS+" "+runs+" --ei "+REPETITIONS+" "+reps+
                    " --ef "+TIMEOUT+" "+testLimit+" --ez "+isDoSum+" true --ez " + isRestAllowed + " false --ez "+
                    VALIDATION_EXPERIMENT+" true --ez "+ DETAILED_LOG+" "+logging+ " --ei SERVER_SOCKET_PORT "+port+
                    " --ez disableRecharging true";
            utils.log(command, isPrintSteps);
            utils.log(commandLineClass.runCommand(command),isDebug);
            //if(true) System.exit(-1);
            serverSocket.setSoTimeout((int)testingTime+10000);
            if(!keepConnection) // Mahmoud: this to be used when we're optimising using a model and don't need to disconnect the devices.
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
            utils.log("Exception occurred ", isPrintSteps);
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
                pull(location,destination.concat("\\").concat((++directoryNumber)+""));
                utils.log("pulling is successful ", isDebug);
                return true;
            }
            utils.log("pulling is not successful ", isDebug);

            return false;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            utils.log("pulling is not successful ", isDebug);
            utils.log(e.getMessage(),isDebug);
            return false;
        }
    }

    public void pull(String location, String destination)
    {

        String command = " pull "+location+" "+destination;
        CommandLineClass commandLineClass = new CommandLineClass(deviceId, portId);
        commandLineClass.runAdbCommand(command);
    }

    public void remove(String location)
    {
        utils.log("removing "+location, isDebug);
        String command = " rm -r "+location;
        CommandLineClass commandLineClass = new CommandLineClass(deviceId, portId);
        commandLineClass.runCommand(command);
        utils.log("removing finished", isDebug);
    }

    public void push(String location, String destination)
    {
        String command = " push "+location+" "+destination;
        CommandLineClass commandLineClass = new CommandLineClass(deviceId, portId);
        commandLineClass.runAdbCommand(command);
    }

    public void enableRecharging(boolean enable)
    {
        CommandLineClass commandLineClass = new CommandLineClass(deviceId, portId);
        commandLineClass.setRecharging(enable);
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
        int[] temp;
        if(OS.contains("M"))
            temp = N6_M_ave;
        else temp = N6_N_ave;
        if(currentBatteryLevel>80) return temp[0];
        else if(currentBatteryLevel>60) return temp[1];
        else if(currentBatteryLevel>40) return temp[2];
        else if(currentBatteryLevel>20) return temp[3];
        else return temp[4];
    }
}
