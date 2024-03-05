package main.java.bgu.spl.net.impl.tftp;

public class Packet {
    private short opcode; 
    private String fileName;
    private String userName;
    private short packetSize;
    private short blockNumber;
    private String data;
    private boolean addedOrDeleted;
    private short errorCode;
    private String errMsg;

    public Packet() {
        opcode = 0;
        fileName = null;
        userName = null;
        packetSize = 0;
        blockNumber = 0;
        data = null;
        addedOrDeleted = false;
        errorCode = 0;
        errMsg = null;
    }

    // Setters
    public void setOpcode(short opcode) {
        this.opcode = opcode;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setPacketSize(short packetSize) {
        this.packetSize = packetSize;
    }

    public void setBlockNumber(short blockNumber) {
        this.blockNumber = blockNumber;
    }

    public void setData(String data) {
        this.data = data;
    }

    public void setAddedOrDeleted(boolean addedOrDeleted) {
        this.addedOrDeleted = addedOrDeleted;
    }

    public void setErrorCode(short errorCode) {
        this.errorCode = errorCode;
    }

    public void setErrMsg(String errMsg) {
        this.errMsg = errMsg;
    }

    // Getters
    public short getOpcode() {
        return opcode;
    }

    public String getFileName() {
        return fileName;
    }

    public String getUserName() {
        return userName;
    }

    public short getPacketSize() {
        return packetSize;
    }

    public short getBlockNumber() {
        return blockNumber;
    }

    public String getData() {
        return data;
    }

    public boolean getAddedOrDeleted() {
        return addedOrDeleted;
    }

    public short getErrorCode() {
        return errorCode;
    }

    public String getErrMsg() {
        return errMsg;
    }
}
