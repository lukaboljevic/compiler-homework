import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class TestParser {

    // Main method of the parser tester
    public static void main(String[] args) {

        String path = "C:\\Users\\Luka\\Desktop\\Kompajleri domaci\\src";
        String source = path + "\\";
        source += "test1.txt";
		/*
            TODO: Error handling in Parser class? I believe all are handled but check just in case
		*/

        try {
            Parser parser = new Parser(new OurScanner(new InputStreamReader(new FileInputStream(source))));
            System.out.println(parser.getErrors() + " errors detected");
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("-- cannot open input file " + source);
        }
    }
}