package interpreter;

import java.util.ArrayList;
import java.util.HashMap;

import errors.CompilationError;
import errors.Exception;
import errors.RuntimeError;
import errors.SyntaxError;

/**
 * The {@link LexicalAnalyzer} is used in the interpreter to parse the input and
 * tokenize it. The tokens are placed in the {@code token_list} ArrayList and
 * can be accessed using {@code getToken_list()}.
 * 
 * @created 19/02/2018
 * @last_edited 15/05/2018
 * @author Sewen Thy
 */
public abstract class LexicalAnalyzer {

	final int SENTINEL = -1;

	// String input
	private static String input;

	// ArrayList of lines:
	public static ArrayList<ArrayList<Token>> token_lines = new ArrayList<ArrayList<Token>>();

	// Hold line number
	private static int line_number;

	// Current line tokens:
	static public ArrayList<Token> token_list = new ArrayList<Token>();
	static public ArrayList<String> text_segments = new ArrayList<String>();

	static String codeblock = "inactive";
	static ArrayList<ArrayList<Token>> codeblocks = new ArrayList<ArrayList<Token>>();
	static boolean has_else = false;
	static int else_ifs_count = 0;
	static boolean in_else_if = false;
	static boolean not_adding_more_codeblock = false;

	/**
	 * This method is responsible for breaking a line apart into tokens. One token
	 * at a time. Then it places it in the ArrayList of Tokens: token_list.
	 * 
	 * @throws SyntaxError
	 */
	public static void getNextLine() throws SyntaxError {
		for (int segment_index = 0; segment_index < text_segments.size(); segment_index++) {
			String current_segment = text_segments.get(segment_index);
			Token temp_token = getToken(current_segment);
			if (temp_token == null) {
				nullStringToken(current_segment);
			} else if (temp_token.isType(TokenType.EQUAL)) {
				HashMap<Integer, Token> before = Expressionizer.getBeforeToken(token_list, token_list.size());
				// System.out.println(token_list);
				if (before == null)
					throw new SyntaxError("Unexpected Token", line_number);
				Token before_token = before.values().iterator().next();
				if (before_token == null)
					throw new SyntaxError("Unexpected Token", before_token, line_number);
				if (token_list.size() == 1 && token_list.get(0).isType(TokenType.VARIABLE)) {
					token_list.add(temp_token);
					continue;
				} else if (TokenType.isTwoPieceComparator(token_list.get(token_list.size() - 1).type)) {
					token_list.add(temp_token);
					continue;
				} else if (!before_token.isType(TokenType.VARIABLE)) {
					token_list.add(temp_token);
					continue;
				} else if (TokenType.containsTokenType(token_list, TokenType.LOOP)
						|| TokenType.containsTokenType(token_list, TokenType.IF)) {
					token_list.add(temp_token);
					continue;
				}
				String newVarName = "";
				for (Token v : token_list)
					newVarName += v.value;
				token_list.clear();
				newVarName = newVarName.trim();
				if (validVariableName(newVarName)) {
					token_list.add(new Token(TokenType.VARIABLE, newVarName));
					token_list.add(temp_token);
				} else {
					Token n = getToken(newVarName);
					if (n == null)
						throw new SyntaxError("Invalid Variable Name: '" + newVarName + "' ", line_number);
					token_list.add(getToken(newVarName));
					token_list.add(temp_token);
				}
				
			} else if (temp_token.isType(TokenType.STRING)) {
				boolean fullString = true;
				String openQuoteIndexs = "0";
				String closeQuoteIndexs = "";
				for (int i = 1; i < current_segment.length() - 1; i++) {
					if (getToken(current_segment.charAt(i)) == null)
						continue;
					if (getToken(current_segment.charAt(i)).isType(TokenType.QUOTE)) {
						fullString = false;
						if (closeQuoteIndexs.isEmpty()) {
							closeQuoteIndexs += i;
							continue;
						}
						if (openQuoteIndexs.length() == closeQuoteIndexs.length())
							openQuoteIndexs += i;
						else
							closeQuoteIndexs += i;
					}
				}
				if (fullString == true)
					token_list.add(temp_token);
				else {
					char[] openQ = openQuoteIndexs.toCharArray();
					char[] closeQ = closeQuoteIndexs.toCharArray();

					boolean alternate = true;

					for (int i = 0; i <= openQ.length / 2; i += 0) {
						if (alternate) {
							String alt_substring = current_segment.substring(Character.getNumericValue(openQ[i]),
									Character.getNumericValue(closeQ[i]) + 1);
							Token temp_small_token = getToken(alt_substring);
							if (temp_small_token == null)
								nullStringToken(alt_substring);
							else
								token_list.add(temp_small_token);
							alternate = false;
						} else {
							if (i == openQ.length / 2)
								break;
							String alt_substring = current_segment.substring(Character.getNumericValue(closeQ[i++]) + 1,
									Character.getNumericValue(openQ[i]));
							Token temp_small_token = getToken(alt_substring);
							if (temp_small_token == null) {
								nullStringToken(alt_substring);
							} else
								token_list.add(temp_small_token);
							alternate = true;
						}
					}
				}
			} else
				token_list.add(temp_token);
		}
	}

