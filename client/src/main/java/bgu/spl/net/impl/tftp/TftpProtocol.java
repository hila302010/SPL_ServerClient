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
            shouldTerminate = true;
        }
        if(opcode == Operations.ERROR.getValue())
        {
            shouldTerminate = true;
        }
        if(opcode == Operations.DATA.getValue())
        {
            
        }
        if(opcode == Operations.ACK.getValue())
        {
            
        }

        // no response is expected
        return null;
    }

    public void SetShouldTerminate(boolean shouldTerminate)
    {
        this.shouldTerminate = shouldTerminate;
    }

    @Override
    public boolean shouldTerminate() {
        // TODO implement this
        return shouldTerminate;
    } 


    
}

