package Mahmoud;

import DPO.OnDeviceExperiment;

import java.io.File;
import java.util.ArrayList;
import java.util.Hashtable;



public class RandomTopology {
    // pull files.

    // update current running.
    // pull current best.
    // choose a device randomly.
    private String chooseRandomIsland(Hashtable<String, Integer> hashtable) {

        // choose random device
        int deviceInd = DPO.OnDeviceExperiment.utils.generateRandomInt(0, hashtable.size()-1);
        String[] array = new String[hashtable.size()];
        hashtable.keySet().toArray(array);
        int count = 0;
        while(hashtable.get(array[deviceInd]) < 30 || DPO.OnDeviceExperiment.device.deviceName.contains(array[deviceInd]))
        {
            deviceInd = DPO.OnDeviceExperiment.utils.generateRandomInt(0, hashtable.size()-1);
            count++;
            if(count>100) return "";

        }
        return array[deviceInd];
    }

    public void chooseRandomIsland(Hashtable<String, Integer> hashtable, boolean isPrintSteps)
    {
        String randomIsland = chooseRandomIsland(hashtable);

        DPO.OnDeviceExperiment.utils.log("Chosen island: "+randomIsland
                +" battery level: "+OnDeviceExperiment.device.currentBatteryLevel, isPrintSteps);

        // pull current best from the current device
        // name it as deviceName-OS-BatteryLevel-RandomIsland.csv, the random island to be evaluated in.
        DPO.OnDeviceExperiment.utils.log("Pull current best from the current device ", isPrintSteps);

        String currentBestFileName = OnDeviceExperiment.device.deviceName+"-"+OnDeviceExperiment.device.OS+
                "-"+OnDeviceExperiment.device.currentBatteryLevel
                +"-"+randomIsland;
        OnDeviceExperiment.device.pullFromDevice(OnDeviceExperiment.DEFINE.concat("bestFound.txt"),"\""+
                OnDeviceExperiment.immigrants_directory.concat(currentBestFileName.concat(".txt"))+"\"");
    }

    public File getImmigrant(String path, boolean isSentToThisDevice, boolean isPrintSteps)
    {
        try {
            File[] files = DPO.OnDeviceExperiment.utils.getFiles(path); // files are sorted by date modified
            if(files == null)
                return null;
            // we will retrieve all immigrants that were sent to be evaluated on this device.
            // then we will choose on of them randomly.
            ArrayList<File> immigrants = new ArrayList<>();

            for(File file : files)
            {
                String filename = file.getName();
                if (isSentToThisDevice) {
                    if (!file.getName().startsWith(DPO.OnDeviceExperiment.device.deviceName)
                            && file.getName().endsWith(DPO.OnDeviceExperiment.device.deviceName + ".txt"))
                        immigrants.add(file);
                }
                else
                {
                    if (!file.getName().startsWith(DPO.OnDeviceExperiment.device.deviceName)) {
                        immigrants.add(file);
                    }
                }
            }
            for (File immigrant : immigrants)
                DPO.OnDeviceExperiment.utils.log("immigrant: " + immigrant.getName(), isPrintSteps);
            if(immigrants.size()>1)
            {
                int ind = DPO.OnDeviceExperiment.utils.generateRandomInt(0,immigrants.size()-1);
                return immigrants.get(ind);
            }
            else if (immigrants.size() == 1)
                return immigrants.get(0);
            else
                return null;
            // to get the most recent one immigrants.get(0);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            DPO.OnDeviceExperiment.utils.log(e.getMessage(), true);
            return null;
        }
    }

    public void pullCurrentBest(Hashtable<String, Integer> hashtable, boolean isPrintSteps)
    {
        // pull current best from the current device
        // name it as deviceName-OS-BatteryLevel.csv, the random island to be evaluated in.
        DPO.OnDeviceExperiment.utils.log("Pull current best from the current device ", isPrintSteps);
        String currentBestFileName = OnDeviceExperiment.device.deviceName+"-"+OnDeviceExperiment.device.OS+
                "-"+OnDeviceExperiment.device.currentBatteryLevel;
        OnDeviceExperiment.device.pullFromDevice(OnDeviceExperiment.DEFINE.concat("bestFound.txt"),"\""+
                OnDeviceExperiment.immigrants_directory.concat(currentBestFileName.concat(".txt"))+"\"");
    }

}
