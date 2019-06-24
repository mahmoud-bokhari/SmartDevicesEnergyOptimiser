package DPO;

/**
 * Created by Mahmoud-Uni on 6/19/2019.

 */

import com.google.common.util.concurrent.Atomics;
import com.numericalmethod.suanshu.stats.test.rank.wilcoxon.WilcoxonRankSum;
import gin.Mahmoud.CommandLineClass;
import gin.Mahmoud.Device;
import gin.Mahmoud.Utils;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;
import java.util.*;


import java.util.ArrayList;
import java.util.Random;


/**
 * Simple local explore.
 */
public class DpoExperimentRunner {

    private static final int seed = 5678;
    private static final int NUM_STEPS = 10000;

    static double SECOND_TERMINATION_CONDITION_DAY = 1; // * 24 * 60 *60 *1000
    static String SEARCH_APPROACH = ""; // either explore or exploit



    public static final int MAE_CONSTRAINT = 10;
    public static final int BATTERY_LIMIT = 20;

    private String OriginalConfigFilePath;
    protected DpoExperiment testRunner;
    protected Random rng;



    ArrayList<DpoExperiment.DpoExperimentResults> allSolutions = new ArrayList<>(); // rename
    ArrayList<DpoExperiment.DpoExperimentResults> tournament = new ArrayList<>();
    ArrayList<DpoExperiment.DpoExperimentResults> bestSolutions = new ArrayList<>(); // rename


    public static int INITIAL_SAMPLE_SIZE=0;
    int RESAMPLING_TYPE = 0;

    String msg = "";

    String FILE_NAME_SUFFIX = "";


    public static boolean isPrintSteps = true;
    public static boolean isDebug=true;

    static Utils utils = new Utils();
    static Device[] devices;



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


        String deviceName="";
        String deviceId="";
        Device device = new Device();
        boolean isInvivo=false;
        int portId=0;

        if(args == null || args.length == 0)
        {
            //args = new String[1];
            deviceName = "nexus6-4";//"nexus6-2";
            portId = 4;
            isInvivo = true;

            device = new Device(deviceName,portId,isInvivo);
            SEARCH_APPROACH = "explore";
            SECOND_TERMINATION_CONDITION_DAY = SECOND_TERMINATION_CONDITION_DAY * 24 * 60 * 60 * 1000;
        }


