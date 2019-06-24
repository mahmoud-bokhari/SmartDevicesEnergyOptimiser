package gin.Mahmoud;



import gin.StructuralTunningExperiment;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by mahmoud on 5/18/2016.
 */
public class CommandLineClass {

    Utils utils = new Utils();
    public static boolean isDebug = false;
    String ADB_SHELL = "";
    String ADB = "";
    int portId = 0;
    private String deviceId;

    public CommandLineClass(String deviceId, int portId)
    {
        this.deviceId = deviceId;
        this.portId = portId;
        ADB = "adb -s "+deviceId+" ";
        ADB_SHELL = ADB+" shell";
    }
    public String getCpuCurrentScalingFrequency()
    {
        String result = "";
        String command = "cat /sys/devices/system/cpu/cpu0/cpufreq/scaling_cur_freq";
        result = runCommandAsSuper(command);
        return result;
    }

    public void setFreq(String freq) {
        //String command = "echo "+freq.getText().toString()+" > /sys/devices/system/cpu/cpu0/cpufreq/scaling_setspeed";
        //System.out.println("freq: " + freq.getText().toString());
        //String command1 = "echo " + freq + " > /sys/devices/system/cpu/cpu0/cpufreq/scaling_setspeed";
        File dir = new File("/sys/devices/system/cpu/");
        //Filter to only list the devices we care about

        int numberOfFiles = getNumberOfCores();
        String commands = "";
        for(int i = 0  ; i <numberOfFiles;i++)
        {
            commands = "chmod 644 /sys/devices/system/cpu/cpu"+i+"/cpufreq/scaling_setspeed";
            runCommandAsSuper(commands);
            commands = "echo " + freq + " > /sys/devices/system/cpu/cpu"+i+"/cpufreq/scaling_setspeed";
            runCommandAsSuper(commands);
        }
        //commands[commands.length-1] = "cat /sys/devices/system/cpu/cpu0/cpufreq/scaling_cur_freq";
//        String command2 = "cat /sys/devices/system/cpu/cpu0/cpufreq/scaling_cur_freq";
 //       String commandResult = runCommandAsSuper(new String[]{ command1, command2});

    }

    public void setMaxSpeed(String freq) {
        //String command = "echo " + String.valueOf(freq) + " > /sys/devices/system/cpu/cpu0/cpufreq/scaling_max_freq";
        int numberOfFiles = getNumberOfCores();
        String commands = "";
        for(int i = 0  ; i <numberOfFiles;i++)
        {
            commands = "chmod 644 /sys/devices/system/cpu/cpu"+i+"/cpufreq/scaling_max_freq";
            runCommandAsSuper(commands);
            commands = "echo " + String.valueOf(freq) + " > /sys/devices/system/cpu/cpu"+i+"/cpufreq/scaling_max_freq";
            runCommandAsSuper(commands);
        }

        //commands[commands.length-1] = "cat /sys/devices/system/cpu/cpu0/cpufreq/scaling_max_freq";
        //String command2 = "cat /sys/devices/system/cpu/cpu0/cpufreq/scaling_max_freq";
        //runCommandAsSuper(new String[]{command, command2});

    }

    public void setMinSpeed(String freq) {
        //String command = "echo " + String.valueOf(freq) + " > /sys/devices/system/cpu/cpu0/cpufreq/scaling_min_freq";
        //String command2 = "cat /sys/devices/system/cpu/cpu0/cpufreq/scaling_min_freq";
        //runCommandAsSuper(new String[]{command, command2});
        int numberOfFiles = getNumberOfCores();
        String commands ="";
        for(int i = 0  ; i <numberOfFiles;i++)
        {
            commands = "chmod 644 /sys/devices/system/cpu/cpu"+i+"/cpufreq/scaling_min_freq";
            runCommandAsSuper(commands);
            commands = "echo " + String.valueOf(freq) + " > /sys/devices/system/cpu/cpu"+i+"/cpufreq/scaling_min_freq";
            runCommandAsSuper(commands);
        }
        //commands[commands.length-1] = "cat /sys/devices/system/cpu/cpu0/cpufreq/scaling_min_freq";

    }



