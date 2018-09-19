import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *  Main class that analyzes the jack program and
 *  outputs an intermediate XML based parse tree
 */


class JackCompiler {

    public static void main(String[] args) throws IOException {

        String inputFileName = args[0];

        List<String> inputFileNames = new ArrayList<>();
        
        File inputFile = new File(inputFileName);
        
        if(inputFile.isDirectory()) {
        	for(File f : inputFile.listFiles(new FilenameFilter() {
				
				@Override
				public boolean accept(File arg0, String arg1) {
					// TODO Auto-generated method stub
					return arg1.endsWith(".jack");
				}
			}))
        	{
        		inputFileNames.add(f.getPath());
        	}
        } else {
        	inputFileNames.add(inputFileName);
        }

        for(String inp : inputFileNames) {

            String outputFileName = inp.replace(".jack", ".vm");
            
            CompilationEngine engine = new CompilationEngine(inp, outputFileName);
            engine.compile();
        }
        
    }

}