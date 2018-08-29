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


    public XMLWriter writeElement(String elemName, String elemValue) {
        addContext(elemName);
        setValue(elemValue);
        closeTag();
        return this;
    }

    public XMLWriter writeElement(String elemName) {
        addContext(elemName);
        newLine();
        return this;
    }

    
    public XMLWriter closeElement() {
    	closeTagln();
    	return this;
    }
    
    private void addContext(String elemName) {
        contexts.add(elemName);
        startTag();
    }

    private void setValue(String value) {
        writer.printf(encode(WHITE_SPACE + value + WHITE_SPACE));
    }


    private void startTag() {
        write("<" + contexts.get(contexts.size() - 1) + ">");
    }

    private void closeTag() {
        writer.printf("</" + contexts.get(contexts.size() - 1) + ">");
        removeContext();
        newLine();
    }
    
    private void closeTagln() {
    	write("</" + contexts.get(contexts.size() - 1) + ">");
    	removeContext();
        newLine();
    }
    
    private void removeContext() {
    	contexts.remove(contexts.size() - 1);
    }

    private void closeAllTags() {
        int len = contexts.size();
        for(int i=0; i < len; i++) {
            closeTagln();
        }
    }

    private void write(String text) {
        for(int i=1; i <= contexts.size() - 1; i++) writer.printf("\t");
        writer.printf(text);
    }
    
    private String encode(String text) {
    	return text.replace("&", "&amp;")
    				.replace(">", "&gt;")
    				.replace("<", "&lt;");
    				
    }

    private void newLine() {
        writer.println();
    }


    public void close() {
        closeAllTags();

        writer.close();
    }


}