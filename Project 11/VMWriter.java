import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;

public class VMWriter {

	private PrintWriter writer;
	
	private UniqueLabelGenerator labelGenerator;
	

	public VMWriter(String outputFileName) throws IOException {
		this.writer = new PrintWriter(new FileWriter(outputFileName));
		this.labelGenerator = new UniqueLabelGenerator();
	}
	
	public void resetLabels() {
		this.labelGenerator = new UniqueLabelGenerator();
	}
	
	public void pushConstant(int value) {
		writePush("constant", value);
	}
	
	public void writePush(String segment, int index) {
		write("push " + segment + " " + index);
	}
	
	public void writePush(int value) {
		write("push " + value);
	}
	
	public void writePop(String segment, int index) {
		write("pop " + segment + " " + index);
	}

	public void writeFunction(String fName, int nLocals) {
		write("function " + fName + " " + nLocals);
	}
	
	public void writeReturn() {
		write("return");
	}

	public void writeCall(String fName, int nParams) {
		write("call " + fName + " " + nParams);
	}
	
	public void writeKeywordConstant(String keywordConstant) {
		if(keywordConstant.equals("false")) {
			pushConstant(0);
		} else if(keywordConstant.equals("true")) {
			pushConstant(0);
			writeUnaryOperator("~");
		} else if(keywordConstant.equals("this")) {
			pushPointer(0);
		} else if(keywordConstant.equals("null")) {
			pushConstant(0);
		}
	}

	
	public void writeOperator(String opSymbol) {
		
		switch(opSymbol) {
		
			case "+" :
				write("add");
				break;
			case "-" :
				write("sub");
				break;
			case "*" :
				writeCall("Math.multiply", 2);
				break;
			case "/" :
				writeCall("Math.divide", 2);
				break;
			case "&" :
				write("and");
				break;
			case "|" :
				write("or");
				break;
			case "<" :
				write("lt");
				break;
			case ">" :
				write("gt");
				break;
			case "=" :
				write("eq");
				break;
			default :
				break;
		
		}
		
	}
	
	public void writeUnaryOperator(String opSymbol) {
		if(opSymbol.equals("-")) {
			write("neg");
		} else {
			// ~
			write("not");
		}
	}

	
	public void beginLoop() {
		writeLabel(labelGenerator.generate("WHILE_EXP"));
	}
	
	public void loopBranch() {
		writeUnaryOperator("~");
		writeIfGoto(labelGenerator.generate("WHILE_END"));
	}
	
	public void endLoop() {
		writeGoto(labelGenerator.getLastGeneratedLabelFor("WHILE_EXP"));
		writeLabel(labelGenerator.getLastGeneratedLabelFor("WHILE_END"));
	}
	
	public void branch() {

		String trueLabel = labelGenerator.generate("IF_TRUE"),
				falseLabel = labelGenerator.generate("IF_FALSE");

		writeIfGoto(trueLabel);
		
		writeGoto(falseLabel);
		
		writeLabel(trueLabel);	
	}
	
	public void writeElse() {
	
		String newLabel = labelGenerator.generate("IF_END");

		writeGoto(newLabel);
		writeLabel(labelGenerator.getLastGeneratedLabelFor("IF_FALSE"));
	}
	
	public void endBranch() {
		writeLabel(labelGenerator.getLastGeneratedLabelFor("IF_FALSE"));
	}
	
	public void branchOut() {
		writeLabel(labelGenerator.getLastGeneratedLabelFor("IF_END"));
	}
	
	private void writeIfGoto(String label) {
		write("if-goto " + label);
	}
	
	private void writeGoto(String label) {
		write("goto " + label);
	}
	
	private void writeLabel(String label) {
		write("label " + label);
	}
	
	// function context vars
	String routineName, routineType;
	
	public void setFunctionContext(String functionName, String routineType) {
		this.routineName = functionName;
		this.routineType = routineType;
		
		resetLabels();
	}
	
	public void startFunction(int nLocals, int nClassLocals) {

		writeFunction(this.routineName, nLocals);
		
		if(routineType.equals("method")) {
			// set "this"
			writePush("argument", 0);
			writePop("pointer", 0);
		} else if(routineType.equals("constructor")) {
			pushConstant(nClassLocals);
			writeAlloc();
			popPointer(0);
		}

		
	}
	
	public void writeString(String text) {
		
		pushConstant(text.length());
		
		writeCall("String.new", 1);
		
		for(int i=0; i<text.length(); i++) {
			
			int asciiEq = text.charAt(i);
			
			pushConstant(asciiEq);
			
			writeCall("String.appendChar", 2);
			
		}
		
	}
	
	
	
	public void pushPointer(int val) {
		writePush("pointer", val);
	}
	
	public void popPointer(int val) {
		writePop("pointer", val);
	}
	
	public void writeAlloc() {
		writeCall("Memory.alloc", 1);
	}
	
	private void write(String text) {
		writer.println(text);
	}
	
	public void close() {
		writer.close();
	}
	
	
	
	private class UniqueLabelGenerator {

		HashMap<String, Integer> labelCountMap;

		HashMap<String, Stack> labelsUsed;
		
		public UniqueLabelGenerator() {
			labelCountMap = new HashMap<>();
			labelsUsed = new HashMap<>();
		}
		
		public String generate(String text) {
			int count = 0;

			if(labelCountMap.containsKey(text)) {
				count = labelCountMap.get(text) + 1;
			}
			
			labelCountMap.put(text, count);
			
			String label = text + count;

			if(!labelsUsed.containsKey(text)) {
				labelsUsed.put(text, new Stack());
			}
			
			labelsUsed.get(text).push(label);
			
			return label;
		}
		
		public String getLastGeneratedLabelFor(String text) {
			
			return labelsUsed.get(text).pop();
		}

	}
	
}
