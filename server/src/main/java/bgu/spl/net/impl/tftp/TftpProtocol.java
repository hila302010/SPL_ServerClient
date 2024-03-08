package bgu.spl.net.impl.tftp;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
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
    private static final String FILES_FOLDER_PATH = "./Files";
    private File filesFolder;

    private String currFileNameWRQ;
    private final Short MAX_PACKET_SIZE = (short)512;

    private List<byte[]> fileChunksRRQ;


    @Override
    public void start(int connectionId, Connections<Packet> connections) {
        // TODO implement this
       this.shouldTerminate = false;
       this.connectionId = connectionId;
       this.connections =  (TftpConnections<Packet>) connections;
       filesFolder = new File(FILES_FOLDER_PATH);

       currFileNameWRQ = null;

       fileChunksRRQ = null;
    }


    @Override
    public void process(Packet packet) {
        // TODO implement this

        short opcode = packet.getOpcode();

        // Check if the user is connected, if he isnt every action exept logrq and disconnect result an error
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

            // ACK
            if(opcode == Operations.ACK.getValue())
              ackReq(packet);

            // LOGRQ
            if(opcode == Operations.LOGRQ.getValue())
                loginReq(packet);
            
            // DELRQ
            if(opcode == Operations.DELRQ.getValue())
                deleteReq(packet);

                //// how do we handle this?????
            if(opcode == Operations.BCAST.getValue())
            {
                
            }
            if(opcode == Operations.ERROR.getValue())
            {
                
            }
            //DATA
            if(opcode == Operations.DATA.getValue())
                dataReq(packet);

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
      // WRQ
        String fileName = packet.getFileName();
        boolean fileExists = checkIfFileInFolder(fileName,filesFolder);

        if (fileExists) {

            Packet errorPacket = getErrPack((short)(5), "File already exists");
            connections.send(connectionId, errorPacket);

        } else {
            this.currFileNameWRQ = fileName;

            Packet ackPacket = getAckPack((short)0);
            connections.send(connectionId, ackPacket);
        }
    }

    public void ackReq(Packet packet)
    {
        if (fileChunksRRQ != null) {
            Short blockNum = packet.getBlockNumber();
            Short newBlockNum = (short) ((int)blockNum + 1);

    
            if (blockNum < fileChunksRRQ.size()) {
                byte[] nextChunk = fileChunksRRQ.get(blockNum);
                if (nextChunk != null) {
                    Packet dataPack = getDataPack((short) nextChunk.length, newBlockNum, nextChunk);
                    connections.send(connectionId, dataPack);
                }
            }
        }
    }

    public void dataReq(Packet packet)
    {
        if(this.currFileNameWRQ != null)
        {

            short packetSize = packet.getPacketSize();
            short blockNumber = packet.getBlockNumber();
            byte[] data = packet.getData();

            try{
                FileOutputStream fos = new FileOutputStream(FILES_FOLDER_PATH + "/" + this.currFileNameWRQ, true);

                fos.write(data, 0, (int)packetSize);

                fos.close();
            }
            catch (IOException e){};

            Packet ackPacket = getAckPack(blockNumber);
            connections.send(connectionId, ackPacket);

            if(packetSize < MAX_PACKET_SIZE){
               
                this.currFileNameWRQ = null;
            }
        }
    }
  
    public void readReq(Packet packet)
    {
        String fileName = packet.getFileName();

        // Check if the file exists in the Files folder
        File file = new File(FILES_FOLDER_PATH + "/" + fileName);

        if (!file.exists()) { //If the file doesn't exists we will send en error packet
            Packet errorPacket = getErrPack((short) 1, "File not found");
            connections.send(connectionId, errorPacket);
            return;

        } else { //If the file exists - we read the it into chunks of 512 bytes 
            //and store it in fileChunksRRQ field
            try (FileInputStream fis = new FileInputStream(file)) {
                fileChunksRRQ = new ArrayList<>();
                byte[] buffer = new byte[512];
                int bytesRead = 0;
                while ((bytesRead = fis.read(buffer)) != -1) {
                    byte[] chunk = new byte[bytesRead];
                    System.arraycopy(buffer, 0, chunk, 0, bytesRead);
                    fileChunksRRQ.add(chunk);
                }
                //Send the first chunk
                byte[] dataToSend = fileChunksRRQ.get(0);

                if(dataToSend == null || dataToSend.length == 0){
                    short zero = 0;
                    byte[] empty = new byte[0];
                    Packet dataPack = getDataPack(zero ,zero, empty);
                    fileChunksRRQ = null;
                    connections.send(connectionId, dataPack);
                }
                else{ //sent the first chunt in the list as block 1
                    Packet dataPack = getDataPack((short)dataToSend.length, (short)1, dataToSend);
                    if(fileChunksRRQ.size() == 1){
                        fileChunksRRQ = null;
                    }
                    connections.send(connectionId, dataPack);
                }
                


            } catch (IOException e) {
                // Error reading the file, send an error packet
                Packet errorPacket = getErrPack((short) 2, "Error reading file");
                connections.send(connectionId, errorPacket);
            }
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
                broadcastPacket.setAddedOrDeleted(false);
                
                // send broadcast to all logged in users
                for (Integer id : connections.user_names.keySet()) {
                    connections.connectionHandlers.get(id).send(broadcastPacket);
                }
                // send ACK packet to client
                Packet ackPacket = getAckPack((short)0);
                connections.send(connectionId, ackPacket);

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
                Packet errorPacket = getErrPack((short)(7), "The name " + userName + " is Already exists");
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


    public Packet getDataPack(short packetSize,short blockNumber, byte[] data){

        Packet dataPacket = new Packet();
        dataPacket.setOpcode(Operations.DATA.getValue());
        dataPacket.setPacketSize(packetSize);;
        dataPacket.setBlockNumber(blockNumber);
        dataPacket.setData(data); 

        return dataPacket;
    }



    @Override
    public boolean shouldTerminate() {
        // TODO implement this
        return shouldTerminate;
    } 


    
}

