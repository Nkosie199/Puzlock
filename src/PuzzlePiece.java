import java.util.ArrayList;

/**
 * @author Nkosi Gumede
 */

public class PuzzlePiece {
    ArrayList<Voxel> piece; //stores the set of voxels which are used to represent one piece
    Voxel anchorVoxel; //stores the anchor voxel
    Voxel anchorVoxel2; //stores the second anchor voxel
    Voxel blocking; //stores the blocking voxel
    Voxel blockee; //stores the blockee voxel
    
    public PuzzlePiece(ArrayList<Voxel> piece, Voxel anchor1, Voxel anchor2, Voxel blocking, Voxel blockee){
        this.piece = piece;
        this.anchorVoxel = anchor1;
        this.anchorVoxel2 = anchor2;
        this.blocking = blocking;
        this.blockee = blockee;
    }
}
