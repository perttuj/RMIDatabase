/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server.model;

import common.ClientDTO;
import common.Credentials;
import common.Receiver;
import java.util.HashMap;
import java.util.Map;

/**
 *  Class for handling all clients
 * @author Perttu Jääskeläinen
 */
public class ClientHandler {
    Map<String, Client> clients;
    public ClientHandler() {
        clients = new HashMap<>();
    }
    private void add(Client c) {
        clients.put(c.getUsername(), c);
    }
    private boolean remove(Client c) {
        return clients.remove(c.getUsername()) != null;
    }
    public ClientDTO login(Credentials cred, Receiver res) {
        Client c = new Client(cred, res);
        add(c);
        return c;
    }
    public boolean logout(ClientDTO dto) {
        Client c = (Client) dto;
        boolean rem = remove(c);
        return rem;
    }
}
