package main.java.bgu.spl.net.impl.tftp;

import bgu.spl.net.srv.ConnectionHandler;
import bgu.spl.net.srv.Connections;


public class Connections implements Connections<byte[]> {
    void connect(int connectionId, ConnectionHandler<T> handler)
    {
        //TODO
    }

    boolean send(int connectionId, T msg)
    {
        //TODO
    }

    void disconnect(int connectionId)
    {
        //TODO
    }
}
