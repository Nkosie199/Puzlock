import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author Nkosi Gumede
 * this class implements 'Section 5.2: Extracting the Key Piece' of Song et al. (2012)
 */
public class Puzlock {
    static Scanner sc;
    static int [][][] inputVoxelizedMesh;
    static int [][][] outputVoxelizedMesh;
    static int inputVoxelizedMeshSize; //the size of the array on the z a-axis
    static Voxel voxel;
    static Voxel seedVoxel;
    static ArrayList<Voxel> voxels; //used to store the entire set of voxels
    static ArrayList<Voxel> outputVoxels = new ArrayList<>(); //stores the entire set of voxels output i.e. puzzle piece
    static ArrayList<Voxel> exteriorVoxels = new ArrayList<>(); //used to store the set of exterior voxels
    static ArrayList<Integer> accessibilityValues = new ArrayList<>(); //used to store the set of accessibility values
    static ArrayList<Voxel> visitedAdjacentVoxels; //stores the set of currently visited voxels, used in breadth first traversal
    static int Nb1 = 50; //# of voxel pairs which are closest to the seed (50 as per Song et al implementation)
    static int Nb2 = 10; //selected among the Nb1 voxel pairs which have the smallest accessibility value (10 as per Song et al implementation)
    static ArrayList<VoxelPair> voxelPairs = new ArrayList<>(Nb1); //stores the Nb1 number of adjacent voxel pairs
    static ArrayList<VoxelPair> voxelPairs2 = new ArrayList<>(Nb2); //stores the Nb2 number of adjacent voxel pairs
    static int bfsi = 1; //an iterator for breadthFirstTraversal2
    static ShortestPath sp;
    static ArrayList<ArrayList> removablePieces = new ArrayList<>(); //stores all the removable pieces
    static int N; //stores the total # of voxels
    static int K; //stores the total # of pieces (K < N < 4)
    static int m; //stores the average number of voxels per puzzle piece i.e. m = N(total # of voxels)/K(total # of pieces)
    static int B = 3; //stores the Beta value described in section 5.1.4
    static ArrayList<Voxel> selectedPiece; //stores the currently selected piece as per the ShortestPath class
    static Voxel blocking; //stores the current blocking voxel as per the ShortestPath class
    static ArrayList<PuzzlePiece> puzzlePieces = new ArrayList<>(); //stores the set of puzzle pieces
    static ArrayList<Voxel> keyPiece = new ArrayList();
    static ArrayList<Voxel> setOfCandidates = new ArrayList<>(); //stores the set of candidate voxels to be added to keyPiece (addedVoxels)
    static IO io = new IO();
    static int top = -1; //stores the y-coordinate of the highest voxel i.e. the top
    
    public static void main(String[] args) throws IOException{
        //0. Setup...
        //0.1. Read the 3D grid
        //inputVoxelizedMesh = io.readFileToInputGrid("inputGrid");
        inputVoxelizedMesh = io.readFileToInputGrid("4x4x4");
        inputVoxelizedMeshSize = inputVoxelizedMesh[0].length;
        System.out.println("vMesh size: "+inputVoxelizedMeshSize);
        //0.2. Initialize the 1D array representing the 3D grid/array
        initializeVoxelArray(inputVoxelizedMesh); //initializes the voxels array and sets the value of top
//        debugPrintVoxelizedMesh(inputVoxelizedMesh);
//        debugPrintVoxels(voxels);
        sc = new Scanner(System.in);
        N = voxels.size();
        System.out.println("Your input contains "+N+" voxels. Type in the number of puzzle pieces required: "); //eg. 8 pieces for a 4^3 voxel cube
        K = sc.nextInt();
        m = N/K; //set m, required for establishing how many voxels to add/remove from the key piece
//        System.out.println("Please set a Beta value ranging from 1 to 6:"); //eg. 8 pieces for a 4^3 voxel cube
//        B = sc.nextInt();
        System.out.println("m has been set to "+m+". B has been set to "+B+"!");
        //1. Extacting the key piece...
        //1.1. Pick a seed voxel
        pickSeedVoxel(inputVoxelizedMesh);
        //1.2. Compute voxel accessibility
        computeVoxelAccessibility();
        //1.3. Ensure blocking and mobility
        ensureBlockingMobility();
        //1.4. Expand the key piece
        keyPiece = expandKeyPiece(selectPiece());
        //1.5. Confirm the key piece
        confirmKeyPiece(keyPiece);
        //n. Print keyPiece(s)
        setOutputPieces(keyPiece, inputVoxelizedMeshSize); //will set outputVoxelizedMesh
        System.out.println("\nDebug printing output mesh and printing to file...");
        debugPrintVoxelizedMesh(outputVoxelizedMesh);
        io.printGridToFile(outputVoxelizedMesh, "keyPiece"); //should print the key piece to a file called keyPiece
//        System.out.println("Checking removable directions...");
//        checkRemovableDirections(outputVoxelizedMesh);
        System.out.println("\nCOMPLETE!");
//        debugPrintOutput(keyPiece);
    }

    //1. Extacting the key piece
    //1.1. Pick a seed voxel
    /* takes in an input mesh and randomly picks a seed voxel from the set of exterior voxels */
    public static void pickSeedVoxel(int[][][] vMesh){
    	System.out.println("1.1. Picking a seed voxel...");
        //• Identify a set of exterior voxels that have exactly a pair of adjacent exterior faces (with one being on top). 
        //Require that these voxels can move out of the puzzle in one movement.
        int i=0;
        System.out.println("Top: "+top+". Size: "+inputVoxelizedMeshSize);
        for (Voxel v: voxels){
            if (v.y == top){ //i.e. if voxel is at the top
                if (getLeft(v.x,v.y,v.z, inputVoxelizedMesh, inputVoxelizedMeshSize, voxels) == null){
                    //and if the 3D array has 0 or null at the left co-ordinate of that voxel...
                    exteriorVoxels.add(v); //this voxel should have met the requirement of being able to move out of the puzzle in one movement
                    v.normalDirection = "left";
                    System.out.println(i+") Added exterior voxel: "+v+" at co-ordinates "+v.x+", "+v.y+", "+v.z+" with normal direction "+v.normalDirection); //debug print the set of added exterior voxels...
                    i++;
                }
                if (getRight(v.x,v.y,v.z, inputVoxelizedMesh, inputVoxelizedMeshSize, voxels) == null){
                    //and if the 3D array has 0 or null at the right co-ordinate of that voxel...
                    exteriorVoxels.add(v); //this voxel should have met the requirement of being able to move out of the puzzle in one movement
                    v.normalDirection = "right";
                    System.out.println(i+") Added exterior voxel: "+v+" at co-ordinates "+v.x+", "+v.y+", "+v.z+" with normal direction "+v.normalDirection); //debug print the set of added exterior voxels...
                    i++;
                }
                if (getForward(v.x,v.y,v.z, inputVoxelizedMesh, inputVoxelizedMeshSize, voxels) == null){
                    //and if the 3D array has 0 or null at the forward co-ordinate of that voxel...
                    exteriorVoxels.add(v); //this voxel should have met the requirement of being able to move out of the puzzle in one movement
                    v.normalDirection = "forward";
                    System.out.println(i+") Added exterior voxel: "+v+" at co-ordinates "+v.x+", "+v.y+", "+v.z+" with normal direction "+v.normalDirection); //debug print the set of added exterior voxels...
                    i++;
                }
                if (getBackward(v.x,v.y,v.z, inputVoxelizedMesh, inputVoxelizedMeshSize, voxels) == null){
                    //and if the 3D array has 0 or null at the backward co-ordinate of that voxel...
                    exteriorVoxels.add(v); //this voxel should have met the requirement of being able to move out of the puzzle in one movement
                    v.normalDirection = "backward";
                    System.out.println(i+") Added exterior voxel: "+v+" at co-ordinates "+v.x+", "+v.y+", "+v.z+" with normal direction "+v.normalDirection); //debug print the set of added exterior voxels...
                    i++;
                }
            }
        }
        //• From the candidate set, we can either randomly pick a seed, or let the user make a choice.
        System.out.println("Bound: "+exteriorVoxels.size());
        int randomNum = ThreadLocalRandom.current().nextInt(0, exteriorVoxels.size());
        seedVoxel = exteriorVoxels.get(randomNum);
        System.out.println("Seed voxel was chosen among exterior voxels at index "+randomNum+"\n");
        
    }

