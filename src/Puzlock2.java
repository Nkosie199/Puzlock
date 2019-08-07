import java.util.ArrayList;
import java.util.Collections;

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
        {{0, 0, 1, 0},{0, 0, 0, 1},{0, 0, 0, 0},{0, 0, 0, 0}}, 
        {{1, 1, 1, 1},{0, 1, 0, 1},{0, 0, 0, 0},{0, 0, 0, 0}}
    };
    static int[][][] remainingPiece = {
        {{1, 1, 1, 1},{1, 1, 1, 1},{1, 1, 1, 1},{1, 1, 1, 1}}, 
        {{1, 1, 1, 1},{1, 1, 1, 1},{1, 1, 1, 1},{1, 1, 1, 1}}, 
        {{1, 1, 0, 1},{1, 1, 1, 0},{1, 1, 1, 1},{1, 1, 1, 1}}, 
        {{0, 0, 0, 0},{1, 0, 1, 0},{1, 1, 1, 1},{1, 1, 1, 1}}
    };
    static ArrayList<Voxel> keyPieceVoxels = puzlock.initializeVoxelArray(keyPiece); //stores the key piece as an arraylist of voxels
    static ArrayList<Voxel> remainingPieceVoxels = puzlock.initializeVoxelArray(remainingPiece); //stores the key piece as an arraylist of voxels
    static int keyPieceSize; //the size of the array on the z a-axis
    static ArrayList<String> removableDirections = new ArrayList<>(); //stores the directions in which the key piece is removable
    
    public static void main(String[] args){
        //2. Extracting other puzzle pieces...
        keyPieceSize = keyPiece[0].length; //set the size to be used
        System.out.println("Key piece dimensions: "+keyPieceSize);
        System.out.println("Key piece size: "+keyPieceVoxels.size());
        System.out.println("Mesh size: "+keyPiece.length);
        //2.0. Check movable direction(s)
        ArrayList<String> targetMovingDirection = checkRemovableDirections(keyPieceVoxels, keyPiece, keyPieceSize, removableDirections);        
        if (targetMovingDirection.size() == 1){ //if the keyPiece has only one moving direction, it can be confirmed to be correct...
            System.out.println("Target moving direction of key piece is "+targetMovingDirection.get(0));
        }else{
            System.out.println("Key piece has less or more than one target moving direction...");
            for (String s: targetMovingDirection) {
                System.out.println(s+" ");
            }
        }
        //2.1. Candidate seed voxels
        ArrayList<Voxel> candidateSeeds = candidateSeedVoxels(keyPieceVoxels, keyPiece, keyPieceSize, targetMovingDirection);
        System.out.println("Debug printing the set of candidate seeds...");
        puzlock.debugPrintVoxels(candidateSeeds);
        //now we have to shortlist the set of candidates to a maximum of 10...
        ArrayList<Voxel> shortlistedSeeds = shortlistCandidates(remainingPiece, keyPieceSize, remainingPieceVoxels, candidateSeeds, removableDirections);
        //2.2. Create an initial Pi+1
        //createInitialPafter(ArrayList<Voxel> shortlist, int[][][] remainingMesh, int remainingMeshSize, ArrayList<Voxel> remainingVoxels, String removableDirection)
        createInitialPafter(shortlistedSeeds, remainingPiece, keyPieceSize, remainingPieceVoxels, removableDirections.get(0));
        //2.3. Ensure local interlocking
        ensureLocalInterlocking();
        //2.4. Expand Pi+1 and Confirm it
        expandPafterandConfirm();
    }
    
    //2. Extracting other puzzle pieces
    /* The procedure of extracting subsequent puzzle pieces, e.g., Pi+1 from Ri, also starts by picking a seed voxel, and then growing Pi+1 from it. 
    However, since there are additional requirements for local interlocking among Pi, Pi+1, and Ri+1, the blocking mechanics are more involved.  
    To facilitate our discussion, we denote d>i as the target moving direction of Pi. */
    /* takes in a keyPiece arrayList of voxels and its 3D array, size and removable directions; checks which directions it is removable in*/
    static ArrayList<String> checkRemovableDirections(ArrayList<Voxel> keyPiece, int[][][] vMesh, int vMeshSize, ArrayList<String> removableDirections){
        ArrayList<String> movingDirections = new ArrayList<>();
        //check left...
        for (Voxel v: keyPiece){ //for each voxel in the piece...
            if (v.value==1){ //if the voxel is set
                //every left neighbour must either be 1 or null
                Voxel leftNeighbour = puzlock.getLeft(v.x, v.y, v.z, vMesh, vMeshSize, keyPiece);
                if (leftNeighbour==null){System.out.println("Left neighbour: "+leftNeighbour);}else{System.out.println("Left neighbour: "+leftNeighbour.value); }
                if (leftNeighbour==null || leftNeighbour.value==1){
                    if (!movingDirections.contains("left")){ //if left is not already a moving direction...
                        movingDirections.add("left"); //add it
                    }
                }else{ //if leftNeighbour.value==0, it is not removable in this direction
                    movingDirections.remove("left"); //remove it
                    break;
                }
            }
        }//check right...
        for (Voxel v: keyPiece){ //for each voxel in the piece...
            if (v.value==1){ //if the voxel is set
                //every right neighbour must either be 1 or null
                Voxel rightNeighbour = puzlock.getRight(v.x, v.y, v.z, vMesh, vMeshSize, keyPiece);
                if (rightNeighbour==null){ System.out.println("Right neighbour: "+rightNeighbour);}else{System.out.println("Right neighbour: "+rightNeighbour.value);}
                if (rightNeighbour==null || rightNeighbour.value==1){
                    if (!movingDirections.contains("right")){ //if right is not already a moving direction...
                        movingDirections.add("right"); //add it
                    }
                }else{ //if rightNeighbour.value==0, it is not removable in this direction
                    movingDirections.remove("right"); //remove it
                    break;
                }
            }
        }//check up...
        for (Voxel v: keyPiece){ //for each voxel in the piece...
            if (v.value==1){ //if the voxel is set
                //every up neighbour must either be 1 or null
                Voxel upNeighbour = puzlock.getUp(v.x, v.y, v.z, vMesh, vMeshSize, keyPiece);
                if (upNeighbour==null){System.out.println("Up neighbour: "+upNeighbour);}else{System.out.println("Up neighbour: "+upNeighbour.value);}
                if (upNeighbour==null || upNeighbour.value==1){
                    if (!movingDirections.contains("up")){ //if up is not already a moving direction...
                        movingDirections.add("up"); //add it
                    }
                }else{ //if upNeighbour.value==0, it is not removable in this direction
                    movingDirections.remove("up"); //remove it
                    break;
                }
            }
        }//check down...
        for (Voxel v: keyPiece){ //for each voxel in the piece...
            if (v.value==1){ //if the voxel is set
                //every down neighbour must either be 1 or null
                Voxel downNeighbour = puzlock.getDown(v.x, v.y, v.z, vMesh, vMeshSize, keyPiece);
                if (downNeighbour==null){System.out.println("Down neighbour: "+downNeighbour);}else{System.out.println("Down neighbour: "+downNeighbour.value); }
                if (downNeighbour==null || downNeighbour.value==1){
                    if (!movingDirections.contains("down")){ //if down is not already a moving direction...
                        movingDirections.add("down"); //add it
                    }
                }else{ //if downNeighbour.value==0, it is not removable in this direction
                    movingDirections.remove("down"); //remove it
                    break;
                }
            }
        }//check forward...
        for (Voxel v: keyPiece){ //for each voxel in the piece...
            if (v.value==1){ //if the voxel is set
                //every forward neighbour must either be 1 or null
                Voxel forwardNeighbour = puzlock.getForward(v.x, v.y, v.z, vMesh, vMeshSize, keyPiece);
                if (forwardNeighbour==null){System.out.println("Forward neighbour: "+forwardNeighbour); }else{System.out.println("Forward neighbour: "+forwardNeighbour.value);}
                if (forwardNeighbour==null || forwardNeighbour.value==1){
                    if (!movingDirections.contains("forward")){ //if forward is not already a moving direction...
                        movingDirections.add("forward"); //add it
                    }
                }else{ //if forwardNeighbour.value==0, it is not removable in this direction
                    movingDirections.remove("forward"); //remove it
                    break;
                }
            }
        }//check backward...
        for (Voxel v: keyPiece){ //for each voxel in the piece...
            if (v.value==1){ //if the voxel is set
                //every backward neighbour must either be 1 or null
                Voxel backwardNeighbour = puzlock.getBackward(v.x, v.y, v.z, vMesh, vMeshSize, keyPiece);
                if (backwardNeighbour==null){System.out.println("Backward neighbour: "+backwardNeighbour); }else{System.out.println("Backward neighbour: "+backwardNeighbour.value);}
                if (backwardNeighbour==null || backwardNeighbour.value==1){
                    if (!movingDirections.contains("backward")){ //if backward is not already a moving direction...
                        movingDirections.add("backward"); //add it
                    }
                }else{ //if backwardNeighbour.value==0, it is not removable in this direction
                    movingDirections.remove("backward"); //remove it
                    break;
                }
            }
        }
        return movingDirections;
    }

    //2.1. Candidate seed voxels
    static ArrayList<Voxel> candidateSeedVoxels(ArrayList<Voxel> keyPiece, int[][][] vMesh, int vMeshSize, ArrayList<String> removableDirections){
        /* • Since Pi+1 is blocked by Pi, but becomes mobilized as soon as Pi is removed, at least one of its voxel must reside next to Pi. 
        Since successive puzzle pieces should move in different directions (by Lemma 3 in Appendix), we use the contact between Pi and Pi+1 to define d>i+1 for blocking Pi+1 
        by the presence of Pi. Our strategy is to pick voxels (in Ri) next to Pi as candidate seeds, requiring them to contact P i in a direction perpendicular to d>i. 
        See Figure 11(b) for valid and invalid candidates in blue and violet, respectively. */
        //1) gather all voxels next to the keyPiece in a set (Rs)...
        ArrayList<Voxel> setOfNeighbours = new ArrayList<>(); //stores all voxels next to the key piece
        String removableDirection = removableDirections.get(0); //we must ensure that the neighbour is not in the opposite direction of the removable direction
        for (Voxel v: keyPiece){ //for each voxel, get its neighbours
            if (v.value==1){ //if the voxel is set
                Voxel leftNeighbour = puzlock.getLeft(v.x, v.y, v.z, vMesh, vMeshSize, keyPiece); 
                Voxel rightNeighbour = puzlock.getRight(v.x, v.y, v.z, vMesh, vMeshSize, keyPiece); 
                Voxel upNeighbour = puzlock.getUp(v.x, v.y, v.z, vMesh, vMeshSize, keyPiece);
                Voxel downNeighbour = puzlock.getDown(v.x, v.y, v.z, vMesh, vMeshSize, keyPiece);
                Voxel forwardNeighbour = puzlock.getForward(v.x, v.y, v.z, vMesh, vMeshSize, keyPiece);
                Voxel backwardNeighbour = puzlock.getBackward(v.x, v.y, v.z, vMesh, vMeshSize, keyPiece);
                if (removableDirection.equals("left")){ //do not check for right neighbours
                    if (upNeighbour!=null && upNeighbour.value == 0){ //if there is an up neighbour
                        if(!setOfNeighbours.contains(upNeighbour)){ //if neighbour is not already in keyPiece nor the set of neighbours
                            upNeighbour.removableDirection = "down"; //the removable direction is opposite to where the neighbour is
                            setOfNeighbours.add(upNeighbour); //add it to the set of neighbours
                        }
                    }if (downNeighbour!=null && downNeighbour.value == 0){ //if there is a down neighbour
                        if(!setOfNeighbours.contains(downNeighbour)){ //if neighbour is not already in keyPiece nor the set of neighbours
                            downNeighbour.removableDirection = "up"; //the removable direction is opposite to where the neighbour is
                            setOfNeighbours.add(downNeighbour); //add it to the set of neighbours
                        }
                    }if (forwardNeighbour!=null && forwardNeighbour.value == 0){ //if there is a forward neighbour
                        if(!setOfNeighbours.contains(forwardNeighbour)){ //if neighbour is not already in keyPiece nor the set of neighbours
                            forwardNeighbour.removableDirection = "backward"; //the removable direction is opposite to where the neighbour is
                            setOfNeighbours.add(forwardNeighbour); //add it to the set of neighbours
                        }
                    }if (backwardNeighbour!=null && backwardNeighbour.value == 0){ //if there is a backward neighbour
                        if(!setOfNeighbours.contains(backwardNeighbour)){ //if neighbour is not already in keyPiece nor the set of neighbours
                            backwardNeighbour.removableDirection = "forward"; //the removable direction is opposite to where the neighbour is
                            setOfNeighbours.add(backwardNeighbour); //add it to the set of neighbours
                        }
                    }
                }else if (removableDirection.equals("right")){ //do not check for left neighbours
                    if (upNeighbour!=null && upNeighbour.value == 0){ //if there is an up neighbour
                        if(!setOfNeighbours.contains(upNeighbour)){ //if neighbour is not already in keyPiece nor the set of neighbours
                            upNeighbour.removableDirection = "down"; //the removable direction is opposite to where the neighbour is
                            setOfNeighbours.add(upNeighbour); //add it to the set of neighbours
                        }
                    }if (downNeighbour!=null && downNeighbour.value == 0){ //if there is a down neighbour
                        if(!setOfNeighbours.contains(downNeighbour)){ //if neighbour is not already in keyPiece nor the set of neighbours
                            downNeighbour.removableDirection = "up"; //the removable direction is opposite to where the neighbour is
                            setOfNeighbours.add(downNeighbour); //add it to the set of neighbours
                        }
                    }if (forwardNeighbour!=null && forwardNeighbour.value == 0){ //if there is a forward neighbour
                        if(!setOfNeighbours.contains(forwardNeighbour)){ //if neighbour is not already in keyPiece nor the set of neighbours
                            forwardNeighbour.removableDirection = "backward"; //the removable direction is opposite to where the neighbour is
                            setOfNeighbours.add(forwardNeighbour); //add it to the set of neighbours
                        }
                    }if (backwardNeighbour!=null && backwardNeighbour.value == 0){ //if there is a backward neighbour
                        if(!setOfNeighbours.contains(backwardNeighbour)){ //if neighbour is not already in keyPiece nor the set of neighbours
                            backwardNeighbour.removableDirection = "forward"; //the removable direction is opposite to where the neighbour is
                            setOfNeighbours.add(backwardNeighbour); //add it to the set of neighbours
                        }
                    }
                }else if (removableDirection.equals("up")){ //do not check for down neighbours
                    if (leftNeighbour!=null && leftNeighbour.value == 0){ //if there is a left neighbour
                        if(!setOfNeighbours.contains(leftNeighbour)){ //if neighbour is not already in keyPiece nor the set of neighbours
                            leftNeighbour.removableDirection = "right"; //the removable direction is opposite to where the neighbour is
                            setOfNeighbours.add(leftNeighbour); //add it to the set of neighbours
                        }
                    }if (rightNeighbour!=null && rightNeighbour.value == 0){ //if there is a right neighbour
                        if(!setOfNeighbours.contains(rightNeighbour)){ //if neighbour is not already in keyPiece nor the set of neighbours
                            rightNeighbour.removableDirection = "left"; //the removable direction is opposite to where the neighbour is
                            setOfNeighbours.add(rightNeighbour); //add it to the set of neighbours
                        }
                    }if (forwardNeighbour!=null && forwardNeighbour.value == 0){ //if there is a forward neighbour
                        if(!setOfNeighbours.contains(forwardNeighbour)){ //if neighbour is not already in keyPiece nor the set of neighbours
                            forwardNeighbour.removableDirection = "backward"; //the removable direction is opposite to where the neighbour is
                            setOfNeighbours.add(forwardNeighbour); //add it to the set of neighbours
                        }
                    }if (backwardNeighbour!=null && backwardNeighbour.value == 0){ //if there is a backward neighbour
                        if(!setOfNeighbours.contains(backwardNeighbour)){ //if neighbour is not already in keyPiece nor the set of neighbours
                            backwardNeighbour.removableDirection = "forward"; //the removable direction is opposite to where the neighbour is
                            setOfNeighbours.add(backwardNeighbour); //add it to the set of neighbours
                        }
                    }
                }else if (removableDirection.equals("down")){ //do not check for up neighbours
                    if (leftNeighbour!=null && leftNeighbour.value == 0){ //if there is a left neighbour
                        if(!setOfNeighbours.contains(leftNeighbour)){ //if neighbour is not already in keyPiece nor the set of neighbours
                            leftNeighbour.removableDirection = "right"; //the removable direction is opposite to where the neighbour is
                            setOfNeighbours.add(leftNeighbour); //add it to the set of neighbours
                        }
                    }if (rightNeighbour!=null && rightNeighbour.value == 0){ //if there is a right neighbour
                        if(!setOfNeighbours.contains(rightNeighbour)){ //if neighbour is not already in keyPiece nor the set of neighbours
                            rightNeighbour.removableDirection = "left"; //the removable direction is opposite to where the neighbour is
                            setOfNeighbours.add(rightNeighbour); //add it to the set of neighbours
                        }
                    }if (forwardNeighbour!=null && forwardNeighbour.value == 0){ //if there is a forward neighbour
                        if(!setOfNeighbours.contains(forwardNeighbour)){ //if neighbour is not already in keyPiece nor the set of neighbours
                            forwardNeighbour.removableDirection = "backward"; //the removable direction is opposite to where the neighbour is
                            setOfNeighbours.add(forwardNeighbour); //add it to the set of neighbours
                        }
                    }if (backwardNeighbour!=null && backwardNeighbour.value == 0){ //if there is a backward neighbour
                        if(!setOfNeighbours.contains(backwardNeighbour)){ //if neighbour is not already in keyPiece nor the set of neighbours
                            backwardNeighbour.removableDirection = "forward"; //the removable direction is opposite to where the neighbour is
                            setOfNeighbours.add(backwardNeighbour); //add it to the set of neighbours
                        }
                    }
                }else if (removableDirection.equals("forward")){ //do not check for backward neighbours
                    if (leftNeighbour!=null && leftNeighbour.value == 0){ //if there is a left neighbour
                        if(!setOfNeighbours.contains(leftNeighbour)){ //if neighbour is not already in keyPiece nor the set of neighbours
                            leftNeighbour.removableDirection = "right"; //the removable direction is opposite to where the neighbour is
                            setOfNeighbours.add(leftNeighbour); //add it to the set of neighbours
                        }
                    }if (rightNeighbour!=null && rightNeighbour.value == 0){ //if there is a right neighbour
                        if(!setOfNeighbours.contains(rightNeighbour)){ //if neighbour is not already in keyPiece nor the set of neighbours
                            rightNeighbour.removableDirection = "left"; //the removable direction is opposite to where the neighbour is
                            setOfNeighbours.add(rightNeighbour); //add it to the set of neighbours
                        }
                    }if (upNeighbour!=null && upNeighbour.value == 0){ //if there is an up neighbour
                        if(!setOfNeighbours.contains(upNeighbour)){ //if neighbour is not already in keyPiece nor the set of neighbours
                            upNeighbour.removableDirection = "down"; //the removable direction is opposite to where the neighbour is
                            setOfNeighbours.add(upNeighbour); //add it to the set of neighbours
                        }
                    }if (downNeighbour!=null && downNeighbour.value == 0){ //if there is a down neighbour
                        if(!setOfNeighbours.contains(downNeighbour)){ //if neighbour is not already in keyPiece nor the set of neighbours
                            downNeighbour.removableDirection = "up"; //the removable direction is opposite to where the neighbour is
                            setOfNeighbours.add(downNeighbour); //add it to the set of neighbours
                        }
                    }
                }else if (removableDirection.equals("backward")){ //do not check for forwardward neighbours
                    if (leftNeighbour!=null && leftNeighbour.value == 0){ //if there is a left neighbour
                        if(!setOfNeighbours.contains(leftNeighbour)){ //if neighbour is not already in keyPiece nor the set of neighbours
                            leftNeighbour.removableDirection = "right"; //the removable direction is opposite to where the neighbour is
                            setOfNeighbours.add(leftNeighbour); //add it to the set of neighbours
                        }
                    }if (rightNeighbour!=null && rightNeighbour.value == 0){ //if there is a right neighbour
                        if(!setOfNeighbours.contains(rightNeighbour)){ //if neighbour is not already in keyPiece nor the set of neighbours
                            rightNeighbour.removableDirection = "left"; //the removable direction is opposite to where the neighbour is
                            setOfNeighbours.add(rightNeighbour); //add it to the set of neighbours
                        }
                    }if (upNeighbour!=null && upNeighbour.value == 0){ //if there is an up neighbour
                        if(!setOfNeighbours.contains(upNeighbour)){ //if neighbour is not already in keyPiece nor the set of neighbours
                            upNeighbour.removableDirection = "down"; //the removable direction is opposite to where the neighbour is
                            setOfNeighbours.add(upNeighbour); //add it to the set of neighbours
                        }
                    }if (downNeighbour!=null && downNeighbour.value == 0){ //if there is a down neighbour
                        if(!setOfNeighbours.contains(downNeighbour)){ //if neighbour is not already in keyPiece nor the set of neighbours
                            downNeighbour.removableDirection = "up"; //the removable direction is opposite to where the neighbour is
                            setOfNeighbours.add(downNeighbour); //add it to the set of neighbours
                        }
                    }
                }
            }
        }
        return setOfNeighbours;
        /* • Since there may be too many valid candidates, trying them all is overly time consuming. 
        Hence, we compute the accessibility of voxels in Ri and reduce the number of candidates to ten by the following equally-weighted criteria: 
        (i) smaller accessibility value; and 
        (ii) shorter distance to the furthest-away voxel in Ri along d>i+1, see Figure 11(c) 
        for examples: an initial Pi+1 formed by C2 will contain more voxels as compared to C1 because of a longer shortest path determined by step 2 below. 
        Hence, the second criteria helps reduce the number of voxels that are required to form an initial Pi+1. 
        Note that we attempt to use fewer voxels (in early steps) to construct an initial Pi+1 because this allows us to have more flexibility when expanding the puzzle piece in step 3. */
        //done in shortlistCandidates method...
    }
    
    static ArrayList<Voxel> shortlistCandidates(int[][][] remainingMesh, int remainingMeshSize, ArrayList<Voxel> remainingVoxels, ArrayList<Voxel> candidateSeedVoxels, ArrayList<String> removableDirections){
        System.out.println("Shortlisting candidates...");
        ArrayList<Voxel> shortlist = new ArrayList<>();
        if (candidateSeedVoxels.size() > 10){ //if the set of candidates is greater than 10
            //i)order candidates in terms of accessibility value: smallest to largest...
            remainingVoxels = puzlock.computeVoxelAccessibility(remainingMesh, remainingMeshSize, remainingVoxels); //the candidate seeds are in the remaining volume
            ArrayList<Double> accVals = new ArrayList(); //stores the accessibility values
            for (Voxel v: remainingVoxels) {
                for (Voxel v2: candidateSeedVoxels) {
                    if (v.getCoordinates().equals(v2.getCoordinates())){ //if the voxels correspond
                        System.out.println("adding accessibility value "+v.accessibilityValue+" at "+v.x+","+v.y+","+v.z);
                        accVals.add(v.accessibilityValue); //add its accessibility value
                        v2.accessibilityValue = v.accessibilityValue; //make sure the accessibility values of the candidate set of voxels are set
                    }
                }
            }
            Collections.sort(accVals); //sorts the stored accessibility values from smallest to largest
            System.out.println("Debug printing the accessibility values...");
            for (Double d: accVals) {
                System.out.print(d+" ");
            }System.out.println("");
            //ii)order ordered set of candidates in terms of shortest distance to the furthest-away voxel in the remaining volume...
            ArrayList<Integer> distances = new ArrayList<>(); //store the voxel distances (from the remaining volume) furthest away from the current voxel according to its blocking direction...
            for (Voxel v: candidateSeedVoxels){ //for each voxel in the set of candidates
                //get its blocking direction
                String blockingDirection = v.removableDirection;
                if (blockingDirection.equals("left")){
                    //go left along the remaining volume, incrementing the distance to the furthest-away voxel...
                    Voxel leftNeighbour = puzlock.getLeft(v.x, v.y, v.z, remainingMesh, remainingMeshSize, remainingVoxels);
                    int currentDistance = 1; //the immediate neighbour has a distance of 1
                    while (leftNeighbour!=null){
                        if (leftNeighbour.value==1){ //if voxel is set
                            v.remainingVolumeDistance = currentDistance; //set the distance to the current distance
                        }
                        leftNeighbour = puzlock.getLeft(leftNeighbour.x, leftNeighbour.y, leftNeighbour.z, remainingMesh, remainingMeshSize, remainingVoxels); //update the left neighbour
                        currentDistance++; //increment the distance
                    }System.out.println("Voxel @ "+v.x+","+v.y+","+v.z+" has blocking direction "+blockingDirection+" and a blocking distance of "+v.remainingVolumeDistance);
                    distances.add(v.remainingVolumeDistance);
                }else if (blockingDirection.equals("right")){
                    //go right along the remaining volume, incrementing the distance to the furthest-away voxel...
                    Voxel rightNeighbour = puzlock.getRight(v.x, v.y, v.z, remainingMesh, remainingMeshSize, remainingVoxels);
                    int currentDistance = 1; //the immediate neighbour has a distance of 1
                    while (rightNeighbour!=null){
                        if (rightNeighbour.value==1){ //if voxel is set
                            v.remainingVolumeDistance = currentDistance; //set the distance to the current distance
                        }
                        rightNeighbour = puzlock.getRight(rightNeighbour.x, rightNeighbour.y, rightNeighbour.z, remainingMesh, remainingMeshSize, remainingVoxels); //update the right neighbour
                        currentDistance++; //increment the distance
                    }System.out.println("Voxel @ "+v.x+","+v.y+","+v.z+" has blocking direction "+blockingDirection+" and a blocking distance of "+v.remainingVolumeDistance);
                    distances.add(v.remainingVolumeDistance);
                }else if (blockingDirection.equals("up")){
                    //go up along the remaining volume, incrementing the distance to the furthest-away voxel...
                    Voxel upNeighbour = puzlock.getUp(v.x, v.y, v.z, remainingMesh, remainingMeshSize, remainingVoxels);
                    int currentDistance = 1; //the immediate neighbour has a distance of 1
                    while (upNeighbour!=null){
                        if (upNeighbour.value==1){ //if voxel is set
                            v.remainingVolumeDistance = currentDistance; //set the distance to the current distance
                        }
                        upNeighbour = puzlock.getUp(upNeighbour.x, upNeighbour.y, upNeighbour.z, remainingMesh, remainingMeshSize, remainingVoxels); //update the up neighbour
                        currentDistance++; //increment the distance
                    }System.out.println("Voxel @ "+v.x+","+v.y+","+v.z+" has blocking direction "+blockingDirection+" and a blocking distance of "+v.remainingVolumeDistance);
                    distances.add(v.remainingVolumeDistance);
                }else if (blockingDirection.equals("down")){
                    //go down along the remaining volume, incrementing the distance to the furthest-away voxel...
                    Voxel downNeighbour = puzlock.getDown(v.x, v.y, v.z, remainingMesh, remainingMeshSize, remainingVoxels);
                    int currentDistance = 1; //the immediate neighbour has a distance of 1
                    while (downNeighbour!=null){
                        if (downNeighbour.value==1){ //if voxel is set
                            v.remainingVolumeDistance = currentDistance; //set the distance to the current distance
                        }
                        downNeighbour = puzlock.getRight(downNeighbour.x, downNeighbour.y, downNeighbour.z, remainingMesh, remainingMeshSize, remainingVoxels); //update the down neighbour
                        currentDistance++; //increment the distance
                    }System.out.println("Voxel @ "+v.x+","+v.y+","+v.z+" has blocking direction "+blockingDirection+" and a blocking distance of "+v.remainingVolumeDistance);
                    distances.add(v.remainingVolumeDistance);
                }else if (blockingDirection.equals("forward")){
                    //go forward along the remaining volume, incrementing the distance to the furthest-away voxel...
                    Voxel forwardNeighbour = puzlock.getForward(v.x, v.y, v.z, remainingMesh, remainingMeshSize, remainingVoxels);
                    int currentDistance = 1; //the immediate neighbour has a distance of 1
                    while (forwardNeighbour!=null){
                        if (forwardNeighbour.value==1){ //if voxel is set
                            v.remainingVolumeDistance = currentDistance; //set the distance to the current distance
                        }
                        forwardNeighbour = puzlock.getRight(forwardNeighbour.x, forwardNeighbour.y, forwardNeighbour.z, remainingMesh, remainingMeshSize, remainingVoxels); //update the forward neighbour
                        currentDistance++; //increment the distance
                    }System.out.println("Voxel @ "+v.x+","+v.y+","+v.z+" has blocking direction "+blockingDirection+" and a blocking distance of "+v.remainingVolumeDistance);
                    distances.add(v.remainingVolumeDistance);
                }else if (blockingDirection.equals("backward")){
                    //go backward along the remaining volume, incrementing the distance to the furthest-away voxel...
                    Voxel backwardNeighbour = puzlock.getBackward(v.x, v.y, v.z, remainingMesh, remainingMeshSize, remainingVoxels);
                    int currentDistance = 1; //the immediate neighbour has a distance of 1
                    while (backwardNeighbour!=null){
                        if (backwardNeighbour.value==1){ //if voxel is set
                            v.remainingVolumeDistance = currentDistance; //set the distance to the current distance
                        }
                        backwardNeighbour = puzlock.getRight(backwardNeighbour.x, backwardNeighbour.y, backwardNeighbour.z, remainingMesh, remainingMeshSize, remainingVoxels); //update the backward neighbour
                        currentDistance++; //increment the distance
                    }System.out.println("Voxel @ "+v.x+","+v.y+","+v.z+" has blocking direction "+blockingDirection+" and a blocking distance of "+v.remainingVolumeDistance);
                    distances.add(v.remainingVolumeDistance);
                } 
                
            }
            //sort the candidates from closest to furthest away...
            Collections.sort(distances); //sorts the stored accessibility values from smallest to largest
            System.out.println("Debug printing the distances of voxels to furthest voxel in the remaining volume along the blocking direction...");
            for (Integer i: distances) {
                System.out.print(i+" ");
            }System.out.println("");
            //get the first voxel which appears in both lists iteratavely until we have 10...
            ArrayList<Integer> rankings = new ArrayList<>();
            //collect voxels according to their distance and accessibility value by ranking them accordingly...
            for (int j = 0; j < candidateSeedVoxels.size(); j++) { //for each candidate seed voxel...
                Voxel currentVoxel = candidateSeedVoxels.get(j);
                for (int k = 0; k < distances.size(); k++) {
                    System.out.println("Current voxel distance vs distance array: "+currentVoxel.remainingVolumeDistance+" vs. "+distances.get(k));
                    if (currentVoxel.remainingVolumeDistance == distances.get(k)){
                        currentVoxel.shortlistRank = k; //set the rank according to distance
                        break; //move onto the next candidate
                    }
                }
                for (int l = 0; l < accVals.size(); l++) {
                    System.out.println("Current voxel accessibility vs accessibility array: "+currentVoxel.accessibilityValue+" vs. "+accVals.get(l));
                    if (currentVoxel.accessibilityValue == accVals.get(l)){
                        currentVoxel.shortlistRank =+ l; //increment the rank according to current ranking
                        rankings.add(currentVoxel.shortlistRank); //store the ranking
                        break; //move onto the next candidate
                    }
                }
            }
            Collections.sort(rankings); //sort the list of rankings from smallest to largest
            System.out.println("Debug printing the rankings of voxels...");
            for (Integer i: rankings) {
                System.out.print(i+" ");
            }System.out.println("");
            for (int i = 0; i < rankings.size(); i++) {
                for (int j = 0; j < candidateSeedVoxels.size(); j++) {
                    Voxel cv = candidateSeedVoxels.get(j);
                    if ((cv.shortlistRank==rankings.get(i)) && (shortlist.size()<10)){ //if we match the lowest ranking to a voxel; and the shortlist consists of less than 10 voxels
                        shortlist.add(candidateSeedVoxels.remove(j)); //add to the shortlist, and remove from the candidate set
                        System.out.println("Added shortlisted voxel @ "+cv.getCoordinates()+" with ranking "+cv.shortlistRank);
                        break; //move onto the next ranking
                    }
                }
            }
        }else{
            shortlist = candidateSeedVoxels;
        }
        return shortlist;
    }

    //2.2. Create an initial Pi+1
    static void createInitialPafter(ArrayList<Voxel> shortlist, int[][][] remainingMesh, int remainingMeshSize, ArrayList<Voxel> remainingVoxels, String removableDirection){
        /* • After step 1, we have a set of candidate seeds, each associated with a d> i+1. 
        Our next step is to pick one of them by examining its cost of making Pi+1 removable in d>i+1: 
        (i) from each candidate, we identify all voxels in Ri along d>i+1 (see the orange voxels in Figure 11(d)) 
        since these voxels must be taken to Pi+1 to make the candidate removable along d>i+1;
        //for each voxel we will generate an associated cost, we will proceed voxel with the smallest voxel...*/
        for (Voxel v: shortlist) {
            
        }
        
        /*(ii) we determine a shortest path to connect the candidate to these identified voxels (Figure 11(e)), and 
        (iii) we locate also any additional voxel required to mobilize the shortest path towards d>i+1 (Figure 11(f)). 
        To choose among the candidates, we sum the accessibility of all the voxels involved in each candidate path (blue voxels in Figure 11(f)), 
        and pick the one with the smallest sum for forming the initial Pi+1. */
    }
    
    static int voxelsAlongDirection(Voxel currentVoxel, int[][][] remainingMesh, int remainingMeshSize, ArrayList<Voxel> remainingVoxels, String removableDirection){
        
        
        return -1;
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

}