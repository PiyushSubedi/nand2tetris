import java.io.IOException;

public class CompilationEngine {

	private Tokenizer tokenizer;
	
	private VMWriter vmWriter;

	private SymbolTableGenerator symbolTableGenerator;

	private String currentClass;

	// for handling expressions
	private Stack operatorStack; 
		
	
	public CompilationEngine(String inputFileName, String outputFileName) throws IOException {
		tokenizer = new Tokenizer(inputFileName);
		vmWriter = new VMWriter(outputFileName);
		
		symbolTableGenerator = new SymbolTableGenerator();
		resetStacks();
	}
	
	private boolean isElement(String... elements) {

		for(String element : elements) {
			if(getCurrentTokenValue().equals(element)) {
				return true;
			}	
		}
		
		return false;
	}
	
	private void start(String elemName) {
		start(elemName, true);
	}
	
	private void start(String elemName, boolean writeToken) {
	
		if(writeToken) {
				
		}
	}
	
	private void finish() {
		finish(true);
	}
	
	private void finish(boolean advance) {
	
		if(advance) {
			advance();	
		}
		
	}
	
	private void advance() {
		if(tokenizer.hasMoreTokens()) {
			tokenizer.advance();	
		}
	}
	
	public void compile() throws IOException {
		
		advance();
		
		if(isElement("class")) {
			compileClass();
		}
		
		vmWriter.close();
	}

	
	void compileClass() {

		start("class");
		
		// className
		currentClass = consumeTokens(1);

		// {
		consumeTokens(1);

		advance();
		
		while(isElement("static", "field")) {
			compileClassVarDec();
		}

		while(isElement("constructor", "method", "function")) {
			compileSubroutine();
		}
		
		// }
		

		finish();
	}
	
	private void compileClassVarDec() {

		start("classVarDec");
		
		String kind = getCurrentTokenValue();
		
		// type
		consumeTokens(1);
		
		String type = getCurrentTokenValue();
		
		// varName
		consumeTokens(1);
		
		String name = getCurrentTokenValue();

		symbolTableGenerator.addClassLevelEntry(name, type, kind);
		
		advance();

		while(isElement(",")) {
			// ,

			// varName
			consumeTokens(1);
			
			name = getCurrentTokenValue();
			
			symbolTableGenerator.addClassLevelEntry(name, type, kind);
			
			advance();
		}
		
		// ;
		

		finish();
	}

	private void compileSubroutine() {

		symbolTableGenerator.resetSubroutineTable();
		
		start("subroutineDec");
		
		String routineType = getCurrentTokenValue();	// constructor, method or function
		
		consumeTokens(1);	// return type
		
		String routineName = consumeTokens(1);

		// (
		consumeTokens(1);

		advance();
		
		// add default "this" parameter/argument to symbol table for "method"s
		if(routineType.equals("method")) {
			symbolTableGenerator.addSubroutineEntry("this", currentClass, "argument");
		}
		
		compileParameterList();

		// )
		advance();
		
		vmWriter.setFunctionContext(currentClass + "." + routineName, routineType);

		compileSubroutineBody();
		
		finish();
	}
	
	private int compileParameterList() {
		
		start("parameterList", false);
		
		int numberOfArguments = 0;

		String kind = "argument";
		
		if(!isElement(")")) {
			
			numberOfArguments++;
			
			// type
			String type = getCurrentTokenValue();
			
			// varName
			String name = consumeTokens(1);
			
			symbolTableGenerator.addSubroutineEntry(name, type, kind);
			
			advance();
			
			while(isElement(",")) {

				numberOfArguments++;
				
				// type
				type = consumeTokens(1);

				// varName
				name = consumeTokens(1);

				symbolTableGenerator.addSubroutineEntry(name, type, kind);
				
				advance();
			}
			
		}
		
		finish(false);
		
		return numberOfArguments;
	}