	/**
	 * Apply Tokenizer to String, if unable to then return null.
	 * 
	 * @param text
	 * @return Token
	 */
	public static Token getToken(String text) {

		// Check for EOF conditions i.e. when there is no more input left to tokenize.
		if (text.equals("EOL"))
			return new Token(TokenType.EOL);
		if (text.equals("EOF"))
			return new Token(TokenType.EOF);

		// Operators:
		if (text.equals("mod"))
			return new Token(TokenType.MODULO);

		if (text.equals("div"))
			return new Token(TokenType.DIV);
		if (text.equals("="))
			return new Token(TokenType.EQUAL);
		if (text.equals("+"))
			return new Token(TokenType.PLUS);
		if (text.equals("-"))
			return new Token(TokenType.MINUS);
		if (text.equals("*"))
			return new Token(TokenType.MULTIPLY);
		if (text.equals("/"))
			return new Token(TokenType.DIVIDE);
		if (text.equals("%"))
			return new Token(TokenType.MODULO);

		// Conditionals:
		if (text.equals("if"))
			return new Token(TokenType.IF);

		if (text.equals("else"))
			return new Token(TokenType.ELSE);

		if (text.equals("endif"))
			return new Token(TokenType.ENDIF);

		if (text.equals("endloop"))
			return new Token(TokenType.ENDLOOP);

		// Loops:
		if (text.equals("loop"))
			return new Token(TokenType.LOOP);

		if (text.equals("while"))
			return new Token(TokenType.WHILE);

		if (text.equals("from"))
			return new Token(TokenType.FROM);

		if (text.equals("to"))
			return new Token(TokenType.TO);

		// Syntactic
		if (text.equals("end"))
			return new Token(TokenType.END);
		if (text.equals("then"))
			return new Token(TokenType.THEN);
		if (text.equals("output"))
			return new Token(TokenType.OUTPUT);
		if (text.equals("input"))
			return new Token(TokenType.INPUT);

		if (text.equals("true"))
			return new Token(TokenType.BOOLEAN, true);
		if (text.equals("false"))
			return new Token(TokenType.BOOLEAN, false);
		if (text.equals("NOT") || text.equals("!"))
			return new Token(TokenType.NOT);

		// Variable:
		if (validVariableName(text))
			return new Token(TokenType.VARIABLE, text);

		// Data types:
		// Checking for integer value
		try {
			int temp_value = Integer.parseInt(text);
			return new Token(TokenType.INTEGER, temp_value);
		} catch (NumberFormatException NotAnInteger) {
			// NotAnInteger.printStackTrace();
		}

		// Checking for float value
		try {
			double temp_value = Double.parseDouble(text);
			return new Token(TokenType.FLOAT, temp_value);
		} catch (NumberFormatException NotADouble) {
			// NotADouble.printStackTrace();
		}

		// Check for String:
		try {
			if (getToken(text.charAt(0)).isType(TokenType.QUOTE)
					&& getToken(text.charAt(text.length() - 1)).isType(TokenType.QUOTE)) {
				return new Token(TokenType.STRING, text);
			}
		} catch (NullPointerException null_char) {

		}
		try {
			if (getToken(text.charAt(0)).isType(TokenType.OPEN_SQUAREBRACKET)
					&& getToken(text.charAt(text.length() - 1)).isType(TokenType.CLOSE_SQUAREBRACKET))
				return new Token(TokenType.ARRAY, text);
		} catch (NullPointerException null_char) {

		}

		return null;
	}

