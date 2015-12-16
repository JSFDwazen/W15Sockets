/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Server;

import Shared.Edge;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author jsf3
 */
public class ServerRunnable implements Runnable, Observer {

    private static final Logger LOG = Logger.getLogger(ServerRunnable.class.getName());

    private Socket socket = null;
    private ObjectOutputStream out = null;
    private ObjectInputStream in = null;
    private final KochFractal koch;
    private List<Edge> edges;
    int optie = 0;

    /**
     * Create object with a given socket.
     *
     * @param s Socket that is used to communicate with client
     */
    public ServerRunnable(Socket s) {
        this.socket = s;
        this.koch = new KochFractal();
        this.koch.addObserver(this);
        this.edges = new ArrayList<>();
    }

    @Override
    public void run() {
        try {
            LOG.log(Level.INFO, "Calculator Runnable Started. Using port: {0}", socket.getPort());

            // Bind input and outputstreams
            this.in = new ObjectInputStream(socket.getInputStream());
            this.out = new ObjectOutputStream(socket.getOutputStream());

            // Send random integer value to client
            this.optie = (int) in.readObject();
            int level = (int) in.readObject();
            //bereken
            this.koch.setLevel(level);
            this.koch.generateLeftEdge();
            this.koch.generateBottomEdge();
            this.koch.generateRightEdge();
            if (optie == 1) {
                out.writeObject(edges);
            }
            this.socket.close();
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void update(Observable o, Object arg) {
        Edge e = (Edge) arg;
        if (optie == 1) {
            edges.add(e);
        } else if (optie == 2) {
            try {
                out.writeObject(e);
            } catch (IOException ex) {
                LOG.log(Level.SEVERE, null, ex);
            }
        }
    }
}
