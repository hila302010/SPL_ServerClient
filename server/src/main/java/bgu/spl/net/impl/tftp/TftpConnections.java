package bgu.spl.net.impl.tftp;

import bgu.spl.net.srv.ConnectionHandler;
import bgu.spl.net.srv.Connections;

public class TftpConnections implements Connections<byte[]> {

    @Override
    public void connect(int connectionId, ConnectionHandler<byte[]> handler) {
        // TODO: Implement connection handling
    }

    @Override
    public boolean send(int connectionId, byte[] msg) {
        // TODO: Implement sending message to the specified connection
        return false; // Placeholder return
    }

    @Override
    public void disconnect(int connectionId) {
        // TODO: Implement disconnection handling
    }
}
