package bgu.spl.net.impl.tftp;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import bgu.spl.net.api.MessageEncoderDecoder;
import main.java.bgu.spl.net.impl.tftp.Packet;

enum operations {
    RRQ, 
    WRQ,
    DATA,
    ACK,
    ERROR,
    DIRQ,
    LOGRQ,
    DELRQ,
    BCAST,
    DISC
}

public class TftpEncoderDecoder implements MessageEncoderDecoder<byte[]> {
    //TODO: Implement here the TFTP encoder and decoder

    private static final int MAX_PACKET_SIZE = 1 << 16;

    private byte[] buffer = new byte[MAX_PACKET_SIZE]; 
    private int bufferCurrentPosition = 0;
    Packet p = new Packet();
    short opcode = null;

    @Override
    public T decodeNextByte(byte nextByte) {
        // TODO: implement this
        
        // check the operation code
        if (bufferCurrentPosition == 2) { // 01a0
            opcode = ( short ) ((( short ) buffer [0]) << 8 | ( short ) ( buffer [1]) );
            packet.setOpcode(opcode);

            if(opcode == operations.DISC || opcode==operations.DIRQ){
                return packet;
            }
        }
        else if(bufferCurrentPosition == 4 & opcode == operations.ACK)
        {
            short blockNumber = ( short ) ((( short ) buffer [2]) << 8 | ( short ) ( buffer [3]) );
            packet.setBlockNumber(blockNumber);
            return packet;
        }
        // check if null
        else if( opcode == operations.DATA){
            if(bufferCurrentPosition == 6)
            {
                short packetSize = ( short ) ((( short ) buffer [2]) << 8 | ( short ) ( buffer [3]) );
                short blockNumber = ( short ) ((( short ) buffer [4]) << 8 | ( short ) ( buffer [5]) );
                packet.setPacketSize(packetSize);
                packet.setBlockNumber(blockNumber);
            }
            else if(bufferCurrentPosition == packet.getPacketSize() + 6)
            {
                // read from the buffer all the bytes from 6 to packet.getPacketSize + 6
                // convert the bytes to string
                String data = new String(buffer, 6, packet.getPacketSize(), StandardCharsets.UTF_8);
                packet.setPacketData(data);
                return packet;
            }
        }
        
        if(nextByte == 0 && bufferCurrentPosition >= 2) 
        {
            if(opcode==operations.RRQ || opcode==operations.WRQ || opcode == operations.DELRQ)
            {
                String fileName = new String(buffer, 2, bufferCurrentPosition-2, StandardCharsets.UTF_8);
                packet.setPacketFileName(fileName);
            }
            else if(opcode==operations.ERROR)
            {
                if(bufferCurrentPosition>=4) // in case the error code is 0- don't stop
                {
                    short errorCode = ( short ) ((( short ) buffer [2]) << 8 | ( short ) ( buffer [3]) );
                    string errorMsg = new String(buffer, 4, bufferCurrentPosition-4, StandardCharsets.UTF_8);
                    packet.setErrorCode(errorCode);
                    packet.setErrorMsg(errorMsg);
                }
            }
            else if(opcode == operations.LOGRQ)
            {
                String userName = new String(buffer, 2, bufferCurrentPosition-2, StandardCharsets.UTF_8);
                packet.setPacketUserName(userName);
            }
            else if(opcode == operations.BCAST && bufferCurrentPosition > 2)
            {
                boolean addedDeleted = buffer[2] != 0;
                String fileName = new String(buffer, 3, bufferCurrentPosition-3, StandardCharsets.UTF_8);
                packet.setPacketFileName(fileName);
                packet.setAddedOrDeleted(addedDeleted);
            }
            return packet;
        }

        buffer[bufferCurrentPosition++] = nextByte; // push into the buffer
        return null; // not a packet yet
    }

    @Override
    public byte[] encode(T message) {
        //TODO: implement this
        
        // get the opcode from the message

        return null;
    }
    // מה מני אמר
    // צריך לשנות את הקוד של השרת
    // להגדיר את הממשק של connections
    // 
}