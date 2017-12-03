/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package client.view;

import client.net.ServerConnection;
import common.ClientDTO;
import common.Credentials;
import common.FileDTO;
import common.Receiver;
import common.Server;
import java.net.InetSocketAddress;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;
import java.util.Scanner;

/**
 *
 * @author Perttu Jääskeläinen
 */
public class UserInterpreter implements Runnable {
    private static Receiver myReceiver;
    private final Scanner console = new Scanner(System.in);
    private final SafePrinter printer = new SafePrinter();
    private final ServerConnection connection = new ServerConnection();
    private boolean running;
    private Server server;
    private long ID;
    
    public UserInterpreter(Server s) throws RemoteException{
        myReceiver = new ConsoleOutput();
        server = s;
        running = false;
        ID = -1;
    }
    
    public void start() {
        if (running) {
            return;
        }
        System.out.println("starting");
        running = true;
        new Thread(this).start();
    }
    private boolean toBoolean(String s) {
        if (s.equalsIgnoreCase("true")) {
            return true;
        }
        return false;
    }
    public void run() {
        while (running) {
            try {
                CommandLine line = new CommandLine(console.nextLine());
                InetSocketAddress addr;
                String filename;
                switch (line.getCommand()) {
                    case QUIT: 
                        running = false;
                        server.leaving(ID);
                        boolean forceUnexport = false;
                        UnicastRemoteObject.unexportObject(myReceiver, forceUnexport);
                        break;
                    case HELP:
                        printer.println("Command usages:");
                        for (Command cmd : Command.values()) {
                            printer.println(cmd.getDescription());
                        }
                        break;
                    case UPLOAD:
                        filename = line.message[1];
                        long size = ServerConnection.getFileSize(filename);
                        if (size == 0L) {
                            myReceiver.receive("file size could not be read");
                            break;
                        }
                        addr = server.uploadFile(ID, filename, size, toBoolean(line.message[2]), toBoolean(line.message[3]), myReceiver);
                        if (addr == null) {
                            filename = null;
                            break;
                        }
                        connection.connectAndSend(addr, ID, filename, false);
                        filename = null;
                        break;
                    case DOWNLOAD:
                        filename = line.message[1];
                        System.out.println(ID + filename);
                        addr = server.downloadFile(ID, filename, myReceiver);
                        if (addr == null) {
                            printer.println("cant download file");
                            break;
                        }
                        connection.connectAndSend(addr, ID, filename, true);
                        filename = null;
                        break;
                    case DELETE:
                        boolean deleted = server.deleteFile(ID, line.message[1], myReceiver);
                        if (!deleted) {
                            printer.println("deleting file failed");
                        }
                        break;
                        
                    case NOTIFY:
                        filename = line.message[1];
                        server.notifyUpdate(ID, filename, myReceiver);
                        break;
                    case LISTALL: 
                        List<? extends FileDTO> list = server.listFiles(ID);
                        final String[][] table = new String[list.size() + 1][];
                        table[0] = new String[]{"Filename:", "Owner:", "Public:", "Size:", "Writable:"};
                        for (int i = 0; i < list.size(); i++) {
                            FileDTO f = list.get(i);
                            table[i + 1] = new String[]{
                                f.getFilename(),
                                f.getOwner(),
                                f.getAccess(),
                                f.getSize(),
                                f.getPermission()};
                        }
                        for (final String[] row : table) {
                            System.out.format("%-15s%-15s%-15s%-15s%-15s\n", row);
                        }
                        break;
                    case UPDATEINFO: 
                        filename = line.message[1];
                        boolean access = toBoolean(line.message[2]);
                        boolean perm = toBoolean(line.message[3]);
                        boolean res = server.updateFileInfo(ID, filename, access, perm, myReceiver);
                        if (res) {
                            myReceiver.receive("updated");
                        } else {
                            myReceiver.receive("failed");
                        }
                        filename = null;
                        break;
                    case UPDATEDATA:
                        filename = line.message[1];
                        addr = server.updateFileData(ID, filename, ServerConnection.getFileSize(filename), myReceiver);
                        connection.connectAndSend(addr, ID, filename, false);
                        filename = null;
                        break;
                    case REGISTER:
                        String registered = server.register(new Credentials(line.message[1], line.message[2]));
                        printer.println(registered);
                        break;
                    case UNREGISTER:
                        String unregistered = server.unregister(new Credentials(line.message[1], line.message[2]));
                        printer.println(unregistered);
                        break;
                    case LOGIN:
                        if (ID != -1) {
                            printer.println("already logged in");
                            break;
                        }
                        ID = server.login(myReceiver, new Credentials(line.message[1], line.message[2]));
                        break;
                    case LOGOUT:
                        if (ID == -1) {
                            printer.println("not logged in");
                            break;
                        }
                        server.logout(ID);
                        ID = -1;
                        break;
                    default:
                        printer.println("error when reading command, type HELP for all commands");
                        break;
                }
            } catch (FormatException e) {
                printer.println("error when reading command, type HELP for all commands");
                e.printStackTrace();
                printer.println(e.getMessage());
                continue;
            } 
            catch (Exception e) {
                e.printStackTrace();
                printer.println("error when reading msg");
            }
        }
    }
    
    private class ConsoleOutput extends UnicastRemoteObject implements Receiver {
        public ConsoleOutput() throws RemoteException { }
        @Override
        public void receive(String message) throws RemoteException{
            printer.println(message);
        }
    }
}
