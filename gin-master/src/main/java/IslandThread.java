import DPO.DpoExperiment;
import DPO.DpoExperimentRunner;
import DPO.Solution;
import Mahmoud.Utils;

import static DPO.DpoExperimentRunner.SECOND_TERMINATION_CONDITION_DAY;

/**
 * Created by Mahmoud-Uni on 6/24/2019.
 */
public class IslandThread extends Island {

    String name="";

    double origFuelUse = 0d;
    Utils utils = new Utils();
    int step = 0;
    public boolean isWorking = false;

    public DpoExperiment.DpoExperimentResults currentBestResults;

    public static final int BATTERY_LIMIT = 20;
    public DpoExperimentRunner dpoExperimentRunner = new DpoExperimentRunner();
    Solution bestSolution = new Solution(5);

    public DpoExperiment testRunner;

    public static boolean isPrintSteps = true;

    public IslandThread(String name, String[] args)
    {

        initialise(args);

        bestSolution.setDecisionVariables(testRunner.getSelectedVariables());
    }
    public DpoExperiment.DpoExperimentResults run(Solution solution, int step)
    {
        start();
        this.step = step;
        return currentBestResults;
    }
    void initialise(String[] args)
    {
        dpoExperimentRunner.run(args);
    }
    @Override
    public void run() {
        super.run();

        utils.log("explore is started \nIt will run for (days): " + (SECOND_TERMINATION_CONDITION_DAY / 24 / 60 / 60 / 1000), isPrintSteps);

        long start = System.currentTimeMillis();

        boolean exchangeInProgress = false;

        while(System.currentTimeMillis() < (start+ (long) SECOND_TERMINATION_CONDITION_DAY) )
        {
            step++;

            isWorking=true;
            if(!exchangeInProgress) currentBestResults = dpoExperimentRunner.evolve(bestSolution, step);

            else{
                forignerBest.extraInfo = forignerBest.extraInfo.concat("--from device: "+forignerBest.deviceName);
                currentBestResults = dpoExperimentRunner.runTournament(bestSolution,forignerBest.solution, step);

                saveExchangeResults(currentBestResults,forignerBest);
            }

            bestSolution = currentBestResults.solution;

            if(step == 1)
                origFuelUse = bestSolution.fitness;

            exchangeInProgress = false;
            isWorking = false;

        } // end of for loop

        testRunner.device.enableRecharging(true);

        utils.log("\nBest solution found: " + utils.arrayToString(bestSolution.getDecisionVariables()), isPrintSteps);
        utils.log("improvement (%): " + (origFuelUse - bestSolution.fitness)/origFuelUse, isPrintSteps);

        //testRunner.saveResults(new StringBuilder(currentBestResult.prepareForPrinting()+"\n"),true,"bestResults"+testRunner.EXPERIMENT_DATE_TIME+".csv");

        //this.testRunner.reloadOriginalSourceFile();
        isWorking = false;

    }

    void saveExchangeResults(DpoExperiment.DpoExperimentResults currentBestResults, DpoExperiment.DpoExperimentResults newSolution)
    {
        testRunner.saveResults(new StringBuilder(currentBestResults.prepareForPrinting()+"\n"+
                newSolution.prepareForPrinting()+"\n"),true,"exchangedTournaments-"+testRunner.EXPERIMENT_DATE_TIME+".csv");
        testRunner.saveResults(new StringBuilder(currentBestResults.prepareForPrinting()+"\n"),true,"exchangedBest-"+testRunner.EXPERIMENT_DATE_TIME+".csv");
    }

}
