import java.util.ArrayList;

/**
 * A Java program for Dijkstra's single source shortest path algorithm.
 * This class is implemented by a method in Puzlock2.java
 * @author Nkosi Gumede
 */

public class ShortestPath2 { 
    static Puzlock puzlock = new Puzlock(); //we will need some Puzlock methods to get the current neighbours etc
    ArrayList<Voxel> visitedVoxels; //the set of unvisited nodes
    ArrayList<Voxel> unvisitedVoxels = new ArrayList<>(); //the set of visited nodes
    int maxDistance = 1000000; //represents the maximum distance (which is set upon initialization) i.e. infinity. Must be > total number of voxels
    static ArrayList currentShortestPath; //stores the set of voxels with represent the shortest path a voxels A to B. Stored from B to A
    public static int cost = 0; //stores the cost which is to be returned as per Puzlock2 (section 5.2.2 - createInitialPafter)
            
    /* takes in the voxel array, source (seed), destination (blockee) and the blocking (the set of voxels which should not be visited) voxels*/
    public ShortestPath2(ArrayList<Voxel> voxels, Voxel source, Voxel destination, int[][][] inputVoxelizedMesh, int inputVoxelizedMeshSize){
        //0. initialize the variables...
        visitedVoxels = new ArrayList<>();
        unvisitedVoxels = (ArrayList)voxels.clone(); //since all nodes are initially unvisted; beware of the shrinking list concurrency problem by ensuring Puzlock.voxels does not shrink
        //initialize the source vertex
        source.shortestDistanceFromSource = 0;
        source.previousVertex = null;
        //initialize the other vertices...
        for (Voxel v: voxels){
            //if current voxel is not equal to source (OR BLOCKING), initialize it with the default values
            if (!v.equals(source)){
                v.shortestDistanceFromSource = maxDistance;
                v.previousVertex = null;
            }
        }
        Voxel currentVoxel = source;
        //1. Visit the unvisited vertex with the smallest know distance from the start vertex...
        while (!unvisitedVoxels.isEmpty()) {
            Voxel leftNeighbour = puzlock.getLeft(currentVoxel.x, currentVoxel.y, currentVoxel.z, inputVoxelizedMesh, inputVoxelizedMeshSize, voxels); 
            Voxel rightNeighbour = puzlock.getRight(currentVoxel.x, currentVoxel.y, currentVoxel.z, inputVoxelizedMesh, inputVoxelizedMeshSize, voxels); 
            Voxel upNeighbour = puzlock.getUp(currentVoxel.x, currentVoxel.y, currentVoxel.z, inputVoxelizedMesh, inputVoxelizedMeshSize, voxels);
            Voxel downNeighbour = puzlock.getDown(currentVoxel.x, currentVoxel.y, currentVoxel.z, inputVoxelizedMesh, inputVoxelizedMeshSize, voxels);
            Voxel forwardNeighbour = puzlock.getForward(currentVoxel.x, currentVoxel.y, currentVoxel.z, inputVoxelizedMesh, inputVoxelizedMeshSize, voxels);
            Voxel backwardNeighbour = puzlock.getBackward(currentVoxel.x, currentVoxel.y, currentVoxel.z, inputVoxelizedMesh, inputVoxelizedMeshSize, voxels);          
            if ((leftNeighbour != null) && (leftNeighbour.shortestDistanceFromSource > currentVoxel.shortestDistanceFromSource + 1)){ //if there is a left neighbour and it is not the blocking voxel and its new shortest distance is less than the old one
                leftNeighbour.shortestDistanceFromSource = currentVoxel.shortestDistanceFromSource + 1; //set its distance equal to the distance of the current voxel + distance to its neighbour   
                leftNeighbour.previousVertex = currentVoxel;//set its previous voxel to the current voxel
            }if ((rightNeighbour != null) && (rightNeighbour.shortestDistanceFromSource > currentVoxel.shortestDistanceFromSource + 1)){ //if there is a right neighbour and its new shortest distance is less than the old one
                rightNeighbour.shortestDistanceFromSource = currentVoxel.shortestDistanceFromSource + 1; //set its distance equal to the distance of the current voxel + distance to its neighbour   
                rightNeighbour.previousVertex = currentVoxel;//set its previous voxel to the current voxel
            }if ((upNeighbour != null) && (upNeighbour.shortestDistanceFromSource > currentVoxel.shortestDistanceFromSource + 1)){ //if there is an up neighbour and its new shortest distance is less than the old one
                upNeighbour.shortestDistanceFromSource = currentVoxel.shortestDistanceFromSource + 1; //set its distance equal to the distance of the current voxel + distance to its neighbour   
                upNeighbour.previousVertex = currentVoxel;//set its previous voxel to the current voxel
            }if ((downNeighbour != null) && (downNeighbour.shortestDistanceFromSource > currentVoxel.shortestDistanceFromSource + 1)){ //if there is a down neighbour and its new shortest distance is less than the old one
                downNeighbour.shortestDistanceFromSource = currentVoxel.shortestDistanceFromSource + 1; //set its distance equal to the distance of the current voxel + distance to its neighbour   
                downNeighbour.previousVertex = currentVoxel;//set its previous voxel to the current voxel
            }if ((forwardNeighbour != null) && (forwardNeighbour.shortestDistanceFromSource > currentVoxel.shortestDistanceFromSource + 1)){ //if there is a forward neighbour and its new shortest distance is less than the old one
                forwardNeighbour.shortestDistanceFromSource = currentVoxel.shortestDistanceFromSource + 1; //set its distance equal to the distance of the current voxel + distance to its neighbour   
                forwardNeighbour.previousVertex = currentVoxel;//set its previous voxel to the current voxel
            }if ((backwardNeighbour != null) && (backwardNeighbour.shortestDistanceFromSource > currentVoxel.shortestDistanceFromSource + 1)){ //if there is a backward neighbour and its new shortest distance is less than the old one
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
        }
        //now that we have all the appropriate values as per the table in Djikstra's algorithm, show us the shortest path...
        getShortestPath(source, destination); //prints out the path from source to destination in terms of which voxel co-ordinates must be visited 
    }
    
    void getShortestPath(Voxel start, Voxel destination){
        //System.out.println("Printing the path from source to destination...");
        currentShortestPath = new ArrayList<>();
        Voxel currentVoxel = destination;
        while (currentVoxel != start){
            System.out.print(currentVoxel.getCoordinates()+" <-- ");
            currentShortestPath.add(currentVoxel);
            currentVoxel = currentVoxel.previousVertex;
            cost++; //increment the cost by 1
        }
        currentShortestPath.add(start);
        System.out.println(start.getCoordinates()+" cost "+cost);
    }
    
    /* takes in a set of voxels (in a path) and an anchor voxel to make 1 puzzle piece, removable by adding the voxels above it (excluding the anchor voxel)*/
    static ArrayList<Voxel> makeRemovable(ArrayList<Voxel> path, String blockingDirection, int[][][] inputVMesh, int inputVMeshSize, ArrayList<Voxel> vs){
        //make the key piece removable then expand the key piece (next method)...
        //add all the voxels above the current set of voxels...
        ArrayList<Voxel> removablePiece = new ArrayList<>();
        for (int j=0; j<path.size(); j++){ //for each voxel in the path
            Voxel currentVoxel = path.get(j);
            if (blockingDirection.equals("left")){
                if ((!removablePiece.contains(currentVoxel)) ){ //if the current voxel is not already in the piece
                    removablePiece.add(currentVoxel); //add the current voxel
                    System.out.print(currentVoxel.getCoordinates()+"(in path); ");
                    for (int k=currentVoxel.x; k>=0; k--){ //for each x co-ordinate from the current voxel's x co-ordinate to left (0)
                        Voxel v = puzlock.getLeft(k, currentVoxel.y, currentVoxel.z, inputVMesh, inputVMeshSize, vs);
                        if ((v!=null) && (v.value==1) && (!removablePiece.contains(v))){ //if y co-ordinate is less than that of the current voxel i.e. on top of it and it has not been added yet
                            removablePiece.add(v); //add it to the set of candidate voxels (represented in figure 9(e))
                            System.out.print(v.getCoordinates()+"(at left); ");
                        }
                    }
                }
            }else if (blockingDirection.equals("right")){
                if ((!removablePiece.contains(currentVoxel)) ){ //if the current voxel is not already in the piece
                    removablePiece.add(currentVoxel); //add the current voxel
                    System.out.print(currentVoxel.getCoordinates()+"(in path); ");
                    for (int k=currentVoxel.x; k<inputVMeshSize; k++){ //for each y co-ordinate from the current voxel's y co-ordinate to top (0)
                        Voxel v = puzlock.getRight(k, currentVoxel.y, currentVoxel.z, inputVMesh, inputVMeshSize, vs);
                        if ((v!=null) && (v.value==1) && (!removablePiece.contains(v))){ //if y co-ordinate is less than that of the current voxel i.e. on top of it and it has not been added yet
                            removablePiece.add(v); //add it to the set of candidate voxels (represented in figure 9(e))
                            System.out.print(v.getCoordinates()+"(at right); ");
                        }
                    }
                }
            }else if (blockingDirection.equals("up")){
                if ((!removablePiece.contains(currentVoxel)) ){ //if the current voxel is not already in the piece
                    removablePiece.add(currentVoxel); //add the current voxel
                    System.out.print(currentVoxel.getCoordinates()+"(in path); ");
                    for (int k=currentVoxel.y; k>=0; k--){ //for each y co-ordinate from the current voxel's y co-ordinate to top (0)
                        Voxel v = puzlock.getUp(currentVoxel.x, k, currentVoxel.z, inputVMesh, inputVMeshSize, vs);
                        if ((v!=null) && (v.value==1) && (!removablePiece.contains(v))){ //if y co-ordinate is less than that of the current voxel i.e. on top of it and it has not been added yet
                            removablePiece.add(v); //add it to the set of candidate voxels (represented in figure 9(e))
                            System.out.print(v.getCoordinates()+"(on top); ");
                        }
                    }
                }
            }else if (blockingDirection.equals("down")){
                if ((!removablePiece.contains(currentVoxel)) ){ //if the current voxel is not already in the piece
                    removablePiece.add(currentVoxel); //add the current voxel
                    System.out.print(currentVoxel.getCoordinates()+"(in path); ");
                    for (int k=currentVoxel.y; k<inputVMeshSize; k++){ //for each y co-ordinate from the current voxel's y co-ordinate to top (0)
                        Voxel v = puzlock.getDown(currentVoxel.x, k, currentVoxel.z, inputVMesh, inputVMeshSize, vs);
                        if ((v!=null) && (v.value==1) && (!removablePiece.contains(v))){ //if y co-ordinate is less than that of the current voxel i.e. on top of it and it has not been added yet
                            removablePiece.add(v); //add it to the set of candidate voxels (represented in figure 9(e))
                            System.out.print(v.getCoordinates()+"(beneath); ");
                        }
                    }
                }
            }else if (blockingDirection.equals("forward")){
                if ((!removablePiece.contains(currentVoxel)) ){ //if the current voxel is not already in the piece
                    removablePiece.add(currentVoxel); //add the current voxel
                    System.out.print(currentVoxel.getCoordinates()+"(in path); ");
                    for (int k=currentVoxel.z; k<inputVMeshSize; k++){ //for each y co-ordinate from the current voxel's y co-ordinate to top (0)
                        Voxel v = puzlock.getForward(currentVoxel.x, currentVoxel.y, k, inputVMesh, inputVMeshSize, vs);
                        if ((v!=null) && (v.value==1) && (!removablePiece.contains(v))){ //if y co-ordinate is less than that of the current voxel i.e. on top of it and it has not been added yet
                            removablePiece.add(v); //add it to the set of candidate voxels (represented in figure 9(e))
                            System.out.print(v.getCoordinates()+"(forward); ");
                        }
                    }
                }
            }else if (blockingDirection.equals("backward")){
                if ((!removablePiece.contains(currentVoxel)) ){ //if the current voxel is not already in the piece
                    removablePiece.add(currentVoxel); //add the current voxel
                    System.out.print(currentVoxel.getCoordinates()+"(in path); ");
                    for (int k=currentVoxel.z; k>=0; k--){ //for each y co-ordinate from the current voxel's y co-ordinate to top (0)
                        Voxel v = puzlock.getBackward(currentVoxel.x, currentVoxel.y, k, inputVMesh, inputVMeshSize, vs);
                        if ((v!=null) && (v.value==1) && (!removablePiece.contains(v))){ //if y co-ordinate is less than that of the current voxel i.e. on top of it and it has not been added yet
                            removablePiece.add(v); //add it to the set of candidate voxels (represented in figure 9(e))
                            System.out.print(v.getCoordinates()+"(behind); ");
                        }
                    }
                }
            }
        }
        return removablePiece;
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