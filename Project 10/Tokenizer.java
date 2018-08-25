import java.io.BufferedReader;
import java.io.FileReader;

/**
 * Class generates tokens from the given jack file
 */


class Tokenizer {

    final String EMPTY_STRING = "",
                 WHITE_SPACE = " ";

    final static String[] KEYWORDS = new String[]
    {
        "class", "constructor", "function", "method", "field", "static", "var", "int",
        "char", "boolean", "void", "true", "false", "null", "this", "let", "do", "if",
        "else", "return", "while"
    };

    final static String[] SYMBOLS = new String[]
    {
        "{","}","(",")","[","]",".",",",";",
        "+","-","*","/","&","|",
        "<",">","=","~"
    };

    private List<String> tokens;

    private Token currentToken;

    private BufferedReader reader;

    public Tokenizer(String inputFileName) {
        this.reader = new BufferedReader(new FileReader(inputFileName));
        this.tokens = new ArrayList<>();
    }


    private void tokenize() {

        String line = reader.readLine();

        while(line != null) {

            line = line.replaceAll("//.*", EMPTY_STRING)    // remove line comments
                        .replaceAll("/\\*(\\*(?!/)|[^*])*\\*/", EMPTY_STRING) // remove block comments
                        .trim();    // remove extra spaces

            if(!line.isEmpty()) {

                // add additional spaces before and after symbol (regardless of the spacing already exists or not)
                // this helps in tokenizing using spaces
                for(String symbol : SYMBOLS) {
                    line = line.replaceAll(symbol, WHITE_SPACE + symbol + WHITE_SPACE);
                }

                tokens.addAll(line.split(WHITE_SPACE));

            }
        }

    }

    /**
        Method returns true if there are more tokens to be produced
     */
    public Boolean hasMoreTokens() {
        return tokens.size() > 0;
    }

    /**
        Method retrieves the token to be processed and sets the pointer to the next token
     */
    public void advance() {
        String token = tokens.pop();

        if(KEYWORDS.contains(token)) {
            currentToken = new Token(token, Token.TokenType.KEYWORD);
        }
        else if(SYMBOLS.contains(token)) {
            currentToken = new Token(token, Token.TokenType.SYMBOL);
        }
        else if(token.matches("\d+")) {
            currentToken = new Token(token, Token.TokenType.INT_CONST);
        }
        else if(token.startsWith()) {
            currentToken = new Token(token, Token.TokenType.STRING_CONST);
        }
        else {
            currentToken = new Token(token, Token.TokenType.IDENTIFIER);
        }
    }

    /**
     */
    public Token getToken() {
        return currentToken;
    }


}