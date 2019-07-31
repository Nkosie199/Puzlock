import java.util.ArrayList;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author adam2
 */
public class PuzlockJUnitTests {
    TestExamples examples = new TestExamples();
    Puzlock puzlock = new Puzlock();
    
    @Test
    public void add() {
        int input1 = 5;
        int input2 = 4;
        int actualResult = examples.add(5, 4);
        int expectedResult = input1+input2;
        assertEquals(expectedResult, actualResult);
    }
    
    //unit testing for every Puzlock method which generates results comparable with expectations...
    
    @Test
    public void expandKeyPiece() {
        //ArrayList<Voxel> expandKeyPiece(ArrayList piece)
        int[][][] outputPiece = {
            {{1, 1, 1, 0},{0, 0, 0, 0},{0, 0, 0, 0},{0, 0, 0, 0}}, 
            {{1, 1, 1, 1},{0, 0, 0, 0},{0, 0, 0, 0},{0, 0, 0, 0}}, 
            {{0, 0, 0, 1},{0, 0, 0, 0},{0, 0, 0, 0},{0, 0, 0, 0}}, 
            {{0, 0, 0, 0},{0, 0, 0, 0},{0, 0, 0, 0},{0, 0, 0, 0}}
        }; //the inputPiece made removable by adding voxels on top of each voxel
        ArrayList<Voxel> expectedResult = new ArrayList<>();
        expectedResult = puzlock.initializeVoxelArray(outputPiece);
        //now lets setup the input piece to get the actual result...
        /* Example of removable piece to be expanded (normal direction is backward, seed is at 0,0,0):
        0) Voxel@232204a1 is at 3,0,1
        1) Voxel@61bbe9ba is at 2,0,1
        2) Voxel@42a57993 is at 2,0,0
        3) Voxel@3d4eac69 is at 1,0,0
        4) Voxel@55f96302 is at 0,0,0
        */
        int[][][] inputPiece = {
            {{1, 1, 1, 0},{0, 0, 0, 0},{0, 0, 0, 0},{0, 0, 0, 0}}, 
            {{0, 0, 1, 1},{0, 0, 0, 0},{0, 0, 0, 0},{0, 0, 0, 0}}, 
            {{0, 0, 0, 0},{0, 0, 0, 0},{0, 0, 0, 0},{0, 0, 0, 0}}, 
            {{0, 0, 0, 0},{0, 0, 0, 0},{0, 0, 0, 0},{0, 0, 0, 0}}
        };
        ArrayList<Voxel> input1 = new ArrayList<>();
        input1 = puzlock.initializeVoxelArray(inputPiece);
        ArrayList<Voxel> actualResult = puzlock.expandKeyPiece(input1);
        //please note that the expansion may occur in many ways therefore is is more accurate to compare by size...
        assertEquals(expectedResult.size(), actualResult.size());
    }
    
    @Test
    public void indexOfCoordinate() {
        //int indexOfCoordinate(int x, int y, int z, int sizeOfAxes)
        int input1 = 1;
        int input2 = 1;
        int input3 = 3;
        int input4 = 4;
        int expectedResult = input1+(input2*input4)+(input3*input4*input4); //remember to cater for padded 0s
        int actualResult = puzlock.indexOfCoordinate(input1, input2, input3, input4);
        assertEquals(expectedResult, actualResult);
    }
    
    
    @Test
    public void computeMeshSize() {
        //int computeMeshSize(int[][][] vMesh) --this method is currently unused
        int[][][] input1 = {
            {{1, 1, 1, 1},{1, 1, 1, 1},{1, 1, 1, 1},{1, 1, 1, 1}}, 
            {{1, 1, 1, 1},{1, 1, 1, 1},{1, 1, 1, 1},{1, 1, 1, 1}}, 
            {{1, 1, 1, 1},{1, 1, 1, 1},{1, 1, 1, 1},{1, 1, 1, 1}}, 
            {{1, 1, 1, 1},{1, 1, 1, 1},{1, 1, 1, 1},{1, 1, 1, 1}}
        };
        int expectedResult = 64;
        int actualResult = puzlock.computeMeshSize(input1);
        assertEquals(expectedResult, actualResult);
    }
    /*
    @Test
    public void setOutputPieces() {
        //int[][][] setOutputPieces(ArrayList<Voxel> piece)
    	int[][][] piece = {
            {{1, 1, 1, 0},{0, 0, 0, 0},{0, 0, 0, 0},{0, 0, 0, 0}}, 
            {{1, 1, 1, 1},{0, 0, 0, 0},{0, 0, 0, 0},{0, 0, 0, 0}}, 
            {{0, 0, 0, 1},{0, 0, 0, 0},{0, 0, 0, 0},{0, 0, 0, 0}}, 
            {{0, 0, 0, 0},{0, 0, 0, 0},{0, 0, 0, 0},{0, 0, 0, 0}}
        };
        ArrayList<Voxel> input1 = puzlock.initializeVoxelArray(piece);
        //System.out.println("debug printing the input arraylist...");
        //for	(int i=0; i<input1.size(); i++) {
        //	System.out.print(input1.get(i)+" ");
        //}
        int[][][] expectedResult = {
            {{1, 1, 1, 0},{0, 0, 0, 0},{0, 0, 0, 0},{0, 0, 0, 0}}, 
            {{1, 1, 1, 1},{0, 0, 0, 0},{0, 0, 0, 0},{0, 0, 0, 0}}, 
            {{0, 0, 0, 1},{0, 0, 0, 0},{0, 0, 0, 0},{0, 0, 0, 0}}, 
            {{0, 0, 0, 0},{0, 0, 0, 0},{0, 0, 0, 0},{0, 0, 0, 0}}
        };
        int[][][] actualResult = puzlock.setOutputPieces(input1, 4);
        assertEquals(expectedResult, actualResult);
    }
    */
    
