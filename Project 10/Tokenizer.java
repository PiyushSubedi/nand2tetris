import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Class generates tokens from the given jack file
 */


class Tokenizer {

    final String EMPTY_STRING = "",
                 WHITE_SPACE = " ";


    static Set<String> KEYWORDS = new HashSet<String>(Arrays.asList(
        new String[]
        {
            "class", "constructor", "function", "method", "field", "static", "var", "int",
            "char", "boolean", "void", "true", "false", "null", "this", "let", "do", "if",
            "else", "return", "while"
        }
    ));

    static Set<String> SYMBOLS = new HashSet<String>(Arrays.asList(
        new String[]
        {
            "{","}","(",")","[","]",".",",",";",
            "+","-","*","/","&","|",
            "<",">","=","~"
        }
    ));

    private List<String> tokens;

    private Token currentToken;

    private BufferedReader reader;

    private XMLWriter writer;

    public Tokenizer(String inputFileName) throws IOException {
        this.reader = new BufferedReader(new FileReader(inputFileName));
        this.tokens = new ArrayList<>();
        this.writer = new XMLWriter(inputFileName.replaceAll(".jack", "T.xml")).addElement("tokens");
    }


    private void tokenize() throws IOException {

        if(tokens.isEmpty()) {
            
            String line = reader.readLine();

            while(line != null) {

                line = line.replaceAll("//.*", EMPTY_STRING)    // remove line comments
                            .replaceAll("/\\*(\\*(?!/)|[^*])*\\*/", EMPTY_STRING) // remove block comments
                            .trim();    // remove extra spaces

                if(!line.isEmpty()) {
                	addTokensFrom(line);
                	break;

                }
                
                line = reader.readLine();
            }
        }

    }
    
    
    /**
     * Method extracts token from the given line from a Jack program
     * and adds to the list of tokens
     * @param line
     */
    private void addTokensFrom(String line) {

    	// add additional spaces before and after symbol (regardless of the spacing already exists or not)
        // this helps in tokenizing using spaces
        for(String symbol : SYMBOLS) {                  	
            line = line.replace(symbol, WHITE_SPACE + symbol + WHITE_SPACE);
        }
        
        List<String> spaceSplitTokens = (Arrays.asList(line.split(WHITE_SPACE + "+")));

        
        String stringPattern = "\"[^\"]+\"";

        
        
        
        // By splitting by space, we made an error
        // Strings inside " " will also be split which is not desirable
        // Hence fixing this issue with this additional hassle
        Boolean isString = false;
        for(String spaceSplitToken : spaceSplitTokens) {
        	if(spaceSplitToken.startsWith("\"")) {
        		isString = true;
        	} else if(spaceSplitToken.endsWith("\"")) {
        		isString = false;
        	} else if(!isString) {
        		tokens.add(spaceSplitToken);
        	}
        }
        
    }
    

    /**
        Method returns true if there are more tokens to be produced
     */
    public Boolean hasMoreTokens() throws IOException {
        tokenize();
        return tokens.size() > 0;
    }

    /**
        Method retrieves the token to be processed and sets the pointer to the next token
     */
    public void advance() {
        String token = tokens.remove(0);

        if(KEYWORDS.contains(token)) {
            currentToken = Token.Keyword(token);
        }
        else if(SYMBOLS.contains(token)) {
            currentToken = Token.Symbol(token);
        }
        else if(token.matches("\\d+")) {
            currentToken = Token.Int_Const(token);
        }
        else if(token.startsWith("\"")) {
            currentToken = Token.String_Const(token.replace("\"", EMPTY_STRING));
        }
        else {
            currentToken = Token.Identifier(token);
        }

        writer.addElement(currentToken.getType().toString(), token);
    }

    /**
        Method returns the current token that has to be processed
     */
    public Token getToken() {
        return currentToken;
    }



    public void save() throws IOException {
        this.writer.close();
        this.reader.close();
    }

}