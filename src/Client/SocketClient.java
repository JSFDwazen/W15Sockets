/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author jsf3
 */
public class SocketClient {

    private static final Logger LOG = Logger.getLogger(SocketClient.class.getName());

    public static void main(String[] args) {

        try {
            try (Socket socket = new Socket("localhost", 8189)) {

                /* Please choose either the code for example a) or b) */
                // Example a) Correct order of initialization
                ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
                // End of correct initialization

//                // Example b) Correct order of initialization
//                ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
//                ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
//                // End of example b)
                int i = (int) in.readObject();
                LOG.log(Level.INFO, "Received {0}", i);
            }
        } catch (IOException | ClassNotFoundException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }

    }
}
