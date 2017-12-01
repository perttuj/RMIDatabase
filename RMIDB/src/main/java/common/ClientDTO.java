/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package common;

import java.io.Serializable;

/**
 *  used by server to limit a clients view for a client handler object
 * @author Perttu Jääskeläinen
 */
public interface ClientDTO extends Serializable {
    public String getUsername();
}
