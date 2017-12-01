/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package common;

import java.io.Serializable;

/**
 *
 * @author Perttu Jääskeläinen
 */
public class Credentials implements Serializable {
    private String username;
    private String password;
    
    public Credentials (String user, String pass) {
        this.username = user;
        this.password = pass;
    }
    
    public String getUser() {
        return this.username;
    }
    public String getPass() {
        return this.password;
    }
}
