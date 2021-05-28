import java.util.ArrayList;

public class Node {

    Node parent;
    ArrayList<Node> sons;
    Object data;

    public Node(Node parent, Object data){
        this.parent = parent;
        this.data = data;
        this.sons = new ArrayList<>();
    }



}
