
import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;

public class OurScanner {
    private final char eofCh = '\u0080';
    private final char eol = '\n';
    private final int  // token codes
    none_ = 0, eof_ = 1, let_ = 2, in_ = 3, end_ = 4, if_ = 5, fi_ = 6, else_ = 7, while_ = 8, for_ = 9,
    break_ = 10, print_ = 11, then_ = 12, // then isn't a keyword but I couldn't be bothered removing it
    readint_ = 13, readstring_ = 14, readbool_ = 15, readdouble_ = 16,

    identifier_ = 17, integer_ = 18, bool_ = 19, string_ = 20, double_ = 21,

    plus_ = 22, minus_ = 23, mult_ = 24, div_ = 25, mod_ = 26, less_ = 27, lessequal_ = 28, greater_ = 29,
    greaterequal_ = 30, assign_ = 31, equal_ = 32, notequal_ = 33, and_ = 34, or_ = 35, not_ = 36, semicolon_ = 37,
    comma_ = 38, period_ = 39, leftpar_ = 40, rightpar_ = 41,
    // comma and period aren't really used, but again, couldn't be bothered removing them
    integerConstant_ = 42, doubleConstant_ = 43, stringConstant_ = 44, boolConstant_ = 45,

    leftCurly_ = 46, rightCurly_ = 47;

    private final HashMap<String, Integer> keywords;  // map for keywords
    private final HashMap<String, Integer> tokensMap;  // map for tokens
    private final HashMap<String, Integer> dataTypes;  // map for data types
    private final String intRegex = "\\d+";  // regular expression for integers
    private final String hexRegex = "^(0[xX])[a-fA-F\\d]+";  // regular expression for hex integers
    private final String doubleRegex = "\\d+\\.\\d*([eE][+-]?\\d+)?";  // regular expression for doubles

    private char lookahead;  // lookahead character
    private int col;  // current column
    private int line; // current line
    private final Reader reader;  // source file reader
//    private int pos; // current position from start of source file
//    private static char[] lex;  // current lexeme (token string)


    //--------- Initialize scanner
    public OurScanner(Reader r) {
        this.reader = new BufferedReader(r);
//        lex = new char[64];
        this.line = 1;
        this.col = 0;

        // Initialize the necessary maps and fill them
        this.tokensMap = new HashMap<>();
        this.fillTokensMap();
        this.keywords = new HashMap<>();
        this.fillKeywordsMap();
        this.dataTypes = new HashMap<>();
        this.fillDatatypesMap();

        // start scanning
        this.nextCharacter();
    }

    // lookahead = next input character
    private void nextCharacter() {
        try {
            this.lookahead = (char) this.reader.read();  // read a single character
            this.col++;
            // this.pos++;
            if (this.lookahead == this.eol) {
                this.line++;
                this.col = 0;
            } else if (this.lookahead == '\uffff')
                this.lookahead = this.eofCh;
        } catch (IOException e) {
            this.lookahead = this.eofCh;
        }
    }

    private void readNumber(Token t) {
        /*
        Read a number constant. Check it's validity using the
        defined regular expressions.
         */
        t.string = Character.toString(this.lookahead);  // we already found one character of t.string in nextToken()
        this.nextCharacter();
        while (Character.isDigit(lookahead) || lookahead == '.' || Character.toString(lookahead).equalsIgnoreCase("x") ||
                ('a' <= lookahead && lookahead <= 'f') || ('A' <= lookahead && lookahead <= 'F') || lookahead == '+' || lookahead == '-') {
            t.string += Character.toString(this.lookahead);
            this.nextCharacter();
        }

        if (t.string.matches(this.intRegex) || t.string.matches(this.hexRegex)) {
            t.kind = this.integerConstant_;
            if (t.string.startsWith("0X") || t.string.startsWith("0x")) {
                // hex integer, parseInt function does not need the 0x/0X to parse it.
                t.intVal = Integer.parseInt(t.string.substring(2), 16);
            } else {
                // regular decimal integer
                t.intVal = Integer.parseInt(t.string);
            }
        } else if (t.string.matches(doubleRegex)) {
            // double value
            t.kind = this.doubleConstant_;
            t.doubleVal = Double.parseDouble(t.string);
        } else {
            System.out.println("Scanner -- line " + this.line + " col " + this.col + ": Invalid number constant");
            t.kind = this.none_;
        }
    }

