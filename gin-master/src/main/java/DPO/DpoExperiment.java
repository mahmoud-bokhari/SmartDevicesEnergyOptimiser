package DPO;

import SolutionGeneratorDPO.ConfigurationFileProcessor;
import gin.*;
import gin.Mahmoud.CommandLineClass;
import gin.Mahmoud.Device;
import gin.Mahmoud.Utils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;

import static DPO.DpoExperimentRunner.MAE_CONSTRAINT;

/**
 * Created by Mahmoud-Uni on 6/17/2019.
 */
public class DpoExperiment {

    public String MAIN_DIRECTORY = "E:\\gin-rebound\\gin-master\\reboundPC\\DPO\\";
    public String REBOUND_PC = "E:\\gin-rebound\\gin-master\\reboundPC\\"; // this is used for on pc testing
    public String JAVA="java ";
    public String CLASS_PATH="E:\\AppData\\local\\sdk\\platforms\\android-23\\data\\res;C:\\Users\\Mahmoud-Uni\\.android\\build-cache\\69c87f5e3335f932ac272c0ab4dd05ecd6a334b6\\output\\jars\\classes.jar;C:\\Users\\Mahmoud-Uni\\.android\\build-cache\\69c87f5e3335f932ac272c0ab4dd05ecd6a334b6\\output\\res;E:\\gin-rebound\\gin-master\\reboundPC\\app\\libs\\mockito-all-1.9.5.jar;C:\\Users\\Mahmoud-Uni\\.android\\build-cache\\da4f894dda8d89b6eba05be4ba2352ebae0aecb1\\output\\res;C:\\Users\\Mahmoud-Uni\\.android\\build-cache\\da4f894dda8d89b6eba05be4ba2352ebae0aecb1\\output\\jars\\classes.jar;C:\\Users\\Mahmoud-Uni\\.android\\build-cache\\401e1a1318bba85c94c76b2e5df5861e56a0f848\\output\\res;C:\\Users\\Mahmoud-Uni\\.android\\build-cache\\401e1a1318bba85c94c76b2e5df5861e56a0f848\\output\\jars\\classes.jar;H:\\gradle\\.gradle\\caches\\modules-2\\files-2.1\\org.hamcrest\\hamcrest-core\\1.3\\42a25dc3219429f0e5d060061f71acb49bf010a0\\hamcrest-core-1.3.jar;C:\\Users\\Mahmoud-Uni\\.android\\build-cache\\c4959a5121e578fdc25486b24e7cbb0989e68100\\output\\jars\\classes.jar;C:\\Users\\Mahmoud-Uni\\.android\\build-cache\\c4959a5121e578fdc25486b24e7cbb0989e68100\\output\\res;E:\\AppData\\local\\sdk\\extras\\android\\m2repository\\com\\android\\support\\support-annotations\\23.4.0\\support-annotations-23.4.0.jar;C:\\Users\\Mahmoud-Uni\\.android\\build-cache\\41e62914f76174941b4eda7f4b595c322f9c9cec\\output\\res;C:\\Users\\Mahmoud-Uni\\.android\\build-cache\\41e62914f76174941b4eda7f4b595c322f9c9cec\\output\\jars\\classes.jar;C:\\Users\\Mahmoud-Uni\\.android\\build-cache\\41e62914f76174941b4eda7f4b595c322f9c9cec\\output\\jars\\libs\\internal_impl-23.4.0.jar;H:\\gradle\\.gradle\\caches\\modules-2\\files-2.1\\junit\\junit\\4.11\\4e031bb61df09069aeb2bffb4019e7a5034a4ee0\\junit-4.11.jar;E:\\gin-rebound\\gin-master\\reboundPC\\build\\generated\\mockable-android-23.jar;" +
            "E:\\gin-rebound\\gin-master\\reboundPC\\build-tools\\android-23\\android.jar;";
    String BIN = "E:\\gin-rebound\\gin-master\\reboundPC\\bin";
    String GEN = "E:\\gin-rebound\\gin-master\\reboundPC\\gen";



    public static final int On_PC_RUNS = 4;

    public String RESULT_DIRECTORY = "E:\\gin-rebound\\gin-master\\reboundPC\\DPO\\results\\";
    public String PHONE_LOGS_DIRECTORY = "E:\\gin-rebound\\gin-master\\reboundPC\\DPO\\results\\";
    public String ORIGINAL_CONFIG_NAME = "originalConfig.csv";
    public String CONFIG_FILE_NAME = "config.csv";

