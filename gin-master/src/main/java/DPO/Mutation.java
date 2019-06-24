package DPO;

import gin.Mahmoud.Utils;

import java.util.Random;

/**
 * Created by Mahmoud-Uni on 6/17/2019.
 */
public class Mutation {
    public static boolean IS_ADAPTIVE=true;
    public static double UPPER_BOUND = 100000d;
    public static double LOWER_BOUND = 0d;
    public int mutationProbability = (int)(1f/Solution.size)*100;
    Utils utils = new Utils();
    public static boolean isPrintSteps=true;


    public Solution mutateUsingUncorrelatedMutation(Solution solution, boolean isSingle, boolean useProbability)
    {
        Solution newSolution = solution.clone();
        int index = 0;
        double sigma = 1;
        boolean wasChanged = false;

        utils.log("now mutating the solution: "+utils.arrayToString(solution.getDecisionVariables())+
                ",isSingle step size: "+isSingle+" useProbability: "+useProbability,isPrintSteps);

        while(!wasChanged) { // while the new solution wasn't changed.
            for (int i = 0; i < newSolution.getDecisionVariables().length; i++) {
                index = getGeneIndexToMutate(useProbability, newSolution.getDecisionVariables().length, i);
                if (index > -1) {
                    sigma = getSigmaUsingLogNormalDistribution(solution, index, isSingle);
                    newSolution.getSigma()[isSingle ? 0 : index] = sigma; // update the new solution's sigma.
                    newSolution = doMutation(newSolution, sigma, index); // do the mutation on the specified index. I send the newSolution as it has the old values and it needs to be updated.
                    wasChanged = true;
                }
                if(!useProbability)
                {
                    wasChanged = true;
                    break; // if we don't use probability, it means we will pick only one gene at random and change it,
                    // therefore we break.

                }
            }
        }
        return newSolution;
    }

    // ------- the following are utils:
    private int getGeneIndexToMutate(boolean useProbability, int solutionSize, int currentIndex)
    {
        utils.log("now determining which gene to mutate",isPrintSteps);
        Random random = new Random();
        int index=0;
        if (useProbability)
        { // why waste iterations (the while loop to check if any change has been applied).
            utils.log("mutation probability: "+mutationProbability,isPrintSteps);

            if (random.nextInt(101) <= mutationProbability)
            {
                // mutate/update sigma using the learning rate t
                return currentIndex;
            }
            else return -1; // don't mutate
        }

        else // pick random index to mutate.
        {
            utils.log("picking a random index ",isPrintSteps);
            return random.nextInt(solutionSize);
        }
    }

    private double getSigmaUsingLogNormalDistribution(Solution solution, int index, boolean isSingle)
    {
        Random random = new Random();
        double t = 1.0 / Math.sqrt(2.0 * solution.getDecisionVariables().length); // t is the learning rate
        double newSigma = 1d;
        utils.log("computing sigma, old sigma: "+newSigma, isPrintSteps);
        if (IS_ADAPTIVE) {
            if (isSingle)// single means we have only one sigma.
            {
                newSigma = solution.getSigma()[0] * Math.exp(t * random.nextGaussian());
                //newSolution.getSigmaUsingLogNormalDistribution()[0] = newSigma;
            } else {
                newSigma = solution.getSigma()[index] * Math.exp(t * random.nextGaussian());
                //newSolution.getSigmaUsingLogNormalDistribution()[index] = newSigma;
            }
        }
        utils.log("new sigma: "+newSigma, isPrintSteps);
        return newSigma;
    }
    private Solution doMutation(Solution newSolution, double newSigma, int index)
    {
        utils.log("mutating the value has started", isPrintSteps);
        Random random = new Random();
        // mutate the decision var using the new sigma
        Double newValue = newSolution.getDecisionVariables()[index] + (newSigma * random.nextGaussian());

        if (newValue > UPPER_BOUND) newValue = (double) UPPER_BOUND;
        else if (newValue < LOWER_BOUND) newValue = (double) LOWER_BOUND;

        utils.log("old value: "+newSolution.getDecisionVariables()[index]+", new values: "+newValue, isPrintSteps);
        newSolution.getDecisionVariables()[index] = newValue;
        return newSolution;
    }
}
