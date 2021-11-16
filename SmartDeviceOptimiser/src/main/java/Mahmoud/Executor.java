package Mahmoud;

import java.io.*;

/**
 * This class is used to runAndSaveProcessOutputs an external application and get the output result
 * Created by Yuanzhong on 11/16/2016.
 */
public class Executor {

    private Process cmdProcess;
    public InputStream inputStream; // from command line to screen
    public BufferedReader bufferedReader;
    public InputStream errorStream; // from command line to screen
    public OutputStream outputStream; // from keyboard to command line
    private String fullInput, fullError;
    Utils utils = new Utils();
    public static boolean isDebug= false;

    /**
     * The only constructor without running afterwards
     */
    public Executor() { }

    public Executor(String cmd) {
        this(cmd, true); // by default is true
    }

    public Executor(String cmd, boolean waitOrNot) {
        this(cmd, "", waitOrNot);
    }

    public Executor(String cmd, String executionPath) {
        this(cmd, executionPath, true); // by default is true
    }

    public Executor(String cmd, String executionPath, boolean waitOrNot) {
        execute(cmd, executionPath, waitOrNot);
    }

    public boolean execute(String cmd) {
        return execute(cmd, "", true);
    }

    /**
     * Execute an external command
     * @param cmd command line
     * @return false if fails, otherwise true
     */
    public boolean execute(String cmd, boolean waitOrNot) {
        return execute(cmd, "", waitOrNot);
    }

    /**
     * Execute an external command
     * @param cmd command line
     * @param executionPath the execution path
     * @return false if fails, otherwise true
     */
    public boolean execute(String cmd, String executionPath, boolean waitOrNot) {
        utils.log("Executing: " + cmd,isDebug);
        try {
            ProcessBuilder pb = null;
            pb = new ProcessBuilder(cmd.split("\\s+"));
            pb.environment().put("PATH", "C:\\Program Files\\ConEmu\\ConEmu\\Scripts;C:\\Program Files\\ConEmu;C:\\Program Files\\ConEmu\\ConEmu;C:\\ProgramData\\Oracle\\Java\\javapath;D:\\Program Files\\Python27\\;D:\\Program Files\\Python27\\Scripts;C:\\WINDOWS\\system32;C:\\WINDOWS;C:\\WINDOWS\\System32\\Wbem;C:\\WINDOWS\\System32\\WindowsPowerShell\\v1.0\\;C:\\Users\\MewCa\\.dnx\\bin;C:\\Program Files\\Microsoft DNX\\Dnvm\\;C:\\Program Files\\Microsoft SQL Server\\120\\Tools\\Binn\\;C:\\Program Files (x86)\\NVIDIA Corporation\\PhysX\\Common;C:\\WINDOWS\\system32;C:\\WINDOWS;C:\\WINDOWS\\System32\\Wbem;C:\\WINDOWS\\System32\\WindowsPowerShell\\v1.0\\;C:\\WINDOWS\\system32\\config\\systemprofile\\.dnx\\bin;C:\\Program Files (x86)\\nodejs\\;D:\\Program Files (x86)\\010 Editor;D:\\upupw\\PHP5;C:\\ProgramData\\ComposerSetup\\bin;C:\\Program Files (x86)\\Windows Kits\\10\\Windows Performance Toolkit\\;D:\\Program Files (x86)\\scala\\bin;D:\\Program Files (x86)\\sbt\\bin;D:\\Program Files\\leJOS EV3\\bin;D:\\Program Files (x86)\\ActiveTcl\\bin;D:\\Ruby21\\bin;C:\\Users\\MewCa\\AppData\\Local\\Programs\\Python\\Python35-32\\Scripts\\;C:\\Users\\MewCa\\AppData\\Local\\Programs\\Python\\Python35-32\\;D:\\Program Files\\Android\\sdk\\platform-tools;D:\\Program Files (x86)\\Microsoft Visual Studio 14.0\\VC\\bin;C:\\Users\\MewCa\\AppData\\Roaming\\npm;D:\\Program Files\\Java\\jre7\\bin;D:\\Program Files\\Java\\jdk1.7.0_79\\bin;C:\\Users\\MewCa\\AppData\\Local\\Programs\\Git\\cmd;C:\\Users\\MewCa\\AppData\\Local\\Programs\\Git\\mingw64\\bin;C:\\Users\\MewCa\\AppData\\Local\\Programs\\Git\\usr\\bin;D:\\Android\\android-sdk\\platform-tools;D:\\Program Files (x86)\\Microsoft VS Code\\bin;D:\\Program Files (x86)\\scala\\bin;D:\\Program Files (x86)\\sbt\\bin;D:\\Dostools\\JetBrains;C:\\Users\\MewCa\\AppData\\Local\\Microsoft\\WindowsApps;C:\\ProgramData\\Oracle\\Java\\javapath;D:\\Program Files\\Python27\\;D:\\Program Files\\Python27\\Scripts;C:\\WINDOWS\\system32;C:\\WINDOWS;C:\\WINDOWS\\System32\\Wbem;C:\\WINDOWS\\System32\\WindowsPowerShell\\v1.0\\;C:\\Users\\MewCa\\.dnx\\bin;C:\\Program Files\\Microsoft DNX\\Dnvm\\;C:\\Program Files\\Microsoft SQL Server\\120\\Tools\\Binn\\;C:\\Program Files (x86)\\NVIDIA Corporation\\PhysX\\Common;C:\\WINDOWS\\system32;C:\\WINDOWS;C:\\WINDOWS\\System32\\Wbem;C:\\WINDOWS\\System32\\WindowsPowerShell\\v1.0\\;C:\\WINDOWS\\system32\\config\\systemprofile\\.dnx\\bin;C:\\Program Files (x86)\\nodejs\\;D:\\Program Files (x86)\\010 Editor;D:\\upupw\\PHP5;C:\\ProgramData\\ComposerSetup\\bin;C:\\Program Files (x86)\\Windows Kits\\10\\Windows Performance Toolkit\\;D:\\Program Files (x86)\\scala\\bin;D:\\Program Files (x86)\\sbt\\bin;D:\\Program Files\\leJOS EV3\\bin;D:\\Program Files (x86)\\JetBrains\\IntelliJ IDEA 2016.1.1\\plugins\\Kotlin\\kotlinc\\bin;D:\\Program Files (x86)\\gradle-3.2.1\\bin");
            pb.redirectErrorStream(true);
//            Main.log(System.getenv("PATH")); // todo
//            if (OSValidator.isWindows()) {
//                pb = new ProcessBuilder("CMD.exe", "/C", "SET"); // SET prints out the environment variables
//                pb.redirectErrorStream(true);
//                Map<String, String> env = pb.environment();
//                String path = env.get("Path");
//                env.put("Path", path);
//            } else {
//                // todo
//
//            }


            if (cmdProcess != null)
                cmdProcess.destroy();
            if (executionPath.length() == 0)
                cmdProcess = pb.start();
            else
                cmdProcess = pb.directory(new File(executionPath)).start();
            outputStream = cmdProcess.getOutputStream();
            errorStream = cmdProcess.getErrorStream();
            inputStream = cmdProcess.getInputStream();
            bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            utils.log("Starting waiting for output...",isDebug);
            if (waitOrNot) {
                String res = getFullInput(true);
                System.out.print("...");
                cmdProcess.waitFor();
                //System.out.print("Done!");
                utils.log(res,isDebug);
            } else {
                utils.log("...Skipped!",isDebug);
            }
            //System.out.println();

//            LogStreamReader lsr = new LogStreamReader(inputStream);
//            readingResult = new Thread(lsr, "LogStreamReader");
//            readingResult.start();

        } catch (Exception e) {
            System.out.println();
            utils.log(e.getMessage(),true);
            e.printStackTrace();
            return false;
        }

        return true;
    }

