package gin;

import gin.Mahmoud.CommandLineClass;
import gin.Mahmoud.Device;
import gin.Mahmoud.Utils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.junit.runner.Result;

import javax.tools.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Arrays;

import static gin.StructuralTunningExperimentRunner.isPrintSteps;

public class StructuralTunningExperiment extends TestRunner {

    public  String App_Directory = "E:\\gin-rebound\\gin-master\\reboundPC" + File.separator;


    public  String Java_Source_Directory = App_Directory +  "app\\src\\main\\java\\com\\example\\mahmoud\\modifiedrebound" ;
    public  String Test_Source_Directory = App_Directory +  "app\\src\\test\\java\\com\\example\\mahmoud\\modifiedrebound";
    public  String RESULT_DIRECTORY = "E:\\gin-rebound\\gin-master\\reboundPC\\results";
    public  String PHONE_LOGS_DIRECTORY = "E:\\gin-rebound\\gin-master\\reboundPC\\results";


    String APP_NAME="Modified_Rebound";
    String PACKAGE_NAME="com.example.mahmoud.modifiedrebound";
    String TEST_SUITE = "com.example.mahmoud.modifiedrebound.devSubsetPc";
    String BIN = App_Directory+"bin";
    String GEN = App_Directory+"gen";
    String ANDROID_AAPT= App_Directory+"build-tools\\25.0.0\\aapt.exe";
    String ANDROID_DX = "java -jar "+App_Directory+"build-tools\\25.0.0\\lib\\dx.jar --dex";
    String PLATFORM=App_Directory+"build-tools\\android-23\\android.jar";
    String CLASS_PATH="E:\\AppData\\local\\sdk\\platforms\\android-23\\data\\res;C:\\Users\\Mahmoud-Uni\\.android\\build-cache\\69c87f5e3335f932ac272c0ab4dd05ecd6a334b6\\output\\jars\\classes.jar;C:\\Users\\Mahmoud-Uni\\.android\\build-cache\\69c87f5e3335f932ac272c0ab4dd05ecd6a334b6\\output\\res;E:\\gin-rebound\\gin-master\\reboundPC\\app\\libs\\mockito-all-1.9.5.jar;C:\\Users\\Mahmoud-Uni\\.android\\build-cache\\da4f894dda8d89b6eba05be4ba2352ebae0aecb1\\output\\res;C:\\Users\\Mahmoud-Uni\\.android\\build-cache\\da4f894dda8d89b6eba05be4ba2352ebae0aecb1\\output\\jars\\classes.jar;C:\\Users\\Mahmoud-Uni\\.android\\build-cache\\401e1a1318bba85c94c76b2e5df5861e56a0f848\\output\\res;C:\\Users\\Mahmoud-Uni\\.android\\build-cache\\401e1a1318bba85c94c76b2e5df5861e56a0f848\\output\\jars\\classes.jar;H:\\gradle\\.gradle\\caches\\modules-2\\files-2.1\\org.hamcrest\\hamcrest-core\\1.3\\42a25dc3219429f0e5d060061f71acb49bf010a0\\hamcrest-core-1.3.jar;C:\\Users\\Mahmoud-Uni\\.android\\build-cache\\c4959a5121e578fdc25486b24e7cbb0989e68100\\output\\jars\\classes.jar;C:\\Users\\Mahmoud-Uni\\.android\\build-cache\\c4959a5121e578fdc25486b24e7cbb0989e68100\\output\\res;E:\\AppData\\local\\sdk\\extras\\android\\m2repository\\com\\android\\support\\support-annotations\\23.4.0\\support-annotations-23.4.0.jar;C:\\Users\\Mahmoud-Uni\\.android\\build-cache\\41e62914f76174941b4eda7f4b595c322f9c9cec\\output\\res;C:\\Users\\Mahmoud-Uni\\.android\\build-cache\\41e62914f76174941b4eda7f4b595c322f9c9cec\\output\\jars\\classes.jar;C:\\Users\\Mahmoud-Uni\\.android\\build-cache\\41e62914f76174941b4eda7f4b595c322f9c9cec\\output\\jars\\libs\\internal_impl-23.4.0.jar;H:\\gradle\\.gradle\\caches\\modules-2\\files-2.1\\junit\\junit\\4.11\\4e031bb61df09069aeb2bffb4019e7a5034a4ee0\\junit-4.11.jar;E:\\gin-rebound\\gin-master\\reboundPC\\build\\generated\\mockable-android-23.jar;" +
            "E:\\gin-rebound\\gin-master\\reboundPC\\build-tools\\android-23\\android.jar;";
    String JAVA="java ";
    String JAVAC_BUILD=App_Directory+"build-tools\\jdk1.7.0_79\\bin\\javac.exe ";
    String APK_SINGER=App_Directory.concat("build-tools\\25.0.0\\lib\\apksigner.jar");
    String MAIN_PATH=App_Directory+"app\\src\\main";
    String KEY_STORE=App_Directory+"mahmoud-rebound-key.keystore";