    public void setGovernor(String governor) {
        //String command;
        //command = "echo " + governor + " > /sys/devices/system/cpu/cpu0/cpufreq/scaling_governor";
        //String command2 = "cat /sys/devices/system/cpu/cpu0/cpufreq/scaling_governor";
        //runCommandAsSuper(new String[]{command, command2});


        //setCpuCoreState(governor.equals("userspace"));

        int numberOfFiles = getNumberOfCores();
        String commands = "";
        for(int i = 0  ; i <numberOfFiles;i++)
        {

            commands = "chmod 644 /sys/devices/system/cpu/cpu"+i+"/cpufreq/scaling_governor";
            runCommandAsSuper(commands);
            commands= "echo " + governor + " > /sys/devices/system/cpu/cpu"+i+"/cpufreq/scaling_governor";
            runCommandAsSuper(commands);
        }
        //commands[commands.length-1] = "cat /sys/devices/system/cpu/cpu3/cpufreq/scaling_governor";

    }

   /* public void setCpuCoreState(boolean state) {

        int numberOfFiles = getNumberOfCores();
        String[] commands = new String[(numberOfFiles-1)*2];
        for(int i = 0  ; i <commands.length/2;i++)
        {
            commands[i] = "chmod 644 /sys/devices/system/cpu/cpu"+(i+1)+"/online";// first core is always online can't be shut down
            commands[i+commands.length/2] = "echo " + (state?1:0) + " > /sys/devices/system/cpu/cpu"+(i+1)+"/online";// first core is always online can't be shut down
        }
        //commands[commands.length-1] = "cat /sys/devices/system/cpu/cpu3/cpufreq/scaling_governor";
        runCommandAsSuper(commands);
    }*/

    /*public int getCpuCoreState() {


        int numberOfFiles = getNumberOfCores();
        String[] commands = new String[numberOfFiles-1];
        for(int i = 0  ; i <commands.length;i++)
        {
            commands[i] = "cat /sys/devices/system/cpu/cpu"+(i+1)+"/online";// first core is always online can't be shut down
        }
        //commands[commands.length-1] = "cat /sys/devices/system/cpu/cpu3/cpufreq/scaling_governor";
        return Integer.parseInt(runCommandAsSuper(commands));
    }*/

    public int getBatteryLevel()
    {

        String commands = "dumpsys battery | grep level";

        //commands[commands.length-1] = "cat /sys/devices/system/cpu/cpu3/cpufreq/scaling_governor";
        String result = runCommand(commands);
        result = result.substring(result.indexOf(":")+1);
        if(utils.isNumeric(result.trim()))
            return Integer.parseInt(result.trim());
        else return -1;
    }

    public void setCpuAt(String mode)
    {
        int CpuFreq = 0;
        String[] cpuFrequencies = getSupportedFrequencies();
        // cpu settings:
        if(mode != "default")
        {
            startCpuGovernorController(false);

            //setCpuCoreState(true);
            if (mode == "min") {
                CpuFreq = Integer.parseInt(cpuFrequencies[0]);
            } else if (mode=="max"){
                CpuFreq = Integer.parseInt(cpuFrequencies[cpuFrequencies.length-1]);
            }
            else
            {
                //for(int )
                CpuFreq = Integer.parseInt(mode.trim());
            }


            setGovernor("userspace");
            utils.log("selected governor: userspace",isDebug);

            setMinSpeed(String.valueOf(CpuFreq));
            utils.log(" selected min speed: " + CpuFreq,isDebug);

            setMaxSpeed(String.valueOf(CpuFreq));
            utils.log("selected max speed: " + CpuFreq, isDebug);

            setFreq(String.valueOf(CpuFreq));
            utils.log("selected fixed speed: " + CpuFreq, isDebug);

        }
    }

    /**
     * This is used to change the file permission on Android 7+.
     *
     * @param policy a string describing the permission. w means we can modify the file whereas r to make it a read-only file.
     * */

