package bgu.spl.net.impl.tftp;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import bgu.spl.net.api.BidiMessagingProtocol;
import bgu.spl.net.srv.Connections;
import bgu.spl.net.impl.tftp.Packet;



public class TftpProtocol implements BidiMessagingProtocol<Packet>  {

    private boolean shouldTerminate;
    private int connectionId;
    private Connections<Packet> connections;
    private static final String FILES_FOLDER_PATH = "./server/Files";
    private File filesFolder;
    


    //private List<String> users;

    @Override
    public void start(int connectionId, Connections<Packet> connections) {
        // TODO implement this
       this.shouldTerminate = false;
       this.connectionId = connectionId;
       this.connections = connections;
       filesFolder = new File(FILES_FOLDER_PATH);
       Holder.ids_login.put(connectionId, true);
    }


    @Override
    public void process(Packet packet) {
        // TODO implement this

        short opcode = packet.getOpcode();

        // -------------Check if the user is connected, if he isnt every action exept logrq result an error--------------------------
        

        // WRQ
        if(opcode == Operations.WRQ.getValue()){

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
                    byte[] data = packet.getData().getBytes(StandardCharsets.UTF_8);
                    fos.write(data);
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

        


        
        
        // RRQ
        if(opcode == Operations.RRQ.getValue()){
            String filename = packet.getFileName();
        }
        
        // happens when DELETE OR ADDING FILE 
        if(opcode == Operations.BCAST.getValue())
        {
            for(Integer id : Holder.ids_login.keySet())
            {
                connections.send(id, packet);
            }
        }
        if(opcode == Operations.DELRQ.getValue())
        {

        }




        // LOGRQ
        if(opcode == Operations.LOGRQ.getValue()){

            String userName = packet.getUserName();
            Boolean isExist = false;

            // needs to add userName if it doesn't exist
            for(String name: UserNames.user_names.keySet()){

                //in case name already exists
                if(name == userName){
                    Packet errorPacket = getErrPack((short)(7), "User already exists");
                    isExist = true;
                    connections.send(connectionId, errorPacket);
                }
            }
            // if successful send ACK RQ:
            if(!isExist){
                UserNames.user_names.put(userName, true);
                Packet ackPacket = getAckPack((short)0);
                connections.send(connectionId, ackPacket);
            }
        }


        // DELRQ
        if(opcode == Operations.DELRQ.getValue()){

            
        }




        // DISC
        if(opcode == Operations.DISC.getValue()){

             UserNames.user_names.remove(packet.getUserName());
             shouldTerminate = true;
             Packet ackPacket = getAckPack((short)0);
             connections.send(connectionId, ackPacket);

        }










        
        // after handeling the message - should terminate = true
        shouldTerminate = true;
        
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



    @Override
    public boolean shouldTerminate() {
        // TODO implement this
        this.connections.disconnect(this.connectionId);
        Holder.ids_login.remove(this.connectionId);

        return shouldTerminate;
    } 


    
}

