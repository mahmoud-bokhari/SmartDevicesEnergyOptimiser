package SolutionGeneratorDPO;

import gin.Mahmoud.Utils;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Mahmoud-Uni on 6/18/2019.
 */
public class ConfigurationFileProcessor {
    Utils utils = new Utils();
    public static boolean isPrintSteps = true;


    public HashMap<String,String> readConfigFile(String path)
    {
        try {

            File file = new File(path);
            utils.log("now reading the configuration file "+file.getAbsolutePath(),isPrintSteps);

            if(!file.exists())
            {
                utils.log("file "+file.getAbsolutePath()+" doesn't exist!", isPrintSteps);
                return null;
            }
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line="";
            HashMap<String,String> configurationHashMap = new HashMap<>();
            while (( line = br.readLine()) != null )
            {
                utils.printOnly("line: "+line,isPrintSteps);
                String[] temp = line.split(",");
                configurationHashMap.put(temp[0],temp[1]);
            }
            return configurationHashMap;
        }

        catch (Exception e)
        {
            e.printStackTrace();
            utils.log("Exception in reading configuration file "+e.getMessage(), true);
            return null;
        }
    }

    public boolean loadSelectedVariables(String[] keys, String[] values, String path)
    {
        try {

            File file = new File(path);
            utils.log("now reading the configuration file "+file.getAbsolutePath(),isPrintSteps);

            if(!file.exists())
            {
                utils.log("file "+file.getAbsolutePath()+" doesn't exist!", isPrintSteps);
                return false;
            }
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line="";
            int count=0;
            while (( line = br.readLine()) != null )
            {
                utils.printOnly("line: "+line,isPrintSteps);
                String[] temp = line.split(",");
                keys[count]=temp[0];
                values[count++]=temp[1];
            }
            return true;
        }

        catch (Exception e)
        {
            e.printStackTrace();
            utils.log("Exception in reading configuration file "+e.getMessage(), true);
            return false;
        }
    }

    public boolean saveConfigurationFile(HashMap<String,String> configFileHashMap, String path)
    {
        try {
            // get the data
            StringBuilder stringBuilder = new StringBuilder();
            for(Map.Entry<String, String> entry : configFileHashMap.entrySet())
            {
                stringBuilder.append(entry.getKey()+","+entry.getValue()+"\n");
            }

            stringBuilder.deleteCharAt(stringBuilder.length()-1); // this to delete the last \n

            // write the data to the file.

            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(path));
            bufferedWriter.write(stringBuilder.toString());
            bufferedWriter.flush();
            bufferedWriter.close();
            return true;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            utils.log("Exception in saving configuration file "+e.getMessage(), true);
            return false;
        }
    }
    public HashMap<String, String> updateConfigurationFile(HashMap<String, String> configurationFileHashMap, String[] keys, String[] values)
    {
        try {
            utils.log("now updating the configuration hashmap ",isPrintSteps);
            for(int i=0;i<keys.length;i++)
            {
                if(configurationFileHashMap.containsKey(keys[i]))
                    configurationFileHashMap.put(keys[i],values[i]);
                else
                    utils.log("key: "+keys[i]+" is not found!",isPrintSteps);
            }
            return configurationFileHashMap;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            utils.log("Exception in updating configuration file "+e.getMessage(), true);
            return configurationFileHashMap;
        }
    }
    public HashMap<String, String> updateConfigurationFile(HashMap<String, String> configurationFileHashMap, Map.Entry<String,String>[] entryArray)
    {
        try
        {
            for(Map.Entry<String, String> entry : entryArray)
            {
                if(configurationFileHashMap.containsKey(entry.getKey()))
                    configurationFileHashMap.put(entry.getKey(),entry.getValue());
                else
                    utils.log("key: "+entry.getKey()+" is not found!",isPrintSteps);
            }
            return configurationFileHashMap;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            utils.log("Exception in updating configuration file "+e.getMessage(), true);
            return configurationFileHashMap;
        }
    }
    public double[] getSelectedVariables()
    {
        return null;
    }
}
