import java.util.ArrayList;

/**
 * @author Nkosi Gumede
 */

public class PuzzlePiece {
    ArrayList<Voxel> piece; //stores the set of voxels which are used to represent one piece
    ArrayList<Voxel> anchorVoxel; //stores the anchor voxels
    ArrayList<Voxel> anchorVoxel2; //stores the second set of anchor voxels
    Voxel blocking; //stores the blocking voxel
    Voxel blockee; //stores the blockee voxel
    
    public PuzzlePiece(ArrayList<Voxel> piece, ArrayList<Voxel> anchor1, ArrayList<Voxel> anchor2, Voxel blocking, Voxel blockee){
        this.piece = piece;
        this.anchorVoxel = anchor1;
        this.anchorVoxel2 = anchor2;
        this.blocking = blocking;
        this.blockee = blockee;
    }
}