    private void readName(Token t) {
        /*
        Read a keyword, data type, boolean constant or an identifier,
        depending on the contents of t.string after the initial while loop
         */

        t.string = Character.toString(this.lookahead);  // we already found one character of t.string in nextToken()
        this.nextCharacter();
        while (Character.isLetterOrDigit(this.lookahead) || this.lookahead == '_') {
            t.string += Character.toString(this.lookahead);
            this.nextCharacter();
        }
        if (t.string.compareTo("true") == 0 || t.string.compareTo("false") == 0) {
            t.kind = this.boolConstant_;
            return;
        }

        // Check if it's a keyword
        for (String keyword : this.keywords.keySet()) {
            if (t.string.compareTo(keyword) == 0) {
                if (t.string.startsWith("READ")) {
                    // READ operation, check for it's validity as well
                    // they have to be of the form READ[SOMETHING]() -> if the brackets are missing, it's an error
                    if (this.lookahead == '(') {
                        this.nextCharacter();
                        if (this.lookahead == ')') {
                            this.nextCharacter();
                            t.kind = this.keywords.get(keyword); // keyword = READ[INT/STRING/BOOL/DOUBLE]
                            return;
                        } else {
                            System.out.println("Scanner -- line " + this.line + " col " + this.col + ": " +
                                    "Invalid READ operation expression, missing )");
                            t.kind = this.none_;
                            return;
                        }
                    } else {
                        System.out.println("Scanner -- line " + this.line + " col " + this.col + ": " +
                                "Invalid READ operation expression, missing (");
                        t.kind = this.none_;
                        return;
                    }
                } else {
                    // not a READ operation but is another keyword
                    t.kind = this.keywords.get(keyword);
                    return;
                }
            }
        }

        // Check for data types
        for (String type : this.dataTypes.keySet()) {
            if (t.string.compareTo(type) == 0) {
                t.kind = this.dataTypes.get(type);
                return;
            }
        }

        t.kind = this.identifier_;  // it's length will be checked by the Parser class
    }

    private void readString(Token t) {
        /*
        Read a string constant
         */
        this.nextCharacter();  // cause we had to get here when a " was scanned, so we need to skip it.
        t.string = "";
        while (this.lookahead != '\"' && this.lookahead != '\n') {
            t.string += Character.toString(this.lookahead);
            this.nextCharacter();
        }
        if (this.lookahead == '\"') {
            // it ended with a " before we found a \n so it's a valid constant
            t.kind = this.stringConstant_;
        } else {
            t.kind = this.none_;
            System.out.println("Scanner -- line: " + this.line + " col: " + this.col + ": found a new line, " +
                    "either the string contains it or the string constant is not properly closed");
        }
        this.nextCharacter();
    }

