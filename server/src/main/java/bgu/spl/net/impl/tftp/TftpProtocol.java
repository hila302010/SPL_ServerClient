package bgu.spl.net.impl.tftp;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

import bgu.spl.net.api.BidiMessagingProtocol;
import bgu.spl.net.srv.BlockingConnectionHandler;
import bgu.spl.net.srv.Connections;
import bgu.spl.net.impl.tftp.Packet;



public class TftpProtocol implements BidiMessagingProtocol<Packet>  {

    private boolean shouldTerminate;
    private int connectionId;
    private TftpConnections<Packet> connections;
    private static final String FILES_FOLDER_PATH = "/home/hila/Projects/Project3/SPL_Project3/server/Files";
    private File filesFolder;
    
    

    @Override
    public void start(int connectionId, Connections<Packet> connections) {
        // TODO implement this
       this.shouldTerminate = false;
       this.connectionId = connectionId;
       this.connections =  (TftpConnections<Packet>) connections;
       filesFolder = new File(FILES_FOLDER_PATH);
    }


    @Override
    public void process(Packet packet) {
        // TODO implement this

        short opcode = packet.getOpcode();

        // Check if the user is connected, if he isnt every action exept logrq result an error
        if(opcode != Operations.LOGRQ.getValue()  && opcode != Operations.DISC.getValue() && connections.user_names.containsKey(connectionId) == false)
        {
            Packet erPacket = getErrPack((short)6, "User is not logged in");
            connections.send(connectionId, erPacket);
        }
        else{
            
            // RRQ
            if (opcode == Operations.RRQ.getValue()) 
                readReq(packet);

            // WRQ
            if(opcode == Operations.WRQ.getValue())
                writeReq(packet);
            
            // DIRQ
            if(opcode == Operations.DIRQ.getValue())
                directoryListenReq(packet);

            // LOGRQ
            if(opcode == Operations.LOGRQ.getValue())
                loginReq(packet);
            
            // DELRQ
            if(opcode == Operations.DELRQ.getValue())
                deleteReq(packet);
            

            if(opcode == Operations.BCAST.getValue())
            {
                
            }
            if(opcode == Operations.ERROR.getValue())
            {
                
            }

            // DISC
            if(opcode == Operations.DISC.getValue()){

                Packet ackPacket = getAckPack((short)0);
                connections.send(connectionId, ackPacket);
                connections.disconnect(this.connectionId);
                shouldTerminate = true;

            }
        }

        
    }

    public void writeReq(Packet packet)
    {
        String fileName = packet.getFileName();
        boolean fileExists = checkIfFileInFolder(fileName,filesFolder);

        if (fileExists) {

            Packet errorPacket = getErrPack((short)(5), "File already exists");
            connections.send(connectionId, errorPacket);

        } else {
            File newFile = new File(filesFolder, fileName);

            // Create a new FileOutputStream for the newFile
            try (FileOutputStream fos = new FileOutputStream(newFile)) {

                // Get the data from the packet and write it to the file
                //byte[] data = packet.getData().getBytes(StandardCharsets.UTF_8);
                //fos.write(data);
            } catch (IOException e) {
                e.printStackTrace();
                // If an error occurs during the file transfer, you might want to send an error packet to the client
                // and handle any cleanup or error recovery logic here.
            }

            // Once the file transfer is complete, you can send an ACK packet to the client
            Packet ackPacket = new Packet();
            ackPacket.setOpcode(Operations.ACK.getValue());
            // Set the block number of the ACK packet appropriately
            // (e.g., to acknowledge the successful receipt of the file)
            connections.send(connectionId, ackPacket);

            // Print a message indicating that the WRQ is complete
            System.out.println("WRQ " + fileName + " complete");
        }
    }

    public void readReq(Packet packet)
    {
        String fileName = packet.getFileName();

        // Check if the file exists in the Files folder
        File file = new File(filesFolder, fileName);
        if (!file.exists()) {
            // File not found, send an error packet
            Packet errorPacket = getErrPack((short) 1, "File not found");
            connections.send(connectionId, errorPacket);
            return;
        }

        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] buffer = new byte[512]; // 512 bytes per DATA packet
            int bytesRead;
            short blockNumber = 1;

            while ((bytesRead = fis.read(buffer)) != -1) {
                // Create a DATA packet with the read data
                Packet dataPacket = new Packet();
                dataPacket.setOpcode(Operations.DATA.getValue());
                dataPacket.setBlockNumber(blockNumber++);
                //dataPacket.setData(new String(buffer, 0, bytesRead, StandardCharsets.UTF_8));

                // Send the DATA packet to the client
                connections.send(connectionId, dataPacket);
            }