	private void compileSubroutineBody() {

		start("subroutineBody");

		advance();
		
		int numberOfLocals = 0;
		while(isElement("var")) {
			numberOfLocals += compileVarDec();
		}

		vmWriter.startFunction(numberOfLocals, symbolTableGenerator.getNumberOfClassLevelEntries());
		
		compileStatements();

		// }

		finish(false);
	}

	private int compileVarDec() {

		int numberOfLocals = 1;
		
		String kind = "var";
		
		start("varDec");

		// type
		consumeTokens(1);
		
		String type = getCurrentTokenValue(); 
		
		// varName
		consumeTokens(1);
		
		String name = getCurrentTokenValue();
		
		symbolTableGenerator.addSubroutineEntry(name, type, kind);
		
		advance();
		
		while(isElement(",")) {

			// varName
			consumeTokens(1);

			name = getCurrentTokenValue();
			
			symbolTableGenerator.addSubroutineEntry(name, type, kind);
			
			advance();
			
			numberOfLocals++;
		}
		
		// ;
		
		
		finish();
		
		return numberOfLocals;
	}
	
	private void compileStatements() {
		
		start("statements", false);	// do not write token

		while(true) {
			if(isElement("let")) {
				compileLet();
			} else if(isElement("if")) {
				compileIf();
			} else if(isElement("while")) {
				compileWhile();
			} else if(isElement("do")) {
				compileDo();
			} else if(isElement("return")) {
				compileReturn();
			} else {
				break;
			}	
		}
		
		finish(false);		
	}
	
	private void compileLet() {
	
		start("letStatement");
		
		// varName		
		String identifier = consumeTokens(1);
		
		SymbolTableGenerator.SymbolTableEntry entry = symbolTableGenerator.lookup(identifier);

		advance();
		Boolean arrayAssignment = false;
		if(isElement("[")) {
			arrayAssignment = true;
			compileArray(identifier);
		}
		
		// =
		advance();
		
		compileExpression();
		
		if(arrayAssignment) {
			vmWriter.writePop("temp", 0);
			vmWriter.popPointer(1);
			vmWriter.writePush("temp", 0);
			vmWriter.writePop("that", 0);
		} else {
			vmWriter.writePop(entry.getSegment(), entry.getIndex());	
		}

		// ;	
		finish();

	}

	public void compileIf() {
	
		start("ifStatement");
		
		// (
		consumeTokens(1);
		
		advance();
		
		compileExpression();
		
		vmWriter.branch();
		
		// {
		consumeTokens(1);

		advance();
		
		compileStatements();
		
		// }		
		advance();

	
		if(isElement("else")) {

			vmWriter.writeElse();
			
			// {
			consumeTokens(1);
			
			advance();
			
			compileStatements();
			
			// }

			advance();
		
			vmWriter.branchOut();
		} else {
			vmWriter.endBranch();
		}

		
		
		finish(false);	// no need to advance, already advanced
	}
	
	public void compileWhile() {

		start("whileStatement");

		vmWriter.beginLoop();
		
		// (
		consumeTokens(1);

		advance();
	
		compileExpression();

		vmWriter.loopBranch();
		
		// {
		consumeTokens(1);
		
		advance();
		
		compileStatements();
		
		vmWriter.endLoop();
		
		finish();
	}
	
	private void compileDo() {
		
		start("doStatement");
		
		// subroutineName | className | varName.
		String name = consumeTokens(1);

		// ( | .
		consumeTokens(1);
		
		compileSubroutineCall(name);
		
		vmWriter.writePop("temp", 0);

		finish();
	}
	
	private void compileSubroutineCall(String name) {
		
		int nParams = 0;
		
		if(isElement("(")) {
			
			advance();
			
			// push this
			vmWriter.pushPointer(0);

			name = currentClass + "." + name;
			
			nParams = compileExpressionList() + 1; // + 1 for "this" argument 
			
		} else if(isElement(".")) {
			
			SymbolTableGenerator.SymbolTableEntry entry 
										= symbolTableGenerator.lookup(name);
			
			if(entry != null) {
				// remote method call
				// replace name with Class name
				name = entry.getType();
				
				// push this
				vmWriter.writePush(entry.getSegment(), entry.getIndex());
				
				nParams = 1;
			}
			
			// subroutineName
			name += "." + consumeTokens(1);
			
			// (
			consumeTokens(1);

			advance();
			
			nParams += compileExpressionList();

		}
		
		// ;
		consumeTokens(1);
		
		vmWriter.writeCall(name, nParams);
		
	}
	
