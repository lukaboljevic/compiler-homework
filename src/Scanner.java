
import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;

public class Scanner {
    private final char eofCh = '\u0080';
    private final char eol = '\n';

    private final HashMap<String, TokenCode> keywords;  // map for keywords
    private final HashMap<String, TokenCode> tokensMap;  // map for tokens
    private final HashMap<String, TokenCode> dataTypes;  // map for data types
    private final String intRegex = "\\d+";  // regular expression for integers
    private final String hexRegex = "^(0[xX])[a-fA-F\\d]+";  // regular expression for hex integers
    private final String doubleRegex = "\\d+\\.\\d*([eE][+-]?\\d+)?";  // regular expression for doubles
    private final String identifierRegex = "^[a-zA-Z]\\w*";  // regular expression for identifier names
    // (\w is [a-zA-Z_0-9])

    private char lookahead;  // lookahead character
    private int col;  // current column
    private int line; // current line
    private final Reader reader;  // source file reader

    // --------- Initialize scanner
    public Scanner(Reader r) {
        this.reader = new BufferedReader(r);
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
            if (this.lookahead == this.eol) {
                this.line++;
                this.col = 0;
            }
            else if (this.lookahead == '\uffff')
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
            t.kind = TokenCode.INTEGER_CONSTANT;
            if (t.string.startsWith("0X") || t.string.startsWith("0x")) {
                // hex integer, parseInt function does not need the 0x/0X to parse it.
                t.intVal = Integer.parseInt(t.string.substring(2), 16);
            }
            else {
                // regular decimal integer
                t.intVal = Integer.parseInt(t.string);
            }
        }
        else if (t.string.matches(doubleRegex)) {
            // double value
            t.kind = TokenCode.DOUBLE_CONSTANT;
            t.doubleVal = Double.parseDouble(t.string);
        }
        else {
            System.out.println("Scanner -- line " + this.line + " col " + this.col + ": Invalid number constant");
            t.kind = TokenCode.NONE;
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
            t.kind = TokenCode.BOOL_CONSTANT;
            return;
        }

        // Check if it's a keyword
        for (String keyword : this.keywords.keySet()) {
            if (t.string.compareTo(keyword) == 0) {
                t.kind = this.keywords.get(keyword);
                return;
            }
        }

        // Check for data types
        for (String type : this.dataTypes.keySet()) {
            if (t.string.compareTo(type) == 0) {
                t.kind = this.dataTypes.get(type);
                return;
            }
        }

