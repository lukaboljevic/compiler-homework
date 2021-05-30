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
    private final Scanner scanner;

    public Parser(Scanner s) {

        this.firstMap = new HashMap<>();
        this.firstMap.put("Declarations", new ArrayList<>(Arrays.asList(TokenCode.INTEGER_TYPE,
                TokenCode.BOOL_TYPE, TokenCode.STRING_TYPE, TokenCode.DOUBLE_TYPE)));
        this.firstMap.put("CommandSequence", new ArrayList<>(Arrays.asList(TokenCode.IF, TokenCode.WHILE,
                TokenCode.FOR, TokenCode.BREAK, TokenCode.PRINT, TokenCode.IDENTIFIER,
                TokenCode.INTEGER_CONSTANT, TokenCode.BOOL_CONSTANT, TokenCode.STRING_CONSTANT,
                TokenCode.DOUBLE_CONSTANT, TokenCode.LEFT_REGULAR, TokenCode.READINT, TokenCode.READSTRING,
                TokenCode.READDOUBLE, TokenCode.READBOOL)));
        this.firstMap.put("Expr", new ArrayList<>(Arrays.asList(TokenCode.IDENTIFIER,
                TokenCode.INTEGER_CONSTANT, TokenCode.BOOL_CONSTANT,
                TokenCode.STRING_CONSTANT, TokenCode.DOUBLE_CONSTANT, TokenCode.LEFT_REGULAR,
                TokenCode.READINT, TokenCode.READSTRING, TokenCode.READDOUBLE, TokenCode.READBOOL)));
        this.firstMap.put("Compare", new ArrayList<>(Arrays.asList(TokenCode.LESS, TokenCode.LESS_EQUAL,
                TokenCode.GREATER, TokenCode.GREATER_EQUAL)));
        this.firstMap.put("Constant", new ArrayList<>(Arrays.asList(TokenCode.INTEGER_CONSTANT,
                TokenCode.BOOL_CONSTANT, TokenCode.STRING_CONSTANT, TokenCode.DOUBLE_CONSTANT)));
        this.firstMap.put("ReadOperations", new ArrayList<>(Arrays.asList(TokenCode.READINT,
                TokenCode.READSTRING, TokenCode.READBOOL, TokenCode.READDOUBLE)));

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
    }

    //------------------- Printing methods ----------------------

    private void printProgram(Program program) {
        System.out.println("DECLARATIONS\n");
        for (Declaration declaration : program.declarations) {
            System.out.printf("\tType: %s, Name: %s\n", declaration.type, declaration.identifier);
        }
        System.out.println("\nCOMMANDS\n");
        for (Statement statement : program.commandSequence.statements) {
            System.out.println(statement.kind);
        }
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
        } else {
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
        this.check(TokenCode.SEMICOLON);
        return new Declaration(type, identifier);
    }

    private String Type() {
        // Type -> integer | bool | string | double
        switch (this.sym) {
            case INTEGER_TYPE:
            case BOOL_TYPE:
            case DOUBLE_TYPE:
            case STRING_TYPE:
                String type = this.sym.toString();
                this.scan();
                return type;
            default:
                this.error("variable type (integer, bool, string, double) expected");
                return "Non existent variable type";
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
			    BreakStmt  | PrintStmt | AssignExpr ; | Expr ;
         */
        if (this.sym == TokenCode.IF) {
            return this.IfStmt();
        } else if (this.sym == TokenCode.WHILE) {
            return this.WhileStmt();
        } else if (this.sym == TokenCode.FOR) {
            return this.ForStmt();
        } else if (this.sym == TokenCode.BREAK) {
            return this.BreakStmt();
        } else if (this.sym == TokenCode.PRINT) {
            return this.PrintStmt();
        } else if (this.sym == TokenCode.IDENTIFIER) {
            ExpressionAssign assign = this.AssignExpr();
            this.check(TokenCode.SEMICOLON);
            return assign;
        } else if (this.firstMap.get("Expr").contains(this.sym)) {
            Expression expr = this.Expr();
            this.check(TokenCode.SEMICOLON);
            return expr;
        } else {
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
        } else if (this.sym == TokenCode.FI) {
            this.scan();
            return new StatementEndIf(null);
        } else {
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

    private ExpressionAssign AssignExpr() {
        // AssignExpr -> ident = Expr
        String identifier = this.la.string;
        this.check(TokenCode.IDENTIFIER);
        this.check(TokenCode.SINGLE_EQUALS);
        Expression expression = this.Expr();
        return new ExpressionAssign(identifier, expression);
    }

    private Expression Expr() {
        // Expr -> Expr2 Expr'
        Expression expr2 = this.Expr2();
        return this.ExprPrim(expr2);
    }

    private Expression ExprPrim(Expression expr2) {
        // Expr' -> Logical Expr2 Expr' | eps
        if (this.sym == TokenCode.AND || this.sym == TokenCode.OR) {
            // Logical -> || | &&
            String operator = this.sym == TokenCode.OR ? "||" : "&&";
            this.scan();
            Expression temp = this.Expr2();
            return this.ExprPrim(new Expression(operator, expr2, temp, StatementKind.BINARY_EXPR));
        }
        // eps
        else {
            return expr2;
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
        } else {
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
            String operator = this.sym.toString();
            this.scan();
            Expression right = this.Expr4();
            return new Expression(operator, left, right, StatementKind.BINARY_EXPR);
        } else {
            return left;
        }
    }

    private Expression Expr4() {
        // Expr4 -> Expr5 Expr4'
        Expression expr5 = this.Expr5();
        return this.Expr4Prim(expr5);
    }

    private Expression Expr4Prim(Expression expr5) {
        // Expr4' -> AddSub Expr5 Expr4' | eps
        if (this.sym == TokenCode.PLUS || this.sym == TokenCode.MINUS) {
            // AddSub -> + | -
            String operator = this.sym == TokenCode.PLUS ? "+" : "-";
            this.scan();
            Expression temp = this.Expr5();
            return this.Expr4Prim(new Expression(operator, expr5, temp, StatementKind.BINARY_EXPR));
        } else {
            return expr5;
        }
    }

    private Expression Expr5() {
        // Expr5 -> Expr6 Expr5'
        Expression expr6 = this.Expr6();
        return this.Expr5Prim(expr6);
    }

    private Expression Expr5Prim(Expression expr6) {
        // Expr5' -> MulDivMod Expr6 Expr5' | eps
        if (this.sym == TokenCode.MULTIPLY || this.sym == TokenCode.DIVIDE || this.sym == TokenCode.MOD) {
            // MulDivMod -> * | / | %
            String operator = "";
            switch (this.sym) {
                case MULTIPLY:
                    operator = "*";
                    break;
                case DIVIDE:
                    operator = "/";
                    break;
                case MOD:
                    operator = "%";
                    break;
            }
            this.scan();
            Expression temp = this.Expr6();
            return this.Expr5Prim(new Expression(operator, expr6, temp, StatementKind.BINARY_EXPR));
        } else {
            return expr6;
        }
    }

    private Expression Expr6() {
        // Expr6 -> ! Expr7 | - Expr7 | Expr7
        String operator = "";
        if (this.sym == TokenCode.NOT || this.sym == TokenCode.MINUS) {
            operator = this.sym == TokenCode.NOT ? "!" : "-";
            this.scan();
        }
        Expression expr7 = this.Expr7();
        if (operator.equals("")) {
            return expr7;
        } else {
            return new Expression(operator, expr7, null, StatementKind.UNARY_EXPR);
        }
    }

    private Expression Expr7() {
        // Expr7 -> Constant | ident | ( Expr ) | READINT () | READSTRING () | READDOUBLE () | READBOOL ()
        if (this.firstMap.get("Constant").contains(this.sym)) {
            // Constant -> integerConstant | boolConstant | stringConstant | doubleConstant
            String constantType = this.sym.toString();
            this.scan();
            return new Expression("", null, null, constantType);
        } else if (this.sym == TokenCode.IDENTIFIER) {
            String identifier = this.la.string;
            this.scan();
            Expression result = new Expression("", null, null, "Identifier");
            result.value = identifier;
            return result;
        } else if (this.sym == TokenCode.LEFT_REGULAR) {
            this.scan();
            Expression result = this.Expr();
            this.check(TokenCode.RIGHT_REGULAR);
            return result;
        } else if (this.firstMap.get("ReadOperations").contains(this.sym)) {
            // read operation
            String readType = this.sym.toString();
            this.scan();
            return new Expression("", null, null, readType);
        } else {
            this.error("Expression expected");
            this.scan();
            return null;
        }
    }
}