    private  String APK_LOCATION = BIN.concat("\\"+APP_NAME.concat(".apk"));
    
    private  String Test_Suite = "devSubsetPc.java";
    private  String Source_File_Name = "Spring.java";
    private String headers = "fuel use, MAE, test suite execution time, wasFitter, pValue, number of samples, pc test execution time," +
            "deployment time, in-vivo time, complete evaluation time,  patch, samples,extra info";
    
        
    public String EXPERIMENT_DATE_TIME;
    private static int Default_Reps = 1;
    public Utils utils = new Utils();

    public String deviceId="";
    public String deviceName="";
    public int portId=0;



    protected SourceFile sourceFile;
    static public boolean isPrintGradleOutput = false;
    public Device device;
    public static boolean isDebug=false;



    public StructuralTunningExperiment(SourceFile classSource, Device device) {
        super(classSource);
        sourceFile = classSource;
        this.device = device;
        EXPERIMENT_DATE_TIME = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss").format(System.currentTimeMillis());
        EXPERIMENT_DATE_TIME = EXPERIMENT_DATE_TIME.replace(" ","_");
        setWorkingDirectories();
        reloadOriginalSourceFile();
    }

    private void setWorkingDirectories()
    {
        if(device.isRunInVivo) {
            App_Directory = "E:\\gin-rebound\\gin-master\\"+device.deviceName+"\\reboundPC" + File.separator;
            RESULT_DIRECTORY = "E:\\gin-rebound\\gin-master\\" + device.deviceName + "\\results"+"\\"+ EXPERIMENT_DATE_TIME;
            PHONE_LOGS_DIRECTORY = RESULT_DIRECTORY+"\\phoneLogs";
            PACKAGE_NAME = "com.example.mahmoud.modifiedrebound";
            Java_Source_Directory = App_Directory + "app\\src\\main\\java\\com\\example\\mahmoud\\modifiedrebound" ;
            Test_Source_Directory = App_Directory + "app\\src\\test\\java\\com\\example\\mahmoud\\modifiedrebound";
            MAIN_PATH=App_Directory+"app\\src\\main";
            BIN = App_Directory+"bin";
            GEN = App_Directory+"gen";
            CLASS_PATH = CLASS_PATH.concat(BIN);
            APK_LOCATION = BIN.concat("\\"+APP_NAME.concat(".apk"));


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
            App_Directory = "E:\\gin-rebound\\gin-master\\reboundPC" + File.separator;
            APK_LOCATION = App_Directory.concat("app\\build\\outputs\\apk\\app-debug.apk");
            PACKAGE_NAME = "com.example.mahmoud.modifiedrebound";
            Java_Source_Directory = App_Directory +  "app\\src\\main\\java\\com\\example\\mahmoud\\modifiedrebound" ;
            Test_Source_Directory = App_Directory +  "app\\src\\test\\java\\com\\example\\mahmoud\\modifiedrebound" ;
            CLASS_PATH = CLASS_PATH.concat(BIN.concat(";"));
            RESULT_DIRECTORY = RESULT_DIRECTORY.concat("\\").concat(EXPERIMENT_DATE_TIME);
        }

        JAVAC_BUILD= JAVAC_BUILD.concat("-classpath "+CLASS_PATH+" -sourcepath src -sourcepath "+GEN+" -d "+BIN);
    }

