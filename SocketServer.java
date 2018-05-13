/**
 * Created by Julien on 11/21/2015.
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;
import java.util.Vector;

/**
 * A simple socket server
 * @author faheem
 *
 */
public class SocketServer {

    public ServerSocket serverSocket;
    private int port;



    public SocketServer(int port) {
        this.port = port;
    }



    public static class recevoir implements Runnable {
        BufferedReader in;
        String msg;
        Vector tabclientsout;
        public recevoir(BufferedReader in, Vector tabclientsout) {
            this.in = in;
            this.tabclientsout=tabclientsout;
        }

        public void run() {
            while(true) {
                try {
                    msg = in.readLine();
                    msg = "Client : "+msg;
                    //tant que le client est connecté
                    while (msg != null) {
                        System.out.println(msg);
                        PrintWriter out; // declaration d'une variable permettant l'envoi de texte vers le client
                        for (int i = 0; i < tabclientsout.size(); i++) // parcours de la table
                        {
                            out = (PrintWriter) tabclientsout.elementAt(i); // extraction de l'élément courant (type PrintWriter)
                            if (out != null) // au cas ou
                            {
                                out.print(msg+"\n");
                                out.flush(); // envoi dans le flux de sortie
                            }
                        }
                        msg=in.readLine();
                        msg = "Client : " + msg;


                    }


                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }





    public static void main(String[] args) {

        ServerSocket serveurSocket = null;
        final Scanner sc=new Scanner(System.in);
        Vector tabClientsout = new Vector(); // contiendra les sorties
        final int[] nbClients = {0};
        final Socket[] clientSocket = new Socket[1];
        try {
            serveurSocket = new ServerSocket(9990);
        } catch (IOException e) {
            e.printStackTrace();
        }
        final BufferedReader[] in = new BufferedReader[1];
        final PrintWriter[] out = new PrintWriter[1];


            final ServerSocket finalServeurSocket = serveurSocket;
            Thread Listen = new Thread(new Runnable (){

                public void run() {
                    // thread qui ne fait que rajouter des connexions
                    try {
                        while(true){
                            clientSocket[0] = finalServeurSocket.accept();
                            nbClients[0]++;
                            out[0] = new PrintWriter(clientSocket[0].getOutputStream());
                            in[0] = new BufferedReader (new InputStreamReader (clientSocket[0].getInputStream()));
                            tabClientsout.addElement(out[0]);
                            Runnable r = new recevoir(in[0], tabClientsout);
                            new Thread(r).start();

                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }

            });

            Listen.start();



            Thread envoi= new Thread(new Runnable() {
                String msg;
                @Override
                public void run() {
                    while(true){
                        msg = sc.nextLine();
                        PrintWriter out; // declaration d'une variable permettant l'envoi de texte vers le client
                        for (int i = 0; i < tabClientsout.size(); i++) // parcours de la table
                        {
                            out = (PrintWriter) tabClientsout.elementAt(i); // extraction de l'élément courant (type PrintWriter)
                            if (out != null) // au cas ou
                            {
                                out.print("Server : " + msg+"\n");
                                out.flush(); // envoi dans le flux de sortie
                            }
                        }
                    }
                }
            });
            envoi.start();




    }
}

