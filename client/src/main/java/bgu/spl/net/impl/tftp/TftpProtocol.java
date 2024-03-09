package bgu.spl.net.impl.tftp;


import bgu.spl.net.api.MessagingProtocol;



public class TftpProtocol implements MessagingProtocol<Packet>  {

    private boolean shouldTerminate;

    @Override
    public Packet process(Packet packet) {
        // TODO implement this
        short opcode = packet.getOpcode();

        if(opcode == Operations.BCAST.getValue())
        {
            System.out.println();
            
        }
        if(opcode == Operations.ERROR.getValue())
        {
            
        }
        if(opcode == Operations.DATA.getValue())
        {
            // sending data in WRQ
        }
        if(opcode == Operations.ACK.getValue())
        {
            // sending ACK in RRQ
        }


        // no response is expected
        return null;
    }

    @Override
    public boolean shouldTerminate() {
        // TODO implement this
        return shouldTerminate;
    } 


    
}

