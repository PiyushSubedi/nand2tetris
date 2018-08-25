import java.io.BufferedReader;
import java.io.FileReader;

/**
 * Class generates tokens from the given jack file
 */


class Tokenizer {

    final String EMPTY_STRING = "";

    private BufferedReader reader;

    public Tokenizer(String inputFileName) {
        this.reader = new BufferedReader(new FileReader(inputFileName));
    }


    private void tokenize() {

        String line = reader.readLine();

        while(line != null) {

            line = line.replaceAll("//.*", EMPTY_STRING)    // remove line comments
                        .replaceAll("/\\*.*\\*/", EMPTY_STRING) // remove block comments
                        .trim();    // remove extra spaces

            

        }

    }


    public Boolean hasMoreTokens() {
        tokenize();
        return false;
    }



    

}