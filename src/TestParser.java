import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class TestParser {

    // Main method of the parser tester
    public static void main(String[] args) {

        String path = System.getProperty("user.dir");  // path to project dir
        path += "\\src\\test.txt";

        try {
            Parser parser = new Parser(new Scanner(new InputStreamReader(new FileInputStream(path))));
            if (parser.getErrors() == 0) {
                System.out.println("Parsing completed successfully.");
            } else {
                System.out.printf("There have been %d detected errors", parser.getErrors());
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("-- cannot open input file " + path);
        }
    }
}