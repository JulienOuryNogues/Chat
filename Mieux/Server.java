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
public class Server {

    public ServerSocket serverSocket;
    private int port;
    Vector tabClientsout;
    private int nbClients;
    private Socket clientSocket; //pour chaque nouveau client
    Scanner sc;
    private BufferedReader in;
    private PrintWriter out;
    String pseudo;
    Vector pseudoClient; //stock des pseudos


    //constructeur
    public Server(int port) throws IOException {
        System.out.println("Initialising connexion at port "+port);
        this.port = port;
        this.sc=new Scanner(System.in);
        this.nbClients=0;
        this.tabClientsout=new Vector();
        this.serverSocket=new ServerSocket(port);
        this.pseudoClient= new Vector();
        System.out.println("Connected. Please enter a nickname : ");
        get_nickname();
    }


    //petit programme pour avoir un pseudo
    public void get_nickname(){
        this.pseudo=sc.nextLine();
        System.out.println("Welcome "+pseudo+". There are a few commands available press command() to see it.");
        pseudoClient.addElement(pseudo);
    }

    // cree un client et lance un thread d'ecoute pour lui propre
    public void addClient() throws IOException{
        this.clientSocket = serverSocket.accept();
        nbClients++;
        out = new PrintWriter(clientSocket.getOutputStream());
        in = new BufferedReader (new InputStreamReader (clientSocket.getInputStream()));
        tabClientsout.addElement(out);
        Runnable r = new recevoir(in, nbClients);
        new Thread(r).start();
    }

    //affiche les commandes du chat

    public void command(){
        System.out.println("List of commands available : ");
        System.out.println("- command() : List of commands available");
        System.out.println("- all() : Print all participants in this chat");
        System.out.println("- quit() :  Quit the server (Launch only when nobody is on the chat !)");
    }

    // c'est all()

    public void printall(){
        String pseu;
        int n=pseudoClient.size();
        int ntot=n;
        for (int i=0;i<n;i++){
            pseu=(String) pseudoClient.elementAt(i);
            if (pseu!=null){
                System.out.println("- "+pseu);
            }else{
                ntot--;
            }
        }
        System.out.println("The total number of participants is : " + ntot);
    }


    //envoie le message a tous

    public void sendAll(String msg){
        msg=pseudo +" : "+msg+"\n";
        System.out.print(msg); //j'ecris pour le server
        for (int i = 0; i < tabClientsout.size(); i++) // parcours de la table
        {
            out = (PrintWriter) tabClientsout.elementAt(i); // extraction de l'élément courant (type PrintWriter)
            if (out != null) // au cas ou
            {
                out.print(msg);
                out.flush(); // envoi dans le flux de sortie
            }
        }
    }



    // le thread recevoir pour chaque client

    public class recevoir implements Runnable {

        String msg;
        int numero; //retiens le ieme visiteur
        int cbieme; //index ou sont stockes les infos dans les vecteurs.
        BufferedReader in;
        String pseudo;
        PrintWriter outperso;

        public recevoir(BufferedReader in, int numero) throws IOException {
            this.numero=numero;
            this.in=in;
            this.cbieme=pseudoClient.size();
            outperso = (PrintWriter) tabClientsout.elementAt(numero-1);
            outperso.println("Connected. Please enter a nickname :");
            outperso.flush();
            this.pseudo=in.readLine();
            outperso.println("Welcome "+pseudo+". There are a few commands available press command() to see it.");
            outperso.flush();
            sendAll(pseudo+" joined the server");
            pseudoClient.addElement(pseudo);

        }

        public void sendAll(String msg){
            System.out.print(msg+"\n"); //j'ecris pour le server
            for (int i = 0; i < tabClientsout.size(); i++) // parcours de la table
            {
                out = (PrintWriter) tabClientsout.elementAt(i); // extraction de l'élément courant (type PrintWriter)
                if (out != null) // au cas ou
                {
                    out.print(msg+"\n");
                    out.flush(); // envoi dans le flux de sortie
                }
            }
        }


        public void printall(){
            String pseu;
            int n=pseudoClient.size();
            outperso.println("The total number of participants is : "+n);
            for (int i=0;i<n;i++){
                pseu=(String) pseudoClient.elementAt(i);
                if (pseu!=null){
                    outperso.println("- "+pseu);
                }
            }
            outperso.flush();
        }

        //affiche la liste des commandes disponibles dans le chat

        public void command(){
            outperso.println("List of commands available : ");
            outperso.println("- command() : List of commands available");
            outperso.println("- all() : Print all participants in this chat");
            outperso.println("- quit() :  Quit the server");
            outperso.flush();
        }


        public void run() {
            boolean deco=false;
            while(!deco) {
                try {
                    msg = in.readLine();
                    if (msg.equals("quit()")){
                        deco=true;
                        break;
                    }
                    else if (msg.equals("command()")){
                        command();
                    }
                    else if (msg.equals("all()")){
                        printall();
                    }else{
                        msg = pseudo+" : "+msg;
                        //tant que le client est connecté
                        sendAll(msg);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } //le client vient de se seconnecter
            // j'envoie le message de deco
            msg = pseudo+" disconnected.";
            sendAll(msg);
            //je supprime et ferme le out
            out = (PrintWriter) tabClientsout.elementAt(cbieme-1);
            out.close();
            tabClientsout.removeElementAt(cbieme-1);
            pseudoClient.removeElementAt(cbieme);

        }
    }







    public static void main(String[] args) {
        Server server;
        try {
            server = new Server(9990) ;
        } catch (IOException e) {
            server=null; // tant pis si ca plante
            e.printStackTrace();
        }
        final boolean[] deco = {false};


            // un thread listen qui ne fait qu'accepter les connexions, et lance le nouveau thread

        final Server finalServer = server;
        Thread Listen = new Thread(new Runnable (){

                public void run() {
                    // thread qui ne fait que rajouter des connexions
                    try {
                        while(true){
                            finalServer.addClient();
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
                    while(!deco[0]){
                        msg = finalServer.sc.nextLine();
                        if (msg.equals("quit()")){
                            deco[0] =true;
                        }
                        else if (msg.equals("command()")){
                            finalServer.command();
                        }
                        else if (msg.equals("all()")){
                            finalServer.printall();
                        }else{
                            finalServer.sendAll(msg);
                        }

                    }
                    //si on arrive la, c'est qu'on deco !
                    finalServer.sendAll("Server shuts down. Bye !");
                    //dans le cas que si le server est seul, le ferme proprement, sinon il faut SWITCHER sur un client -> how ?
                    PrintWriter out;
                    for (int i=0;i<finalServer.tabClientsout.size();i++){
                        out = (PrintWriter) finalServer.tabClientsout.elementAt(i);
                        if (out!=null){
                            out.close();
                        }
                    }
                    Listen.stop(); //je kill le listen
                    try {
                        finalServer.serverSocket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
            envoi.start();




    }
}

