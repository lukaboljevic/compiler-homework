public enum TokenCode {
    // Keywords
    NONE,
    EOF,
    LET,
    IN,
    END,
    IF,
    FI,
    ELSE,
    WHILE,
    FOR,
    BREAK,
    PRINT,
    READINT,
    READSTRING,
    READBOOL,
    READDOUBLE,

    // Identifier and data types
    IDENTIFIER,
    INTEGER_TYPE,
    BOOL_TYPE,
    STRING_TYPE,
    DOUBLE_TYPE,

    // Operators, braces, special characters
    PLUS,
    MINUS,
    MULTIPLY,
    DIVIDE,
    MOD,
    LESS,
    LESS_EQUAL,
    GREATER,
    GREATER_EQUAL,
    SINGLE_EQUALS,
    DOUBLE_EQUALS,
    NOT_EQUALS,
    AND,
    OR,
    NOT,
    SEMICOLON,
    LEFT_REGULAR,
    RIGHT_REGULAR,
    LEFT_CURLY,
    RIGHT_CURLY,

    // Constants
    INTEGER_CONSTANT,
    DOUBLE_CONSTANT,
    STRING_CONSTANT,
    BOOL_CONSTANT,
}
