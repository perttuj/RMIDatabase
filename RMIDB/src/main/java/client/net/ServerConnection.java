/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package client.net;

import common.ServerResponseTypes;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.nio.ByteBuffer;

/**
 *
 * @author Perttu Jääskeläinen
 */
public class ServerConnection {
    public static final String USER_DIRECTORY = "user/";
    private static final int TIMEOUT_USER_SOCKET = 1800000;   // User socket timeout time
    private static final int TIMEOUT_SERVER_SOCKET = 300000;   // Timeout for server socket
    
    public ServerConnection() {
    }
    
    public void connectAndSend(InetSocketAddress add, long ID, String filename, boolean receive) throws IOException {
        System.out.println("connecting to server");
        Socket socket = new Socket();
        socket.connect(add, TIMEOUT_SERVER_SOCKET);
        socket.setSoTimeout(TIMEOUT_USER_SOCKET);
        start(socket, filename, ID, receive);
    }
    private void start(Socket server, String filename, long ID, boolean receive) throws SocketException, IOException  {
        waitForServer(server, ID);
        if (receive) {
            receiveFrom(server, filename);
        } else {
            sendTo(server, filename);
        }
    }
    public static long getFileSize(String filename) {
        File f = new File(USER_DIRECTORY + filename);
        return f.length();
    }
    private void waitForServer(Socket socket, long ID) throws IOException {
        InputStream in = socket.getInputStream();
        OutputStream out = socket.getOutputStream();
        String p = ServerResponseTypes.SERVER_READY.name();
        byte[] buf = new byte[1024];
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.putLong(ID);
        byte[] bufarr = buffer.array();
        boolean waiting = true;
        while (waiting) {
            in.read(buf);
            String s = new String(buf);
            if (s.contains(p)) {
                waiting = false;
                out.write(bufarr);
                out.flush();
            }
        }
    }
    private void sendTo(Socket server, String filename) throws IOException {
        System.out.println("sending to server");
        FileInputStream in = new FileInputStream(USER_DIRECTORY + filename);
        OutputStream out = server.getOutputStream();
        byte[] buffer = new byte[8192];
        int bytes;
        while ((bytes = in.read(buffer)) != -1) {
            System.out.println(bytes);
            out.write(buffer, 0, bytes);
        }
        out.flush();
        out.close();
        server.close();
        System.out.println("sending done");
    }
    private void receiveFrom(Socket server, String filename) throws IOException {
        System.out.println("receiving from server");
        File file = new File(USER_DIRECTORY + filename);
        OutputStream out = new FileOutputStream(file);
        InputStream in = server.getInputStream();
        byte[] buffer = new byte[4096];
        int bytes;
        while ((bytes = in.read(buffer)) != -1) {
            out.write(buffer, 0, bytes);
        }
        out.flush();
        out.close();
        server.close();
        System.out.println("download done");
    }
}
