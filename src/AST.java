public class AST {

    Node root;

    public AST(Node root){
        this.root = root;
    }

    public void add(Node node){
        if(this.root == null){
            this.root = node;
            return;
        }


    }
}
