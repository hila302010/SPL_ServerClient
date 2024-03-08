package bgu.spl.net.impl.tftp;

public enum Operations {
    
    RRQ((short) 1), 
    WRQ((short) 2),
    DATA((short) 3),
    ACK((short) 4),
    ERROR((short) 5),
    DIRQ((short) 6),
    LOGRQ((short) 7),
    DELRQ((short) 8),
    BCAST((short) 9),
    DISC((short) 10);

    private final short value;

    Operations(short value) {
        this.value = value;
    }

    public short getValue() {
        return value;
    }
}