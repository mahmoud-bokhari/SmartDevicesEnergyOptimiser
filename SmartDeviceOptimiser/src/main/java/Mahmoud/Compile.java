package Mahmoud;

import com.numericalmethod.suanshu.stats.test.rank.wilcoxon.WilcoxonRankSum;

import org.apache.commons.io.FileUtils;

import java.io.File;

/**
 * Created by Mahmoud-Uni on 5/1/2019.
 * The bash script has some issues after integration with the java framework. This class has the same functionality. 
 */
public class Compile {
    static String App_Directory = "gin-master\\reboundPC" + File.separator;
    //static String App_Directory = "E:\\gin-rebound\\gin-master\\reboundPC" + File.separator;
    static String APK_LOCATION = App_Directory.concat("app\\build\\outputs\\apk\\app-debug.apk");
    static String Java_Source_Directory = App_Directory + File.separator + "app\\src\\main\\java\\com\\example\\mahmoud\\modifiedrebound" + File.separator;
    static String Test_Source_Directory = App_Directory + File.separator + "app\\src\\test\\java\\com\\example\\mahmoud\\modifiedrebound" + File.separator;
    static String RESULT_DIRECTORY = "E:\\gin-master\\results";
    static String APP_NAME="Modified_Rebound";
    static String PACKAGE_NAME="com.example.mahmoud.modifiedrebound";
    static String TEST_SUITE = "com.example.mahmoud.modifiedrebound.devSubsetPc";//"com.example.mahmoud.modifiedrebound.SpringTest";
    static String BIN = App_Directory+"bin";
    static String GEN = App_Directory+"gen";
    static String ANDROID_AAPT= App_Directory+"build-tools\\25.0.0\\aapt.exe";
    static String ANDROID_DX = "java -jar "+App_Directory+"build-tools\\25.0.0\\lib\\dx.jar --dex";
    static String PLATFORM=App_Directory+"build-tools\\android-23\\android.jar";
    static String CLASS_PATH="E:\\AppData\\local\\sdk\\platforms\\android-23\\data\\res;C:\\Users\\Mahmoud-Uni\\.android\\build-cache\\69c87f5e3335f932ac272c0ab4dd05ecd6a334b6\\output\\jars\\classes.jar;C:\\Users\\Mahmoud-Uni\\.android\\build-cache\\69c87f5e3335f932ac272c0ab4dd05ecd6a334b6\\output\\res;E:\\gin-rebound\\gin-master\\reboundPC\\app\\libs\\mockito-all-1.9.5.jar;C:\\Users\\Mahmoud-Uni\\.android\\build-cache\\da4f894dda8d89b6eba05be4ba2352ebae0aecb1\\output\\res;C:\\Users\\Mahmoud-Uni\\.android\\build-cache\\da4f894dda8d89b6eba05be4ba2352ebae0aecb1\\output\\jars\\classes.jar;C:\\Users\\Mahmoud-Uni\\.android\\build-cache\\401e1a1318bba85c94c76b2e5df5861e56a0f848\\output\\res;C:\\Users\\Mahmoud-Uni\\.android\\build-cache\\401e1a1318bba85c94c76b2e5df5861e56a0f848\\output\\jars\\classes.jar;H:\\gradle\\.gradle\\caches\\modules-2\\files-2.1\\org.hamcrest\\hamcrest-core\\1.3\\42a25dc3219429f0e5d060061f71acb49bf010a0\\hamcrest-core-1.3.jar;C:\\Users\\Mahmoud-Uni\\.android\\build-cache\\c4959a5121e578fdc25486b24e7cbb0989e68100\\output\\jars\\classes.jar;C:\\Users\\Mahmoud-Uni\\.android\\build-cache\\c4959a5121e578fdc25486b24e7cbb0989e68100\\output\\res;E:\\AppData\\local\\sdk\\extras\\android\\m2repository\\com\\android\\support\\support-annotations\\23.4.0\\support-annotations-23.4.0.jar;C:\\Users\\Mahmoud-Uni\\.android\\build-cache\\41e62914f76174941b4eda7f4b595c322f9c9cec\\output\\res;C:\\Users\\Mahmoud-Uni\\.android\\build-cache\\41e62914f76174941b4eda7f4b595c322f9c9cec\\output\\jars\\classes.jar;C:\\Users\\Mahmoud-Uni\\.android\\build-cache\\41e62914f76174941b4eda7f4b595c322f9c9cec\\output\\jars\\libs\\internal_impl-23.4.0.jar;H:\\gradle\\.gradle\\caches\\modules-2\\files-2.1\\junit\\junit\\4.11\\4e031bb61df09069aeb2bffb4019e7a5034a4ee0\\junit-4.11.jar;E:\\gin-rebound\\gin-master\\reboundPC\\build\\generated\\mockable-android-23.jar;"+BIN;
    static String JAVA="java "; //static String JAVA=APK_LOCATION+"\\build-tools\\jdk1.7.0_79\\bin\\java.exe ";
    static String JAVAC_BUILD=App_Directory+"build-tools\\jdk1.7.0_79\\bin\\javac.exe -classpath "+CLASS_PATH+" -sourcepath src -sourcepath "+GEN+" -d "+BIN;
    static String APK_SINGER=App_Directory.concat("build-tools\\25.0.0\\lib\\apksigner.jar");
    static String MAIN_PATH=App_Directory+"app\\src\\main";
    static String KEY_STORE=App_Directory+"\\mahmoud-rebound-key.keystore";



