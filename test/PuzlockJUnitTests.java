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
        }
        System.out.println("Voxel left of "+input1+","+input2+","+input3);
        System.out.println("\n---");*/
        Voxel expectedResult = input6.get(18); //the 18th voxel to be added is the left neighbour
        Voxel actualResult = puzlock.getLeft(input1, input2, input3, input4, input5, input6);
        /*System.out.println("Expected: "+expectedResult.getCoordinates());
        if (actualResult!=null) {
        	System.out.println("Actual: "+actualResult.getCoordinates()); //NullPointerException if backward is null
        }else {
        	System.out.println("Actual: null");
        }*/
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
        }
        System.out.println("Voxel right of "+input1+","+input2+","+input3);
        System.out.println("\n---");*/
        Voxel expectedResult = null; //the 18th voxel to be added is the right neighbour
        Voxel actualResult = puzlock.getRight(input1, input2, input3, input4, input5, input6);
        /*System.out.println("Expected: "+expectedResult.getCoordinates());
        if (actualResult!=null) {
        	System.out.println("Actual: "+actualResult.getCoordinates()); //NullPointerException if backward is null
        }else {
        	System.out.println("Actual: null");
        }*/
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
        }
        System.out.println("Voxel up of "+input1+","+input2+","+input3);
        System.out.println("\n---");*/
        Voxel expectedResult = null;; //the 35th voxel to be added is the up neighbour
        Voxel actualResult = puzlock.getUp(input1, input2, input3, input4, input5, input6);
        /*System.out.println("Expected: "+expectedResult.getCoordinates());
        if (actualResult!=null) {
        	System.out.println("Actual: "+actualResult.getCoordinates()); //NullPointerException if backward is null
        }else {
        	System.out.println("Actual: null");
        }*/
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
        }
        System.out.println("Voxel down of "+input1+","+input2+","+input3);
        System.out.println("\n---");*/
        Voxel expectedResult = input6.get(23); //the 23rd voxel to be added is the down neighbour
        Voxel actualResult = puzlock.getDown(input1, input2, input3, input4, input5, input6);
        /*System.out.println("Expected: "+expectedResult.getCoordinates());
        if (actualResult!=null) {
        	System.out.println("Actual: "+actualResult.getCoordinates()); //NullPointerException if backward is null
        }else {
        	System.out.println("Actual: null");
        }*/
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
        }
        System.out.println("Voxel forward of "+input1+","+input2+","+input3);
        System.out.println("\n---");*/
        Voxel expectedResult = input6.get(35); //the 35th voxel to be added is the forward neighbour
        Voxel actualResult = puzlock.getForward(input1, input2, input3, input4, input5, input6);
        /*System.out.println("Expected: "+expectedResult.getCoordinates());
        if (actualResult!=null) {
        	System.out.println("Actual: "+actualResult.getCoordinates()); //NullPointerException if backward is null
        }else {
        	System.out.println("Actual: null");
        }*/
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
        }
        System.out.println("Voxel backward of "+input1+","+input2+","+input3);
        System.out.println("\n---");*/
        Voxel expectedResult = input6.get(3); //the 3rd voxel to be added is the backward neighbour
        Voxel actualResult = puzlock.getBackward(input1, input2, input3, input4, input5, input6);
        /*System.out.println("Expected: "+expectedResult.getCoordinates());
        if (actualResult!=null) {
        	System.out.println("Actual: "+actualResult.getCoordinates()); //NullPointerException if backward is null
        }else {
        	System.out.println("Actual: null");
        }*/
        assertEquals(expectedResult, actualResult);
    }
    
    @Test
    public void countNeighbours() {
        //int countNeighbours(Voxel v, int[][][] vMesh, int vMeshSize, ArrayList vs)
    	int[][][] input2 = {
            {{1, 1, 1, 0},{0, 0, 0, 0},{0, 0, 0, 0},{0, 0, 0, 0}}, 
            {{1, 1, 1, 1},{0, 0, 0, 0},{0, 0, 0, 0},{0, 0, 0, 0}}, 
            {{0, 0, 0, 1},{0, 0, 0, 0},{0, 0, 0, 0},{0, 0, 0, 0}}, 
            {{0, 0, 0, 0},{0, 0, 0, 0},{0, 0, 0, 0},{0, 0, 0, 0}}
        };
    	ArrayList<Voxel> input4 = puzlock.initializeVoxelArray(input2);
        Voxel input1 = input4.get(19); //voxel at index 3,0,1 is at index 19 and has 2 neighbours
        int input3 = input2[0].length;
        int expectedResult = 2;
        int actualResult = puzlock.countNeighbours(input1, input2, input3, input4);
        assertEquals(expectedResult, actualResult);
    }
    
    @Test
    public void sumOfNeighboursAccValues() {
        //double sumOfNeighboursAccValues(int x, int y, int z, int[][][] vMesh, int vMeshSize, ArrayList<Voxel> vs)
    	//note: this method is implemented in computeVoxelAccessibility()...
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
        //first compute the initial accessibility values...
        // first pass - simple neighbour count
        int p=1; //stores index of iterator
        for (int z=0; z<input5; z++){
            for (int y=0; y<input5; y++){
                for (int x=0; x<input5; x++){
                    if (input4[z][y][x] == 1){
                        //get the voxel at that index and set its accessibility value...
                        int index = puzlock.indexOfCoordinate(x,y,z, input5);
                        Voxel v = input6.get(index);
                        int neighbours = puzlock.countNeighbours(v,input4,input5,input6);
                        v.accessibilityValue = neighbours;
                        System.out.println(p+") Voxel "+v+" at index "+x+","+y+","+z+" has "+neighbours+" neighbours");
                        p++;
                    }
                }
            }
        }
        double expectedResult = 4; //3,0,1 has 2 neighbours - 3,0,2 (1 neighbour) + 2,0,1 (3 neighbours)
        double actualResult = puzlock.sumOfNeighboursAccValues(input1, input2, input3, input4, input5, input6);
        assertEquals(expectedResult, actualResult);
    }
    
    @Test
    public void addVoxels() {
        //ArrayList<Voxel> addVoxels(ArrayList<Voxel> addedVoxels, ArrayList<PuzzlePiece> pieces, int[][][] vMesh, int vMeshSize, ArrayList<Voxel> vs, ArrayList<Voxel> candidates, int beta)
    	//note: this method is implemented in expandKeyPiece(ArrayList<Voxel> piece)...
    	/* Example of removable piece to be expanded (normal direction is backward, seed is at 0,0,0):
        Anchor voxel is at 1,0,1
        Anchor2 is at 2,0,1 (anchor2 is neither in path nor the blocking???)
        Blockee is at 0,0,1
        Blocking is at 0,0,0
        0) Voxel@38af3868 is at 0,0,1
		1) Voxel@e9e54c2 is at 1,0,1
		2) Voxel@65b54208 is at 1,0,0
		3) Voxel@1be6f5c3 is at 2,0,0
		4) Voxel@6b884d57 is at 3,0,0
        */
    	int[][][] inputPiece = {
    		{{0, 0, 0, 0},{0, 0, 0, 0},{0, 0, 0, 0},{0, 0, 0, 0}}, 
            {{0, 0, 0, 0},{0, 0, 0, 0},{0, 0, 0, 0},{0, 0, 0, 0}}, 
            {{0, 0, 0, 0},{0, 0, 0, 0},{0, 0, 0, 0},{0, 0, 0, 0}}, 
            {{0, 0, 0, 0},{0, 0, 0, 0},{0, 0, 0, 0},{0, 0, 0, 0}}
        }; //initial, used array
        int[][][] inputPiece1 = {
			{{1, 1, 1, 1},{1, 1, 1, 1},{1, 1, 1, 1},{1, 1, 1, 1}}, 
	        {{1, 1, 1, 1},{1, 1, 1, 1},{1, 1, 1, 1},{1, 1, 1, 1}}, 
	        {{1, 1, 1, 1},{1, 1, 1, 1},{1, 1, 1, 1},{1, 1, 1, 1}}, 
	        {{1, 1, 1, 1},{1, 1, 1, 1},{1, 1, 1, 1},{1, 1, 1, 1}}
        }; ///inputVoxelizedMesh
        int[][][] inputPiece2 = {
    		{{0, 1, 1, 1},{0, 0, 0, 0},{0, 0, 0, 0},{0, 0, 0, 0}}, 
            {{1, 1, 0, 0},{0, 0, 0, 0},{0, 0, 0, 0},{0, 0, 0, 0}}, 
            {{0, 0, 0, 0},{0, 0, 0, 0},{0, 0, 0, 0},{0, 0, 0, 0}}, 
            {{0, 0, 0, 0},{0, 0, 0, 0},{0, 0, 0, 0},{0, 0, 0, 0}}
        }; //removablePiece
    	ArrayList<Voxel> input = (ArrayList<Voxel>) puzlock.initializeVoxelArray(inputPiece).clone();
    	input.get(16).value = 1; //0,0,1
    	input.get(17).value = 1; //1,0,1
    	input.get(1).value = 1; //1,0,0
    	input.get(2).value = 1; //2,0,0
    	input.get(3).value = 1; //3,0,0
    	ArrayList<Voxel> input1 = new ArrayList<>();
    	input1.add(input.get(16)); //0,0,1
    	input1.add(input.get(17)); //1,0,1
    	input1.add(input.get(1)); //1,0,0
    	input1.add(input.get(2)); //2,0,0
    	input1.add(input.get(3)); //3,0,0
    	System.out.println("debug printing the input arraylist...");
        for	(int i=0; i<input1.size(); i++) {
        	Voxel v = input1.get(i);
    		System.out.println(i+") "+v+"@"+v.getCoordinates()+"-->"+v.value);
        }
        Voxel anchorVoxel = input.get(17); //1,0,1
        Voxel anchorVoxel2 = input.get(18); //2,0,1
        Voxel blocking = input.get(0); //at 0,0,0
        Voxel blockee = input.get(16); //at 0,0,1
    	ArrayList<PuzzlePiece> input2 = new ArrayList<>();
    	input2.add(new PuzzlePiece(input1, anchorVoxel, anchorVoxel2, blocking, blockee));
    	int[][][] input3 = inputPiece1; //inputVoxelizedMesh
        int input4 = 4; //inputVoxelizedMeshSize
        ArrayList<Voxel> input5 = (ArrayList<Voxel>) input.clone();
        for	(int i=0; i<input5.size(); i++) {
        	input5.get(i).value = 1;
        }
        ArrayList<Voxel> input6 = new ArrayList<>(); //the set of candidates is initialized to an empty array
        int input7 = 3; //Beta = 3
        /*Debug printing the set of candidates...
		0) Voxel@5b2133b1 is at 0,0,2
		1) Voxel@63961c42 is at 0,0,0
		2) Voxel@65ab7765 is at 2,0,1
		3) Voxel@77459877 is at 3,0,1
		Chosen candidate voxel is @ 3,0,1! Added 3,0,1 to the key piece
        Chosen candidate voxel is @ 0,0,2! Added 0,0,2 to the key piece
        Chosen candidate voxel is @ 2,0,1! Added 2,0,1 to the key piece
        */
        ArrayList<Voxel> expectedResult = (ArrayList<Voxel>) input1.clone(); //initialized to the removablePiece
        input.get(19).value = 1; //3,0,1
        expectedResult.add(input.get(19));
        input.get(32).value = 1; //0,0,2
        expectedResult.add(input.get(32));
        input.get(18).value = 1; //2,0,1
        expectedResult.add(input.get(18));
        ArrayList<Voxel> actualResult = puzlock.addVoxels(input1, input2, input3, input4, input5, input6, input7);
        System.out.println("debug printing the expected arraylist...");
        for	(int i=0; i<expectedResult.size(); i++) {
        	Voxel v = expectedResult.get(i);
        	System.out.println(i+") "+v+"@"+v.getCoordinates()+"-->"+v.value);
        }
        System.out.println("debug printing the actual arraylist...");
        for	(int i=0; i<actualResult.size(); i++) {
        	Voxel v2 = actualResult.get(i);
        	System.out.println(i+") "+v2+"@"+v2.getCoordinates()+"-->"+v2.value);
        }
        assertEquals(expectedResult, actualResult);
        //note: this test fails as we have to specify the accessibility value of each voxel as required in addVoxels2 --> sumOfAccessVals...
    }
    /*
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
