/**
 * @author Nkosi Gumede
 */

public class VoxelPair{
    private final Voxel voxel1;
    private final Voxel voxel2;

    public VoxelPair(Voxel v1, Voxel v2){
        voxel1 = v1;
        voxel2 = v2;
    }

    public Voxel getVoxel1(){ return voxel1; }
    public Voxel getVoxel2(){ return voxel2; }
}