	public static Token getToken(char current_char) {
		if (Character.isDigit(current_char))
			return new Token(TokenType.INTEGER, Character.getNumericValue(current_char));
		// Variable name
		else if (Character.isAlphabetic(current_char)) {
			if (Character.isUpperCase(current_char))
				return new Token(TokenType.VARIABLE, current_char + "");
		}
		// Syntactic tokens
		else if (current_char == '"')
			return new Token(TokenType.QUOTE);
		else if (current_char == '[')
			return new Token(TokenType.OPEN_SQUAREBRACKET);
		else if (current_char == ']')
			return new Token(TokenType.CLOSE_SQUAREBRACKET);
		else if (current_char == '(')
			return new Token(TokenType.OPEN_BRACKET, current_char + "");
		else if (current_char == ')')
			return new Token(TokenType.CLOSE_BRACKET, current_char + "");
		else if (current_char == '=')
			return new Token(TokenType.EQUAL);
		else if (current_char == '.')
			return new Token(TokenType.DOT, current_char + "");
		else if (current_char == ',')
			return new Token(TokenType.COMMA, current_char + "");
		else if (current_char == ' ')
			return new Token(TokenType.WHITESPACE, current_char + "");

		// Operators
		else if (current_char == '+')
			return new Token(TokenType.PLUS);
		else if (current_char == '-')
			return new Token(TokenType.MINUS);
		else if (current_char == '*')
			return new Token(TokenType.MULTIPLY);
		else if (current_char == '/')
			return new Token(TokenType.DIVIDE);
		else if (current_char == '%')
			return new Token(TokenType.MODULO);

		// Comparators
		else if (current_char == '<')
			return new Token(TokenType.LESS);
		else if (current_char == '>')
			return new Token(TokenType.MORE);
		else if (current_char == '!')
			return new Token(TokenType.NOT);

		return null;
	}

