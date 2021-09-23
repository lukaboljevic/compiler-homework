import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class Parser {

    private Token la;       // lookahead token (still unrecognized)
    private TokenCode sym;  // always contains la.kind; token code of the lookahead token
    private int errDist;    // no. of correctly recognized tokens since last error
    private int errors;     // error counter

    // maps a nonterminal to it's FirstSet; the FirstSet is a list of codes from above
    private final HashMap<String, ArrayList<TokenCode>> firstMap;
    private final HashMap<String, Pair> symbolTable;
    private final Scanner scanner;

    public Parser(Scanner s) {

        this.symbolTable = new HashMap<>();
        this.firstMap = new HashMap<>();
        this.firstMap.put("Declarations", new ArrayList<>(Arrays.asList(TokenCode.INTEGER_TYPE,
                TokenCode.BOOL_TYPE, TokenCode.STRING_TYPE, TokenCode.DOUBLE_TYPE)));
        this.firstMap.put("CommandSequence", new ArrayList<>(Arrays.asList(TokenCode.IF, TokenCode.WHILE,
                TokenCode.FOR, TokenCode.BREAK, TokenCode.PRINT, TokenCode.REPEAT,
                TokenCode.CALC_BEGIN, TokenCode.IDENTIFIER, TokenCode.INTEGER_CONSTANT,
                TokenCode.BOOL_CONSTANT, TokenCode.STRING_CONSTANT, TokenCode.DOUBLE_CONSTANT,
                TokenCode.LEFT_REGULAR, TokenCode.READINT, TokenCode.READSTRING, TokenCode.READDOUBLE,
                TokenCode.READBOOL)));
        this.firstMap.put("Compare", new ArrayList<>(Arrays.asList(TokenCode.LESS, TokenCode.LESS_EQUAL,
                TokenCode.GREATER, TokenCode.GREATER_EQUAL)));
        this.firstMap.put("Constant", new ArrayList<>(Arrays.asList(TokenCode.INTEGER_CONSTANT,
                TokenCode.BOOL_CONSTANT, TokenCode.STRING_CONSTANT, TokenCode.DOUBLE_CONSTANT)));
        this.firstMap.put("ReadOperations", new ArrayList<>(Arrays.asList(TokenCode.READINT,
                TokenCode.READSTRING, TokenCode.READBOOL, TokenCode.READDOUBLE)));
        // had to put it just for the purposes of CalcStatement
        this.firstMap.put("Expression",
                new ArrayList<>(Arrays.asList(TokenCode.IDENTIFIER, TokenCode.INTEGER_CONSTANT,
                TokenCode.BOOL_CONSTANT, TokenCode.STRING_CONSTANT, TokenCode.DOUBLE_CONSTANT,
                TokenCode.LEFT_REGULAR, TokenCode.READINT, TokenCode.READSTRING, TokenCode.READDOUBLE,
                TokenCode.READBOOL)));

        // start parsing
        this.errors = 0;
        this.scanner = s;
        this.errDist = 3;
        this.scan();
        Program program = this.Program();
        if (this.sym != TokenCode.EOF) {
            error("end of file found before end of program");
        }

        // Print the AST/Parse tree/whatever the hell
        this.printProgram(program);
        System.out.println();
//        System.out.println("----------- SYMBOL TABLE -----------\n");
//        for (String identifier : this.symbolTable.keySet()) {
//            Pair information = this.symbolTable.get(identifier);
//            System.out.printf("IDENTIFIER: %s, TYPE: %s\nEXPRESSION:\n", identifier, information.type);
//            this.printExpr(information.expression, 1);
//            System.out.println();
//        }
    }

    //------------------- Printing methods ----------------------

    /*
    I started off the printing process by just passing in `numTabs + 1` as the parameter
    to the next command in sequence, but through trial and error I ended up with what I did.
    There is no real... algorithm? I just started with the most logical approach (which is
    to pass in numTabs + 1 to every child statement/expression) and just went from there.
     */

    private String generateTabs(int numTabs) {
        String res = "";
        for (int i = 0; i < numTabs; i++) {
            res = res.concat("\t");
        }
        return res;
    }

    private void printProgram(Program program) {
        System.out.println("\nDECLARATIONS\n");
        for (Declaration declaration : program.declarations) {
            if (declaration == null) {
                continue;
            }
            System.out.printf("\tType: %s, Name: %s\n", declaration.type, declaration.identifier);
        }
        System.out.println();
        printCommandSequence(program.commandSequence, 1);
    }

    private void printCommandSequence(CommandSequence commandSequence, int numTabs) {
        System.out.println(generateTabs(numTabs - 1) + "COMMANDS:");
        for (Statement statement : commandSequence.statements) {
            this.printStatement(statement, numTabs);
        }
    }

    private void printStatement(Statement statement, int numTabs) {
        if (statement == null) {
            System.out.println(generateTabs(numTabs) + "NULL STATEMENT");
            return;
        }
        switch (statement.kind) {
            case StatementKind.IF_STATEMENT -> printIfStatement((StatementIf) statement, numTabs);
            case StatementKind.WHILE_STATEMENT -> printWhileStatement((StatementWhile) statement, numTabs);
            case StatementKind.FOR_STATEMENT -> printForStatement((StatementFor) statement, numTabs);
            case StatementKind.BREAK_STATEMENT -> System.out.println(generateTabs(numTabs) + statement.kind);
            case StatementKind.PRINT_STATEMENT -> printPrintStatement((StatementPrint) statement, numTabs);
            case StatementKind.REPEAT_STATEMENT -> printRepeatStatement((StatementRepeat) statement, numTabs);
            case StatementKind.CALC_STATEMENT -> printCalcStatement((StatementCalc) statement, numTabs);
            case StatementKind.ASSIGN_EXPR -> printAssignExpr((ExpressionAssign) statement, numTabs);
            case StatementKind.BINARY_EXPR, StatementKind.UNARY_EXPR -> printExpr((Expression) statement,
                    numTabs);
        }
    }

    private void printIfStatement(StatementIf ifStatement, int numTabs) {
        String tabs = generateTabs(numTabs);
        System.out.println(tabs + ifStatement.kind);
        System.out.println(tabs + "\t" + "CONDITION:");
        this.printExpr(ifStatement.expression, numTabs + 2);
        this.printCommandSequence(ifStatement.commandSequence, numTabs + 2);
        this.printEndIfStatement(ifStatement.endIfStatement, numTabs);
    }

    private void printEndIfStatement(StatementEndIf endIfStatement, int numTabs) {
        if (endIfStatement == null) {
            System.out.println(generateTabs(numTabs) + "NULL END IF STATEMENT");
            return;
        }
        String tabs = generateTabs(numTabs);
        if (endIfStatement.commandSequence != null) {
            System.out.println(tabs + "ELSE statement");
            this.printCommandSequence(endIfStatement.commandSequence, numTabs + 2);
        }
        else {
            System.out.println(tabs + "END IF");
        }
    }

    private void printWhileStatement(StatementWhile whileStatement, int numTabs) {
        String tabs = generateTabs(numTabs);
        System.out.println(tabs + whileStatement.kind);
        System.out.println(tabs + "\t" + "CONDITION:");
        this.printExpr(whileStatement.expression, numTabs + 2);
        this.printCommandSequence(whileStatement.commandSequence, numTabs + 2);
    }

    private void printForStatement(StatementFor forStatement, int numTabs) {
        String tabs = generateTabs(numTabs);
        System.out.println(tabs + forStatement.kind);
        this.printAssignExpr(forStatement.first, numTabs + 1);
        System.out.println(tabs + "\t" + "EXPRESSION:");
        this.printExpr(forStatement.expression, numTabs + 2);
        this.printAssignExpr(forStatement.second, numTabs + 1);
        this.printCommandSequence(forStatement.commandSequence, numTabs + 2);
    }

    private void printPrintStatement(StatementPrint printStatement, int numTabs) {
        String tabs = generateTabs(numTabs);
        System.out.println(tabs + printStatement.kind);
        System.out.println(tabs + "\t" + "EXPRESSION:");
        this.printExpr(printStatement.expression, numTabs + 2);
    }

    private void printRepeatStatement(StatementRepeat repeatStatement, int numTabs) {
        String tabs = generateTabs(numTabs);
        System.out.println(tabs + repeatStatement.kind);
        this.printCommandSequence(repeatStatement.commandSequence, numTabs + 2);
        System.out.println(tabs + "\t" + "UNTIL:");
        this.printExpr(repeatStatement.expression, numTabs + 2);
    }

    private void printCalcStatement(StatementCalc calcStatement, int numTabs) {
        String tabs = generateTabs(numTabs);
        System.out.println(tabs + calcStatement.kind);
        for (Expression expression: calcStatement.expressions) {
            this.printExpr(expression, numTabs + 1);
        }
    }

    private void printAssignExpr(ExpressionAssign assign, int numTabs) {
        String tabs = generateTabs(numTabs);
        System.out.println(tabs + assign.kind);
        System.out.println(tabs + "\t" + "IDENTIFIER: " + assign.identifier);
        System.out.println(tabs + "\t" + "EXPRESSION:");
        this.printExpr(assign.expression, numTabs + 2);
        System.out.println(tabs + "\t" + "END:");
        this.printAssignExprEnd(assign.end, numTabs + 1);
    }

    private void printAssignExprEnd(ExpressionAssignEnd end, int numTabs) {
        String tabs = generateTabs(numTabs);
        if (end == null){
            System.out.println(tabs + "IMMEDIATE ASSIGN EXPR END");
        }
        else {
            System.out.println(tabs + "\t" + "FIRST EXPR:");
            this.printExpr(end.first, numTabs + 2);
            System.out.println(tabs + "\t" + "SECOND EXPR:");
            this.printExpr(end.second, numTabs + 2);
        }
    }

    private void printExpr(Expression expression, int numTabs) {
        if (expression == null) {
            System.out.println(generateTabs(numTabs) + "NULL EXPRESSION");
            return;
        }
        switch (expression.kind) {
            case StatementKind.BINARY_EXPR -> this.printBinaryExpr(expression, numTabs);
            case StatementKind.UNARY_EXPR -> this.printUnaryExpr(expression, numTabs);
            case StatementKind.CONSTANT, StatementKind.IDENTIFIER,
                    StatementKind.READ_OPERATION -> System.out.println(generateTabs(numTabs) +
                    expression.kind + ": " + expression.value);
        }
    }

    private void printBinaryExpr(Expression binary, int numTabs) {
        String tabs = generateTabs(numTabs);
        System.out.println(tabs + binary.kind);
        System.out.println(tabs + "\t" + "LEFT OPERAND:");
        this.printExpr(binary.left, numTabs + 2);
        System.out.println(tabs + "\t" + "OPERATOR: " + binary.operator);
        System.out.println(tabs + "\t" + "RIGHT OPERAND:");
        this.printExpr(binary.right, numTabs + 2);
    }

    private void printUnaryExpr(Expression unary, int numTabs) {
        String tabs = generateTabs(numTabs);
        System.out.println(tabs + unary.kind);
        System.out.println(tabs + "\t" + "OPERATOR: " + unary.operator);
        System.out.println(tabs + "\t" + "EXPRESSION:");
        this.printExpr(unary.left, numTabs + 2);
    }

    //------------------- Auxiliary methods ----------------------

    private void scan() {
        // scan for the next token
        this.la = this.scanner.nextToken();
        while (this.la == null) {
            this.la = this.scanner.nextToken();
        }
//        if (la.kind == this.none_){
//            error("Invalid token");
//        }
        this.sym = this.la.kind;
        this.errDist++;
    }

    private void check(TokenCode expected) {
        // check if the kind of the lookahead token is the expected one
        if (this.sym == expected) {
            if (this.sym == TokenCode.IDENTIFIER && this.la.string.length() > 31) {
                error("Identifier name too long (must be <= 31)");
            }
            this.scan();  // read ahead
        }
        else {
            this.error(expected.toString() + " expected");
        }
    }

    private void error(String msg) {
        // syntactic error at the lookahead token
        if (this.errDist >= 3) {
            System.out.println("-- line " + this.la.line + " col " + this.la.col + ": " + msg);
            this.errors++;
        }
        this.errDist = 0;
    }

    public int getErrors() {
        return this.errors;
    }

    //-------------- P A R S I N G   M E T H O D S -----------------

    private Program Program() {
        // Program -> LET Declarations IN CommandSequence END
        this.check(TokenCode.LET);
        ArrayList<Declaration> declarations = this.Declarations();
        this.check(TokenCode.IN);
        CommandSequence cs = this.CommandSequence();
        this.check(TokenCode.END);
        return new Program(declarations, cs);
    }

    private ArrayList<Declaration> Declarations() {
        // Declarations -> Decl+
        ArrayList<Declaration> declarations = new ArrayList<>();
        while (this.firstMap.get("Declarations").contains(this.sym)) {
            declarations.add(this.Decl());
        }
        return declarations;
    }

    private Declaration Decl() {
        // Decl -> Type ident ;
        String type = this.Type();
        String identifier = this.la.string;
        this.check(TokenCode.IDENTIFIER);
        if (this.symbolTable.containsKey(identifier)) {
            this.error("Identifier " + identifier + " has already been declared");
            this.check(TokenCode.SEMICOLON);
            return null;
        }
        else {
            this.symbolTable.put(identifier, new Pair(type, null)); // for now there is no expression
            this.check(TokenCode.SEMICOLON);
            return new Declaration(type, identifier);
        }
    }

    private String Type() {
        // Type -> integer | bool | string | double
        switch (this.sym) {
            case INTEGER_TYPE, BOOL_TYPE, DOUBLE_TYPE, STRING_TYPE -> {
                String type = this.sym.toString();
                this.scan();
                return type;
            }
            default -> {
                this.error("variable type (integer, bool, string, double) expected");
                return "Non existent variable type";
            }
        }
    }

    private CommandSequence CommandSequence() {
        // CommandSequence -> {Stmt+}
        ArrayList<Statement> statements = new ArrayList<>();
        this.check(TokenCode.LEFT_CURLY);
        while (this.firstMap.get("CommandSequence").contains(this.sym))
            statements.add(this.Stmt());
        this.check(TokenCode.RIGHT_CURLY);
        return new CommandSequence(statements);
    }

    private Statement Stmt() {
        /*
        Stmt -> IfStmt  | WhileStmt |  ForStmt |
			    BreakStmt  | PrintStmt | RepeatStmt ; | AssignExpr ;
         */
        if (this.sym == TokenCode.IF) {
            return this.IfStmt();
        }
        else if (this.sym == TokenCode.WHILE) {
            return this.WhileStmt();
        }
        else if (this.sym == TokenCode.FOR) {
            return this.ForStmt();
        }
        else if (this.sym == TokenCode.BREAK) {
            return this.BreakStmt();
        }
        else if (this.sym == TokenCode.PRINT) {
            return this.PrintStmt();
        }
        else if (this.sym == TokenCode.REPEAT) {
            return this.RepeatStmt();
        }
        else if (this.sym == TokenCode.CALC_BEGIN) {
            return this.CalcStmt();
        }
        else if (this.sym == TokenCode.IDENTIFIER) {
            ExpressionAssign assign = this.AssignExpr();
            this.check(TokenCode.SEMICOLON);
            return assign;
        }
        else {
            this.error("Statement error");
            return null;
        }
    }

    private StatementIf IfStmt() {
        // IfStmt -> IF ( Expr ) CommandSequence IfStmtEnd
        this.check(TokenCode.IF);
        this.check(TokenCode.LEFT_REGULAR);
        Expression expr = this.Expr();
        this.check(TokenCode.RIGHT_REGULAR);
        CommandSequence cs = this.CommandSequence();
        StatementEndIf endIf = this.EndIf();
        return new StatementIf(expr, cs, endIf);
    }

    private StatementEndIf EndIf() {
        // EndIf -> ELSE CommandSequence FI | FI
        if (this.sym == TokenCode.ELSE) {
            this.scan();
            CommandSequence cs = this.CommandSequence();
            this.check(TokenCode.FI);
            return new StatementEndIf(cs);
        }
        else if (this.sym == TokenCode.FI) {
            this.scan();
            return new StatementEndIf(null);
        }
        else {
            this.error("Invalid end for IF statement");
            return null;
        }
    }

    private StatementWhile WhileStmt() {
        // WhileStmt -> WHILE ( Expr ) CommandSequence
        this.check(TokenCode.WHILE);
        this.check(TokenCode.LEFT_REGULAR);
        Expression expression = this.Expr();
        this.check(TokenCode.RIGHT_REGULAR);
        CommandSequence cs = this.CommandSequence();
        return new StatementWhile(expression, cs);
    }

    private StatementFor ForStmt() {
        // ForStmt -> FOR ( AssignExpr ; Expr ; AssignExpr ) CommandSequence
        this.check(TokenCode.FOR);
        this.check(TokenCode.LEFT_REGULAR);
        ExpressionAssign first = this.AssignExpr();
        this.check(TokenCode.SEMICOLON);
        Expression expression = this.Expr();
        this.check(TokenCode.SEMICOLON);
        ExpressionAssign second = this.AssignExpr();
        this.check(TokenCode.RIGHT_REGULAR);
        CommandSequence cs = this.CommandSequence();
        return new StatementFor(first, expression, second, cs);
    }

    private Statement BreakStmt() {
        // BreakStmt -> BREAK ;
        this.check(TokenCode.BREAK);
        this.check(TokenCode.SEMICOLON);
        Statement statement = new Statement();
        statement.kind = StatementKind.BREAK_STATEMENT;
        return statement;
    }

    private StatementPrint PrintStmt() {
        // PrintStmt -> PRINT (Expr) ;
        this.check(TokenCode.PRINT);
        this.check(TokenCode.LEFT_REGULAR);
        Expression expression = this.Expr();
        this.check(TokenCode.RIGHT_REGULAR);
        this.check(TokenCode.SEMICOLON);
        return new StatementPrint(expression);
    }

    private StatementRepeat RepeatStmt() {
        // RepeatStmt -> REPEAT CommandSequence UNTIL ( Expr ) ;
        this.check(TokenCode.REPEAT);
        CommandSequence cs = this.CommandSequence();
        this.check(TokenCode.UNTIL);
        this.check(TokenCode.LEFT_REGULAR);
        Expression expr = this.Expr();
        this.check(TokenCode.RIGHT_REGULAR);
        this.check(TokenCode.SEMICOLON);
        return new StatementRepeat(cs, expr);
    }

    private StatementCalc CalcStmt() {
        // CalcStmt -> CALC_BEGIN (Expr;) + CALC_END
        this.check(TokenCode.CALC_BEGIN);
        ArrayList<Expression> expressions = new ArrayList<>();
        while (this.firstMap.get("Expression").contains(this.sym)){
            expressions.add(this.Expr());
            this.check(TokenCode.SEMICOLON);
        }
        this.check(TokenCode.CALC_END);
        return new StatementCalc(expressions);
    }

    private ExpressionAssign AssignExpr() {
        // AssignExpr -> ident = Expr
        String identifier = this.la.string;
        this.check(TokenCode.IDENTIFIER);
        this.check(TokenCode.SINGLE_EQUALS);
        Expression expression = this.Expr();
        if (this.symbolTable.containsKey(identifier)) {
            this.symbolTable.get(identifier).expression = expression;
        }
        else {
            this.error("Identifier " + identifier + " has not been declared");
        }
        ExpressionAssignEnd end = this.AssignExprEnd();
        return new ExpressionAssign(identifier, expression, end);
    }

    private ExpressionAssignEnd AssignExprEnd() {
        if (this.sym == TokenCode.QUESTION_MARK) {
            this.scan();
            Expression first = this.Expr();
            this.check(TokenCode.COLON);
            Expression second = this.Expr();
            return new ExpressionAssignEnd(first, second);
        }
        else {
            return null;
        }
    }

    private Expression Expr() {
        // Expr -> Expr2 Expr'
        Expression left = this.Expr2();
        return this.ExprPrim(left);
    }

    private Expression ExprPrim(Expression left) {
        // Expr' -> Logical Expr2 Expr' | eps
        if (this.sym == TokenCode.AND || this.sym == TokenCode.OR) {
            // Logical -> || | &&
            String operator = this.sym == TokenCode.OR ? "||" : "&&";
            this.scan();
            Expression right = this.Expr2();
            return this.ExprPrim(new Expression(operator, left, right, StatementKind.BINARY_EXPR));
        }
        // eps
        else {
            return left;
        }
    }

    private Expression Expr2() {
        // Expr2 -> Expr3 EndE2
        Expression left = this.Expr3();
        return this.EndE2(left);
    }

    private Expression EndE2(Expression left) {
        // EndE2 -> Equality Expr3 | eps
        if (this.sym == TokenCode.DOUBLE_EQUALS || this.sym == TokenCode.NOT_EQUALS) {
            // Equality -> == | !=
            String operator = this.sym == TokenCode.DOUBLE_EQUALS ? "==" : "!=";
            this.scan();
            Expression right = this.Expr3();
            return new Expression(operator, left, right, StatementKind.BINARY_EXPR);
        }
        else {
            return left;
        }
    }

    private Expression Expr3() {
        // Expr3 -> Expr4 EndE3
        Expression left = this.Expr4();
        return this.EndE3(left);
    }

    private Expression EndE3(Expression left) {
        // EndE3 -> Compare Expr4 | eps
        if (this.firstMap.get("Compare").contains(this.sym)) {
            // Compare -> < | <= | > | >=
            String operator = switch (this.sym) {
                case LESS -> "<";
                case LESS_EQUAL -> "<=";
                case GREATER -> ">";
                case GREATER_EQUAL -> ">=";
                default -> "";
            };
            this.scan();
            Expression right = this.Expr4();
            return new Expression(operator, left, right, StatementKind.BINARY_EXPR);
        }
        else {
            return left;
        }
    }

    private Expression Expr4() {
        // Expr4 -> Expr5 Expr4'
        Expression left = this.Expr5();
        return this.Expr4Prim(left);
    }

    private Expression Expr4Prim(Expression left) {
        // Expr4' -> AddSub Expr5 Expr4' | eps
        if (this.sym == TokenCode.PLUS || this.sym == TokenCode.MINUS) {
            // AddSub -> + | -
            String operator = this.sym == TokenCode.PLUS ? "+" : "-";
            this.scan();
            Expression right = this.Expr5();
            return this.Expr4Prim(new Expression(operator, left, right, StatementKind.BINARY_EXPR));
        }
        else {
            return left;
        }
    }

    private Expression Expr5() {
        // Expr5 -> Expr6 Expr5'
        Expression left = this.Expr6();
        return this.Expr5Prim(left);
    }

    private Expression Expr5Prim(Expression left) {
        // Expr5' -> MulDivMod Expr6 Expr5' | eps
        if (this.sym == TokenCode.MULTIPLY || this.sym == TokenCode.DIVIDE || this.sym == TokenCode.MOD) {
            // MulDivMod -> * | / | %
            String operator = switch (this.sym) {
                case MULTIPLY -> "*";
                case DIVIDE -> "/";
                case MOD -> "%";
                default -> "";
            };
            this.scan();
            Expression right = this.Expr6();
            return this.Expr5Prim(new Expression(operator, left, right, StatementKind.BINARY_EXPR));
        }
        else {
            return left;
        }
    }

    private Expression Expr6() {
        // Expr6 -> ! Expr7 | - Expr7 | Expr7
        String operator = "";
        if (this.sym == TokenCode.NOT || this.sym == TokenCode.MINUS) {
            operator = this.sym == TokenCode.NOT ? "!" : "-";
            this.scan();
        }
        Expression expr = this.Expr7();
        if (operator.equals("")) {
            return expr;
        }
        else {
            return new Expression(operator, expr, null, StatementKind.UNARY_EXPR);
        }
    }

    private Expression Expr7() {
        // Expr7 -> Constant | ident | ( Expr ) | READINT () | READSTRING () | READDOUBLE () | READBOOL ()
        if (this.firstMap.get("Constant").contains(this.sym)) {
            // Constant -> integerConstant | boolConstant | stringConstant | doubleConstant
            Expression constant = new Expression("", null, null, StatementKind.CONSTANT);
            constant.value = switch (this.sym) {
                case INTEGER_CONSTANT -> this.la.intVal; // integer value
                case DOUBLE_CONSTANT -> this.la.doubleVal; // double value
                case BOOL_CONSTANT, STRING_CONSTANT -> this.la.string; // true/false/someString
                default -> "No value";
            };
            this.scan();
            return constant;
        }
        else if (this.sym == TokenCode.IDENTIFIER) {
            Expression result = new Expression("", null, null, StatementKind.IDENTIFIER);
            String identifier = this.la.string;
            result.value = identifier; // identifier name
            this.scan();
            if (!this.symbolTable.containsKey(identifier)) {
                this.error("Identifier " + identifier + " has not been declared");
            }
            return result;
        }
        else if (this.sym == TokenCode.LEFT_REGULAR) {
            this.scan();
            Expression result = this.Expr();
            this.check(TokenCode.RIGHT_REGULAR);
            return result;
        }
        else if (this.firstMap.get("ReadOperations").contains(this.sym)) {
            // read operation
            Expression read = new Expression("", null, null, StatementKind.READ_OPERATION);
            read.value = this.sym.toString(); // TokenCode.READ*
            this.scan();
            this.check(TokenCode.LEFT_REGULAR);
            this.check(TokenCode.RIGHT_REGULAR);
            return read;
        }
        else {
            this.error("Expression expected");
            this.scan();
            return null;
        }
    }
}