    //1.2. Compute voxel accessibility
    static void computeVoxelAccessibility(){
    	System.out.println("1.2. Computing voxel accessibility...");
        /* • Compute an accessibility value, aj(x), for each voxel x in a remaining volume, and use it later as a heuristic to alleviate fragmentation, 
        where aj(x) is computed by recursively counting the (weighted) number of voxel neighbours: 
        aj(x) = number of neighbors of x, for j = 0
        aj(x) = aj-1(x) + a^j ∑i aj-1(yi(x)), for j > 0 , where yi(x)’s are neighboring voxels of x in the remaining volume. 
        Note that the weight factor α is set to 0.1 in our implementation. We stop the recursion at j = 3 
        because we found experimentally that the resulting accessibility values are sufficient for guiding the voxel selection, see Figure 8 for an example. 
        Since voxels with low accessibility are likely to be fragmented, we prioritize to include them when constructing a puzzle piece.*/
        // first pass - simple neighbour count
        int p=1; //stores index of iterator
        for (int z=0; z<inputVoxelizedMesh.length; z++){
            for (int y=0; y<inputVoxelizedMesh.length; y++){
                for (int x=0; x<inputVoxelizedMesh.length; x++){
                    if (inputVoxelizedMesh[z][y][x] == 1){
                        //get the voxel at that index and set its accessibility value...
                        int index = indexOfCoordinate(x,y,z, inputVoxelizedMeshSize);
                        Voxel v = voxels.get(index);
                        int neighbours = countNeighbours(v,inputVoxelizedMesh,inputVoxelizedMeshSize,voxels);
                        v.accessibilityValue = neighbours;
                        System.out.println(p+") Voxel "+v+" at index "+x+","+y+","+z+" has "+neighbours+" neighbours");
                        p++;
                    }
                }
            }
        }
        System.out.println("");
        // subsequent passes - implicitly include neighbours from further afield...
        int passes = 3; //# of passes (j from the eqn in the paper) i.e. = 3
        ArrayList<Voxel> voxels2 = new ArrayList<>(); //create a new voxels arraylist to store the set of new voxels and corresponding access values
        int q = 0; //stores the current index
        int pass = 1; //stores the pass number, ranging from 1 to 3
        subsequentPasses(voxels, voxels2, passes, pass, q); 
    }

    //1.3. Ensure blocking and mobility
    static void ensureBlockingMobility(){
    	System.out.println("1.3. Ensuring blocking and mobility...");
        /* • Develop the key piece such that it is removable by a translation along one direction. 
        Identify the normal direction (vn) of the non-upward-facing exterior face of the seed voxel. */
        String vn = seedVoxel.normalDirection; //stores the normal direction
        /* • Do a breadth-first traversal from the seed to find Nb1 pairs of voxels (that orient along vn) that are the nearest to the seed, 
        where in each pair, the voxels on the positive and negative sides of vn are called the blocker and blockee voxels, respectively. 
        Among them, we select Nb2 pairs whose blockee has the smallest accessibility among the Nb1 pairs 
        (in their implementation Nb1 and Nb2 are set to 50 and 10 respectively). */
        breadthFirstTraversal(); //for the purpose of storing the the Nb1 number of voxel pairs in the voxelPairs array
        //now we can select the Nb2 number of pairs whose blockee has the smallest accessibility value...
        System.out.println("\nSelecting "+Nb2+" pairs among "+voxelPairs.size()+" pairs...");
        //algorithm: store all blockees in an array sorted by accessibility value (minimum to maximum) and select the first Nb2 number of blockees..
        ArrayList<Double> accVals = new ArrayList(); //stores the accessibility values
        ArrayList<VoxelPair> accessibleVoxelPairs = new ArrayList(); //stores the voxel pairs with the lowest accessibility values
        double maxAccVal = 0; //stores the maximum accessibility value
        for (VoxelPair p: voxelPairs) {
            Voxel v = p.getVoxel2(); //get each blockee voxel
            accVals.add(v.accessibilityValue); //add its accessibility value
        }
        Collections.sort(accVals); //sorts the stores accessibility values
        maxAccVal = accVals.get(Nb2-1); //gets the last of the sorted array
        for (VoxelPair q: voxelPairs) { //for each voxelpair...
            Double d = q.getVoxel2().accessibilityValue; //get the voxelpair's blockee's accessibility value
            //if a voxelpair's blockee has an accessibilty value less than or equal to the max of the sorted accessibility value, 
            //and it is not already in the set of accessible voxel pairs
            if ((d <= maxAccVal) && (!accessibleVoxelPairs.contains(q))){
                accessibleVoxelPairs.add(q); //add it to the list of Nb2 voxel pairs
                if (accessibleVoxelPairs.size() == Nb2){
                    break; //if the list of accessible voxel pairs is large enough, break out of the loop
                }
            }
        }
        System.out.println("Debug printing the sorted list of accessibility values, where max = "+maxAccVal+"...");
        for (int i=0; i<Nb2; i++) { //for each accessible voxel pair...
            //debug print the sorted list of accessibility values and voxel pairs
            VoxelPair vp = accessibleVoxelPairs.get(i);
            Voxel blocking = vp.getVoxel1();
            Voxel blockee = vp.getVoxel2();
            System.out.print(i+") Access value: "+blockee.accessibilityValue);
            System.out.println(", voxel pair: "+vp+", blockee is at "+blockee.getCoordinates()+", blocking is at "+blocking.getCoordinates()+" and normal direction is "+seedVoxel.normalDirection+"...");
            System.out.print("Shortest path from "+seedVoxel.getCoordinates()+" (seed) to "+blockee.getCoordinates()+" (blockee): ");
            ArrayList<Voxel> voxels2 = (ArrayList)voxels.clone(); //must clone the array to prevent the concurrency issue
            sp = new ShortestPath(voxels2, seedVoxel, blockee, blocking); //computes the shortest path from the seed to all other voxels
            //create new puzzle piece based on the removable piece and add it to the set of stored pieces in Puzlock...
            puzzlePieces.add(new PuzzlePiece(sp.removablePiece, sp.anchorVoxel, sp.anchorVoxel2, blocking, blockee));
        }
        /* • Block the key from moving towards vn by 
        (i) determining a set of shortest path candidates from the seed to each blockee voxel candidate 
        (without crossing the related blocking voxel and voxels below it); 
        we later will select one of them for evolving the key; and 
        (ii) extract all the voxels along a selected shortest path until the blockee, and adding these voxels to evolve the key piece. */
        //done in ShortestPath class referenced in the for-loop above...
        
        /* • Ensure the key to be removable upward by including any voxel above the selected shortest path. 
        This is why the shortest paths determined in the strategy above should not go through the blocking voxel or any voxel below it, 
        else the blockage is destructed. 
        Moreover, we ignore the shortest path candidates that eventually add excessive voxels since the key should less than m voxels. */
        //done by makeRemovable method in ShortestPath class...

        /* • Devise a key that moves upward but not along vn. However, since new voxels are added to the key, 
        the key may accidentally become mobilized in a direction along which the seed was originally blocked (eg. +X, -Y, +/-Z). 
        This would require testing the blockage (or mobility) every time we add voxels to the key. 
        However, our third strategy avoids such test: we identify an anchor voxel that is directly-connected and furthest away from the seed (figure 9e). 
        The key idea is that if these anchor voxels stay with the remaining volume (but not added to the key), 
        the key can remain to be immobilized in the blocked directions even if we add more voxels to it. 
        Mobility test is not required to ensure the maintenance of the blockage. */
        //done by setAnchorVoxel method in ShortestPath class...

        /* • To choose among the shortest paths resulted from the first two strategies, we sum for each path the accessibility of all the voxels 
        required to be added to the key, i.e., voxels along the path, the blockee, and any voxel above. 
        Then, we pick the one with the smallest accessibility sum for evolving the key with the appropriate blockage. */
        //done by selectPiece method in ShortestPath class...
    }

    //1.4. Expand the key piece
    static ArrayList<Voxel> expandKeyPiece(ArrayList<Voxel> piece){
    	System.out.println("1.4. Expand the key piece...");
        /* • Identify an additional anchor voxel for the direction immobilized by the blocking voxel picked in step 3 – it is furthest away from the blocking
        voxel in direction vn; if no such voxels exist, we use the blocking voxel as the anchor; 
        note that the anchor voxel idea is crucial for the expansion process as the existing blockage 
        could be undesirably destructed if an anchor voxel in inappropriately added to the key piece. */
        //done by setAnchorVoxel2 method in ShortestPath class...

        /* • Identify a set of candidate voxels to be added to the key, say {ui}, that are resided next to the key but neither at the anchor voxels nor below the anchors. 
        For each ui, we identify also the voxels directly above it, so we know the voxels required to be added to the key if ui is chosen. 
        Furthermore, if the number of voxels exceeds the number of extra voxels the key needs, we remove ui, from the candidate set. */
        //done by addVoxels and addVoxels2 methods of this class...
        ArrayList<Voxel> addedVoxels = new ArrayList<>(); //stores the set of candidate voxels to be added to the key (neighbours but not below the anchors)
        addedVoxels = (ArrayList)piece.clone(); //initialized to the selected piece
        System.out.println("Debug printing the removable piece...");
        debugPrintVoxels(addedVoxels);
        ArrayList addedVoxels2 = addVoxels(addedVoxels, puzzlePieces, inputVoxelizedMesh, inputVoxelizedMeshSize, voxels, setOfCandidates, B);
        System.out.println("Debug printing the new piece with added voxels (neighbouring and above) and voxels added by probability (as per subpoint 3's strategy)...");
        debugPrintVoxels(addedVoxels2);
        return addedVoxels;
    }  

