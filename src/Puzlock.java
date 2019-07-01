import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author Nkosi Gumede
 */
public class Puzlock {
    static int [][][] inputVoxelizedMesh;
    static int [][][] outputVoxelizedMesh;
    static int inputVoxelizedMeshSize;
    static Voxel voxel;
    static Voxel seedVoxel;
    static ArrayList<Voxel> voxels = new ArrayList<>(); //used to store the set of exterior voxel
    static ArrayList<Voxel> exteriorVoxels = new ArrayList<>(); //used to store the set of exterior voxels
    static ArrayList<Integer> accessibilityValues = new ArrayList<>(); //used to store the set of exterior voxel
    static ArrayList<Voxel> unvisitedAdjacentVoxels;
    static int Nb1 = 50; //# of voxel pairs which are closest to the seed (50 as per Song et al implementation)
    static int Nb2 = 10; //selected among the Nb1 voxel pairs which have the smallest accessibility value (10 as per Song et al implementation)
    static ArrayList<VoxelPair> voxelPairs = new ArrayList<>(Nb1); //stores the Nb1 number of adjacent voxel pairs
    static ArrayList<VoxelPair> voxelPairs2 = new ArrayList<>(Nb2); //stores the Nb2 number of adjacent voxel pairs
    static int bfsi = 1; //an iterator for breadthFirstTraversal2 
    static int bfsi2 = 1; //an iterator for breadthFirstTraversal2 
    
    public static void main(String[] args){ 
        //0.1. Read the 3D grid
        inputVoxelizedMesh = readVoxelizedMesh();
        //0.2. Initialize the 1D array representing the 3D grid/array
        initializeVoxelArray(inputVoxelizedMesh);
        debugPrintVoxelizedMesh(inputVoxelizedMesh);
        debugPrintVoxels();
        //1. Extacting the key piece
        //1.1. Pick a seed voxel
        System.out.println("1.1. Picking a seed voxel...");
        pickSeedVoxel(inputVoxelizedMesh);
        //1.2. Compute voxel accessibility
        System.out.println("1.2. Computing voxel accessibility...");
        computeVoxelAccessibility();
        //1.3. Ensure blocking and mobility
        System.out.println("1.3. Ensuring blocking and mobility...");
        ensureBlockingMobility();
        System.out.println("COMPLETE!");
    }

    //0. Read/initialize the 3D grid
    static int[][][] readVoxelizedMesh(){
        //ideally we would use an input mesh, but we will generate a cube in this example...
        int [][][] voxelizedMesh = {
            {{1, 1, 1, 1},{1, 1, 1, 1},{1, 1, 1, 1},{1, 1, 1, 1}}, 
            {{1, 1, 1, 1},{1, 1, 1, 1},{1, 1, 1, 1},{1, 1, 1, 1}}, 
            {{1, 1, 1, 1},{1, 1, 1, 1},{1, 1, 1, 1},{1, 1, 1, 1}}, 
            {{1, 1, 1, 1},{1, 1, 1, 1},{1, 1, 1, 1},{1, 1, 1, 1}}
        }; 
        return voxelizedMesh;
    }

    //1. Extacting the key piece
    //1.1. Pick a seed voxel
    /* takes in an input mesh and  */
    public static void pickSeedVoxel(int[][][] vMesh){
        //• Identify a set of exterior voxels that have exactly a pair of adjacent exterior faces (with one being on top). 
        //Require that these voxels can move out of the puzzle in one movement.
        int i=1;
        for (Voxel v: voxels){
            if (v.y == inputVoxelizedMeshSize){
                //i.e. if voxel is at the top
                if (getLeft(v.x,v.y,v.z) == null){
                    //and if the 3D array has 0 or null at the left co-ordinate of that voxel...
                    exteriorVoxels.add(v); //this voxel should have met the requirement of being able to move out of the puzzle in one movement
                    v.normalDirection = "left";
                    System.out.println(i+") Added exterior voxel: "+v+" at co-ordinates "+v.x+", "+v.y+", "+v.z+" with normal direction "+v.normalDirection); //debug print the set of added exterior voxels...
                    i++;
                }
                if (getRight(v.x,v.y,v.z) == null){
                    //and if the 3D array has 0 or null at the right co-ordinate of that voxel...
                    exteriorVoxels.add(v); //this voxel should have met the requirement of being able to move out of the puzzle in one movement
                    v.normalDirection = "right";
                    System.out.println(i+") Added exterior voxel: "+v+" at co-ordinates "+v.x+", "+v.y+", "+v.z+" with normal direction "+v.normalDirection); //debug print the set of added exterior voxels...
                    i++;
                }
                if (getForward(v.x,v.y,v.z) == null){
                    //and if the 3D array has 0 or null at the forward co-ordinate of that voxel...
                    exteriorVoxels.add(v); //this voxel should have met the requirement of being able to move out of the puzzle in one movement
                    v.normalDirection = "forward";
                    System.out.println(i+") Added exterior voxel: "+v+" at co-ordinates "+v.x+", "+v.y+", "+v.z+" with normal direction "+v.normalDirection); //debug print the set of added exterior voxels...
                    i++;
                }
                if (getBackward(v.x,v.y,v.z) == null){
                    //and if the 3D array has 0 or null at the backward co-ordinate of that voxel...
                    exteriorVoxels.add(v); //this voxel should have met the requirement of being able to move out of the puzzle in one movement
                    v.normalDirection = "backward";
                    System.out.println(i+") Added exterior voxel: "+v+" at co-ordinates "+v.x+", "+v.y+", "+v.z+" with normal direction "+v.normalDirection); //debug print the set of added exterior voxels...
                    i++;
                }
            }
            
        }

        //• From the candidate set, we can either randomly pick a seed, or let the user make a choice.
        int randomNum = ThreadLocalRandom.current().nextInt(0, exteriorVoxels.size());
        seedVoxel = exteriorVoxels.get(randomNum);
        System.out.println("Seed voxel randomly chose from set is: "+seedVoxel+" at index "+randomNum+"\n");
        
    }

