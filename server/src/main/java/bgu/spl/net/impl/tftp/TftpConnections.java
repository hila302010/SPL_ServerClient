package bgu.spl.net.impl.tftp;

import bgu.spl.net.srv.ConnectionHandler;
import bgu.spl.net.srv.Connections;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

class Holder{
    static ConcurrentHashMap<Integer, Boolean> ids_login = new ConcurrentHashMap<>();
}

class UserNames{
    static ConcurrentHashMap<String, Boolean> user_names = new ConcurrentHashMap<>();
}


public class TftpConnections<T> implements Connections<T> {
    
    private Map<Integer, ConnectionHandler<T>> connectionHandlers = new ConcurrentHashMap<>();
    
    @Override
    public void connect(int connectionId, ConnectionHandler<T> handler) {
        // TODO: Implement connection handling
        connectionHandlers.put(connectionId, handler);
    }

    @Override
    public boolean send(int connectionId, T msg) {
        // TODO: Implement sending message to the specified connection
        ConnectionHandler<T> handler = connectionHandlers.get(connectionId);
        if (handler != null) {
            handler.send(msg);
            return true;
        }
        return false; // Placeholder return
    }

    @Override
    public void disconnect(int connectionId) {
        // TODO: Implement disconnection handling
        //מיפוי ID מול קונקשן הנדלר
        // להוריד מהמיפוי
        connectionHandlers.remove(connectionId);
        
    }
}
