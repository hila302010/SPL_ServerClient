package bgu.spl.net.impl.tftp;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import bgu.spl.net.api.MessageEncoderDecoder;
import bgu.spl.net.impl.tftp.Packet;

import java.nio.ByteBuffer;

public class TftpEncoderDecoder implements MessageEncoderDecoder<Packet> {
    

    //TODO: Implement here the TFTP encoder and decoder

    private static final int MAX_PACKET_SIZE = 1 << 16;
    private byte[] buffer = new byte[MAX_PACKET_SIZE]; 
    private int bufferCurrentPosition = 0;
    Packet packet = new Packet();
    short opcode = 0;
    int encodeIdx;


    @Override
    public Packet decodeNextByte(byte nextByte) {
        // TODO: implement this
        
        // check the operation code
        if (bufferCurrentPosition == 2) { 
            opcode = ( short ) ((( short ) buffer [0] &  0xFF) << 8 | ( short ) ( buffer [1]) &  0xFF);
            packet.setOpcode(opcode);

            if(opcode == Operations.DISC.getValue() || opcode==Operations.DIRQ.getValue()){
                return packet;
            }
        }
        else if(bufferCurrentPosition == 4 & opcode == Operations.ACK.getValue())
        {
            short blockNumber = ( short ) ((( short ) buffer [2] &  0xFF) << 8 | ( short ) ( buffer [3]) &  0xFF);
            packet.setBlockNumber(blockNumber);
            return packet;
        }
        // check if null
        else if( opcode == Operations.DATA.getValue()){
            if(bufferCurrentPosition == 6)
            {
                short packetSize = ( short ) ((( short ) buffer [2] &  0xFF) << 8 | ( short ) ( buffer [3] &  0xFF) );
                short blockNumber = ( short ) ((( short ) buffer [4] &  0xFF) << 8 | ( short ) ( buffer [5] &  0xFF) );
                packet.setPacketSize(packetSize);
                packet.setBlockNumber(blockNumber);
            }
            else if(bufferCurrentPosition == packet.getPacketSize() + 6)
            {
                // read from the buffer all the bytes from 6 to packet.getPacketSize + 6
                // convert the bytes to string
                String data = new String(buffer, 6, packet.getPacketSize(), StandardCharsets.UTF_8);
                packet.setData(data);
                return packet;
            }
        }
        
        if(nextByte == 0 && bufferCurrentPosition >= 2 && opcode!=Operations.DATA.getValue()) 
        {
            if(opcode==Operations.RRQ.getValue() || opcode==Operations.WRQ.getValue() || opcode == Operations.DELRQ.getValue())
            {
                String fileName = new String(buffer, 2, bufferCurrentPosition-2, StandardCharsets.UTF_8);
                packet.setFileName(fileName);
            }
            else if(opcode==Operations.ERROR.getValue())
            {
                if(bufferCurrentPosition>=4) // in case the error code is 0- don't stop
                {
                    short errorCode = ( short ) ((( short ) buffer [2]) << 8 | ( short ) ( buffer [3]) );
                    String errorMsg = new String(buffer, 4, bufferCurrentPosition-4, StandardCharsets.UTF_8);
                    packet.setErrorCode(errorCode);
                    packet.setErrMsg(errorMsg);
                }
            }
            else if(opcode == Operations.LOGRQ.getValue())
            {
                String userName = new String(buffer, 2, bufferCurrentPosition-2, StandardCharsets.UTF_8);
                packet.setUserName(userName);
            }
            else if(opcode == Operations.BCAST.getValue() && bufferCurrentPosition > 2)
            {
                boolean addedDeleted = buffer[2] != 0;
                String fileName = new String(buffer, 3, bufferCurrentPosition-3, StandardCharsets.UTF_8);
                packet.setFileName(fileName);
                packet.setAddedOrDeleted(addedDeleted);
            }
            return packet;
        }

        buffer[bufferCurrentPosition++] = nextByte; // push into the buffer
        return null; // not a packet yet
    }





