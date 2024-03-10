package bgu.spl.net.impl.tftp;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import bgu.spl.net.impl.tftp.Packet;

import java.io.File;
import java.io.FileInputStream;




public class KeyBoard implements Runnable{

    public static String currFileNameRRQclient = null;
    public static String currFileNameWRQclient = null;
    public static List<byte[]> fileChunksWRQclient;

    boolean shouldTerminate = false;
    private Listening listening;

    public KeyBoard(Listening listening) {
        this.listening = listening;
    }


    @Override
    public void run() {
        try (Scanner scanner = new Scanner(System.in)){
            while (!shouldTerminate) {
                String response = scanner.nextLine();
                Packet packet = checkInput(response);

                if(packet!=null)
                {
                    synchronized (listening.waitForServer) {
                        listening.send(packet);
                        try {
                            // wait for listening thread to notify that the server finished handeling request
                            listening.waitForServer.wait();
                            } catch (InterruptedException e) {e.printStackTrace();}
                    }
                    if(packet.getOpcode() == Operations.DISC.getValue())
                        listening.shouldTerminate = true;

                }

            }
        } 
    }



    public Packet checkInput(String response)
    {
        Packet packet = new Packet();

        // Split the string by spaces
        String[] words = response.split("\\s+");
        
        if(words.length==2 && words[0].compareTo("LOGRQ") == 0)
        {
            packet.setOpcode(Operations.LOGRQ.getValue());
            packet.setUserName(words[1]);
        }
        else if(words.length==2 && words[0].compareTo("RRQ") == 0)
        {
            String fileName = words[1];

            // check if the file exist
            File file = new File("./" + fileName);
            if (file.exists()) { //If the file doesn't exists we will send en error packet
                deleteFileContents("./" + fileName);
            }
            packet.setOpcode(Operations.RRQ.getValue());
            packet.setFileName(fileName);
            currFileNameRRQclient = fileName;
        }

        else if(words.length==2 && words[0].compareTo("WRQ") == 0)
        {
            // To check if the file exists
            String fileName = words[1];
            File file = new File("./" + fileName);
            if (!file.exists()) { //If the file doesn't exists we will send en error packet
                System.out.println("File do not exist");
            }
            else{
                packet.setOpcode(Operations.WRQ.getValue());
                packet.setFileName(words[1]);

                KeyBoard.currFileNameWRQclient = fileName;
                try (FileInputStream fis = new FileInputStream(file)) {
                    fileChunksWRQclient = new ArrayList<>();
                    byte[] buffer = new byte[512];
                    int bytesRead = 0;
                    while ((bytesRead = fis.read(buffer)) != -1) {
                        byte[] chunk = new byte[bytesRead];
                        System.arraycopy(buffer, 0, chunk, 0, bytesRead);
                        fileChunksWRQclient.add(chunk);
                    }
                } catch (IOException e) {
                    // Error reading the file, send an error packet
                    System.out.println("Error reading file");
                }
            }
        }

        else if(words.length==1 && words[0].compareTo("DIRQ") == 0)
        {
            packet.setOpcode(Operations.DIRQ.getValue());
        }
        else if(words.length==2 && words[0].compareTo("DELRQ") == 0)
        {
            packet.setOpcode(Operations.DELRQ.getValue());
            packet.setFileName(words[1]);
        }
        else if(words.length==1 && words[0].compareTo("DISC") == 0)
        {
            packet.setOpcode(Operations.DISC.getValue());
            shouldTerminate = true;
        }
        else
        {
            System.out.println("Illegal TFTP operation");
            return null;
        }

        return packet;
    }

        public static void deleteFileContents(String filePath) {
        try {
            Path path = Paths.get(filePath);
            Files.write(path, new byte[0]); // Overwrite the file contents with an empty byte array
        } catch (IOException e) {
        }
    }
}