    //1.5. Confirm the key piece
    static void confirmKeyPiece(ArrayList<Voxel> keyPiece) throws FileNotFoundException, UnsupportedEncodingException{
    	System.out.println("1.5. Confirm the key piece...");
    	/* • After steps 1 to 4, the key is guaranteed to fulfill all interlocking requirements, except that R2 needs to be simply connected. 
        With the help of accessibilty, the chance of fragmenting R2 (and other remaining volumes) is rather low; 
        hence, testing the connectivity of voxels in R2 at the end of the key piece generation procedure is more efficient than doing it at multiple places. 
        To guarantee that R2 is simply connected, we gather all the voxels next to the key in a set, say Rs, and apply a simple flooding algorithm to test 
        whether all voxels in Rs can be visited or not in R2. */
        //done by floodFill method this class...
        floodFill(keyPiece);
    }
    
    /* Takes in a 3D array and stores it as voxels in a 1D voxel arraylist */
    static ArrayList<Voxel> initializeVoxelArray(int[][][] vMesh){
    	voxels = new ArrayList<>();
    	//int iterator = 0;
        for (int z=0; z<vMesh.length; z++){
            for (int y=0; y<vMesh.length; y++){
                for (int x=0; x<vMesh.length; x++){
                    if (vMesh[z][y][x] == 1){//only if index == 1
                        if ((y < top) || (top==-1)){ //if the is a lesser y-coordinate or if it has not yet been set, update top
                            top = y;
                        }
                        Voxel v = new Voxel(x,y,z,1);
                        //System.out.println(iterator+") "+1+" @ "+x+","+y+","+z);
                        voxels.add(v);
                        //iterator++;
                    }else if (vMesh[z][y][x] == 0){//only if index == 0
                        Voxel v = new Voxel(x,y,z,0);
                        //System.out.println(iterator+") "+0+" @ "+x+","+y+","+z);
                        voxels.add(v);
                        //iterator++;
                    }
                }
            }
        }
        return voxels;
    }
    
    /* Takes in a 3D array and stores it as voxels in a 1D voxel arraylist */
    static ArrayList<Voxel> initializeOutputArray(int [][][] vMesh){
        for (int z=0; z<vMesh.length; z++){
            for (int y=0; y<vMesh.length; y++){
                for (int x=0; x<vMesh.length; x++){
                    if (vMesh[z][y][x] == 1){//only if index == 1
                        Voxel v = new Voxel(x,y,z,1);
                        outputVoxels.add(v);
                    }else if (vMesh[z][y][x] == 0){//only if index == 0
                        Voxel v = new Voxel(x,y,z,0);
                        outputVoxels.add(v);
                    }
                }
            }
        }
        return outputVoxels;
    }
    
    /* Takes in a 3D array and prints it to console with each z-axis layer being separated by '---'*/
    static void debugPrintVoxelizedMesh(int [][][] vMesh){
        for (int z=0; z<vMesh.length; z++){
            for (int y=0; y<vMesh.length; y++){
                for (int x=0; x<vMesh.length; x++){
                    System.out.print(vMesh[z][y][x]+" ");
                }
                System.out.println("");
            }
            System.out.println("---z="+z+"---");
        }
    }
    
    /* Takes in a 3D array and counts to number of elements it contains */
    static int computeMeshSize(int[][][] vMesh){
        int size = 0;
        for (int z=0; z<vMesh.length; z++){
            for (int y=0; y<vMesh.length; y++){
                for (int x=0; x<vMesh.length; x++){
                    size++;
                }
            }
        }
        return size;
    }
    
    /* takes in a set of co-ordinates and returns the index of it on the arraylist 
    sizeOfAxes is the number of voxels (ones on the in the inputGrid's x-axis)*/
    static synchronized int indexOfCoordinate(int x, int y, int z, int sizeOfAxes){
        int index = 0;
        index = index + (x*1);
        index = index + (y*sizeOfAxes);
        index = index + (z*sizeOfAxes*sizeOfAxes);
        return index;
    }
    
    /* debug prints the voxels array */
    static void debugPrintVoxels(ArrayList<Voxel> voxels){
        for (int i=0; i<voxels.size(); i++) {
            Voxel v = voxels.get(i);
            System.out.println(i+") "+v+" is at "+v.x+","+v.y+","+v.z);
        }
        System.out.println("");
    }
    
    /* Takes in a puzzle piece(as an array) to print and sets it to outputVoxelizedMesh */
    static int[][][] setOutputPieces(ArrayList<Voxel> piece, int size){
        //remember: current program only works with grids of 32x32x32
        outputVoxelizedMesh = new int[size][size][size];
        //first initialize outputVoxelizedMesh to all 0s...
        for (int z = 0; z < size; z++) { //for each voxel...
            for (int y = 0; y < size; y++) {
                for (int x = 0; x < size; x++) {
                	outputVoxelizedMesh[z][y][x] = 0; //set index to 0
                }
            }
        }
        //now we can add 1s where appropriate...
        for (int i = 0; i < piece.size(); i++) { //for each voxel in the piece...
        	Voxel voxel = piece.get(i); //stores the current voxel
            int xcoord = voxel.x; //stores the current x co-ordinate
            int ycoord = voxel.y; //stores the current y co-ordinate
            int zcoord = voxel.z; //stores the current z co-ordinate
        	for (int z = 0; z < size; z++) { //for each voxel...
                for (int y = 0; y < size; y++) {
                    for (int x = 0; x < size; x++) {
                        if ((x==xcoord) && (y==ycoord) && (z==zcoord)){ //if index is 0 or null and the co-ordinates match
                        	outputVoxelizedMesh[z][y][x] = 1; //set index to 0
                        }
                    }
                }
            }
        }
        return outputVoxelizedMesh;
    }
    
    /* Takes in the co-ordinates of voxel and checks if there a voxel to the left in the inputVoxelizedMesh */
    static Voxel getLeft(int x, int y, int z, int[][][] vMesh, int vMeshSize, ArrayList<Voxel> voxelArray){
        try{ //it will either be 0, 1 or null
        	if (vMesh[z][y][x-1] == 1 | vMesh[z][y][x-1] == 0){
        		int index = indexOfCoordinate(x-1, y, z, vMeshSize);
        		//System.out.println("Left index: "+index);
                Voxel v = voxelArray.get(index);
                int currentIndex = indexOfCoordinate(v.x, v.y, v.z, vMeshSize);
                if (!(v.x+", "+v.y+", "+v.z).equals((x-1)+", "+y+", "+z)){
                    System.out.println("ERROR! Voxel left of "+x+", "+y+", "+z+" is at "+v.x+", "+v.y+", "+v.z+". Correct answer: "+(x-1)+", "+y+", "+z);
                    System.out.println("Cuurent index: "+currentIndex+". Correct index: "+index);
                    System.out.println("");
                }
                return v;
            }
        }catch(Exception e){
            //System.out.println("Exception: "+e);
        }
        return null;
    }
    
    /* Takes in the co-ordinates of voxel and checks if there a voxel to the right in the inputVoxelizedMesh */
    static Voxel getRight(int x, int y, int z, int[][][] vMesh, int vMeshSize, ArrayList<Voxel> voxelArray){
        try{ //it will either be 0, 1 or null
            if (vMesh[z][y][x+1] == 1 | vMesh[z][y][x+1] == 0){
            	//System.out.println("Right in vMesh: "+vMesh[x+1][y][z]+" at co-ordinates "+(x+1)+","+y+","+z+". Given mesh size: "+vMeshSize);
                int index = indexOfCoordinate(x+1, y, z, vMeshSize);
                Voxel v = voxelArray.get(index);
                int currentIndex = indexOfCoordinate(v.x, v.y, v.z, vMeshSize);
                if (!(v.x+", "+v.y+", "+v.z).equals((x+1)+", "+y+", "+z)){
                    System.out.println("ERROR! Voxel right of "+x+", "+y+", "+z+" is at "+v.x+", "+v.y+", "+v.z+". Correct answer: "+(x+1)+", "+y+", "+z);
                    System.out.println("Cuurent index: "+currentIndex+". Correct index: "+index);
                    System.out.println("");
                }
                return v;
            }
        }catch(Exception e){
            //System.out.println("Exception: "+e);
        }
        return null;
    }
    
