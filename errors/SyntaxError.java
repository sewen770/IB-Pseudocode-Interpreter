package errors;

import interpreter.Token;

/**
 * SyntaxError for the {@link LexicalAnalyzer} to Throw.
 * <p>It extends Exception and has a {@code serialVersionUID = -3220134339137347292L}.
 * 
 * @author Sewen Thy
 * @since 20/02/2018
 */
public class SyntaxError extends Exception {

	private static final long serialVersionUID = -3220134339137347292L;
	//static String message = "SyntaxError: ";

	public SyntaxError() {
		super();
	}

	/**
	 * SyntaxError constructore for invalid Token.
	 * 
	 * @param current_class
	 * @param invalidToken
	 * @param lineNumber
	 */
	public SyntaxError(String current_class,String errorMessage, Token invalidToken, int lineNumber) {
		super(errorMessage+ invalidToken.toString() + " at " + current_class + ':' + lineNumber
				+ '.');
	}
	
	public SyntaxError(String errorMessage, Token invalidToken, int lineNumber){
		super(errorMessage+": "+invalidToken.toString()+" at line: "+lineNumber+'.');
	}
	public SyntaxError(String errorMessage,int line) {
		super(errorMessage+" at line: "+line+'.');
	}

}