	public static void secondLevelTokenizer() throws Exception {
		Token cur_token = null;
		int cur_line = 1;
		if (TokenType.containsTokenType(token_list, TokenType.INTEGER))
			for (int i = 0; i < token_list.size(); i++) {
				cur_token = token_list.get(i);
				
				if (cur_token.isType(TokenType.EOL))
					continue;

				if (cur_token.isType(TokenType.MINUS)) {
					if (i != 0)
						
						continue;
					String cur_value = "";
					Token num = combineIndividualNumbers(cur_token, i);
					cur_value += num.value;
					
					if (num.isType(TokenType.INTEGER))
						token_list.add(i, new Token(TokenType.INTEGER, Integer.parseInt(cur_value)));
					else if (num.isType(TokenType.FLOAT))
						token_list.add(i, new Token(TokenType.FLOAT, Double.parseDouble(cur_value)));

				} else if (cur_token.isType(TokenType.INTEGER)) {
					
					Token num = combineIndividualNumbers(cur_token, i);
					
					if (num != null)
						token_list.add(i, num);
				}
			}
		for (int i = 0; i < token_list.size() - 1; i++) {
			cur_token = token_list.get(i);
			if(cur_token.type!=null)
			if (TokenType.isComparator(cur_token.type) && token_list.get(i + 1).type == TokenType.EQUAL) {
				switch (cur_token.type) {
				case MORE:
					token_list.remove(i);
					token_list.remove(i);
					token_list.add(i, new Token(TokenType.MOREEQ));
					break;
				case LESS:
					token_list.remove(i);
					token_list.remove(i);
					token_list.add(i, new Token(TokenType.LESSEQ));
					break;
				case NOT:
					break;
				case EQUAL:
					break;
				default:
					throw new SyntaxError("Invalid comparator", cur_token, line_number);
				}
			}
		}
		for (int i = 0; i < token_list.size(); i++) {
			cur_token = token_list.get(i);
			if (cur_token.isType(TokenType.EOL)) {
				cur_line++;
				continue;
			}
			if (cur_token.isType(TokenType.DOT)) {
				String methodName = ".";
				int j = i + 1;
				while (token_list.get(j++).type == null)
					methodName += token_list.get(j - 1).value;
				if (!token_list.get(--j).isType(TokenType.OPEN_BRACKET))
					throw new SyntaxError("Invalid Method: Expected '(' but recieved: ", token_list.get(j), cur_line);
				methodName += token_list.get(j++).value;
				methodName += token_list.get(j++).value;
				
				int n = i;
				for (; n < i + methodName.length(); n++)
					token_list.remove(i);
				token_list.add(i, new Token(TokenType.TESTMETHOD, methodName));
			} else if (cur_token.isType(TokenType.OPEN_SQUAREBRACKET)) {
				if (i == 0 || !(token_list.get(i - 1).isType(TokenType.CLOSE_SQUAREBRACKET)
						|| token_list.get(i - 1).isType(TokenType.VARIABLE))) {
					int arrayStart = i;
					ArrayList<Token> temp = new ArrayList<Token>();
					

					if (TokenType.containsTokenType(token_list, TokenType.CLOSE_SQUAREBRACKET)) {
						temp.add(cur_token);
						for (int j = i + 1; j < token_list.size(); j++) {
							if (token_list.get(j).isType(TokenType.CLOSE_SQUAREBRACKET)) {
								temp.add(token_list.get(j));
								break;
							} else if (token_list.get(j).isType(TokenType.MINUS)) {
								Token num = combineIndividualNumbers(token_list.get(j), j);
								token_list.add(j, num);
								temp.add(num);
							} else
								temp.add(token_list.get(j));
						}
						Token newToken = new Token(TokenType.ARRAY, temp);
						if (!validArray(newToken))
							throw new SyntaxError("Illegal Array Definition. Unexpected Token: ", newToken, cur_line);

						
						for (int n = arrayStart; n < arrayStart + temp.size(); n++) {
							token_list.remove(arrayStart);
						}
						token_list.add(arrayStart, newToken);
					}
				}
			}
		}
		for (int i = 0; i < token_list.size(); i++) {
			cur_token = token_list.get(i);
			if (cur_token.isType(TokenType.END)) {
				HashMap<Integer, Token> next = Expressionizer.getAfterToken(token_list, i);
				int rm_end = next.keySet().iterator().next();
				while (rm_end-- >= 0)
					token_list.remove(i);
				if (next.values().iterator().next().isType(TokenType.IF))
					token_list.add(i, new Token(TokenType.ENDIF));
				else if (next.values().iterator().next().isType(TokenType.LOOP))
					token_list.add(i, new Token(TokenType.ENDLOOP));
				break;
			}
		}
		// TODO: Parallel refactoring
		ArrayList<Token> cur_block = null;
		//ArrayList<Token> newCurBlock = null;
		if (codeblocks.size() != 0)
			cur_block = codeblocks.get(codeblocks.size() - 1);
		int tokenListSize = token_list.size();
		for (int i = 0; i < tokenListSize; i++) {
			cur_token = token_list.get(i);
			HashMap<Integer, Token> after_t = Expressionizer.getAfterToken(token_list, i);
			if (cur_token.isType(TokenType.ELSE) && !not_adding_more_codeblock) {
				if (after_t == null || !after_t.values().iterator().next().isType(TokenType.IF)) {
					// TODO: ADD THE NEW LINE TO THE NEXT ONE AFTER THE CODE BLOCK TO BE INTERPRET
					// BY THE INACTIVE STATE
					has_else = true;
					codeblock = "activeIF";
					Interpreter.codeblock = codeblock;
					codeblocks.add(new ArrayList<Token>());
					break;
				} else if (after_t.values().iterator().next().isType(TokenType.IF)) {
					else_ifs_count++;
					codeblock = "activeIF";
					Interpreter.codeblock = codeblock;
					codeblocks.add(new ArrayList<Token>());
					cur_block = codeblocks.get(codeblocks.size() - 1);
					in_else_if = true;
				} else
					throw new CompilationError("Unexpected Token", cur_token, line_number);
			}
			if (cur_token.isType(TokenType.THEN) && !not_adding_more_codeblock) {
				if (TokenType.containsTokenType(token_list, TokenType.IF)) {
					codeblock = "activeIF";
					Interpreter.codeblock = codeblock;
					codeblocks.add(new ArrayList<Token>());
					if (in_else_if) {
						cur_block.add(cur_token);
						in_else_if = false;
					}
				} else
					throw new SyntaxError("Invalid statement", line_number);
				break;
			}
			if (cur_token.isType(TokenType.LOOP) && !not_adding_more_codeblock) {
				if (TokenType.containsTokenType(token_list, TokenType.WHILE)) {
					codeblock = "activeWHILE";
					codeblocks.add(new ArrayList<Token>());
					Interpreter.codeblock = codeblock;
				} else if (TokenType.containsTokenType(token_list, TokenType.FROM)
						&& TokenType.containsTokenType(token_list, TokenType.TO)) {
					codeblock = "activeFROM";
					codeblocks.add(new ArrayList<Token>());
					Interpreter.codeblock = codeblock;
				} else
					throw new SyntaxError("Invalid statement", line_number);
				break;
			}

			if (TokenType.containsTokenType(token_list, TokenType.ENDIF)
					|| TokenType.containsTokenType(token_list, TokenType.ENDLOOP)) {
				codeblock = "inactive";
				Interpreter.codeblock = codeblock;
				break;
			}
			if (codeblock.equals("activeIF") || codeblock.equals("activeWHILE") || codeblock.equals("activeFROM")) {
				if (cur_block == null)
					throw new RuntimeError("Codeblock error", line_number);
				
				cur_block.add(cur_token);
			}
		}
		// Parenthesis codeblock
		int size = token_list.size();
		for (int i = 0; i < size; i++) {
			cur_token = token_list.get(i);
			if (cur_token.isType(TokenType.OPEN_BRACKET)) {
				ArrayList<Token> codeblock = new ArrayList<Token>();
				token_list.remove(i);
				while (!(cur_token = token_list.get(i)).isType(TokenType.CLOSE_BRACKET)) {
					codeblock.add(cur_token);
					token_list.remove(i);
				}
				token_list.remove(i);
				token_list.add(i, new Token(TokenType.PARENTHESIS_CODEBLOCK, codeblock));
				size = token_list.size();
			}
		}
	}

