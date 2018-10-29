package interpreter;

import java.util.ArrayList;
import java.util.HashMap;

import errors.Exception;

/**
 * This is the enumerations for the {@link Token} class. It consists of
 * constants definitions for all the Token's types that are going to be used in
 * the pseudocode interpreter.
 * 
 * Methods in here are helper methods to find Token(s) inside a list.
 * 
 * @author Sewen Thy
 * @created: 20/02/2018
 */
public enum TokenType {
	// Tests:
	TESTMETHOD,
	// EOF is End-Of-File type which is a token for the end of the input and EOL is
	// End-Of-Line
	EOF, EOL,

	// Data types:
	INTEGER, STRING, BOOLEAN, FLOAT, ARRAY,

	// Operators type:
	PLUS, MINUS, MULTIPLY, DIVIDE, MODULO, DIV,

	// Comparators:
	EQUAL, LESS, LESSEQ, MORE, MOREEQ, NOT, AND, OR,

	// Conditionals:
	IF, ELSE, ENDIF,

	// Loops:
	LOOP, WHILE, FROM, TO, ENDLOOP,

	// Syntactic Expressions:
	VARIABLE, QUOTE, OPEN_SQUAREBRACKET, CLOSE_SQUAREBRACKET, OPEN_BRACKET, CLOSE_BRACKET, END, THEN, OUTPUT, 
	INPUT, DOT, WHITESPACE, COMMA, PARENTHESIS_CODEBLOCK;

	// Methods:
	public static boolean isSymbol(TokenType type) {
		return (type == EQUAL || type == LESS || type == LESSEQ || type == MORE || type == MOREEQ || type == PLUS
				|| type == MINUS || type == MULTIPLY || type == DIVIDE || type == MODULO || type == DIV
				|| type == OPEN_SQUAREBRACKET || type == CLOSE_SQUAREBRACKET || type == OPEN_BRACKET
				|| type == CLOSE_BRACKET || type == DOT);
	}

	public static boolean isOperator(TokenType type) {
		return (type == PLUS || type == MINUS || type == MULTIPLY || type == DIVIDE || type == MODULO || type == DIV);
	}

	public static boolean isComparator(TokenType type) {
		switch (type) {
		case EQUAL:
			return true;
		case LESS:
			return true;
		case LESSEQ:
			return true;
		case MORE:
			return true;
		case MOREEQ:
			return true;
		case NOT:
			return true;
		default:
			return false;

		}
	}

	public static boolean isTwoPieceComparator(TokenType type) {
		return (type == MORE || type == LESS || type == NOT);
	}

	public static boolean isLogic(TokenType type) {
		return (type == AND || type == OR || type == NOT);
	}

	public static boolean containsTokenType(ArrayList<Token> list, TokenType type) {
		for (Token t : list) {
			if (t.isType(type))
				return true;
		}
		return false;
	}

	public static boolean containsOperator(ArrayList<HashMap<Integer, TokenType>> list, TokenType type) {
		for (int x = 0; x < list.size(); x++) {
			HashMap<Integer, TokenType> cur = list.get(x);
			if (cur.values().iterator().next() == type)
				return true;
		}
		return false;
	}

	public static boolean containsOperators(ArrayList<Token> list) {
		for (Token c : list)
			if (isOperator(c.type))
				return true;
		return false;
	}

	public static HashMap<Integer, TokenType> getFirstOperator(ArrayList<HashMap<Integer, TokenType>> list,
			TokenType type) {
		for (int x = 0; x < list.size(); x++) {
			HashMap<Integer, TokenType> cur = list.get(x);
			if (cur.values().iterator().next() == type)
				return cur;
		}
		return null;
	}

	public static Token getFirstTokenType(ArrayList<Token> list, TokenType type) {
		for (Token t : list) {
			if (t.isType(type))
				return t;
		}
		return null;
	}

	public static Token getFirstToken(ArrayList<Token> list, Token token) {
		for (Token t : list) {
			if (t.equals(token))
				return t;
		}
		return null;
	}

	public static int getFirstTokenTypeIndex(ArrayList<Token> list, TokenType type) {
		for (int n = 0; n < list.size(); n++)
			if (list.get(n).isType(type))
				return n;
		return -1;
	}

	public static int getFirstTokenIndex(ArrayList<Token> list, Token token) {
		for (int n = 0; n < list.size(); n++)
			if (list.get(n).equals(token))
				return n;
		return -1;
	}

	public static int getFirstTokenIndex(ArrayList<Token> list, Token token, int after) {
		for (int n = after; n < list.size(); n++)
			if (list.get(n).equals(token))
				return n;
		return -1;
	}

	public static boolean containsToken(ArrayList<Token> list, Token token) {
		for (Token t : list) {
			if (t.equals(token))
				return true;
		}
		return false;
	}

	@SuppressWarnings("unchecked")
	public static boolean referredTo(ArrayList<Token> list, Token token) throws Exception {
		String name;
		if (token.value.getClass()==String.class) name= (String) token.value;
		else if(token.value.getClass()==HashMap.class) return true;
		else throw new Exception("Invalid variable",token);
		//System.out.println(name);
		for (Token t : list) {
			System.out.println(list.toString());
//			System.out.println(t.value);
			HashMap<String, Token> sec_map = (HashMap<String, Token>) t.value;
			String name2 = sec_map.keySet().iterator().next();
			if (name.equals(name2))
				return true;
		}
		return false;
	}
	
	@SuppressWarnings("unchecked")
	public static boolean basicVarReferredTo(ArrayList<Token> list, Token token) throws Exception {
		String name;
		if (token.value.getClass()==String.class) name= (String) token.value;
		else if(token.value.getClass()==HashMap.class) {
			HashMap<String, Object> sec = (HashMap<String, Object>)token.value;
			name = sec.keySet().iterator().next();
		}
		else throw new Exception("Invalid variable",token);
		for (Token t : list) {
			System.out.println(list.toString());
			String name2 = (String) t.value;
			if (name.equals(name2))
				return true;
		}
		return false;
	}

	@SuppressWarnings("unchecked")
	public static int getFirstVariableIndex(ArrayList<Token> list, Token token) throws Exception {
		String name;
		if (token.value.getClass()==String.class) name= (String) token.value;
		else if(token.value.getClass()==HashMap.class)  name= ((HashMap<String,Token>) token.value).keySet().iterator().next();
		else throw new Exception("Invalid variable",token);
		for (int n = 0; n < list.size(); n++) {
			String name2;
			if(list.get(n).value.getClass()==String.class) name2= (String) list.get(n).value;
			else {
				HashMap<String, Token> sec_map = (HashMap<String, Token>) list.get(n).value;
				name2 = sec_map.keySet().iterator().next();
			}
			if (name.equals(name2))
				return n;
		}
		return -1;
	}
	
}