    /**
     * Get full output of a command line application.
     * Note: should be carefully use if the application does not finish execution immediately.
     * @param force true to force re-read command output
     * @return the full contents.
     */
    // TODO: in a separate thread: http://stackoverflow.com/questions/17038324/cannot-get-the-getinputstream-from-runtime-getruntime-exec
    public String getFullInput(boolean force) {
        if (fullInput != null && fullInput.length() > 0 && !force) return fullInput;
        fullInput = "";

        StringBuilder sb = new StringBuilder();
        try {
            //Thread.sleep(2000);// I'm not sure why Yuanzhong wanted so sleep in here. He wanted to wait for something.
            while (true) {
                String temp = bufferedReader.readLine();
                if (temp == null) break;
                sb.append(temp).append("\n");
            }
//            while (inputStream.available() > 0)
//                sb.append((char) inputStream.read());
        } catch (Exception e) {
            utils.log(e.getMessage(),true);
            e.printStackTrace();
        }
        fullInput = sb.toString();
        return fullInput;
    }

    public String getFullError() {
        if (fullError != null && fullError.length() > 0) return fullError;

        StringBuilder sb = new StringBuilder();
        try {
            Thread.sleep(2000);
            while (errorStream.available() > 0)
                sb.append((char) errorStream.read());
        } catch (Exception e) {
            utils.log(e.getMessage(),true);
            e.printStackTrace();
        }
        fullError = sb.toString();
        return fullError;
    }
}
