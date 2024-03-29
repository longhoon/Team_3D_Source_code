import java.io.BufferedReader;

import java.io.BufferedWriter;

import java.io.InputStreamReader;

import java.io.OutputStreamWriter;

import java.io.PrintWriter;

import java.net.ServerSocket;

import java.net.Socket;

 

public class TCPServer implements Runnable {

    public static final int ServerPort = 9999;

    public static final String ServerIP = "xxx.xxx.xxx.xxxx"; //ip address

 

    @Override

    public void run() {

        // TODO Auto-generated method stub

        try {

            System.out.println("S: Connecting...");

            ServerSocket serverSocket = new ServerSocket(ServerPort);

 

            while (true) {

                Socket client = serverSocket.accept();

                System.out.println("S: Receiving...");

 

                try {

                    BufferedReader in = new BufferedReader(

                    new InputStreamReader(client.getInputStream()));

                    String str = in.readLine();

                    System.out.println("S: Received: '" + str + "'");

                    PrintWriter out = new PrintWriter(new BufferedWriter(

                    newOutputStreamWriter(client.getOutputStream())),true);

                    out.println("Server Received " + str);
               if(str.contains("2")){
                  String command = "aplay police_s.wav";
                  Runtime rt = Runtime.getRuntime();
                  Process p = rt.exec(command);
               }

                } catch (Exception e) {

                    System.out.println("S: Error");

                    e.printStackTrace();

                } finally {

                    client.close();

                    System.out.println("S: Done.");

                }

            }

        } catch (Exception e) {

            System.out.println("S: Error");

            e.printStackTrace();

        }

    }

 

    public static void main(String[] args) {

        // TODO Auto-generated method stub

        Thread desktopServerThread = new Thread(new TCPServer());

        desktopServerThread.start();

    }

}