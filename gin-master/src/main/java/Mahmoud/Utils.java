package Mahmoud;

import org.apache.commons.io.FileUtils;

import java.io.*;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by Mahmoud-Uni on 1/9/2019.
 */
public class Utils {

    static StringBuilder logger = new StringBuilder();
    public static boolean isDebug = true;
    boolean scriptOutput = true;
    public void log(String className, String methodName, String message, boolean print)
    {
        if (print)
            System.out.println(className+"."+methodName+": "+message);
        logger.append(className+"."+methodName+": "+message+"\n");
    }

    public void log(String message, boolean print)
    {
        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
        if (print) {
            System.out.println();
            System.out.print(stackTraceElements[2].getClassName() + "." +
                    stackTraceElements[2].getMethodName() + ": " + message);
        }

        logger.append(stackTraceElements[2].getClassName()+"."+
                stackTraceElements[2].getMethodName()+": "+message+"\n");
    }

    public void printOnly(String message, boolean print)
    {
        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
        if (print) {
            System.out.println();
            System.out.print(stackTraceElements[2].getClassName() + "." +
                    stackTraceElements[2].getMethodName() + ": " + message);
        }
    }

    public void resetLogger()
    {
        logger = new StringBuilder();
    }

    public String readFile(String fileName)
    {
        try {
            FileReader fileReader = new FileReader(fileName);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            StringBuilder stringBuilder = new StringBuilder();
            String line = "";
            while((line = bufferedReader.readLine())!= null)
                stringBuilder.append(line+"\n");
            fileReader.close();
            bufferedReader.close();
            //log("line: " + line,isPrintSteps);
            return stringBuilder.toString();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            log("readFile: could not read the given file: "+fileName+"\n"+e.getMessage(),true);
            return "";
        }
    }
    public void saveLog(String path, boolean appeand)
    {
        saveData(logger,path,"", appeand);
    }

    public ArrayList<Double> toDoubleArrayList(Double[] arr)
    {
        ArrayList<Double> result = new ArrayList<>(arr.length);
        for(double d:arr)
            result.add(d);
        return result;
    }



    public void saveData(StringBuilder data, String path, String headers, boolean append)
    {
        FileWriter fileWriter;
        BufferedWriter bufferedWriter;
        try
        {
            File file = new File(path);

            fileWriter = new FileWriter(file,append);
            bufferedWriter = new BufferedWriter(fileWriter);
            if(!file.exists())
                bufferedWriter.write(headers+"\n"+data.toString());
            else bufferedWriter.write(data.toString());
            //fileWriter.
            bufferedWriter.flush();
            bufferedWriter.close();
        }
        catch (Exception e)
        {
            //fileWriter=null;
            //bufferedWriter=null;
            e.printStackTrace();
            log(e.getMessage(),true);
        }
    }


    public String[] doubleArrayToStringArray(Double[] doubles)
    {
        String[] result = new String[doubles.length];
        for(int i=0; i<result.length;i++)
            result[i] = new BigDecimal(doubles[i]).toPlainString();
        return result;
    }