            // Print a message indicating that the RRQ is complete
            System.out.println("RRQ " + fileName + " complete");
        } catch (IOException e) {
            // Error reading the file, send an error packet
            Packet errorPacket = getErrPack((short) 2, "Error reading file");
            connections.send(connectionId, errorPacket);
        }
    }
    
    public void deleteReq(Packet packet)
    {
         // get file to delete
         String fileNameToDelete = packet.getFileName();
         File fileToDelete = new File(filesFolder, fileNameToDelete);

         if (fileToDelete.exists()) {
             if (fileToDelete.delete()) {
                 // File deletion successful, send broadcast
                 Packet broadcastPacket = new Packet();
                 broadcastPacket.setOpcode(Operations.BCAST.getValue());
                 broadcastPacket.setFileName(fileNameToDelete);
                 broadcastPacket.setAddedOrDeleted(true);
                 
                 for (Integer id : connections.connectionHandlers.keySet()) {
                     connections.send(id, broadcastPacket);
                 }
             } else {
                 // File deletion failed
                 Packet errorPacket = getErrPack((short) 2, "Failed to delete file");
                 connections.send(connectionId, errorPacket);
             }
         } 
         else { // File does not exist
             Packet errorPacket = getErrPack((short) 1, "File not found");
             connections.send(connectionId, errorPacket);
         }
    }

    public void loginReq(Packet packet)
    {
        
        String userName = packet.getUserName();
        Boolean isExist = false;

        // needs to add userName if it doesn't exist
        for(String name: connections.user_names.values()){

            //in case name already exists
            if(name.compareTo(userName) == 0){
                Packet errorPacket = getErrPack((short)(7), "User already exists");
                isExist = true;
                connections.send(connectionId, errorPacket);
            }
        }
        // if successful send ACK RQ:
        if(!isExist){
            connections.user_names.put(connectionId, userName);
            Packet ackPacket = getAckPack((short)0);
            connections.send(connectionId, ackPacket);
        }
    }

    public void directoryListenReq(Packet packet)
    {
        // Get the list of files in the directory
        File[] files = filesFolder.listFiles();

        StringBuilder fileListBuilder = new StringBuilder();
        if(files!= null)
        {
            // Append file names to the directory listing string
            for (File file : files) {
                fileListBuilder.append(file.getName()).append("\0"); // Separate file names with null byte
            }
        }

        String fileList = fileListBuilder.toString();
        
        short blockNum = 1;
        // Split the file list into chunks of 512 bytes (maximum packet size)
        while (fileList.length() > 0) {
            int endIndex = Math.min(fileList.length(), 512);
            String chunk = fileList.substring(0, endIndex);
            fileList = fileList.substring(endIndex);

            // Create and send a DATA packet containing the chunk
            Packet dataPacket = getDataPack((short) chunk.length(), blockNum, chunk.getBytes());

            connections.send(connectionId, dataPacket);
            blockNum++;
        }
    }


    public boolean checkIfFileInFolder(String fileName, File filesFolder){

        // saves the names of the files in filesFolder in an array
        File[] filesInFolder = filesFolder.listFiles();

        boolean fileExists = false;
            
        // Check if the file exists in the Files folder
        if (filesInFolder != null) {
            for (File file : filesInFolder) {
                if (file.isFile() && file.getName().equals(fileName)) {
                    fileExists = true;
                    break;
                }
            }
        }
        return fileExists;
    }


    public Packet getErrPack(short errCode, String errMsg){

        Packet errorPacket = new Packet();
        errorPacket.setOpcode(Operations.ERROR.getValue());
        errorPacket.setErrorCode(errCode);
        errorPacket.setErrMsg(errMsg); 

        return errorPacket;
    }

    public Packet getAckPack(short blockNum){

        Packet ackPacket = new Packet();
        ackPacket.setOpcode(Operations.ACK.getValue());
        ackPacket.setBlockNumber(blockNum);

        return ackPacket;
    }

    public Packet getDataPack(short packetSize, short blockNum, byte[] data){

        Packet dataPacket = new Packet();
        dataPacket.setOpcode(Operations.DATA.getValue());
        dataPacket.setPacketSize(packetSize);
        dataPacket.setBlockNumber(blockNum);
        dataPacket.setData(data);

        return dataPacket;
    }




    @Override
    public boolean shouldTerminate() {
        // TODO implement this
        return shouldTerminate;
    } 


    
}

