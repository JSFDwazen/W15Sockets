/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Server;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
/**
 *
 * @author jsf3
 */
public class SocketServer {
     private static final Logger LOG = Logger.getLogger(SocketServer.class.getName());

    public static void main(String[] args) {
        try {
            // Establish server socket
            ServerSocket serverSocket = new ServerSocket(8189);
            LOG.log(Level.INFO, "Server is running. Listening on port: {0}", serverSocket.getLocalPort());
            while (true) {
                try {
                    // Wait for client connection
                    Socket incomingSocket = serverSocket.accept();
                    LOG.log(Level.INFO, "New Client Connected: {0}", incomingSocket.getInetAddress());
                    
                    // Handle client request in a new thread
                    Thread t = new Thread(new ServerRunnable(incomingSocket));
                    t.start();
                } catch (IOException e) {
                    LOG.log(Level.WARNING, "IOException occurred: {0}", e.getMessage());
                } catch (ClassNotFoundException ex) {
                    Logger.getLogger(SocketServer.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }
}
