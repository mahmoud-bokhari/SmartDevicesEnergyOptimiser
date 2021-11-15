package DPO;

import Mahmoud.*;
import org.apache.commons.io.FileUtils;

import javax.rmi.CORBA.Util;
import java.io.File;
import java.net.Inet4Address;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Hashtable;

/**
 * Created by Mahmoud-Uni on 12/9/2019.
 */
public class OnDeviceExperiment {

    public static String MAIN_DIRECTORY = "DPO"+ File.separator;
    public static String immigrants_directory = MAIN_DIRECTORY+"in_progress"+File.separator+"immigrants"+ File.separator;
    public static String used_immigrants_directory = MAIN_DIRECTORY+"in_progress"+File.separator+"usedImmigrants"+ File.separator;
    public static String current_running_devices_log = MAIN_DIRECTORY+"in_progress"+File.separator+"current_running_devices.csv";
    public static String originalConfig = "originalConfig.csv";
    public static String resultsDirectory = "results";
    public static String replaces_selection = "replaces_selection.dat";
    public static CommandLineClass commandLineClass;
    public static PhoneController phoneController;


    public static String DEFINE = "sdcard/define/";
    String[] args;
    public static Device device;
    public static String []neighbours = new String[2];
    public static Utils utils = new Utils();
    private static boolean isPrintSteps = true;
    private static boolean isDebug = false;


    public OnDeviceExperiment()
    {

    }
    public OnDeviceExperiment(String[] args)
    {
        this.args = args;
        main(args);
    }
    public static void pushMainFiles(Device device)
    {
        try
        {
            utils.log("pushing main experimental files",isPrintSteps);
            device.pushToDevice(MAIN_DIRECTORY.concat(originalConfig),DEFINE);
            device.pushToDevice(MAIN_DIRECTORY.concat(replaces_selection),DEFINE);
            device.createFile("sdcard/define/immigrants");
            //device.push(MAIN_DIRECTORY.concat(originalConfig),DEFINE);

        }
        catch (Exception e)
        {
            e.printStackTrace();
            utils.log(e.getMessage(),true);
        }
    }

    public void run()
    {
        /*String deviceName = args[1];
        int portId = Integer.parseInt(args[2]);
        boolean isInvivo = Boolean.parseBoolean(args[3]);
        float testLimit = Float.parseFloat(args[4]);*/

        //String testCommand = "am instrument -w  -e class com.example.mahmoud.modifiedrebound.SpringTest com.example.mahmoud.modifiedrebound.test/android.support.test.runner.AndroidJUnitRunner";
        String testCommand = "am instrument -w  -e class com.example.mahmoud.modifiedrebound.devSubset com.example.mahmoud.modifiedrebound.test/android.support.test.runner.AndroidJUnitRunner";
        //String testCommand = "am instrument -w  -e class com.example.mahmoud.modifiedrebound.devSubsetNoAssertions com.example.mahmoud.modifiedrebound.test/android.support.test.runner.AndroidJUnitRunner";
        // PUSH files (replaces_selection.dat ...) to the phone "sdcard"+File.separator+"define"
    }

