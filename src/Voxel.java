/**
 * @author Nkosi Gumede
 */

public class Voxel {
    public String normalDirection;
    public double accessibilityValue;
    public int x, y, z; //voxel co-ordinates...
    int shortestDistanceFromSource; //used in shortest path algorithm
    Voxel previousVertex; //used in shortest path algorithm
    
    public Voxel(int x, int y, int z){
        this.x = x;
        this.y = y;
        this.z = z;
    }
    
    public String getCoordinates(){
        return x+","+y+","+z;
    }
     
}
