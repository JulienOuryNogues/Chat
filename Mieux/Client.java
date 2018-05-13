/**
 * Created by Julien on 11/21/2015.
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

/**
 * A Simple Socket client that connects to our socket server
 * @author faheem
 *
 */
public class Client {

    public static void main(String arg[]) {
        final Socket clientSocket;
        final BufferedReader in;
        final PrintWriter out;
        final Scanner sc = new Scanner(System.in);//pour lire � partir du clavier

        try {
         /*
         * les informations du serveur ( port et adresse IP ou nom d'hote
         * 127.0.0.1 est l'adresse local de la machine
         */
            clientSocket = new Socket("127.0.0.1", 9990);

            //flux pour envoyer
            out = new PrintWriter(clientSocket.getOutputStream());
            //flux pour recevoir
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));


            //thread pour envoyer les donnees
            Thread envoyer = new Thread(new Runnable() {
                String msg;
                @Override
                public void run() {
                    while(true){
                        msg = sc.nextLine(); //des qu'il recoit une ligne
                        out.println(msg);
                        out.flush();
                    }
                }
            });
            envoyer.start();


            //thread pour recevoir les messages

            Thread recevoir = new Thread(new Runnable() {
                String msg;
                @Override
                public void run() {
                    try {
                        msg = in.readLine(); //toujours une ligne
                        while(msg!=null){
                            System.out.println(msg);
                            msg = in.readLine();
                        }
                        System.out.println("Disconnected. Press Enter to close.");
                        clientSocket.close();
                        envoyer.stop();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
            recevoir.start();



        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}