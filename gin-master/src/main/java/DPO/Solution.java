package DPO;

import Mahmoud.Utils;

import java.util.ArrayList;

/**
 * Created by Mahmoud-Uni on 1/8/2019.
 */
public class Solution {
    private Double []decisionVariables;
    private Double []sigma;
    //public double sigma=250; // in ms
    public double fitness;
    public boolean wasBest = false;
    public static int size=5;

    Utils utils = new Utils();
    public ArrayList<Double> samples = new ArrayList<>();

    public void addSample(Double sample)
    {
        samples.add(sample);
    }

    @Override
    public String toString() {

        return (utils.arrayToString(decisionVariables))+","+fitness+","+wasBest+","+utils.toDoubleArrayList(sigma)+","+samples.size()+","+samples.toString().replace("[","").replace("]","");
    }

    public String prepareForPrinting() {

        return (utils.arrayToString(decisionVariables))+","+utils.toDoubleArrayList(sigma);
    }

    public String getHeader()
    {
        StringBuilder stringBuilder = new StringBuilder();
        for(int i=0; i<decisionVariables.length; i++)
        {
            stringBuilder.append("var "+(i+1)+",");
        }
        stringBuilder.append("fitness,wasBest,sigma,number of samples");
        return stringBuilder.toString();
    }

    /*public long getDuration()
    {
        return duration+Mutation.baseDuration;
    }*/
    public Solution clone()
    {

        Solution solution = new Solution(this.size);
        solution.decisionVariables = new Double[this.decisionVariables.length];
        solution.sigma = new Double[this.sigma.length];

        for(int i=0; i<decisionVariables.length;i++) {
            solution.decisionVariables[i] = this.decisionVariables[i];
            solution.sigma[i] = this.sigma[i];
        }
        solution.fitness = this.fitness;
        solution.wasBest = this.wasBest;
        Double []arr = new Double[samples.size()];
        arr = this.samples.toArray(arr);
        solution.samples = utils.toDoubleArrayList(arr);
        return solution;
    }

    public Double[] getDecisionVariables() {
        return decisionVariables;
    }

    public void setDecisionVariables(Double[] decisionVariables) {
        this.decisionVariables = decisionVariables;
    }

    public Double[] getSigma() {
        return sigma;
    }

    public void setSigma(Double[] sigma) {
        this.sigma = sigma;
    }

    public Solution(int size)
    {
        decisionVariables = new Double[size];
        sigma = new Double[size];
        for(int i=0;i<size;i++)
            sigma[i]=1d;
        this.size = size;
    }
}

