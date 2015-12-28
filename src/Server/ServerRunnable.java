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
import javafx.scene.input.MouseButton;

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

    // Zoom
    private double zoomTranslateX = 0.0;
    private double zoomTranslateY = 0.0;
    private double zoom = 1.0;

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
            //bereken
            if (optie == 3) {
                zoom = (double) in.readObject();
                zoomTranslateX = (double) in.readObject();
                zoomTranslateY = (double) in.readObject();
                Edge edge = (Edge) in.readObject();

                out.writeObject(edgeAfterZoomAndDrag(edge));
            } else {
                int level = (int) in.readObject();
                this.koch.setLevel(level);
                this.koch.generateLeftEdge();
                this.koch.generateBottomEdge();
                this.koch.generateRightEdge();
                if (optie == 1) {
                    out.writeObject(edges);
                }
            }
            this.socket.close();
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }

    public Edge edgeAfterZoomAndDrag(Edge e) {
        return new Edge(
                e.X1 * zoom + zoomTranslateX,
                e.Y1 * zoom + zoomTranslateY,
                e.X2 * zoom + zoomTranslateX,
                e.Y2 * zoom + zoomTranslateY,
                e.color);
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