    //1.2. Compute voxel accessibility
    static void computeVoxelAccessibility(){
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
                        int index = indexOfCoordinate(x,y,z);
                        Voxel v = voxels.get(index);
                        int neighbours = countNeighbours(x,y,z);
                        v.accessibilityValue = neighbours;
                        System.out.println(p+") Voxel "+v+" at index "+x+","+y+","+z+" has "+neighbours+" neighbours");
                        p++;
                    }
                }
            }
        }
        System.out.println("");
        // subsequent passes - implicitly include neighbours from further afield
        int passes = 3; //# of passes (j from the eqn in the paper) i.e. = 3
        int q = 1;
        for (int pass = 1; pass < passes; pass++) {
            for (int z=0; z<inputVoxelizedMesh.length; z++){
                for (int y=0; y<inputVoxelizedMesh.length; y++){
                    for (int x=0; x<inputVoxelizedMesh.length; x++){
                        //B_ijk = A_ijk + pow(alpha, pass) * sum of A_ijk values in neighbours of ijk
                        double newAccessValue; //stores the new accessibility value as per the subsequent passes
                        int index = indexOfCoordinate(x,y,z);
                        double weightFactor = 0.1; //set to 0.1 in Song et al (2012) implementation

                        double power = (double) Math.pow(weightFactor, pass); //alpha to the power of j in Song et al (2012) implementation
                        double sum = sumOfNeighboursAccValues(x,y,z); //stores the sum of accessibilty values of the voxel's neighbours
                        newAccessValue = voxels.get(index).accessibilityValue + (power * sum); //I STOPPED HERE!!!
                        //A = B
                        Voxel v = voxels.get(index);
                        v.accessibilityValue = newAccessValue;
                        System.out.println(q+") Voxel "+v+" at index "+x+","+y+","+z+" has accessibility value "+newAccessValue);
                        q++;
                    }
                }
            }
            System.out.println("");
        }

    }

    //1.3. Ensure blocking and mobility
    static void ensureBlockingMobility(){
        /* • Develop the key piece such that it is removable by a translation along one direction. 
        Identify the normal direction (vn) of the non-upward-facing exterior face of the seed voxel. */
        String vn = seedVoxel.normalDirection; //stores the normal direction
        /* • Do a breadth-first traversal from the seed to find Nb1 pairs of voxels (that orient along vn) that are the nearest to the seed, 
        where in each pair, the voxels on the positive and negative sides of vn are called the blocker and blockee voxels, respectively. 
        Among them, we select Nb2 pairs whose blockee has the smallest accessibility among the Nb1 pairs (in their omplementation Nb1 and Nb2 are set to 50 and 10 respectively). */
        breadthFirstTraversal(); //for the purpose of storing the the Nb1 number of voxel pairs in the voxelPairs array
        //now we can select the Nb2 number of pairs whose blockee has the smallest accessibility value...
        //algorithm: store all blockees in an array sorted by accessibility value (minimum to maximum) and select the first Nb2 number of blockees..
        
        /* • Block the key from moving towards vn by 
        (i) determining a set of shortest path candidates from the seed to each blockee voxel candidate (without crossing the related blocking voxel and voxels below it); 
        we later will select one of them for evolving the key; and 
        (ii) extract all the voxels along a selected shortest path until the blockee, and adding these voxels to evolve the key piece. */


        /* • Ensure the key to be removable upward by including any voxel above the selected shortest path. 
        //This is why the shortest paths determined in the strategy above should not go through the blocking voxel or any voxel below it, else the blockage is destructed. 
        //Moreover, we ignore the shortest path candidates that eventually add excessive voxels since the key should less than m voxels. */

        /* • Devise a key that moves upward but not along vn. However, since new voxels are added to the key, 
        the key may accidentally become mobilized in a direction along which the seed was originally blocked (eg. +X, -Y, +/-Z). 
        This would require testing the blockage (or mobility) every time we add voxels to the key. 
        However, our third strategy avoids such test: we identify an anchor voxel that is directly-connected and furthest away from the seed (figure 9e). 
        The key idea is that if these anchor voxels stay with the remaining volume (but not added to the key), 
        the key can remain to be immobilized in the blocked directions even if we add more voxels to it. 
        Mobility test is not required to ensure the maintenance of the blockage. */

        /* • To choose among the shortest paths resulted from the first two strategies, we sum for each path the accessibility of all the voxels required to be added to the key, 
        i.e., voxels along the path, the blockee, and any voxel above. Then, we pick the one with the smallest accessibility sum for evolving the key with the appropriate blockage. */ 
    }

    //1.4. Expand the key piece
    void expandKeyPiece(){
        /* • Identify an additional anchor voxel for the direction immobilized by the blocking voxel picked in step 3 – it is furthest away from the blocking voxel in direction vn; 
        if no such voxels exist, we use the blocking voxel as the anchor; note that the anchor voxel idea is crucial for the expansion process as the existing blockage 
        could be undesirably destructed if an anchor voxel in inappropriately added to the key piece. */

        /* • Identify a set of candidate voxels to be added to the key, say {ui}, that are resided next to the key but neither at the anchor voxels nor below the anchors. 
        For each ui, we identify also the voxels directly above it, so we know the voxels required to be added to the key if ui is chosen. 
        Furthermore, if the number of voxels exceeds the number of extra voxels the key needs, we remove ui, from the candidate set. */

        /* • Sum the accessibility of each ui and the voxels above it, say sumi, and normalize pi = sumi^(-β) to be p2i = pi/∑ipi, where β is a parameter ranged from 1 to 6. 
        Hence, we can randomly pick a ui with p2i as the probability of choosing it, and expand the key piece. These substeps are repeated until the key contains roughly m voxels. */
    }  

    //1.5. Confirm the key piece
    void confirmKeyPiece(){
        /* • After steps 1 to 4, the key is guaranteed to fulfill all interlocking requirements, except that R2 needs to be simply connected. 
        With the help of accessibilty, the chance of fragmenting R2 (and other remaining volumes) is rather low; 
        hence, testing the connectivity of voxels in R2 at the end of the key piece generation procedure is more efficient than doing it at multiple places. 
        To guarantee that R2 is simply connected, we gather all the voxels next to the key in a set, say Rs, and apply a simple flooding algorithm to test 
        whether all voxels in Rs can be visited or not in R2. */
    }

    //2. Extracting other puzzle pieces
    /* The procedure of extracting subsequent puzzle pieces, e.g., Pi+1 from Ri, also starts by picking a seed voxel, and then growing Pi+1 from it. 
    However, since there are additional requirements for local interlocking among Pi, Pi+1, and Ri+1, the blocking mechanics are more involved.  
    To facilitate our discussion, we denote d>i as the target moving direction of Pi. */
    //2.1. Candidate seed voxels
    void candidateSeedVoxels(){
        /* • Since Pi+1 is blocked by Pi, but becomes mobilized as soon as Pi is removed, at least one of its voxel must reside next to Pi. 
        Since successive puzzle pieces should move in different directions (by Lemma 3 in Appendix), we use the contact between Pi and Pi+1 to define d>i+1 for blocking Pi+1 
        by the presence of Pi. Our strategy is to pick voxels (in Ri) next to Pi as candidate seeds, requiring them to contact P i in a direction perpendicular to d>i. 
        See Figure 11(b) for valid and invalid candidates in blue and violet, respectively. */

        /* • Since there may be too many valid candidates, trying them all is overly time consuming. 
        Hence, we compute the accessibility of voxels in Ri and reduce the number of candidates to ten by the following equally-weighted criteria: 
        (i) smaller accessibility value; and 
        (ii) shorter distance to the furthest-away voxel in Ri along d>i+1, see Figure 11(c) 
        for examples: an initial Pi+1 formed by C2 will contain more voxels as compared to C1 because of a longer shortest path determined by step 2 below. 
        Hence, the second criteria helps reduce the number of voxels that are required to form an initial Pi+1. 
        Note that we attempt to use fewer voxels (in early steps) to construct an initial Pi+1 because this allows us to have more flexibility when expanding the puzzle piece in step 3. */
    }

    //2.2. Create an initial Pi+1
    void createInitialPafter(){
        /* • After step 1, we have a set of candidate seeds, each associated with a d> i+1. Our next step is to pick one of them by examining its cost of making Pi+1 removable in d>i+1: 
        (i) from each candidate, we identify all voxels in Ri along d>i+1 (see the orange voxels in Figure 11(d)) 
        since these voxels must be taken to Pi+1 to make the candidate removable along d>i+1; 
        (ii) we determine a shortest path to connect the candidate to these identified voxels (Figure 11(e)), and 
        (iii) we locate also any additional voxel required to mobilize the shortest path towards d>i+1 (Figure 11(f)). 
        To choose among the candidates, we sum the accessibility of all the voxels involved in each candidate path (blue voxels in Figure 11(f)), 
        and pick the one with the smallest sum for forming the initial Pi+1. */
    }

    //2.3. Ensure local interlocking
    void ensureLocalInterlocking(){
        /* • Until now, Pi+1 is modeled with appropriate blocking for direction d>i+1 (Figure 11(f)), where its mobility towards d>i+1 depends on the presence of Pi alone. 
        Next, we further have to ensure appropriate blocking for the other five directions for achieving local interlocking in [Pi, Pi+1, Ri+1]: 
        (i) Pi+1 is immobilized in the presence of Ri+1 and Pi, and (ii) it cannot co-move with Pi. 
        For this, we perform a mobility check for each of the five directions to see if Pi+1 is blocked or not. 
        Note that the mobility check of Pi+1 along any direction d> is done by checking if any voxel from Pi or Ri+1 contacts Pi+1 along d>. 
        If this is true, Pi+1 is immobilized to move along d>. 
        Only if Pi+1 is movable, we apply the first two strategies in Section 5.1 (step 3) to extend Pi+1 to some blockee voxels for achieving appropriate blocking in related direction(s). */


        /* • For requirement (i) above, we perform the mobility check on Pi+1 in the presence of both Pi and Ri+1. 
        However, the tricky part for direction d>i is that since Pi+1 should not be co-movable with Pi, 
        we have to perform the mobility check on Pi+1 in the absence of Pi for this particular direction. 
        Lastly, note further that the anchor voxel strategy in Section 5.1 can also be applied here. 
        See Figure 10 (left) for the local interlocking established in an 8-piece 4^3 CUBE. */
    }

    //2.4. Expand Pi+1 and Confirm it
    void expandPafterandConfirm(){
        /* • After the above steps, Pi+1 can fulfill the local interlocking requirement, but yet we have to expand it to m voxels and check whether Ri+1 is simply connected or not. 
        These are done in the same way as in Section 5.1. */
    }
    
    /* Takes in a 3D array and stores it as voxels in a 1D voxel arraylist */
    static void initializeVoxelArray(int [][][] vMesh){
        for (int z=0; z<vMesh.length; z++){
            for (int y=0; y<vMesh.length; y++){
                for (int x=0; x<vMesh.length; x++){
                    voxels.add(new Voxel(x,y,z));
                }
            }
        }
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
            System.out.println("---");
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
    
    /* takes in a set of co-ordinates and returns the index of it on the arraylist */
    static synchronized int indexOfCoordinate(int x, int y, int z){
        int index = 0;
        index = index + (x*1);
        index = index + (y*4);
        index = index + (z*16);
        return index;
    }
    
    static void debugPrintVoxels(){
        for (int i=0; i<voxels.size(); i++) {
            Voxel v = voxels.get(i);
            System.out.println(i+") "+v+" is at "+v.x+", "+v.y+", "+v.z);
        }
        System.out.println("");
    }
    
    /* Takes in the co-ordinates of voxel and checks if there a voxel to the left in the inputVoxelizedMesh */
    static Voxel getLeft(int x, int y, int z){
        //it will either be 0, 1 or null
        try{
            if (inputVoxelizedMesh[x-1][y][z] == 1){ 
                int index = indexOfCoordinate(x-1, y, z);
                Voxel v = voxels.get(index);
                System.out.println("Voxel left of "+x+", "+y+", "+z+" is at "+v.x+", "+v.y+", "+v.z+". Correct answer: "+(x-1)+", "+y+", "+z);
                return v;
            }
        }catch(Exception e){}
        return null;
        
    }
    
    /* Takes in the co-ordinates of voxel and checks if there a voxel to the right in the inputVoxelizedMesh */
    static Voxel getRight(int x, int y, int z){
        //it will either be 0, 1 or null
        try{
            if (inputVoxelizedMesh[x+1][y][z] == 1){ 
                int index = indexOfCoordinate(x+1, y, z);
                Voxel v = voxels.get(index);
                System.out.println("Voxel right of "+x+", "+y+", "+z+" is at "+v.x+", "+v.y+", "+v.z+". Correct answer: "+(x+1)+", "+y+", "+z);
                return v;
            }
        }catch(Exception e){}
        return null;
    }
    
    /* Takes in the co-ordinates of voxel and checks if there a voxel above the inputVoxelizedMesh */
    static Voxel getUp(int x, int y, int z){
        //it will either be 0, 1 or null
        try{
            if (inputVoxelizedMesh[x][y+1][z] == 1){ 
                int index = indexOfCoordinate(x, y+1, z);
                Voxel v = voxels.get(index);
                System.out.println("Voxel up of "+x+", "+y+", "+z+" is at "+v.x+", "+v.y+", "+v.z+". Correct answer: "+x+", "+(y+1)+", "+z);
                return v;
            }
        }catch(Exception e){}
        return null;
    }
    
    /* Takes in the co-ordinates of voxel and checks if there a voxel beneath the inputVoxelizedMesh */
    static Voxel getDown(int x, int y, int z){
        //it will either be 0, 1 or null
        try{
            if (inputVoxelizedMesh[x][y-1][z] == 1){ 
                int index = indexOfCoordinate(x, y-1, z);
                Voxel v = voxels.get(index);
                System.out.println("Voxel down of "+x+", "+y+", "+z+" is at "+v.x+", "+v.y+", "+v.z+". Correct answer: "+x+", "+(y-1)+", "+z);
                return v;
            }
        }catch(Exception e){}
        return null;
    }
    
    /* Takes in the co-ordinates of voxel and checks if there a voxel in front of the inputVoxelizedMesh */
    static Voxel getForward(int x, int y, int z){
        //it will either be 0, 1 or null
        try{
            if (inputVoxelizedMesh[x][y][z+1] == 1){ 
                int index = indexOfCoordinate(x, y, z+1);
                Voxel v = voxels.get(index);
                System.out.println("Voxel forward of "+x+", "+y+", "+z+" is at "+v.x+", "+v.y+", "+v.z+". Correct answer: "+x+", "+y+", "+(z+1));
                return v;
            }
        }catch(Exception e){}
        return null;
    }
    
    /* Takes in the co-ordinates of voxel and checks if there a voxel behind the inputVoxelizedMesh */
    static Voxel getBackward(int x, int y, int z){
        //it will either be 0, 1 or null
        try{
            if (inputVoxelizedMesh[x][y][z-1] == 1){ 
                int index = indexOfCoordinate(x, y, z-1);
                Voxel v = voxels.get(index);
                System.out.println("Voxel backward of "+x+", "+y+", "+z+" is at "+v.x+", "+v.y+", "+v.z+". Correct answer: "+x+", "+y+", "+(z-1));
                return v;
            }
        }catch(Exception e){}
        return null;
    }
    
    /* Takes in a voxel's co-ordinates and counts its neighbours */
    static int countNeighbours(int x, int y, int z){
        //neighbour can either be 0, 1 or null...
        int neighbours = 0;
        try{
            if (inputVoxelizedMesh[x-1][y][z] == 1){
                //check left
                neighbours++;
            }
        }catch(Exception e){}
        try{
            if (inputVoxelizedMesh[x+1][y][z] == 1){
                //check right
                neighbours++;
            }
        }catch(Exception e){}
        try{
            if (inputVoxelizedMesh[x][y+1][z] == 1){
                //check up
                neighbours++;
            }
        }catch(Exception e){}
        try{
            if (inputVoxelizedMesh[x][y-1][z] == 1){
                //check down
                neighbours++;
            }
        }catch(Exception e){}
        try{
            if (inputVoxelizedMesh[x][y][z+1] == 1){
                //check forward
                neighbours++;
            }
        }catch(Exception e){}
        try{
            if (inputVoxelizedMesh[x][y][z-1] == 1){
                //check backward
                neighbours++;
            }
        }catch(Exception e){}

        return neighbours;
    }
    
    /* Takes in a voxel's co-ordinates and counts its neighbours */
    static double sumOfNeighboursAccValues(int x, int y, int z){
        double sum = 0;
        try{
            if (inputVoxelizedMesh[x-1][y][z] == 1){
                //check left
                int index = indexOfCoordinate(x-1, y, z); //index of left voxel
                double accVal = voxels.get(index).accessibilityValue; //accessibilty value of the voxel at that index
                sum = sum + accVal;
            }
        }catch(Exception e){}
        try{
            if (inputVoxelizedMesh[x+1][y][z] == 1){
                //check right
                int index = indexOfCoordinate(x+1, y, z); //index of left voxel
                double accVal = voxels.get(index).accessibilityValue; //accessibilty value of the voxel at that index
                sum = sum + accVal;
            }
        }catch(Exception e){}
        try{
            if (inputVoxelizedMesh[x][y+1][z] == 1){
                //check up
                int index = indexOfCoordinate(x, y+1, z); //index of left voxel
               double accVal = voxels.get(index).accessibilityValue; //accessibilty value of the voxel at that index
                sum = sum + accVal;
            }
        }catch(Exception e){}
        try{
            if (inputVoxelizedMesh[x][y-1][z] == 1){
                //check down
                int index = indexOfCoordinate(x, y-1, z); //index of left voxel
                double accVal = voxels.get(index).accessibilityValue; //accessibilty value of the voxel at that index
                sum = sum + accVal;
            }
        }catch(Exception e){}
        try{
            if (inputVoxelizedMesh[x][y][z+1] == 1){
                //check forward
                int index = indexOfCoordinate(x, y, z+1); //index of left voxel
                double accVal = voxels.get(index).accessibilityValue; //accessibilty value of the voxel at that index
                sum = sum + accVal;
            }
        }catch(Exception e){}
        try{
            if (inputVoxelizedMesh[x][y][z-1] == 1){
                //check backward
                int index = indexOfCoordinate(x, y, z-1); //index of left voxel
                double accVal = voxels.get(index).accessibilityValue; //accessibilty value of the voxel at that index
                sum = sum + accVal;
            }
        }catch(Exception e){}
        //return the sum...
        return sum;
    }
    
    /* conducts the breadth-first traversal from the seed voxel which is necessary to store the blocker and blockee voxel pairs */
    static void breadthFirstTraversal(){
        unvisitedAdjacentVoxels = new ArrayList<>(inputVoxelizedMeshSize); //stores the currently unvisited voxels which are adjacent to the current voxel 
        unvisitedAdjacentVoxels = voxels;       
        System.out.println("Breadth-first traversal from the seed voxel...");
        //first determine the direction in which to traverse though the graph to find the nearest pairs of voxels...
        Voxel currentVoxel = seedVoxel;  
        String normalDir = seedVoxel.normalDirection;
        System.out.println("Seed voxel normal direction = "+normalDir);
        breadthFirstTraversal2(currentVoxel, normalDir); //traverse thought its neighbours
    }
    
    /* takes in a voxel and its nornal direction, and traverses through its neighbours, setting the blocking pairs and reducing the set of unvisited voxels */
    static synchronized void breadthFirstTraversal2(Voxel currentVoxel, String normalDir){   
        if (!unvisitedAdjacentVoxels.isEmpty() && voxelPairs.size()<Nb1){ //if the list of unvisited adjacent voxels is not empty i.e. there are unvisited voxels; and we have not exceeded the number of pairs needed...
            unvisitedAdjacentVoxels.remove(currentVoxel); //remove the current voxel from the set of unvisited voxels
            //visit current voxels neighbours...
            Voxel leftNeighbour = getLeft(currentVoxel.x, currentVoxel.y, currentVoxel.z); 
            Voxel rightNeighbour = getRight(currentVoxel.x, currentVoxel.y, currentVoxel.z); 
            Voxel upNeighbour = getUp(currentVoxel.x, currentVoxel.y, currentVoxel.z);
            Voxel downNeighbour = getDown(currentVoxel.x, currentVoxel.y, currentVoxel.z);
            Voxel forwardNeighbour = getForward(currentVoxel.x, currentVoxel.y, currentVoxel.z);
            Voxel backwardNeighbour = getBackward(currentVoxel.x, currentVoxel.y, currentVoxel.z);

            if (normalDir.equals("left")){
                System.out.println(bfsi+") Traversing right i.e. x++");
                if (leftNeighbour != null){ //if there is a left neighbour  
                    Voxel voxel1 = leftNeighbour; //voxel of the left of the normal direction, i.e. the blockee
                    Voxel voxel2 = getRight(voxel1.x, voxel1.y, voxel1.z); //voxel of the right of the normal direction, i.e. the blocking
                    if (voxel2 != null){ //if there exists a possible pair
                        System.out.println("Voxel1 is at "+voxel1.x+", "+voxel1.y+", "+voxel1.z+". Voxel2 is at "+voxel2.x+", "+voxel2.y+", "+voxel2.z);
                        voxelPairs.add(new VoxelPair(voxel1,voxel2)); //add the pair to the list of pairs
                        unvisitedAdjacentVoxels.remove(voxel1); //remove voxel1 from the set of unvisited voxels
                        bfsi++;
                        breadthFirstTraversal2(voxel1, normalDir); //recurse from the left neighbour
                    } 
                }if (rightNeighbour != null){ //if there is a right neighbour
                    Voxel voxel1 = rightNeighbour; //voxel of the left of the normal direction, i.e. the blockee
                    Voxel voxel2 = getRight(voxel1.x, voxel1.y, voxel1.z); //voxel of the right of the normal direction, i.e. the blocking
                    if (voxel2 != null){ //if there exists a possible pair
                        System.out.println("Voxel1 is at "+voxel1.x+", "+voxel1.y+", "+voxel1.z+". Voxel2 is at "+voxel2.x+", "+voxel2.y+", "+voxel2.z);
                        voxelPairs.add(new VoxelPair(voxel1,voxel2)); //add the pair to the list of pairs
                        unvisitedAdjacentVoxels.remove(voxel1); //remove voxel1 from the set of unvisited voxels
                        bfsi++;
                        breadthFirstTraversal2(voxel1, normalDir); //recurse from the right neighbour
                    } 
                }if (upNeighbour != null){ //if there is an up neighbour
                    Voxel voxel1 = upNeighbour; //voxel of the left of the normal direction, i.e. the blockee
                    Voxel voxel2 = getRight(voxel1.x, voxel1.y, voxel1.z); //voxel of the right of the normal direction, i.e. the blocking
                    if (voxel2 != null){ //if there exists a possible pair
                        System.out.println("Voxel1 is at "+voxel1.x+", "+voxel1.y+", "+voxel1.z+". Voxel2 is at "+voxel2.x+", "+voxel2.y+", "+voxel2.z);
                        voxelPairs.add(new VoxelPair(voxel1,voxel2)); //add the pair to the list of pairs
                        unvisitedAdjacentVoxels.remove(voxel1); //remove voxel1 from the set of unvisited voxels
                        bfsi++;
                        breadthFirstTraversal2(voxel1, normalDir); //recurse from the up neighbour
                    } 
                }if (downNeighbour != null){ //if there is a down neighbour
                    Voxel voxel1 = downNeighbour; //voxel of the left of the normal direction, i.e. the blockee
                    Voxel voxel2 = getRight(voxel1.x, voxel1.y, voxel1.z); //voxel of the right of the normal direction, i.e. the blocking
                    if (voxel2 != null){ //if there exists a possible pair
                        System.out.println("Voxel1 is at "+voxel1.x+", "+voxel1.y+", "+voxel1.z+". Voxel2 is at "+voxel2.x+", "+voxel2.y+", "+voxel2.z);
                        voxelPairs.add(new VoxelPair(voxel1,voxel2)); //add the pair to the list of pairs
                        unvisitedAdjacentVoxels.remove(voxel1); //remove voxel1 from the set of unvisited voxels
                        bfsi++;
                        breadthFirstTraversal2(voxel1, normalDir); //recurse from the down neighbour
                    } 
                }if (forwardNeighbour != null){ //if there is a forward neighbour
                    Voxel voxel1 = forwardNeighbour; //voxel of the left of the normal direction, i.e. the blockee
                    Voxel voxel2 = getRight(voxel1.x, voxel1.y, voxel1.z); //voxel of the right of the normal direction, i.e. the blocking
                    if (voxel2 != null){ //if there exists a possible pair
                        System.out.println("Voxel1 is at "+voxel1.x+", "+voxel1.y+", "+voxel1.z+". Voxel2 is at "+voxel2.x+", "+voxel2.y+", "+voxel2.z);
                        voxelPairs.add(new VoxelPair(voxel1,voxel2)); //add the pair to the list of pairs
                        unvisitedAdjacentVoxels.remove(voxel1); //remove voxel1 from the set of unvisited voxels
                        bfsi++;
                        breadthFirstTraversal2(voxel1, normalDir); //recurse from the forward neighbour
                    } 
                }if (backwardNeighbour != null){ //if there is a backward neighbour
                    Voxel voxel1 = backwardNeighbour; //voxel of the left of the normal direction, i.e. the blockee
                    Voxel voxel2 = getRight(voxel1.x, voxel1.y, voxel1.z); //voxel of the right of the normal direction, i.e. the blocking
                    if (voxel2 != null){ //if there exists a possible pair
                        System.out.println("Voxel1 is at "+voxel1.x+", "+voxel1.y+", "+voxel1.z+". Voxel2 is at "+voxel2.x+", "+voxel2.y+", "+voxel2.z);
                        voxelPairs.add(new VoxelPair(voxel1,voxel2)); //add the pair to the list of pairs
                        unvisitedAdjacentVoxels.remove(voxel1); //remove voxel1 from the set of unvisited voxels
                        bfsi++;
                        breadthFirstTraversal2(voxel1, normalDir); //recurse from the backward neighbour
                    } 
                }
            }
            else if (normalDir.equals("right")){
                System.out.println(bfsi+") Traversing left i.e. x--");
                if (leftNeighbour != null){ //if there is a left neighbour  
                    Voxel voxel1 = leftNeighbour; //voxel of the left of the normal direction, i.e. the blockee
                    Voxel voxel2 = getLeft(voxel1.x, voxel1.y, voxel1.z); //voxel of the right of the normal direction, i.e. the blocking
                    if (voxel2 != null){ //if there exists a possible pair
                        System.out.println("Voxel1 is at "+voxel1.x+", "+voxel1.y+", "+voxel1.z+". Voxel2 is at "+voxel2.x+", "+voxel2.y+", "+voxel2.z);
                        voxelPairs.add(new VoxelPair(voxel1,voxel2)); //add the pair to the list of pairs
                        unvisitedAdjacentVoxels.remove(voxel1); //remove voxel1 from the set of unvisited voxels
                        bfsi++;
                        breadthFirstTraversal2(voxel1, normalDir); //recurse from the left neighbour
                    } 
                }if (rightNeighbour != null){ //if there is a right neighbour
                    Voxel voxel1 = rightNeighbour; //voxel of the left of the normal direction, i.e. the blockee
                    Voxel voxel2 = getLeft(voxel1.x, voxel1.y, voxel1.z); //voxel of the right of the normal direction, i.e. the blocking
                    if (voxel2 != null){ //if there exists a possible pair
                        System.out.println("Voxel1 is at "+voxel1.x+", "+voxel1.y+", "+voxel1.z+". Voxel2 is at "+voxel2.x+", "+voxel2.y+", "+voxel2.z);
                        voxelPairs.add(new VoxelPair(voxel1,voxel2)); //add the pair to the list of pairs
                        unvisitedAdjacentVoxels.remove(voxel1); //remove voxel1 from the set of unvisited voxels
                        bfsi++;
                        breadthFirstTraversal2(voxel1, normalDir); //recurse from the right neighbour
                    } 
                }if (upNeighbour != null){ //if there is an up neighbour
                    Voxel voxel1 = upNeighbour; //voxel of the left of the normal direction, i.e. the blockee
                    Voxel voxel2 = getLeft(voxel1.x, voxel1.y, voxel1.z); //voxel of the right of the normal direction, i.e. the blocking
                    if (voxel2 != null){ //if there exists a possible pair
                        System.out.println("Voxel1 is at "+voxel1.x+", "+voxel1.y+", "+voxel1.z+". Voxel2 is at "+voxel2.x+", "+voxel2.y+", "+voxel2.z);
                        voxelPairs.add(new VoxelPair(voxel1,voxel2)); //add the pair to the list of pairs
                        unvisitedAdjacentVoxels.remove(voxel1); //remove voxel1 from the set of unvisited voxels
                        bfsi++;
                        breadthFirstTraversal2(voxel1, normalDir); //recurse from the up neighbour
                    } 
                }if (downNeighbour != null){ //if there is a down neighbour
                    Voxel voxel1 = downNeighbour; //voxel of the left of the normal direction, i.e. the blockee
                    Voxel voxel2 = getLeft(voxel1.x, voxel1.y, voxel1.z); //voxel of the right of the normal direction, i.e. the blocking
                    if (voxel2 != null){ //if there exists a possible pair
                        System.out.println("Voxel1 is at "+voxel1.x+", "+voxel1.y+", "+voxel1.z+". Voxel2 is at "+voxel2.x+", "+voxel2.y+", "+voxel2.z);
                        voxelPairs.add(new VoxelPair(voxel1,voxel2)); //add the pair to the list of pairs
                        unvisitedAdjacentVoxels.remove(voxel1); //remove voxel1 from the set of unvisited voxels
                        bfsi++;
                        breadthFirstTraversal2(voxel1, normalDir); //recurse from the down neighbour
                    } 
                }if (forwardNeighbour != null){ //if there is a forward neighbour
                    Voxel voxel1 = forwardNeighbour; //voxel of the left of the normal direction, i.e. the blockee
                    Voxel voxel2 = getLeft(voxel1.x, voxel1.y, voxel1.z); //voxel of the right of the normal direction, i.e. the blocking
                    if (voxel2 != null){ //if there exists a possible pair
                        System.out.println("Voxel1 is at "+voxel1.x+", "+voxel1.y+", "+voxel1.z+". Voxel2 is at "+voxel2.x+", "+voxel2.y+", "+voxel2.z);
                        voxelPairs.add(new VoxelPair(voxel1,voxel2)); //add the pair to the list of pairs
                        unvisitedAdjacentVoxels.remove(voxel1); //remove voxel1 from the set of unvisited voxels
                        bfsi++;
                        breadthFirstTraversal2(voxel1, normalDir); //recurse from the forward neighbour
                    } 
                }if (backwardNeighbour != null){ //if there is a backward neighbour
                    Voxel voxel1 = backwardNeighbour; //voxel of the left of the normal direction, i.e. the blockee
                    Voxel voxel2 = getLeft(voxel1.x, voxel1.y, voxel1.z); //voxel of the right of the normal direction, i.e. the blocking
                    if (voxel2 != null){ //if there exists a possible pair
                        System.out.println("Voxel1 is at "+voxel1.x+", "+voxel1.y+", "+voxel1.z+". Voxel2 is at "+voxel2.x+", "+voxel2.y+", "+voxel2.z);
                        voxelPairs.add(new VoxelPair(voxel1,voxel2)); //add the pair to the list of pairs
                        unvisitedAdjacentVoxels.remove(voxel1); //remove voxel1 from the set of unvisited voxels
                        bfsi++;
                        breadthFirstTraversal2(voxel1, normalDir); //recurse from the backward neighbour
                    } 
                }
            }
            else if (normalDir.equals("forward")){
                System.out.println(bfsi+") Traversing backward i.e. z--");
                if (leftNeighbour != null){ //if there is a left neighbour  
                    Voxel voxel1 = leftNeighbour; //voxel of the left of the normal direction, i.e. the blockee
                    Voxel voxel2 = getForward(voxel1.x, voxel1.y, voxel1.z); //voxel of the right of the normal direction, i.e. the blocking
                    if (voxel2 != null){ //if there exists a possible pair
                        System.out.println("Voxel1 is at "+voxel1.x+", "+voxel1.y+", "+voxel1.z+". Voxel2 is at "+voxel2.x+", "+voxel2.y+", "+voxel2.z);
                        voxelPairs.add(new VoxelPair(voxel1,voxel2)); //add the pair to the list of pairs
                        unvisitedAdjacentVoxels.remove(voxel1); //remove voxel1 from the set of unvisited voxels
                        bfsi++;
                        breadthFirstTraversal2(voxel1, normalDir); //recurse from the left neighbour
                    } 
                }if (rightNeighbour != null){ //if there is a right neighbour
                    Voxel voxel1 = rightNeighbour; //voxel of the left of the normal direction, i.e. the blockee
                    Voxel voxel2 = getForward(voxel1.x, voxel1.y, voxel1.z); //voxel of the right of the normal direction, i.e. the blocking
                    if (voxel2 != null){ //if there exists a possible pair
                        System.out.println("Voxel1 is at "+voxel1.x+", "+voxel1.y+", "+voxel1.z+". Voxel2 is at "+voxel2.x+", "+voxel2.y+", "+voxel2.z);
                        voxelPairs.add(new VoxelPair(voxel1,voxel2)); //add the pair to the list of pairs
                        unvisitedAdjacentVoxels.remove(voxel1); //remove voxel1 from the set of unvisited voxels
                        bfsi++;
                        breadthFirstTraversal2(voxel1, normalDir); //recurse from the right neighbour
                    } 
                }if (upNeighbour != null){ //if there is an up neighbour
                    Voxel voxel1 = upNeighbour; //voxel of the left of the normal direction, i.e. the blockee
                    Voxel voxel2 = getForward(voxel1.x, voxel1.y, voxel1.z); //voxel of the right of the normal direction, i.e. the blocking
                    if (voxel2 != null){ //if there exists a possible pair
                        System.out.println("Voxel1 is at "+voxel1.x+", "+voxel1.y+", "+voxel1.z+". Voxel2 is at "+voxel2.x+", "+voxel2.y+", "+voxel2.z);
                        voxelPairs.add(new VoxelPair(voxel1,voxel2)); //add the pair to the list of pairs
                        unvisitedAdjacentVoxels.remove(voxel1); //remove voxel1 from the set of unvisited voxels
                        bfsi++;
                        breadthFirstTraversal2(voxel1, normalDir); //recurse from the up neighbour
                    } 
                }if (downNeighbour != null){ //if there is a down neighbour
                    Voxel voxel1 = downNeighbour; //voxel of the left of the normal direction, i.e. the blockee
                    Voxel voxel2 = getForward(voxel1.x, voxel1.y, voxel1.z); //voxel of the right of the normal direction, i.e. the blocking
                    if (voxel2 != null){ //if there exists a possible pair
                        System.out.println("Voxel1 is at "+voxel1.x+", "+voxel1.y+", "+voxel1.z+". Voxel2 is at "+voxel2.x+", "+voxel2.y+", "+voxel2.z);
                        voxelPairs.add(new VoxelPair(voxel1,voxel2)); //add the pair to the list of pairs
                        unvisitedAdjacentVoxels.remove(voxel1); //remove voxel1 from the set of unvisited voxels
                        bfsi++;
                        breadthFirstTraversal2(voxel1, normalDir); //recurse from the down neighbour
                    } 
                }if (forwardNeighbour != null){ //if there is a forward neighbour
                    Voxel voxel1 = forwardNeighbour; //voxel of the left of the normal direction, i.e. the blockee
                    Voxel voxel2 = getForward(voxel1.x, voxel1.y, voxel1.z); //voxel of the right of the normal direction, i.e. the blocking
                    if (voxel2 != null){ //if there exists a possible pair
                        System.out.println("Voxel1 is at "+voxel1.x+", "+voxel1.y+", "+voxel1.z+". Voxel2 is at "+voxel2.x+", "+voxel2.y+", "+voxel2.z);
                        voxelPairs.add(new VoxelPair(voxel1,voxel2)); //add the pair to the list of pairs
                        unvisitedAdjacentVoxels.remove(voxel1); //remove voxel1 from the set of unvisited voxels
                        bfsi++;
                        breadthFirstTraversal2(voxel1, normalDir); //recurse from the forward neighbour
                    } 
                }if (backwardNeighbour != null){ //if there is a backward neighbour
                    Voxel voxel1 = backwardNeighbour; //voxel of the left of the normal direction, i.e. the blockee
                    Voxel voxel2 = getForward(voxel1.x, voxel1.y, voxel1.z); //voxel of the right of the normal direction, i.e. the blocking
                    if (voxel2 != null){ //if there exists a possible pair
                        System.out.println("Voxel1 is at "+voxel1.x+", "+voxel1.y+", "+voxel1.z+". Voxel2 is at "+voxel2.x+", "+voxel2.y+", "+voxel2.z);
                        voxelPairs.add(new VoxelPair(voxel1,voxel2)); //add the pair to the list of pairs
                        unvisitedAdjacentVoxels.remove(voxel1); //remove voxel1 from the set of unvisited voxels
                        bfsi++;
                        breadthFirstTraversal2(voxel1, normalDir); //recurse from the backward neighbour
                    } 
                }
            }
            else if (normalDir.equals("backward")){
                System.out.println(bfsi+") Traversing forward i.e. z++");
                if (leftNeighbour != null){ //if there is a left neighbour  
                    Voxel voxel1 = leftNeighbour; //voxel of the left of the normal direction, i.e. the blockee
                    Voxel voxel2 = getBackward(voxel1.x, voxel1.y, voxel1.z); //voxel of the right of the normal direction, i.e. the blocking
                    if (voxel2 != null){ //if there exists a possible pair
                        System.out.println("Voxel1 is at "+voxel1.x+", "+voxel1.y+", "+voxel1.z+". Voxel2 is at "+voxel2.x+", "+voxel2.y+", "+voxel2.z);
                        voxelPairs.add(new VoxelPair(voxel1,voxel2)); //add the pair to the list of pairs
                        unvisitedAdjacentVoxels.remove(voxel1); //remove voxel1 from the set of unvisited voxels
                        bfsi++;
                        breadthFirstTraversal2(voxel1, normalDir); //recurse from the left neighbour
                    } 
                }if (rightNeighbour != null){ //if there is a right neighbour
                    Voxel voxel1 = rightNeighbour; //voxel of the left of the normal direction, i.e. the blockee
                    Voxel voxel2 = getBackward(voxel1.x, voxel1.y, voxel1.z); //voxel of the right of the normal direction, i.e. the blocking
                    if (voxel2 != null){ //if there exists a possible pair
                        System.out.println("Voxel1 is at "+voxel1.x+", "+voxel1.y+", "+voxel1.z+". Voxel2 is at "+voxel2.x+", "+voxel2.y+", "+voxel2.z);
                        voxelPairs.add(new VoxelPair(voxel1,voxel2)); //add the pair to the list of pairs
                        unvisitedAdjacentVoxels.remove(voxel1); //remove voxel1 from the set of unvisited voxels
                        bfsi++;
                        breadthFirstTraversal2(voxel1, normalDir); //recurse from the right neighbour
                    } 
                }if (upNeighbour != null){ //if there is an up neighbour
                    Voxel voxel1 = upNeighbour; //voxel of the left of the normal direction, i.e. the blockee
                    Voxel voxel2 = getBackward(voxel1.x, voxel1.y, voxel1.z); //voxel of the right of the normal direction, i.e. the blocking
                    if (voxel2 != null){ //if there exists a possible pair
                        System.out.println("Voxel1 is at "+voxel1.x+", "+voxel1.y+", "+voxel1.z+". Voxel2 is at "+voxel2.x+", "+voxel2.y+", "+voxel2.z);
                        voxelPairs.add(new VoxelPair(voxel1,voxel2)); //add the pair to the list of pairs
                        unvisitedAdjacentVoxels.remove(voxel1); //remove voxel1 from the set of unvisited voxels
                        bfsi++;
                        breadthFirstTraversal2(voxel1, normalDir); //recurse from the up neighbour
                    } 
                }if (downNeighbour != null){ //if there is a down neighbour
                    Voxel voxel1 = downNeighbour; //voxel of the left of the normal direction, i.e. the blockee
                    Voxel voxel2 = getBackward(voxel1.x, voxel1.y, voxel1.z); //voxel of the right of the normal direction, i.e. the blocking
                    if (voxel2 != null){ //if there exists a possible pair
                        System.out.println("Voxel1 is at "+voxel1.x+", "+voxel1.y+", "+voxel1.z+". Voxel2 is at "+voxel2.x+", "+voxel2.y+", "+voxel2.z);
                        voxelPairs.add(new VoxelPair(voxel1,voxel2)); //add the pair to the list of pairs
                        unvisitedAdjacentVoxels.remove(voxel1); //remove voxel1 from the set of unvisited voxels
                        bfsi++;
                        breadthFirstTraversal2(voxel1, normalDir); //recurse from the down neighbour
                    } 
                }if (forwardNeighbour != null){ //if there is a forward neighbour
                    Voxel voxel1 = forwardNeighbour; //voxel of the left of the normal direction, i.e. the blockee
                    Voxel voxel2 = getBackward(voxel1.x, voxel1.y, voxel1.z); //voxel of the right of the normal direction, i.e. the blocking
                    if (voxel2 != null){ //if there exists a possible pair
                        System.out.println("Voxel1 is at "+voxel1.x+", "+voxel1.y+", "+voxel1.z+". Voxel2 is at "+voxel2.x+", "+voxel2.y+", "+voxel2.z);
                        voxelPairs.add(new VoxelPair(voxel1,voxel2)); //add the pair to the list of pairs
                        unvisitedAdjacentVoxels.remove(voxel1); //remove voxel1 from the set of unvisited voxels
                        bfsi++;
                        breadthFirstTraversal2(voxel1, normalDir); //recurse from the forward neighbour
                    } 
                }if (backwardNeighbour != null){ //if there is a backward neighbour
                    Voxel voxel1 = backwardNeighbour; //voxel of the left of the normal direction, i.e. the blockee
                    Voxel voxel2 = getBackward(voxel1.x, voxel1.y, voxel1.z); //voxel of the right of the normal direction, i.e. the blocking
                    if (voxel2 != null){ //if there exists a possible pair
                        System.out.println("Voxel1 is at "+voxel1.x+", "+voxel1.y+", "+voxel1.z+". Voxel2 is at "+voxel2.x+", "+voxel2.y+", "+voxel2.z);
                        voxelPairs.add(new VoxelPair(voxel1,voxel2)); //add the pair to the list of pairs
                        unvisitedAdjacentVoxels.remove(voxel1); //remove voxel1 from the set of unvisited voxels
                        bfsi++;
                        breadthFirstTraversal2(voxel1, normalDir); //recurse from the backward neighbour
                    } 
                }
            }
            else{
                System.out.println("ERROR! The seed voxel's normal direction seems to have not been set in either direction");
            }
        }else{
            System.out.println(bfsi2+") We have now visited all voxels! STOPPING...");
            bfsi2++;
        }
    }
    
    
}
