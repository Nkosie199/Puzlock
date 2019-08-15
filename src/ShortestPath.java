import java.util.ArrayList;

/**
 * A Java program for Dijkstra's single source shortest path algorithm. 
 * @author Nkosi Gumede
 */

public class ShortestPath { 
    static Puzlock puzlock = new Puzlock(); //we will need some Puzlock methods to get the current neighbours etc
    ArrayList<Voxel> visitedVoxels; //the set of unvisited nodes
    ArrayList<Voxel> unvisitedVoxels = new ArrayList<>(); //the set of visited nodes
    int maxDistance = 1000000; //represents the maximum distance (which is set upon initialization) i.e. infinity. Must be > total number of voxels
    static ArrayList<Voxel> removablePiece; //stores piece given by the shortest path which have been made removable by adding the voxels above them
    static ArrayList currentShortestPath; //stores the set of voxels with represent the shortest path a voxels A to B. Stored from B to A
    static Voxel anchorVoxel = null; //each and every shortest path must have one anchor voxel which is furthest away the seed of the size opposite the normal direction
    static Voxel anchorVoxel2 = null; //stores the second anchor voxel as per section 4
    
    /* takes in the voxel array, source (seed), destination (blockee) and the blocking (the set of voxels which should not be visited) voxels*/
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
        while (!unvisitedVoxels.isEmpty()) {
            Voxel leftNeighbour = puzlock.getLeft(currentVoxel.x, currentVoxel.y, currentVoxel.z, puzlock.inputVoxelizedMesh, puzlock.inputVoxelizedMeshSize, puzlock.voxels); 
            Voxel rightNeighbour = puzlock.getRight(currentVoxel.x, currentVoxel.y, currentVoxel.z, puzlock.inputVoxelizedMesh, puzlock.inputVoxelizedMeshSize, puzlock.voxels); 
            Voxel upNeighbour = puzlock.getUp(currentVoxel.x, currentVoxel.y, currentVoxel.z, puzlock.inputVoxelizedMesh, puzlock.inputVoxelizedMeshSize, puzlock.voxels);
            Voxel downNeighbour = puzlock.getDown(currentVoxel.x, currentVoxel.y, currentVoxel.z, puzlock.inputVoxelizedMesh, puzlock.inputVoxelizedMeshSize, puzlock.voxels);
            Voxel forwardNeighbour = puzlock.getForward(currentVoxel.x, currentVoxel.y, currentVoxel.z, puzlock.inputVoxelizedMesh, puzlock.inputVoxelizedMeshSize, puzlock.voxels);
            Voxel backwardNeighbour = puzlock.getBackward(currentVoxel.x, currentVoxel.y, currentVoxel.z, puzlock.inputVoxelizedMesh, puzlock.inputVoxelizedMeshSize, puzlock.voxels);          
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
        }
        //now that we have all the appropriate values as per the table in Djikstra's algorithm, show us the shortest path...
        getShortestPath(source, destination); //prints out the path from source to destination in terms of which voxel co-ordinates must be visited 
        setAnchorVoxel(currentShortestPath); //set the anchor voxel among the set of voxels in the current shortest path
        makeRemovable(currentShortestPath, anchorVoxel); //make the piece removable by adding voxels above non-anchor voxels
        setAnchorVoxel2(anchorVoxel, blocking, removablePiece); //sets the second anchor voxel from the now removable piece
        //selectPiece(); //returns the piece with the smallest sum of accessibility values
    }
    
    void getShortestPath(Voxel start, Voxel destination){
        //System.out.println("Printing the path from source to destination...");
        currentShortestPath = new ArrayList<>();
        Voxel currentVoxel = destination;
        while (currentVoxel != start){
            System.out.print(currentVoxel.getCoordinates()+" <-- ");
            currentShortestPath.add(currentVoxel);
            currentVoxel = currentVoxel.previousVertex;
        }
        currentShortestPath.add(start);
        System.out.println(start.getCoordinates());
    }
    
    void setAnchorVoxel(ArrayList<Voxel> path){
        //get the side opposite to the seed's normal direction...
        Voxel seed = puzlock.seedVoxel;//stores the seed voxel
        String normalDir = seed.normalDirection;
        int noOfCandidates = path.size();
        System.out.print("Among "+noOfCandidates+" candidates. ");
        if (normalDir.equals("left")){
            System.out.print("Picking an anchor at the far right..."); //then we want to pick the voxel to the far right...
            int rightness = 0; //indicates the position of the rightest voxel, initialized as left i.e. 0 (left)
            for (int i=1; i<(noOfCandidates-1); i++){ //for each voxel excluding the source and destination voxels
                Voxel currentVoxel = path.get(i); //get the current voxel
                //make sure it is on the same y (and z???)
                if ((currentVoxel.y == seed.y) && (currentVoxel.x >= seed.x) && (currentVoxel.x > rightness)){ //if current voxel is on the same y co-ordinate of the seed and to the right of the seed and more right than any other
                    anchorVoxel = currentVoxel;
                    rightness = currentVoxel.x; //update the rightest position
                }
            }
            if (anchorVoxel == null){
                System.out.println("ERROR: Anchor voxel is null");
            }
        }else if (normalDir.equals("right")){
            System.out.println("Picking an anchor at the far left..."); //then we want to pick the voxel to the far left...
            int leftness = puzlock.inputVoxelizedMeshSize; //indicates the position of the rightest voxel, initialized as right i.e. inputVoxelizedMeshSize
            for (int i=1; i<(noOfCandidates-1); i++) { //for each voxel excluding the source and destination voxels
                Voxel currentVoxel = path.get(i); //get the current voxel
                if ((currentVoxel.y == seed.y) && (currentVoxel.x <= seed.x) && (currentVoxel.x < leftness)){ //if current voxel is on the same y co-ordinate of the seed and to the left of the seed and more left than any other
                    anchorVoxel = currentVoxel;
                    leftness = currentVoxel.x; //update the leftest position
                }
            }
            if (anchorVoxel == null){
                System.out.println("ERROR: Anchor voxel is null");
            }
        }else if (normalDir.equals("forward")){
            System.out.println("Picking an anchor at the far back..."); //then we want to pick the voxel to the far back...
            int backness = puzlock.inputVoxelizedMeshSize; //indicates the position of the backest voxel, initialized as forward i.e. inputVoxelizedMeshSize
            for (int i=1; i<(noOfCandidates-1); i++) { //for each voxel excluding the source and destination voxels
                Voxel currentVoxel = path.get(i); //get the current voxel
                if ((currentVoxel.y == seed.y) && (currentVoxel.z <= seed.z) && (currentVoxel.z < backness)){ //if current voxel is on the same y co-ordinate of the seed and to the right of the seed and more right than any other
                    anchorVoxel = currentVoxel;
                    backness = currentVoxel.z; //update the backest position
                }
            }
            if (anchorVoxel == null){
                System.out.println("ERROR: Anchor voxel is null");
            }
        }else if (normalDir.equals("backward")){
            System.out.println("Picking an anchor at the far front..."); //then we want to pick the voxel to the far front...
            int forwardness = 0; //indicates the position of the forwardest voxel, initialized as back i.e. 0
            for (int i=1; i<(noOfCandidates-1); i++) { //for each voxel excluding the source and destination voxels
                Voxel currentVoxel = path.get(i); //get the current voxel
                if ((currentVoxel.y == seed.y) && (currentVoxel.z >= seed.z) && (currentVoxel.z > forwardness)){ //if current voxel is on the same y co-ordinate of the seed and to the right of the seed and more right than any other
                    anchorVoxel = currentVoxel;
                    forwardness = currentVoxel.z; //update the forwardest position
                }
            }
            if (anchorVoxel == null){
                System.out.println("ERROR: Anchor voxel is null");
            }
        }
    }
    
    /* takes in the first anchor voxel, the blocking voxel, and the currently selected piece (lowest sum of accessibility values); sets the second anchor voxel */
    static void setAnchorVoxel2(Voxel anchor, Voxel blocking, ArrayList<Voxel> path){
        Voxel seed = puzlock.seedVoxel;//stores the seed voxel
        String normalDir = seed.normalDirection; //get the side opposite to the seed's normal direction...
        int noOfCandidates = path.size();
        System.out.print("Among "+noOfCandidates+" candidates. ");
        if (normalDir.equals("left")){
            System.out.println("Picking anchor2 at the far right..."); //then we want to pick the voxel to the far right...
            int rightness = 0; //indicates the position of the rightest voxel, initialized as left i.e. 0 (left)
            for (int i=1; i<(noOfCandidates-1); i++){ //for each voxel excluding the source and destination voxels
                Voxel currentVoxel = path.get(i); //get the current voxel
                //make sure it is on the same y (and z???)
                if ((currentVoxel != anchor) && (currentVoxel.y == seed.y) && (currentVoxel.x >= seed.x) && (currentVoxel.x > rightness)){ 
                    //if current voxel is not the anchor voxel, is on the same y co-ordinate of the seed and to the right of the seed and more right than any other
                    anchorVoxel2 = currentVoxel;
                    rightness = currentVoxel.x; //update the rightest position
                }
            }
            if (anchorVoxel2 != null){
                System.out.println("Anchor2 is at "+anchorVoxel2.getCoordinates()); //debug print the anchor voxel
            }else{
                anchorVoxel2 = blocking; //set the anchor voxel to the blocking voxel
                System.out.println("Anchor2 is at "+anchorVoxel2.getCoordinates()); //debug print the anchor voxel
            }
        }else if (normalDir.equals("right")){
            System.out.println("Picking anchor2 at the far left..."); //then we want to pick the voxel to the far left...
            int leftness = puzlock.inputVoxelizedMeshSize; //indicates the position of the rightest voxel, initialized as right i.e. inputVoxelizedMeshSize
            for (int i=1; i<(noOfCandidates-1); i++) { //for each voxel excluding the source and destination voxels
                Voxel currentVoxel = path.get(i); //get the current voxel
                if ((currentVoxel != anchor) && (currentVoxel.y == seed.y) && (currentVoxel.x <= seed.x) && (currentVoxel.x < leftness)){ 
                    //if current voxel is not the anchor voxel, on the same y co-ordinate of the seed and to the left of the seed and more left than any other
                    anchorVoxel2 = currentVoxel;
                    leftness = currentVoxel.x; //update the leftest position
                }
            }
            if (anchorVoxel2 != null){
                System.out.println("Anchor2 is at "+anchorVoxel2.getCoordinates()); //debug print the anchor voxel
            }else{
                anchorVoxel2 = blocking; //set the anchor voxel to the blocking voxel
                System.out.println("Anchor2 is at "+anchorVoxel2.getCoordinates()); //debug print the anchor voxel
            }
        }else if (normalDir.equals("forward")){
            System.out.println("Picking anchor2 at the far back..."); //then we want to pick the voxel to the far back...
            int backness = puzlock.inputVoxelizedMeshSize; //indicates the position of the backest voxel, initialized as forward i.e. inputVoxelizedMeshSize
            for (int i=1; i<(noOfCandidates-1); i++) { //for each voxel excluding the source and destination voxels
                Voxel currentVoxel = path.get(i); //get the current voxel
                if ((currentVoxel != anchor) && (currentVoxel.y == seed.y) && (currentVoxel.z <= seed.z) && (currentVoxel.z < backness)){ 
                    //if current voxel is not the anchor voxel, on the same y co-ordinate of the seed and to the right of the seed and more right than any other
                    anchorVoxel2 = currentVoxel;
                    backness = currentVoxel.z; //update the backest position
                }
            }
            if (anchorVoxel2 != null){
                System.out.println("Anchor2 is at "+anchorVoxel2.getCoordinates()); //debug print the anchor voxel
            }else{
                anchorVoxel2 = blocking; //set the anchor voxel to the blocking voxel
                System.out.println("Anchor2 is at "+anchorVoxel2.getCoordinates()); //debug print the anchor voxel
            }
        }else if (normalDir.equals("backward")){
            System.out.println("Picking anchor2 at the far front..."); //then we want to pick the voxel to the far front...
            int forwardness = 0; //indicates the position of the forwardest voxel, initialized as back i.e. 0
            for (int i=1; i<(noOfCandidates-1); i++) { //for each voxel excluding the source and destination voxels
                Voxel currentVoxel = path.get(i); //get the current voxel
                if ((currentVoxel != anchor) && (currentVoxel.y == seed.y) && (currentVoxel.z >= seed.z) && (currentVoxel.z > forwardness)){ 
                    //if current voxel is not the anchor voxel, is on the same y co-ordinate of the seed, and to the right of the seed, and more right than any other
                    anchorVoxel2 = currentVoxel;
                    forwardness = currentVoxel.z; //update the forwardest position
                }
            }
            if (anchorVoxel2 != null){
                System.out.println("Anchor2 is at "+anchorVoxel2.getCoordinates()); //debug print the anchor voxel
            }else{
                anchorVoxel2 = blocking; //set the anchor voxel to the blocking voxel
                System.out.println("Anchor2 is at "+anchorVoxel2.getCoordinates()); //debug print the anchor voxel
            }
        }
        System.out.println("\n********************************************************************");
    }
    
    
    /* takes in a set of voxels (in a path) and an anchor voxel to make 1 puzzle piece, removable by adding the voxels above it (excluding the anchor voxel)*/
    static void makeRemovable(ArrayList<Voxel> path, Voxel anchor){
        //make the key piece removable then expand the key piece (next method)...
        //add all the voxels above the current set of voxels...
        removablePiece = new ArrayList<>(); 
        System.out.println("Anchor voxel is at "+anchor.getCoordinates()+". Making piece removable...");
        for (int j=0; j<path.size(); j++){ //for each voxel in the path
            Voxel currentVoxel = path.get(j);
            if ((!removablePiece.contains(currentVoxel)) ){ //if the current voxel is not already in the piece and it is not the anchor
                removablePiece.add(currentVoxel); //add the current voxel
                System.out.print(currentVoxel.getCoordinates()+"(in path); ");
                for (int k=currentVoxel.y; k>=0; k--){ //for each y co-ordinate from the current voxel's y co-ordinate to top (0)
                    Voxel above = puzlock.getUp(currentVoxel.x, k, currentVoxel.z, puzlock.inputVoxelizedMesh, puzlock.inputVoxelizedMeshSize, puzlock.voxels);
                    if ((above != null) && (above.value == 1) && (!removablePiece.contains(above)) && (currentVoxel != anchor)){ //if y co-ordinate is less than that of the current voxel i.e. on top of it and it has not been added yet
                        removablePiece.add(above); //add it to the set of candidate voxels (represented in figure 9(e))
                        System.out.print(above.getCoordinates()+"(on top); ");
                    }
                }
            }
        }
        //should debug print the removable piece here, remember to ensure that the anchor is not added...
        ArrayList<Voxel> rPiece = (ArrayList)removablePiece.clone(); //clone the removable piece to avoid the concurrency issue
        puzlock.removablePieces.add(rPiece); //store the removable piece
    }
    
    /* find the piece with the smallest sum of accessibility values 
    static ArrayList<Voxel> selectPiece(){
        puzlock.selectedPiece = new ArrayList<>();
        double currentHighestSum = 1000000; //stores the current lowest sum of accessibility values, initialized to 1000000
        for (ArrayList rPiece: puzlock.removablePieces){
            double currentSum = puzlock.sumOfAccessVals(rPiece);
            if (currentSum<currentHighestSum){ //if current sum of accessibility values is the lowest
                currentHighestSum = currentSum; //set the current highest sum to the current sum
                puzlock.selectedPiece = rPiece; //set the selected piece to the current piece
            }
        }
        return puzlock.selectedPiece;
    }*/
    
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