package gin.Optimiser;

/**
 * Created by Mahmoud-Uni on 3/24/2019.
 * This class is based on GIN original LocalSearch class.
 * It's equivalent to Device class in my Optimisers code
 */

import com.numericalmethod.suanshu.stats.test.rank.wilcoxon.WilcoxonRankSum;
import Mahmoud.Device;
import Mahmoud.Experiment;
import Mahmoud.Utils;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;
import java.util.*;


import java.util.ArrayList;
import java.util.List;
import java.util.Random;


/**
 * Simple local explore.
 */
public class StructuralTunningExperimentRunner {

    private static final int seed = 5678;
    private static final int NUM_STEPS = 10000;

    static double SECOND_TERMINATION_CONDITION_DAY = 1; // * 24 * 60 *60 *1000
    static String SEARCH_APPROACH = ""; // either explore or exploit


    public static final int On_PC_RUNS = 30;
    public static final int MAE_CONSTRAINT = 10;
    private static final int BATTERY_LIMIT = 20;

    protected SourceFile sourceFile;
    protected StructuralTunningExperiment testRunner;
    protected Random rng;



    ArrayList<StructuralTunningExperiment.StructuralExperimentResults> allSolutions = new ArrayList<>();
    ArrayList<StructuralTunningExperiment.StructuralExperimentResults> tournament = new ArrayList<>();
    ArrayList<StructuralTunningExperiment.StructuralExperimentResults> bestSolutions = new ArrayList<>();


    public static int INITIAL_SAMPLE_SIZE=0;
    int RESAMPLING_TYPE = 0;

    String msg = "";
    
    String FILE_NAME_SUFFIX = "";

    ArrayList<String> goodPatchs = new ArrayList<>();
    ArrayList<String> patchStats = new ArrayList<>();
    public static boolean isPrintSteps = true;
    public static boolean isDebug=true;

    static Utils utils = new Utils();
    static Queue<Device> devices = new LinkedList<>();