    public Token nextToken() {
        /*
        Return the next input token
         */
        while (this.lookahead <= ' ') {
            this.nextCharacter();  // skip blanks, tabs, eols
        }
        Token t = new Token();
        t.line = this.line;
        t.col = this.col;

        // Based on the lookahead character, perform the necessary operation(s)
        if (('a' <= this.lookahead && this.lookahead <= 'z') || ('A' <= this.lookahead && this.lookahead <= 'Z')) {
            this.readName(t);
        } else if ('0' <= this.lookahead && this.lookahead <= '9') {
            this.readNumber(t);
        } else {
            String chString = Character.toString(this.lookahead);
            switch (this.lookahead) {
                case '\"':
                    this.readString(t);
                    break;
                case '+':
                case '-':
                case '*':
                case ';':
                case '.':
                case ',':
                case '(':
                case ')':
                case '%':
                case '{':
                case '}':
                    this.nextCharacter();  // continue scanning
                    t.kind = this.tokensMap.get(chString);
                    t.string = chString;
                    break;

                case '/':
                    // can be a single or multi line comment, or just regular division.
                    this.nextCharacter();
                    if (this.lookahead == '/') {  // single line comment
                        this.nextCharacter();
                        while (this.lookahead != this.eol && this.lookahead != this.eofCh) {
                            this.nextCharacter();
                        }
                        this.nextCharacter();
                        t = null;  // set it to null so the parser can completely skip it
                    } else if (lookahead == '*') {  // multi line comment
                        char prev;
                        this.nextCharacter();
                        while (this.lookahead != this.eofCh && this.lookahead != '/') {
                            prev = this.lookahead;
                            this.nextCharacter();
                            if (this.lookahead == '/') {
                                if (prev == '*') {
                                    break;
                                } else
                                    this.nextCharacter();
                                while (this.lookahead == '/') this.nextCharacter();
                            }
                        }
                        if (this.lookahead == this.eofCh) {
                            t.kind = this.none_;
                        } else {
                            this.nextCharacter();
                        }
                        t = null;  // set it to null so the parser can completely skip it
                    } else {  // regular division
                        t.kind = this.div_;
                        t.string = chString;
                    }
                    break;

                case '<':
                case '>':
                case '=':
                case '!':
                    // check if it's <, >, =, ! or <=, >=, ==, !=
                    this.nextCharacter();
                    if (this.lookahead == '=') {
                        this.nextCharacter();  // continue scanning after the =
                        chString += "=";
                    }
                    t.kind = this.tokensMap.get(chString);
                    t.string = chString;
                    break;

                case '&':
                    // the next character must be a &; our language does not support bitwise operations
                    this.nextCharacter();
                    if (this.lookahead == '&') {
                        this.nextCharacter();
                        t.kind = this.and_;
                        t.string = "&&";
                    } else {
                        t.kind = this.none_;
                    }
                    break;

                case '|':
                    // the next character must be a |; our language does not support bitwise operations
                    this.nextCharacter();
                    if (this.lookahead == '|') {
                        this.nextCharacter();
                        t.kind = this.or_;
                        t.string = "||";
                    } else {
                        t.kind = this.none_;
                    }
                    break;

                case eofCh: // end of file
                    t.kind = this.eof_;
                    break;

                default:  // invalid character so just move on with the scanning process
                    this.nextCharacter();
                    t.kind = this.none_;
                    break;
            }
        }
//        if (t != null) {
//            System.out.println(t);
//        }
        return t;
    }

    private void fillTokensMap() {
        // Fill the tokens map; data types and keywords and the identifier token aren't included
        this.tokensMap.put("+", this.plus_);
        this.tokensMap.put("-", this.minus_);
        this.tokensMap.put("*", this.mult_);
        this.tokensMap.put("/", this.div_);
        this.tokensMap.put("%", this.mod_);
        this.tokensMap.put("<", this.less_);
        this.tokensMap.put("<=", this.lessequal_);
        this.tokensMap.put(">", this.greater_);
        this.tokensMap.put(">=", this.greaterequal_);
        this.tokensMap.put("=", this.assign_);
        this.tokensMap.put("==", this.equal_);
        this.tokensMap.put("!=", this.notequal_);
        this.tokensMap.put("&&", this.and_);
        this.tokensMap.put("||", this.or_);
        this.tokensMap.put("!", this.not_);
        this.tokensMap.put(";", this.semicolon_);
        this.tokensMap.put(".", this.period_);
        this.tokensMap.put(",", this.comma_);
        this.tokensMap.put("(", this.leftpar_);
        this.tokensMap.put(")", this.rightpar_);
        this.tokensMap.put("{", this.leftCurly_);
        this.tokensMap.put("}", this.rightCurly_);
    }

    private void fillKeywordsMap() {
        // Fill the keywords map
        this.keywords.put("LET", this.let_);
        this.keywords.put("IN", this.in_);
        this.keywords.put("END", this.end_);
        this.keywords.put("IF", this.if_);
        this.keywords.put("FI", this.fi_);
        this.keywords.put("ELSE", this.else_);
        this.keywords.put("WHILE", this.while_);
        this.keywords.put("FOR", this.for_);
        this.keywords.put("BREAK", this.break_);
        this.keywords.put("PRINT", this.print_);
        this.keywords.put("READINT", this.readint_);
        this.keywords.put("READSTRING", this.readstring_);
        this.keywords.put("READBOOL", this.readbool_);
        this.keywords.put("READDOUBLE", this.readdouble_);
    }

    private void fillDatatypesMap() {
        // Fill the data types map
        this.dataTypes.put("integer", this.integer_);
        this.dataTypes.put("string", this.string_);
        this.dataTypes.put("bool", this.bool_);
        this.dataTypes.put("double", this.double_);
    }
}