    private HashMap<String, String> originalConfig;
    private HashMap<String, String> configHashMap;
    private String[] selectedVariables;
    private String[] defaultValues;

    private String headers = "fuel use, MAE, test suite execution time, wasFitter, pValue, number of samples, pc test execution time," +
            "deployment time, in-vivo time, complete evaluation time, extra info, samples";

    String TEST_SUITE = "com.example.mahmoud.modifiedrebound.devSubsetPc";


    public String EXPERIMENT_DATE_TIME;

    public Utils utils = new Utils();


    public int portId=0;

    public Device device;
    public static boolean isPrintSteps = true;
    public static boolean isDebug=false;

    public DpoExperiment( Device device) {
        this.device = device;
        EXPERIMENT_DATE_TIME = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss").format(System.currentTimeMillis());
        EXPERIMENT_DATE_TIME = EXPERIMENT_DATE_TIME.replace(" ","_");

        setWorkingDirectories();
        reloadOriginalSourceFile();
        // load the configuration file into a hashmap
        ConfigurationFileProcessor configurationFileProcessor = new ConfigurationFileProcessor();
        originalConfig = configurationFileProcessor.readConfigFile(MAIN_DIRECTORY.concat(ORIGINAL_CONFIG_NAME));

        // load the selected solutions.

        selectedVariables=new String[5];
        defaultValues= new String[5];

        configurationFileProcessor.loadSelectedVariables(selectedVariables,defaultValues,MAIN_DIRECTORY.concat("replaces_selection.dat"));
        configHashMap = configurationFileProcessor.readConfigFile(MAIN_DIRECTORY.concat(ORIGINAL_CONFIG_NAME));
    }

    private void setWorkingDirectories()
    {
        if(device.isRunInVivo) {
            MAIN_DIRECTORY = "E:\\gin-rebound\\gin-master\\"+device.deviceName+"\\DPO\\";
            REBOUND_PC = "E:\\gin-rebound\\gin-master\\"+device.deviceName+"\\reboundPC\\";
            RESULT_DIRECTORY = "E:\\gin-rebound\\gin-master\\" + device.deviceName + "\\DPO\\results"+"\\"+ EXPERIMENT_DATE_TIME+"\\";
            PHONE_LOGS_DIRECTORY = RESULT_DIRECTORY+"phoneLogs";

            BIN = "E:\\gin-rebound\\gin-master\\"+device.deviceName+"\\reboundPC\\bin" ;
            GEN = "E:\\gin-rebound\\gin-master\\"+device.deviceName+"\\reboundPC\\gen";
            CLASS_PATH = CLASS_PATH.concat(BIN);



            try {
                File file = new File(RESULT_DIRECTORY);
                file.mkdirs();
                file = new File(PHONE_LOGS_DIRECTORY);
                file.mkdirs();

                file = new File(BIN);
                file.mkdirs();
                file = new File(GEN);
                file.mkdirs();
            }
            catch (Exception e)
            {
                utils.log("issues with setting and creating directories "+e.getMessage(),isPrintSteps);
                e.printStackTrace();
            }
        }
        else
        {
            MAIN_DIRECTORY = "E:\\gin-rebound\\gin-master\\reboundPC\\DPO\\";

            CLASS_PATH = CLASS_PATH.concat(BIN.concat(";"));
            RESULT_DIRECTORY = MAIN_DIRECTORY.concat("results\\").concat(EXPERIMENT_DATE_TIME+"\\");
        }

    }

