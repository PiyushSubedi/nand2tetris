import java.io.PrintWriter;
import java.io.FileWriter;
import java.util.List;
import java.util.ArrayList;
import java.io.IOException;


public class XMLWriter {

    private final String WHITE_SPACE = " ";

    private List<String> contexts; // holds the names of child elements that are yet to be closed

    private PrintWriter writer;


    public XMLWriter(String outputFileName) throws IOException {
        this.writer = new PrintWriter(new FileWriter(outputFileName));
        this.contexts = new ArrayList<>();
    }


    public XMLWriter addElement(String elemName, String elemValue) {
        addContext(elemName);
        setValue(elemValue);
        return this;
    }

    public XMLWriter addElement(String elemName) {
        addContext(elemName);
        newLine();
        return this;
    }

    private void addContext(String elemName) {
        contexts.add(elemName);
        startTag();
    }

    private void setValue(String value) {
        writer.printf(WHITE_SPACE + value + WHITE_SPACE);
        closeTag();
    }


    private void startTag() {
        write("<" + contexts.get(contexts.size() - 1) + ">");
    }

    private void closeTag() {
        write("</" + contexts.remove(contexts.size() - 1) + ">");
        newLine();
    }

    private void closeAllTags() {
        int len = contexts.size();
        for(int i=0; i < len; i++) {
            closeTag();
        }
    }

    private void write(String text) {
        for(int i=0; i < contexts.size() - 1; i++) writer.printf("\t");
        writer.printf(text);
    }

    private void newLine() {
        writer.println();
    }


    public void close() {
        closeAllTags();

        writer.close();
    }


}