	public static void secondLevelTokenizer(ArrayList<Token> list) throws SyntaxError, RuntimeError, CompilationError {
		Token cur_token = null;
		int cur_line = 1;
		if (TokenType.containsTokenType(list, TokenType.INTEGER))
			for (int i = 0; i < list.size(); i++) {
				cur_token = list.get(i);
			
				if (cur_token.isType(TokenType.EOL))
					continue;
				if (cur_token.isType(TokenType.MINUS)) {
					if (i != 0)
						
						continue;
					String cur_value = "";
					Token num = combineIndividualNumbers(cur_token, i);
					
					cur_value += num.value;
					
					if (num.isType(TokenType.INTEGER))
						list.add(i, new Token(TokenType.INTEGER, Integer.parseInt(cur_value)));
					else if (num.isType(TokenType.FLOAT))
						list.add(i, new Token(TokenType.FLOAT, Double.parseDouble(cur_value)));

				} else if (cur_token.isType(TokenType.INTEGER)) {
					Token num = combineIndividualNumbers(cur_token, i);
					
					if (num != null)
						list.add(i, num);
				}
			}
		for (int i = 0; i < list.size() - 1; i++) {
			cur_token = list.get(i);
			
			if (TokenType.isComparator(cur_token.type) && list.get(i + 1).type == TokenType.EQUAL) {
				switch (cur_token.type) {
				case MORE:
					list.remove(i);
					list.remove(i);
					list.add(i, new Token(TokenType.MOREEQ));
					break;
				case LESS:
					list.remove(i);
					list.remove(i);
					list.add(i, new Token(TokenType.LESSEQ));
					break;
				case NOT:
					break;
				case EQUAL:
					break;
				default:
					throw new SyntaxError("Invalid comparator", cur_token, line_number);
				}
			}
		}
		for (int i = 0; i < list.size(); i++) {
			cur_token = list.get(i);
			if (cur_token.isType(TokenType.EOL)) {
				cur_line++;
				continue;
			}
			if (cur_token.isType(TokenType.DOT)) {
				String methodName = ".";
				int j = i + 1;
				while (list.get(j++).type == null)
					methodName += list.get(j - 1).value;
				if (!list.get(--j).isType(TokenType.OPEN_BRACKET))
					throw new SyntaxError("Invalid Method: Expected '(' but recieved: ", token_list.get(j), cur_line);
				methodName += list.get(j++).value;
				methodName += list.get(j++).value;
				
				int n = i;
				for (; n < i + methodName.length(); n++)
					list.remove(i);
				list.add(i, new Token(TokenType.TESTMETHOD, methodName));
			} else if (cur_token.isType(TokenType.OPEN_SQUAREBRACKET)) {
				if (i == 0 || !(list.get(i - 1).isType(TokenType.CLOSE_SQUAREBRACKET)
						|| list.get(i - 1).isType(TokenType.VARIABLE))) {
					int arrayStart = i;
					ArrayList<Token> temp = new ArrayList<Token>();
					

					if (TokenType.containsTokenType(list, TokenType.CLOSE_SQUAREBRACKET)) {
						temp.add(cur_token);
						for (int j = i + 1; j < list.size(); j++) {
							if (list.get(j).isType(TokenType.CLOSE_SQUAREBRACKET)) {
								temp.add(list.get(j));
								break;
							} else if (list.get(j).isType(TokenType.MINUS)) {
								Token num = combineIndividualNumbers(list.get(j), j);
								list.add(j, num);
								temp.add(num);
							} else
								temp.add(list.get(j));
						}
						Token newToken = new Token(TokenType.ARRAY, temp);
						if (!validArray(newToken))
							throw new SyntaxError("Illegal Array Definition. Unexpected Token: ", newToken, cur_line);

						
						for (int n = arrayStart; n < arrayStart + temp.size(); n++) {
							list.remove(arrayStart);
						}
						list.add(arrayStart, newToken);
					}
				}
			}
		}

		// Parenthesis codeblock
		int size = list.size();
		for (int i = 0; i < size; i++) {
			cur_token = list.get(i);
			if (cur_token.isType(TokenType.EOL) || cur_token.isType(TokenType.EOF))
				break;
			if (cur_token.isType(TokenType.OPEN_BRACKET)) {
				ArrayList<Token> codeblock = new ArrayList<Token>();
				list.remove(i);
				while (!(cur_token = list.get(i)).isType(TokenType.CLOSE_BRACKET)) {
					codeblock.add(cur_token);
					list.remove(i);
				}
				list.remove(i);

				list.add(i, new Token(TokenType.PARENTHESIS_CODEBLOCK, codeblock));
				size = list.size();
			}
		}

		for (int i = 0; i < list.size(); i++) {
			cur_token = list.get(i);
			if (cur_token.isType(TokenType.END)) {
				HashMap<Integer, Token> next = Expressionizer.getAfterToken(list, i);
				int rm_end = next.keySet().iterator().next();
				while (rm_end-- >= 0)
					list.remove(i);
				if (next.values().iterator().next().isType(TokenType.IF))
					list.add(i, new Token(TokenType.ENDIF));
				else if (next.values().iterator().next().isType(TokenType.LOOP))
					list.add(i, new Token(TokenType.ENDLOOP));
				break;
			}
		}

		ArrayList<Token> cur_block = null;
		if (codeblocks.size() != 0)
			cur_block = codeblocks.get(codeblocks.size() - 1);
		int tokenListSize = list.size();
		for (int i = 0; i < tokenListSize; i++) {
			cur_token = list.get(i);
			HashMap<Integer, Token> after_t = Expressionizer.getAfterToken(list, i);
			if (cur_token.isType(TokenType.ELSE) && !not_adding_more_codeblock) {
				if (after_t == null || !after_t.values().iterator().next().isType(TokenType.IF)) {
					cur_block = new ArrayList<Token>();
					has_else = true;

					codeblock = "activeIF";
					Interpreter.codeblock = codeblock;
					codeblocks.add(new ArrayList<Token>());
					break;
				} else if (after_t.values().iterator().next().isType(TokenType.IF) && !not_adding_more_codeblock) {
					else_ifs_count++;
					Interpreter.codeblock = codeblock;
					codeblocks.add(new ArrayList<Token>());
					cur_block = codeblocks.get(codeblocks.size() - 1);
				} else
					throw new CompilationError("Unexpected Token", cur_token, line_number);
			}
			if (cur_token.isType(TokenType.THEN) && !not_adding_more_codeblock) {
				if (TokenType.containsTokenType(list, TokenType.IF)) {
					codeblock = "activeIF";
					Interpreter.codeblock = codeblock;
					codeblocks.add(new ArrayList<Token>());
					if (in_else_if) {
						cur_block.add(cur_token);
						in_else_if = false;
					}
				} else
					throw new CompilationError("Invalid statement", line_number);
				break;
			}
			if (cur_token.isType(TokenType.LOOP) && !not_adding_more_codeblock) {
				if (TokenType.containsTokenType(list, TokenType.WHILE)) {
					codeblock = "activeWHILE";
					codeblocks.add(new ArrayList<Token>());
					Interpreter.codeblock = codeblock;
				} else if (TokenType.containsTokenType(list, TokenType.FROM)
						&& TokenType.containsTokenType(list, TokenType.TO)) {
					codeblock = "activeFROM";
					codeblocks.add(new ArrayList<Token>());
					Interpreter.codeblock = codeblock;
				} else
					throw new CompilationError("Invalid statement", line_number);
				break;
			}

			if (TokenType.containsTokenType(list, TokenType.ENDIF)
					|| TokenType.containsTokenType(list, TokenType.ENDLOOP)) {
				System.out.println("Reached END");
				codeblock = "inactive";
				Interpreter.codeblock = codeblock;
				break;
			}
			if (codeblock.equals("activeIF") || codeblock.equals("activeWHILE") || codeblock.equals("activeFROM")) {
				if (cur_block == null)
					throw new RuntimeError("Codeblock error", line_number);
				
				cur_block.add(cur_token);
			}
		}
	}