    public StructuralExperimentResults run(Patch patch) {
        if(device.isRunInVivo)
            return run(patch, device.getMinimalSetSize());
        else
            return run(patch, StructuralTunningExperimentRunner.On_PC_RUNS);
    }

    public void deleteEvaluationValues()
    {

            try {
                if(new File(App_Directory,"evaluationValues.txt").exists())
                    FileUtils.forceDelete(new File(App_Directory,"evaluationValues.txt"));
            } catch (Exception e) {
                utils.log(e.getMessage(),isDebug);
                e.printStackTrace();
            }
    }
    public StructuralExperimentResults run(Patch patch, int reps) {

        deleteEvaluationValues();

        try {
            long initialTime = System.currentTimeMillis();
            // Apply the patch
            SourceFile patchedSource = patch.apply();

            // If unable to apply patch, report as invalid

            if (patchedSource == null) {
                utils.log("unable to apply patch, report as invalid",isPrintSteps);
                return new StructuralExperimentResults(null, -1, false, false, "", patch, -1, -2); // patch error
            }

            // Copy patched sourceFile and test source to temp directory
            copySource(patchedSource);

            // Compile the patched sourceFile and test classes
            utils.log("Compile the patched sourceFile",isPrintSteps);
            boolean compiledOK = compileUsingAndroidSDK();//runCompilationThread(MAIN_DIRECTORY, 1, false);
            long compilationTime = System.currentTimeMillis() - initialTime;
            utils.log("compilation took(ms): "+compilationTime, isPrintSteps);
            // If failed to compile, return with partial result
            if (!compiledOK) {
                utils.log("failed to compile, return with partial result",isPrintSteps);
                //reloadOriginalSourceFile();
                return new StructuralExperimentResults(null, -1, false, true, patchedSource.getSource(), patch, -1, -3); // Main compilation error
            }

            /////////////run test on PC//////////
            utils.log("compile the tests on the PC",isPrintSteps);
            boolean compileTestPcOK = compileTestOnPc();
            if(!compileTestPcOK)
            {
                utils.log("test on PC didn't compile",isPrintSteps);
                return new StructuralExperimentResults(null, -1, false, true, patchedSource.getSource(), patch, -1, -4);// Test compilation error
            }

            utils.log("run the tests on the PC",isPrintSteps);
            long PcTestTime = System.currentTimeMillis();
            boolean pcTestOK = runTestOnPc(1);
            if(!device.isRunInVivo)
                if(pcTestOK && reps > 1)
                    runTestOnPc(reps-1);

            PcTestTime = System.currentTimeMillis() - PcTestTime;
            utils.log("test on PC took(ms): "+PcTestTime, isPrintSteps);

            if(!pcTestOK)
            {
                utils.log("test on PC wasn't successful",isPrintSteps);
                return new StructuralExperimentResults(null, -1, false, true, patchedSource.getSource(), patch, -1, -5); // Pc Test run error
            }

            // read the results
            StructuralExperimentResults result = readResults(App_Directory);// MAIN_DIRECTORY+File.separator+"app"
            result.patchedProgram = patchedSource.getSource();
            result.patch = patch;
            result.pcTestExecutionTime = (int)PcTestTime;

            if(result.MAE == -1) // test is not successful!
                return result;

            // check the constraint
            utils.log("check the constraint on the MAE (accuracy): "+result.MAE,isPrintSteps);
            if(result.MAE > StructuralTunningExperimentRunner.MAE_CONSTRAINT) {
                utils.log("MAE is greater than "+ StructuralTunningExperimentRunner.MAE_CONSTRAINT
                        +", invalid solution!",isPrintSteps);
                //result.testExecutionTime = Double.MAX_VALUE;
                //result.fuelUse = Double.MAX_VALUE;
            }
            else
                utils.log("check the constraint on the MAE (accuracy) ... no violation",isPrintSteps);


            if(device.isRunInVivo)
            {
                //check the mae if it's less than StructuralTunningExperimentRunner.MAE_CONSTRAINT deploy otherwise return the current fitness evaluation which is fuel_use = Double.MAX
                if(result.MAE < StructuralTunningExperimentRunner.MAE_CONSTRAINT)
                {
                    // build the apk file
                    if(!createApkUsingAndroidSDK(BIN,APP_NAME.concat(".apk")))
                    {
                        utils.log("couldn't build apk file!!! ",isPrintSteps);
                        return result;
                    }
                    // prepare the phone
                    long operationTime= System.currentTimeMillis();
                    device.prepare();
                    operationTime = System.currentTimeMillis() - operationTime;
                    utils.log("preparation time (ms): "+operationTime, isPrintSteps);
                    // deploy the app
                    operationTime = System.currentTimeMillis();

                    device.deploy(isPrintGradleOutput,PACKAGE_NAME , APK_LOCATION);
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
                    copyEvaluationValuesFile(PHONE_LOGS_DIRECTORY.concat("\\"+device.directoryNumber),App_Directory);
                    //device.pull("sdcard/Android/data/com.example.mahmoud.batterymonitor/files/evaluationValues.txt",MAIN_DIRECTORY);//


                    result = readResults(App_Directory); // MAIN_DIRECTORY+File.separator+"app"
                    result.patchedProgram = patchedSource.getSource();
                    result.patch = patch;
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
            return new StructuralExperimentResults(null, -1, false, false, "", null, -1, -10);// Exception
        }

    }


    void reloadOriginalSourceFile()
    {

        try {
            FileUtils.deleteQuietly(new File(Java_Source_Directory, Source_File_Name));
            FileUtils.copyFile(new File(App_Directory , Source_File_Name), new File(Java_Source_Directory, Source_File_Name));
        } catch (Exception e) {
            e.printStackTrace();
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
    StructuralExperimentResults readResults(String path)
    {
        try {
            File resultDir = new File(path);
            File evaluationValuesFile = new File(resultDir, "evaluationValues.txt");
            StructuralExperimentResults structuralExperimentResults;

            if(!evaluationValuesFile.exists()) {
                utils.log("evaluationValues.txt wasn't found! "+evaluationValuesFile.getAbsolutePath(), isPrintSteps);
                return new StructuralExperimentResults(null, -1, true, true, "", null, -1, -9); // evaluation values
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

            // structuralExperimentResults.fuel is the median of the fuel of the in-vivo  runs.
            // If it is not in-vivo then structuralExperimentResults.fuel is the median of advance time of the in-vivo runs
            if(device.isRunInVivo)
                structuralExperimentResults = new StructuralExperimentResults(new Result(), inVivoTestTime.getPercentile(50), true, true, "", null, objectiveStats.getPercentile(50), mae);
            else
                structuralExperimentResults = new StructuralExperimentResults(new Result(), objectiveStats.getPercentile(50), true, true, "", null, objectiveStats.getPercentile(50), mae);
            structuralExperimentResults.objectiveStats = objectiveStats;
            return structuralExperimentResults;
        }
        catch (Exception e)
        {
            utils.log(e.getMessage(),isPrintSteps);
            e.printStackTrace();
        }

        return new StructuralExperimentResults(null, -1, true, true, "", null, -1, -10); // Exception
    }
    /**
     * Write the patched source and test class to a temporary directory.
     *
     * @param patchedProgram The original sourceFile with a patch applied, to be written to the temp directory.
     */
    protected void copySource(SourceFile patchedProgram) {

        File packageDir = new File(Java_Source_Directory);
        File usedDir = new File(App_Directory+"used"+File.separator);

        packageDir.mkdirs();
        usedDir.mkdirs();


        // Write patched sourceFile to temp dir
        String programFilename = new File(sourceFile.getFilename()).getName();
        File tmpSourceFile = new File(packageDir, programFilename);
        try {
            FileWriter writer = new FileWriter(tmpSourceFile);
            writer.write(patchedProgram.getSource());
            writer.flush();
            writer.close();
            //FileUtils.moveFileToDirectory(tmpSourceFile,usedDir,true);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Copy test source to tmp directory (NO NEED)


    }


    boolean compileThreadStuck = true; // if it's initialised with false, it will cause a bug.
    // the main thread is faster than the compilation thread, and therefore it will reach the while loop before the compilation thread
    // set compileThreadStuck to false (i.e. before even starting the compilation).

    // TODO: 4/1/2019 The current issues: for some reason after running the thread, the second print of compilation
    // is in progress do not get execute. Using the task manager I found 4 processes for gradle. after killing them all the explore proceeded!
    private boolean runCompilationThread(String path, int reps, boolean doClean)
    {
        final boolean[] result = {false};
        final boolean[] clean = {doClean};
        Runnable runnable = new Runnable() {
            boolean isStuck = false;
            @Override
            public void run() {
                compileThreadStuck = true;
                result[0] = compile();
                /*try {
                    utils.log("starting the build",isPrintSteps);
                    Thread.sleep(5000);
                    utils.log("still building",isPrintSteps);
                    Thread.sleep(5000);
                    utils.log("A problem occurred",isPrintSteps);
                    Thread.sleep(5000);
                } catch (Exception e) {
                    e.printStackTrace();
                }*/
                compileThreadStuck = false;
            }
        };
        Thread thread = new Thread(runnable);
        thread.setName("gradle build task");
        utils.log("running the thread",isPrintSteps);
        thread.start();
        try {
            utils.log("compilation is in progress...",isPrintSteps);
            boolean isSecondChance = false;
            long startTime = System.currentTimeMillis();
            while(compileThreadStuck) {
                Thread.sleep(6000); //
                utils.log("compilation still is in progress...",isPrintSteps);
                if(System.currentTimeMillis()-startTime > 60000)
                {
                    if(!isSecondChance) {
                        utils.log("building got stuck, now terminating the thread",isPrintSteps);
                        thread.interrupt();
                        thread = null;

                        utils.log("gradle build got stuck, terminated after 1 min, now free memory and try again", isPrintSteps);

                        freeMemory();

                        //return false; // try not give another chance!


                        thread = new Thread(runnable);
                        thread.run();
                        utils.log("building is restarted again...",isPrintSteps);
                        isSecondChance = true;
                        startTime = System.currentTimeMillis();

                    }
                    else {
                        utils.log("building is interrupted....",isPrintSteps);
                        thread.interrupt();
                        thread = null;
                        break; // you had a second chance and it didn't work.
                    }
                }
            }
            if(compileThreadStuck)
            {
                utils.log("gradle build got stuck again, terminated after 2 mins, ignore this patch", isPrintSteps);
                thread=null;
                freeMemory();
                return false;
            }
            freeMemory();
            thread.interrupt();
            thread=null;
        } catch (Exception e) {
            utils.log("exception! "+e.getMessage(), isPrintSteps);
            e.printStackTrace();
        }
        return result[0];
    }

    void freeMemory(){
        System.runFinalization();
        Runtime.getRuntime().gc();
        System.gc();
    }

    public  boolean compileUsingAndroidSDK(){
        try {
            String compilationResults = App_Directory + "\\compilationResults.txt";

            // -------- maybe I can create a temp file here, then redirect the outputs (error stdout) of the process to the file
            // then at the end of compileUsingAndroidSDK I delete the file. This important so we don't get to a deadlock.

            // create the R.java file
            utils.log("create the R.java file", isDebug);

            utils.runCMD(ANDROID_AAPT + " package -f -m -J " + GEN + " -M " + MAIN_PATH + "\\AndroidManifest.xml -S " + MAIN_PATH + "\\res -I " + PLATFORM + " -F " + BIN + "\\resources.apk"
                    , compilationResults, isDebug);


            // compile the main project
            utils.log("compile the main project", isDebug);
            StringBuilder commandResult = new StringBuilder(utils.runCMD(JAVAC_BUILD + " " + Java_Source_Directory + "\\*.java ", compilationResults, isDebug));
            if (utils.checkForFailure(commandResult)) {
                utils.log("compilation failed!\n" + commandResult.toString(), isDebug);
                return false;
            }
            utils.log("compilation successful!", isDebug);

            FileUtils.forceDelete(new File(compilationResults));
            return true;
        }
        catch (Exception e)
        {
            utils.log(e.getMessage(), isPrintSteps);
            e.printStackTrace();
        }

        return false;


    }

    public  boolean compileTestOnPc() //
    {
        try {


            utils.log("runTestOnPc started", isDebug);

            String compilationResults = App_Directory + "\\compilationResults.txt";
            utils.log("compile the test", isDebug);
            StringBuilder commandResult = new StringBuilder(utils.runCMD(JAVAC_BUILD + " " + Test_Source_Directory + "\\*.java", compilationResults, isDebug));
            if (utils.checkForFailure(commandResult)) {
                utils.log("compiling test on PC failed!!!\n" + commandResult.toString(), isDebug);
                return false;
            }
            utils.log("compilation successful!", isDebug);
            return true;
        }
        catch (Exception e)
        {
            utils.log(e.getMessage(), isPrintSteps);
            e.printStackTrace();
            return false;
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
            String testResults = App_Directory + "\\testResults.txt";

            utils.log("run the test on the pc", isDebug);
            StringBuilder commandResult = new StringBuilder(utils.runCMD("cmd /c cd " + App_Directory + " && " + JAVA + " -classpath \"" + CLASS_PATH + "\" org.junit.runner.JUnitCore " + TEST_SUITE + "  " // > nul
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

    public  boolean createApkUsingAndroidSDK(String outputPath, String apk)
    {

        try {
            utils.log("createApkUsingAndroidSDK started", isPrintSteps);
            if(new File(outputPath.concat("\\".concat(apk))).exists())
                FileUtils.forceDelete(new File(outputPath.concat("\\".concat(apk))));

            if(new File(outputPath + "\\classes.dex").exists())
                FileUtils.forceDelete(new File(outputPath + "\\classes.dex"));

            //String apk = APP_NAME.concat(".apk");
            StringBuilder commandResult = new StringBuilder(utils.runCMD(ANDROID_DX + " --output=" + outputPath + "\\classes.dex " + outputPath, isDebug));
            utils.log("dex converter result: "+commandResult, isDebug);
            if(utils.checkForFailure(commandResult)) return false;

            FileUtils.moveFile(new File(outputPath.concat("\\resources.apk")), new File(outputPath.concat("\\".concat(apk))));
            // add the dex to the apk using aapt.exe
            commandResult = new StringBuilder(utils.runCMD(ANDROID_AAPT + " add -k " + outputPath.concat("\\".concat(apk)) + " " + outputPath.concat("\\classes.dex"), isDebug));

            utils.log("aapt tool result: "+commandResult, isDebug);
            if(utils.checkForFailure(commandResult)) return false;
            // sign the apk file
            commandResult = new StringBuilder(utils.runCMD(JAVA+" -jar "+APK_SINGER + " sign --ks "+KEY_STORE+" --ks-pass pass:123456 " +
                    outputPath.concat("\\".concat(apk)), isDebug));
            utils.log("app signer result: "+commandResult, isDebug);
            if(utils.checkForFailure(commandResult)) return false;
        }
        catch (Exception e)
        {
            utils.log(e.getMessage(), StructuralTunningExperimentRunner.isPrintSteps);
            e.printStackTrace();
            return false;
        }
        isDebug = false;////////////////////////////////////////////////////////
        return true;

    }

    /**
     * Callback for the compilation. Used to silence the compiler.
     */
    private static final class MyDiagnosticListener implements DiagnosticListener {
        @Override
        public void report(Diagnostic diagnostic) {
            //to have more control over formatting etc, use indivual methods of
            //diagnostic instead
            System.out.println(diagnostic);
        }
    }

    /**
     * Helper function to clean a directory.
     * @param f
     */
    protected void ensureDirectory(File f) {
        FileUtils.deleteQuietly(f);
        f.mkdirs();
    }
    // this to be changed according to my requirements
    //public class StructuralExperimentResults {
    public class StructuralExperimentResults extends TestResult {
        public double fuelUse = -1;
        public double MAE = -1; // second objective or a constraint
        public double testExecutionTime = -1;
        //public double advanceExecutionTime = -1;


        String patchedProgram = "";
        Patch patch ; // I'm saving the patch here to make printing easier
        Result junitResult = null;

        public DescriptiveStatistics objectiveStats = new DescriptiveStatistics();
        public boolean wasFitter=false;
        public double pValue=-1;

        public int pcTestExecutionTime = 0;
        public int deploymentTime=0;
        public int inVivoEvaluationTime=0;
        public int completeEvaluationTime = 0;

        boolean compiled = false;
        boolean patchSuccess = false;
        String extraInfo="";



        public StructuralExperimentResults(Result result, double testExecutionTime, boolean compiled, boolean patchedOK,
                                           String patchedProgram, Patch patch, double fuel, double mae) {
            super(result, testExecutionTime, compiled, patchedOK, patchedProgram);
            this.junitResult = result;
            this.testExecutionTime = testExecutionTime;
            this.compiled = compiled;
            this.patchSuccess = patchedOK;
            this.patchedProgram = patchedProgram;
            this.patch = patch;
            this.fuelUse = fuel;
            this.MAE = mae;

            if(!device.isRunInVivo) fuelUse = testExecutionTime;
        }


        public StructuralExperimentResults clone()
        {
            StructuralExperimentResults cloned = new StructuralExperimentResults(this.junitResult,this.testExecutionTime,this.compiled,this.patchSuccess,
                    this.patchedProgram,this.patch,this.fuelUse,this.MAE);

            cloned.wasFitter = this.wasFitter;
            cloned.pValue = this.pValue;
            //cloned.advanceExecutionTime = this.advanceExecutionTime;


            cloned.pcTestExecutionTime = this.pcTestExecutionTime;
            cloned.completeEvaluationTime = this.completeEvaluationTime;




            for(double d : this.objectiveStats.getValues())
                cloned.objectiveStats.addValue(d);
            return cloned;
        }
        public String toString() {
            boolean junitOK = false;
            if (this.junitResult != null) {
                junitOK = this.junitResult.wasSuccessful();
            }
            return String.format("Patch Valid: %b; Compiled: %b; fuel use: %f; Time: %f; MAE: %f; Passed: %b", this.patchSuccess,
                    this.compiled,this.fuelUse, this.testExecutionTime, this.MAE, junitOK);
        }

        public String prepareForPrinting()
        {
            String pathString = "null-exception";
            if(patch != null)
                pathString = patch.toString();

            return String.format("%f,%s,%f,%b,%f,%d,%d,%d,%d,%d,%s,%s,%s",this.fuelUse,String.valueOf(this.MAE),this.testExecutionTime,wasFitter,pValue,
                    objectiveStats.getValues().length,pcTestExecutionTime,deploymentTime,inVivoEvaluationTime,completeEvaluationTime,
                    pathString,
                    Arrays.toString(objectiveStats.getValues()).replace("[","").replace("]",""),extraInfo);
        }
    }

    public void saveHistoricalBestSolutions(SourceFile patchedProgram, int step) {

        File packageDir = new File(Java_Source_Directory);
        File usedDir = new File(RESULT_DIRECTORY.concat("\\").concat("used").concat("\\"));

        packageDir.mkdirs();
        usedDir.mkdirs();


        // Write patched sourceFile to temp dir
        String programFilename = new File(sourceFile.getFilename()).getName();
        File tmpSourceFile = new File(usedDir, programFilename.concat(step+""));
        try {
            FileWriter writer = new FileWriter(tmpSourceFile);
            writer.write(patchedProgram.getSource());
            writer.flush();
            writer.close();
            //FileUtils.moveFileToDirectory(tmpSourceFile,usedDir,true);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Copy test source to tmp directory (NO NEED)
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
            if(!file.exists())
                allData=headers+"\n"+data.toString();
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

                CommandLineClass commandLineClass = new CommandLineClass(deviceId,portId);
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
}