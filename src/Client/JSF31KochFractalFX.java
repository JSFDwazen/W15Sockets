/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Client;

//import calculate.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.input.*;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import Shared.Edge;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.control.Spinner;

/**
 *
 * @author Nico Kuijpers
 */
public class JSF31KochFractalFX extends Application implements Observer {

    public static List<Edge> edges = new ArrayList<>();
    private SocketClient sc;

    // Zoom and drag
    private double zoomTranslateX = 0.0;
    private double zoomTranslateY = 0.0;
    private double zoom = 1.0;
    private double startPressedX = 0.0;
    private double startPressedY = 0.0;
    private double lastDragX = 0.0;
    private double lastDragY = 0.0;

    // Current level of Koch fractal
    public static int currentLevel = 1;

    // Labels for level, nr edges, calculation time, and drawing time
    private Label labelLevel;
    private Label labelNrEdges;
    private Label labelNrEdgesText;
    private Label labelCalc;
    private Label labelCalcText;
    private Label labelDraw;
    private Label labelDrawText;

    // Koch panel and its size
    private Canvas kochPanel;
    private final int kpWidth = 500;
    private final int kpHeight = 500;

    // level
    Spinner spinnerLevel;

    @Override
    public void start(Stage primaryStage) throws IOException {

        // Define grid pane
        GridPane grid;
        grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(25, 25, 25, 25));

        // For debug purposes
        // Make de grid lines visible
        // grid.setGridLinesVisible(true);
        // Drawing panel for Koch fractal
        kochPanel = new Canvas(kpWidth, kpHeight);
        grid.add(kochPanel, 0, 1, 25, 1);

        // Labels to present number of edges for Koch fractal
        labelNrEdges = new Label("Nr edges:");
        labelNrEdgesText = new Label();
        labelNrEdgesText.setText("" + edges.size());
        grid.add(labelNrEdges, 0, 0, 4, 1);
        grid.add(labelNrEdgesText, 4, 0, 22, 1);

        // Label to present current level of Koch fractal
        labelLevel = new Label("Level: " + currentLevel);
        grid.add(labelLevel, 4, 2);

        spinnerLevel = new Spinner(1, 10, 1);
        grid.add(spinnerLevel, 0, 3);

