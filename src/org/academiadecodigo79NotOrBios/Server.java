package org.academiadecodigo79NotOrBios;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.List;
public class Server {
    private ServerSocket serverSocket;
    private final List<Worker> workers = Collections.synchronizedList(new ArrayList<>());
    public Server() {
        try {
            BufferedReader portInput = new BufferedReader(new InputStreamReader(System.in));
            System.out.println("Whats the port number?");
            int port = Integer.parseInt(portInput.readLine());
            portInput.close();
            System.out.println("Binding to port " + port);
            serverSocket = new ServerSocket(port);
            System.out.println("Waiting for clients...");
        } catch (IOException e) {
            System.out.println("another one!");
        }
    }
    public static void main(String args[]) {
        try {
            // try to create an instance of the ChatServer at port specified at args[0]
            Server server = new Server();
            server.serverExecute();
        } catch (NumberFormatException ex) {
            // write an error message if an invalid port was specified by the user
            System.out.println("Invalid port number " + args[0]);
        }
    }
    public void serverExecute() {
        while (true) {
            try {
                Socket clientSocket = serverSocket.accept();
                ExecutorService cachedPool = Executors.newCachedThreadPool();
                Worker worker = new Worker(clientSocket);
                cachedPool.submit(worker);
                workers.add(worker);
                System.out.println("Client on...");
            } catch (IOException e) {
                System.out.println("Error receiving client connection");
            }
        }
    }
    public class Worker implements Runnable {
        private Socket clientSocket;
        private BufferedReader inputBufferedReader;
        private BufferedWriter outputBufferedWriter;
        private String userName;
        public Worker(Socket clientSocket) {
            try {
                System.out.println("Trying to establish a connection...");
                this.clientSocket = clientSocket;
                System.out.println("Connected to: " + clientSocket);
                setupClientSocket();
                outputBufferedWriter.write("insert name \n");
                outputBufferedWriter.flush();
                userName = inputBufferedReader.readLine();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        public void setupClientSocket() throws IOException {
            inputBufferedReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            outputBufferedWriter = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
        }
        @Override
        public void run() {
            String line = "";
            // while the client doesn't signal to quit
            while (!line.equals("/quit")) {
                try {
                    // read the pretended message from the console
                    line = inputBufferedReader.readLine();
                    // write the pretended message to the output buffer
                    for (Worker currentWorker : workers) {
                        if (!(currentWorker == this)) {
                            currentWorker.outputBufferedWriter.write(userName + ":" + line);
                            currentWorker.outputBufferedWriter.newLine();
                            currentWorker.outputBufferedWriter.flush();
                        }
                    }
                    System.out.println(userName + ":" + line);
                } catch (IOException ex) {
                    System.out.println("Sending error: " + ex.getMessage() + ", closing client...");
                    break;
                }
            }
            System.out.println("Chat is closed....Thanks for your visit");
        }
    }
}