        if (t.string.matches(identifierRegex)) {
            t.kind = TokenCode.IDENTIFIER;  // it's length will be checked by the Parser class
        }
        else {
            System.out.println("Scanner -- line " + this.line + " col " + this.col + ": Invalid identifier " +
                    "name");
            t.kind = TokenCode.NONE;
        }
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
            t.kind = TokenCode.STRING_CONSTANT;
        }
        else {
            t.kind = TokenCode.NONE;
            System.out.println("Scanner -- line: " + this.line + " col: " + this.col + ": found a new line," +
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
        }
        else if ('0' <= this.lookahead && this.lookahead <= '9') {
            this.readNumber(t);
        }
        else {
            String lookaheadString = Character.toString(this.lookahead);
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
                    t.kind = this.tokensMap.get(lookaheadString);
                    t.string = lookaheadString;
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
                    }
                    else if (lookahead == '*') {  // multi line comment
                        char prev;
                        this.nextCharacter();
                        while (this.lookahead != this.eofCh && this.lookahead != '/') {
                            prev = this.lookahead;
                            this.nextCharacter();
                            if (this.lookahead == '/') {
                                if (prev == '*') {
                                    break;
                                }
                                else
                                    this.nextCharacter();
                                while (this.lookahead == '/') this.nextCharacter();
                            }
                        }
                        if (this.lookahead == this.eofCh) {
                            t.kind = TokenCode.NONE;
                        }
                        else {
                            this.nextCharacter();
                        }
                        t = null;  // set it to null so the parser can completely skip it
                    }
                    else {  // regular division
                        t.kind = TokenCode.DIVIDE;
                        t.string = lookaheadString;
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
                        lookaheadString += "=";
                    }
                    t.kind = this.tokensMap.get(lookaheadString);
                    t.string = lookaheadString;
                    break;

                case '&':
                    // the next character must be a &; our language does not support bitwise operations
                    this.nextCharacter();
                    if (this.lookahead == '&') {
                        this.nextCharacter();
                        t.kind = TokenCode.AND;
                        t.string = "&&";
                    }
                    else {
                        t.kind = TokenCode.NONE;
                    }
                    break;

                case '|':
                    // the next character must be a |; our language does not support bitwise operations
                    this.nextCharacter();
                    if (this.lookahead == '|') {
                        this.nextCharacter();
                        t.kind = TokenCode.OR;
                        t.string = "||";
                    }
                    else {
                        t.kind = TokenCode.NONE;
                    }
                    break;

                case eofCh: // end of file
                    t.kind = TokenCode.EOF;
                    break;

                default:  // invalid character so just move on with the scanning process
                    this.nextCharacter();
                    t.kind = TokenCode.NONE;
                    break;
            }
        }
        // print if not null, if necessary
        return t;
    }

    private void fillTokensMap() {
        // Fill the tokens map; data types and keywords and the identifier token aren't included
        this.tokensMap.put("+", TokenCode.PLUS);
        this.tokensMap.put("-", TokenCode.MINUS);
        this.tokensMap.put("*", TokenCode.MULTIPLY);
        this.tokensMap.put("/", TokenCode.DIVIDE);
        this.tokensMap.put("%", TokenCode.MOD);
        this.tokensMap.put("<", TokenCode.LESS);
        this.tokensMap.put("<=", TokenCode.LESS_EQUAL);
        this.tokensMap.put(">", TokenCode.GREATER);
        this.tokensMap.put(">=", TokenCode.GREATER_EQUAL);
        this.tokensMap.put("=", TokenCode.SINGLE_EQUALS);
        this.tokensMap.put("==", TokenCode.DOUBLE_EQUALS);
        this.tokensMap.put("!=", TokenCode.NOT_EQUALS);
        this.tokensMap.put("&&", TokenCode.AND);
        this.tokensMap.put("||", TokenCode.OR);
        this.tokensMap.put("!", TokenCode.NOT);
        this.tokensMap.put(";", TokenCode.SEMICOLON);
        this.tokensMap.put("(", TokenCode.LEFT_REGULAR);
        this.tokensMap.put(")", TokenCode.RIGHT_REGULAR);
        this.tokensMap.put("{", TokenCode.LEFT_CURLY);
        this.tokensMap.put("}", TokenCode.RIGHT_CURLY);
    }

    private void fillKeywordsMap() {
        // Fill the keywords map
        this.keywords.put("LET", TokenCode.LET);
        this.keywords.put("IN", TokenCode.IN);
        this.keywords.put("END", TokenCode.END);
        this.keywords.put("IF", TokenCode.IF);
        this.keywords.put("FI", TokenCode.FI);
        this.keywords.put("ELSE", TokenCode.ELSE);
        this.keywords.put("WHILE", TokenCode.WHILE);
        this.keywords.put("FOR", TokenCode.FOR);
        this.keywords.put("BREAK", TokenCode.BREAK);
        this.keywords.put("PRINT", TokenCode.PRINT);
        this.keywords.put("READINT", TokenCode.READINT);
        this.keywords.put("READSTRING", TokenCode.READSTRING);
        this.keywords.put("READBOOL", TokenCode.READBOOL);
        this.keywords.put("READDOUBLE", TokenCode.READDOUBLE);
        this.keywords.put("REPEAT", TokenCode.REPEAT);
        this.keywords.put("UNTIL", TokenCode.UNTIL);
    }

    private void fillDatatypesMap() {
        // Fill the data types map
        this.dataTypes.put("integer", TokenCode.INTEGER_TYPE);
        this.dataTypes.put("string", TokenCode.STRING_TYPE);
        this.dataTypes.put("bool", TokenCode.BOOL_TYPE);
        this.dataTypes.put("double", TokenCode.DOUBLE_TYPE);
    }
}
