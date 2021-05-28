public class AssignExpression extends Statement{

    String identifier;
    String operator = "=";
    Expression expression;

    public AssignExpression(){
        this.kind = "Assign Expression";
    }

}
