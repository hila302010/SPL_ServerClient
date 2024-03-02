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
    
    private int[] opcode; 
    private String fileName;
    private int[] packetSize;
    private int[] blockNumber;
    private String data;
    private boolean addedDeleted;
    private int[] errorCode;
    private String errMsg;
} 