	public void compileReturn() {
		
		start("returnStatement");
		
		advance();
		
		if(!isElement(";")) {
			compileExpression();
		} else {
			// return void
			vmWriter.pushConstant(0);	
		}

		vmWriter.writeReturn();
		
		finish();
	}

	
	private void resetStacks() {
		operatorStack = new Stack();
	}

	public void compileExpression() {
		
		start("expression", false);
			
		compileTerm();
		
		// is operator
		while(isElement("+", "-", "*", "/", "&", "|", "<", ">", "=")) {

			operatorStack.push(getCurrentTokenValue());
			
			advance();
			
			compileTerm();
			
			vmWriter.writeOperator(operatorStack.pop());
		}
		
		//popAllOperators();
		
		finish(false);	// compileTerm has already advanced the token	
	}
	
	private void compileArray(String name) {

		SymbolTableGenerator.SymbolTableEntry entry 
					= symbolTableGenerator.lookup(name);
		
		advance();
		
		compileExpression();

		vmWriter.writePush(entry.getSegment(), entry.getIndex());
		
		vmWriter.writeOperator("+");
		
		advance(); // ]	
	}
	
	public void compileTerm() {
		
		start("term");
	
		if(isElement("(")) {

			advance();

			compileExpression();

			// )
			advance();

		} else if(isElement("-", "~")) {
			
			String op = getCurrentTokenValue();
			
			advance();
			
			compileTerm();
			
			vmWriter.writeUnaryOperator(op);

		} else {
			
			// constant | identifier

			if(getToken().getType().equals(Token.TokenType.STRING_CONST)) {
				vmWriter.writeString(getCurrentTokenValue());
				advance();
			} else if(getToken().getType().equals(Token.TokenType.INT_CONST)) {
			
				vmWriter.pushConstant(Integer.valueOf(getCurrentTokenValue()));
				advance();
			
			} else if(getToken().getType().equals(Token.TokenType.KEYWORD)) {

				// keyword constant : true, false, null
				vmWriter.writeKeywordConstant(getCurrentTokenValue());
				advance();
				
			} else if(getToken().getType().equals(Token.TokenType.IDENTIFIER)) {

				String name = getCurrentTokenValue();

				String nextToken = consumeTokens(1);
				
				if(nextToken.equals("(") || nextToken.equals(".")) {
					compileSubroutineCall(name);
				} else if(nextToken.equals("[")) {
					compileArray(name); // this will point to the address
					// we need the value
					
					vmWriter.popPointer(1);
					vmWriter.writePush("that", 0);
					
					
				} else {
					SymbolTableGenerator.SymbolTableEntry entry 
												= symbolTableGenerator.lookup(name);

					vmWriter.writePush(entry.getSegment(), entry.getIndex());
				}
				
			}

		}		
		
		finish(false);
	}

	public int compileExpressionList() {
		start("expressionList", false);
		
		int nParams = 0;
		
		if(!isElement(")")) {
			
			nParams++;
			
			compileExpression();
			
		
			while(isElement(",")) {
				// ,
				nParams++;
				
				advance();

				compileExpression();

			}
		}
		
		finish(false);
		
		return nParams;
	}
	
	
	private String consumeTokens(int numberOfTokensToConsume) {
		while(numberOfTokensToConsume > 0) {
			advance();
			

			numberOfTokensToConsume--;
		}
		
		return getCurrentTokenValue();
	}
	

	private Token getToken() {
		return tokenizer.getToken();
	}

	private String getCurrentTokenValue() {
		return getToken().getValue();
	}
}