        // Button to fit Koch fractal in Koch panel
        Button buttonFitFractal = new Button();
        buttonFitFractal.setText("Fit Fractal");
        buttonFitFractal.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                fitFractalButtonActionPerformed(event);
            }

        });
        grid.add(buttonFitFractal, 0, 2);

        Button buttonEdgesList = new Button();
        buttonEdgesList.setText("Get List");
        buttonEdgesList.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                edges.clear();
                sc = new SocketClient();
                edges = sc.getEdges(getSpinnerLevel());
                requestDrawEdges();
            }
        });
        grid.add(buttonEdgesList, 1, 3);

        Button buttonEdgesLos = new Button();
        buttonEdgesLos.setText("Get Individual");
        buttonEdgesLos.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                edges.clear();
                clearKochPanel();
                sc = new SocketClient();
                sc.addObserver(JSF31KochFractalFX.this);
                sc.getEdge(getSpinnerLevel());
            }
        });
        grid.add(buttonEdgesLos, 2, 3);

        // Add mouse clicked event to Koch panel
        kochPanel.addEventHandler(MouseEvent.MOUSE_CLICKED,
                new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent event) {
                        kochPanelMouseClicked(event);
                    }
                });

        // Add mouse pressed event to Koch panel
        kochPanel.addEventHandler(MouseEvent.MOUSE_PRESSED,
                new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent event) {
                        kochPanelMousePressed(event);
                    }
                });

        // Add mouse dragged event to Koch panel
        kochPanel.setOnMouseDragged(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                kochPanelMouseDragged(event);
            }
        });

        // Create Koch manager and set initial level
        resetZoom();

        // Create the scene and add the grid pane
        Group root = new Group();
        Scene scene = new Scene(root, kpWidth + 50, kpHeight + 135);
        root.getChildren().add(grid);

        // Define title and assign the scene for main window
        primaryStage.setTitle("Koch Fractal");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public int getSpinnerLevel() {
        return (int) spinnerLevel.getValue();
    }

    public void drawEdges() {
        this.clearKochPanel();
        for (Edge e : edges) {
            Platform.runLater(() -> this.drawEdge(e));
        }
    }

    public void clearKochPanel() {
        GraphicsContext gc = kochPanel.getGraphicsContext2D();
        gc.clearRect(0.0, 0.0, kpWidth, kpHeight);
        gc.setFill(Color.BLACK);
        gc.fillRect(0.0, 0.0, kpWidth, kpHeight);
    }

    public void drawEdge(Edge e) {
        // Graphics
        GraphicsContext gc = kochPanel.getGraphicsContext2D();

        // Adjust edge for zoom and drag
        Edge e1 = edgeAfterZoomAndDrag(e);

        // Set line color
        gc.setStroke(Color.web(e1.color));

        // Set line width depending on level
        if (currentLevel <= 3) {
            gc.setLineWidth(2.0);
        } else if (currentLevel <= 5) {
            gc.setLineWidth(1.5);
        } else {
            gc.setLineWidth(1.0);
        }

        // Draw line
        gc.strokeLine(e1.X1, e1.Y1, e1.X2, e1.Y2);
    }

    public void setTextNrEdges(String text) {
        labelNrEdgesText.setText(text);
    }

    public void requestDrawEdges() {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                drawEdges();
            }
        });
    }

    private void fitFractalButtonActionPerformed(ActionEvent event) {
        resetZoom();
        this.drawEdges();
    }

    private void kochPanelMouseClicked(MouseEvent event) {
        if (Math.abs(event.getX() - startPressedX) < 1.0
                && Math.abs(event.getY() - startPressedY) < 1.0) {
            double originalPointClickedX = (event.getX() - zoomTranslateX) / zoom;
            double originalPointClickedY = (event.getY() - zoomTranslateY) / zoom;
            if (event.getButton() == MouseButton.PRIMARY) {
                zoom *= 2.0;
            } else if (event.getButton() == MouseButton.SECONDARY) {
                zoom /= 2.0;
            }
            zoomTranslateX = (int) (event.getX() - originalPointClickedX * zoom);
            zoomTranslateY = (int) (event.getY() - originalPointClickedY * zoom);
            this.drawEdges();
        }
    }

    private void kochPanelMouseDragged(MouseEvent event) {
        zoomTranslateX = zoomTranslateX + event.getX() - lastDragX;
        zoomTranslateY = zoomTranslateY + event.getY() - lastDragY;
        lastDragX = event.getX();
        lastDragY = event.getY();
        this.drawEdges();
    }

    private void kochPanelMousePressed(MouseEvent event) {
        startPressedX = event.getX();
        startPressedY = event.getY();
        lastDragX = event.getX();
        lastDragY = event.getY();
    }

    private void resetZoom() {
        int kpSize = Math.min(kpWidth, kpHeight);
        zoom = kpSize;
        zoomTranslateX = (kpWidth - kpSize) / 2.0;
        zoomTranslateY = (kpHeight - kpSize) / 2.0;
    }

    private Edge edgeAfterZoomAndDrag(Edge e) {
        return new Edge(
                e.X1 * zoom + zoomTranslateX,
                e.Y1 * zoom + zoomTranslateY,
                e.X2 * zoom + zoomTranslateX,
                e.Y2 * zoom + zoomTranslateY,
                e.color);
    }

    public static void main(String[] args) {
        Application.launch(args);
    }

    public void requestDrawEdge(final Edge e) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                drawEdge(e);
            }
        });
    }

    @Override
    public void update(Observable o, Object arg) {
        requestDrawEdge((Edge) arg);
    }
}