    static boolean isPrintSteps = true;
    static Utils utils = new Utils();

    public static void main(String[] args) {
        System.out.println(computePValueLeft(new double[]{1,2,3,4,5,6}, new double[]{7,8,9,10,11,12}));

        /*runAndSaveProcessOutputs();
        if(!compileTestOnPc())
            return;
        for(int i=0;i<30;i++)runTestOnPc();
        if(createApkUsingAndroidSDK(BIN,APP_NAME.concat(".apk"))) {
            Device device = new Device("nexus6-2", 2, true);
            device.deploy(isPrintSteps, PACKAGE_NAME, BIN.concat("\\" + APP_NAME.concat(".apk")));
        }*/



    }
    public static String run1(){


        Utils utils = new Utils();
        String shellPath = "cmd.exe";
        //String shellPath = "e:\\Program Files\\Git\\bin\\bash.exe";
        String[] commands = new String[]{,
        };
        // -------- maybe I can create a temp file here, then redirect the outputs (error stdout) of the process to the file
        // then at the end of compileUsingAndroidSDK I delete the file. This important so we don't get to a deadlock.

        // create the R.java file
        utils.log("create the R.java file", isPrintSteps);
        utils.runCMD(ANDROID_AAPT+" package -f -m -J "+GEN+" -M " + MAIN_PATH + "\\AndroidManifest.xml -S "+MAIN_PATH+"\\res -I " + PLATFORM + " -F "+BIN+"\\resources.apk"
                , true);
        // compile the main project
        utils.log("compile the main project", isPrintSteps);
        StringBuilder commandResult = utils.runCMD(JAVAC_BUILD+" "+Java_Source_Directory+"/*.java ", true);
        if(commandResult.toString().contains("error"))
        {
            utils.log("compilation failed!", isPrintSteps);
            return "compilation failed";
        }
        else
        {
            utils.log("compilation successful!", isPrintSteps);
            //return;
        }
        // if the above is successful, then compile the test and compileUsingAndroidSDK it.
        utils.log("compile the test", isPrintSteps);
        commandResult = utils.runCMD(JAVAC_BUILD+" "+Test_Source_Directory+"/*.java", true);
        utils.log("compilation successful!", isPrintSteps);

        utils.log("compileUsingAndroidSDK the test on the pc", isPrintSteps);
        commandResult = utils.runCMD("cmd /c cd "+App_Directory+" && "+JAVA+" -classpath \""+CLASS_PATH+"\" org.junit.runner.JUnitCore "+TEST_SUITE+" > nul ", true);
        // The above steps are for running on the pc
        // The following is for running on the smart-device
        if(!Device.isRunInVivo)
            return commandResult.toString();

        return commandResult.toString();


    }

    /**
     * run compile
     * */
    public static String runAndSaveProcessOutputs(){
        try {
            String compilationResults = App_Directory + "\\compilationResults.txt";

            // -------- maybe I can create a temp file here, then redirect the outputs (error stdout) of the process to the file
            // then at the end of compileUsingAndroidSDK I delete the file. This important so we don't get to a deadlock.

            // create the R.java file
            utils.log("create the R.java file", isPrintSteps);

            utils.runCMD(ANDROID_AAPT + " package -f -m -J " + GEN + " -M " + MAIN_PATH + "\\AndroidManifest.xml -S " + MAIN_PATH + "\\res -I " + PLATFORM + " -F " + BIN + "\\resources.apk"
                    , compilationResults, true);
            // compile the main project
            utils.log("compile the main project", isPrintSteps);
            StringBuilder commandResult = new StringBuilder(utils.runCMD(JAVAC_BUILD + " " + Java_Source_Directory + "\\*.java ", compilationResults, true));
            if (utils.checkForFailure(commandResult)) {
                utils.log("compilation failed!\n" + commandResult.toString(), isPrintSteps);
                return "compilation failed";
            } else {
                utils.log("compilation successful!", isPrintSteps);
                //return;
            }
            FileUtils.forceDelete(new File(compilationResults));
            return commandResult.toString();
        }
        catch (Exception e)
        {
            utils.log(e.getMessage(), isPrintSteps);
            e.printStackTrace();
        }
        finally {
            return "Exception";
        }

    }

