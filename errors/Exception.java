package errors;

import interpreter.Token;

public class Exception extends java.lang.Exception{
	/**
	 * 
	 */
	private static final long serialVersionUID = 2944327792610938113L;
	
	public Exception() {
		super();
	}
	
	public Exception(String message) {
		super(message);
	}
	public Exception(String errorMessage, Token invalidToken, int lineNumber){
		super(errorMessage+": "+invalidToken.toString()+" at line: "+lineNumber+'.');
	}
	
	public Exception(String errorMessage, Token invalidToken){
		super(errorMessage+": "+invalidToken.toString()+'.');
	}
}
