import java.net.*;
import java.io.*;
import java.util.*;
import javax.json.*;
import java.nio.charset.StandardCharsets;

/*****************************/
/**
* \brief It implements a distributed chat. 
* It creates a ring and delivers messages
* using flooding 
**********************************/
public class Chat {

    /*
       Json Messages:
     
      {
        "type" :  "JOIN",
        "parameters" :
               {   
                    "myAlias" : string,
                    "myPort"  : number
               }
       }
     
       {
        "type" :  "ACCEPT",
        "parameters" :
               {   
                   "ipPred"    : string,
                   "portPred"  : number
               }
    }
     
    {
         "type" :  "LEAVE",
         "parameters" :
         {
             "ipPred"    : string,
             "portPred"  : number
         }
    }
    
       {
         "type" :  "Put",
        "parameters" :
         {
             "aliasSender"    : string,
             "aliasReceiver"  : string,
             "message"        : string
        }
       }
     
     {
        "type" :  "NEWSUCCESSOR",
        "parameters" :
        {
            "ipSuccessor"    : string,
            "portSuccessor"  : number
        }
     }
     */

    // My info
    public String alias;
    public int myPort;
    // Successor
    public String ipSuccessor;
    public int portSuccessor;
    // Predecessor
    public String ipPredecessor;
    public int portPredecessor;

    private Object lock1 = new Object();

    public Socket GetSocket(int port) throws IOException {
        Socket socket = new Socket("127.0.0.1", port);
        return socket;
    }

    /*****************************/
    /**
    * \class Server class "chat.java" 
    * \brief It implements the server
    **********************************/
    private class Server implements Runnable {
        public Server() {
        }

        /*   
            {
            "type" :  "ACCEPT",
            "parameters" :
                {   
                "ipPred"    : string,
                "portPred"  : number
                }
            }
        */
        public void accept(Socket clientSocket, String ipPred, int portPred, int myPort) throws IOException {
            JsonObject jsonAcceptMessageObject = Json.createObjectBuilder().add("type", "ACCEPT").add("parameters", Json
                    .createObjectBuilder().add("ipPred", ipPred).add("portPred", portPredecessor).add("myPort", myPort))
                    .build();
            OutputStreamWriter out = new OutputStreamWriter(clientSocket.getOutputStream(), StandardCharsets.UTF_8);
            out.write(jsonAcceptMessageObject.toString());
            out.close();
        }

        /*   
            {
            "type" :  "ACCEPTED",
            "parameters" :
                {   
                "ipPred"    : string,
                "portPred"  : number
                }
            }
        */
        public void accepted(Socket peerSocket, String updatedPredIP, int updatedPortPred, int myPort)
                throws IOException {
            JsonObject jsonAcceptMessageObject = Json
                    .createObjectBuilder().add("type", "ACCEPTED").add("parameters", Json.createObjectBuilder()
                            .add("ipPred", updatedPredIP).add("portPred", updatedPortPred).add("myPort", myPort))
                    .build();
            OutputStreamWriter out = new OutputStreamWriter(peerSocket.getOutputStream(), StandardCharsets.UTF_8);
            out.write(jsonAcceptMessageObject.toString());
            out.close();
        }

        /*
            {
            "type" :  "NEWSUCCESSOR",
            "parameters" :
                {
                "ipSuccessor"    : string,
                "portSuccessor"  : number
                }
            }
        */
        public void NewSuccessor(Socket succSocket, String ipSucc, int portSucc, int myPort) throws IOException {
            JsonObject jsonAcceptMessageObject = Json.createObjectBuilder().add("type", "NEWSUCCESSOR")
                    .add("parameters", Json.createObjectBuilder().add("ipSuccessor", ipSucc)
                            .add("portSuccessor", portSucc).add("myPort", myPort))
                    .build();
            OutputStreamWriter out = new OutputStreamWriter(succSocket.getOutputStream(), StandardCharsets.UTF_8);
            out.write(jsonAcceptMessageObject.toString());
            out.close();
        }

