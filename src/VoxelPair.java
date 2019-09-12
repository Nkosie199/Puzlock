/**
 * @author Nkosi Gumede
 */
public class VoxelPair{
    private final Voxel voxel1; //blocking voxel
    private final Voxel voxel2; //blockee voxel

    public VoxelPair(Voxel v1, Voxel v2){
        voxel1 = v1;
        voxel2 = v2;
    }

    public Voxel getBlockingVoxel(){ return voxel1; }
    public Voxel getBlockeeVoxel(){ return voxel2; }
}