    /* Takes in the co-ordinates of voxel and checks if there a voxel above the inputVoxelizedMesh */
    static Voxel getUp(int x, int y, int z, int[][][] vMesh, int vMeshSize, ArrayList<Voxel> voxelArray){
        try{ //it will either be 0, 1 or null
            if (vMesh[z][y-1][x] == 1 | vMesh[z][y-1][x] == 0){ 
                int index = indexOfCoordinate(x, y-1, z, vMeshSize);
                Voxel v = voxelArray.get(index);
                int currentIndex = indexOfCoordinate(v.x, v.y, v.z, vMeshSize);
                if (!(v.x+", "+v.y+", "+v.z).equals(x+", "+(y-1)+", "+z)){
                    System.out.println("ERROR! Voxel up of "+x+", "+y+", "+z+" is at "+v.x+", "+v.y+", "+v.z+". Correct answer: "+x+", "+(y-1)+", "+z);
                    System.out.println("Cuurent index: "+currentIndex+". Correct index: "+index);
                    System.out.println("");
                }
                return v;
            }
        }catch(Exception e){
            //System.out.println("Exception: "+e);
        }
        return null;
    }
    
    /* Takes in the co-ordinates of voxel and checks if there a voxel beneath the inputVoxelizedMesh */
    static Voxel getDown(int x, int y, int z, int[][][] vMesh, int vMeshSize, ArrayList<Voxel> voxelArray){
        try{ //it will either be 0, 1 or null
            if (vMesh[z][y+1][x] == 1 | vMesh[z][y+1][x] == 0){
                int index = indexOfCoordinate(x, y+1, z, vMeshSize);
                Voxel v = voxelArray.get(index);
                int currentIndex = indexOfCoordinate(v.x, v.y, v.z, vMeshSize);
                if (!(v.x+", "+v.y+", "+v.z).equals(x+", "+(y+1)+", "+z)){
                    System.out.println("ERROR! Voxel down of "+x+", "+y+", "+z+" is at "+v.x+", "+v.y+", "+v.z+". Correct answer: "+x+", "+(y+1)+", "+z);
                    System.out.println("Cuurent index: "+currentIndex+". Correct index: "+index);
                    System.out.println("");
                }
                return v;
            }
        }catch(Exception e){
            //System.out.println("Exception: "+e);
        }
        return null;
    }
    
    /* Takes in the co-ordinates of voxel and checks if there a voxel in front of the inputVoxelizedMesh */
    static Voxel getForward(int x, int y, int z, int[][][] vMesh, int vMeshSize, ArrayList<Voxel> voxelArray){
        try{ //it will either be 0, 1 or null
            if (vMesh[z+1][y][x] == 1 | vMesh[z+1][y][x] == 0){
                int index = indexOfCoordinate(x, y, z+1, vMeshSize);
                Voxel v = voxelArray.get(index);
                int currentIndex = indexOfCoordinate(v.x, v.y, v.z, vMeshSize);
                if (!(v.x+", "+v.y+", "+v.z).equals(x+", "+y+", "+(z+1))){
                    System.out.println("ERROR! Voxel forward of index: "+x+", "+y+", "+z+" is at "+v.x+", "+v.y+", "+v.z+". Correct answer: "+x+", "+y+", "+(z+1));
                    System.out.println("Cuurent index: "+currentIndex+". Correct index: "+index);
                    System.out.println("");
                }
                return v;
            }
        }catch(Exception e){
            //System.out.println("Exception: "+e);
        }
        return null;
    }
    
    /* Takes in the co-ordinates of voxel and checks if there a voxel behind the inputVoxelizedMesh */
    static Voxel getBackward(int x, int y, int z, int[][][] vMesh, int vMeshSize, ArrayList<Voxel> voxelArray){
        try{ //it will either be 0, 1 or null
            if (vMesh[z-1][y][x] == 1 | vMesh[z-1][y][x] == 0){
                int index = indexOfCoordinate(x, y, z-1, vMeshSize);
                Voxel v = voxelArray.get(index);
                int currentIndex = indexOfCoordinate(v.x, v.y, v.z, vMeshSize);
                if (!(v.x+", "+v.y+", "+v.z).equals(x+", "+y+", "+(z-1))){
                    System.out.println("ERROR! Voxel backward of "+x+", "+y+", "+z+" is at "+v.x+", "+v.y+", "+v.z+". Correct answer: "+x+", "+y+", "+(z-1));
                    System.out.println("Cuurent index: "+currentIndex+". Correct index: "+index);
                    System.out.println("");
                }
                return v;
            }
        }catch(Exception e){
            //System.out.println("Exception: "+e);
        }
        return null;
    }
    
    /* Takes in a voxel's co-ordinates and counts its neighbours */
    static int countNeighbours(Voxel v, int[][][] vMesh, int vMeshSize, ArrayList vs){
        //neighbour can either be 0, 1 or null...
        int neighbours = 0;
        Voxel left = getLeft(v.x, v.y, v.z, vMesh, vMeshSize, vs);
        Voxel right = getRight(v.x, v.y, v.z, vMesh, vMeshSize, vs);
        Voxel up = getUp(v.x, v.y, v.z, vMesh, vMeshSize, vs);
        Voxel down = getDown(v.x, v.y, v.z, vMesh, vMeshSize, vs);
        Voxel forward = getForward(v.x, v.y, v.z, vMesh, vMeshSize, vs);
        Voxel backward = getBackward(v.x, v.y, v.z, vMesh, vMeshSize, vs);
        if (left != null && left.value == 1){ //check left
            neighbours++;
        }if (right != null && right.value == 1){ //check right
            neighbours++;
        }if (up != null && up.value == 1){ //check up
            neighbours++;
        }if (down != null && down.value == 1){ //check down
            neighbours++;
        }if (forward != null && forward.value == 1){ //check forward
            neighbours++;
        }if (backward != null && backward.value == 1){ //check backward
            neighbours++;
        }
        return neighbours;
    }
    
    /* Takes in a voxel's co-ordinates and counts its neighbours */
    static double sumOfNeighboursAccValues(int x, int y, int z, int[][][] vMesh, int vMeshSize, ArrayList<Voxel> vs){
        double sum = 0;
        try{if (vMesh[z][y][x-1] == 1){ //check left
                int index = indexOfCoordinate(x-1, y, z, vMeshSize); //index of left voxel
                Voxel v = vs.get(index);
                double accVal = v.accessibilityValue; //accessibilty value of the voxel at that index
                sum = sum + accVal;
                //System.out.println("Updated voxel @ "+x+","+y+","+z+"'s access value to "+sum+" thanks to left neighbour @ "+v.x+","+v.y+","+v.z);
            }
        }catch(Exception e){
        	//System.out.println("Exception: "+e);
        }
        try{if (vMesh[z][y][x+1] == 1){ //check right
                int index = indexOfCoordinate(x+1, y, z, vMeshSize); //index of right voxel
                Voxel v = vs.get(index);
                double accVal = v.accessibilityValue; //accessibilty value of the voxel at that index
                sum = sum + accVal;
                //System.out.println("Updated voxel @ "+x+","+y+","+z+"'s access value to "+sum+" thanks to right neighbour @ "+v.x+","+v.y+","+v.z);
        	}
        }catch(Exception e){
        	//System.out.println("Exception: "+e);
        }
        try{if (vMesh[z][y-1][x] == 1){ //check up
                int index = indexOfCoordinate(x, y-1, z, vMeshSize); //index of up voxel
                Voxel v = vs.get(index);
                double accVal = v.accessibilityValue; //accessibilty value of the voxel at that index
                sum = sum + accVal;
                //System.out.println("Updated voxel @ "+x+","+y+","+z+"'s access value to "+sum+" thanks to up neighbour @ "+v.x+","+v.y+","+v.z);
        	}
        }catch(Exception e){
        	//System.out.println("Exception: "+e);
        }
        try{if (vMesh[z][y+1][x] == 1){ //check down
                int index = indexOfCoordinate(x, y+1, z, vMeshSize); //index of down voxel
                Voxel v = vs.get(index);
                double accVal = v.accessibilityValue; //accessibilty value of the voxel at that index
                sum = sum + accVal;
                //System.out.println("Updated voxel @ "+x+","+y+","+z+"'s access value to "+sum+" thanks to down neighbour @ "+v.x+","+v.y+","+v.z);
        	}
        }catch(Exception e){
        	//System.out.println("Exception: "+e);
        }
        try{if (vMesh[z+1][y][x] == 1){ //check forward
                int index = indexOfCoordinate(x, y, z+1, vMeshSize); //index of forward voxel
                Voxel v = vs.get(index);
                double accVal = v.accessibilityValue; //accessibilty value of the voxel at that index
                sum = sum + accVal;
                //System.out.println("Updated voxel @ "+x+","+y+","+z+"'s access value to "+sum+" thanks to forward neighbour @ "+v.x+","+v.y+","+v.z);
        	}
        }catch(Exception e){
        	//System.out.println("Exception: "+e);
        }
        try{if (vMesh[z-1][y][x] == 1){ //check backward
                int index = indexOfCoordinate(x, y, z-1, vMeshSize); //index of backward voxel
                Voxel v = vs.get(index);
                double accVal = v.accessibilityValue; //accessibilty value of the voxel at that index
                sum = sum + accVal;
                //System.out.println("Updated voxel @ "+x+","+y+","+z+"'s access value to "+sum+" thanks to backward neighbour @ "+v.x+","+v.y+","+v.z);
        	}
        }catch(Exception e){
        	//System.out.println("Exception: "+e);
        }
        return sum; //return the sum...
    }
    