    public static void main(String[] args)
    {
        try {

            final boolean isOnlyPullLogs = true;
            String deviceName = "moto-g-3";//"nexus6-3";//"moto-g-3";
            int portId = 3;
            float testLimit = 100f;
            neighbours[0] = "moto-g-4";
            neighbours[1] = "nexus6-1";
            int startRunNumber = 0;
            boolean isImmigrationAllowed = false;
            final int numberOfRuns = 1;

            String host = Inet4Address.getLocalHost().getHostAddress();
            Device.isDebug = true;

            if (args != null) {
                if (args.length == 7) {
                    deviceName = args[0];
                    portId = Integer.parseInt(args[1]);
                    testLimit = Float.parseFloat(args[2]);
                    neighbours[0] = args[3];
                    neighbours[1] = args[4];
                    if(args[5] != null)
                        startRunNumber = Integer.parseInt(args[5]);
                    if(args[6] != null) isImmigrationAllowed = Boolean.parseBoolean(args[6]);
                }

            }


            boolean isInvivo = true;

            device = new Device(deviceName, portId, isInvivo, testLimit, Experiment.NOISE_HANDLING_DUMMY_LOOP);


            pushMainFiles(device);
            String intentAction = "com.example.mahmoud.batterymonitor.externalMeterExperiment";//"com.example.mahmoud_uni.sendCommandToPC.command"
            int batteryLowerLimit = 20;
            //int reps = 1;

            float MAE_CONSTRAINT = 1;
            boolean logging = true;
            boolean keepConnection = false;
            String testCommand = "\"am instrument -w -e class com.example.mahmoud.modifiedrebound.devSubset com.example.mahmoud.modifiedrebound.test/android.support.test.runner.AndroidJUnitRunner\"";


            ServerSocket serverSocket = null;
            Socket clientSocket = null;
            MyService myService = new MyService();

            commandLineClass = new CommandLineClass(device.deviceId, device.portId);
            int port = 0;

            PhoneController phoneController = new PhoneController(device.deviceId, device.portId);

            // setup folders on the PC
            resultsDirectory = MAIN_DIRECTORY.concat(resultsDirectory).concat(File.separator).concat(device.deviceName).concat(File.separator);
            String experimentDateTime = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss").format(System.currentTimeMillis());
            experimentDateTime = experimentDateTime.replace(" ", "_");
            //immigrants_directory = MAIN_DIRECTORY.concat("immigrants".concat(File.separator).concat(deviceName).concat(File.separator));
            resultsDirectory = resultsDirectory.concat(experimentDateTime);


            if(isOnlyPullLogs) {
                saveResults(resultsDirectory, true);
                if (true)
                    System.exit(0);
            }

//            if (device.getBatteryLevel() < 100)
//                device.rechargeTill(100);
            try {

                //(new File(immigrants_directory)).mkdirs();

                //long testingTime = (batteryLowerLimit * reps * (long)device.testLimit + ((batteryLowerLimit+reps==2)?0:60)) * 1000;

                utils.log("listening on port: " + port, isPrintSteps);
                boolean isNewRun = true;
                // delete previous files if found
                deleteOldLogs();
                int i = startRunNumber;
                while (true) {
                    if (i >= numberOfRuns) break;
//                serverSocket = new ServerSocket(0); //0 61954 61955  61954 62056
//                serverSocket.setReuseAddress(true);
//                port = serverSocket.getLocalPort();
                    ServerSocket listener = new ServerSocket(0);
                    listener.setReuseAddress(true);
                    port = listener.getLocalPort();


                /*if(i == 0) // ????? do the initialisations only at the first run??? or with every run?
                // close app*/
                    phoneController.forceStopBatteryMonitor();
                    phoneController.startBatteryMonitor();



                    // prepare command
                    String command = "am broadcast -a " + intentAction +
                            " --es event " + Experiment.OPTIMISATION_ON_PHONE_EXPERIMENT +
                            " --ez " + Experiment.OPTIMISATION_ON_PHONE_EXPERIMENT + " true" +

                            " --ei " + Experiment.MEASUREMENT_INTERVAL + " " + 250 +
                            " --ei " + Experiment.LOWER_BATTERY_LIMIT + " " + batteryLowerLimit +
                            " --ef " + Experiment.TIMEOUT + " " + device.testLimit +
                            //" --es " + Experiment.TEST_COMMAND + " " + testCommand + // problem: I can't send the command
                            " --ez " + Experiment.DETAILED_LOG + " " + logging +
                            " --es " + Experiment.SERVER_HOST_IP + " " + host +
                            " --ei " + Experiment.SERVER_SOCKET_PORT + " " + port +
                            " --ez " + Experiment.NEW_OPTIMISATION_EXP + " " + isNewRun +
                            " --es " + Experiment.MEASUREMENT_SOURCE + " " + Experiment.MEASUREMENT_SOURCE_BATTERY +
                            " --ef " + Experiment.MAE_CONSTRAINT + " " + MAE_CONSTRAINT+
                            " --ez " + Experiment.IMMIGRATION_ALLOWED + " " + isImmigrationAllowed;
                /*command = "am broadcast -a "+ intentAction+
                        " --es event "+Experiment.OPTIMISATION_ON_PHONE_EXPERIMENT+
                        " --ez " + Experiment.OPTIMISATION_ON_PHONE_EXPERIMENT + " true"+
                        " --es " + Experiment.TEST_COMMAND + " " + testCommand+
                        " --ef " + Experiment.MEASUREMENT_INTERVAL + " " +0.25+
                        " --ei " + Experiment.LOWER_BATTERY_LIMIT + " " + batteryLowerLimit+
                        " --ef " + Experiment.TIMEOUT + " " + device.testLimit +
                        " --ez " + Experiment.DETAILED_LOG + " " + logging +
                        " --es "+Experiment.SERVER_HOST_IP+ " " + host +
                        " --ei "+Experiment.SERVER_SOCKET_PORT+" "+port+
                        " --ez " + Experiment.NEW_OPTIMISATION_EXP + " " + isNewRun+
                        " --es " + Experiment.MEASUREMENT_SOURCE + " " + Experiment.MEASUREMENT_SOURCE_BATTERY +
                        " --ef " + Experiment.MAE_CONSTRAINT + " " + MAE_CONSTRAINT;*/
                    utils.log(command, isPrintSteps);
                    utils.log(commandLineClass.runCommand(command), isDebug);
                    //if(true) System.exit(-1);

                    if (!keepConnection) // Mahmoud: this is used when we're optimising using a model and don't need to disconnect the devices.
                        commandLineClass.usbPortClose();

                    //serverSocket.setSoTimeout((int)testingTime+10000);
                    utils.log("Start waiting for from " + (new SimpleDateFormat("yyyy-MM-dd HH-mm-ss").format(System.currentTimeMillis())), isPrintSteps);


                    myService.startService(listener);


                    // pulling results
                    File resultFolder = new File(resultsDirectory);
                    resultFolder.mkdirs();// this to make sure that MAIN_DIR/deviceName/experimentDateTime exists

                    String tempDestination = resultsDirectory.concat(File.separator + i);
                    saveResults(tempDestination, true);

                    utils.saveLog(resultsDirectory.concat("\\").concat("detailedLog-").concat(experimentDateTime).concat(".txt"),true);
                    utils.resetLogger();

                    device.rechargeTill(100);
                    if (device.getBatteryLevel() == 100)
                        Thread.sleep(60000);

                    i++;
                    //if(isIslandsExperiment)
                    if(isImmigrationAllowed) waitForOtherDevices();
//                    if (i%2 == 0) isNewRun = true;
//                    else isNewRun = false;
                }

            } catch (Exception e) {
                utils.log("Exception occurred ", isPrintSteps);
                utils.log(e.getMessage(), isPrintSteps);

                utils.log(e.getMessage(), isPrintSteps);
                e.printStackTrace();
            } finally {

                try {
                /*if (serverSocket != null) {
                    if (!serverSocket.isClosed())
                        serverSocket.close();
                    clientSocket = null;

                }
                if (clientSocket != null) clientSocket = null;
*/
                    myService.stopService();
                    Thread.sleep(3000);

                    utils.log("operation finished!", isPrintSteps);

                    commandLineClass.usbPortOpen();

                } catch (Exception e) {
                    utils.log(e.getMessage(),true);
                    e.printStackTrace();
                }

            }
        }
        catch (Exception e) {
            utils.log(e.getMessage(),true);
            e.printStackTrace();
        }

    }

