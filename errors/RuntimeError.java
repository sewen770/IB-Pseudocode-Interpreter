package errors;

import interpreter.Token;

public class RuntimeError extends Exception {

	private static final long serialVersionUID = 5590167057885560356L;
	
	public RuntimeError() {
		super();
	}
	
	public RuntimeError(String current_class,String errorMessage, Token invalidToken, int lineNumber) {
		super("'"+ errorMessage+ invalidToken.toString() + "' at " + current_class + ':' + lineNumber
				+ '.');
	}
	
	public RuntimeError(String errorMessage, Token invalidToken, int lineNumber){
		super(errorMessage+": "+invalidToken.toString()+" at line: "+lineNumber+'.');
	}
	public RuntimeError(String errorMessage,int line) {
		super(errorMessage+" at line: "+line+'.');
	}
	
	public RuntimeError(String errorMessage, Token invalidToken){
		super(errorMessage+": "+invalidToken.toString()+'.');
	}

}
