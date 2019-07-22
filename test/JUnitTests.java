import java.util.ArrayList;
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
    TestExamples examples = new TestExamples();
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
    
    @Test
    public void add() {
        int input1 = 5;
        int input2 = 4;
        int actualResult = examples.add(5, 4);
        int expectedResult = input1+input2;
        assertEquals(expectedResult, actualResult);
    }
    
    /* unit testing for every Puzlock method which generates results comparable with expectations...
    */
    @Test
    public void initializeVoxelArray() {
        //ArrayList<Voxel> initializeVoxelArray(int[][][] vMesh)
        int[][][] input1= {
            {{1, 1, 1, 1},{1, 1, 1, 1},{1, 1, 1, 1},{1, 1, 1, 1}}, 
            {{1, 1, 1, 1},{1, 1, 1, 1},{1, 1, 1, 1},{1, 1, 1, 1}}, 
            {{1, 1, 1, 1},{1, 1, 1, 1},{1, 1, 1, 1},{1, 1, 1, 1}}, 
            {{1, 1, 1, 1},{1, 1, 1, 1},{1, 1, 1, 1},{1, 1, 1, 1}}
        };
        ArrayList<Voxel> actualResult = puzlock.initializeVoxelArray(input1);
        ArrayList<Voxel> expectedResult = new ArrayList<>();
        for (int z = 0; z < 4; z++) {
            for (int y = 0; y < 4; y++) {
                for (int x = 0; x < 4; x++) {
                    expectedResult.add(new Voxel(x, y, z));
                }
            }
        }
        assertEquals(expectedResult, actualResult);
    }
    
    @Test
    public void expandKeyPiece(ArrayList piece) {
        //ArrayList<Voxel> expandKeyPiece(ArrayList piece)
        ArrayList input1; //yet to implement
        ArrayList<Voxel> actualResult = puzlock.expandKeyPiece(input1);
        //eg. of manually settign an ArrayList...
        //ArrayList<Integer> possibleValues2 = new ArrayList<Integer>(Arrays.asList(1,2,3,4,5,6,7,8,9));
        ArrayList<Voxel> expectedResult; //yet to implement
        assertEquals(expectedResult, actualResult);
    }
    
    @Test
    public void indexOfCoordinate() {
        //int indexOfCoordinate(int x, int y, int z, int sizeOfAxes)
        int input1 = 1;
        int input2 = 1;
        int input3 = 3;
        int sizeOfAxes = 4;
        int expectedResult = input1+input2;
        int actualResult = examples.add(5, 4);
        
        assertEquals(expectedResult, actualResult);
    }
    
    @Test
    public void initializeOutputArray() {
        //ArrayList<Voxel> initializeOutputArray(int [][][] vMesh)
        int input1 = 5;
        int input2 = 4;
        int actualResult = examples.add(5, 4);
        int expectedResult = input1+input2;
        assertEquals(expectedResult, actualResult);
    }
    
    @Test
    public void computeMeshSize() {
        //int computeMeshSize(int[][][] vMesh)
        int input1 = 5;
        int input2 = 4;
        int actualResult = examples.add(5, 4);
        int expectedResult = input1+input2;
        assertEquals(expectedResult, actualResult);
    }
    
    @Test
    public void setOutputPieces() {
        //int[][][] setOutputPieces(ArrayList<Voxel> piece)
        int input1 = 5;
        int input2 = 4;
        int actualResult = examples.add(5, 4);
        int expectedResult = input1+input2;
        assertEquals(expectedResult, actualResult);
    }
    
    @Test
    public void getLeft() {
        //Voxel getLeft(int x, int y, int z)
        int input1 = 5;
        int input2 = 4;
        int actualResult = examples.add(5, 4);
        int expectedResult = input1+input2;
        assertEquals(expectedResult, actualResult);
    }
    
    @Test
    public void getRight() {
        //Voxel getRight(int x, int y, int z)
        int input1 = 5;
        int input2 = 4;
        int actualResult = examples.add(5, 4);
        int expectedResult = input1+input2;
        assertEquals(expectedResult, actualResult);
    }
    
    @Test
    public void getUp() {
        //Voxel getUp(int x, int y, int z)
        int input1 = 5;
        int input2 = 4;
        int actualResult = examples.add(5, 4);
        int expectedResult = input1+input2;
        assertEquals(expectedResult, actualResult);
    }
    
    @Test
    public void getDown() {
        //Voxel getDown(int x, int y, int z)
        int input1 = 5;
        int input2 = 4;
        int actualResult = examples.add(5, 4);
        int expectedResult = input1+input2;
        assertEquals(expectedResult, actualResult);
    }
    
    @Test
    public void getForward() {
        //Voxel getForward(int x, int y, int z)
        int input1 = 5;
        int input2 = 4;
        int actualResult = examples.add(5, 4);
        int expectedResult = input1+input2;
        assertEquals(expectedResult, actualResult);
    }
    
    @Test
    public void getBackward() {
        //Voxel getBackward(int x, int y, int z)
        int input1 = 5;
        int input2 = 4;
        int actualResult = examples.add(5, 4);
        int expectedResult = input1+input2;
        assertEquals(expectedResult, actualResult);
    }
    
    @Test
    public void countNeighbours() {
        //int countNeighbours(Voxel v)
        int input1 = 5;
        int input2 = 4;
        int actualResult = examples.add(5, 4);
        int expectedResult = input1+input2;
        assertEquals(expectedResult, actualResult);
    }
    
    @Test
    public void sumOfNeighboursAccValues() {
        //double sumOfNeighboursAccValues(int x, int y, int z)
        int input1 = 5;
        int input2 = 4;
        int actualResult = examples.add(5, 4);
        int expectedResult = input1+input2;
        assertEquals(expectedResult, actualResult);
    }
    
    @Test
    public void addVoxels() {
        //ArrayList<Voxel> addVoxels(ArrayList<Voxel> addedVoxels)
        int input1 = 5;
        int input2 = 4;
        int actualResult = examples.add(5, 4);
        int expectedResult = input1+input2;
        assertEquals(expectedResult, actualResult);
    }
    
    @Test
    public void addVoxels2() {
        //ArrayList<Voxel> addVoxels2(ArrayList<Voxel> addedVoxels)
        int input1 = 5;
        int input2 = 4;
        int actualResult = examples.add(5, 4);
        int expectedResult = input1+input2;
        assertEquals(expectedResult, actualResult);
    }
    
    @Test
    public void floodFill() {
        //ArrayList<Voxel> floodFill(ArrayList<Voxel> keyPiece)
        int input1 = 5;
        int input2 = 4;
        int actualResult = examples.add(5, 4);
        int expectedResult = input1+input2;
        assertEquals(expectedResult, actualResult);
    }
    
    @Test
    public void floodFill2() {
        //ArrayList<Voxel> floodFill2(ArrayList<Voxel> unvisitedVoxels, Voxel firstVoxel)
        int input1 = 5;
        int input2 = 4;
        int actualResult = examples.add(5, 4);
        int expectedResult = input1+input2;
        assertEquals(expectedResult, actualResult);
    }
}