    public static double getProcessCpuLoad() {

        try {


            MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
            ObjectName name = ObjectName.getInstance("java.lang:type=OperatingSystem");
            AttributeList list = mbs.getAttributes(name, new String[]{"ProcessCpuLoad"});

            if (list.isEmpty()) return Double.NaN;

            Attribute att = (Attribute) list.get(0);
            Double value = (Double) att.getValue();

            // usually takes a couple of seconds before we get real values
            if (value == -1.0) return Double.NaN;
            // returns a percentage value with 1 decimal point precision
            return ((int) (value * 1000) / 10.0);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return -100;
    }

    /**
     * Main method. Take a source code filename, instantiate a explore instance and execute the explore.
     * @param args A single source code filename, .java
     */
    public void run(String[] args) {

        /*long start = System.currentTimeMillis();
        for(int i=0; i<30; i++) {
            CommandLineClass commandLineClass = new CommandLineClass("ZX1G425WCX");
            System.out.println(commandLineClass.runAdbCommand("uninstall com.example.mahmoud.modifiedrebound"));
            System.out.println(commandLineClass.runAdbCommand("-d install gin-master\\reboundPC\\app-debug.apk"));
            System.out.println(commandLineClass.runCommandAsSuper("pm grant com.example.mahmoud.modifiedrebound android.permission.WRITE_EXTERNAL_STORAGE"));
            System.out.println(commandLineClass.runCommandAsSuper("pm grant com.example.mahmoud.modifiedrebound android.permission.READ_EXTERNAL_STORAGE"));
            System.out.println("--------------------------------------------------");
            System.out.println("           iteration number: "+(i+1));
            System.out.println("--------------------------------------------------");
        }
        long end = System.currentTimeMillis();
        System.out.println("100 uninstalls, installs, granting permissions took in ms "+(end-start));
        System.out.println("100 uninstalls, installs, granting permissions took in s "+((end-start)/1000));*/
        /*CommandLineClass commandLineClass = new CommandLineClass("ZX1G425WCX");
        commandLineClass.setRecharging(false);
        if(true) return;*/

        /*System.out.println(getProcessCpuLoad());
        for(int i=0 ;i<1000000; i++)
            System.out.println(getProcessCpuLoad());

        if(true) return;*/


        /*devices.add(new Device("nexus6-1",1, true));
        devices.add(new Device("nexus6-2",2, true));
        devices.add(new Device("nexus6-4",4, true));
        devices.add(new Device("nexus6-5",5, true));*/
        //devices.add(new Device("nexus6-2",2, true));



        utils.log("Working Directory = " +
                System.getProperty("user.dir"), isPrintSteps);
        String sourceFilename;

        String deviceName="";
        String deviceId="";
        Device device = new Device();
        boolean isInvivo=false;
        int portId=0;
        float testLimit = 1f;
        if(args == null || args.length == 0)
        {
            utils.log("Please specify a source file to optimise.", isPrintSteps);
            //args = new String[1];

            deviceName = "nexus6-4";//"nexus6-2";
            portId = 4;
            isInvivo = true;
            sourceFilename="gin-master\\reboundPC\\app\\src\\main\\java\\com\\example\\mahmoud\\modifiedrebound\\Spring.java";

            device = new Device(deviceName,portId,isInvivo,testLimit, Experiment.NOISE_HANDLING_TABLE);
            SEARCH_APPROACH = "explore";
            SECOND_TERMINATION_CONDITION_DAY = SECOND_TERMINATION_CONDITION_DAY * 24 * 60 * 60 * 1000;
        }

                    //args[0]="gin-rebound/classes/examples/rebound/Spring.java";

                    //args[0]="gin-master/examples/Triangle.java";
        else {

            if(args[0].contains("gin-rebound")) {

                deviceName = args[1];
                portId = Integer.parseInt(args[2]);
                isInvivo = Boolean.parseBoolean(args[3]);
                testLimit = Float.parseFloat(args[4]);
                if (args[5].contains("explore") || args[5].equals("1"))
                    SEARCH_APPROACH = "explore";
                else
                    SEARCH_APPROACH = "exploit";

                if (args[6] != null)
                    SECOND_TERMINATION_CONDITION_DAY = Double.parseDouble(args[6]) * 24 * 60 * 60 * 1000;

                sourceFilename = "gin-master\\reboundPC\\app\\src\\main\\java\\com\\example\\mahmoud\\modifiedrebound\\Spring.java";

                device = new Device(deviceName, portId, isInvivo, testLimit, Experiment.NOISE_HANDLING_TABLE);

                utils.log("Optimising source file: " + sourceFilename + "\n", isPrintSteps);

                //StructuralTunningExperimentRunner localSearch = new StructuralTunningExperimentRunner(sourceFilename, device);
                this.sourceFile = new SourceFile(sourceFilename);  // just parses the code and counts statements etc.
                this.testRunner = new StructuralTunningExperiment(this.sourceFile, device); // Utility class for running junits
                this.rng = new Random(); // use seed if we want same results each time
                if(SEARCH_APPROACH.equals("explore"))
                    //localSearch.explore();
                    explore();
                else
                    //localSearch.exploit();
                exploit();

            }

        }
    }
    public StructuralTunningExperimentRunner() {
        {

        }
    }

    /**
     * Constructor: Create a sourceFile and a testRunner object based on the input filename.
     *              Initialise the RNG.
     * @param sourceFilename
     */
    public StructuralTunningExperimentRunner(String sourceFilename, Device device) {

        //this.testRunner.reloadOriginalSourceFile();
        this.sourceFile = new SourceFile(sourceFilename);  // just parses the code and counts statements etc.
        this.testRunner = new StructuralTunningExperiment(this.sourceFile, device); // Utility class for running junits
        this.rng = new Random(); // use seed if we want same results each time

    }

    /**
     * Actual LocalSearch.
     * @return
     */
    public Patch explore() {

        if(testRunner.device.isRunInVivo)
            testRunner.rechargeTill(100);


            // start with the empty patch
        Patch bestPatch = new Patch(sourceFile);
        this.testRunner.reloadOriginalSourceFile();
        //double bestTime = testRunner.test(bestPatch, On_PC_RUNS).testExecutionTime;
        double bestFuelUse = Double.MAX_VALUE;//testRunner.compileUsingAndroidSDK(bestPatch, On_PC_RUNS).fuelUse;
        StructuralTunningExperiment.StructuralExperimentResults currentBestResult;
        double origFuelUse=0;
        int bestStep = 0;

        utils.log("explore is started \nIt will run for (days): " + (SECOND_TERMINATION_CONDITION_DAY / 24 / 60 / 60 / 1000), isPrintSteps);


        /*testRunner.device.remove("sdcard/Android/data/com.example.mahmoud.batterymonitor/files");
        if(true) return null;*/



        long start = System.currentTimeMillis();
        //long secondTerminationCondition = 24 * 60 * 60 * 1000;
        //for (int step = 1; step <= NUM_STEPS; step++) {
        int step = 0;
        while(System.currentTimeMillis() < (start+ (long) SECOND_TERMINATION_CONDITION_DAY) )
        {
            step++;
            //this.testRunner.reloadOriginalSourceFile();
            if(testRunner.device.isRunInVivo) {
                utils.log("checking the battery level... " + testRunner.device.currentBatteryLevel, isPrintSteps);
                utils.log("tournament size will be: "+testRunner.device.getMinimalSetSize(),isPrintSteps);
                if (testRunner.device.currentBatteryLevel < BATTERY_LIMIT) break;

            }
            utils.log("Step " + step + " ", isPrintSteps);


            // first step: send evaluate the original on every device, create a new solution for each device and evaluate it.


            // the original and the current best must always valid solutions.
            if(step == 1)
                utils.log("evaluating the original variant ...", isPrintSteps);
            else
                utils.log("evaluating the current best...", isPrintSteps);
            currentBestResult = testRunner.run(bestPatch);//testRunner.compileUsingAndroidSDK(bestPatch).fuelUse;// best patch should always successful (applied, compiled and tested)
            if(step == 1)
                 origFuelUse = currentBestResult.fuelUse;

            utils.log("evaluating a valid neighbour...", isPrintSteps);
            // a neighbour must be a valid solution.

            StructuralTunningExperiment.StructuralExperimentResults neighbourTestResult = evaluateValidNeighbour(bestPatch);

            utils.log("current best fitness: " + currentBestResult.fuelUse,isPrintSteps);
            utils.log("current best accuracy (MAE): " + currentBestResult.MAE,isPrintSteps);

            utils.log("new variant fitness: " + neighbourTestResult.fuelUse,isPrintSteps);
            utils.log("new variant accuracy (MAE): " + neighbourTestResult.MAE,isPrintSteps);
            // both are valid.

            computeFitness(currentBestResult,neighbourTestResult,0.05d);
            allSolutions.add(currentBestResult.clone());

            tournament.add(currentBestResult.clone());
            tournament.add(neighbourTestResult.clone());


            //if (neighbour <= currentBest) {
            if (!currentBestResult.wasFitter) { // this is an eclectic approach
            //if (neighbourTestResult.wasFitter) { // this is a conservative approach
                currentBestResult = neighbourTestResult.clone();
                bestPatch = currentBestResult.patch;
                bestFuelUse = currentBestResult.fuelUse;
                bestStep = step;
                utils.log("*** New best *** fitness: " + bestFuelUse + "(nAh)"+", MAE: "+neighbourTestResult.MAE, isPrintSteps);
                utils.log("*** New best *** patch: "+bestPatch.toString(), isPrintSteps);
                testRunner.saveHistoricalBestSolutions(bestPatch.apply(),step); // modify save
                bestSolutions.add(neighbourTestResult);
            } else {
                utils.log("fitness: " + neighbourTestResult.fuelUse+", MAE: "+neighbourTestResult.MAE, isPrintSteps);
            }

            // use the tournament list as sometimes neighbour = currentBest
            testRunner.saveResults(new StringBuilder(tournament.get(0).prepareForPrinting()+"\n"+
                    tournament.get(1).prepareForPrinting()+"\n"),true,"tournaments-"+testRunner.EXPERIMENT_DATE_TIME+".csv");
            tournament.clear();
            testRunner.saveResults(new StringBuilder(currentBestResult.prepareForPrinting()+"\n"),true,"bestResults-"+testRunner.EXPERIMENT_DATE_TIME+".csv");


//            if(step%2 == 0)
//            {
            StringBuilder temp = new StringBuilder();
            for(StructuralTunningExperiment.StructuralExperimentResults structuralExperimentResults : allSolutions)
                temp.append(structuralExperimentResults.prepareForPrinting()+"\n");
            allSolutions.clear();
            testRunner.saveResults(temp,true,"allSolutions-"+testRunner.EXPERIMENT_DATE_TIME+".csv");
            utils.saveLog(testRunner.RESULT_DIRECTORY.concat("\\").concat("detailedLog-").concat(testRunner.EXPERIMENT_DATE_TIME).concat(".txt"),true);
            utils.resetLogger();
//            }
            System.out.println("=====================================================================================");

        } // end of for loop

        testRunner.device.enableRecharging(true);

        RemoveIneffectiveEdit(bestPatch, bestFuelUse);

        utils.log("\nBest patch found: " + bestPatch, isPrintSteps);
        utils.log("Found at step: " + bestStep, isPrintSteps);
        utils.log("Best fuel use: " + bestFuelUse + " (nAh) ", isPrintSteps);
        utils.log("Speedup (%): " + (origFuelUse - bestFuelUse)/origFuelUse, isPrintSteps);
        bestPatch.writePatchedSourceToFile(sourceFile.getFilename() + ".optimised");
        //testRunner.saveResults(new StringBuilder(currentBestResult.prepareForPrinting()+"\n"),true,"bestResults"+testRunner.EXPERIMENT_DATE_TIME+".csv");

        this.testRunner.reloadOriginalSourceFile();

        return bestPatch;
    }

    public Patch exploit() {

        if(testRunner.device.isRunInVivo)
            testRunner.rechargeTill(100);

        // start with the empty patch
        Patch bestPatch = new Patch(sourceFile);
        this.testRunner.reloadOriginalSourceFile();
        //double bestTime = testRunner.test(bestPatch, On_PC_RUNS).testExecutionTime;
        double bestFuelUse = Double.MAX_VALUE;//testRunner.compileUsingAndroidSDK(bestPatch, On_PC_RUNS).fuelUse;
        StructuralTunningExperiment.StructuralExperimentResults currentBestResult;
        double origFuelUse=0;
        int bestStep = 0;

        utils.log("exploit is started \nIt will run for (days): "+(SECOND_TERMINATION_CONDITION_DAY/24/60/60/1000), isPrintSteps);

        /*testRunner.device.remove("sdcard/Android/data/com.example.mahmoud.batterymonitor/files");
        if(true) return null;*/

        long start = System.currentTimeMillis();

        //for (int step = 1; step <= NUM_STEPS; step++) {
        int step = 0;
        while(System.currentTimeMillis() < (start + (long) SECOND_TERMINATION_CONDITION_DAY) )
        {
            step++;
            //this.testRunner.reloadOriginalSourceFile();
            if(testRunner.device.isRunInVivo) {
                utils.log("checking the battery level... " + testRunner.device.currentBatteryLevel, isPrintSteps);
                utils.log("tournament size will be: "+testRunner.device.getMinimalSetSize(),isPrintSteps);
                if (testRunner.device.currentBatteryLevel < BATTERY_LIMIT) break;

            }
            utils.log("Step " + step + " ", isPrintSteps);


            // first step: send evaluate the original on every device, create a new solution for each device and evaluate it.


            // the original and the current best must always valid solutions.
            if(step == 1)
                utils.log("evaluating the original variant ...", isPrintSteps);
            else
                utils.log("evaluating the current best...", isPrintSteps);
            currentBestResult = testRunner.run(bestPatch);//testRunner.compileUsingAndroidSDK(bestPatch).fuelUse;// best patch should always successful (applied, compiled and tested)
            if(step == 1)
                origFuelUse = currentBestResult.fuelUse;

            utils.log("evaluating a valid neighbour...", isPrintSteps);
            // a neighbour must be a valid solution.

            StructuralTunningExperiment.StructuralExperimentResults neighbourTestResult = evaluateValidNeighbour(bestPatch);

            utils.log("current best fitness: " + currentBestResult.fuelUse,isPrintSteps);
            utils.log("current best accuracy (MAE): " + currentBestResult.MAE,isPrintSteps);

            utils.log("new variant fitness: " + neighbourTestResult.fuelUse,isPrintSteps);
            utils.log("new variant accuracy (MAE): " + neighbourTestResult.MAE,isPrintSteps);
            // both are valid.

            computeFitness(currentBestResult,neighbourTestResult,0.05d);
            allSolutions.add(currentBestResult.clone());

            tournament.add(currentBestResult.clone());
            tournament.add(neighbourTestResult.clone());


            //if (neighbour <= currentBest) {
            //if (!currentBestResult.wasFitter) { // this is an eclectic approach
            if (neighbourTestResult.wasFitter) { // this is a conservative approach
                currentBestResult = neighbourTestResult.clone();
                bestPatch = currentBestResult.patch;
                bestFuelUse = currentBestResult.fuelUse;
                bestStep = step;
                utils.log("*** New best *** fitness: " + bestFuelUse + "(nAh)"+", MAE: "+neighbourTestResult.MAE, isPrintSteps);
                utils.log("*** New best *** patch: "+bestPatch.toString(), isPrintSteps);
                testRunner.saveHistoricalBestSolutions(bestPatch.apply(),step); // modify save
                bestSolutions.add(neighbourTestResult);
            } else {
                utils.log("fitness: " + neighbourTestResult.fuelUse+", MAE: "+neighbourTestResult.MAE, isPrintSteps);
            }

            // use the tournament list as sometimes neighbour = currentBest
            testRunner.saveResults(new StringBuilder(tournament.get(0).prepareForPrinting()+"\n"+
                    tournament.get(1).prepareForPrinting()+"\n"),true,"tournaments-"+testRunner.EXPERIMENT_DATE_TIME+".csv");
            tournament.clear();
            testRunner.saveResults(new StringBuilder(currentBestResult.prepareForPrinting()+"\n"),true,"bestResults-"+testRunner.EXPERIMENT_DATE_TIME+".csv");


//            if(step%2 == 0)
//            {
            StringBuilder temp = new StringBuilder();
            for(StructuralTunningExperiment.StructuralExperimentResults structuralExperimentResults : allSolutions)
                temp.append(structuralExperimentResults.prepareForPrinting()+"\n");
            allSolutions.clear();
            testRunner.saveResults(temp,true,"allSolutions-"+testRunner.EXPERIMENT_DATE_TIME+".csv");
            utils.saveLog(testRunner.RESULT_DIRECTORY.concat("\\").concat("detailedLog-").concat(testRunner.EXPERIMENT_DATE_TIME).concat(".txt"),true);
            utils.resetLogger();
//            }
            System.out.println("=====================================================================================");

        } // end of for loop

        testRunner.device.enableRecharging(true);

        RemoveIneffectiveEdit(bestPatch, bestFuelUse);

        utils.log("\nBest patch found: " + bestPatch, isPrintSteps);
        utils.log("Found at step: " + bestStep, isPrintSteps);
        utils.log("Best fuel use: " + bestFuelUse + " (nAh) ", isPrintSteps);
        utils.log("Speedup (%): " + (origFuelUse - bestFuelUse)/origFuelUse, isPrintSteps);
        bestPatch.writePatchedSourceToFile(sourceFile.getFilename() + ".optimised");
        //testRunner.saveResults(new StringBuilder(currentBestResult.prepareForPrinting()+"\n"),true,"bestResults"+testRunner.EXPERIMENT_DATE_TIME+".csv");

        this.testRunner.reloadOriginalSourceFile();

        return bestPatch;
    }

    private void seekImmigrant()
    {

    }

    private void computeFitness(StructuralTunningExperiment.StructuralExperimentResults currentBest, StructuralTunningExperiment.StructuralExperimentResults newSolution, double confidenceLevel)
    {
        currentBest.pValue = computePValueLeft(currentBest.objectiveStats.getValues(),
            newSolution.objectiveStats.getValues());

        newSolution.pValue = computePValueLeft(newSolution.objectiveStats.getValues(),
                currentBest.objectiveStats.getValues());


        utils.log("current best < new solution ? p-value: "+currentBest.pValue,isPrintSteps);

        utils.log("new solution < current best ? p-value: "+newSolution.pValue,isPrintSteps);
        if(currentBest.pValue < confidenceLevel) {
            // I'm using < to explore for novelty (exploration),
            // otherwise <= is for exploitation (more conservative). The former is Markus suggestion on 30 of April 2019.
            // He's decision is not backed-up.
            utils.log("the current best is fitter at confidence level "+ confidenceLevel+
            "\np-value: "+currentBest.pValue,isPrintSteps);
            currentBest.wasFitter=true;
            newSolution.wasFitter=false;
            return;
        }

        if(newSolution.pValue < confidenceLevel) {
            utils.log("the new solution is fitter at confidence level " + confidenceLevel +
                    "\np-value: " + newSolution.pValue, isPrintSteps);
            currentBest.wasFitter=false;
            newSolution.wasFitter=true;
            return;
        }
        else {
            utils.log("data is not enough no tell which one is fitter, take the new solution ", isPrintSteps);
            currentBest.wasFitter=false;
            newSolution.wasFitter=false;
            return;
        }

    }

    static double computePValueLeft(double[]currentBest, double[]newSolution)
    {
        WilcoxonRankSum wilcoxonRankSum = new WilcoxonRankSum(currentBest,newSolution);
        return wilcoxonRankSum.pValue1SidedLess;
    }

    StructuralTunningExperiment.StructuralExperimentResults evaluateValidNeighbour(Patch bestPatch)
    {


        StructuralTunningExperiment.StructuralExperimentResults neighbourTestResult;
        while(true) {
            Patch neighbour = neighbour(bestPatch, rng);
            utils.log(neighbour.toString(), isPrintSteps);
            neighbourTestResult = testRunner.run(neighbour);
            allSolutions.add(neighbourTestResult.clone());
            if (!neighbourTestResult.patchSuccess) {
                utils.log("Patch invalid", isPrintSteps);
                continue;
            }

            if (!neighbourTestResult.compiled) {
                utils.log("Failed to compile", isPrintSteps);
                continue;
            }

            if (neighbourTestResult.MAE > MAE_CONSTRAINT) {
                utils.log("constraint violated", isPrintSteps);
                continue;
            }

            if(neighbourTestResult.junitResult != null) {
                if (!neighbourTestResult.junitResult.wasSuccessful()) {
                    utils.log("Failed to pass all tests", isPrintSteps);
                    continue;
                }
            }
            else
            {
                utils.log("JUnit result is null, results weren't found", isPrintSteps);
                continue;
            }


            break; // if we get to here then it's a valid one
        }
        return neighbourTestResult;
    }

    private StructuralTunningExperiment.StructuralExperimentResults evaluate(Patch patch)
    {
        //testRunner.compileUsingAndroidSDK(patch);
        while (!devices.isEmpty())
        {
            Device device = devices.poll();

            utils.log(device.toString(), isPrintSteps);

            if(device.currentBatteryLevel > 20)
            {
                // deploy


                devices.add(device);
            }
            else
            {
                // do not put the device (device) back
            }

        }
        return null;
    }

    private  void RemoveIneffectiveEdit(Patch bestPatch, double originalBestTime)
    {

        List<Integer> ineffectiveEdit = new ArrayList<Integer>();
        for(int i=0; i< bestPatch.size();i++)
        {
            Patch  clonePath = bestPatch.clone();

            clonePath.remove(i);
            StructuralTunningExperiment.StructuralExperimentResults testResult = testRunner.run(clonePath, On_PC_RUNS);

            if( testResult.compiled && originalBestTime<testResult.fuelUse)
            {
                ineffectiveEdit.add(i);
            }
        }

        for(int i=0;  i< ineffectiveEdit.size();i++)
        {
            bestPatch.remove((ineffectiveEdit.get(i) - i));
        }
    }

    /**
     * Generate a neighbouring patch, by either deleting a randomly chosen edit, or adding a new random edit
     * @param patch Generate a neighbour of this patch.
     * @return A neighbouring patch.
     */
    public Patch neighbour(Patch patch, Random rng) {

        Patch neighbour = patch.clone();

        if (neighbour.size() > 0 && rng.nextFloat() > 0.5) {
            neighbour.remove(rng.nextInt(neighbour.size()));
        } else {
            neighbour.addRandomEdit(rng);
        }

        return neighbour;

    }


    /**
     * To be deleted at the end
     * Actual LocalSearch.
     * @return
     */
    public Patch searchOld() {

        // start with the empty patch
        Patch bestPatch = new Patch(sourceFile);
        this.testRunner.reloadOriginalSourceFile();
        //double bestTime = testRunner.test(bestPatch, On_PC_RUNS).testExecutionTime;
        double bestTime = testRunner.run(bestPatch, On_PC_RUNS).fuelUse;
        double origTime = bestTime;
        int bestStep = 0;

        utils.log("Initial fuel use: " + bestTime + " (nAh) \n", isPrintSteps);
        //if(true) return null;


        for (int step = 1; step <= NUM_STEPS; step++) {
            //this.testRunner.reloadOriginalSourceFile();
            utils.log("Step " + step + " ", isPrintSteps);

            // first step: send evaluate the original on every device, create a new solution for each device and evaluate it.

            Patch neighbour = neighbour(bestPatch, rng);

            utils.log(neighbour.toString(), isPrintSteps);


            StructuralTunningExperiment.StructuralExperimentResults testResult = testRunner.run(neighbour);

            if (!testResult.patchSuccess) {
                utils.log("Patch invalid", isPrintSteps);
                continue;
            }

            if (!testResult.compiled) {
                utils.log("Failed to compile", isPrintSteps);
                continue;
            }

            if (!testResult.junitResult.wasSuccessful()) {
                utils.log("Failed to pass all tests", isPrintSteps);
                continue;
            }

            if( testResult.MAE > 1)
            {
                utils.log("contraint violated", isPrintSteps);
                continue;
            }

            if (testResult.fuelUse < bestTime) {
                bestPatch = neighbour;
                bestTime = testResult.fuelUse;
                bestStep = step;
                utils.log("*** New best *** fuel use: " + bestTime + "(nAh)"+", MAE: "+testResult.MAE, isPrintSteps);
                System.out.println("*** New best *** fuel use: " + bestTime + "(nAh)");
                testRunner.saveHistoricalBestSolutions(bestPatch.apply(),step);
            } else {
                utils.log("Time: " + testResult.fuelUse+", MAE: "+testResult.MAE, isPrintSteps);
            }

            //getBatteryLevel(deviceName, portId);

            if(testRunner.device.currentBatteryLevel <= BATTERY_LIMIT) break;

        }
        RemoveIneffectiveEdit(bestPatch, bestTime);

        utils.log("\nBest patch found: " + bestPatch, isPrintSteps);
        utils.log("Found at step: " + bestStep, isPrintSteps);
        utils.log("Best fuel use: " + bestTime + " (nAh) ", isPrintSteps);
        utils.log("Speedup (%): " + (origTime - bestTime)/origTime, isPrintSteps);
        bestPatch.writePatchedSourceToFile(sourceFile.getFilename() + ".optimised");

        this.testRunner.reloadOriginalSourceFile();
        return bestPatch;

    }
}