	public static void codeblockTokenizer() throws Exception {
		int cur_size = codeblocks.size();
		ArrayList<Token> newCB = new ArrayList<Token>();
		System.out.println("before codeblockTokenizer: " + codeblocks);
		for (int i = 0; i < cur_size; i++) {
			not_adding_more_codeblock = true;
			System.out.println("codeblock: " + codeblocks.get(i));
			ArrayList<Token> codeblock = codeblocks.get(i);
			ArrayList<Token> segment = new ArrayList<Token>();
			int t = 0;
			while (t < codeblock.size()) {
				Token temp = codeblock.get(t);
				if (!(temp.isType(TokenType.EOL) || temp.isType(TokenType.EOF))) {
					segment.add(temp);
					t++;
					continue;
				} else {

					System.out.println("segment before: " + segment);
					secondLevelTokenizer(segment);
					System.out.println("segment after: " + segment);

					while (segment.size() > 0) {
						// System.out.println(segment.get(0));
						newCB.add(segment.get(0));
						segment.remove(0);
					}
					t++;
				}
			}

			codeblocks.remove(i);
			codeblocks.add(i, newCB);
			cur_size = codeblocks.size();
			System.out.println("during codeblockTokenizer:" + codeblocks);
			
		}
		not_adding_more_codeblock = false;
		codeblock = "inactive";
		Interpreter.codeblock = codeblock;
	}