    public static boolean compileTestOnPc() // I return the test output as I might need to parse later.
    {
        try {
            utils.log("runTestOnPc started", isPrintSteps);
            String testResults = App_Directory + "\\testResults.txt";
            String compilationResults = App_Directory + "\\compilationResults.txt";
            utils.log("compile the test", isPrintSteps);
            StringBuilder commandResult = new StringBuilder(utils.runCMD(JAVAC_BUILD + " " + Test_Source_Directory + "\\*.java", compilationResults, true));
            if (utils.checkForFailure(commandResult)) {
                utils.log("compiling test on PC failed!!!\n" + commandResult.toString(), isPrintSteps);
                return false;
            }
            utils.log("compilation successful!", isPrintSteps);
            return true;
        }
        catch (Exception e)
        {
            utils.log(e.getMessage(), isPrintSteps);
            e.printStackTrace();
        }
        return false;
    }
    public static boolean runTestOnPc() // I return the test output as I might need to parse later.
    {
        String testResults = App_Directory + "\\testResults.txt";
        try {
            utils.log("run the test on the pc", isPrintSteps);
            StringBuilder commandResult = new StringBuilder(utils.runCMD("cmd /c cd " + App_Directory + " && " + JAVA + " -classpath \"" + CLASS_PATH + "\" org.junit.runner.JUnitCore " + TEST_SUITE + "  " // > nul
                    , testResults, true));
            if (utils.checkForFailure(commandResult)) {
                utils.log("test on PC failed!!!", isPrintSteps);
                return false;
            }
            utils.log("test on PC is successful!!!", isPrintSteps);
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

    public static boolean createApkUsingAndroidSDK(String outputPath, String apk)
    {
        try {
            utils.log("createApkUsingAndroidSDK started", isPrintSteps);
            if(new File(outputPath.concat("\\".concat(apk))).exists())
                FileUtils.forceDelete(new File(outputPath.concat("\\".concat(apk))));

            if(new File(outputPath + "\\classes.dex").exists())
                FileUtils.forceDelete(new File(outputPath + "\\classes.dex"));

            //String apk = APP_NAME.concat(".apk");
            StringBuilder commandResult = new StringBuilder(utils.runCMD(ANDROID_DX + " --output=" + outputPath + "\\classes.dex " + outputPath, true));
            utils.log("dex converter result: "+commandResult, isPrintSteps);
            if(utils.checkForFailure(commandResult)) return false;

            FileUtils.moveFile(new File(outputPath.concat("\\resources.apk")), new File(outputPath.concat("\\".concat(apk))));
            // add the dex to the apk using aapt.exe
            commandResult = new StringBuilder(utils.runCMD(ANDROID_AAPT + " add -k " + outputPath.concat("\\".concat(apk)) + " " + outputPath.concat("\\classes.dex"), true));

            utils.log("aapt tool result: "+commandResult, isPrintSteps);
            if(utils.checkForFailure(commandResult)) return false;
            // sign the apk file
            commandResult = new StringBuilder(utils.runCMD(JAVA+" -jar "+APK_SINGER + " sign --ks "+KEY_STORE+" --ks-pass pass:123456 " +
                    outputPath.concat("\\".concat(apk)), true));
            utils.log("app signer result: "+commandResult, isPrintSteps);
            if(utils.checkForFailure(commandResult)) return false;
        }
        catch (Exception e)
        {
            utils.log(e.getMessage(), isPrintSteps);
            e.printStackTrace();
        }
        finally {
            return false;
        }
    }

    /**
     * The median of currentBest(x) is less than the median of newSolution (y)
     * */
    static double computePValueLeft(double[]currentBest, double[]newSolution)
    {
        WilcoxonRankSum wilcoxonRankSum = new WilcoxonRankSum(currentBest,newSolution);
        return wilcoxonRankSum.pValue1SidedLess;
    }
}
