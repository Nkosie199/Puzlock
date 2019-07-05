import java.util.ArrayList;

/**
 * A Java program for Dijkstra's single source shortest path algorithm. 
 * @author Nkosi Gumede
 */

public class ShortestPath { 
    Puzlock puzlock = new Puzlock(); //we will need some Puzlock methods to get the current neighbours etc
    ArrayList<Voxel> visitedVoxels; //the set of unvisited nodes
    ArrayList<Voxel> unvisitedVoxels = new ArrayList<>(); //the set of visited nodes
    int maxDistance = 1000000; //represents the maximum distance (which is set upon initialization) i.e. infinity. Must be > total number of voxels
    ArrayList currentShortestPath; //stores the set of voxels with represent the shortest path a voxels A to B. Stored from B to A
    ArrayList<ArrayList> shortestPathCandidates = new ArrayList<>();; //an array which stores the set of voxels in a shortest path
    static Voxel anchorVoxel; //each and every shortest path must have one anchor voxel which is furthest away the seed of the size opposite the normal diraction
    
    /* takes in the voxel array, source (seed), destination (blockee) and the blocking voxel*/
    public ShortestPath(ArrayList<Voxel> voxels, Voxel source, Voxel destination, Voxel blocking){
        //0. initialize the variables...
        visitedVoxels = new ArrayList<>();
        unvisitedVoxels = (ArrayList)voxels.clone(); //since all nodes are initially unvisted; beware of the shrinking list concurrency problem by ensuring Puzlock.voxels does not shrink
        unvisitedVoxels.remove(blocking); //remove the blocking voxel from the set of unvisited voxels
        
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
            if (v.y > blocking.y){ //if the current voxel is below the blocking voxel 
                unvisitedVoxels.remove(v); //remove the voxel below the blocking voxel from the set of unvisited voxels
            }
        }
        Voxel currentVoxel = source;
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
        currentShortestPath = new ArrayList<>();
        Voxel currentVoxel = destination;
        while (currentVoxel != start){
            System.out.print(currentVoxel.getCoordinates()+" <-- ");
            currentShortestPath.add(currentVoxel);
            currentVoxel = currentVoxel.previousVertex;
        }
        currentShortestPath.add(start);
        System.out.println(start.getCoordinates());
        setAnchorVoxel(currentShortestPath); //set the anchor voxel among the set of voxels in the current shortest path
        shortestPathCandidates.add(currentShortestPath); //finally, add the shortest path to the list of shortest path candidates
        
    }
    
    void setAnchorVoxel(ArrayList<Voxel> path){
        //get the side opposite to the seed's normal direction...
        Voxel seed = puzlock.seedVoxel;//stores the seed voxel
        String normalDir = seed.normalDirection;
        int noOfCandidates = path.size();
        if (normalDir.equals("left")){
            //then we want to pick the voxel to the far right...
            System.out.print("Among "+noOfCandidates+" candidates. ");
            System.out.println("Picking an anchor at the far right...");
            int rightness = 0; //indicates the position of the rightest voxel, initialized as left i.e. 0 (left)
            for (int i=1; i<(noOfCandidates-1); i++) { //for each voxel excluding the source and destination voxels
                Voxel currentVoxel = path.get(i); //get the current voxel
                //make sure it is on the same y (and z???)
                if ((currentVoxel.y == seed.y) && (currentVoxel.x > seed.x) && (currentVoxel.x > rightness)){ //if current voxel is on the same y co-ordinate of the seed and to the right of the seed and more right than any other
                    anchorVoxel = currentVoxel;
                    rightness = currentVoxel.x; //update the rightest position
                }
            }
            if (anchorVoxel != null){
                System.out.println("Anchor voxel is at "+anchorVoxel.getCoordinates()); //debug print the anchor voxel
            }else{
                System.out.println("Anchor voxel is null");
            }
        }else if (normalDir.equals("right")){
            //then we want to pick the voxel to the far left...
            System.out.print("Among "+noOfCandidates+" candidates. ");
            System.out.println("Picking an anchor at the far left...");
            int leftness = puzlock.inputVoxelizedMeshSize; //indicates the position of the rightest voxel, initialized as right i.e. inputVoxelizedMeshSize
            for (int i=1; i<(noOfCandidates-1); i++) { //for each voxel excluding the source and destination voxels
                Voxel currentVoxel = path.get(i); //get the current voxel
                if ((currentVoxel.y == seed.y) && (currentVoxel.x < seed.x) && (currentVoxel.x < leftness)){ //if current voxel is on the same y co-ordinate of the seed and to the left of the seed and more left than any other
                    anchorVoxel = currentVoxel;
                    leftness = currentVoxel.x; //update the leftest position
                }
            }
            if (anchorVoxel != null){
                System.out.println("Anchor voxel is at "+anchorVoxel.getCoordinates()); //debug print the anchor voxel
            }else{
                System.out.println("Anchor voxel is null");
            }
        }else if (normalDir.equals("up")){
            System.out.print("Among "+noOfCandidates+" candidates. ");
            System.out.println("Picking an anchor at the far top...");
            //then we want to pick the voxel to the far bottom...
            int bottomness = 0; //indicates the position of the lowest voxel, initialized as bottom i.e. 0 (top)
            for (int i=1; i<(noOfCandidates-1); i++) { //for each voxel excluding the source and destination voxels
                Voxel currentVoxel = path.get(i); //get the current voxel
                if ((currentVoxel.y >= seed.y) && (currentVoxel.y > bottomness)){ //if current voxel is on the same y co-ordinate of the seed or lower and the lowest
                    anchorVoxel = currentVoxel;
                    bottomness = currentVoxel.y; //update the bottomest position
                }
            }
            if (anchorVoxel != null){
                System.out.println("Anchor voxel is at "+anchorVoxel.getCoordinates()); //debug print the anchor voxel
            }else{
                System.out.println("Anchor voxel is null");
            }
        }else if (normalDir.equals("down")){
            System.out.print("Among "+noOfCandidates+" candidates. ");
            System.out.println("Picking an anchor at the far bottom...");
            //then we want to pick the voxel to the far top...
            int topness = puzlock.inputVoxelizedMeshSize;; //indicates the position of the topest voxel, initialized as bottom i.e. inputVoxelizedMeshSize
            for (int i=1; i<(noOfCandidates-1); i++) { //for each voxel excluding the source and destination voxels
                Voxel currentVoxel = path.get(i); //get the current voxel
                if ((currentVoxel.y <= seed.y) && (currentVoxel.y < topness)){ //if current voxel is on the same y co-ordinate of the seed or higher and the highest
                    anchorVoxel = currentVoxel;
                    topness = currentVoxel.y; //update the topest position
                }
            }
            if (anchorVoxel != null){
                System.out.println("Anchor voxel is at "+anchorVoxel.getCoordinates()); //debug print the anchor voxel
            }else{
                System.out.println("Anchor voxel is null");
            }
        }else if (normalDir.equals("forward")){
            System.out.print("Among "+noOfCandidates+" candidates. ");
            System.out.println("Picking an anchor at the far back...");
            //then we want to pick the voxel to the far back...
            int backness = puzlock.inputVoxelizedMeshSize; //indicates the position of the backest voxel, initialized as forward i.e. inputVoxelizedMeshSize
            for (int i=1; i<(noOfCandidates-1); i++) { //for each voxel excluding the source and destination voxels
                Voxel currentVoxel = path.get(i); //get the current voxel
                if ((currentVoxel.y == seed.y) && (currentVoxel.z < seed.z) && (currentVoxel.z < backness)){ //if current voxel is on the same y co-ordinate of the seed and to the right of the seed and more right than any other
                    anchorVoxel = currentVoxel;
                    backness = currentVoxel.z; //update the backest position
                }
            }
            if (anchorVoxel != null){
                System.out.println("Anchor voxel is at "+anchorVoxel.getCoordinates()); //debug print the anchor voxel
            }else{
                System.out.println("Anchor voxel is null");
            }
        }else if (normalDir.equals("backward")){
            System.out.print("Among "+noOfCandidates+" candidates. ");
            System.out.println("Picking an anchor at the far front...");
            //then we want to pick the voxel to the far front...
            int forwardness = 0; //indicates the position of the forwardest voxel, initialized as back i.e. 0
            for (int i=1; i<(noOfCandidates-1); i++) { //for each voxel excluding the source and destination voxels
                Voxel currentVoxel = path.get(i); //get the current voxel
                if ((currentVoxel.y == seed.y) && (currentVoxel.z > seed.z) && (currentVoxel.z > forwardness)){ //if current voxel is on the same y co-ordinate of the seed and to the right of the seed and more right than any other
                    anchorVoxel = currentVoxel;
                    forwardness = currentVoxel.z; //update the forwardest position
                }
            }
            if (anchorVoxel != null){
                System.out.println("Anchor voxel is at "+anchorVoxel.getCoordinates()); //debug print the anchor voxel
            }else{
                System.out.println("Anchor voxel is null");
            }
        }
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