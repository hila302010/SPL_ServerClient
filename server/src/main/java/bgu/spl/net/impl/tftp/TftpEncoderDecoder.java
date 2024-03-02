package bgu.spl.net.impl.tftp;

import bgu.spl.net.api.MessageEncoderDecoder;

public class TftpEncoderDecoder implements MessageEncoderDecoder<byte[]> {
    //TODO: Implement here the TFTP encoder and decoder


    @Override
    public byte[] decodeNextByte(byte nextByte) {
        // TODO: implement this

        return null;
    }

    @Override
    public byte[] encode(byte[] message) {
        //TODO: implement this
        return null;
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
} 