/**
 * Class represents a Lexical Token in JACK
 */

class Token {

    public enum TokenType { KEYWORD, SYMBOL, IDENTIFIER, INT_CONST, STRING_CONST };

    private TokenType type;

    private String value;

    public Token(String value, TokenType type) {
        this.value = value;
        this.type = type;
    }


    public TokenType type() {
        return this.type;
    }

    public String value() {
        return this.value;
    }


}