	/**
	 * Forming <b>INTEGER/DOUBLE Token</b> from separate INTEGER Tokens (and DOT
	 * Token).
	 * 
	 * @param firstInt
	 * @param originalIndex
	 * @return
	 *         <p>
	 *         DOUBLE Token if it has a DOT Token in the combination
	 *         </p>
	 *         <p>
	 *         INTEGER Token if it does not have a DOT Token in the combination
	 *         </p>
	 *         <p>
	 *         null if it is already a complete Token
	 *         </p>
	 */
	public static Token combineIndividualNumbers(Token firstInt, int originalIndex) {
		int index = originalIndex;
		Token cur_token = null;
		String cur_value = "" + firstInt.value;
		boolean isDouble = false;
		if (Expressionizer.getAfterToken(token_list, index) != null)
			cur_token = Expressionizer.getAfterToken(token_list, index).values().iterator().next();
		while (cur_token != null &&( index < token_list.size() && cur_token.value != null
				&& (cur_token.isType(TokenType.DOT)) || cur_token.isType(TokenType.INTEGER)
				|| cur_token.isType(TokenType.FLOAT))) {
			if (cur_token.isType(TokenType.DOT) || cur_token.isType(TokenType.FLOAT))
				isDouble = true;
			cur_value += cur_token.value;
			index++;
			if (Expressionizer.getAfterToken(token_list, index) != null)
				cur_token = Expressionizer.getAfterToken(token_list, index).values().iterator().next();
		}
		int rm = 0;
		while (rm++ < ((index - originalIndex >= 1) ? (index - originalIndex + 1) : (index - originalIndex))) {
			token_list.remove(originalIndex);
		}
		if (cur_value.length() == firstInt.value.toString().length())
			return null;
		if (isDouble)
			return new Token(TokenType.FLOAT, Double.parseDouble(cur_value));
		else
			return new Token(TokenType.INTEGER, Integer.parseInt(cur_value));

	}

	/**
	 * Pass in an already defined Array token and check for its validity.
	 * 
	 * @param array
	 * @return
	 *         <p>
	 *         <b> true </b>if the array is valid
	 *         </p>
	 *         <b> false </b> if the array is invalid
	 */
	public static boolean validArray(Token array) {
		boolean state = false;
		@SuppressWarnings("unchecked")
		ArrayList<Token> itr = (ArrayList<Token>) array.value;
		TokenType arrayType = null;
		int commasCount = 0;
		int valuesCount = 0;
		
		for (int i = 0; i < itr.size(); i++) {
			Token cur_value = itr.get(i);
		
			if (cur_value.isType(TokenType.OPEN_SQUAREBRACKET) || cur_value.isType(TokenType.WHITESPACE)
					|| cur_value.isType(TokenType.CLOSE_SQUAREBRACKET)) {
				continue;
			}
			if (arrayType == null)
				arrayType = cur_value.type;
			if (cur_value.isType(TokenType.COMMA))
				commasCount++;
			else {
				if (arrayType == null)
					return state;
				if (!cur_value.isType(arrayType))
					return state;
				valuesCount++;
			}
			
		}
		
		if (valuesCount != commasCount + 1)
			return state;

		return state = true;
	}