    public void changeFileAccessPolicy(String policy, String []filePaths)
    {
        String commands = "";
        String policyCode = "";
        if(policy.contains("w")) {
            policyCode = "644";
        }
        else if(policy.contains("r"))
            policyCode = "444";
        else if(policy.contains("all"))
            policyCode = "777";
        else
            return;

        for (int i = 0; i < filePaths.length; i++) {
            commands = "chmod " + policyCode + " " + filePaths[i];
            runCommandAsSuper(commands);
            utils.log("changeFileAccessPolicy: "+commands,isDebug);
        }

        //commands[commands.length-1] = "cat /sys/devices/system/cpu/cpu3/cpufreq/scaling_governor";

    }

    /**
     * http://stackoverflow.com/questions/30119604/how-to-get-the-number-of-cores-of-an-android-device
     * Gets the number of cores available in this device, across all processors.
     * Requires: Ability to peruse the filesystem at "/sys/devices/system/cpu"
     * @return The number of cores, or 1 if failed to get result
    public int getNumberOfCores() {
    //Private Class to display only CPU devices in the directory listing
    class CpuFilter implements FileFilter {
    @Override
    public boolean accept(File pathname) {
    //Check if filename is "cpu", followed by a single digit number
    if(Pattern.matches("cpu[0-9]+", pathname.getName())) {
    return true;
    }
    return false;
    }
    }

    try {
    //Get directory containing CPU info
    File dir = new File("/sys/devices/system/cpu/");
    //Filter to only list the devices we care about
    File[] files = dir.listFiles(new CpuFilter());
    //Return the number of cores (virtual CPU devices)
    return files.length;
    } catch(Exception e) {
    //Default to return 1 core
    return 1;
    }
    }
     */

    public int getNumberOfCores()
    {
        /*if(sBUILD_MODEL.contains("nexus 6")) return 4;
        else if(sBUILD_MODEL.contains("nexus 9")) return 2;*/
        return 4;
    }
    public String getCurrentMaxFreq() {
        String command = "cat /sys/devices/system/cpu/cpu0/cpufreq/scaling_max_freq";
        String commandResult = runCommandAsSuper(command);
        utils.log("getCurrentMaxFreq: " + commandResult,isDebug);
        return commandResult;
    }

    public String getBatteryStatsAdbReport() {
        String command = "dumpsys batterystats --charged";
        String commandResult = runCommandAsSuper(command);
        //Log.d("CommandLineClass", "getBatteryStatsAdbReport: " + commandResult);
        return commandResult;
    }

    public void resetBatteryStatsAdbReport() {
        String command = "dumpsys batterystats --reset";
        String commandResult = runCommandAsSuper(command);

    }

    public String getCurrentMinFreq() {
        String command = "cat /sys/devices/system/cpu/cpu0/cpufreq/scaling_min_freq";
        String commandResult = runCommandAsSuper(command);

        return commandResult;
    }

    public String getCurrentFreq() {
        String fileName = "/sys/devices/system/cpu/cpu0/cpufreq/scaling_cur_freq";

        return readFile(fileName);
    }
    public String readFile(String fileName)
    {
        try {
            FileReader fileReader = new FileReader(fileName);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String result = bufferedReader.readLine();
            fileReader.close();
            bufferedReader.close();
            utils.log("result: " + result,isDebug);
            return result;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            utils.log("readFile: could not read the given file",isDebug);
            return "";
        }
    }

    public String getCurrentGovernor() {
        String command = "cat /sys/devices/system/cpu/cpu0/cpufreq/scaling_governor";
        String commandResult = runCommandAsSuper(command);
        return commandResult;
    }

    public String[] getSupportedFrequencies()
    {
        String command = "cat /sys/devices/system/cpu/cpu0/cpufreq/scaling_available_frequencies";
        String[] result = runCommandAsSuper(command).split("\\s+");

        return result;
    }


    public String getTemperatureUsingThermalZone()
    {
        String command = "cat /sys/devices/virtual/thermal/thermal_zone*/temp";
        String[] result = runCommandAsSuper(command).split("\n");

        return Arrays.toString(result).replace("[","").replace("]","");
    }

    public int getTemperature(int index)
    {
        String command = "cat /sys/devices/virtual/thermal/thermal_zone"+index+"/temp";
        String result = runCommandAsSuper(command);
        //Log.d("CommandLineClass", "getTemperature: ");
        try {
            return Integer.parseInt(result.replaceAll("\n",""));
        }
        catch (Exception e){e.printStackTrace();}
        return Integer.MAX_VALUE;
    }


