/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Server;

import Shared.Edge;
import TimeStamp.TimeStamp;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.paint.Color;

/**
 *
 * @author jsf3
 */
public class FileManager {
    
    private final File fileMapped;    
    private final FileOutputStream writer;
    
    
    public FileManager(int id) throws FileNotFoundException{
        this.fileMapped = new File("/media/Fractal/fileMapped" + id +".tmp");
        this.writer = new FileOutputStream(fileMapped);        
    }
    
    public void writeFileMapped(List<Edge> edges) throws IOException {        
        this.fileMapped.delete();
        FileChannel fc = new RandomAccessFile(this.fileMapped, "rw").getChannel();
        long bufferSize = 8 * 1000000;
        MappedByteBuffer mappedBB = fc.map(FileChannel.MapMode.READ_WRITE, 0, bufferSize);
        long counter = 0;
        mappedBB.putInt(edges.size());
        for (Edge edge : edges) {
            mappedBB.putDouble(edge.X1);
            mappedBB.putDouble(edge.Y1);
            mappedBB.putDouble(edge.X2);
            mappedBB.putDouble(edge.Y2);
            mappedBB.putDouble(Color.valueOf(edge.color).getRed());
            mappedBB.putDouble(Color.valueOf(edge.color).getGreen());
            mappedBB.putDouble(Color.valueOf(edge.color).getBlue());
            counter++;
        }
        System.out.println("Total edges written: " + counter);
    }
    
    public List<Edge> readFileMapped() throws IOException {
        List<Edge> edges = new ArrayList();
        try (RandomAccessFile aFile = new RandomAccessFile(this.fileMapped.getAbsolutePath(), "r"); FileChannel inChannel = aFile.getChannel()) {
            MappedByteBuffer buffer = inChannel.map(FileChannel.MapMode.READ_ONLY, 0, inChannel.size());
            buffer.load();
            int size = buffer.getInt();
            for (int i = 0; i < size; i++) {
                double X1 = buffer.getDouble();
                double Y1 = buffer.getDouble();
                double X2 = buffer.getDouble();
                double Y2 = buffer.getDouble();
                String color = new Color(buffer.getDouble(), buffer.getDouble(), buffer.getDouble(), 1).toString();

                //create edge
                Edge edge = new Edge(X1, Y1, X2, Y2, color);
                edges.add(edge);
            }
            System.out.println("number of edges: " + edges.size());            
            buffer.clear(); // do something with the data and clear/compact it.
            return edges;
        }
    }
    
}