    void reloadOriginalSourceFile()
    {

        try {
            FileUtils.deleteQuietly(new File(MAIN_DIRECTORY, CONFIG_FILE_NAME));
            System.out.println(MAIN_DIRECTORY);
            FileUtils.copyFile(new File(MAIN_DIRECTORY , ORIGINAL_CONFIG_NAME), new File(MAIN_DIRECTORY, CONFIG_FILE_NAME));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public DpoExperimentResults run(Solution solution) {
        // first write the solution into a csv file.
        writeSolutionIntoCSV(solution);
        DpoExperimentResults dpoExperimentResults;
        if(device.isRunInVivo) {
            dpoExperimentResults = run(solution, device.getMinimalSetSize());
            dpoExperimentResults.solution = solution;
        }
        else {
            dpoExperimentResults = run(solution, On_PC_RUNS);
            dpoExperimentResults.solution = solution;
        }
        return dpoExperimentResults;
    }
    public void writeSolutionIntoCSV(Solution solution)
    {
        utils.log("now write the solution: "+(utils.arrayToString(solution.getDecisionVariables())
        +" into a CSV file "+MAIN_DIRECTORY.concat(CONFIG_FILE_NAME)),isPrintSteps);
        ConfigurationFileProcessor configurationFileProcessor = new ConfigurationFileProcessor();
        configHashMap = configurationFileProcessor.updateConfigurationFile(configHashMap,selectedVariables,utils.doubleArrayToStringArray(solution.getDecisionVariables()));
        configurationFileProcessor.saveConfigurationFile(configHashMap,MAIN_DIRECTORY.concat(CONFIG_FILE_NAME));
        try {
            FileUtils.copyFileToDirectory(new File(MAIN_DIRECTORY,CONFIG_FILE_NAME),new File(REBOUND_PC));
        } catch (IOException e) {
            utils.log("Couldn't copy the config file to "+REBOUND_PC+" for on PC testing!!!",true);
            utils.log(e.getMessage(),true);
            e.printStackTrace();
        }
    }
    public void deleteEvaluationValues()
    {
        try {
            if(new File(MAIN_DIRECTORY,"evaluationValues.txt").exists())
                FileUtils.forceDelete(new File(MAIN_DIRECTORY,"evaluationValues.txt"));
        } catch (Exception e) {
            utils.log(e.getMessage(),isDebug);
            e.printStackTrace();
        }
    }

    public DpoExperimentResults run(Solution solution, int reps) {

        deleteEvaluationValues();

        try {
            long initialTime = System.currentTimeMillis();

            utils.log("run the tests on the PC",isPrintSteps);
            long PcTestTime = System.currentTimeMillis();
            boolean pcTestOK = runTestOnPc(1);
            if(!device.isRunInVivo)
                if(pcTestOK && reps > 1)
                    runTestOnPc(reps-1);

            PcTestTime = System.currentTimeMillis() - PcTestTime;
            utils.log("test on PC took(ms): "+PcTestTime, isPrintSteps);
            DpoExperimentResults result;
            if(!pcTestOK)
            {
                utils.log("test on PC wasn't successful",isPrintSteps);
                result = new DpoExperimentResults(-1,-1,-1,"test on PC wasn't successful",device.deviceName);
                result.pcTestExecutionTime = (int)PcTestTime;
                return result;
            }

            // read the results
            result = readResults(MAIN_DIRECTORY);// MAIN_DIRECTORY+File.separator+"app"
            
            result.pcTestExecutionTime = (int)PcTestTime;

            if(result.MAE == -1) // test is not successful!
            {
                return result;
            }

            // check the constraint
            utils.log("check the constraint on the MAE (accuracy): "+result.MAE,isPrintSteps);
            if(result.MAE > MAE_CONSTRAINT) {
                utils.log("MAE is greater than "+ MAE_CONSTRAINT
                        +", invalid solution!",isPrintSteps);
                result.extraInfo="constraint was violated";
                //result.testExecutionTime = Double.MAX_VALUE;
                //result.fuelUse = Double.MAX_VALUE;
            }
            else
                utils.log("check the constraint on the MAE (accuracy) ... no violation",isPrintSteps);

            if(device.isRunInVivo)
            {
                //check the mae if it's less than MAE_CONSTRAINT deploy otherwise return the current fitness evaluation which is fuel_use = Double.MAX
                if(result.MAE < MAE_CONSTRAINT)
                {
                    // prepare the phone
                    long operationTime= System.currentTimeMillis();
                    device.prepare();
                    operationTime = System.currentTimeMillis() - operationTime;
                    utils.log("preparation time (ms): "+operationTime, isPrintSteps);
                    // deploy the app
                    operationTime = System.currentTimeMillis();

                    device.deploy(isDebug,MAIN_DIRECTORY.concat(CONFIG_FILE_NAME));
                    operationTime = System.currentTimeMillis() - operationTime;
                    utils.log("deploy time (ms): "+operationTime, isPrintSteps);
                    result.deploymentTime = (int)operationTime;

                    // now run the instrumentation test
                    operationTime = System.currentTimeMillis();
                    device.runInstrumentationTestInVivo(reps);
                    operationTime = System.currentTimeMillis() - operationTime;
                    utils.log("in-vivo evaluation time (ms): "+operationTime, isPrintSteps);
                    result.inVivoEvaluationTime = (int)operationTime;


                    operationTime = System.currentTimeMillis();
                    device.pullExperimentsLogs("sdcard/Android/data/com.example.mahmoud.batterymonitor/files",PHONE_LOGS_DIRECTORY);
                    device.remove("sdcard/Android/data/com.example.mahmoud.batterymonitor/files");
                    copyEvaluationValuesFile(PHONE_LOGS_DIRECTORY.concat("\\"+device.directoryNumber), MAIN_DIRECTORY);
                    //device.pull("sdcard/Android/data/com.example.mahmoud.batterymonitor/files/evaluationValues.txt",MAIN_DIRECTORY);//


                    result = readResults(MAIN_DIRECTORY); // MAIN_DIRECTORY+File.separator+"app"
                    operationTime = System.currentTimeMillis() - operationTime;
                    utils.log("result fetching time (ms): "+operationTime, isPrintSteps);

                    device.updateCurrentBatteryLevel();
                }
                //device.updateCurrentBatteryLevel();
            }
            utils.log("complete evaluation time(ms): "+(System.currentTimeMillis()-initialTime), isPrintSteps);

            result.completeEvaluationTime = (int)(System.currentTimeMillis()-initialTime);
            //reloadOriginalSourceFile();
            return result;
        }
        catch (Exception e)
        {
            //reloadOriginalSourceFile();
            e.printStackTrace();
            return new DpoExperimentResults(-1,-1,-1, "Exception: "+e.getMessage(), device.deviceName);//
        }

    }
    

    public void copyEvaluationValuesFile(String location, String destination)
    {
        try {
            utils.log("copying evaluation values...",isPrintSteps);
            String fileLocation = location+"\\evaluationValues.txt";
            FileUtils.copyFileToDirectory(new File(fileLocation),new File(destination));
        }
        catch (Exception e)
        {
            utils.log("failed to copy evaluation values...",isPrintSteps);
            e.printStackTrace();
            copyBadEvaluationValues(destination);
            utils.log(e.getMessage(),isPrintSteps);
        }
    }

    public void copyBadEvaluationValues(String destination)
    {
        try {
            utils.log("copying bad evaluation values...",isPrintSteps);
            FileWriter fileWriter = new FileWriter(destination+"\\evaluationValues.txt");
            for(int i=0;i<10;i++)
            {
                fileWriter.append(Double.MAX_VALUE+","+Double.MAX_VALUE+","+Double.MAX_VALUE+","+Double.MAX_VALUE+","+Double.MAX_VALUE+","+Double.MAX_VALUE+","+Double.MAX_VALUE+","+Double.MAX_VALUE+","+Double.MAX_VALUE+","+Double.MAX_VALUE+","+Double.MAX_VALUE+","+Double.MAX_VALUE+","+Double.MAX_VALUE+","+Double.MAX_VALUE+"\n");
            }
            fileWriter.flush();
            fileWriter.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            utils.log(e.getMessage(),isPrintSteps);
        }
    }

    /**
     * Read the results of the test from the csv file
     * NOTE: since this method doesn't know the patch and the source code, it will always result in null values for both object
     * therefore, it's the client's responsibility to "REASSIGN them
     * */
    DpoExperimentResults readResults(String path)
    {
        try {
            File resultDir = new File(path);
            File evaluationValuesFile = new File(resultDir, "evaluationValues.txt");


            if(!evaluationValuesFile.exists()) {
                utils.log("evaluationValues.txt wasn't found! "+evaluationValuesFile.getAbsolutePath(), isPrintSteps);
                return new DpoExperimentResults( -1, -1,-1, "evaluationValues wasn't found", device.deviceName); // evaluation values
            }
            BufferedReader br = new BufferedReader(new FileReader(evaluationValuesFile));
            String line = "";
            //double timeResults = -1;
            DescriptiveStatistics objectiveStats = new DescriptiveStatistics();
            DescriptiveStatistics inVivoTestTime = new DescriptiveStatistics();
            double mae = -1;
            double fuel = -1;

            utils.log("Now reading file: "+evaluationValuesFile.getAbsolutePath(),isDebug);
            while ((line=br.readLine())!= null) {
                utils.log("evaluation: "+line,isPrintSteps);
                String[] tempArr = line.split(",");

                mae = Double.parseDouble(tempArr[1]);
                if(device.isRunInVivo) {
                    //timeResults = ; // 3 advance time, 5 execution time (by android).
                    fuel = Double.parseDouble(tempArr[0]);
                    objectiveStats.addValue(fuel);
                    inVivoTestTime.addValue(Double.parseDouble(tempArr[5]));
                }
                else {
                    // if not in-vivo, then we don't have fuel, therefore store the time in it as well.
                    //timeResults = ; // advanceTotalTimeNs 6, testSuiteTotalTimeNs 7.
                    objectiveStats.addValue(Double.parseDouble(tempArr[7]));
                }
            }
            br.close();

            deleteEvaluationValues();

            // experimentResults.fuel is the median of the fuel of the in-vivo  runs.
            // If it is not in-vivo then experimentResults.fuel is the median of advance time of the in-vivo runs
            DpoExperimentResults results;
            if(device.isRunInVivo)
                results = new DpoExperimentResults(inVivoTestTime.getPercentile(50), objectiveStats.getPercentile(50), mae, "", device.deviceName);
            else
                results = new DpoExperimentResults(objectiveStats.getPercentile(50), objectiveStats.getPercentile(50), mae, "", device.deviceName);
            results.objectiveStats = objectiveStats;
            if(mae<0)
                results.extraInfo="timeout solution";
            else if(mae == Double.MAX_VALUE)
                results.extraInfo="timeout solution";
            return results;
        }
        catch (Exception e)
        {
            utils.log(e.getMessage(),isPrintSteps);
            e.printStackTrace();
        }

        //return new StructuralTunningExperiment.StructuralExperimentResults(null, -1, true, true, "", null, -1, -10); // Exception
        return null;
    }


    public class DpoExperimentResults {
        public double fuelUse = -1;
        public double MAE = -1; // second objective or a constraint
        public double testExecutionTime = -1;
        //public double advanceExecutionTime = -1;
        Solution solution;

        public String deviceName="";


        public DescriptiveStatistics objectiveStats = new DescriptiveStatistics();
        public boolean wasFitter=false;
        public double pValue=-1;

        public int pcTestExecutionTime = 0;
        public int deploymentTime=0;
        public int inVivoEvaluationTime=0;
        public int completeEvaluationTime = 0;

        String extraInfo="";



        public DpoExperimentResults(double testExecutionTime,  double fuel, double mae, String extraInfo,String deviceName) {


            this.solution = new Solution(5);
            this.testExecutionTime = testExecutionTime;
            this.fuelUse = fuel;
            this.MAE = mae;
            this.extraInfo = extraInfo;
            if(!device.isRunInVivo) fuelUse = testExecutionTime;
            this.deviceName = deviceName;
        }


        public DpoExperimentResults clone()
        {
            DpoExperimentResults cloned = new DpoExperimentResults(this.testExecutionTime,this.fuelUse,this.MAE, this.extraInfo, this.deviceName);

            cloned.solution = this.solution;
            cloned.wasFitter = this.wasFitter;
            cloned.pValue = this.pValue;

            cloned.deviceName = this.deviceName;
            //cloned.advanceExecutionTime = this.advanceExecutionTime;

            cloned.pcTestExecutionTime = this.pcTestExecutionTime;
            cloned.completeEvaluationTime = this.completeEvaluationTime;


            for(double d : this.objectiveStats.getValues())
                cloned.objectiveStats.addValue(d);
            return cloned;
        }
        public String toString() {
            return String.format("fuel use: %f; Time: %f; MAE: %f;",this.fuelUse, this.testExecutionTime, this.MAE);
        }

        public String prepareForPrinting()
        {

            return String.format("%s,%s,%s,%f,%s,%f,%b,%f,%d,%d,%d,%d,%d,%s,%s",deviceName,utils.arrayToString(solution.getDecisionVariables()),
                    utils.arrayToString(solution.getSigma()),this.fuelUse,String.valueOf(this.MAE),this.testExecutionTime,wasFitter,pValue,
                    objectiveStats.getValues().length,pcTestExecutionTime,deploymentTime,inVivoEvaluationTime,completeEvaluationTime,

                    extraInfo,Arrays.toString(objectiveStats.getValues()).replace("[","").replace("]",""));
        }
    }


    public boolean runTestOnPc(int reps)
    {
        for(int i=0;i<reps;i++)
            if(!runTestOnPc())
                return false;
        return true;
    }
    public  boolean runTestOnPc() //
    {
        try {
            String testResults = MAIN_DIRECTORY + "testResults.txt";

            utils.log("run the test on the pc", isDebug);
            StringBuilder commandResult = new StringBuilder(utils.runCMD("cmd /c cd " + MAIN_DIRECTORY + " && " + JAVA + " -classpath \"" + CLASS_PATH + "\" org.junit.runner.JUnitCore " + TEST_SUITE + "  " // > nul
                    , testResults, isDebug));
            if (utils.checkForFailure(commandResult)) {
                utils.log("test on PC failed!!!", isDebug);
                return false;
            }
            utils.log("test on PC is successful!!!", isDebug);
            FileUtils.forceDelete(new File(testResults));
            return true;
        }
        catch (Exception e)
        {
            utils.log(e.getMessage(), isPrintSteps);
            e.printStackTrace();
        }
        return false;

    }


    /**
     * Callback for the compilation. Used to silence the compiler.
     */

    public void saveHistoricalBestSolutions(int step) {

        try {
            File usedDir = new File(RESULT_DIRECTORY.concat("\\").concat("usedBest").concat("\\"));
            usedDir.mkdirs();
            usedDir = new File(RESULT_DIRECTORY.concat("\\").concat("usedBest").concat("\\"+step+".csv"));

            FileUtils.copyFile(new File(MAIN_DIRECTORY,CONFIG_FILE_NAME),usedDir);

        }
        catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void saveResults(StringBuilder data, boolean append, String fileName)
    {
        FileWriter fileWriter;
        BufferedWriter bufferedWriter;
        try
        {
            new File(RESULT_DIRECTORY).mkdirs();
            File file = new File(RESULT_DIRECTORY, fileName);

            String allData = "";
            if(!file.exists()) {
                StringBuilder stringBuilder = new StringBuilder();
                for(int i=0; i<selectedVariables.length; i++)
                    stringBuilder.append(selectedVariables[i]+",");
                for(int i=0; i<Solution.size; i++)
                    stringBuilder.append("sigma "+(1+i)+",");
                stringBuilder.append(headers);
                allData = stringBuilder.toString() + "\n" + data.toString();
            }
            else allData=data.toString();
            fileWriter = new FileWriter(file,append);
            bufferedWriter = new BufferedWriter(fileWriter);
            bufferedWriter.write(allData);
            //fileWriter.
            bufferedWriter.flush();
            bufferedWriter.close();
        }
        catch (Exception e)
        {
            //fileWriter=null;
            //bufferedWriter=null;
            e.printStackTrace();
        }
    }

    public void rechargeTill(int batteryLevel)
    {
        try {
            if(device.isRunInVivo)
            {
                int counter = 0;
                int limit = 2;
                int bootCounter = 0;
                long sleepFor = 5 * 60 * 1000;

                device.updateCurrentBatteryLevel();
                int previousLevel = device.currentBatteryLevel;

                CommandLineClass commandLineClass = new CommandLineClass(device.deviceId,portId);
                while( device.currentBatteryLevel < batteryLevel)
                {
                    Thread.sleep(sleepFor);

                    if(bootCounter > 0)
                    {
                        utils.log("There's a problem in the device, it is not recharging and" +
                                " it has been rebooted, the experiment will proceed...", isPrintSteps);
                    }
                    device.updateCurrentBatteryLevel();
                    if(previousLevel == device.currentBatteryLevel)
                    {
                        counter++;
                        utils.log("battery level didn't change!", isPrintSteps);
                    }

                    if(counter > limit)
                    {
                        utils.log("The device was plugged in for (mins): "+(sleepFor/60/1000), isPrintSteps);
                        utils.log("reboot and recharge again", isPrintSteps);
                        commandLineClass.runAdbCommand("reboot");
                        Thread.sleep(sleepFor/2); // this is an additional recharging period for rebooting.
                        bootCounter++;
                    }
                    previousLevel = device.currentBatteryLevel;
                }


            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            utils.log("exception: "+e.getMessage(), isPrintSteps);
        }
    }

    public Double[] getSelectedVariables()
    {
        //return getSelectedVariables(originalConfig,(String[]) selectedVariables.keySet().toArray());

        return utils.arrayToDoubleArray(defaultValues);
    }
    public Double[] getSelectedVariables(HashMap<String, String> configurationFileHashMap, String[] keys)
    {
        Double[] values = new Double[keys.length];
        try {
            for(int i=0;i<keys.length;i++)
            {
                if(configurationFileHashMap.containsKey(keys[i]))
                    values[i] = Double.parseDouble(configurationFileHashMap.get(keys[i]));
                else
                    utils.log("key: "+keys[i]+" is not found!",isPrintSteps);
            }
            return values;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            utils.log("Exception in updating configuration file "+e.getMessage(), true);
            return values;
        }
    }
}
