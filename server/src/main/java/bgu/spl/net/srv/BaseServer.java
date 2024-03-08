package bgu.spl.net.srv;

import bgu.spl.net.impl.tftp.TftpEncoderDecoder;
import bgu.spl.net.impl.tftp.TftpProtocol;
import bgu.spl.net.impl.tftp.Packet;
import bgu.spl.net.impl.tftp.TftpConnections;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.function.Supplier;

public abstract class BaseServer<T> implements Server<T> {

    private final int port;
    private final Supplier<TftpProtocol> protocolFactory;
    private final Supplier<TftpEncoderDecoder> encdecFactory;
    private ServerSocket sock;
    private final TftpConnections<T> connections;
    private int nextConnectionId = 0;

    public BaseServer(
            int port,
            Supplier<TftpProtocol> protocolFactory,
            Supplier<TftpEncoderDecoder> encdecFactory){

        this.port = port;
        this.protocolFactory = protocolFactory;
        this.encdecFactory = encdecFactory;
		this.sock = null;
        connections = new TftpConnections<>();
    }

    @Override
    public void serve() {

        try (ServerSocket serverSock = new ServerSocket(port)) {
			System.out.println("Server started");

            this.sock = serverSock; //just to be able to close

            while (!Thread.currentThread().isInterrupted()) {

                Socket clientSock = serverSock.accept();

                
                BlockingConnectionHandler<T> handler = new BlockingConnectionHandler<>(
                        clientSock,
                        encdecFactory.get(),
                        protocolFactory.get(),
                        nextConnectionId,
                        connections);

                connections.connect(nextConnectionId, handler);
                execute(handler);
                nextConnectionId++;
            }
        } catch (IOException ex) {}

        System.out.println("server closed!!!");
    }

    @Override
    public void close() throws IOException {
		if (sock != null)
			sock.close();
    }

    protected abstract void execute(BlockingConnectionHandler<T>  handler);

}
