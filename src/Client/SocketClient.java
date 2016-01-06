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
import java.util.List;
import java.util.Observable;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author jsf3
 */
public class SocketClient extends Observable {

    private static final Logger LOG = Logger.getLogger(SocketClient.class.getName());
    private Thread t;
    Socket socket;
    ObjectOutputStream out;
    ObjectInputStream in;

    public SocketClient(int id) {
        try {
            socket = new Socket("localhost", 8189);

            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());            
            // End of correct initialization
            out.writeObject(id);
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

    public void getEdge(int level) {
        try {
            out.writeObject(2);
            out.writeObject(level);
            out.flush();
            for (int i = 0; i < (int) (3 * Math.pow(4, level - 1)); i++) {
                Edge e = (Edge) in.readObject();
                this.setChanged();
                this.notifyObservers(e);
            }
        } catch (IOException | ClassNotFoundException ex) {
            Logger.getLogger(SocketClient.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public List<Edge> edgeAfterZoomAndDrag(double zoom, double zoomTranslateX, double zoomTranslateY) {
        try {
            out.writeObject(3);
            out.writeObject(zoom);
            out.writeObject(zoomTranslateX);
            out.writeObject(zoomTranslateY);
            out.flush();
            return (List<Edge>) in.readObject();
        } catch (IOException | ClassNotFoundException ex) {
            Logger.getLogger(SocketClient.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
}
