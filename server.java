import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.*;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class ServerClientThread extends Thread {
    Socket serverClient;
    Socket Clients;

    ServerClientThread(Socket inSocket, Socket ComeSocket){
        serverClient = inSocket;
        Clients = ComeSocket;
    }

    public void run(){
        System.out.println("Thread is running");
        try{
            DataInputStream inStream = new DataInputStream(serverClient.getInputStream());
            DataOutputStream outStream = new DataOutputStream(Clients.getOutputStream());
            //transfer inputstream to outputstream
            byte[] tempData= new byte [10000];
            int bytesRead = 0;
            while ((bytesRead = inStream.read(tempData, 0, tempData.length)) != -1){
                outStream.write(tempData, 0, bytesRead);
            }
            outStream.flush();
            inStream.close();
            outStream.close();
        }catch(Exception ex){
            System.out.println(ex);
        }finally{
            System.out.println("Thread done");
        }
    }
}

public class server {
    public static void main(String args[]) throws Exception {
        int n_port = 9787;
        System.out.println("port is" + n_port + '\n');
        //write assigned port number into port
        String portString = String.valueOf(n_port);
        File file = new File("port");
        file.createNewFile();
        FileWriter writer = new FileWriter(file);
        writer.write(portString + "\n");
        writer.flush();
        writer.close();
        System.out.println("finish writing to port");

        //create TCP socket
        ServerSocket welcomeSocket = new ServerSocket(n_port);
        String clientMessage = "";

        //The dictionary of the coming client<key, socket>
        Map<String, Socket> dictionary = new HashMap<String, Socket>();
        //running thread list
        List<ServerClientThread> runningThread = new ArrayList<ServerClientThread>();


        //TCP recieving
        while (true) {
            System.out.println("server starts to listen...");
            Socket connectionSocket = welcomeSocket.accept();
            BufferedReader inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));

            //read the request from client
            clientMessage = inFromClient.readLine();
            if (!clientMessage.substring(0, 1).equals("F")) {
                System.out.println("Command from client: " + clientMessage.substring(0, 1) + ". Key is :" + clientMessage.substring(1));

                //The client is a uploader
                if (clientMessage.substring(0, 1).equals("P")) {
                    System.out.println("client is uploader");
                    //matched, start upload
                    if (dictionary.get(clientMessage.substring(1)) != null) {
                        System.out.println("client is uploader, matched, upload!");

                        //send the request to a separate thread
                        Socket uploadSocket = dictionary.remove(clientMessage.substring(1));
                        ServerClientThread sct = new ServerClientThread(connectionSocket, uploadSocket);
                        runningThread.add(sct);
                        sct.start();
                    } else{
                        //not matched, write down in the dictionary
                        dictionary.put(clientMessage.substring(1), connectionSocket);
                        System.out.println("client is uploader, Not matched, wait for matching");
                    }
                }

                //The client is a downloader
                else if (clientMessage.substring(0, 1).equals("G")) {
                    System.out.println("client is downloader");
                    //not matched, write down in the dictionary
                    if (dictionary.get(clientMessage.substring(1)) == null) {
                        dictionary.put(clientMessage.substring(1), connectionSocket);
                        System.out.println("client is downloader, Not matched, wait for matching");
                    }
                    //matched
                    else if (dictionary.get(clientMessage.substring(1)) != null) {
                        System.out.println("can't open a uploader before a downloader");
                        Socket downloadSocket = dictionary.remove(clientMessage.substring(1));
                        downloadSocket.close();
                    }
                }
            } else {
                // F terminate, first let running thread finish
                for(int i=0; i<runningThread.size();i++) {
                    runningThread.get(i).join();
                }
                //then close all socket in the waiting
                for (String key : dictionary.keySet()) {
                    dictionary.get(key).close();
                    System.out.println(dictionary.get(key)+"close");
                }
                break;
            }
        }
        //close the server socket
        welcomeSocket.close();
        System.out.println("server close");
    }
}