    /* conducts the breadth-first traversal from the seed voxel which is necessary to store the blocker and blockee voxel pairs */
    static void breadthFirstTraversal(){
        visitedAdjacentVoxels = new ArrayList<>(); //stores the currently unvisited voxels which are adjacent to the current voxel     
        System.out.println("Breadth-first traversal from the seed voxel...");
        //first determine the direction in which to traverse though the graph to find the nearest pairs of voxels...
        Voxel currentVoxel = seedVoxel;  
        String normalDir = seedVoxel.normalDirection;
        System.out.println("Seed voxel randomly chosen from set is: "+seedVoxel+" at co-ordinates "+seedVoxel.x+", "+seedVoxel.y+", "+seedVoxel.z+"; normal direction = "+normalDir);
        breadthFirstTraversal2(currentVoxel, normalDir); //traverse thought its neighbours
    }
    
    /* takes in a voxel and its nornal direction, and traverses through its neighbours, setting the blocking pairs and reducing the set of unvisited voxels */
    static synchronized void breadthFirstTraversal2(Voxel currentVoxel, String normalDir){
        visitedAdjacentVoxels.add(currentVoxel); //add the current voxel from the set of visited voxels
//        System.out.println(bfsi+") Currently at voxel "+currentVoxel+" at co-ordinates "+currentVoxel.x+", "+currentVoxel.y+", "+currentVoxel.z);
        //visit current voxels neighbours...
        Voxel leftNeighbour = getLeft(currentVoxel.x, currentVoxel.y, currentVoxel.z, inputVoxelizedMesh, inputVoxelizedMeshSize, voxels); 
        Voxel rightNeighbour = getRight(currentVoxel.x, currentVoxel.y, currentVoxel.z, inputVoxelizedMesh, inputVoxelizedMeshSize, voxels); 
        Voxel upNeighbour = getUp(currentVoxel.x, currentVoxel.y, currentVoxel.z, inputVoxelizedMesh, inputVoxelizedMeshSize, voxels);
        Voxel downNeighbour = getDown(currentVoxel.x, currentVoxel.y, currentVoxel.z, inputVoxelizedMesh, inputVoxelizedMeshSize, voxels);
        Voxel forwardNeighbour = getForward(currentVoxel.x, currentVoxel.y, currentVoxel.z, inputVoxelizedMesh, inputVoxelizedMeshSize, voxels);
        Voxel backwardNeighbour = getBackward(currentVoxel.x, currentVoxel.y, currentVoxel.z, inputVoxelizedMesh, inputVoxelizedMeshSize, voxels);
        Voxel voxel1; //voxel of the left of the normal direction, i.e. the blocking
        Voxel voxel2; //voxel of the right of the normal direction, i.e. the blockee
        ArrayList<Voxel> setOfNeighbours = new ArrayList<>(); //stores the set of neighbours
        if (normalDir.equals("left")){
//            System.out.println(bfsi+") Traversing right i.e. x++");
            if (leftNeighbour != null){ //if there is a left neighbour  
                voxel1 = leftNeighbour;
                setOfNeighbours.add(voxel1);
                voxel2 = getRight(voxel1.x, voxel1.y, voxel1.z, inputVoxelizedMesh, inputVoxelizedMeshSize, voxels);
                breadthFirstTraversal3(voxel1,voxel2);
            }if (rightNeighbour != null){ //if there is a right neighbour
                voxel1 = rightNeighbour;
                setOfNeighbours.add(voxel1);
                voxel2 = getRight(voxel1.x, voxel1.y, voxel1.z, inputVoxelizedMesh, inputVoxelizedMeshSize, voxels);
                breadthFirstTraversal3(voxel1,voxel2);
            }if (upNeighbour != null){ //if there is an up neighbour
                voxel1 = upNeighbour;
                setOfNeighbours.add(voxel1);
                voxel2 = getRight(voxel1.x, voxel1.y, voxel1.z, inputVoxelizedMesh, inputVoxelizedMeshSize, voxels);
                breadthFirstTraversal3(voxel1,voxel2);
            }if (downNeighbour != null){ //if there is a down neighbour
                voxel1 = downNeighbour;
                setOfNeighbours.add(voxel1);
                voxel2 = getRight(voxel1.x, voxel1.y, voxel1.z, inputVoxelizedMesh, inputVoxelizedMeshSize, voxels);
                breadthFirstTraversal3(voxel1,voxel2);
            }if (forwardNeighbour != null){ //if there is a forward neighbour
                voxel1 = forwardNeighbour;
                setOfNeighbours.add(voxel1);
                voxel2 = getRight(voxel1.x, voxel1.y, voxel1.z, inputVoxelizedMesh, inputVoxelizedMeshSize, voxels);
                breadthFirstTraversal3(voxel1,voxel2);
            }if (backwardNeighbour != null){ //if there is a backward neighbour
                voxel1 = backwardNeighbour;
                setOfNeighbours.add(voxel1);
                voxel2 = getRight(voxel1.x, voxel1.y, voxel1.z, inputVoxelizedMesh, inputVoxelizedMeshSize, voxels);
                breadthFirstTraversal3(voxel1,voxel2);
            }
            //now that we have all level n neighbours, we can proceed to level n+1...
            for (Voxel v: setOfNeighbours){ //for each voxel in the set of neighbours...
                if ((!visitedAdjacentVoxels.contains(v))){ //if some neighbour has not been visited
                    breadthFirstTraversal2(v, normalDir); //visit their neighbours
                }
            }
        }
        else if (normalDir.equals("right")){
//            System.out.println(bfsi+") Traversing left i.e. x--");
            if (leftNeighbour != null){ //if there is a left neighbour  
                voxel1 = leftNeighbour;
                setOfNeighbours.add(voxel1);
                voxel2 = getLeft(voxel1.x, voxel1.y, voxel1.z, inputVoxelizedMesh, inputVoxelizedMeshSize, voxels);
                breadthFirstTraversal3(voxel1,voxel2);
            }if (rightNeighbour != null){ //if there is a right neighbour
                voxel1 = rightNeighbour;
                setOfNeighbours.add(voxel1);
                voxel2 = getLeft(voxel1.x, voxel1.y, voxel1.z, inputVoxelizedMesh, inputVoxelizedMeshSize, voxels);
                breadthFirstTraversal3(voxel1,voxel2);
            }if (upNeighbour != null){ //if there is an up neighbour
                voxel1 = upNeighbour; 
                setOfNeighbours.add(voxel1);
                voxel2 = getLeft(voxel1.x, voxel1.y, voxel1.z, inputVoxelizedMesh, inputVoxelizedMeshSize, voxels);
                breadthFirstTraversal3(voxel1,voxel2);
            }if (downNeighbour != null){ //if there is a down neighbour
                voxel1 = downNeighbour;
                setOfNeighbours.add(voxel1);
                voxel2 = getLeft(voxel1.x, voxel1.y, voxel1.z, inputVoxelizedMesh, inputVoxelizedMeshSize, voxels);
                breadthFirstTraversal3(voxel1,voxel2);
            }if (forwardNeighbour != null){ //if there is a forward neighbour
                voxel1 = forwardNeighbour;
                setOfNeighbours.add(voxel1);
                voxel2 = getLeft(voxel1.x, voxel1.y, voxel1.z, inputVoxelizedMesh, inputVoxelizedMeshSize, voxels);
                breadthFirstTraversal3(voxel1,voxel2);
            }if (backwardNeighbour != null){ //if there is a backward neighbour
                voxel1 = backwardNeighbour;
                setOfNeighbours.add(voxel1);
                voxel2 = getLeft(voxel1.x, voxel1.y, voxel1.z, inputVoxelizedMesh, inputVoxelizedMeshSize, voxels);
                breadthFirstTraversal3(voxel1,voxel2);
            }
            //now that we have all level n neighbours, we can proceed to level n+1...
            for (Voxel v: setOfNeighbours){ //for each voxel in the set of neighbours...
                if ((!visitedAdjacentVoxels.contains(v))){ //if some neighbour has not been visited
                    breadthFirstTraversal2(v, normalDir); //visit their neighbours
                }
            }
        }
        else if (normalDir.equals("forward")){
//            System.out.println(bfsi+") Traversing backward i.e. z--");
            if (leftNeighbour != null){ //if there is a left neighbour  
                voxel1 = leftNeighbour;
                setOfNeighbours.add(voxel1);
                voxel2 = getBackward(voxel1.x, voxel1.y, voxel1.z, inputVoxelizedMesh, inputVoxelizedMeshSize, voxels);
                breadthFirstTraversal3(voxel1,voxel2); 
            }if (rightNeighbour != null){ //if there is a right neighbour
                voxel1 = rightNeighbour;
                setOfNeighbours.add(voxel1);
                voxel2 = getBackward(voxel1.x, voxel1.y, voxel1.z, inputVoxelizedMesh, inputVoxelizedMeshSize, voxels);
                breadthFirstTraversal3(voxel1,voxel2);
            }if (upNeighbour != null){ //if there is an up neighbour
                voxel1 = upNeighbour;
                setOfNeighbours.add(voxel1);
                voxel2 = getBackward(voxel1.x, voxel1.y, voxel1.z, inputVoxelizedMesh, inputVoxelizedMeshSize, voxels);
                breadthFirstTraversal3(voxel1,voxel2);
            }if (downNeighbour != null){ //if there is a down neighbour
                voxel1 = downNeighbour;
                setOfNeighbours.add(voxel1);
                voxel2 = getBackward(voxel1.x, voxel1.y, voxel1.z, inputVoxelizedMesh, inputVoxelizedMeshSize, voxels);
                breadthFirstTraversal3(voxel1,voxel2);
            }if (forwardNeighbour != null){ //if there is a forward neighbour
                voxel1 = forwardNeighbour;
                setOfNeighbours.add(voxel1);
                voxel2 = getBackward(voxel1.x, voxel1.y, voxel1.z, inputVoxelizedMesh, inputVoxelizedMeshSize, voxels);
                breadthFirstTraversal3(voxel1,voxel2);
            }if (backwardNeighbour != null){ //if there is a backward neighbour
                voxel1 = backwardNeighbour;
                setOfNeighbours.add(voxel1);
                voxel2 = getBackward(voxel1.x, voxel1.y, voxel1.z, inputVoxelizedMesh, inputVoxelizedMeshSize, voxels);
                breadthFirstTraversal3(voxel1,voxel2);
            }
            //now that we have all level n neighbours, we can proceed to level n+1...
            for (Voxel v: setOfNeighbours){ //for each voxel in the set of neighbours...
                if ((!visitedAdjacentVoxels.contains(v))){ //if some neighbour has not been visited
                    breadthFirstTraversal2(v, normalDir); //visit their neighbours
                }
            }
        }
        else if (normalDir.equals("backward")){
//            System.out.println(bfsi+") Traversing forward i.e. z++");
            if (leftNeighbour != null){ //if there is a left neighbour  
                voxel1 = leftNeighbour;
                setOfNeighbours.add(voxel1);
                voxel2 = getForward(voxel1.x, voxel1.y, voxel1.z, inputVoxelizedMesh, inputVoxelizedMeshSize, voxels);
                breadthFirstTraversal3(voxel1,voxel2);
            }if (rightNeighbour != null){ //if there is a right neighbour
                voxel1 = rightNeighbour;
                setOfNeighbours.add(voxel1);
                voxel2 = getForward(voxel1.x, voxel1.y, voxel1.z, inputVoxelizedMesh, inputVoxelizedMeshSize, voxels);
                breadthFirstTraversal3(voxel1,voxel2);
            }if (upNeighbour != null){ //if there is an up neighbour
                voxel1 = upNeighbour;
                setOfNeighbours.add(voxel1);
                voxel2 = getForward(voxel1.x, voxel1.y, voxel1.z, inputVoxelizedMesh, inputVoxelizedMeshSize, voxels);
                breadthFirstTraversal3(voxel1,voxel2);
            }if (downNeighbour != null){ //if there is a down neighbour
                voxel1 = downNeighbour;
                setOfNeighbours.add(voxel1);
                voxel2 = getForward(voxel1.x, voxel1.y, voxel1.z, inputVoxelizedMesh, inputVoxelizedMeshSize, voxels);
                breadthFirstTraversal3(voxel1,voxel2);
            }if (forwardNeighbour != null){ //if there is a forward neighbour
                voxel1 = forwardNeighbour;
                setOfNeighbours.add(voxel1);
                voxel2 = getForward(voxel1.x, voxel1.y, voxel1.z, inputVoxelizedMesh, inputVoxelizedMeshSize, voxels);
                breadthFirstTraversal3(voxel1,voxel2);
            }if (backwardNeighbour != null){ //if there is a backward neighbour
                voxel1 = backwardNeighbour;
                setOfNeighbours.add(voxel1);
                voxel2 = getForward(voxel1.x, voxel1.y, voxel1.z, inputVoxelizedMesh, inputVoxelizedMeshSize, voxels);
                breadthFirstTraversal3(voxel1,voxel2); 
            }
            //now that we have all level n neighbours, we can proceed to level n+1...
            for (Voxel v: setOfNeighbours){ //for each voxel in the set of neighbours...
                if ((!visitedAdjacentVoxels.contains(v))){ //if some neighbour has not been visited
                    breadthFirstTraversal2(v, normalDir); //visit their neighbours
                }
            }
        }
    }
    
