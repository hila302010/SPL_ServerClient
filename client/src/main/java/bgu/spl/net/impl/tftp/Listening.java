package bgu.spl.net.impl.tftp;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

import java.util.Scanner;

public class Listening implements Runnable{
    TftpEncoderDecoder encdec = new TftpEncoderDecoder();
    TftpProtocol protocol = new TftpProtocol();
    private BufferedInputStream in;
    private BufferedOutputStream out;
    private boolean serverConnected = false;
    boolean shouldTerminate = false;
    public Object waitForServer = new Object();


    @Override
    public void run() {
        //BufferedReader and BufferedWriter automatically using UTF-8 encoding

        try (Socket sock = new Socket("localhost", 7777)){
            System.out.println("Connected to Server!");

            serverConnected = true;
            int read;

            in = new BufferedInputStream(sock.getInputStream());
            out = new BufferedOutputStream(sock.getOutputStream());       

            while ((read = in.read()) >= 0 && !shouldTerminate)  {
                Packet nextMessage = encdec.decodeNextByte((byte) read);
                if (nextMessage != null) {
                    Packet packetToSend = protocol.process((Packet) nextMessage);
                    encdec.reset();
                    if(packetToSend != null)
                        send(packetToSend);
                    else{
                        synchronized(waitForServer){
                            waitForServer.notify();
                        }
                    }
                }
            }
        }
        catch(Exception e){System.out.println(e);}
    }

    public void send(Packet msg) {
        try{
            if (msg != null) {
                out.write(encdec.encode((Packet) msg));
                out.flush();
            }
        }catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public boolean isServerConnected() {
        return serverConnected;
    }
    public boolean shouldTerminate() {
        return shouldTerminate;
    }
}
