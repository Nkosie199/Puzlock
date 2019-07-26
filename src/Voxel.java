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
