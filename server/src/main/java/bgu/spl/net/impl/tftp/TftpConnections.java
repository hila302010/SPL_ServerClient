package bgu.spl.net.impl.tftp;

import bgu.spl.net.impl.tftp.TftpEncoderDecoder;
import bgu.spl.net.srv.ConnectionHandler;
import bgu.spl.net.srv.Connections;

import java.util.concurrent.ConcurrentHashMap;



class Holder{
    static ConcurrentHashMap<Integer, Boolean> ids_login = new ConcurrentHashMap<>();
}

class UserNames{
    static ConcurrentHashMap<String, Boolean> user_names = new ConcurrentHashMap<>();
}


public class TftpConnections implements Connections<Packet> {
    

    @Override
    public void connect(int connectionId, ConnectionHandler<Packet> handler) {
        // TODO: Implement connection handling
    }

    @Override
    public boolean send(int connectionId, Packet msg) {
        // TODO: Implement sending message to the specified connection
        return false; // Placeholder return
    }

    @Override
    public void disconnect(int connectionId) {
        // TODO: Implement disconnection handling
        //מיפוי ID מול קונקשן הנדלר
        // להוריד מהמיפוי
    }
}
