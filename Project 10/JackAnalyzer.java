import java.io.IOException;

/**
 *  Main class that analyzes the jack program and
 *  outputs an intermediate XML based parse tree
 */


class JackAnalyzer {

    public static void main(String[] args) throws IOException {

        String inputFilename = args[0];

        Tokenizer tokenizer = new Tokenizer(inputFilename);

        while(tokenizer.hasMoreTokens()) {
            tokenizer.advance();
        }

        tokenizer.save();
        
        System.out.println("Done");

    }

}