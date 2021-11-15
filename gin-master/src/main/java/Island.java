import DPO.DpoExperiment;
import Mahmoud.Device;

import java.util.ArrayList;

import static DPO.DpoExperimentRunner.BATTERY_LIMIT;

/**
 * Created by Mahmoud-Uni on 6/24/2019.
 */
public class Island extends Thread {

    public static DpoExperiment.DpoExperimentResults[] threadBests;
    public DpoExperiment.DpoExperimentResults forignerBest;




    String[] deviceNames = new String[]{"nexus9-1","nexus6-5","moto-g-2"};
    int NumberOfDevices=deviceNames.length;
    ArrayList<IslandThread> islandThreads = new ArrayList<>(NumberOfDevices);

    public static boolean resume = false;
    public boolean isPrintSteps = true;
    int id=0;
    int[] ports = new int[]{2,5,4};

    @Override
    public void run() {
        super.run();
    }

    public void run(String[] args)
    {
        try {


            int step = 0;
            for (int i = 0; i < NumberOfDevices; i++) {
                args[1] = deviceNames[i];
                args[2] = ports[i] + "";

                islandThreads.add(new IslandThread(deviceNames[i], args));
            }

            while (islandThreads.size()>0) {
                for (int i = 0; i < NumberOfDevices; i++)
                    threadBests[i] = islandThreads.get(i).run(islandThreads.get(i).bestSolution, step++);
                while (!resume) {
                    Thread.sleep(10000);
                }

                for (int i = 0; i < NumberOfDevices; i++) {
                    if(!islandThreads.get(i).isWorking) {
                        if (islandThreads.get(i).dpoExperimentRunner.testRunner.device.isRunInVivo) {
                            islandThreads.get(i).dpoExperimentRunner.utils.log("checking the battery level... " + islandThreads.get(i).dpoExperimentRunner.testRunner.device.currentBatteryLevel, isPrintSteps);
                            islandThreads.get(i).dpoExperimentRunner.utils.log("tournament size will be: " + islandThreads.get(i).dpoExperimentRunner.testRunner.device.getMinimalSetSize(), isPrintSteps);
                            if (islandThreads.get(i).dpoExperimentRunner.testRunner.device.currentBatteryLevel < BATTERY_LIMIT)
                                islandThreads.remove(i);
                            // TODO: 6/24/2019 to change the condition batteryLevel%10==0 so it includes cases where the level drops more than 1% for each solution evaluations( e.g. 91% -> 89%).

                            if(islandThreads.get(i).dpoExperimentRunner.testRunner.device.currentBatteryLevel != 100 && islandThreads.get(i).dpoExperimentRunner.testRunner.device.currentBatteryLevel%10 == 0)
                            {
                                //exchangeInProgress=true;
                                Device nextDevice = islandThreads.get(i).dpoExperimentRunner.seekImmigration(islandThreads.get(i).dpoExperimentRunner.testRunner.device);
                                int index = getDeviceID(nextDevice);
                                //islandThreads.get(index).dpoExperimentRunner.testRunner.device = islandThreads.get(i).dpoExperimentRunner.testRunner.device;
                                //islandThreads.get(i).dpoExperimentRunner.testRunner.device = nextDevice;
                                islandThreads.get(i).forignerBest = islandThreads.get(index).currentBestResults;
                                islandThreads.get(index).forignerBest = islandThreads.get(i).currentBestResults;

                            }
                        }
                    }
                }

            }// end while


        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    int getDeviceID(Device device)
    {
        for(int i=0;i<deviceNames.length; i++) {
            String id = device.deviceId.concat("-").concat(device.portId + "");
            if(id.equals(deviceNames[i].concat("-").concat(ports[i]+"")));
            return i;

        }
        return -1;
    }
}
