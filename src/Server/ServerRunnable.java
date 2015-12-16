/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author jsf3
 */
public class ServerRunnable implements Runnable {

    private static final Logger LOG = Logger.getLogger(ServerRunnable.class.getName());

    private Socket socket = null;
    private ObjectOutputStream out = null;
    private ObjectInputStream in = null;

    /**
     * Create object with a given socket.
     *
     * @param s Socket that is used to communicate with client
     */
    public ServerRunnable(Socket s) {
        this.socket = s;
    }

    @Override
    public void run() {
        try {
            LOG.log(Level.INFO, "Calculator Runnable Started. Using port: {0}", socket.getPort());

            // Bind input and outputstreams
            this.in = new ObjectInputStream(socket.getInputStream());
            this.out = new ObjectOutputStream(socket.getOutputStream());

            // Send random integer value to client
            int value = (new Random()).nextInt(1000);
            out.writeObject(value);
            LOG.log(Level.INFO, "Value sent: {0}", value);

            socket.close();
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }
}
