import java.io.IOException;

public class CompilationEngine {

	private Tokenizer tokenizer;
	
	private XMLWriter writer;
	

	public CompilationEngine(String inputFileName, String outputFileName) throws IOException {
		tokenizer = new Tokenizer(inputFileName);
		writer = new XMLWriter(outputFileName);
	}
	
	private boolean isElement(String... elements) {

		for(String element : elements) {
			if(getToken().getValue().equals(element)) {
				return true;
			}	
		}
		
		return false;
	}
	
	private void start(String elemName) {
		start(elemName, true);
	}
	
	private void start(String elemName, boolean writeToken) {
		writer.writeElement(elemName);
		
		if(writeToken) {
			writeToken();	
		}
	}
	
	private void finish() {
		finish(true);
	}
	
	private void finish(boolean advance) {
		writer.closeElement();
		
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
		
		writer.close();
		tokenizer.close();
	}

	
	void compileClass() {

		start("class");
		
		// className {
		consumeTokens(2);

		advance();
		
		while(isElement("static", "field")) {
			compileClassVarDec();
		}

		while(isElement("constructor", "method", "function")) {
			compileSubroutine();
		}
		
		// }
		writeToken();

		finish();
	}
	
	private void compileClassVarDec() {

		start("classVarDec");
		
		// type varName
		consumeTokens(2);

		advance();

		while(isElement(",")) {
			// ,
			writeToken();

			// varName
			consumeTokens(1);
			
			advance();
		}
		
		// ;
		writeToken();

		finish();
	}

	private void compileSubroutine() {

		start("subroutineDec");

		// (void|type) subroutineName (
		consumeTokens(3);

		advance();

		compileParameterList();
		
		// )
		writeToken();
		
		advance();
		
		compileSubroutineBody();
		
		finish();		
	}
	
	private void compileParameterList() {
		
		start("parameterList", false);
		
		if(!isElement(")")) {
			
			// type
			writeToken();
			
			// varName
			consumeTokens(1);
			
			advance();
			
			while(isElement(",")) {

				// ,
				writeToken();

				// type varName
				consumeTokens(2);
				
				advance();
			}
			
		}
		
		finish(false);
		
	}

	private void compileSubroutineBody() {

		start("subroutineBody");

		advance();
		
		while(isElement("var")) {
			compileVarDec();
		}

		compileStatements();

		// }
		writeToken();
		
		finish(false);
	}

	private void compileVarDec() {
		
		start("varDec");

		// type varName
		consumeTokens(2);
		
		advance();
		
		while(isElement(",")) {

			// ,
			writeToken();

			// varName
			consumeTokens(1);
			
			advance();
		}
		
		// ;
		writeToken();
		
		finish();
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
		consumeTokens(1);
		
		advance();
		
		if(isElement("[")) {
			
			writeToken();
			
			advance();

			compileExpression();
			
			// ]
			writeToken();
			
			advance();
		}
		
		// =
		writeToken();
		
		advance();
		
		compileExpression();
		
		// ;
		writeToken();
		
		finish();
	}

	public void compileIf() {
	
		start("ifStatement");
		
		// (
		consumeTokens(1);
		
		advance();
		
		compileExpression();
		
		// )
		writeToken();
		
		// {
		consumeTokens(1);

		advance();
		
		compileStatements();
		
		// }		
		writeToken();
		
		advance();

		if(isElement("else")) {
			
			writeToken();

			// {
			consumeTokens(1);
			
			advance();
			
			compileStatements();
			
			// }
			writeToken();

			advance();
		}
		
		finish(false);	// no need to advance, already advanced
	}
	
	public void compileWhile() {

		start("whileStatement");
		
		// (
		consumeTokens(1);
		
		advance();
		
		compileExpression();
		
		// )
		writeToken();
		
		// {
		consumeTokens(1);
		
		advance();
		
		compileStatements();
		
		// }
		writeToken();
		
		
		finish();
	}
	
	private void compileDo() {
		
		start("doStatement");
		
		// subroutineName( | (className | varName).
		consumeTokens(2);
		
		if(isElement("(")) {
			
			advance();
			
			compileExpressionList();
			
			// )
			writeToken();

		} else if(isElement(".")) {
			
			// subroutineName(
			consumeTokens(2);
			
			advance();
			
			compileExpressionList();
			
			// )
			writeToken();
		}
		
		// ;
		consumeTokens(1);
		
		finish();
	}
	
	public void compileReturn() {
		
		start("returnStatement");
		
		advance();
		
		if(!isElement(";")) {
			compileExpression();
		}
		
		// ;
		writeToken();
		
		finish();
	}
	
	
	public void compileExpression() {
		
		start("expression", false);
		
		compileTerm();

		// is operator
		while(isElement("+", "-", "*", "/", "&", "|", "<", ">", "=")) {
			writeToken();
			
			advance();
			
			compileTerm();
		}
		
		finish(false);	// compileTerm has already advanced the token	
	}
	
	public void compileTerm() {
		
		start("term");

		if(isElement("(")) {
			
			advance();
			
			compileExpression();
			
			// )
			writeToken();
			
			advance();
		
		} else if(isElement("-", "~")) {
			
			advance();
			
			compileTerm();

		} else {
			
			advance();
			
			if(isElement("[")) {
				writeToken();

				advance();
				
				compileExpression();
				
				// ]
				writeToken();
				
				advance();
				
			} else {
				// subroutine call ?

				if(isElement("(")) {
					writeToken();

					advance();
					
					compileExpressionList();
					
					// )
					writeToken();

					advance();
					
				} else if(isElement(".")) {
					writeToken();
					
					// subroutineName (
					consumeTokens(2);
					
					advance();
					
					compileExpressionList();
					
					// )
					writeToken();
					
					advance();
				}
			}
			
		}		
		
		finish(false);
	}
	
	public void compileExpressionList() {
		start("expressionList", false);
		
		if(!isElement(")")) {
			
			compileExpression();
			
			while(isElement(",")) {
				// ,
				writeToken();
				
				advance();

				compileExpression();
			}
		}
		
		finish(false);
	}
	
	
	private void consumeTokens(int numberOfTokensToConsume) {
		while(numberOfTokensToConsume > 0) {
			advance();
			writeToken();

			numberOfTokensToConsume--;
		}
	}
	
	
	private void writeToken() {
		writer.writeElement(getToken().getType().toString(), getToken().getValue());
	}
	
	private Token getToken() {
		return tokenizer.getToken();
	}
}