    private static void waitForOtherDevices()
    {

    }

    private static void saveResults(String destination, boolean isDeleteAfterPulling)
    {

        /*utils.copyFolderFromTo(immigrants_directory,resultsDirectory);
        utils.deleteFile(immigrants_directory);
        (new File(immigrants_directory)).mkdirs();*/

        utils.log("now pulling log files", isPrintSteps);
        device.pullFromDevice("sdcard/Android/data/com.example.mahmoud.batterymonitor/files",destination);
        utils.log("now pulling define folder", isPrintSteps);
        device.pullFromDevice(DEFINE,destination);
        utils.log("now pulling usedDefine", isPrintSteps);
        device.pullFromDevice("sdcard/usedDefine/",destination);
        utils.log("now pulling test-results", isPrintSteps);
        device.pullFromDevice("sdcard/test-results", destination);

        //device.pushToDevice("sdcard/define/immigrants", destination);

        if(isDeleteAfterPulling)
            deleteOldLogs();
    }

    private static void deleteOldLogs()
    {
        utils.log("now removing old log files", isPrintSteps);
        device.remove("sdcard/Android/data/com.example.mahmoud.batterymonitor/files");
        utils.log("now removing old usedDefine", isPrintSteps);
        device.remove("sdcard/usedDefine/");
        device.createFile("sdcard/usedDefine/");

        utils.log("now removing old files test-results", isPrintSteps);
        device.remove("sdcard/test-results");
        device.createFile("sdcard/test-results");

        /*utils.log("now removing old immigrants", isPrintSteps);

        device.remove(DEFINE.concat(immigrants));
        device.createFile(DEFINE.concat(immigrants));*/
    }
    private void connectToPhone(int port, boolean keepConnection)
    {

        ServerSocket serverSocket = null;
        Socket clientSocket = null;

        CommandLineClass commandLineClass = new CommandLineClass(device.deviceId, device.portId);

        try {

            //if(true) System.exit(-1);

            if (!keepConnection) // Mahmoud: this is used when we're optimising using a model and don't need to disconnect the devices.
                commandLineClass.usbPortClose();

            serverSocket.setSoTimeout(0);
            clientSocket = serverSocket.accept();//Thread.sleep(testingTime);
            utils.log(">======incoming on=======> " + serverSocket.getLocalPort(), isPrintSteps);

            clientSocket.close();
            serverSocket.close();

        } catch (Exception e) {
            utils.log("Exception occurred ", isPrintSteps);
            utils.log(e.getMessage(), isPrintSteps);

            utils.log(e.getMessage(), isPrintSteps);
            e.printStackTrace();
        } finally {

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

    public static Hashtable<String, Integer> getCurrentRunningDevices()
    {
        CuncurrentFileProcessing cuncurrentFileProcessing = new CuncurrentFileProcessing();
        File file = new File(current_running_devices_log);
        // read the log
        StringBuilder stringBuilder = cuncurrentFileProcessing.lockFileForReading(file);
        // create the table out of the log
        return cuncurrentFileProcessing.convertFileToHashTable(stringBuilder);
    }
    public void prepareDeviceForRun()
    {
        //OnDeviceExperiment.phoneController.changeWifiState(false);
        //device.prepareForRun();
        OnDeviceExperiment.commandLineClass.usbPortClose();

    }

    public void updateCurrentRunningDevicesLog(Hashtable<String, Integer> hashtable)
    {
        CuncurrentFileProcessing cuncurrentFileProcessing = new CuncurrentFileProcessing();
        StringBuilder stringBuilder = cuncurrentFileProcessing.convertHashToStringBuilder(hashtable);
        cuncurrentFileProcessing.lockFileForWriting(new File(current_running_devices_log),stringBuilder);
    }

}