    /* a continutation of breadthFirstTraversal2 */
    static void breadthFirstTraversal3(Voxel voxel1, Voxel voxel2){
        if ((voxel2 != null)  && (voxelPairs.size()<Nb1) && (!visitedAdjacentVoxels.contains(voxel1))){ 
            //if there exists a possible pair and we still don't have enough voxel pairs  and voxel1 has not been visited...
            System.out.println(bfsi+") Blocking voxel is at "+voxel1.getCoordinates()+". Blockee voxel is at "+voxel2.getCoordinates());
            voxelPairs.add(new VoxelPair(voxel1,voxel2)); //add the pair to the list of pairs
            bfsi++;
        }
    }
    
    /* takes in a removable puzzle piece an calculates its sum of accessibility values*/
    public static double sumOfAccessVals(ArrayList<Voxel> piece){
        double sum = 0;
        for (Voxel v: piece) {
            sum = sum + v.accessibilityValue;
        }
        return sum;
    }
    
    static ArrayList<Voxel> addVoxels(ArrayList<Voxel> addedVoxels, ArrayList<PuzzlePiece> pieces, int[][][] vMesh, int vMeshSize, ArrayList<Voxel> vs, ArrayList<Voxel> candidates, int beta){
        //perhaps we will need a set of neighbours?
        Voxel anchor = null; //store these variables here so they can be used for the recursive case...
        Voxel anchor2 = null;
        Voxel blockingV = null;
        Voxel blockeeV = null;
        for (PuzzlePiece p: pieces) { //find piece p...
//            System.out.println("Selected piece is: "); debugPrintVoxels(addedVoxels); System.out.println(". Current piece is: "); debugPrintVoxels(p.piece);
            if (p.piece.equals(addedVoxels)){ //once we have retrieved our selected piece from the set of puzzle pieces generated and made removable
                anchor = p.anchorVoxel;
                anchor2 = p.anchorVoxel2;
                blockingV = p.blocking;
                blockeeV = p.blockee;
                System.out.println("SELECTION FOUND! Anchor is at "+anchor.getCoordinates()+" and anchor2 is at "+anchor2.getCoordinates());
                for (int i=0; i<addedVoxels.size(); i++) { //iterate through the voxels of the selected piece
                    Voxel currentVoxel = addedVoxels.get(i);
                    if ((!currentVoxel.equals(anchor)) && (!currentVoxel.equals(anchor2))){ //if the current voxel is not any of the anchors and is actually a voxel (set to 1)
                        //add neighbouring voxels (excluding the anchors or beneath them) until addedVoxels.size() > m...
                        Voxel leftNeighbour = getLeft(currentVoxel.x, currentVoxel.y, currentVoxel.z, vMesh, vMeshSize, vs); 
                        Voxel rightNeighbour = getRight(currentVoxel.x, currentVoxel.y, currentVoxel.z, vMesh, vMeshSize, vs); 
                        Voxel upNeighbour = getUp(currentVoxel.x, currentVoxel.y, currentVoxel.z, vMesh, vMeshSize, vs);
                        Voxel downNeighbour = getDown(currentVoxel.x, currentVoxel.y, currentVoxel.z, vMesh, vMeshSize, vs);
                        Voxel forwardNeighbour = getForward(currentVoxel.x, currentVoxel.y, currentVoxel.z, vMesh, vMeshSize, vs);
                        Voxel backwardNeighbour = getBackward(currentVoxel.x, currentVoxel.y, currentVoxel.z, vMesh, vMeshSize, vs);
                        if ((!candidates.contains(leftNeighbour)) && (!addedVoxels.contains(leftNeighbour)) && (leftNeighbour!=null) && (leftNeighbour.value==1)){ //if there is a left neighbour which is not already contained in addedVoxels nor in the set of candidates
                            candidates.add(leftNeighbour);
                        }if ((!candidates.contains(rightNeighbour)) && (!addedVoxels.contains(rightNeighbour)) && (rightNeighbour!=null) && (rightNeighbour.value==1)){ //if there is a right neighbour which is not already contained in addedVoxels nor in the set of candidates
                            candidates.add(rightNeighbour);
                        }if ((!candidates.contains(upNeighbour)) && (!addedVoxels.contains(upNeighbour)) && (upNeighbour!=null) && (upNeighbour.value==1)){ //if there is an up neighbour which is not already contained in addedVoxels nor in the set of candidates
                            candidates.add(upNeighbour);
                        }if ((!candidates.contains(downNeighbour)) && (!addedVoxels.contains(downNeighbour)) && ((downNeighbour!=null)  && (downNeighbour.value==1) && (downNeighbour.x==anchor.x) && (downNeighbour.y>anchor.y) && (downNeighbour.z==anchor.z))){
                            //if there is a down neighbour which is not already contained in addedVoxels nor in the set of candidates 
                            //and if it does not have a y-coordinate greater that either anchor given the same x and z
                            candidates.add(downNeighbour);
                        }if ((!candidates.contains(forwardNeighbour)) && (!addedVoxels.contains(forwardNeighbour)) && (forwardNeighbour!=null) && (forwardNeighbour.value==1)){ //if there is a forward neighbour which is not already contained in addedVoxels nor in the set of candidates
                            candidates.add(forwardNeighbour);
                        }if ((!candidates.contains(backwardNeighbour)) && (!addedVoxels.contains(backwardNeighbour)) && (backwardNeighbour!=null) && (backwardNeighbour.value==1)){ //if there is a backward neighbour which is not already contained in addedVoxels nor in the set of candidates
                            candidates.add(backwardNeighbour);
                        }
                    }
                }
            }
        }
        ArrayList<Voxel> addedVs = addVoxels2(addedVoxels, vMesh, vMeshSize, vs, candidates, beta); //add the voxels above the added neighbour
        if (addedVs.size() >= m){ //if size is suitable
            return addedVs; //return
        }else{
            System.out.println("Piece is still to small at size "+addedVs.size()+", repeating the process of expansion");
            if ((anchor != null) && (anchor2 != null) && (blockingV != null) && (blockeeV != null)){
                puzzlePieces.add(new PuzzlePiece(addedVoxels, anchor, anchor2, blockingV, blockeeV)); //add this updated piece
            }else{
                System.out.println("ERROR: some variables in addVoxels have not been initialized.");
            }
            return expandKeyPiece(addedVs); //repeat the process of expansion
        }
        
    }
   
