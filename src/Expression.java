public class Expression extends Statement {

    String operator;
    Expression left, right;
    Object value;  // used for identifier names

    Expression(String operator, Expression left, Expression right, String kind){
        this.operator = operator;
        this.left = left;
        this.right = right;
        this.kind = kind;
    }
}
