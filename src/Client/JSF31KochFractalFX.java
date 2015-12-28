/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Client;

import java.io.IOException;
import java.util.ArrayList;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
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
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.control.Spinner;
import TimeStamp.TimeStamp;

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
    private Label labelDrawingProgress;
    private Label labelDrawingProgressCurrent;
    private Label labelDrawingProgressOutOf;
    private Label labelDrawingProgressMax;
    private ProgressBar pbDrawing;
    private Label labelDraw;
    private Label labelDrawText;

    // Koch panel and its size
    private Canvas kochPanel;
    private final int kpWidth = 500;
    private final int kpHeight = 500;

    private TimeStamp tsGenerate = new TimeStamp();

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

        // Drawing panel for Koch fractal
        kochPanel = new Canvas(kpWidth, kpHeight);

        // Labels to present number of edges for Koch fractal
        labelNrEdges = new Label("Nr edges:");
        labelNrEdgesText = new Label();
        labelNrEdgesText.setText("" + edges.size());
        labelDrawingProgress = new Label("Drawing progress:");
        pbDrawing = new ProgressBar(-1.0);
        labelDrawingProgressCurrent = new Label("0");
        labelDrawingProgressOutOf = new Label("out of");
        labelDrawingProgressMax = new Label("0");

        // Labels to present time of drawing for Koch fractal
        labelDraw = new Label("Drawing time:");
        labelDrawText = new Label("0 mSec");
        // Label to present current level of Koch fractal
        labelLevel = new Label("Level: 0");
        spinnerLevel = new Spinner(1, 10, 1);

        // Button to fit Koch fractal in Koch panel
        Button buttonFitFractal = new Button();
        buttonFitFractal.setText("Fit Fractal");
        buttonFitFractal.setOnAction((ActionEvent event) -> {
            fitFractalButtonActionPerformed(event);
        });

        Button buttonEdgesList = new Button();
        buttonEdgesList.setText("Get List");
        buttonEdgesList.setOnAction((ActionEvent event) -> {
            edges.clear();
            updateGui();
            sc = new SocketClient();
            tsGenerate.setBegin();
            edges = sc.getEdges(getSpinnerLevel());
            requestDrawEdges();
            tsGenerate.setEnd();
            labelDrawText.setText(tsGenerate.toString());
            tsGenerate.init();
        });

        Button buttonEdgesLos = new Button();
        buttonEdgesLos.setText("Get Individual");
        buttonEdgesLos.setOnAction((ActionEvent event) -> {
            edges.clear();
            updateGui();
            clearKochPanel();
            sc = new SocketClient();
            tsGenerate.setBegin();
            sc.addObserver(JSF31KochFractalFX.this);
            sc.getEdge(getSpinnerLevel());
            tsGenerate.setEnd();
            labelDrawText.setText(tsGenerate.toString());
            tsGenerate.init();
        });

        // Add mouse clicked event to Koch panel
        kochPanel.addEventHandler(MouseEvent.MOUSE_CLICKED, (MouseEvent event) -> {
            kochPanelMouseClicked(event);
        });

        // Add mouse pressed event to Koch panel
        kochPanel.addEventHandler(MouseEvent.MOUSE_PRESSED, (MouseEvent event) -> {
            kochPanelMousePressed(event);
        });

        // Add mouse dragged event to Koch panel
        kochPanel.setOnMouseDragged((MouseEvent event) -> {
            kochPanelMouseDragged(event);
        });

        grid.add(labelNrEdges, 0, 0, 4, 1);
        grid.add(labelNrEdgesText, 1, 0, 22, 1);
        grid.add(labelDrawingProgress, 0, 1, 2, 1);
        grid.add(pbDrawing, 1, 1, 12, 1);
        grid.add(labelDrawingProgressCurrent, 3, 1);
        grid.add(labelDrawingProgressOutOf, 4, 1);
        grid.add(labelDrawingProgressMax, 5, 1);
        grid.add(labelDraw, 0, 2, 4, 1);
        grid.add(labelDrawText, 1, 2, 22, 1);
        grid.add(kochPanel, 0, 3, 25, 1);
        grid.add(buttonFitFractal, 0, 4);
        grid.add(labelLevel, 1, 4);
        grid.add(spinnerLevel, 0, 5);
        grid.add(buttonEdgesList, 1, 5);
        grid.add(buttonEdgesLos, 2, 5);

        // Create Koch manager and set initial level
        resetZoom();
        clearKochPanel();

        // Create the scene and add the grid pane
        Group root = new Group();
        Scene scene = new Scene(root, kpWidth + 50, kpHeight + 190);
        root.getChildren().add(grid);

        // Define title and assign the scene for main window
        primaryStage.setTitle("Koch Fractal");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public void updateGui() {
        this.labelNrEdgesText.setText("" + (int) (3 * Math.pow(4, getSpinnerLevel() - 1)));
        this.labelLevel.setText("" + getSpinnerLevel());
        this.labelDrawingProgressCurrent.setText("0");
        this.labelDrawingProgressMax.setText("0");
    }

    public int getSpinnerLevel() {
        return (int) spinnerLevel.getValue();
    }

    public ProgressBar getpbDrawing() {
        return pbDrawing;
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
        sc = new SocketClient();
        return sc.edgeAfterZoomAndDrag(zoom, zoomTranslateX, zoomTranslateY, e);
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
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                labelDrawingProgressMax.setText(labelNrEdgesText.getText());
                labelDrawingProgressCurrent.setText("" + (Integer.parseInt(labelDrawingProgressCurrent.getText()) + 1));
                pbDrawing.progressProperty().set(Integer.parseInt(labelDrawingProgressCurrent.getText()) / Integer.parseInt(labelDrawingProgressMax.getText()));
                requestDrawEdge((Edge) arg);
            }
        });
    }
}