        /*****************************/
        /**
        * \brief It allows the system to interact with the participants. 
        **********************************/
        public void run() {
            try {
                ServerSocket servSock = new ServerSocket(myPort);
                while (true) {
                    /*
                     Socket clntSock = servSock.accept(); // Get client connections
                     Create a new thread to handle the connection
                    
                     ObjectInputStream  ois = new
                     ObjectInputStream(clntSock.getInputStream());
                     ObjectOutputStream oos = new
                     ObjectOutputStream(clntSock.getOutputStream());
                     ois.read();    reads the message using JsonParser and handle the messages
                    
                     oos.write(m);   only if the message requires a response
                     clntSock.close();
                     */

                    Socket clientSock = servSock.accept();
                    new Thread() {
                        public void run() {
                            try {
                                JsonReader reader = Json.createReader(clientSock.getInputStream());
                                JsonObject message = reader.readObject();
                                reader.close();
                                clientSock.close();
                                // System.out.println(message.toString());
                                JsonObject parameters = message.getJsonObject("parameters");
                                if (message.getString("type").equals("JOIN")) {
                                    int joinPort = parameters.getInt("myPort");
                                    System.out.println("Peer at port " + joinPort + " is requesting to join.");
                                    System.out.println("Sending request to accept to peer at port " + joinPort + ".");
                                    Socket acceptSocket = GetSocket(joinPort);
                                    accept(acceptSocket, ipPredecessor, portPredecessor, myPort);
                                    Socket predSocket = GetSocket(portPredecessor);
                                    System.out.println("Sending successor information to predecessor at port "
                                            + portPredecessor + ".");
                                    NewSuccessor(predSocket, "127.0.0.1", joinPort, myPort);
                                    synchronized (lock1) {
                                        System.out.println("Lock set.");
                                        System.out.println("> Updating portPredecessor -> " + joinPort);
                                        portPredecessor = joinPort;
                                        System.out.println("Unlocking.");
                                    }
                                } else if (message.getString("type").equals("ACCEPT")) {
                                    int acceptPredPort = parameters.getInt("portPred");
                                    int peerPort = parameters.getInt("myPort");
                                    System.out.println("Peer at port " + peerPort + " is requesting to accept.");
                                    synchronized (lock1) {
                                        System.out.println("Lock set.");
                                        System.out.println("> Updating portPredecessor -> " + acceptPredPort);
                                        portPredecessor = acceptPredPort;
                                        System.out.println("> Updating portSuccessor -> " + peerPort);
                                        portSuccessor = peerPort;
                                        System.out.println("Unlocking.");
                                    }
                                    System.out.println("Accepting peer at port " + peerPort + ".");
                                    Socket acceptedSocket = GetSocket(peerPort);
                                    accepted(acceptedSocket, ipPredecessor, portPredecessor, myPort);
                                } else if (message.getString("type").equals("ACCEPTED")) {
                                    int peerPort = parameters.getInt("myPort");
                                    System.out.println("Peer at port " + peerPort + "accepted connection.");
                                } else if (message.getString("type").equals("NEWSUCCESSOR")) {
                                    int newSuccessorPort = parameters.getInt("portSuccessor");
                                    int peerPort = parameters.getInt("myPort");
                                    System.out.println("Peer at port " + peerPort
                                            + " is requesting to update portSuccessor information.");
                                    synchronized (lock1) {
                                        System.out.println("Lock set.");
                                        System.out.println("> Updating portSuccessor -> " + newSuccessorPort);
                                        portSuccessor = newSuccessorPort;
                                        System.out.println("Unlocking.");
                                    }
                                }
                                // ObjectOutputStream oos = new ObjectOutputStream(clientSock.getOutputStream());
                            } catch (JsonException j) {
                                System.out.println("Json exception exiting...");
                                System.exit(-1);
                            } catch (IOException io) {
                                System.out.println("IOException in inner thread block. Exiting...");
                                System.exit(-1);
                            } finally {
                                synchronized (lock1) {
                                    System.out.println("State of system:");
                                    System.out.println("> myAlias         : " + alias);
                                    System.out.println("> myPort          : " + myPort);
                                    System.out.println("> portPredecessor : " + portPredecessor);
                                    System.out.println("> portSuccessor   : " + portSuccessor);
                                }
                            }
                        }
                    }.start();
                }
            } catch (IOException e) {
                System.out.println("IOException in server block...");
                System.exit(-1);
            }
        }
    }

    /*****************************/
    /** 
    * \brief It implements the client
    *********************************/
    private class Client implements Runnable {

        public Client() {
        }

