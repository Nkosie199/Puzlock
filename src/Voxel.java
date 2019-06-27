import java.util.ArrayList;
import java.util.List;

/**
 * @author Nkosi Gumede
 */
public class Voxel {
    public List<Voxel> neighbours = new ArrayList<Voxel>();
    public String normalDirection;
    public int accessibilityValue;
    //voxel co-ordinates...
    public int x, y, z;
    
    public Voxel(int x, int y, int z){
        this.x = x;
        this.y = y;
        this.z = z;
    }
     
}
