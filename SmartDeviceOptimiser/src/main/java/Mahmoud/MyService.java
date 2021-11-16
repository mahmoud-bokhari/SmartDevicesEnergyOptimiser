package Mahmoud;

import DPO.OnDeviceExperiment;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Hashtable;



/**
 * Created by Mahmoud-Uni on 8/15/2018.
 */
public class MyService {
    public static final String END = "END";
    public static final String ONLY_CONNECT = "ONLY_CONNECT";
    public static final String READY = "READY";
    private static final String RECHARGE = "RECHARGE";
    private static final String EXCHANGE_IMMIGRANTS = "SEND_IMMIGRANTS";

    Thread serviceThread;
    ServerSocket listener;
    //Device device;
    public static Utils utils = new Utils();
    boolean isPrintSteps = true;

    public void startServiceUsingThread(int port) {
        /*String deviceName = "nexus9-1";
        int portId = 2;
        boolean isInvivo = true;
        float testLimit = 200f;
        device = new Device(deviceName,portId,isInvivo, testLimit, Experiment.NOISE_HANDLING_DUMMY_LOOP);*/

        //CommandLineClass commandLineClass = new CommandLineClass(device.deviceId, device.portId);
        serviceThread = new Thread("" + System.currentTimeMillis()) {
            @Override
            public void run() {
                try {


                    listener = new ServerSocket(port);
                    int portToSend = port;

//                    if(portToSend == 0)
//                        portToSend = listener.getLocalPort();
                    ///* the following is for testing only
//                    String intentAction = "com.example.mahmoud.batterymonitor.externalMeterExperiment";//"com.example.mahmoud_uni.sendCommandToPC.command"
//                    String command = "am broadcast -a "+ intentAction+
//                            " --es event testNew"+
//                            " --es "+Experiment.SERVER_HOST_IP+" 129.127.10.209"+
//                            " --ei "+Experiment.SERVER_SOCKET_PORT+" "+portToSend;
//                    utils.log(commandLineClass.runCommand(command),true);

                    System.out.println(String.format("listening on port = %d", portToSend));

                    Socket s;

                    InputStreamReader in;
                    BufferedReader br;


                    while (true) {

                        s = listener.accept();

                        System.out.println("client connected");

                        in = new InputStreamReader(s.getInputStream());
                        br = new BufferedReader(in);

                        String str = br.readLine();
                        System.out.println("Client : " + str);

                        // sending to the client:
                        PrintWriter pr = new PrintWriter(s.getOutputStream());
                        if (str.contains(END) || str.contains(ONLY_CONNECT)) {
                            break;
                        } else if (str.contains(RECHARGE)) {
                            System.out.println("Server: working on request: " + str);
                            pr.println("Server: working on request: " + str); // response/command
                            Thread.sleep(6 * 60 * 1000);
                            pr.println(END);
                            pr.flush();
                        } else {
                            System.out.println("Server: working on request: " + str);

                            System.out.println("Server: finishing request: " + str);
                            /*pr.println(READY);
                            pr.flush();
                        pr.println("yes");
                        pr.println("yes");
                        pr.flush();
                        pr.println("100");
                        pr.println("\nyes");
                        pr.println("end");
                        pr.flush();*/
                            pr.println("end"); // response/command
                            pr.flush();
                            break; // if the server send end then it must exit as well
                        }

                    }
                    utils.log("bye bye", true);
                    br.close();
                    in.close();
                    s.close();
                    listener.close();
                } catch (Exception e) {
                    utils.log(e.getMessage(),true);
                    e.printStackTrace();
                }
            }

        };
        serviceThread.start();
    }

