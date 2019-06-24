import DPO.DpoExperiment;
import DPO.DpoExperimentRunner;
import SolutionGeneratorDPO.ConfigurationFileProcessor;
import gin.Mahmoud.*;
import gin.StructuralTunningExperimentRunner;

/**
 * Created by Mahmoud-Uni on 6/17/2019.
 */
public class Main {

    public static boolean isPrintSteps = true;
    private boolean isDebug=true;

    static Utils utils = new Utils();
    public static void main(String[] args) {

       /*devices.add(new Device("nexus6-1",1, true));
        devices.add(new Device("nexus6-2",2, true));
        devices.add(new Device("nexus6-4",4, true));
        devices.add(new Device("nexus6-5",5, true));*/
        //devices.add(new Device("nexus6-2",2, true));


        // this for printing all steps:
        AdbExecutor.isDebug = false;
        Device.isDebug = false;
        Device.isPrintSteps = true;
        Executor.isDebug = false;
        PhoneController.isPrintSteps = false;
        CommandLineClass.isDebug = false;
        DpoExperiment.isDebug = false;
        ConfigurationFileProcessor.isPrintSteps=true;
        Utils.isDebug=true;

        String sourceFilename;

        String deviceName="";
        String deviceId="";
        Device device = new Device();
        boolean isInvivo=false;
        int portId=0;


        //args[0]="gin-rebound/classes/examples/rebound/Spring.java";

        //args[0]="gin-master/examples/Triangle.java";


        if(args == null || args.length == 0)
        {

            deviceName = "nexus6-4";//"nexus6-2";
            portId = 4;
            isInvivo = true;
            sourceFilename="gin-master\\reboundPC\\app\\src\\main\\java\\com\\example\\mahmoud\\modifiedrebound\\Spring.java";

            device = new Device(deviceName,portId,isInvivo);
            String SEARCH_APPROACH = "explore";
            double SECOND_TERMINATION_CONDITION_DAY = 1d;
            SECOND_TERMINATION_CONDITION_DAY = SECOND_TERMINATION_CONDITION_DAY * 24 * 60 * 60 * 1000;
            args= new String[]{"dpo-es-1+1",deviceName,portId+"",isInvivo+"","explore",SECOND_TERMINATION_CONDITION_DAY+""};

        }
            if(args[0].contains("gin-rebound")) {

                StructuralTunningExperimentRunner structuralTunningExperimentRunner = new StructuralTunningExperimentRunner();
                structuralTunningExperimentRunner.run(args);
            }
            else if(args[0].contains("dpo-es-1+1"))
            {
                DpoExperimentRunner dpoExperimentRunner = new DpoExperimentRunner();

                //System.out.println(dpoExperimentRunner.seekImmigration(device));

                dpoExperimentRunner.run(args);
            }

            else if(args[0].contains("islands-dpo-es-1+1"))
            {
                DpoExperimentRunner dpoExperimentRunner = new DpoExperimentRunner();

                //System.out.println(dpoExperimentRunner.seekImmigration(device));

                dpoExperimentRunner.run(args);
            }

    }
    class IslandThread extends Thread
    {
        String name="";
        public DpoExperiment.DpoExperimentResults currentBest;
        public DpoExperiment.DpoExperimentResults forignerBest;
        public IslandThread(String name)
        {
            super(name);
        }
        @Override
        public void run() {
            super.run();
        }
    }
}
