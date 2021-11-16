package Mahmoud;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.Hashtable;
import java.util.Map;

public class CuncurrentFileProcessing {

    public StringBuilder convertHashToStringBuilder(Hashtable<String, Integer> hashtable) {
        try {
            StringBuilder stringBuilder = new StringBuilder();
            for (Map.Entry<String, Integer> entry : hashtable.entrySet()) {
                stringBuilder.append(entry.getKey()).append(",").append(entry.getValue()).append("\n");
            }
            return stringBuilder;

        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }

    public void lockFileForWriting(File fileName, StringBuilder stringBuilder) {
        boolean isSuccessful = false;
        System.out.println("now I'm locking the file for writing: "+System.currentTimeMillis());
        try (
                FileOutputStream fileOutputStream = new FileOutputStream(fileName);
                FileChannel channel = fileOutputStream.getChannel();
                FileLock lock = channel.lock(0, Long.MAX_VALUE, false);
        ) {
            // This method blocks until it can retrieve the lock.
            System.out.println("File is locked for writing: "+System.currentTimeMillis());

            channel.write(ByteBuffer.wrap(stringBuilder.toString().getBytes()));
        } catch (Exception e) {
            e.printStackTrace();
            //return null;
        }
    }

    public StringBuilder lockFileForReading(File fileName) {
        boolean isSuccessful = false;
        System.out.println("now I'm locking the file for reading: "+System.currentTimeMillis());
        try (
                FileInputStream fileInputStream = new FileInputStream(fileName);
                FileChannel channel = fileInputStream.getChannel();
                FileLock lock = channel.lock(0, Long.MAX_VALUE, true);
        ) {
            // This method blocks until it can retrieve the lock.
            System.out.println("File is locked for reading: "+System.currentTimeMillis());
            StringBuilder allLines = readAllLines(channel);
            if (allLines == null) {
                System.out.println("ERROR");
                System.exit(1);
            }
            return allLines;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public StringBuilder readAllLines(FileChannel channel) {
        try {
            BufferedReader br = new BufferedReader(Channels.newReader(channel, "UTF-8"));
            StringBuilder stringBuilder = new StringBuilder();
            String line = "";
            while ((line = br.readLine()) != null) {
                stringBuilder.append(line).append("\n");
            }
            return stringBuilder;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public Hashtable<String, Integer> convertFileToHashTable(StringBuilder fileString) {
        try {
            String[] lines = fileString.toString().split("\n");
            Hashtable<String, Integer> hashtable = new Hashtable<>();
            for (String line : lines) {
                String[] lineArray = line.split(",");
                Integer value = Integer.valueOf(lineArray[1].trim());
                if (hashtable.containsKey(lineArray[0]))
                    hashtable.replace(lineArray[0], value);
                else hashtable.put(lineArray[0], value);
            }
            return hashtable;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