        else {
            if(args[0].contains("dpo-es-1+1")) {

                deviceName = args[1];
                portId = Integer.parseInt(args[2]);
                isInvivo = Boolean.parseBoolean(args[3]);
                if (args[4].contains("explore") || args[4].equals("1"))
                    SEARCH_APPROACH = "explore";
                else
                    SEARCH_APPROACH = "exploit";

                if (args[5] != null)
                    SECOND_TERMINATION_CONDITION_DAY = Double.parseDouble(args[5]) * 24 * 60 * 60 * 1000;

                device = new Device(deviceName, portId, isInvivo);

                testRunner = new DpoExperiment(device);

                if(SEARCH_APPROACH.equals("explore"))
                    explore();
                else
                    exploit();

            }
        }
    }
    public DpoExperimentRunner() {
    }

    /**
     * Constructor: Create a OriginalConfigFilePath and a testRunner object based on the input filename.
     *              Initialise the RNG.
     * @param sourceFilename
     */
    public DpoExperimentRunner(String sourceFilename, Device device) {

        //this.testRunner.reloadOriginalSourceFile();
        this.OriginalConfigFilePath = sourceFilename;  // just parses the code and counts statements etc.

        this.rng = new Random(); // use seed if we want same results each time

    }

    /**
     * Actual LocalSearch.
     * @return
     */
    public Solution explore() {

        DpoExperiment.DpoExperimentResults currentBestResult;
        Solution bestSolution = new Solution(5);
        bestSolution.setDecisionVariables(testRunner.getSelectedVariables());
        double origFuelUse=0;
        double bestFuelUse=0;

        utils.log("explore is started \nIt will run for (days): " + (SECOND_TERMINATION_CONDITION_DAY / 24 / 60 / 60 / 1000), isPrintSteps);

        long start = System.currentTimeMillis();
        int step = 0;
        int bestStep=0;
        while(System.currentTimeMillis() < (start+ (long) SECOND_TERMINATION_CONDITION_DAY) )
        {
            step++;

            if(testRunner.device.isRunInVivo) {
                utils.log("checking the battery level... " + testRunner.device.currentBatteryLevel, isPrintSteps);
                utils.log("tournament size will be: "+testRunner.device.getMinimalSetSize(),isPrintSteps);
                if (testRunner.device.currentBatteryLevel < BATTERY_LIMIT) break;

                if(testRunner.device.currentBatteryLevel != 100 && testRunner.device.currentBatteryLevel%10 == 0)
                {

                    Device nextDevice = seekImmigration(testRunner.device);

                }
            }
            utils.log("Tournament " + step + " ", isPrintSteps);


            // first step: send evaluate the original on every device, create a new solution for each device and evaluate it.


            // the original and the current best must always valid solutions.
            if(step == 1)
                utils.log("evaluating the original variant ...", isPrintSteps);
            else
                utils.log("evaluating the current best...", isPrintSteps);

            // evaluate the original or the current best.
            currentBestResult = testRunner.run(bestSolution);// best solution should always successful (applied, compiled and tested)

            if(step == 1)
                origFuelUse = currentBestResult.fuelUse;

            utils.log("evaluating a valid neighbour...", isPrintSteps);
            // a neighbour must be a valid solution.


            DpoExperiment.DpoExperimentResults newSolutionTestResult = evaluateValidNeighbour(bestSolution);

            utils.log("current best fitness: " + currentBestResult.fuelUse,isPrintSteps);
            utils.log("current best accuracy (MAE): " + currentBestResult.MAE,isPrintSteps);

            utils.log("new variant fitness: " + newSolutionTestResult.fuelUse,isPrintSteps);
            utils.log("new variant accuracy (MAE): " + newSolutionTestResult.MAE,isPrintSteps);
            // both are valid.

            computeFitness(currentBestResult,newSolutionTestResult,0.05d);
            allSolutions.add(currentBestResult.clone());
            allSolutions.add(newSolutionTestResult.clone());

            tournament.add(currentBestResult.clone());
            tournament.add(newSolutionTestResult.clone());

            //if (neighbour <= currentBest) {
            if (!currentBestResult.wasFitter) { // this is an eclectic approach
                //if (neighbourTestResult.wasFitter) { // this is a conservative approach
                currentBestResult = newSolutionTestResult.clone();
                bestSolution=currentBestResult.solution.clone();

                bestFuelUse = currentBestResult.fuelUse;
                bestStep = step;
                utils.log("*** New best *** fitness: " + bestFuelUse + "(nAh)"+", MAE: "+currentBestResult.MAE, isPrintSteps);
                utils.log("*** New best *** solution: "+bestSolution.toString(), isPrintSteps);
                testRunner.saveHistoricalBestSolutions(step); // modify save
                bestSolutions.add(currentBestResult);
            } else {
                utils.log("fitness: " + newSolutionTestResult.fuelUse+", MAE: "+newSolutionTestResult.MAE, isPrintSteps);
            }

            // use the tournament list as sometimes neighbour = currentBest
            testRunner.saveResults(new StringBuilder(tournament.get(0).prepareForPrinting()+"\n"+
                    tournament.get(1).prepareForPrinting()+"\n"),true,"tournaments-"+testRunner.EXPERIMENT_DATE_TIME+".csv");
            tournament.clear();
            testRunner.saveResults(new StringBuilder(currentBestResult.prepareForPrinting()+"\n"),true,"bestResults-"+testRunner.EXPERIMENT_DATE_TIME+".csv");


//            if(step%2 == 0)
//            {
            StringBuilder temp = new StringBuilder();
            for(DpoExperiment.DpoExperimentResults result : allSolutions)
                temp.append(result.prepareForPrinting()+"\n");
            allSolutions.clear();
            testRunner.saveResults(temp,true,"allSolutions-"+testRunner.EXPERIMENT_DATE_TIME+".csv");
            utils.saveLog(testRunner.RESULT_DIRECTORY.concat("\\").concat("detailedLog-").concat(testRunner.EXPERIMENT_DATE_TIME).concat(".txt"),true);
            utils.resetLogger();
//            }
            System.out.println("=====================================================================================");

        } // end of for loop

        testRunner.device.enableRecharging(true);

        utils.log("\nBest solution found: " + utils.arrayToString(bestSolution.getDecisionVariables()), isPrintSteps);
        utils.log("Found at step: " + bestStep, isPrintSteps);
        utils.log("Best fuel use: " + bestFuelUse + " (nAh) ", isPrintSteps);
        utils.log("Speedup (%): " + (origFuelUse - bestFuelUse)/origFuelUse, isPrintSteps);

        //testRunner.saveResults(new StringBuilder(currentBestResult.prepareForPrinting()+"\n"),true,"bestResults"+testRunner.EXPERIMENT_DATE_TIME+".csv");

        //this.testRunner.reloadOriginalSourceFile();

        return bestSolution;
    }


    public Solution exploit() {
        DpoExperiment.DpoExperimentResults currentBestResult;
        Solution bestSolution = new Solution(5);
        bestSolution.setDecisionVariables(testRunner.getSelectedVariables());
        double origFuelUse=0;
        double bestFuelUse=0;

        if(testRunner.device.isRunInVivo)
            testRunner.rechargeTill(100);


        utils.log("explore is started \nIt will run for (days): " + (SECOND_TERMINATION_CONDITION_DAY / 24 / 60 / 60 / 1000), isPrintSteps);


        long start = System.currentTimeMillis();
        int step = 0;
        int bestStep=0;
        while(System.currentTimeMillis() < (start+ (long) SECOND_TERMINATION_CONDITION_DAY) )
        {
            step++;

            if(testRunner.device.isRunInVivo) {
                utils.log("checking the battery level... " + testRunner.device.currentBatteryLevel, isPrintSteps);
                utils.log("tournament size will be: "+testRunner.device.getMinimalSetSize(),isPrintSteps);
                if (testRunner.device.currentBatteryLevel < BATTERY_LIMIT) break;
            }
            utils.log("Tournament " + step + " ", isPrintSteps);


            // first step: send evaluate the original on every device, create a new solution for each device and evaluate it.


            // the original and the current best must always valid solutions.
            if(step == 1)
                utils.log("evaluating the original variant ...", isPrintSteps);
            else
                utils.log("evaluating the current best...", isPrintSteps);

            // evaluate the original or the current best.
            currentBestResult = testRunner.run(bestSolution);// best solution should always successful (applied, compiled and tested)

            if(step == 1)
                origFuelUse = currentBestResult.fuelUse;

            utils.log("evaluating a valid neighbour...", isPrintSteps);
            // a neighbour must be a valid solution.

            DpoExperiment.DpoExperimentResults newSolutionTestResult = evaluateValidNeighbour(bestSolution);

            utils.log("current best fitness: " + currentBestResult.fuelUse,isPrintSteps);
            utils.log("current best accuracy (MAE): " + currentBestResult.MAE,isPrintSteps);

            utils.log("new variant fitness: " + newSolutionTestResult.fuelUse,isPrintSteps);
            utils.log("new variant accuracy (MAE): " + newSolutionTestResult.MAE,isPrintSteps);
            // both are valid.

            computeFitness(currentBestResult,newSolutionTestResult,0.05d);
            allSolutions.add(currentBestResult.clone());
            allSolutions.add(newSolutionTestResult.clone());

            tournament.add(currentBestResult.clone());
            tournament.add(newSolutionTestResult.clone());


            //if (neighbour <= currentBest) {
            //if (!currentBestResult.wasFitter) { // this is an eclectic approach
            if (newSolutionTestResult.wasFitter) { // this is a conservative approach
                currentBestResult = newSolutionTestResult.clone();

                bestFuelUse = currentBestResult.fuelUse;
                bestStep = step;
                utils.log("*** New best *** fitness: " + bestFuelUse + "(nAh)"+", MAE: "+currentBestResult.MAE, isPrintSteps);
                utils.log("*** New best *** solution: "+bestSolution.toString(), isPrintSteps);
                testRunner.saveHistoricalBestSolutions(step); // modify save
                bestSolutions.add(currentBestResult);
            } else {
                utils.log("fitness: " + newSolutionTestResult.fuelUse+", MAE: "+newSolutionTestResult.MAE, isPrintSteps);
            }

            // use the tournament list as sometimes neighbour = currentBest
            testRunner.saveResults(new StringBuilder(tournament.get(0).prepareForPrinting()+"\n"+
                    tournament.get(1).prepareForPrinting()+"\n"),true,"tournaments-"+testRunner.EXPERIMENT_DATE_TIME+".csv");
            tournament.clear();
            testRunner.saveResults(new StringBuilder(currentBestResult.prepareForPrinting()+"\n"),true,"bestResults-"+testRunner.EXPERIMENT_DATE_TIME+".csv");


//            if(step%2 == 0)
//            {
            StringBuilder temp = new StringBuilder();
            for(DpoExperiment.DpoExperimentResults result : allSolutions)
                temp.append(result.prepareForPrinting()+"\n");
            allSolutions.clear();
            testRunner.saveResults(temp,true,"allSolutions-"+testRunner.EXPERIMENT_DATE_TIME+".csv");
            utils.saveLog(testRunner.RESULT_DIRECTORY.concat("\\").concat("detailedLog-").concat(testRunner.EXPERIMENT_DATE_TIME).concat(".txt"),true);
            utils.resetLogger();
//            }
            System.out.println("=====================================================================================");

        } // end of for loop



        return bestSolution;
    }


    public Device seekImmigration(Device device)
    {
        // get the list of the connected devices.
        // hard code all device and their potential ports as it is really hard to tell which one is connected to which
        // port. Note: there's a method for retrieving the list of connected devices in command class.
        devices= new Device[]{
                //new Device("nexus6-1", 1, true),
                //new Device("moto-g-2", 2, true),
                //new Device("nexus6-3", 3, true),
                new Device("nexus6-4", 4, true),
                //new Device("nexus6-5", 5, true),
                //new Device("nexus9-1",0, true)};
        };

        String[] connectedDevices = CommandLineClass.getListOfDevices().split("\n");

        Device differentOS = new Device();
        Device differentDeviceSameModel = new Device();
        for(int i=1;i<connectedDevices.length;i++)
        {
            String tempID = connectedDevices[i].split("\t")[0];
            if(!device.deviceId.equals(connectedDevices[i]))
            {
                String tempOS = CommandLineClass.runCommandUnsafely("adb -s "+tempID+" shell getprop ro.build.version.release").trim().replaceAll("\n","");
                tempOS = tempOS.contains("6")?"M":"N";
                String deviceArch = CommandLineClass.runCommandUnsafely("adb -s "+tempID+" shell getprop ro.product.model").toLowerCase().replaceAll(" ","-").replaceAll("\n","");
                deviceArch = deviceArch.substring(0,deviceArch.indexOf("-")+2);// inlcude -X, X here is 6, 9 or g
                if(!device.deviceName.contains(deviceArch)) {// different model.
                    utils.log("found different model to "+device.deviceName, isPrintSteps);
                    if (!device.OS.equals(tempOS)) // different OS
                    {
                        utils.log("found different model and different OS "+device.OS, isPrintSteps);
                        return getDevice(tempID);// this will be used for next evaluations.
                    }
                    else
                    {
                        utils.log("found different model but same OS "+device.OS, isPrintSteps);
                        return getDevice(tempID);// this will be used for next evaluations.
                    }
                }
                else // same model
                {
                    if(!device.OS.equals(tempOS))
                    {
                        differentOS = getDevice(tempID);// this will be used for next evaluations.
                    }
                    else
                        differentDeviceSameModel = getDevice(tempID);
                }
            }
        }
        if(!differentOS.deviceName.equals("")) {
            utils.log("found same model BUT different OS " + device.OS, isPrintSteps);
            return differentOS;
        }
        if(!differentDeviceSameModel.deviceName.equals("")){
            utils.log("found same model and same OS but different device " + device.OS, isPrintSteps);
            return differentOS;
        }
        utils.log("couldn't found any other device!" , isPrintSteps);

        return device;
    }

    public Device getDevice(String id)
    {
        utils.log("looking for device: "+id, isPrintSteps);
        for(int i=0; i<devices.length; i++)
        {
            if(id.equals(devices[i].deviceId)) {
                utils.log("device found! "+devices[i].deviceName, isPrintSteps);
                return devices[i];
            }

        }
        utils.log("device wasn't found!! ", isPrintSteps);
        return null;
    }
    public void runMultiple()
    {

        long start = System.currentTimeMillis();
        int step = 0;
        int bestStep=0;
        Solution bestSolution = new Solution(5);
        double bestFuelUse = 0d;
        double origFuelUse = 0d;

        if(testRunner.device.isRunInVivo)
            testRunner.rechargeTill(100);


        while(System.currentTimeMillis() < (start+ (long) SECOND_TERMINATION_CONDITION_DAY) )
        {
            // the evolution loop
        }


        testRunner.device.enableRecharging(true);

        utils.log("\nBest solution found: " + utils.arrayToString(bestSolution.getDecisionVariables()), isPrintSteps);

        utils.log("Found at step: " + bestStep, isPrintSteps);
        utils.log("Best fuel use: " + bestFuelUse + " (nAh) ", isPrintSteps);
        utils.log("Speedup (%): " + (origFuelUse - bestFuelUse)/origFuelUse, isPrintSteps);

        //testRunner.saveResults(new StringBuilder(currentBestResult.prepareForPrinting()+"\n"),true,"bestResults"+testRunner.EXPERIMENT_DATE_TIME+".csv");

        //this.testRunner.reloadOriginalSourceFile();
    }

    private void computeFitness(DpoExperiment.DpoExperimentResults currentBest, DpoExperiment.DpoExperimentResults newSolution, double confidenceLevel)
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

    DpoExperiment.DpoExperimentResults evaluateValidNeighbour(Solution solution)
    {
        DpoExperiment.DpoExperimentResults newSolutionTestResult;
        Mutation mutation  = new Mutation();
        Solution newSolution;
        while(true) {
            newSolution = mutation.mutateUsingUncorrelatedMutation(solution,false,true);

            utils.log(newSolution.toString(), isPrintSteps);
            newSolutionTestResult = testRunner.run(newSolution);
            allSolutions.add(newSolutionTestResult.clone());

            if(newSolutionTestResult.fuelUse < 0 )
            {
                utils.log(newSolutionTestResult.extraInfo, isPrintSteps);
                continue;
            }

            if (newSolutionTestResult.MAE > MAE_CONSTRAINT) {
                utils.log("constraint violated", isPrintSteps);
                continue;
            }
            allSolutions.remove(allSolutions.size()-1);// delete the last solution since it's valid, we will re add it later in run method.
            break; // if we get to here then it's a valid one
        }
        return newSolutionTestResult;
    }
}

