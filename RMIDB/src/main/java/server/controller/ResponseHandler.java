/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server.controller;

/**
 *
 * @author Perttu Jääskeläinen
 */
public interface ResponseHandler {
    public void receive(String msg);
}
