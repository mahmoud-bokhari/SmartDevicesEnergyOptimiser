package gin.Mahmoud;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by MewX on 1/23/2017.
 * Locker file name rule: usbport.portid.timestamp.lock
 */
public class AdbExecutor extends Executor {
    private static int WAIT_TIME = 2000; // 2 seconds for next retry
    int MAX_USB_PORT_NUMBER=8;
    private ArrayList<String> portLocPrefixkList = new ArrayList<>();
    public static boolean isDebug = true;

    public AdbExecutor() {
    }

    public AdbExecutor(String cmd, int portId) {
        this(cmd, "", true, portId);
    }

    public AdbExecutor(String cmd, boolean waitOrNot, int portId) {
        this(cmd, "", waitOrNot, portId);
    }

    public AdbExecutor(String cmd, String executionPath, int portId) {
        this(cmd, executionPath, true, portId);
    }

    public AdbExecutor(String cmd, String executionPath, boolean waitOrNot, int portId) {
        // init
        for (int i = 0; i < MAX_USB_PORT_NUMBER; i++) {
            portLocPrefixkList.add(generateLockFilePrefix(i));
        }

        // before execute, generate the adb lock
        String lockerName = generateLockFileName(portId);
        lockIt(lockerName, portId);

        // charge phone if required
        //chargePhone(); // **** Mahmoud: no need to charge the phone from here. The deepParameterTuning does it anyway.

        // execute the right command, and then unlock it
        execute(cmd, executionPath, waitOrNot);
        unLockIt(lockerName);
    }


    public static void killServer(int portId) {
        new AdbExecutor("adb kill-server", portId);
    }

    /**
     * Generate the lock file to make sure only one adb is running
     * @param lockerFileName make sure locker and unlocker use the same file name
     */
    public void lockIt(String lockerFileName, int portId) {
        // find existing lock file and remove it
        final String curLockFilePrefix = generateLockFilePrefix(portId);
        final String basePath = "locks";
        File lockFile = new File(basePath + File.separatorChar + lockerFileName);
        try {
            new File(basePath).mkdirs();
            lockFile.createNewFile();
            String[] fileList = getFileNameList(basePath);
            for (String temp : fileList) {
                if (temp.startsWith(curLockFilePrefix) && !temp.equals(lockerFileName)) {
                    FileUtils.forceDelete(new File(basePath, temp)); // delete previous lockers
                    utils.log("Removed previous lock file: " + temp,isDebug);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // check running lockers
        long currentLockTimestamp = Long.valueOf(lockerFileName.split("\\.")[2]);
        String[] fileList = getFileNameList(basePath);
        for (String temp : fileList) {
            if (temp.equals(lockerFileName)) continue;

            String[] parts = temp.split("\\."); // usbport.portid.timestamp.lock
            if (Long.valueOf(parts[2]) < currentLockTimestamp) {
                utils.log("Waiting for earlier locker: " + temp+" in folder: " + lockFile.getAbsolutePath(),isDebug);
                File tempFile = new File(basePath + File.separatorChar + temp);
                while (tempFile.exists()) {
                    // wait for running processes
                    try {
                        Thread.sleep(WAIT_TIME);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public void unLockIt(String lockerFileName) {
        final String basePath = "locks";
        new File(basePath + File.separator + lockerFileName).delete();
    }

    private String[] getFileNameList(String dir) {
        File[] files = new File(dir).listFiles();
        String[] strings = new String[files.length];
        for (int i = 0; i < strings.length; i ++) {
            strings[i] = files[i].getName();
        }
        return strings;
    }

    private String generateLockFileName(int port) {
        // filename: adbport{0}.lock
        return generateLockFilePrefix(port) + "." + System.currentTimeMillis() + ".lock";
    }

    private String generateLockFilePrefix(int port) {
        return "usbport." + port;
    }
}

