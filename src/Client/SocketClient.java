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
    private List<Edge> edges;
    private Thread t;

    public static void main(String[] args) {
        new SocketClient();
    }

    public SocketClient() {
        edges = new ArrayList<>();
        int optie = 1;
        int level = 1;
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
                Scanner scanner = new Scanner(System.in);
                System.out.print("lijst(1), los(2): ");
                optie = scanner.nextInt();
                System.out.print("Welk level gegenereerd worden?: ");
                level = scanner.nextInt();
                out.writeObject(optie);
                out.writeObject(level);
                out.flush();
                //out.write(level);
                if (optie == 1) {
                    edges = (List<Edge>) in.readObject();
                } else if (optie == 2) {
                    for (int i = 0; i < (int) (3 * Math.pow(4, level - 1)); i++) {
                        edges.add((Edge) in.readObject());
                    }
                }
                System.out.println("" + edges.size());
                JSF31KochFractalFX.edges = this.edges;
                JSF31KochFractalFX.currentLevel = level;
                this.t = new Thread() {
                    public void run() {
                        JSF31KochFractalFX.main(new String[0]);
                    }
                };
                this.t.start();
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(SocketClient.class.getName()).log(Level.SEVERE, null, ex);
            }
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }

    }
}
