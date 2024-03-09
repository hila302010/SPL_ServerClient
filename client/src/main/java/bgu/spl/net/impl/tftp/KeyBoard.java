package bgu.spl.net.impl.tftp;

import java.io.IOException;
import java.util.Scanner;

public class KeyBoard implements Runnable{
    boolean shouldTerminate = false;
    private Listening listening;

    public KeyBoard(Listening listening) {
        this.listening = listening;
    }


    @Override
    public void run() {
        try (Scanner scanner = new Scanner(System.in)){
            while (!shouldTerminate) {
                String response = scanner.nextLine();
                Packet packet = checkInput(response);

                // how to send the packet to the server???
                if(packet!=null)
                {
                    listening.send(packet);
                    try{
                        listening.waitForServer.wait(); // wait for listening thread to notiffy that the server finished handeling request
                    }catch(InterruptedException e){}

                }

            }
        } 
    }



    public Packet checkInput(String response)
    {
        Packet packet = new Packet();

        // Split the string by spaces
        String[] words = response.split("\\s+");
        
        if(words.length==2 && words[0].compareTo("LOGRQ") == 0)
        {
            packet.setOpcode(Operations.LOGRQ.getValue());
            packet.setUserName(words[1]);
        }
        else if(words.length==2 && words[0].compareTo("RRQ") == 0)
        {
            packet.setOpcode(Operations.RRQ.getValue());
            packet.setFileName(words[1]);
        }
        else if(words.length==2 && words[0].compareTo("WRQ") == 0)
        {
            packet.setOpcode(Operations.WRQ.getValue());
            packet.setFileName(words[1]);
        }
        else if(words.length==1 &&words[0].compareTo("DIRQ") == 0)
        {
            packet.setOpcode(Operations.DIRQ.getValue());
        }
        else if(words.length==2 && words[0].compareTo("DELRQ") == 0)
        {
            packet.setOpcode(Operations.DELRQ.getValue());
            packet.setFileName(words[1]);
        }
        else if(words.length==1 && words[0].compareTo("DISC") == 0)
        {
            packet.setOpcode(Operations.DISC.getValue());
        }
        else
        {
            System.out.println("Invalid input");
            return null;
        }

        return packet;
    }
}
