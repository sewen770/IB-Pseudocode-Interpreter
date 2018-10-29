package errors;

import interpreter.Token;

public class CompilationError extends Exception {

	public CompilationError(String errorMessage, Token invalidToken, int lineNumber) {
		super(errorMessage, invalidToken, lineNumber);
	}
	
	public CompilationError(String errorMessage, int lineNumber) {
		super(errorMessage+" at line: "+lineNumber+'.');
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 6589969648480638107L;

}
