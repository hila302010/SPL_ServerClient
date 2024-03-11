package bgu.spl.net.impl.tftp;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import bgu.spl.net.api.MessagingProtocol;

import bgu.spl.net.impl.tftp.KeyBoard;



public class TftpProtocol implements MessagingProtocol<Packet>  {

    private boolean shouldTerminate;
    private static final String FILES_FOLDER_PATH = "./Files";
    private File filesFolder;

    private String currFileNameWRQ;
    private final Short MAX_PACKET_SIZE = (short)512;

    private List<byte[]> fileChunksRRQ;
    

    @Override
    public Packet process(Packet packet) {
        // TODO implement this
        short opcode = packet.getOpcode();

        if(opcode == Operations.BCAST.getValue())
        {
            String status = "deleted";
            if(packet.getAddedOrDeleted()){
                status = "added";
            }
            System.out.println("Handling BCAST");
            System.out.println("The file " + packet.getFileName() + " has been " + status);
            System.out.println();
        }

        if(opcode == Operations.ERROR.getValue())
        {
            System.out.println(packet.getErrMsg());
        }

        if(opcode == Operations.DATA.getValue()){
            if(KeyBoard.currFileNameRRQclient != null){

                // it's RRQ 
                short packetSize = packet.getPacketSize();
                short blockNumber = packet.getBlockNumber();
                byte[] data = packet.getData();

                System.out.println("Received: block " + blockNumber + " size " + packetSize + " data " + Arrays.toString(data));
                System.out.println("Handling DATA");
                
                try{
                    FileOutputStream fos = new FileOutputStream("./" + KeyBoard.currFileNameRRQclient, true);

                    fos.write(data, 0, (int)packetSize);

                    fos.close();
                }
                catch (IOException e){};

                Packet ackPacket = getAckPack(blockNumber);

                if(packetSize < MAX_PACKET_SIZE){
                    
                    System.out.println("RRQ " + KeyBoard.currFileNameRRQclient + " completed");
                    KeyBoard.currFileNameRRQclient = null;
                }
                else{
                    return ackPacket;
                }
            }
            else{
                //it's DIRQ 
                String files = new String(packet.getData(), 0, packet.getData().length, StandardCharsets.UTF_8);
                for(String file : files.split("\0"))
                {
                    System.out.println(file);
                }
                
            }
            
        }


        if(opcode == Operations.ACK.getValue())
        {

            System.out.println("Received: opcode " + packet.getOpcode() + " blockNum " + packet.getBlockNumber());
            System.out.println("Handling ACK");
            Short blockNum = packet.getBlockNumber();
            Short newBlockNum = (short) ((int)blockNum + 1);

            if (KeyBoard.fileChunksWRQclient != null) {

                // In case the all the blocks already passed we reset this fields
                if ((blockNum >= KeyBoard.fileChunksWRQclient.size())) {
                    System.out.println("WRQ " + KeyBoard.currFileNameWRQclient  + " completed");
                    KeyBoard.fileChunksWRQclient = null;
                    KeyBoard.currFileNameWRQclient = null;
                }
            
                else if (blockNum < KeyBoard.fileChunksWRQclient.size()) {
                    byte[] nextChunk = KeyBoard.fileChunksWRQclient.get(blockNum);
                    if (nextChunk != null) {
                        Packet dataPack = getDataPack((short) nextChunk.length, newBlockNum, nextChunk);
                        return dataPack;
                    }
                }
            }
            
            // sending ACK in RRQ
        }


        // no response is expected
        return null;
    }

    public void SetShouldTerminate(boolean shouldTerminate)
    {
        this.shouldTerminate = shouldTerminate;
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

