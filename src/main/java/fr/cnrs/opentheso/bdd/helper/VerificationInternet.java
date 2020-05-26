/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnrs.opentheso.bdd.helper;
import java.net.Socket;

/**
 *
 * @author antonio.perez
 */

public class VerificationInternet {
    private final String dirWeb = "www.google.fr";
    private final int port = 80;
    
    
    public boolean isConected()
    {
        boolean status = false;
        try{
            Socket socket = new Socket(dirWeb, port);
            if(socket.isConnected())
            {
                status=true;
            }
        }catch(Exception e){
            System.out.println("Il n'y a pas de connexion");
        }
        
        return status;
    }   
}
