/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server.net;

import common.ServerResponseTypes;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.AbstractMap.SimpleEntry;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ForkJoinPool;
import server.controller.ResponseHandler;

/**
 *
 * @author Perttu Jääskeläinen
 */
public class UserConnection implements Runnable {
    public static final String FILE_CATALOG = "catalog/";
    private static final String IP_ADDRESS = "127.0.0.1";
    private static final int MAX_QUEUE_SIZE = 100;
    private static final int    PORT_NO         = 8080;         // default port number
    private final int   LINGER_TIME     = 30000;        // linger time when closing socket
    private final int   SOCKET_TIMEOUT  = 1800000;      // time before timing out a connection
    private ServerSocket server;
    private ResponseHandler receive;
    private Map<Long, Entry<String, Boolean>> operations;
    private BlockingQueue<Socket> clientQueue;
    private boolean running;
    private boolean firsttime;
    
    public static InetSocketAddress getAddress() {
        return new InetSocketAddress(IP_ADDRESS, PORT_NO);
    }
    
    public UserConnection(ResponseHandler c) throws IOException {
        server = new ServerSocket(PORT_NO);
        c.receive("server socket started");
        operations = new HashMap<>();
        clientQueue = new ArrayBlockingQueue<>(MAX_QUEUE_SIZE);
        this.receive = c;
        firsttime = true;
        new Thread(this).start();
    }
    public boolean addOperation(long ID, String filename, boolean operation) {
        if (operations.containsKey(ID)) {
            return false;
        } else {
            operations.put(ID, new SimpleEntry(filename, operation));
            return true;
        }
    }
    public void start() {
        if (running) {
            return;
        }
        running = true;
        firsttime = false;
        serve();
    }
    public synchronized void serve() {
        while (running) {
            try {
                Socket client = server.accept();
                receive.receive("socket accepted");
                clientQueue.add(client);
                ForkJoinPool.commonPool().execute(this);
            } catch (IOException e) {
                e.printStackTrace();
                receive.receive("Error when accepting socket");
            } 
        }     
    }
    @Override
    public void run() {
        if (!running) {
            if (firsttime) {
                start();
            }
            return;
        }
        while (clientQueue.peek() != null) {
            Socket client = clientQueue.poll();
            try {
                client.setSoLinger(true, LINGER_TIME);
                client.setSoTimeout(SOCKET_TIMEOUT);
                long id = readId(client);
                Entry<String, Boolean> e = operations.get(id);
                operations.remove(id);
                if (e.getValue()) {
                    receiveFrom(client, e.getKey());
                } else {
                    sendTo(client, e.getKey());
                }
                client.close();
                operations.remove(id);
            } catch (IOException e) {
                try {
                    OutputStream out = client.getOutputStream();
                    out.write(ServerResponseTypes.SERVER_ERROR.name().getBytes());
                    client.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                e.printStackTrace();
            }
            client = null;
        }   
    }
    private long readId(Socket client) throws IOException {
        OutputStream out = client.getOutputStream();
        InputStream in = client.getInputStream();
        byte[] arr = ServerResponseTypes.SERVER_READY.name().getBytes();
        byte[] buffer = new byte[Long.BYTES];
        out.write(arr);
        out.flush();
        String s = new String(arr);        
        in.read(buffer, 0, buffer.length);
        ByteBuffer buf = ByteBuffer.allocate(Long.BYTES);
        buf.put(buffer);
        buf.flip();
        long l = buf.getLong();
        receive.receive(String.valueOf(l));
        return l;
    }
    private void sendTo(Socket client, String filename) throws IOException {
        InputStream in = new FileInputStream(FILE_CATALOG + filename);
        OutputStream out = client.getOutputStream();
        byte[] buffer = new byte[4096];
        int bytes;
        while ((bytes = in.read(buffer)) != -1) {
            receive.receive("wrote:" + bytes);
            out.write(buffer, 0, bytes);
        }
        out.flush();
        out.close();
        in.close();
        client.close();
    }
    private void receiveFrom(Socket client, String filename) throws IOException {
        File file = new File(FILE_CATALOG + filename);
        InputStream in = client.getInputStream();
        FileOutputStream os = new FileOutputStream(file);
        byte[] buffer = new byte[4096];
        int bytes;
        while ((bytes = in.read(buffer)) != -1) {
            os.write(buffer, 0, bytes);
        }
        os.flush();
        in.close();
        os.close();
    }
}
