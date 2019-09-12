/**
 * @author Nkosi Gumede
 */
public class Voxel {
    public String normalDirection;
    public double accessibilityValue;
    public int x, y, z; //voxel co-ordinates...
    int shortestDistanceFromSource; //used in shortest path algorithm
    Voxel previousVertex; //used in shortest path algorithm
    double pi2; //used in section 5.1.4 point 3
    int value; //if value is 0, no voxel. Otherwise value > 0 is a specific voxel
    public String removableDirection; //used in section 5.2.1 - the direction for blocking Piece(i+1)
    public int remainingVolumeDistance = 0; //used in section 5.2.1 to store the distance to the furthest away voxel in the remaining volume along the removable direction
    public int shortlistRank; //used in section 5.2.1 to get the 10 best candidates according to the equally weighted criteria
    
    public Voxel(int x, int y, int z, int value){
        this.x = x;
        this.y = y;
        this.z = z;
        this.value = value;
    }
    
    public String getCoordinates(){
        return x+","+y+","+z;
    }
}
