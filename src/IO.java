import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * @author Nkosi Gumede
 */

public class IO {
    int [][][] inputArray; //3D array to story the file's contents
    int x,y,z = 0; //store co-ordinates, initialized to 0
    
    public static void main(String[] args) throws FileNotFoundException, IOException{
        Scanner sc = new Scanner(System.in);
        System.out.println("Please enter the name of the file you wish to read in...");
        String filename = sc.nextLine();
        readFileToInputGrid(filename);
    }
    
    static void readFileToInputGrid(String filename) throws FileNotFoundException, IOException{
        File file = new File(filename); 
        BufferedReader br = new BufferedReader(new FileReader(file));
        String line = br.readLine();
        System.out.println(line+"\n");
        while (line!=null){
            System.out.println(line);
            line = br.readLine(); //each line represents 1 z
            
        }
    }
    
    static void printGridToFile(ArrayList<Voxel> grid){
        
    }
}
