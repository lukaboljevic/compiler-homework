import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class Parser {
    private final int  // token codes
    none_ = 0, eof_ = 1, let_ = 2, in_ = 3, end_ = 4, if_ = 5, fi_ = 6, else_ = 7, while_ = 8, for_ = 9,
    break_ = 10, print_ = 11, then_ = 12,
    readint_ = 13, readstring_ = 14, readbool_ = 15, readdouble_ = 16,

    identifier_ = 17, integer_ = 18, bool_ = 19, string_ = 20, double_ = 21,

    plus_ = 22, minus_ = 23, mult_ = 24, div_ = 25, mod_ = 26, less_ = 27, lessequal_ = 28, greater_ = 29,
    greaterequal_ = 30, assign_ = 31, equal_ = 32, notequal_ = 33, and_ = 34, or_ = 35, not_ = 36, semicolon_ = 37,
    comma_ = 38, period_ = 39, leftpar_ = 40, rightpar_ = 41,

    integerConstant_ = 42, doubleConstant_ = 43, stringConstant_ = 44, boolConstant_ = 45,

    leftCurly_ = 46, rightCurly_ = 47;

    private final String[] name = { // token names for error messages
            // THEN, . and the , are here because 1. I accidentally added then to the list of keywords
            // way before I realised it wasn't a keyword, and the . and , are there because of the consistency
            // in the numbering of the tokens
            "none", "eof", "LET", "IN", "END", "IF", "FI", "ELSE", "WHILE", "FOR", "BREAK", "PRINT", "THEN",
            "READINT()", "READSTRING()", "READBOOL()", "READDOUBLE()", "identifier", "integer", "bool", "string",
            "double", "+", "-", "*", "/", "%", "<", "<=", ">", ">=", "=", "==", "!=", "&&", "||", "!", ";", ",",
            ".", "(", ")", "integer constant", "double constant", "string constant", "bool constant", "{", "}"
    };

    private Token curr;        // current token (most recently recognized token)
    private Token la;        // lookahead token (still unrecognized)
    private int sym;            // always contains la.kind; token number of the lookahead token
    private int errDist;        // no. of correctly recognized tokens since last error
    private int errors;    // error counter

    // maps a nonterminal to it's FirstSet; the FirstSet is a list of codes from above
    private final HashMap<String, ArrayList<Integer>> firstMap;
    private final OurScanner scanner;

    public Parser(OurScanner s) {  // this is our Scanner, and not the built in one

        this.firstMap = new HashMap<>();
        this.firstMap.put("Declarations", new ArrayList<>(Arrays.asList(integer_, bool_, string_, double_)));
        this.firstMap.put("Type", new ArrayList<>(Arrays.asList(integer_, bool_, string_, double_)));
        this.firstMap.put("CommandSequence", new ArrayList<>(Arrays.asList(if_, while_, for_, break_, print_, identifier_,
                integerConstant_, boolConstant_, stringConstant_, doubleConstant_, leftpar_, readint_, readstring_,
                readdouble_, readbool_)));
        this.firstMap.put("Expr2", new ArrayList<>(Arrays.asList(identifier_, integerConstant_, boolConstant_,
                stringConstant_, doubleConstant_, leftpar_, readint_, readstring_, readdouble_, readbool_)));
        this.firstMap.put("LogicalOp", new ArrayList<>(Arrays.asList(and_, or_)));
        this.firstMap.put("EqualityOp", new ArrayList<>(Arrays.asList(equal_, notequal_)));
        this.firstMap.put("CompareOp", new ArrayList<>(Arrays.asList(less_, lessequal_, greater_, greaterequal_)));
        this.firstMap.put("AddOp", new ArrayList<>(Arrays.asList(plus_, minus_)));
        this.firstMap.put("MulOp", new ArrayList<>(Arrays.asList(mult_, div_, mod_)));
        this.firstMap.put("Constant", new ArrayList<>(Arrays.asList(integerConstant_, boolConstant_, stringConstant_, doubleConstant_)));
        this.firstMap.put("ReadOperations", new ArrayList<>(Arrays.asList(readint_, readstring_, readbool_, readdouble_)));

        // start parsing
        this.errors = 0;
        this.scanner = s;
        this.errDist = 3;
        this.scan();
        this.Program();
        if (this.sym != this.eof_) {
            error("end of file found before end of program");
        }
    }

    //------------------- Auxiliary methods ----------------------

    private void scan() {
        // scan for the next token
        this.curr = this.la;
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

    private void check(int expected) {
        // check if the kind of the lookahead token is the expected one
        if (this.sym == expected) {
            if (this.sym == this.identifier_ && this.la.string.length() > 31) {
                error("Identifier name too long (must be <= 31)");
            }
            this.scan();  // recognized => read ahead
        } else {
            this.error(this.name[expected] + " expected");
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

    private void Program() {
        // Program -> LET Declarations IN CommandSequence END
        this.check(let_);
        this.Declarations();
        this.check(in_);
        this.CommandSequence();
        this.check(end_);
    }

    private void Declarations() {
        // Declarations -> Decl+
        ArrayList<Integer> firstDeclarations = this.firstMap.get("Declarations");
        while (firstDeclarations.contains(this.sym)) {
            this.Decl();
        }
    }

    private void Decl() {
        // Decl -> Type ident ;
        this.Type();
        this.check(identifier_);
        this.check(semicolon_);
    }

    private void Type() {
        // Type -> integer | bool | string | double
        // moze i firstMap.get("Type"). ...
        switch (this.sym) {
            case integer_:
            case bool_:
            case string_:
            case double_:
                this.scan();
                break;
            default:
                this.error("variable type (integer, bool, string, double) expected");
                break;
        }
    }

    private void CommandSequence() {
        // CommandSequence -> {Stmt+}
        ArrayList<Integer> firstCommandSequence = this.firstMap.get("CommandSequence");
        check(this.leftCurly_);
        while (firstCommandSequence.contains(this.sym))
            this.Stmt();
        check(this.rightCurly_);

    }

    private void Stmt() {
        /*
        Stmt -> IfStmt  | WhileStmt |  ForStmt |
			    BreakStmt  | PrintStmt | AssignExpr ; | Expr2 ;
         */
        if (this.sym == this.if_)
            this.IfStmt();
        else if (this.sym == this.while_)
            this.WhileStmt();
        else if (this.sym == this.for_)
            this.ForStmt();
        else if (this.sym == this.break_)
            this.BreakStmt();
        else if (this.sym == this.print_)
            this.PrintStmt();
        else if (this.sym == this.identifier_) {
            this.AssignExpr();
            this.check(semicolon_);
        } else if (this.firstMap.get("Expr2").contains(this.sym)) {
            this.Expr2();
            this.check(semicolon_);
        } else {
            this.error("Statement error");
        }
    }

    private void IfStmt() {
        // IfStmt -> IF ( Expr2 ) CommandSequence IfStmtEnd
        this.check(this.if_);
        this.check(this.leftpar_);
        this.Expr2();
        this.check(this.rightpar_);
        this.CommandSequence();
        this.IfStmtEnd();
    }

    private void IfStmtEnd() {
        // IfStmtEnd -> ELSE CommandSequence FI | FI
        if (this.sym == this.else_) {
            this.scan();
            this.CommandSequence();
            this.check(this.fi_);
        } else if (this.sym == this.fi_) {
            this.scan();
        } else {
            this.error("Invalid end for IF statement");
        }
    }

    private void WhileStmt() {
        // WhileStmt -> WHILE ( Expr2 ) CommandSequence
        this.check(this.while_);
        this.check(this.leftpar_);
        this.Expr2();
        this.check(this.rightpar_);
        this.CommandSequence();
    }

    private void ForStmt() {
        // ForStmt -> FOR ( AssignExpr ; Expr2 ; AssignExpr ) CommandSequence
        this.check(this.for_);
        this.check(this.leftpar_);
        this.AssignExpr();
        this.check(this.semicolon_);
        this.Expr2();
        this.check(this.semicolon_);
        this.AssignExpr();
        this.check(this.rightpar_);
        this.CommandSequence();
    }

    private void BreakStmt() {
        // BreakStmt ->  BREAK ;
        this.check(this.break_);
        this.check(this.semicolon_);
    }

    private void PrintStmt() {
        // PrintStmt -> PRINT (Expr2) ;
        this.check(this.print_);
        this.check(this.leftpar_);
        this.Expr2();
        this.check(this.rightpar_);
        this.check(this.semicolon_);
    }

    private void AssignExpr() {
        // AssignExpr -> ident = Expr2
        this.check(this.identifier_);
        this.check(this.assign_);
        this.Expr2();
    }

    private void Expr2() {
        // Expr2 -> Expr3 Expr2'
        this.Expr3();
        this.Expr2p();
    }

    private void Expr2p() {
        // Expr2' -> LogicalOp Expr3 Expr2' | eps
        // eps means do nothing basically
        if (this.firstMap.get("LogicalOp").contains(this.sym)) {
            // LogicalOp -> || | &&
            this.scan();
            this.Expr3();
            this.Expr2p();
        }
    }

    private void Expr3() {
        // Expr3 -> Expr4 ENDEXPR3
        this.Expr4();
        this.EndExpr3();
    }

    private void EndExpr3() {
        // ENDEXPR3	-> EqualityOp Expr4 | eps
        if (this.firstMap.get("EqualityOp").contains(this.sym)) {
            // EqualityOp -> == | !=
            this.scan();
            this.Expr4();
        }
    }

    private void Expr4() {
        // Expr4 -> Expr5 ENDEXPR4
        this.Expr5();
        this.EndExpr4();
    }

    private void EndExpr4() {
        // ENDEXPR4	-> CompareOp Expr5 | eps
        if (this.firstMap.get("CompareOp").contains(this.sym)) {
            // CompareOp -> < | <= | > | >=
            this.scan();
            this.Expr5();
        }
    }

    private void Expr5() {
        // Expr5 -> Expr6 Expr5'
        this.Expr6();
        this.Expr5p();
    }

    private void Expr5p() {
        // Expr5' -> AddOp Expr6 Expr5' | eps
        if (this.firstMap.get("AddOp").contains(this.sym)) {
            // AddOp -> + | -
            this.scan();
            this.Expr6();
            this.Expr5p();
        }
    }

    private void Expr6() {
        // Expr6 -> Expr7 Expr6'
        this.Expr7();
        this.Expr6p();
    }

    private void Expr6p() {
        // Expr6' -> MulOp Expr7 Expr6' | eps
        if (this.firstMap.get("MulOp").contains(this.sym)) {
            // MulOp -> * | / | %
            this.scan();
            this.Expr7();
            this.Expr6p();
        }
    }

    private void Expr7() {
        // Expr7 -> ! Expr8 | - Expr8 | Expr8
        if (this.sym == this.not_ || this.sym == this.minus_) {
            this.scan();
        }
        this.Expr8();
    }

    private void Expr8() {
        // Expr8 -> Constant | ident | ( Expr2 ) | READINT ( ) | READSTRING ( ) | READDOUBLE (  ) | READBOOL ( )
        if (this.firstMap.get("Constant").contains(this.sym)) {
            // Constant -> intConstant | boolConstant | stringConstant | doubleConstant
            this.scan();
        } else if (this.sym == this.identifier_)
            this.scan();
        else if (this.sym == this.leftpar_) {
            this.scan();
            this.Expr2();
            this.check(this.rightpar_);
        } else if (this.firstMap.get("ReadOperations").contains(this.sym)) {
            // read operation
            this.scan();
        } else {
            this.error("Expression expected");
            this.scan();
        }
    }
}