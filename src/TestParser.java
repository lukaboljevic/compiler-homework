import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class TestParser {

    // Main method of the parser tester
    public static void main(String[] args) {

        String path = "C:\\Users\\lukab\\Desktop\\compiler-homework\\src\\";  // path to project dir
        path += "test1.txt";

        try {
            Parser parser = new Parser(new OurScanner(new InputStreamReader(new FileInputStream(path))));
            System.out.println(parser.getErrors() + " errors detected");
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("-- cannot open input file " + path);
        }
    }
}