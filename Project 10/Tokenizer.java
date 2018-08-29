import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
        this.writer = new XMLWriter(inputFileName.replaceAll(".jack", "_MY_T.xml")).writeElement("tokens");
    }

    
    public void close() {
    	writer.close();
    }

    private void tokenize() {

        if(tokens.isEmpty()) {
            
            try {
				String line = reader.readLine();

				while(line != null) {

				    line = line.replaceAll("//.*", EMPTY_STRING)    // remove line comments
				    	        .trim();    // remove extra spaces
				    
				    // remove block comments
				    if(line.startsWith("/**") || line.startsWith("*")) {
				    	line = EMPTY_STRING;
				    }

				    if(!line.isEmpty()) {
				    	addTokensFrom(line);
				    	break;

				    }
				    
				    line = reader.readLine();
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }

    }
    
    
    /**
     * Method extracts token from the given line from a Jack program
     * and adds to the list of tokens
     * @param line
     */
    private void addTokensFrom(String line) {


        // We shall be splitting by space to get tokens

        // Strings inside " " will also be split by space which is not desirable
        // Hence storing the string beforehand
        List<String> stringConstants = new ArrayList<>();
        
    	Boolean insideString = false;
    	String temp = EMPTY_STRING;
        for(int i=0; i < line.length(); i++) {
        	if(line.charAt(i) == '"') {
        		if(insideString) {
        			// end of string detected
        			stringConstants.add("\"" + temp + "\"");
            		temp = EMPTY_STRING;
        		}
    			insideString = !insideString;
        	} else if(insideString) {
        		temp += line.charAt(i);
        	}
        }

    	// add additional spaces before and after symbol (regardless of the spacing already exists or not)
        // this helps in tokenizing using spaces
        for(String symbol : SYMBOLS) {                  	
            line = line.replace(symbol, WHITE_SPACE + symbol + WHITE_SPACE);
        }

        List<String> spaceSplitTokens = (Arrays.asList(line.split(WHITE_SPACE + "+")));

        // now remove all the space split string constants
        // and add the string constants saved earlier instead
        int index = 0;	// index for string constants list
        boolean skip = false;
        for(String token : spaceSplitTokens) {
        	
        	if(token.startsWith("\"") || token.endsWith("\"")) {
        		if(!skip) {
        			tokens.add(stringConstants.get(index++));	
        		}
        		skip = !skip;
        	} else if(!skip && !token.isEmpty()) {
        		tokens.add(token);
        	}
       	
        }
        
    }
    

    /**
        Method returns true if there are more tokens to be produced
     */
    public Boolean hasMoreTokens() {
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
            currentToken = Token.String_Const(token.replaceAll("\"", EMPTY_STRING));
        }
        else {
            currentToken = Token.Identifier(token);
        }

        writer.writeElement(currentToken.getType().toString(), currentToken.getValue());
    }

    /**
        Method returns the current token that has to be processed
     */
    public Token getToken() {
        return currentToken;
    }


}