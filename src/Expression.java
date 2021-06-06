public class Expression extends Statement {

    String operator;
    Expression left, right;
    Object value; // identifier name, constant value, type of read operation

    Expression(String operator, Expression left, Expression right, String kind) {
        this.operator = operator;
        this.left = left;
        this.right = right;
        this.kind = kind;
    }
}
