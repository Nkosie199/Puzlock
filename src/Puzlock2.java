
import java.util.ArrayList;

/**
 * @author nkosi gumede
 * this class implements 'Section 5.2: Extracting other Puzzle Pieces' of Song et al. (2012)
 */
public class Puzlock2 {
    static Puzlock puzlock = new Puzlock(); //we will need some Puzlock methods to get the current neighbours etc
    //start with an example of a valid key piece (red piece as per pg. 6 of Song et al. 2012)...
    static int[][][] keyPiece = {
        {{0, 0, 0, 0},{0, 0, 0, 0},{0, 0, 0, 0},{0, 0, 0, 0}}, 
        {{0, 0, 0, 0},{0, 0, 0, 0},{0, 0, 0, 0},{0, 0, 0, 0}}, 
        {{0, 0, 1, 0},{0, 0, 0, 0},{0, 0, 0, 0},{0, 0, 0, 0}}, 
        {{1, 1, 1, 1},{0, 1, 1, 1},{0, 0, 0, 0},{0, 0, 0, 0}}
    };
    static ArrayList<Voxel> keyPieceVoxels = puzlock.initializeVoxelArray(keyPiece); //stores the key piece as an arraylist of voxels
    static int keyPieceSize; //the size of the array on the z a-axis
    static ArrayList<String> removableDirections = new ArrayList<>(); //stores the directions in which the key piece is removable
    
    public static void main(String[] args){
        //2. Extracting other puzzle pieces...
        keyPieceSize = keyPiece[0].length;
        //2.0. Check movable direction(s)
        checkRemovableDirections(keyPieceVoxels, keyPiece, keyPieceSize, removableDirections);
        //2.1. Candidate seed voxels
        candidateSeedVoxels();
        //2.2. Create an initial Pi+1
        createInitialPafter();
        //2.3. Ensure local interlocking
        ensureLocalInterlocking();
        //2.4. Expand Pi+1 and Confirm it
        expandPafterandConfirm();
    }
    
    //2. Extracting other puzzle pieces
    /* The procedure of extracting subsequent puzzle pieces, e.g., Pi+1 from Ri, also starts by picking a seed voxel, and then growing Pi+1 from it. 
    However, since there are additional requirements for local interlocking among Pi, Pi+1, and Ri+1, the blocking mechanics are more involved.  
    To facilitate our discussion, we denote d>i as the target moving direction of Pi. */
    //2.1. Candidate seed voxels
    static void candidateSeedVoxels(){
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
    static void createInitialPafter(){
        /* • After step 1, we have a set of candidate seeds, each associated with a d> i+1. Our next step is to pick one of them by examining its cost of making Pi+1 removable in d>i+1: 
        (i) from each candidate, we identify all voxels in Ri along d>i+1 (see the orange voxels in Figure 11(d)) 
        since these voxels must be taken to Pi+1 to make the candidate removable along d>i+1; 
        (ii) we determine a shortest path to connect the candidate to these identified voxels (Figure 11(e)), and 
        (iii) we locate also any additional voxel required to mobilize the shortest path towards d>i+1 (Figure 11(f)). 
        To choose among the candidates, we sum the accessibility of all the voxels involved in each candidate path (blue voxels in Figure 11(f)), 
        and pick the one with the smallest sum for forming the initial Pi+1. */
    }

    //2.3. Ensure local interlocking
    static void ensureLocalInterlocking(){
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
    static void expandPafterandConfirm(){
        /* • After the above steps, Pi+1 can fulfill the local interlocking requirement, but yet we have to expand it to m voxels and check whether Ri+1 is simply connected or not. 
        These are done in the same way as in Section 5.1. */
    }
    
    /* takes in a keyPiece arrayList of voxels and its 3D array, size and removable directions; checks which directions it is removable in*/
    static void checkRemovableDirections(ArrayList<Voxel> keyPiece, int[][][] vMesh, int vMeshSize, ArrayList<String> removableDirections){
        //1) gather all voxels next to the keyPiece in a set (Rs)...
        ArrayList<Voxel> setOfNeighbours = new ArrayList<>(); //stores all voxels next to the key piece
        for (Voxel v: keyPiece){ //for each voxel, get its neighbours
            Voxel leftNeighbour = puzlock.getLeft(v.x, v.y, v.z, vMesh, vMeshSize, keyPiece); 
            Voxel rightNeighbour = puzlock.getRight(v.x, v.y, v.z, vMesh, vMeshSize, keyPiece); 
            Voxel upNeighbour = puzlock.getUp(v.x, v.y, v.z, vMesh, vMeshSize, keyPiece);
            Voxel downNeighbour = puzlock.getDown(v.x, v.y, v.z, vMesh, vMeshSize, keyPiece);
            Voxel forwardNeighbour = puzlock.getForward(v.x, v.y, v.z, vMesh, vMeshSize, keyPiece);
            Voxel backwardNeighbour = puzlock.getBackward(v.x, v.y, v.z, vMesh, vMeshSize, keyPiece);
            if (leftNeighbour != null){ //if there is a left neighbour
                if((!keyPiece.contains(leftNeighbour)) && (!setOfNeighbours.contains(leftNeighbour))){ //if neighbour is not already in keyPiece nor the set of neighbours
                    setOfNeighbours.add(leftNeighbour); //add it to the set of neighbours
                }
            }if (rightNeighbour != null){ //if there is a right neighbour
                if((!keyPiece.contains(rightNeighbour)) && (!setOfNeighbours.contains(rightNeighbour))){ //if neighbour is not already in keyPiece nor the set of neighbours
                    setOfNeighbours.add(rightNeighbour); //add it to the set of neighbours
                }
            }if (upNeighbour != null){ //if there is an up neighbour
                if((!keyPiece.contains(upNeighbour)) && (!setOfNeighbours.contains(upNeighbour))){ //if neighbour is not already in keyPiece nor the set of neighbours
                    setOfNeighbours.add(upNeighbour); //add it to the set of neighbours
                }
            }if (downNeighbour != null){ //if there is a down neighbour
                if((!keyPiece.contains(downNeighbour)) && (!setOfNeighbours.contains(downNeighbour))){ //if neighbour is not already in keyPiece nor the set of neighbours
                    setOfNeighbours.add(downNeighbour); //add it to the set of neighbours
                }
            }if (forwardNeighbour != null){ //if there is a forward neighbour
                if((!keyPiece.contains(forwardNeighbour)) && (!setOfNeighbours.contains(forwardNeighbour))){ //if neighbour is not already in keyPiece nor the set of neighbours
                    setOfNeighbours.add(forwardNeighbour); //add it to the set of neighbours
                }
            }if (backwardNeighbour != null){ //if there is a backward neighbour
                if((!keyPiece.contains(backwardNeighbour)) && (!setOfNeighbours.contains(backwardNeighbour))){ //if neighbour is not already in keyPiece nor the set of neighbours
                    setOfNeighbours.add(backwardNeighbour); //add it to the set of neighbours
                }
            }
        }
    }

}
