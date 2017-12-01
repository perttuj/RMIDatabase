/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package client.net;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;

/**
 *
 * @author Perttu Jääskeläinen
 */
public class ServerConnection {
    public static final String USER_DIRECTORY = "user/";
    private static final int TIMEOUT_USER_SOCKET = 1800000;   // User socket timeout time
    private static final int TIMEOUT_SERVER_SOCKET = 30000;   // Timeout for server socket
    private Socket socket;
    private String filename;
    private InetSocketAddress address;
    private boolean receive;
    
    public ServerConnection(InetSocketAddress add, String fname, boolean receiving) {
        this.filename = fname;
        this.receive = receiving;
        this.address = add;
    }
    
    public void connectAndSend() throws IOException {
        System.out.println("connecting");
        socket = new Socket();
        socket.connect(address, TIMEOUT_SERVER_SOCKET);
        socket.setSoTimeout(TIMEOUT_USER_SOCKET);
        start(socket);
        close();
    }
    private void close() throws IOException {
        socket.close();
    }
    private void start(Socket server) throws SocketException, IOException  {
        if (receive) {
            receiveFrom(server);
        } else {
            sendTo(server);
        }
    }
    private void sendTo(Socket server) throws IOException {
        System.out.println("sending");
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(server.getOutputStream()));
        BufferedReader br = new BufferedReader(new FileReader(ServerConnection.USER_DIRECTORY + filename));
        String line;
        while ((line = br.readLine()) != null) {
            bw.write(line);
        }
        bw.flush();
        System.out.println("done sending");
    }
    private void receiveFrom(Socket server) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(server.getInputStream()));
        BufferedWriter bw = new BufferedWriter(new FileWriter(ServerConnection.USER_DIRECTORY + filename));
        String line;
        while ((line = in.readLine()) != null) {
            bw.write(line);
        }
    }
}
