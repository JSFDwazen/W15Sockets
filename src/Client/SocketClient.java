/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Client;

import Shared.Edge;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author jsf3
 */
public class SocketClient {

    private static final Logger LOG = Logger.getLogger(SocketClient.class.getName());
    private Thread t;
    Socket socket;
    ObjectOutputStream out;
    ObjectInputStream in;

    public SocketClient() {
        try {
            socket = new Socket("localhost", 8189);

            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());
            // End of correct initialization
        } catch (IOException ex) {
            Logger.getLogger(SocketClient.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public List<Edge> getEdges(int level) {
        try {
            out.writeObject(1);
            out.writeObject(level);
            out.flush();

            return (List<Edge>) in.readObject();
        } catch (IOException | ClassNotFoundException ex) {
            Logger.getLogger(SocketClient.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public Edge getEdge(int level) {
        try {
            out.writeObject(2);
            out.writeObject(level);
            out.flush();

            return (Edge) in.readObject();
        } catch (IOException | ClassNotFoundException ex) {
            Logger.getLogger(SocketClient.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
}
