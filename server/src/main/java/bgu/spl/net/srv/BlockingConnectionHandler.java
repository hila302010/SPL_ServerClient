package bgu.spl.net.srv;

import bgu.spl.net.impl.tftp.Packet;
import bgu.spl.net.impl.tftp.TftpEncoderDecoder;
import bgu.spl.net.impl.tftp.TftpProtocol;
import bgu.spl.net.impl.tftp.TftpConnections;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;

public class BlockingConnectionHandler<T> implements Runnable, ConnectionHandler<T> {

    private final TftpProtocol protocol;
    private final TftpEncoderDecoder encdec;
    private final TftpConnections<T> connections;
    private final Socket sock;
    private BufferedInputStream in;
    private BufferedOutputStream out;
    private volatile boolean connected = true;
    private int connectionId;

    public BlockingConnectionHandler(Socket sock, TftpEncoderDecoder reader, TftpProtocol protocol, TftpConnections<T> connections, int connectionId) {
        this.sock = sock;
        this.encdec = reader;
        this.protocol = protocol;
        this.connections = connections;
        this.connectionId = connectionId;
    }

    @Override
    public void run() {
        try (Socket sock = this.sock) { //just for automatic closing
            int read;

            in = new BufferedInputStream(sock.getInputStream());
            out = new BufferedOutputStream(sock.getOutputStream());

            connections.connect(connectionId, this);
            protocol.start(connectionId, (TftpConnections<Packet>)connections);
            while (!protocol.shouldTerminate() && connected && (read = in.read()) >= 0) {
                Packet nextMessage = encdec.decodeNextByte((byte) read);
                if (nextMessage != null) {
                    protocol.process((Packet) nextMessage);
                }
                
            }

        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }

    @Override
    public void close() throws IOException {
        connected = false;
        sock.close();
    }

    @Override
    public void send(T msg) {
        try{
            if (msg != null) {
                out.write(encdec.encode((Packet) msg));
                out.flush();
            }
        }catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
