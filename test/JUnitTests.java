import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author adam2
 */
public class JUnitTests {
    Puzlock puzlock = new Puzlock();
    IO io = new IO();
    
    public JUnitTests() {
    }
    
    @BeforeAll
    public static void setUpClass() {
    }
    
    @AfterAll
    public static void tearDownClass() {
    }
    
    @BeforeEach
    public void setUp() throws Exception {
    }
    
    @AfterEach
    public void tearDown() {
    }
    
    /* unit testing for every Puzlock method which generates results comparable with expectations... 
    List of Puzlock methods which require unit testing:
        get<Directions> (for each direction)
        countNeighbours
        setAccessibilityValues
        indexOfCoordinate
    */
    @Test
    public void getDirections() {
        System.out.println("getting directions...");
    }
    
    @Test
    public void from1Dto3D() {
        System.out.println("from 1d to 3d...");
    }
}
