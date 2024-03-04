package main.java.bgu.spl.net.impl.tftp;

public class Packet {
    private short[] opcode; 
    private String fileName;
    private short[] packetSize;
    private short[] blockNumber;
    private String data;
    private boolean addedDeleted;
    private short[] errorCode;
    private String errMsg;

    Pubic Packet()
    {
        _opcode = null;
        _fileName = null;
        _packetSize = null;
        _blockNumber = null;
        _data = null;
        _addedDeleted = null;
        _errorCode = null;
        _errMsg = null;
    }

    // implement setters and getters

    
    
}