    @Override
    // CHAT GPT
    public byte[] encode(Packet p) {
        short opcode = p.getOpcode();
        ByteBuffer buffer = ByteBuffer.allocate(MAX_PACKET_SIZE);
        buffer.putShort(opcode);
    
        if (opcode == Operations.DISC.getValue() || opcode == Operations.DIRQ.getValue()) {
            // No additional data for DISC or DIRQ
        } else if (opcode == Operations.ACK.getValue()) {
            buffer.putShort(p.getBlockNumber());
        } else if (opcode == Operations.DATA.getValue()) {
            buffer.putShort(p.getPacketSize());
            buffer.putShort(p.getBlockNumber());
            buffer.put(p.getData().getBytes(StandardCharsets.UTF_8));
        } else if (opcode == Operations.RRQ.getValue() ||
                   opcode == Operations.WRQ.getValue() ||
                   opcode == Operations.DELRQ.getValue()) {
            buffer.put(p.getFileName().getBytes(StandardCharsets.UTF_8));
            buffer.put((byte) 0); // Null terminator
        } else if (opcode == Operations.ERROR.getValue()) {
            buffer.putShort(p.getErrorCode());
            buffer.put(p.getErrMsg().getBytes(StandardCharsets.UTF_8));
            buffer.put((byte) 0); // Null terminator
        } else if (opcode == Operations.LOGRQ.getValue()) {
            buffer.put(p.getUserName().getBytes(StandardCharsets.UTF_8));
            buffer.put((byte) 0); // Null terminator
        } else if (opcode == Operations.BCAST.getValue()) {
            buffer.put((byte) (p.getAddedOrDeleted() ? 1 : 0));
            buffer.put(p.getFileName().getBytes(StandardCharsets.UTF_8));
            buffer.put((byte) 0); // Null terminator
        }
    
        return Arrays.copyOfRange(buffer.array(), 0, buffer.position());
    }

    
    /*public byte[] encode(Packet p) {
        
        byte[] output;
        int currIndex = 0;

        short opcode = p.getOpcode();
        byte[] opcodeByte = shortToByte(opcode);

        // separate to the different scenarios
        if(opcode == Operations.DISC.getValue() || opcode==Operations.DIRQ.getValue()){
            return opcodeByte;
        }


        else if(opcode == Operations.ACK.getValue()){

            output = new byte[4];
            byte[] blockNumberByte = shortToByte(p.getBlockNumber());

            mergeByteArr(output, opcodeByte);
            mergeByteArr(output, blockNumberByte);

            return output;
        }


        else if( opcode == Operations.DATA.getValue()){

            // if the data is more than 512 bytes, we need to send few blocks
            //-------------to complete---------------

            byte[] packetSizeByte = shortToByte(p.getPacketSize());
            byte[] blockNumberByte = shortToByte(p.getBlockNumber());
            byte[] dataByte = stringToByte(p.getData());
            int pSize = 6 + dataByte.length;
            output = new byte[pSize];

            mergeByteArr(output, opcodeByte);
            mergeByteArr(output, packetSizeByte);
            mergeByteArr(output, blockNumberByte);
            mergeByteArr(output, dataByte);

            return output;
        }


        else if(opcode == Operations.RRQ.getValue() || opcode == Operations.WRQ.getValue() || opcode == Operations.DELRQ.getValue()){
            
            byte[] fileNameByte = stringToByte(p.getFileName());
            int pSize = 3 + fileNameByte.length;
            output = new byte[pSize];
            
            mergeByteArr(output, opcodeByte);
            mergeByteArr(output, fileNameByte);
            output[pSize-1] = 0; // The packet ends whis byte 0

            return output;
        }


        else if(opcode == Operations.ERROR.getValue()){
            
            byte[] errCodeByte = shortToByte(p.getErrorCode());
            byte[] errMsgByte = stringToByte(p.getErrMsg());
            int pSize = 5 + errMsgByte.length;
            output = new byte[pSize];
            
            mergeByteArr(output, opcodeByte);
            mergeByteArr(output, errCodeByte);
            mergeByteArr(output, errMsgByte);
            output[pSize-1] = 0; // The packet ends whis byte 0

            return output;
        }


        else if(opcode == Operations.LOGRQ.getValue()){

            byte[] userNameByte = stringToByte(p.getUserName());
            int pSize = 3 + userNameByte.length;
            output = new byte[pSize];
            
            mergeByteArr(output, opcodeByte);
            mergeByteArr(output, userNameByte);
            output[pSize-1] = 0; // The packet ends whis byte 0

            return output;
        }


        else if(opcode == Operations.BCAST.getValue()){
            
            byte[] blockNumberByte = booleanToByte(p.getAddedOrDeleted());
            byte[] fileNameByte = stringToByte(p.getFileName());
            int pSize = 4 + fileNameByte.length;
            output = new byte[pSize];

            mergeByteArr(output, opcodeByte);
            mergeByteArr(output, blockNumberByte);
            mergeByteArr(output, fileNameByte);
            output[pSize-1] = 0; // The packet ends whis byte 0

            return output;
        }


        return null;
    }*/
    
    
    public byte[] mergeByteArr(byte[] baseArr, byte[] arrToAdd) {
        
        for(byte b: arrToAdd){
            baseArr[encodeIdx] = b;
            encodeIdx++;
        }
        return baseArr;
    }

    public byte[] stringToByte(String s){
        return s.getBytes();
    }

    public byte[] shortToByte(Short s){
        return (new byte[]{(byte)(s >> 8), (byte)(s & 0xff)});
    }

    public byte[] booleanToByte(boolean value) {
        byte[] byteArray = new byte[1];
        byteArray[0] = (byte) (value ? 1 : 0);
        return byteArray;
    }
    
}

 
 