import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * @author Nkosi Gumede
 */
public class IO {
    static int [][][] inputArray; //3D array to story the file's contents
    static Scanner sc = new Scanner(System.in);
    static ArrayList<Integer> xVals; //stores the xs which will be stored in the set of ys
    static ArrayList<ArrayList> yVals; //stores the ys which will store xs and be stored in the set of zs
    static ArrayList<ArrayList> zVals;  //stores the zs which will store ys 
    
    /*
    public static void main(String[] args) throws FileNotFoundException, IOException{
        System.out.println("Please enter the name of the file you wish to read in...");
        String filename = sc.nextLine();
        readFileToInputGrid(filename);
        System.out.println("Debug printing the input array...");
        debugPrintVoxelizedMesh(inputArray);
        printGridToFile(inputArray);
    }
    */
    
    static int[][][] readFileToInputGrid(String filename) throws FileNotFoundException, IOException{
        File file = new File(filename); 
        BufferedReader br = new BufferedReader(new FileReader(file));
        String line = br.readLine();
        int zs = 0; //an iterator indicating where we are on the z-axis
        zVals = new ArrayList<>(); 
        while (line!=null){ //Each line represents 1 z
        	String[] yy = line.split(","); //store the ys by splitting
        	yVals = new ArrayList<>(); 
        	for (int i=0; i<yy.length; i++) { //for all y indices
                    String[] xx = yy[i].split("");
                    xVals = new ArrayList<>(); 
                    for (int j=0; j<xx.length; j++) { //for all x indices
                        xVals.add(Integer.parseInt(xx[j])); //sets the value of the input array
                    }
                    yVals.add(xVals); //store all xs in yVals
        	}
        	zVals.add(yVals); //store all ys in zVals
        	//System.out.println(line);
        	line = br.readLine(); //update the line
        	zs++; //increment the z-axis
        }
        return readFileToInputGrid2(xVals, yVals, zVals);
    }
    
    /* Completes the implementation of readFileToInputGrid. 
     * Helps set inputVoxelizedMesh to appropriate value */
    static int[][][] readFileToInputGrid2(ArrayList xs, ArrayList ys, ArrayList zs) {
//    	System.out.println("Length of z-axis: "+zs.size());
//    	System.out.println("Length of y-axis: "+ys.size());
//    	System.out.println("Length of x-axis: "+xs.size());
    	inputArray = new int[zs.size()][ys.size()][xs.size()];
    	for (int h=0; h<zs.size(); h++){ //Each line represents 1 z
            for	(int i=0; i<ys.size(); i++) { //for all y indices
                for (int j=0; j<xs.size(); j++) { //for all x indices
                    ArrayList<ArrayList> z2 = (ArrayList<ArrayList>) zs.get(h);
                    ArrayList<Integer> y2 = (ArrayList<Integer>) z2.get(i);
                    int x2 = y2.get(j);
                    inputArray[h][i][j] = x2; //sets the value of the input array
                }
            }
        }
        return inputArray;
    }
    
    /* takes in a 3D array and prints it to file in the appropriate format */
    static void printGridToFile(int[][][] grid, String filename) throws FileNotFoundException, UnsupportedEncodingException{
//        System.out.println("Please enter the name of the output file:");
//        String outfile = sc.nextLine();
//        PrintWriter writer = new PrintWriter(outfile, "UTF-8");
        //output grid must always be 32x32x32...
        PrintWriter writer = new PrintWriter(filename, "UTF-8");
        for (int i = 0; i < 32; i++) {
            for (int j = 0; j < 32; j++) {
                for (int k = 0; k < 32; k++) {
                    try{
                        writer.print(grid[i][j][k]); //prints xs
                    }catch (Exception e){ //outofbounds exception means we should pad with 0s
                        writer.print("0"); //prints 0s
                    }
                    
                }
                if (j+1 != 32){ //if not the last y in a set of ys, print the comma
                    writer.print(","); //prints comma to separate each y of xs
                }
            }
            if (i+1 != 32){ //if not the last z in a set of zs, print the newline
                writer.println(); //print newline to separate each z of ys
            }
        }
        writer.close();
    }
    
    /* Takes in a 3D array and prints it to console with each z-axis layer being separated by '---'*/
    static void debugPrintVoxelizedMesh(int [][][] vMesh){
        for (int z=0; z<vMesh.length; z++){
            for (int y=0; y<vMesh.length; y++){
                for (int x=0; x<vMesh.length; x++){
                    System.out.print(vMesh[z][y][x]);
                }
                System.out.println("");
            }
            System.out.println("---");
        }
    }
}
