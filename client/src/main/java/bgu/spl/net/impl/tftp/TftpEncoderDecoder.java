package bgu.spl.net.impl.tftp;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import bgu.spl.net.api.MessageEncoderDecoder;

import java.nio.ByteBuffer;

public class TftpEncoderDecoder implements MessageEncoderDecoder<Packet> {
    

    //TODO: Implement here the TFTP encoder and decoder

    private static final int MAX_PACKET_SIZE = 1 << 16;
    private byte[] buffer = new byte[MAX_PACKET_SIZE]; 
    private int bufferCurrentPosition = 0;
    Packet packet = new Packet();
    short opcode = 0;


    @Override
    public Packet decodeNextByte(byte nextByte) {
        // TODO: implement this

        
        buffer[bufferCurrentPosition++] = nextByte; // push into the buffer
        
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
                byte[] data = Arrays.copyOfRange(buffer, 6, 6 + packet.getPacketSize());;
                packet.setData(data);
                return packet;
            }
        }
        
        if(nextByte == 0 && bufferCurrentPosition >= 2) 
        {
            if(opcode==Operations.RRQ.getValue() || opcode==Operations.WRQ.getValue() || opcode == Operations.DELRQ.getValue())
            {
                String fileName = new String(buffer, 2, bufferCurrentPosition-3, StandardCharsets.UTF_8);
                packet.setFileName(fileName);
                return packet;
            }
            else if(opcode == Operations.LOGRQ.getValue())
            {
                String userName = new String(buffer, 2, bufferCurrentPosition-3, StandardCharsets.UTF_8);
                packet.setUserName(userName);
                return packet;
            }
        }
        if(nextByte == 0 && opcode==Operations.ERROR.getValue())
        {
            if(bufferCurrentPosition>4) // in case the error code is 0- don't stop
            {
                short errorCode = ( short ) ((( short ) buffer [2]) << 8 | ( short ) ( buffer [3]) );
                String errorMsg = new String(buffer, 4, bufferCurrentPosition-5, StandardCharsets.UTF_8);
                packet.setErrorCode(errorCode);
                packet.setErrMsg(errorMsg);
                return packet;
            }
        }
        if(nextByte == 0 && opcode == Operations.BCAST.getValue() && bufferCurrentPosition > 4)
        {
            boolean addedDeleted = (buffer[2] != 0);
            String fileName = new String(buffer, 3, bufferCurrentPosition-4, StandardCharsets.UTF_8);
            packet.setFileName(fileName);
            packet.setAddedOrDeleted(addedDeleted);
            return packet;
        }

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
            buffer.put(p.getData());
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

    public void reset()
    {
        bufferCurrentPosition = 0;
        opcode = 0;
        packet.setOpcode((short)0);
    }
}



 
 