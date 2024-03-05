package bgu.spl.net.impl.tftp;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import bgu.spl.net.api.BidiMessagingProtocol;
import bgu.spl.net.srv.Connections;
import bgu.spl.net.impl.tftp.Packet;


class holder{
    static ConcurrentHashMap<Integer, Boolean> ids_login = new ConcurrentHashMap<>();
}



public class TftpProtocol implements BidiMessagingProtocol<Packet>  {

    private boolean shouldTerminate;
    private int connectionId;
    private Connections<Packet> connections;
    //private List<String> users;

    @Override
    public void start(int connectionId, Connections<Packet> connections) {
        // TODO implement this
       this.shouldTerminate = false;
       this.connectionId = connectionId;
       this.connections = connections;
       holder.ids_login.put(connectionId, true);
    }

    @Override
    public void process(Packet packet) {
        // TODO implement this

        short opcode = packet.getOpcode();
        
        // happens when DELETE OR ADDING FILE 
        if(opcode == Operations.BCAST.getValue())
        {
            for(Integer id : holder.ids_login.keySet())
            {
                connections.send(id, packet);
            }
        }
        if(opcode == Operations.DELRQ.getValue())
        {

        }
        if(opcode == Operations.LOGRQ.getValue())
        {
            // needs to add userName if it doesn't exist
            
            // if successful send ACK RQ:
            
        }


        
        // after handeling the message - should terminate = true
        shouldTerminate = true;
        
    }

    @Override
    public boolean shouldTerminate() {
        // TODO implement this
        this.connections.disconnect(this.connectionId);
        holder.ids_login.remove(this.connectionId);

        return shouldTerminate;
    } 


    
}
