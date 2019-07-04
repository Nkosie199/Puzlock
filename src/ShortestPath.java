
import java.util.ArrayList;

/**
 * A Java program for Dijkstra's single source shortest path algorithm. 
 * @author Nkosi Gumede
 */

public class ShortestPath { 
    Puzlock puzlock = new Puzlock(); //we will need some Puzlock methods to get the current neighbours etc
    ArrayList<Vertex> vertices = new ArrayList<>();
    ArrayList<Voxel> visitedVoxels = new ArrayList<>(); //the set of unvisited nodes
    ArrayList<Voxel> unvisitedVoxels = new ArrayList<>(); //the set of visited nodes
    int maxDistance = 1000000; //represents the maximum distance (which is set upon initialization) i.e. infinity. Must be > total number of voxels
    
    /* takes in the voxel array, source (seed), destination (blockee) and the blocking voxel*/
    public ShortestPath(ArrayList<Voxel> voxels, Voxel source, Voxel destination, Voxel blocking){
        //0. initialize the variables...
        unvisitedVoxels = voxels; //since all nodes are initially unvisted; beware of the shrinking list concurrency problem by ensuring Puzlock.voxels does not shrink
        //initialize the source vertex
        vertices.add(new Vertex(source, 0, null));
        //initialize the other vertices...
        for (Voxel v: voxels){
            //if current voxel is not equal to source (OR BLOCKING???)
            if (!v.equals(source)){
                vertices.add(new Vertex(v, maxDistance, null));
            }
        }
        //debug print the vertices...
//        debugPrintVerices();
        //1. Visit the unvisited vertex with the smallest know distance from the start vertex...
        //for the current vertex v, visit the univisited neighbours
        for (Vertex v: vertices) {
            Voxel currentVoxel = v.id;
            Voxel leftNeighbour = puzlock.getLeft(currentVoxel.x, currentVoxel.y, currentVoxel.z); 
            Voxel rightNeighbour = puzlock.getRight(currentVoxel.x, currentVoxel.y, currentVoxel.z); 
            Voxel upNeighbour = puzlock.getUp(currentVoxel.x, currentVoxel.y, currentVoxel.z);
            Voxel downNeighbour = puzlock.getDown(currentVoxel.x, currentVoxel.y, currentVoxel.z);
            Voxel forwardNeighbour = puzlock.getForward(currentVoxel.x, currentVoxel.y, currentVoxel.z);
            Voxel backwardNeighbour = puzlock.getBackward(currentVoxel.x, currentVoxel.y, currentVoxel.z);
            
            if (leftNeighbour != null){ //if there is a left neighbour
                //set its distance equal to the distance of the current voxel + distance to its neighbour               
                
            }if (rightNeighbour != null){ //if there is a right neighbour
                
            }if (upNeighbour != null){ //if there is an up neighbour
                
            }if (downNeighbour != null){ //if there is a down neighbour
                
            }if (forwardNeighbour != null){ //if there is a forward neighbour
                
            }if (backwardNeighbour != null){ //if there is a backward neighbour
                
            }
        }
    }
    
    void debugPrintVerices(){
        System.out.println("Debug printing all vertices...");
        for (int i = 0; i < vertices.size(); i++) {
            Vertex v = vertices.get(i);
            System.out.println(i+") ID: "+v.id+", Shortest distance from source: "+v.shortestDistanceFromSource+", Previous vertex: "+v.previousVertex);
        }
    }
}

/* this class represents each vertex in the list of vertices */
class Vertex{
    Voxel id;
    int shortestDistanceFromSource;
    Voxel previousVertex;
    
    Vertex(Voxel id, int shortestDistFromSource, Voxel previousVertex){
        this.id = id;
        this.shortestDistanceFromSource = shortestDistFromSource;
        this.previousVertex = previousVertex;
    }
}