    @Test
    public void getLeft() {
        //Voxel getLeft(int x, int y, int z, int[][][] vMesh, int vMeshSize, ArrayList<Voxel> voxelArray)
    	int input1 = 3; //x
        int input2 = 0; //y
        int input3 = 1; //z
        int[][][] input4 = {
            {{1, 1, 1, 0},{0, 0, 0, 0},{0, 0, 0, 0},{0, 0, 0, 0}}, 
            {{1, 1, 1, 1},{0, 0, 0, 0},{0, 0, 0, 0},{0, 0, 0, 0}}, 
            {{0, 0, 0, 1},{0, 0, 0, 0},{0, 0, 0, 0},{0, 0, 0, 0}}, 
            {{0, 0, 0, 0},{0, 0, 0, 0},{0, 0, 0, 0},{0, 0, 0, 0}}
        };
        int input5 = input4[0].length;
        ArrayList<Voxel> input6 = puzlock.initializeVoxelArray(input4);
        /*
        System.out.println("debug printing the input arraylist...");
        for	(int i=0; i<input6.size(); i++) {
        	Voxel v = input6.get(i);
        	System.out.println(i+") "+v+"@"+v.getCoordinates()+": "+v.value);
        }*/
        System.out.println("Voxel left of "+input1+","+input2+","+input3);
        System.out.println("\n---");
        Voxel expectedResult = input6.get(18); //the 18th voxel to be added is the left neighbour
        System.out.println("Expected: "+expectedResult.getCoordinates());
        Voxel actualResult = puzlock.getLeft(input1, input2, input3, input4, input5, input6);
        if (actualResult!=null) {
        	System.out.println("Actual: "+actualResult.getCoordinates()); //NullPointerException if left is null
        }else {
        	System.out.println("Actual: null");
        }
        assertEquals(expectedResult, actualResult);
    }
    