    public String[] getSupportedGovernors()
    {
        String command = "cat /sys/devices/system/cpu/cpu0/cpufreq/scaling_available_governors";
        String[] result = runCommandAsSuper(command).split(" ");

        return result;
    }

    public Long[] getSupportedFrequenciesInLong()
    {
        String command = "cat /sys/devices/system/cpu/cpu0/cpufreq/scaling_available_frequencies";
        String[] result = runCommandAsSuper(command).split(" ");
        Long[] finalResult = new Long[result.length];
        for(int i = 0 ; i < result.length; i++)
            finalResult[i] = Long.parseLong(result[i]);
        return finalResult;
    }
    public void startCpuGovernorController(boolean state)
    {
        String command;
        if(state)
            command = "setprop ctl.start mpdecision;start mpdecision";
        else
            command = "setprop ctl.stop mpdecision;stop mpdecision";

        runCommandAsSuper(command);

    }
    /**
     * @return Always contain a new line!
     * */
   /* public String runCommandAsSuper(String[] commands)
    {
        String []suCommands = new String[commands.length+1];
        String line = "";
        for(int i=0 ; i < commands.length; i++)
        {
            if(!commands[i].equals("")) {
                suCommands[i] = commands[i];
            }
        }
        suCommands[suCommands.length-1] = "exit";
        String commandResult = "";
        String errorString = "";
        try {
            Process process;
            process = Runtime.getRuntime().exec(ADB_SHELL.concat(" su -c "+ commands[0]));

            DataOutputStream outputStream = new DataOutputStream(process.getOutputStream());
            for (int i = 0; i < suCommands.length; i++)
            {
                //outputStream.writeBytes(suCommands[i] + "\n");
            }
            outputStream.writeBytes("exit\n");
            outputStream.flush();

            BufferedReader stdInput = new BufferedReader(new
                    InputStreamReader(process.getInputStream()));

            BufferedReader stdError = new BufferedReader(new
                    InputStreamReader(process.getErrorStream()));

            // read the output from the command



            while ((line = stdInput.readLine()) != null) {
                commandResult+=line +  "\n";
            }
            *//*if(commandResult!= "") {
                System.out.println("Here is the standard output of the command:\n");
                if (!commands[0].contains("dumpsys batterystats --charged"))
                    Log.d("CommandLineClass", "runCommandAsSuper: " + commandResult);
            }*//*
            // read any errors from the attempted command

            while ((line = stdError.readLine()) != null) {
                errorString+=line+"\n";

            }
            if(errorString!="") {
                System.out.println("Here is the standard error of the command:\n");
                utils.log("runCommandAsSuper: " + errorString,isPrintSteps);

            }

            stdError.close();
            stdInput.close();

        }
        catch (Exception e) {
            e.printStackTrace();
            System.out.println(line);
            utils.log(line,isPrintSteps);
            if(line.contains("device offline")) {
                try {
                    usbPortReopen();
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
                runCommand(commands);
            }
            utils.log(" can't compileUsingAndroidSDK su user, now running as normal user",isPrintSteps);
            commandResult = runCommand(suCommands);
        }
        return commandResult;
    }*/
    public String runCommandAsSuper(String command)
    {
        String result="";
        try {
            result = doAdbExeSafely(ADB_SHELL.concat(" su -c ").concat(command));
        } catch (InterruptedException e) {
            utils.log("error: "+e.getMessage(),isDebug);
            e.printStackTrace();
        }
        return result;
    }

