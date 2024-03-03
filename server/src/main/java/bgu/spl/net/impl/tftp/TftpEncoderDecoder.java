package bgu.spl.net.impl.tftp;

import java.util.Arrays;

import bgu.spl.net.api.MessageEncoderDecoder;

public class TftpEncoderDecoder implements MessageEncoderDecoder<byte[]> {
    //TODO: Implement here the TFTP encoder and decoder

    private byte[] buffer = new byte[1 << 10]; //start with 1k
    private int bufferCurrentPosition = 0;

    @Override
    public byte[] decodeNextByte(byte nextByte) {
        // TODO: implement this
        
        // check the operation code
        if (bufferCurrentPosition == 2) {
            //byte [] b = new byte []{0 , 10};
            short opcode = ( short ) ((( short ) buffer [0]) << 8 | ( short ) ( buffer [1]) );
            checkOpcode(opcode);
        }

        pushByte(nextByte);
        return null;
    }

    @Override
    public byte[] encode(byte[] message) {
        //TODO: implement this


        return null;
    }

    private void pushByte(byte nextByte) {
        if (len >= bytes.length) {
            bytes = Arrays.copyOf(bytes, len * 2);
        }

        bytes[len++] = nextByte;
    }

    private void checkOpcode(short opcode)
    {
        switch (opcode) {
            case 1: // RRQ: Read request
                
                break;
            case 2: // WRQ: Write request
                
                break;
            case 3: // Data: Data Packet
                
                break;
            case 4: // ACK: Acknowledgment
                
                break;
            case 5: // ERROR
                
                break;
            case 6: // DIRQ: Directory listing request
                
                break;
            case 7: // LOGRQ: Login request
                
                break;
            case 8: // DELRQ: Delete file request
                
                break;
            case 9: // BCAST: Brodcast file added/deleted
                
                break;
            case 10: // DISC: Disconnect
                
                break;
        
            default:
                break;
        }
    }
}

class Operation{
    
    private short[] opcode; 
    private String fileName;
    private short[] packetSize;
    private short[] blockNumber;
    private String data;
    private boolean addedDeleted;
    private short[] errorCode;
    private String errMsg;

    Operation()
    {
        opcode = null;
        fileName = null;
        packetSize = null;
        blockNumber = null;
        data = null;
        addedDeleted = null;
        errorCode = null;
        errMsg = null;
    }

    void DecodeMsg (byte[] msg)
    {
        //int opcode =
    }
} 