    public String startService(ServerSocket listener) {
        /*String deviceName = "nexus9-1";
        int portId = 2;
        boolean isInvivo = true;
        float testLimit = 200f;
        device = new Device(deviceName,portId,isInvivo, testLimit, Experiment.NOISE_HANDLING_DUMMY_LOOP);*/

        //CommandLineClass commandLineClass = new CommandLineClass(device.deviceId, device.portId);
        String result = "";
        try {
            this.listener = listener;
            int port = listener.getLocalPort();

//                    if(portToSend == 0)
//                        portToSend = listener.getLocalPort();
            ///* the following is for testing only
//                    String intentAction = "com.example.mahmoud.batterymonitor.externalMeterExperiment";//"com.example.mahmoud_uni.sendCommandToPC.command"
//                    String command = "am broadcast -a "+ intentAction+
//                            " --es event testNew"+
//                            " --es "+Experiment.SERVER_HOST_IP+" 129.127.10.209"+
//                            " --ei "+Experiment.SERVER_SOCKET_PORT+" "+portToSend;
//                    utils.log(commandLineClass.runCommand(command),true);



            Socket s;

            InputStreamReader in;
            BufferedReader br;


            while (true) {
                utils.log(String.format("listening on port = %d", port), isPrintSteps);
                System.out.println("\n"+OnDeviceExperiment.device.deviceName);
                System.out.println("\n"+OnDeviceExperiment.device.currentBatteryLevel);
                s = listener.accept();
                utils.log(">======incoming on=======> " + port, isPrintSteps);
                utils.log("At "+ (new SimpleDateFormat("yyyy-MM-dd HH-mm-ss").format(System.currentTimeMillis())), isPrintSteps);


                in = new InputStreamReader(s.getInputStream());
                br = new BufferedReader(in);

                String str = br.readLine();
                utils.log("Client : " + str,isPrintSteps);

                // sending to the client:
                PrintWriter pr = new PrintWriter(s.getOutputStream());
                if (str.contains(END) || str.contains(ONLY_CONNECT)) {
                    pr.println(END);
                    pr.flush();
                    result = END;
                    break;
                } else if (str.contains(RECHARGE)) {
                    utils.log("Server: working on request: " + str, isPrintSteps);
                    pr.println("Server: working on request: " + str); // response/command
                    Thread.sleep(60 * 1000);
                    pr.println(END);
                    result = RECHARGE;
                    pr.flush();
                }
                else if (str.contains("EXCHANGE")) {
                    utils.log("Server: working on request: " + str, isPrintSteps);
                    //pr.println("Server: working on request: " + str); // response/command
                    // update the device battery
                    OnDeviceExperiment.device.updateCurrentBatteryLevel();
                    // get the current running devices log
                    OnDeviceExperiment onDeviceExperiment = new OnDeviceExperiment();
                    Hashtable<String, Integer> hashtable = OnDeviceExperiment.getCurrentRunningDevices();

                    // update the log with the current device's battery level.
                    hashtable.replace(OnDeviceExperiment.device.deviceName,
                            OnDeviceExperiment.device.currentBatteryLevel);
                    onDeviceExperiment.updateCurrentRunningDevicesLog(hashtable);
                    // now pick a random island
                    RandomTopology randomTopology = new RandomTopology();
                    randomTopology.chooseRandomIsland(hashtable, isPrintSteps);
                    /*OnDeviceExperiment.device.pullFromDevice(OnDeviceExperiment.DEFINE.concat("bestFound.txt"),
                            OnDeviceExperiment.immigrants_directory.concat(currentBestFileName.concat(".csv")));*/


                    // check if there is a solution from another island to migrate to the current device.
                    utils.log("Check whether there is an immigrant TO the current device ", isPrintSteps);
                    boolean isSentToThisDevice = false;

                    File immigrant = randomTopology.getImmigrant(OnDeviceExperiment.immigrants_directory, isSentToThisDevice, isPrintSteps);
                    if (immigrant != null) {
                        utils.log("An immigrant found "+immigrant.getName(), isPrintSteps);
                        // second push to the device its neighbour's immigrants
                        OnDeviceExperiment.device.pushToDevice(immigrant.getAbsolutePath(), OnDeviceExperiment.DEFINE.concat("immigrants/"));
                        //FileUtils.forceDelete(immigrant);
                        if(!isSentToThisDevice)
                        {
                            try {
                                String name = immigrant.getAbsolutePath();
                                // append the current device name (island) to the immigrant.
                                // this to make easier to trace files.
                                name = name.replace(".txt", OnDeviceExperiment.device.deviceName.concat(".txt"));
                                FileUtils.copyFile(immigrant, new File(name));
                                FileUtils.moveFileToDirectory(new File(name),
                                        new File(OnDeviceExperiment.used_immigrants_directory),
                                        true);
                                FileUtils.forceDelete(immigrant);
                            }
                            catch (Exception e)
                            {
                                utils.log(e.getMessage(), true);
                            }
                        }
                        else {
                            try {
                                FileUtils.moveFileToDirectory(immigrant,
                                        new File(OnDeviceExperiment.used_immigrants_directory),
                                        true);
                            }
                            catch (Exception e)
                            {
                                utils.log(e.getMessage(), true);
                            }
                        }
                    }
                    else
                        utils.log("NO immigrant found ", isPrintSteps);


                    utils.log("Immigration exchange is done",isPrintSteps);
                    // delete old immigrants from the device?
                    result = EXCHANGE_IMMIGRANTS;
                    pr.println(END);
                    pr.flush();

                    onDeviceExperiment.prepareDeviceForRun();
                    String simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss").format(System.currentTimeMillis());
                    utils.log("Exchange finished at " + simpleDateFormat, isPrintSteps);
                    //continue;
                }
                else {
                    utils.log("Server: working on request: " + str, isPrintSteps);

                    utils.log("Server: finishing request: " + str, isPrintSteps);
                            /*pr.println(REA DY);
                            pr.flush();
                        pr.println("yes");
                        pr.println("yes");
                        pr.flush();
                        pr.println("100");
                        pr.println("\nyes");
                        pr.println("end");
                        pr.flush();*/
                    pr.println("end"); // response/command
                    pr.flush();
                    result = EXCHANGE_IMMIGRANTS;
                    break; // if the server send end then it must exit as well
                }

            }
            utils.log("bye bye", true);
            br.close();
            in.close();
            s.close();
            listener.close();
        } catch (Exception e) {
            utils.log(e.getMessage(),true);
            result = "ERROR";
            e.printStackTrace();
        }
        return result;
    }

    public void stopService() {
        try {
            if (listener != null) {
                if (!listener.isClosed()) {

                    listener.close();
                    utils.log("stopping listening on port: " + listener.getLocalPort() + listener.isBound(),isPrintSteps);
                    listener = null;

                } else utils.log("listening on port: " + listener.isClosed(),isPrintSteps);
            } else utils.log("serverSocket is dead ",isPrintSteps);
        } catch (Exception e) {
            utils.log(e.getMessage(),true);
            e.printStackTrace();
        }
    }

    public void stopServiceThread() {
        try {
            stopService();
            if (serviceThread != null) serviceThread = null;
            else utils.log("serviceThread is already dead",isPrintSteps);
        } catch (Exception e) {

            utils.log(e.getMessage(),true);
            e.printStackTrace();
            listener = null;
        }
    }

    public static void main(String[] args) throws InterruptedException {
        // write your code here
//        MyService myService = new MyService();
//        myService.startService(0);
//        Thread.sleep(10l * 60 * 1000);
//        myService.stopServiceThread();

    }


}