    public String runCommand(String commands)
    {
        String result="";
        try {
            result = doAdbExeSafely(ADB_SHELL.concat(" "+commands));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return result;
    }

    public String runAdbCommand(String command)
    {
        String result="";
        try {
            result = doAdbExeSafely(ADB.concat(command));
        } catch (InterruptedException e) {
            utils.log("error: "+e.getMessage(),isDebug);
            e.printStackTrace();
        }
        return result;
    }

    static public String runCommandUnsafely(String command)
    {
        String commandResult="";
        //ArrayList<Device> results = new ArrayList<>();
        Utils utils1 = new Utils();
        try {
            int count =0;
            while(count < 5)
            {
                commandResult = new Executor(command).getFullInput(false);
                if(!commandResult.contains("not running") ||
                        !commandResult.contains("error"))
                    break;
                new Executor("adb kill-server").getFullInput(false);
                Thread.sleep(1000);
                count++;
            }

            /*String[] allLines = commandResult.split("\n");
            for(int i=0; i< allLines.length; i++)
            {
                String[] temp = allLines[i].split("\t");
                results.add(new Device()); // temp[0] should contains the device ID, temp[1] its status online, offline...
            }*/
        } catch (Exception e) {
            utils1.log("error: "+e.getMessage(),isDebug);
            e.printStackTrace();
        }
        //return results;
        return commandResult;
    }

    static public String getListOfDevices()
    {
        return runCommandUnsafely("adb devices");
    }

    public String runAdbRootCommand(String command)
    {
        String result="";
        try {
            result = doAdbExeSafely(ADB.concat(command));
        } catch (InterruptedException e) {
            utils.log("error: "+e.getMessage(),isDebug);
            e.printStackTrace();
        }
        return result;
    }
    public void setRecharging(boolean state)
    {
        String command = "";
        if(state)
            command ="echo 1 > /sys/class/power_supply/battery/charging_enabled";
        else
            command ="echo 0 > /sys/class/power_supply/battery/charging_enabled";
        runCommandAsSuper(command);
    }

    /*public String runCommand(String[] commands)
    {

        String commandResult = "";
        try {
            Process process;
            process = Runtime.getRuntime().exec(ADB_SHELL.concat(" "+commands[0]));
            String line = null;

            BufferedReader stdInput = new BufferedReader(new
                    InputStreamReader(process.getInputStream()));

            BufferedReader stdError = new BufferedReader(new
                    InputStreamReader(process.getErrorStream()));

            // read the output from the command


            while ((line = stdInput.readLine()) != null) {
                utils.log(line,isPrintSteps);
                commandResult+=line +  "\n";
            }
            // read any errors from the attempted command

            while ((line = stdError.readLine()) != null) {
                System.out.println(line);
                utils.log(line,isPrintSteps);
                if(line.contains("device offline")||line.contains("error")||!line.contains(Device.deviceId)) {
                    usbPortReopen();
                    Thread.sleep(2000);
                    runCommand(commands);
                }
            }
            stdError.close();
            stdInput.close();

        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return commandResult;
    }*/

    public String doAdbExeSafely(String command) throws InterruptedException {
        String result = "";
        while (true) {
            final String adbDevices = "adb devices";
            String output = new Executor(adbDevices).getFullInput(false);

            int idxDevice = output.indexOf(deviceId);
            if (idxDevice != -1) {
                int idxEnd = output.indexOf("\n", idxDevice);
                if (idxEnd == -1) idxEnd = output.length();
                if (output.substring(idxDevice, idxEnd).contains("offline")) {
                    utils.log("[1st] contains \"offline\"",isDebug);
                } else {
                    break;
                }
            } else {
                utils.log("[1st] Device id not found!",isDebug);
            }
            usbPortReopen();
            AdbExecutor.killServer(portId);
        }

        while (true) {
            utils.log("ADB executing: " + command,isDebug);
            AdbExecutor e = new AdbExecutor(command, portId);
            String output = e.getFullInput(false);
            if ((output.contains("error") || output.contains("offline") || output.contains("smartsocket"))
                    && !output.contains("does not exist")) { // if remove object does not exist, skip
                // reopen usb port
                utils.log("[2nd] adb contains \'error\' or \'offline\'\n" + output,isDebug);
                usbPortReopen();

                // restart adb server
//                    new Executor(BASE_ADB_PATH + "devices", false);
                AdbExecutor.killServer(portId);
            } else {
                result=output;
                break;
            }
        }
        return result;
    }

    public  void usbPortOpen() {
        final String usbOn = "python usbhub.py open " + portId;
        new Executor(usbOn);
    }

    public  void usbPortClose() {
        final String usbOn = "python usbhub.py close " + portId;
        new Executor(usbOn);
    }
    public  void usbPortReopen() throws InterruptedException {
        usbPortClose();
        Thread.sleep(500);
        usbPortOpen();
    }
}
