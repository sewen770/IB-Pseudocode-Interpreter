package interpreter;

import java.util.ArrayList;
import java.util.HashMap;

import errors.RuntimeError;

/**
 * Token definition class contains interpreted representations of parsed text
 * 
 * @created: 23/01/2018 2:40pm
 * 
 * @author: Sewen Thy
 */
public class Token {

	// States of a Token
	public TokenType type;
	public Object value;
	
	// Extra state of a loop/if statements
	public ArrayList<Token> codeblock=null;

	public Token(TokenType type, Object value) {
		this.type = type;
		this.value = value;
	}

	public Token(TokenType type) {
		this.type = type;
		this.value = null;
		if(this.type == TokenType.IF)
			this.codeblock = new ArrayList<Token>();
	}

	public String toString() {
		// String representation of the class
		return "Token(" + this.type + ", " + this.value + ")";
	}

	public boolean isType(TokenType type) {
		return (this.type == type);
	}

	public boolean equals(Token token) {
		return (this.type == token.type && this.value.equals(token.value));
	}
	
	public void variableDefinition(Token token) {
		HashMap<String, Token> map = new HashMap<String, Token>();
		map.put((String) this.value, token);
		this.value = map;
	}

	@SuppressWarnings("unchecked")
	public void variableRedefinition(Token token) throws RuntimeError {
		if (this.value.getClass() != HashMap.class)
			throw new RuntimeError("Unable to redefine variable", this);
		HashMap<String, Token> temp = (HashMap<String, Token>) this.value;
		HashMap<String, Token> map = new HashMap<String, Token>();
		map.put(temp.keySet().iterator().next(), token);
		this.value = map;
	}

	public void argsDefinition(Token token) {
		variableDefinition(token);
	}
	
	@SuppressWarnings("unchecked")
	public String output() throws RuntimeError {
		switch(type) {
		case INTEGER:
			return this.value+"";
		case STRING:
			return ((String)this.value).substring(1, ((String)this.value).length()-1);
		case BOOLEAN:
			if((boolean)this.value==true) return "true";
			else if((boolean)this.value==false) return "false";
		case FLOAT:
			return this.value+"";
		case ARRAY:
			String out="[";
			for(int i=1;i<((ArrayList<Token>)this.value).size()-1;i++) 
				out+=((ArrayList<Token>)this.value).get(i).value;
			return out+"]";
		case VARIABLE:
			return ((Token)((HashMap<String, Token>)this.value).values().iterator().next()).output();
		default:
			throw new RuntimeError("Unknown Printing Error.",this);
		}
		
	}
	
	public Token concat(Token after) throws RuntimeError {
		if(this.type==TokenType.STRING&&after.type==TokenType.STRING) return new Token(TokenType.STRING,((String)this.value).substring(0, ((String)this.value).length()-1)+((String)after.value).substring(1));
		throw new RuntimeError("Unable to concatenate STRING Tokens: "+this.toString()+" and "+after.toString(),this);
	}
	
	public Token clone() {
		return new Token(this.type,this.value);
	}
}
