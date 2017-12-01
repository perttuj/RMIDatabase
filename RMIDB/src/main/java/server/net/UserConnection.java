/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server.net;

import common.Receiver;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Console;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

/**
 *
 * @author Perttu Jääskeläinen
 */
public class UserConnection implements Runnable {
    public static final String FILE_CATALOG = "catalog/";
    private static final String IP_ADDRESS = "127.0.0.1";
    private static final int    PORT_NO         = 8080;         // default port number
    private final int   LINGER_TIME     = 30000;        // linger time when closing socket
    private final int   SOCKET_TIMEOUT  = 1800000;      // time before timing out a connection
    private File file;
    private String filename;
    private boolean receive;
    private Socket client;
    private Receiver r;
    
    public static InetSocketAddress getAddress() {
        return new InetSocketAddress(IP_ADDRESS, PORT_NO);
    }
    
    public UserConnection(String f, boolean receiving, Receiver c) {
        this.r = c;
        this.file = new File(FILE_CATALOG + f);
        this.filename = f;
        this.receive = receiving;
    }
    @Override
    public void run() {
        try {
            ServerSocket server = new ServerSocket(PORT_NO);
            client = server.accept();
            start(client);
        } catch (IOException e) {
            close();
            System.out.println("Error when creating server socket with port: " + PORT_NO);
        }    
    }
    private void close() {
        try {
           client.close();
        } catch (IOException e) {
            System.out.println("failed when closing socket");
        } finally {
            client = null;
        }
    }
    private void start(Socket client) throws SocketException, IOException  {
        client.setSoLinger(true, LINGER_TIME);
        client.setSoTimeout(SOCKET_TIMEOUT);
        if (receive) {
            receiveFrom(client);
        } else {
            sendTo(client);
        }
    }
    private void sendTo(Socket client) throws IOException {
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));
        BufferedReader br = new BufferedReader(new FileReader(file));
        String line;
        while ((line = br.readLine()) != null) {
            bw.write(line);
        }
        bw.flush();
    }
    private void receiveFrom(Socket client) throws IOException {
        r.receive("reciving..");
        BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
        BufferedWriter bw = new BufferedWriter(new FileWriter(file));
        String line;
        r.receive("next line");
        while ((line = in.readLine()) != null) {
            r.receive("loaded line");
            System.out.println(line);
            bw.write(line);
        }
        r.receive("done receiving");
        bw.flush();
    }
}
