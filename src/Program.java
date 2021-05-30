import java.util.ArrayList;

public class Program {

    ArrayList<Declaration> declarations;
    CommandSequence commandSequence;

    public Program(ArrayList<Declaration> decls, CommandSequence cs){
        this.declarations = decls;
        this.commandSequence = cs;
    }

}
