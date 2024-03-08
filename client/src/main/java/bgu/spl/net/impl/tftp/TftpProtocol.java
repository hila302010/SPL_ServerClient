package bgu.spl.net.impl.tftp;


import bgu.spl.net.api.MessagingProtocol;



public class TftpProtocol implements MessagingProtocol<Packet>  {

    private boolean shouldTerminate;
    


    @Override
    public Packet process(Packet packet) {
        // TODO implement this
        



        // no response is expected
        return null;

        
    }
    @Override
    public boolean shouldTerminate() {
        // TODO implement this
        return shouldTerminate;
    } 


    
}