        /*
            {
            "type" :  "JOIN",
            "parameters" :
                {   
                "myAlias" : string,
                "myPort"  : number
                }
            }
        */
        public void join(Socket socket) throws IOException {
            JsonObject jsonJoinMessageObject = Json.createObjectBuilder().add("type", "JOIN")
                    .add("parameters", Json.createObjectBuilder().add("myAlias", alias).add("myPort", myPort)).build();
            OutputStreamWriter out = new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8);
            out.write(jsonJoinMessageObject.toString());
            out.close();
            JsonReader reader = Json.createReader(socket.getInputStream());
            JsonObject jsonAcceptMessageObject = reader.readObject();
            reader.close();
        }

        public int SelectionMenu(Scanner in) {
            boolean correct_input = false;
            int selection = 0;
            while (!correct_input) {
                System.out.println("Please select an option:");
                System.out.println("(1) Join");
                System.out.println("(2) Exit");
                System.out.print("> ");
                try {
                    selection = in.nextInt();
                    if (selection != 1 && selection != 2) {
                        System.out.println("Invalid choice. Try again...");
                    } else {
                        correct_input = true;
                    }
                } catch (InputMismatchException input) {
                    System.out.println("This menu only accepts integers. Try again...");
                } catch (NoSuchElementException element) {
                    element.printStackTrace();
                    System.out.println("Scanner error, quitting...");
                    System.exit(-1);
                }
            }
            return selection;
        }

        public int JoinMenu(Scanner in) {
            boolean correct_port = false;
            int port = myPort;
            while (!correct_port) {
                System.out.println("Please enter a port to connect to.");
                System.out.print("> ");
                try {
                    port = in.nextInt();
                    correct_port = true;
                } catch (InputMismatchException input) {
                    System.out.println("Enter an interger port number.");
                } catch (NoSuchElementException element) {
                    element.printStackTrace();
                    System.out.println("Scanner error, quitting...");
                    System.exit(-1);
                }
            }
            System.out.println();
            return port;
        }

        /*****************************/
        /**
        * \brief It allows the user to interact with the system. 
        **********************************/
        public void run() {
            synchronized (lock1) {
                ipSuccessor = "127.0.0.1";
                ipPredecessor = "127.0.0.1";
                portSuccessor = myPort;
                portPredecessor = myPort;
            }
            while (true) {
                /*
                Create a simple user interface
                
                The first thing to do is to join
                   ask the ip and port when joining and set ipSuccessor = ip, portSuccessor = port
                Socket socket = new Socket(ipSuccessor, portSuccessor);
                
                
                // Create the mssages m using JsonWriter and send it as stream
                
                ObjectOutputStream oos = new
                ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream ois = new
                ObjectInputStream(socket.getInputStream());
                oos.write(m);   this sends the message
                  ois.read();    reads the response and parse it using JsonParser
                socket.close();
                 
                 Use mutex to handle race condition when reading and writing the global variable (ipSuccessor, 
                portSuccessor, ipPredecessor, portPredecessor)
                 
                 */
                Scanner in = new Scanner(System.in);
                int userSelection = SelectionMenu(in);
                switch (userSelection) {
                case 1:
                    int port = JoinMenu(in);
                    try {
                        Socket sock = GetSocket(port);
                        join(sock);
                        wait();
                    } catch (IOException io) {
                        io.printStackTrace();
                        System.out.println("IO Exception in Case 1.");
                    } catch (InterruptedException interr) {
                        interr.printStackTrace();
                        System.out.println("InterruptedException in Client Run. Exiting...");
                        System.exit(-1);
                    }
                    break;
                case 2:
                    System.out.println("Exiting...");
                    System.exit(0);
                    break;
                }
            }
        }
    }

    /*****************************/
    /**
    * Starts the threads with the client and server:
    * \param Id unique identifier of the process
    * \param port where the server will listen
    **********************************/
    public Chat(String alias, int myPort) {

        this.alias = alias;
        this.myPort = myPort;
        // Initialization of the peer
        Thread server = new Thread(new Server());
        Thread client = new Thread(new Client());
        server.start();
        client.start();
        try {
            client.join();
            server.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
            System.out.println("InterruptedException in Chat class. Exiting...");
            System.exit(-1);
        }
    }

    public static void main(String[] args) {
        if (args.length < 2) {
            throw new IllegalArgumentException("Parameter: <alias> <myPort>");
        }
        Chat chat = new Chat(args[0], Integer.parseInt(args[1]));
    }
}