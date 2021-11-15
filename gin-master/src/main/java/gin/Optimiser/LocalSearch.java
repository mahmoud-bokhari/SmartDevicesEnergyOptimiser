package gin.Optimiser;


import Mahmoud.Device;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Simple local explore.
 */
public class LocalSearch {

    private static final int seed = 5678;
    private static final int NUM_STEPS = 1000;
    private static final int WARMUP_REPS = 10;

    protected SourceFile sourceFile;
    protected StructuralTunningExperiment testRunner;
    protected Random rng;

    /**
     * Main method. Take a source code filename, instantiate a explore instance and execute the explore.
     * @param args A single source code filename, .java
     */
    public static void main(String[] args) {

        args = new String[1];
        args[0]="gin-master\\reboundPC\\app\\src\\main\\java\\com\\example\\mahmoud\\modifiedrebound\\Spring.java";
        //args[0]="gin-rebound/classes/examples/rebound/Spring.java";

        //args[0]="gin-master/examples/Triangle.java";
        System.out.println("Working Directory = " +
                System.getProperty("user.dir"));
        if (args.length == 0) {

            System.out.println("Please specify a source file to optimise.");

        } else {

            String sourceFilename = args[0];
            System.out.println("Optimising source file: " + sourceFilename + "\n");

            LocalSearch localSearch = new LocalSearch(sourceFilename);
            localSearch.search();

        }

    }

    /**
     * Constructor: Create a sourceFile and a testRunner object based on the input filename.
     *              Initialise the RNG.
     * @param sourceFilename
     */
    public LocalSearch(String sourceFilename) {


        this.sourceFile = new SourceFile(sourceFilename);  // just parses the code and counts statements etc.

        this.testRunner = new StructuralTunningExperiment(this.sourceFile,new Device()); // Utility class for running junits
        this.testRunner.reloadOriginalSourceFile();
        this.rng = new Random(); // use seed if we want same results each time

    }

    /**
     * Actual LocalSearch.
     * @return
     */
    public Patch search() {

        StructuralTunningExperiment.isPrintGradleOutput = true;
        // start with the empty patch
        Patch bestPatch = new Patch(sourceFile);
        this.testRunner.reloadOriginalSourceFile();
        //double bestTime = testRunner.test(bestPatch, WARMUP_REPS).testExecutionTime;
        double bestTime = testRunner.run(bestPatch, WARMUP_REPS).testExecutionTime;
        double origTime = bestTime;
        int bestStep = 0;
        //if(true) System.exit(1);

        System.out.println("Initial execution time: " + bestTime + " (ns) \n");
        //if(true) return null;

        for (int step = 1; step <= NUM_STEPS; step++) {
            //this.testRunner.reloadOriginalSourceFile();
            System.out.print("Step " + step + " ");

            Patch neighbour = neighbour(bestPatch, rng);

            System.out.print(neighbour);

            //TestRunner.TestResult testResult = testRunner.test(neighbour);
            StructuralTunningExperiment.StructuralExperimentResults testResult = testRunner.run(neighbour);

            if (!testResult.patchSuccess) {
                System.out.println("Patch invalid");
                continue;
            }

            if (!testResult.compiled) {
                System.out.println("Failed to compile");
                continue;
            }

            if (!testResult.junitResult.wasSuccessful()) {
                System.out.println("Failed to pass all tests");
                continue;
            }

            if (testResult.testExecutionTime < bestTime) {
                bestPatch = neighbour;
                bestTime = testResult.testExecutionTime;
                bestStep = step;
                System.out.println("*** New best *** Time: " + bestTime + "(ns)");
                testRunner.saveHistoricalBestSolutions(bestPatch.apply(),step);
            } else {
                System.out.println("Time: " + testResult.testExecutionTime +", MAE: "+testResult.MAE);
            }

        }
        RemoveIneffectiveEdit(bestPatch, bestTime);
		
        System.out.println("\nBest patch found: " + bestPatch);
        System.out.println("Found at step: " + bestStep);
        System.out.println("Best execution time: " + bestTime + " (ns) ");
        System.out.println("Speedup (%): " + (origTime - bestTime)/origTime);
        bestPatch.writePatchedSourceToFile(sourceFile.getFilename() + ".optimised");

        this.testRunner.reloadOriginalSourceFile();
        return bestPatch;

    }

	private  void RemoveIneffectiveEdit(Patch bestPatch, double originalBestTime) 
    {
    	
    	List<Integer> ineffectiveEdit = new ArrayList<Integer>();
    	for(int i=0; i< bestPatch.size();i++)
    	{
    		Patch  clonePath = bestPatch.clone();
    		
    		clonePath.remove(i);
    		TestRunner.TestResult testResult = testRunner.run(clonePath, WARMUP_REPS);
    		
    		if( testResult.compiled && originalBestTime<testResult.executionTime)
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


}