    @Test
    public void getRight() {
        //Voxel getRight(int x, int y, int z)
    	int input1 = 3; //x
        int input2 = 0; //y
        int input3 = 1; //z
        int[][][] input4 = {
            {{1, 1, 1, 0},{0, 0, 0, 0},{0, 0, 0, 0},{0, 0, 0, 0}}, 
            {{1, 1, 1, 1},{0, 0, 0, 0},{0, 0, 0, 0},{0, 0, 0, 0}}, 
            {{0, 0, 0, 1},{0, 0, 0, 0},{0, 0, 0, 0},{0, 0, 0, 0}}, 
            {{0, 0, 0, 0},{0, 0, 0, 0},{0, 0, 0, 0},{0, 0, 0, 0}}
        };
        int input5 = input4[0].length;
        ArrayList<Voxel> input6 = puzlock.initializeVoxelArray(input4);
        /*
        System.out.println("debug printing the input arraylist...");
        for	(int i=0; i<input6.size(); i++) {
        	Voxel v = input6.get(i);
        	System.out.println(i+") "+v+"@"+v.getCoordinates()+": "+v.value);
        }*/
        System.out.println("Voxel right of "+input1+","+input2+","+input3);
        System.out.println("\n---");
        Voxel expectedResult = null; //the 18th voxel to be added is the right neighbour
        System.out.println("Expected: "+expectedResult);
        Voxel actualResult = puzlock.getRight(input1, input2, input3, input4, input5, input6);
        if (actualResult!=null) {
        	System.out.println("Actual: "+actualResult.getCoordinates()); //NullPointerException if left is null
        }else {
        	System.out.println("Actual: null");
        }
        assertEquals(expectedResult, actualResult);
    }
    
    
    @Test
    public void getUp() {
        //Voxel getUp(int x, int y, int z)
    	int input1 = 3; //x
        int input2 = 0; //y
        int input3 = 1; //z
        int[][][] input4 = {
            {{1, 1, 1, 0},{0, 0, 0, 0},{0, 0, 0, 0},{0, 0, 0, 0}}, 
            {{1, 1, 1, 1},{0, 0, 0, 0},{0, 0, 0, 0},{0, 0, 0, 0}}, 
            {{0, 0, 0, 1},{0, 0, 0, 0},{0, 0, 0, 0},{0, 0, 0, 0}}, 
            {{0, 0, 0, 0},{0, 0, 0, 0},{0, 0, 0, 0},{0, 0, 0, 0}}
        };
        int input5 = input4[0].length;
        ArrayList<Voxel> input6 = puzlock.initializeVoxelArray(input4);
        /*System.out.println("debug printing the input arraylist...");
        for	(int i=0; i<input6.size(); i++) {
        	Voxel v = input6.get(i);
        	System.out.println(i+") "+v+"@"+v.getCoordinates()+": "+v.value);
        }*/
        System.out.println("Voxel up of "+input1+","+input2+","+input3);
        System.out.println("\n---");
        Voxel expectedResult = null;; //the 35th voxel to be added is the up neighbour
        System.out.println("Expected: "+expectedResult);
        Voxel actualResult = puzlock.getUp(input1, input2, input3, input4, input5, input6);
        if (actualResult!=null) {
        	System.out.println("Actual: "+actualResult.getCoordinates()); //NullPointerException if left is null
        }else {
        	System.out.println("Actual: null");
        }
        assertEquals(expectedResult, actualResult);
    }
    
    @Test
    public void getDown() {
        //Voxel getDown(int x, int y, int z)
    	int input1 = 3; //x
        int input2 = 0; //y
        int input3 = 1; //z
        int[][][] input4 = {
            {{1, 1, 1, 0},{0, 0, 0, 0},{0, 0, 0, 0},{0, 0, 0, 0}}, 
            {{1, 1, 1, 1},{0, 0, 0, 0},{0, 0, 0, 0},{0, 0, 0, 0}}, 
            {{0, 0, 0, 1},{0, 0, 0, 0},{0, 0, 0, 0},{0, 0, 0, 0}}, 
            {{0, 0, 0, 0},{0, 0, 0, 0},{0, 0, 0, 0},{0, 0, 0, 0}}
        };
        int input5 = input4[0].length;
        ArrayList<Voxel> input6 = puzlock.initializeVoxelArray(input4);
        /*System.out.println("debug printing the input arraylist...");
        for	(int i=0; i<input6.size(); i++) {
        	Voxel v = input6.get(i);
        	System.out.println(i+") "+v+"@"+v.getCoordinates()+": "+v.value);
        }*/
        System.out.println("Voxel down of "+input1+","+input2+","+input3);
        System.out.println("\n---");
        Voxel expectedResult = input6.get(23); //the 23rd voxel to be added is the down neighbour
        System.out.println("Expected: "+expectedResult.getCoordinates());
        Voxel actualResult = puzlock.getDown(input1, input2, input3, input4, input5, input6);
        if (actualResult!=null) {
        	System.out.println("Actual: "+actualResult.getCoordinates()); //NullPointerException if down is null
        }else {
        	System.out.println("Actual: null");
        }
        assertEquals(expectedResult, actualResult);
    }
    
