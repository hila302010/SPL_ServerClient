package bgu.spl.net.impl.tftp;

import bgu.spl.net.srv.BlockingConnectionHandler;
import bgu.spl.net.srv.ConnectionHandler;
import bgu.spl.net.srv.Connections;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;



public class TftpConnections<T> implements Connections<T> {
    
    public ConcurrentHashMap<Integer, ConnectionHandler<T>> connectionHandlers = new ConcurrentHashMap<>();
    public ConcurrentHashMap<Integer, String> user_names = new ConcurrentHashMap<>();

    
    @Override
    public void connect(int connectionId, ConnectionHandler<T> handler) {
        // TODO: Implement connection handling
        connectionHandlers.put(connectionId, handler);
    }

    @Override
    public boolean send(int connectionId, T msg) {
        // TODO: Implement sending message to the specified connection
        BlockingConnectionHandler<T> handler = (BlockingConnectionHandler<T>) connectionHandlers.get(connectionId);
        if (handler != null) {
            try
            {
                Boolean isLocked = handler.sem.tryAcquire();
                if(isLocked){
                    handler.send(msg);
                    handler.sem.release();
                }
                else{
                    handler.packetQueue.offer(msg);
                }
                return true;
            }
            catch(Exception e){}
        }
        return false; // Placeholder return
    }

    @Override
    public void disconnect(int connectionId) {
        // TODO: Implement disconnection handling
        connectionHandlers.remove(connectionId);
        if(user_names.containsKey(connectionId))
            user_names.remove(connectionId);
        
    }
}
