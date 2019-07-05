import java.util.ArrayList;

/**
 * A Java program for Dijkstra's single source shortest path algorithm. 
 * @author Nkosi Gumede
 */

public class ShortestPath { 
    Puzlock puzlock = new Puzlock(); //we will need some Puzlock methods to get the current neighbours etc
//    ArrayList<Vertex> vertices = new ArrayList<>();
    ArrayList<Voxel> visitedVoxels; //the set of unvisited nodes
    ArrayList<Voxel> unvisitedVoxels = new ArrayList<>(); //the set of visited nodes
    int maxDistance = 1000000; //represents the maximum distance (which is set upon initialization) i.e. infinity. Must be > total number of voxels
    
    /* takes in the voxel array, source (seed), destination (blockee) and the blocking voxel*/
    public ShortestPath(ArrayList<Voxel> voxels, Voxel source, Voxel destination, Voxel blocking){
        //0. initialize the variables...
        visitedVoxels = new ArrayList<>();
        unvisitedVoxels = voxels; //since all nodes are initially unvisted; beware of the shrinking list concurrency problem by ensuring Puzlock.voxels does not shrink
        unvisitedVoxels.remove(blocking); //remove the blocking voxel from the set of unvisited voxels
        //initialize the source vertex
        source.shortestDistanceFromSource = 0;
        source.previousVertex = null;
        //initialize the other vertices...
        for (Voxel v: voxels){
            //if current voxel is not equal to source (OR BLOCKING???)
            if (!v.equals(source)){
                v.shortestDistanceFromSource = maxDistance;
                v.previousVertex = null;
            }
        }
        Voxel currentVoxel = source;
        //debug print the vertices...
//        System.out.println("Debug printing all unvisited voxels as vertices...");
//        debugPrintVertices(unvisitedVoxels);
        //1. Visit the unvisited vertex with the smallest know distance from the start vertex...
        //for the current vertex v, visit the unvisited neighbours
        while (!unvisitedVoxels.isEmpty()) {
//            System.out.println("# of unvisited voxels: "+unvisitedVoxels.size());
            Voxel leftNeighbour = puzlock.getLeft(currentVoxel.x, currentVoxel.y, currentVoxel.z); 
            Voxel rightNeighbour = puzlock.getRight(currentVoxel.x, currentVoxel.y, currentVoxel.z); 
            Voxel upNeighbour = puzlock.getUp(currentVoxel.x, currentVoxel.y, currentVoxel.z);
            Voxel downNeighbour = puzlock.getDown(currentVoxel.x, currentVoxel.y, currentVoxel.z);
            Voxel forwardNeighbour = puzlock.getForward(currentVoxel.x, currentVoxel.y, currentVoxel.z);
            Voxel backwardNeighbour = puzlock.getBackward(currentVoxel.x, currentVoxel.y, currentVoxel.z);          
            if ((leftNeighbour != null) && (!leftNeighbour.equals(blocking)) && (leftNeighbour.shortestDistanceFromSource > currentVoxel.shortestDistanceFromSource + 1)){ //if there is a left neighbour and it is not the blocking voxel and its new shortest distance is less than the old one
                leftNeighbour.shortestDistanceFromSource = currentVoxel.shortestDistanceFromSource + 1; //set its distance equal to the distance of the current voxel + distance to its neighbour   
                leftNeighbour.previousVertex = currentVoxel;//set its previous voxel to the current voxel
            }if ((rightNeighbour != null) && (!rightNeighbour.equals(blocking)) && (rightNeighbour.shortestDistanceFromSource > currentVoxel.shortestDistanceFromSource + 1)){ //if there is a right neighbour and its new shortest distance is less than the old one
                rightNeighbour.shortestDistanceFromSource = currentVoxel.shortestDistanceFromSource + 1; //set its distance equal to the distance of the current voxel + distance to its neighbour   
                rightNeighbour.previousVertex = currentVoxel;//set its previous voxel to the current voxel
            }if ((upNeighbour != null) && (!upNeighbour.equals(blocking)) && (upNeighbour.shortestDistanceFromSource > currentVoxel.shortestDistanceFromSource + 1)){ //if there is an up neighbour and its new shortest distance is less than the old one
                upNeighbour.shortestDistanceFromSource = currentVoxel.shortestDistanceFromSource + 1; //set its distance equal to the distance of the current voxel + distance to its neighbour   
                upNeighbour.previousVertex = currentVoxel;//set its previous voxel to the current voxel
            }if ((downNeighbour != null) && (!downNeighbour.equals(blocking)) && (downNeighbour.shortestDistanceFromSource > currentVoxel.shortestDistanceFromSource + 1)){ //if there is a down neighbour and its new shortest distance is less than the old one
                downNeighbour.shortestDistanceFromSource = currentVoxel.shortestDistanceFromSource + 1; //set its distance equal to the distance of the current voxel + distance to its neighbour   
                downNeighbour.previousVertex = currentVoxel;//set its previous voxel to the current voxel
            }if ((forwardNeighbour != null) && (!forwardNeighbour.equals(blocking)) && (forwardNeighbour.shortestDistanceFromSource > currentVoxel.shortestDistanceFromSource + 1)){ //if there is a forward neighbour and its new shortest distance is less than the old one
                forwardNeighbour.shortestDistanceFromSource = currentVoxel.shortestDistanceFromSource + 1; //set its distance equal to the distance of the current voxel + distance to its neighbour   
                forwardNeighbour.previousVertex = currentVoxel;//set its previous voxel to the current voxel
            }if ((backwardNeighbour != null) && (!backwardNeighbour.equals(blocking)) && (backwardNeighbour.shortestDistanceFromSource > currentVoxel.shortestDistanceFromSource + 1)){ //if there is a backward neighbour and its new shortest distance is less than the old one
                backwardNeighbour.shortestDistanceFromSource = currentVoxel.shortestDistanceFromSource + 1; //set its distance equal to the distance of the current voxel + distance to its neighbour   
                backwardNeighbour.previousVertex = currentVoxel;//set its previous voxel to the current voxel
            }
            visitedVoxels.add(currentVoxel); //add to the set of visited voxels
            unvisitedVoxels.remove(currentVoxel);//remove from the set of unvisted voxels
            //now we must visited the first unvisited vertex with the smallest known distance from the start vertex...
            int smallestDistance = maxDistance; //stores the smallest known distance from the start vertex
            for (Voxel u: unvisitedVoxels) {
                //if the current voxel has a smaller distance than the smallest distance from the start vertex, set if to the current
                if (u.shortestDistanceFromSource < smallestDistance){
                    smallestDistance = u.shortestDistanceFromSource;//set voxel's shortest distance to the smallest distance
                    currentVoxel = u; //set voxel to the current voxel as we will now start from here
                }
            }
            //now that we have all the appropriate values as per the table in Djikstra's algorithm, show us the shortest path...
        }
        System.out.println("Debug printing all visited voxels as vertices...");
        debugPrintVertices(visitedVoxels);
        getShortestPath(source, destination); //prints out the path from source to destination in terms of which voxel co-ordinates must be visited 
    }
    
    void getShortestPath(Voxel start, Voxel destination){
        System.out.println("Printing the path from destination to source...");
        Voxel currentVoxel = destination;
        while (currentVoxel != start){
            System.out.print(currentVoxel.getCoordinates()+" --> ");
            currentVoxel = currentVoxel.previousVertex;
        }
        System.out.println(start.getCoordinates());
    }
    
    void debugPrintVertices(ArrayList<Voxel> voxels){
        for (int i = 0; i < voxels.size(); i++) {
            Voxel v = voxels.get(i);
            if (v.previousVertex == null){
                System.out.println(i+") Voxel at "+v.getCoordinates()+": Shortest distance from source: "+v.shortestDistanceFromSource+", Previous vertex: "+v.previousVertex);
            }else{
                System.out.println(i+") Voxel at "+v.getCoordinates()+": Shortest distance from source: "+v.shortestDistanceFromSource+", Previous vertex: "+v.previousVertex+" at "+v.previousVertex.getCoordinates());
            }
            
        }
    }
}