    @Test
    public void getForward() {
        //Voxel getForward(int x, int y, int z)
    	int input1 = 3; //x
        int input2 = 0; //y
        int input3 = 1; //z
        int[][][] input4 = {
            {{1, 1, 1, 0},{0, 0, 0, 0},{0, 0, 0, 0},{0, 0, 0, 0}}, 
            {{1, 1, 1, 1},{0, 0, 0, 0},{0, 0, 0, 0},{0, 0, 0, 0}}, 
            {{0, 0, 0, 1},{0, 0, 0, 0},{0, 0, 0, 0},{0, 0, 0, 0}}, 
            {{0, 0, 0, 0},{0, 0, 0, 0},{0, 0, 0, 0},{0, 0, 0, 0}}
        };
        int input5 = input4[0].length;
        ArrayList<Voxel> input6 = puzlock.initializeVoxelArray(input4);
        /*System.out.println("debug printing the input arraylist...");
        for	(int i=0; i<input6.size(); i++) {
        	Voxel v = input6.get(i);
        	System.out.println(i+") "+v+"@"+v.getCoordinates()+": "+v.value);
        }*/
        System.out.println("Voxel forward of "+input1+","+input2+","+input3);
        System.out.println("\n---");
        Voxel expectedResult = input6.get(35); //the 35th voxel to be added is the forward neighbour
        System.out.println("Expected: "+expectedResult.getCoordinates());
        Voxel actualResult = puzlock.getForward(input1, input2, input3, input4, input5, input6);
        if (actualResult!=null) {
        	System.out.println("Actual: "+actualResult.getCoordinates()); //NullPointerException if forward is null
        }else {
        	System.out.println("Actual: null");
        }
        assertEquals(expectedResult, actualResult);
    }
    
    @Test
    public void getBackward() {
        //Voxel getBackward(int x, int y, int z)
    	int input1 = 3; //x
        int input2 = 0; //y
        int input3 = 1; //z
        int[][][] input4 = {
            {{1, 1, 1, 0},{0, 0, 0, 0},{0, 0, 0, 0},{0, 0, 0, 0}}, 
            {{1, 1, 1, 1},{0, 0, 0, 0},{0, 0, 0, 0},{0, 0, 0, 0}}, 
            {{0, 0, 0, 1},{0, 0, 0, 0},{0, 0, 0, 0},{0, 0, 0, 0}}, 
            {{0, 0, 0, 0},{0, 0, 0, 0},{0, 0, 0, 0},{0, 0, 0, 0}}
        };
        int input5 = input4[0].length;
        ArrayList<Voxel> input6 = puzlock.initializeVoxelArray(input4);
        /*System.out.println("debug printing the input arraylist...");
        for	(int i=0; i<input6.size(); i++) {
        	Voxel v = input6.get(i);
        	System.out.println(i+") "+v+"@"+v.getCoordinates()+": "+v.value);
        }*/
        System.out.println("Voxel backward of "+input1+","+input2+","+input3);
        System.out.println("\n---");
        Voxel expectedResult = input6.get(3); //the 3rd voxel to be added is the backward neighbour
        System.out.println("Expected: "+expectedResult.getCoordinates());
        Voxel actualResult = puzlock.getBackward(input1, input2, input3, input4, input5, input6);
        if (actualResult!=null) {
        	System.out.println("Actual: "+actualResult.getCoordinates()); //NullPointerException if backward is null
        }else {
        	System.out.println("Actual: null");
        }
        assertEquals(expectedResult, actualResult);
    }
    /*
    @Test
    public void countNeighbours() {
        //int countNeighbours(Voxel v)
        Voxel input1;
        int expectedResult = 3;
        int actualResult = examples.add(5, 4);
        assertEquals(expectedResult, actualResult);
    }
    
    @Test
    public void sumOfNeighboursAccValues() {
        //double sumOfNeighboursAccValues(int x, int y, int z)
        int input1 = 5;
        int input2 = 4;
        int expectedResult = input1+input2;
        int actualResult = examples.add(5, 4);
        assertEquals(expectedResult, actualResult);
    }
    
    @Test
    public void addVoxels() {
        //ArrayList<Voxel> addVoxels(ArrayList<Voxel> addedVoxels)
        int input1 = 5;
        int input2 = 4;
        int expectedResult = input1+input2;
        int actualResult = examples.add(5, 4);
        assertEquals(expectedResult, actualResult);
    }
    
    @Test
    public void addVoxels2() {
        //ArrayList<Voxel> addVoxels2(ArrayList<Voxel> addedVoxels)
        int input1 = 5;
        int input2 = 4;
        int expectedResult = input1+input2;
        int actualResult = examples.add(5, 4);
        assertEquals(expectedResult, actualResult);
    }
    
    @Test
    public void floodFill() {
        //ArrayList<Voxel> floodFill(ArrayList<Voxel> keyPiece)
        int input1 = 5;
        int input2 = 4;
        int expectedResult = input1+input2;
        int actualResult = examples.add(5, 4);
        assertEquals(expectedResult, actualResult);
    }
    
    @Test
    public void floodFill2() {
        //ArrayList<Voxel> floodFill2(ArrayList<Voxel> unvisitedVoxels, Voxel firstVoxel)
        int input1 = 5;
        int input2 = 4;
        int expectedResult = input1+input2;
        int actualResult = examples.add(5, 4);
        assertEquals(expectedResult, actualResult);
    }
    */
}