    /* an implementation of section 1.4 point 2 & 3: Expands the key piece by adding neighbouring voxels by probability value pi2. Returns new set of added nieghbours */
    static ArrayList<Voxel> addVoxels2(ArrayList<Voxel> addedVoxels, int[][][] vMesh, int vMeshSize, ArrayList<Voxel> vs, ArrayList<Voxel> candidates, int beta){
        System.out.println("Debug printing the set of candidates...");
        debugPrintVoxels(candidates);
        System.out.println("---------------------------------------");
        //debug print the set of candidates
        //compute the sum of accessibility values all voxels in addedVoxels with the same x and z but lower y values than upNeighbour i.e voxels above the added neighbour 
        //for each voxel, check if it's upNeighbour is already contained in the addedVoxels piece...
        ArrayList<ArrayList> setOfUpNeighbours = new ArrayList<>(); //each neighbouring candidate and the voxels above will be contained in the set of neighbours
        double sumOfPis = 0;
        for (Voxel v: candidates) { //this loop is used for calculating pi and the sumOfPis
            Voxel upNeighbour = getUp(v.x, v.y, v.z, vMesh, vMeshSize, vs);
            ArrayList<Voxel> upNeighbours = new ArrayList<>(); //stores the list of voxels above the current voxel
            upNeighbours.add(v); //add the current voxel to the set
            while (upNeighbour!=null){ //for each neighbour above the current voxel
                upNeighbours.add(upNeighbour); //add the upNeighbour to the set of upNeighbours
                upNeighbour = getUp(upNeighbour.x, upNeighbour.y, upNeighbour.z, vMesh, vMeshSize, vs); //set upNeighbour to the voxel above the current upNeighbour
            }
            double sum = sumOfAccessVals(upNeighbours); //adds the sum of voxels above each voxel (ui)
            double pi = Math.pow(sum, -beta); //pi = sumi^(-β) i.e. the weighted sum
            sumOfPis = sumOfPis+pi; //add this pi to the sim of all pis
            setOfUpNeighbours.add(upNeighbours); //store the set of upNeighbours
        }
        //at this point we should have an array of neighbours above the current voxel; from this we should be able to calculate the pi2 of each voxel
        double sumOfProbabilities = 0;
        for (int i=0; i<setOfUpNeighbours.size(); i++) { //this loop is used for calculating pi2 given the sumOfPis
            ArrayList<Voxel> upNeighbours = setOfUpNeighbours.get(i);
            double sum = sumOfAccessVals(upNeighbours); //adds the sum of voxels above each voxel (ui)
            double pi = Math.pow(sum, -beta); //pi = sumi^(-β) i.e. the weighted sum
            Voxel ui = upNeighbours.get(0); //each neighbour (ui) will be the first element in the set of upNieghbours
            if (ui.getCoordinates().equals(candidates.get(i).getCoordinates())){
                ui.pi2 = pi/(sumOfPis); //pi2 = pi/∑ipi, the probability of addeding a voxel piece
                System.out.println((ui.pi2*100)+" is the probability of choosing voxel @ "+ui.getCoordinates());
                sumOfProbabilities = sumOfProbabilities + ui.pi2;
            }else{
                System.out.println("ERROR! Ui is @ "+ui.getCoordinates()+". It is supposed to be @ "+candidates.get(i).getCoordinates());
            }
        }
        //now we should be able to add voxels from the candidate set according to each ones probability. We must first ensure that the sum of probablities equals 1 (or 100)
        System.out.println("The sum of probabilites is: "+sumOfProbabilities);
        int iterator = 0; //an iterator to ensure the loop below breaks
        while (addedVoxels.size() < m && (iterator < 50)){ //now we will keep adding neighbouring voxels by probability until we have enough
            int max = (int)(sumOfProbabilities*100); //stores the maximum for the range 0 to sumOfProbilites (1) times 100. It should equal 100
            double randomNum = ThreadLocalRandom.current().nextDouble(0, max); //pick a random number between 0 and the max (100)
            double rum = (double)(randomNum/100);
            int randomNum2 = (int)(rum*candidates.size()); ////make the random number represent an index in the set of candidates by dividing it by the size. Should range from 0 to setOfCandidates-1
            System.out.println("Random number in the range of 0 - "+max+" is "+randomNum+" which represents index "+randomNum2);
            Voxel chosenCandidate = candidates.get(randomNum2); //get the chosen candidate from the appropriate index
            if (!addedVoxels.contains(chosenCandidate)){ //if the chosen candidate is not already in the array
                addedVoxels.add(chosenCandidate); //add the chosen candidate to the key piece
                System.out.print("Chosen candidate voxel is @ "+chosenCandidate.getCoordinates()+"! ");
                System.out.print("Added "+chosenCandidate.getCoordinates()+" to the key piece");
            }
            System.out.println("");
            iterator++;
        }
        return addedVoxels;
    }
   
    /* Flooding algorithm which helps compleete section 1.5) Confirming the key piece */
    static void floodFill(ArrayList<Voxel> keyPiece) throws FileNotFoundException, UnsupportedEncodingException{
        //change of strategy: the set of neighbours will now be the remaining volume...
    	ArrayList<Voxel> setOfNeighbours = new ArrayList<>(); //stores all voxels in the remaining volume
    	for (Voxel v: voxels) { //for each and every voxel
			if (!keyPiece.contains(v)) { //if the voxel is not in the keyPiece, add it to the set of neighbours
				setOfNeighbours.add(v);
    		}
    	}
        System.out.println("Debug printing the set of remaining volume (Rs)...");
        debugPrintVoxels(setOfNeighbours);
        int[][][] neighbours = new int[32][32][32]; //stores the set of neighbours in a 3D array
        for (int h = 0; h < setOfNeighbours.size(); h++) { //for each neighbour voxel
            for (int i = 0; i < 32; i++) { //iterates though zs
                for (int j = 0; j < 32; j++) { //iterates though ys
                    for (int k = 0; k < 32; k++) { //iterates though xs
                        neighbours[i][j][k] = 0; //fill it up with 0s
                    }
                }
            }
        }
        for (int h = 0; h < setOfNeighbours.size(); h++) { //for each neighbour voxel
            Voxel v = setOfNeighbours.get(h); //get the current voxel
            for (int i = 0; i < 32; i++) { //iterates though zs
                for (int j = 0; j < 32; j++) { //iterates though ys
                    for (int k = 0; k < 32; k++) { //iterates though xs
                        if ((v.x==k) && (v.y==j) && (v.z==i) && (neighbours[i][j][k]!=1)){ //if the coordinates match and index value has not already been set to 1
                            neighbours[i][j][k] = 1; //set the index value of neighbours to 1
                        }
                    }
                }
            }
        }
//        debugPrintVoxelizedMesh(neighbours);
        //now we should also be able to print the set of neighbours to a file
        io.printGridToFile(neighbours, "setOfNeighbours"); //should print the key piece to a file called keyPiece
        //2) apply flooding algorithm to test whether all voxels in Rs can be visited. If so, the key piece is confirmed. If not, reject the key piece...
        ArrayList<Voxel> unvisitedVoxels = new ArrayList<>(); //stores the set of unvisited voxels, initialized to the set of neighbours
        unvisitedVoxels = (ArrayList)setOfNeighbours.clone(); //copy the set of neighbours to avoid concurrency issues
        Voxel firstVoxel = unvisitedVoxels.get(0); //get the first voxel, this will be used to visit every other neighbour
        floodFill2(unvisitedVoxels, firstVoxel); //implements the visiting of every neighbouring voxel and removal from the set of unvisited neighbours
        if (unvisitedVoxels.isEmpty()){ //if the set of unvisited voxels is empty at the end, the key piece is confirmed
            System.out.println("THE KEY PIECE IS CONFIRMED!");
        }else{ //if there are still some unvisited voxels at the end, reject the key piece
            System.out.println("ERROR! THE KEY PIECE IS CANNOT BE CONFIRMED. Below are the voxels which remain unvisted...");
            for (Voxel v: unvisitedVoxels) {
                System.out.println(v.getCoordinates()+"; ");
            }
        }
    }
   
