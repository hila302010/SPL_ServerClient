package bgu.spl.net.impl.tftp;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;


public class TftpClient {
    //TODO: implement the main logic of the client, when using a thread per client the main logic goes here
    public static void main(String[] args) throws IOException {

        // should we handle the packets with a blocking queue?
        BlockingQueue<Packet> packetsToSend = new LinkedBlockingQueue<>();

        Listening listening = new Listening();
        Thread listeningThread = new Thread(listening);

        KeyBoard keyBoard = new KeyBoard(listening);
        Thread keyBoardThread = new Thread(keyBoard);

        listeningThread.start();
        keyBoardThread.start();

        // Wait for listening thread to finish
        try{
            listeningThread.join();
        }catch(InterruptedException e){}

        // Check if the server connection failed
        if (!listening.isServerConnected()) {
            System.out.println("Server connection failed. Exiting...");
            System.exit(1);
        }
    }
}