    public String runScript(String shellPath, String scriptName, String []parameters)
    {
        try {

            log("shellPath: "+ shellPath+", scriptName: "+ scriptName+", parameters"+parameters, isDebug);
            String []cmd = new String[parameters.length+2];
            // bash deploy.sh testapps/au.edu.adelaide.lujunweng7099project.tools.busyloop.apk 15sexample sampleSize deviceID

            File file = new File(shellPath);
            if (!file.exists())
                file = new File("c:/Program Files/Git/bin/bash.exe");
            if(!file.exists())
                file = new File("d:/Program Files/Git/bin/bash.exe");
            if(!file.exists())
            {
                log("can not find bash.exe, exiting ...",true);
                System.exit(1);
            }
            cmd[0] = file.getAbsolutePath();

            //cmd[0] = shellPath;// "e:/Program Files/Git/bin/bash.exe";
            cmd[1] = scriptName;// "runOptimisationOnPhone.bsh";
            for(int i=0;i<parameters.length;i++)
                cmd[i+2] = parameters[i];//cmd[2] has the parameters

            File scriptFile = new File(scriptName);
            if(!scriptFile.exists()){
                log("script "+scriptFile.getAbsolutePath()+" is not found",isDebug);
                return "";
            }
            Process pr = Runtime.getRuntime().exec(cmd);
            BufferedReader in = new BufferedReader(new InputStreamReader(pr.getInputStream()));
            BufferedReader errorSTD = new BufferedReader(new InputStreamReader(pr.getErrorStream()));
            StringBuilder commandResults = new StringBuilder();
            String inputLine;
            String error="error";



            /*log("process wait for result: "+processResult,isPrintSteps);
            if(processResult!=0)
                commandResults.append("error");
            return commandResults;*/

            commandResults.append("errors:\n");
            while ((error = errorSTD.readLine()) != null) {
                log(" (stdoutError) Line: "  + error, true);
                commandResults.append(error+"\n");
            }

            if(commandResults.length()=="errors:\n".length())
                commandResults = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                log(" (stdout) Line: " + inputLine, true);
                commandResults.append(inputLine+"\n");
            }
            commandResults.deleteCharAt(commandResults.length()-1);
            errorSTD.close();
            in.close();
            pr.destroy();

            return commandResults.toString();
        } catch (Exception e) {

            log("Exception " + e.getMessage(), true);
            e.printStackTrace();
            System.exit(1);
        }
        return "";
    }

    public StringBuilder runCMD(String cmd, boolean isPrintResults)
    {
        try {

            log("command: "+cmd, isDebug);


            Process pr = Runtime.getRuntime().exec(cmd);
            BufferedReader in = new BufferedReader(new InputStreamReader(pr.getInputStream()));
            BufferedReader errorSTD = new BufferedReader(new InputStreamReader(pr.getErrorStream()));
            StringBuilder commandResults = new StringBuilder();
            String inputLine;
            String error="error";



            /*log("process wait for result: "+processResult,isPrintSteps);
            if(processResult!=0)
                commandResults.append("error");
            return commandResults;*/

            commandResults.append("errors:\n");
            while ((error = errorSTD.readLine()) != null) {
                log(" (stdoutError) Line: "  + error, isPrintResults);
                commandResults.append(error+"\n");
            }

            if(commandResults.length()=="errors:\n".length())
                commandResults = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                log(" (stdout) Line: " + inputLine, isPrintResults);
                commandResults.append(inputLine+"\n");
            }




            int processResult = pr.waitFor();
            log("process wait for result: "+processResult,isPrintResults);

            errorSTD.close();
            in.close();
            pr.destroy();
            return commandResults;
        } catch (Exception e) {

            log("Exception " + e.getMessage(), true);
            e.printStackTrace();
            System.exit(1);
        }
        return null;
    }

    public String runCMD(String cmd, String path ,boolean isPrintResults)
    {
        try
        {
            log("command: "+cmd, isPrintResults);
            FileOutputStream fos = new FileOutputStream(path,true);
            Runtime rt = Runtime.getRuntime();
            Process proc = rt.exec(cmd);
            // any error message?
            ProcessIOReader error = new
                    ProcessIOReader(proc.getErrorStream(), "ERROR", fos);

            // any output?
            ProcessIOReader output = new
                    ProcessIOReader(proc.getInputStream(), "OUTPUT", fos);

            // start readers
            error.start();
            output.start();

            // any error???
            int exitVal = proc.waitFor();
            log("ExitValue: " + exitVal, isPrintResults);
            fos.flush();
            fos.close();
            String result = error.fullOutput.toString();
            if(result != null && !result.isEmpty()) {

                return output.fullOutput.toString() + "\n" + result;
            }
            //output.fullOutput.toString();

        } catch (Throwable t)
        {
            t.printStackTrace();
            log(t.getMessage(),true);
        }
        return "";
    }
    public int generateRandomInt(int min, int max)
    {
        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }
    public File[] getFiles(String path)
    {
        File[] files;
        try {
            File file = new File(path);
            files =  file.listFiles();
            Arrays.sort(files, Comparator.comparingLong(File::lastModified));
            return files;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            log(e.getMessage(), true);
            return null;
        }

    }
    public boolean checkForFailure(StringBuilder stringBuilder)
    {
        String temp = stringBuilder.toString().toLowerCase();
        if(temp.contains("error")||temp.contains("failed")||temp.contains("failure")||temp.contains("failures")||temp.contains("unsuccessful")
                ||temp.contains("error")||temp.contains("errors"))
            return true;
        return false;
    }
    class ProcessIOReader extends Thread
    {
        InputStream is;
        String type;
        OutputStream os;
        public StringBuilder fullOutput = new StringBuilder();
        public ProcessIOReader(InputStream is, String type)
        {
            this(is, type, null);
        }
        ProcessIOReader(InputStream is, String type, OutputStream redirect)
        {
            this.is = is;
            this.type = type;
            this.os = redirect;
        }

        public void run()
        {
            try
            {
                PrintWriter pw = null;
                if (os != null)
                    pw = new PrintWriter(os);

                InputStreamReader isr = new InputStreamReader(is);
                BufferedReader br = new BufferedReader(isr);
                String line=null;
                //System.out.println(type+"...");
                while ( (line = br.readLine()) != null)
                {
                    fullOutput.append(line+"\n");
                    if (pw != null) {
                        pw.println(line);
                    }
                }
                if (pw != null) {
                    pw.flush();
                    pw.close();
                }
            } catch (Exception e)
            {
                e.printStackTrace();
                log(e.getMessage(),true);
            }
        }
    }

    public boolean isNumeric(String s) {
            return s.matches("\\d+");
        }


    public  < E > String arrayToString( E[] inputArray ) {
        // Display array elements
        StringBuilder result = new StringBuilder();
        for(E element : inputArray) {
            result.append(element.toString()+",");
        }
        return result.deleteCharAt(result.length()-1).toString();
    }

    public Double[] arrayToDoubleArray( String[] inputArray ) {
        // Display array elements
        Double[] result = new Double[inputArray.length];
        int last = 0;
        for(int i=0;i<result.length;i++) {
            result[i] = Double.parseDouble(inputArray[i]);
        }
        return result;
    }

    public void copyFileFromTo(String location, String destination, String fileName)
    {
        try {

            FileUtils.copyFileToDirectory(new File(location,fileName),new File(destination));
        }
        catch (Exception e)
        {
            log("failed to copy file..."+location+" to "+destination,true);
            e.printStackTrace();
            log(e.getMessage(),true);
        }
    }
    public void copyFolderFromTo(String location, String destination)
    {
        try {

            FileUtils.copyDirectory(new File(location),new File(destination));
        }
        catch (Exception e)
        {
            log("failed to copy file..."+location+" to "+destination,true);
            e.printStackTrace();
            log(e.getMessage(),true);
        }
    }
    public void deleteFile(String filePath)
    {
        try {
            if(new File(filePath).exists())
                FileUtils.forceDelete(new File(filePath));
        } catch (Exception e) {
            log(e.getMessage(),true);
            e.printStackTrace();
        }
    }
}