    /* Actual implementation of the flooding algorithm used for confirming the key piece */
    static void floodFill2(ArrayList<Voxel> unvisitedVoxels, Voxel firstVoxel){
        ArrayList<Voxel> visitedVoxels = new ArrayList<>();
        if (!unvisitedVoxels.isEmpty()){ //until the set of unvisited voxels is empty. We will go from the first voxel and try to visit every neighbour until the set is empty
            Voxel leftNeighbour = getLeft(firstVoxel.x, firstVoxel.y, firstVoxel.z, inputVoxelizedMesh, inputVoxelizedMeshSize, voxels); 
            Voxel rightNeighbour = getRight(firstVoxel.x, firstVoxel.y, firstVoxel.z, inputVoxelizedMesh, inputVoxelizedMeshSize, voxels); 
            Voxel upNeighbour = getUp(firstVoxel.x, firstVoxel.y, firstVoxel.z, inputVoxelizedMesh, inputVoxelizedMeshSize, voxels);
            Voxel downNeighbour = getDown(firstVoxel.x, firstVoxel.y, firstVoxel.z, inputVoxelizedMesh, inputVoxelizedMeshSize, voxels);
            Voxel forwardNeighbour = getForward(firstVoxel.x, firstVoxel.y, firstVoxel.z, inputVoxelizedMesh, inputVoxelizedMeshSize, voxels);
            Voxel backwardNeighbour = getBackward(firstVoxel.x, firstVoxel.y, firstVoxel.z, inputVoxelizedMesh, inputVoxelizedMeshSize, voxels);
            if (leftNeighbour != null){ //if there is a left neighbour
                if(unvisitedVoxels.contains(leftNeighbour) && (!visitedVoxels.contains(leftNeighbour))){ //if neighbour is in the set of unvisited voxels and not in the set of visited voxels
                    visitedVoxels.add(leftNeighbour); //add it to the set of visited voxels
                    unvisitedVoxels.remove(leftNeighbour); //remove it from the set of unvisited voxels
                }
            }if (rightNeighbour != null){ //if there is a right neighbour
                if(unvisitedVoxels.contains(rightNeighbour) && (!visitedVoxels.contains(rightNeighbour))){ //if neighbour is in the set of unvisited voxels and not in the set of visited voxels
                    visitedVoxels.add(rightNeighbour); //add it to the set of visited voxels
                    unvisitedVoxels.remove(rightNeighbour); //remove it from the set of unvisited voxels
                }
            }if (upNeighbour != null){ //if there is an up neighbour
                if(unvisitedVoxels.contains(upNeighbour) && (!visitedVoxels.contains(upNeighbour))){ //if neighbour is in the set of unvisited voxels and not in the set of visited voxels
                    visitedVoxels.add(upNeighbour); //add it to the set of visited voxels
                    unvisitedVoxels.remove(upNeighbour); //remove it from the set of unvisited voxels
                }
            }if (downNeighbour != null){ //if there is a down neighbour
                if(unvisitedVoxels.contains(downNeighbour) && (!visitedVoxels.contains(downNeighbour))){ //if neighbour is in the set of unvisited voxels and not in the set of visited voxels
                    visitedVoxels.add(downNeighbour); //add it to the set of visited voxels
                    unvisitedVoxels.remove(downNeighbour); //remove it from the set of unvisited voxels
                }
            }if (forwardNeighbour != null){ //if there is a forward neighbour
                if(unvisitedVoxels.contains(forwardNeighbour) && (!visitedVoxels.contains(forwardNeighbour))){ //if neighbour is in the set of unvisited voxels and not in the set of visited voxels
                    visitedVoxels.add(forwardNeighbour); //add it to the set of visited voxels
                    unvisitedVoxels.remove(forwardNeighbour); //remove it from the set of unvisited voxels
                }
            }if (backwardNeighbour != null){ //if there is a backward neighbour
                if(unvisitedVoxels.contains(backwardNeighbour) && (!visitedVoxels.contains(backwardNeighbour))){ //if neighbour is in the set of unvisited voxels and not in the set of visited voxels
                    visitedVoxels.add(backwardNeighbour); //add it to the set of visited voxels
                    unvisitedVoxels.remove(backwardNeighbour); //remove it from the set of unvisited voxels
                }
            }
            for (Voxel v: visitedVoxels){
                floodFill2(unvisitedVoxels, v); //recurse from the neighbouring voxels
            }
        }
    }
    
    /* find the piece with the smallest sum of accessibility values */
    static ArrayList<Voxel> selectPiece(){
        selectedPiece = new ArrayList<>();
        double currentHighestSum = 1000000; //stores the current lowest sum of accessibility values, initialized to 1000000
        for (ArrayList rPiece: removablePieces){
            double currentSum = sumOfAccessVals(rPiece);
            if (currentSum<currentHighestSum){ //if current sum of accessibility values is the lowest
                currentHighestSum = currentSum; //set the current highest sum to the current sum
                selectedPiece = rPiece; //set the selected piece to the current piece
            }
        }
        return selectedPiece;
    }
    
    static void debugPrintOutput(ArrayList<Voxel> keyPiece){
        System.out.println("\nCOMPLETE! Debug printing "+keyPiece.size()+" voxels of the final puzzle piece...");
        setOutputPieces(keyPiece, inputVoxelizedMeshSize); //represent a new puzzle piece in a 3D array
        debugPrintVoxelizedMesh(outputVoxelizedMesh); //print out the currently set output voxelized mesh
        System.out.println("-------------------------------------------------------------------------------------------");
    }
    
    static void subsequentPasses(ArrayList<Voxel> oldvoxels, ArrayList<Voxel> newvoxels, int noOfPasses, int pass, int q) {
        if (pass <= noOfPasses) { //for the sake of recursion...
            for (int z=0; z<inputVoxelizedMeshSize; z++){
                for (int y=0; y<inputVoxelizedMeshSize; y++){
                    for (int x=0; x<inputVoxelizedMeshSize; x++){
                        if (inputVoxelizedMesh[z][y][x] == 1){
                            //B_ijk = A_ijk + pow(alpha, pass) * sum of A_ijk values in neighbours of ijk
                            double newAccessValue; //stores the new accessibility value as per the subsequent passes
                            int index = indexOfCoordinate(x,y,z, inputVoxelizedMeshSize);
                            double weightFactor = 0.1; //set to 0.1 in Song et al (2012) implementation
                            double power = (double) Math.pow(weightFactor, pass); //alpha to the power of j in Song et al (2012) implementation
                            double sum = sumOfNeighboursAccValues(x,y,z,inputVoxelizedMesh,inputVoxelizedMeshSize,voxels); //stores the sum of accessibilty values of the voxel's neighbours
                            Voxel v = oldvoxels.get(index);
                            newAccessValue = v.accessibilityValue + (power * sum); //A = B. Calculates the new accessibility value
                            Voxel v2 = new Voxel(v.x, v.y, v.z, v.value); //the new voxel which is based on the old voxel and has a new accessibility value                    
                            v2.accessibilityValue = newAccessValue;
                            newvoxels.add(v2); //add v2 to newvoxels to store it
                            System.out.println(q+") Voxel "+v2+" at index "+x+","+y+","+z+" has accessibility value "+newAccessValue);
                            q++;
                        }
                    }
                }
            }
            System.out.println("");
            pass++; //increase the pass by one
            ArrayList<Voxel> voxels2 = new ArrayList<>(); //create a new voxels arraylist to store the set of new voxels and corresponding access values
            subsequentPasses(newvoxels, voxels2, noOfPasses, pass, q); //recurse with the new set of voxels being old and a new array being new
        }
        voxels = oldvoxels; //now we can replace the global set of voxels with the new set of voxels
    }
}