	public static void errorAnalysis() throws SyntaxError {
		Token cur;
		for (int i = 0; i < token_list.size(); i++) {
			cur = token_list.get(i);
			if (cur.isType(TokenType.INTEGER) && token_list.get(i + 1).isType(TokenType.WHITESPACE)
					&& token_list.get(i + 2).isType(TokenType.INTEGER)) {
				throw new SyntaxError("Invalid Token", token_list.get(i + 1), line_number);
			}
		}
	}

	public static void stripWhitespace(ArrayList<Token> list) {
		int i = 0;
		while (TokenType.containsTokenType(list, TokenType.WHITESPACE)) {
			if (list.get(i).isType(TokenType.WHITESPACE))
				list.remove(i--);
			i++;
		}
	}

	public static void nextLine() {
		token_lines.add(token_list);
		token_list = new ArrayList<Token>();
	}

	public static void setLineNumber(int n) {
		LexicalAnalyzer.line_number = n;
	}

	public static void lexicalSplit() {
		// Trimming white spaces
		String text = getInput().trim();
		char[] temp = text.toCharArray();
		ArrayList<String> splitted = new ArrayList<String>();
		int numQuotes = 0;
		int lastPos = 0;
		for (int i = 0; i < text.length(); i++) {
			if (temp[i] == '"') {
				numQuotes++;
			}
			if (numQuotes % 2 == 0) {
				if (temp[i] == ' ') {
					splitted.add(text.substring(lastPos, i));
					splitted.add(" ");
					lastPos = i + 1;
				} else {
					try {
						Token temp_token = getToken(temp[i]);
						if (temp_token.type != TokenType.QUOTE && TokenType.isSymbol(temp_token.type)) {
							
							splitted.add(text.substring(lastPos, i));
							lastPos = i + 1;
							splitted.add("" + text.charAt(i));
							lastPos = i + 1;
						}
					} catch (NullPointerException null_token) {

					}
				}
				if (i == text.length() - 1)
					splitted.add(text.substring(lastPos));
			} else if (i == text.length() - 1)
				splitted.add(text.substring(lastPos));
		}
		// Getting usable segments of the text
		text_segments = new ArrayList<String>();
		for (String n : splitted) {
			if (!n.isEmpty())
				text_segments.add(n);
		}

	}

	public static void nullStringToken(String invalidString) {
		for (int char_token_index = 0; char_token_index < invalidString.length(); char_token_index++) {
			Token temp_char_token = getToken(invalidString.charAt(char_token_index));
			if (temp_char_token != null) {
	
				token_list.add(temp_char_token);
			} else
				token_list.add(new Token(null, invalidString.charAt(char_token_index)));
		}
	}

	public static boolean validVariableName(String segment) {
		boolean isVariableName = true;
		boolean hasLetter = false;
		if (segment.charAt(0) != '$' && segment.charAt(0) != '_' && !Character.isUpperCase(segment.charAt(0)))
			return false;
		if (Character.isUpperCase(segment.charAt(0)))
			hasLetter = true;
		for (int text_index = 1; text_index < segment.length(); text_index++) {
			char temp_char = segment.charAt(text_index);
			if (Character.isUpperCase(temp_char))
				hasLetter = true;
			if (!(Character.isWhitespace(temp_char) || Character.isLowerCase(temp_char))
					&& (Character.isUpperCase(temp_char) || Character.isDigit(temp_char) || temp_char == '_'))
				;
			else {
				isVariableName = false;
				break;
			}
		}
		return hasLetter && isVariableName;
	}

	public static String getInput() {
		return input;
	}

	public static void setInput(String input) {
		LexicalAnalyzer.input = input;
	}

	public ArrayList<Token> getToken_list() {
		return token_list;
	}

	public static void reset() {
		token_list.clear();
		text_segments.clear();
		line_number = 1;
		token_lines.clear();
		input = "";
		codeblocks.clear();
		codeblock = "inactive";

		has_else = false;
		else_ifs_count = 0;
		in_else_if = false;
		not_adding_more_codeblock = false;
	}

	public static void resetCodeblocks() {
		if(codeblocks!=null)
		codeblocks.clear();
		has_else = false;
		else_ifs_count = 0;
		in_else_if = false;
		not_adding_more_codeblock = false;
		if(codeblock!=null)
		codeblock = "inactive";
	}
	
}
