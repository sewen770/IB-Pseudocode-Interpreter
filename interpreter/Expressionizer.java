package interpreter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

import errors.Exception;
import errors.RuntimeError;
import errors.SyntaxError;

/**
 * Evaluations of Tokens.
 * 
 * @author Sewen Thy
 * @created: 21/02/2018
 */
public abstract class Expressionizer {

	public static Token cur_token = null;

	public static ArrayList<Token> variables = new ArrayList<Token>();

	public static ArrayList<Token> token_list;

	public static ArrayList<ArrayList<Token>> lines = new ArrayList<ArrayList<Token>>();

	public static int line_num = 0;

	public static boolean onlyAddingVar = false;
	public static boolean debug = false;
	public static int inCB_line = 0;
	static ArrayList<ArrayList<Token>> debugCodeblock = new ArrayList<ArrayList<Token>>();

	@SuppressWarnings("unchecked")
	public static ArrayList<Token> evaluateTokens(ArrayList<Token> list) throws SyntaxError, RuntimeError, Exception {
		if (onlyAddingVar) {
			for (int i = 0; i < list.size(); i++) {
				if (list.get(i).isType(TokenType.VARIABLE))
					if (!TokenType.containsToken(variables, list.get(i)))
						variables.add(list.get(i));
			}
			return null;
		}
		if (TokenType.containsTokenType(list, TokenType.IF)) {
			LexicalAnalyzer.not_adding_more_codeblock = true;
			Interpreter.codeblock = "inactive";

			int i = 1;
			ArrayList<Token> condition_list = new ArrayList<Token>();
			while (!(cur_token = list.get(i++)).isType(TokenType.THEN)) {
				if (cur_token.isType(TokenType.WHITESPACE) || cur_token.isType(TokenType.IF))
					continue;
				else if (cur_token.isType(TokenType.EOF) || cur_token.isType(TokenType.EOL))
					break;
				else
					condition_list.add(cur_token);
			}
			evaluateTokens(condition_list);
			boolean condition = evaluateBoolean(condition_list);
			ArrayList<Token> codeblock = LexicalAnalyzer.codeblocks.get(0);
			LexicalAnalyzer.codeblocks.remove(0);
			if (condition) {
				ArrayList<Token> segment = new ArrayList<Token>();
				ArrayList<Token> newCB = new ArrayList<Token>();
				int t = 0;
				while (t < codeblock.size()) {
					Token temp = codeblock.get(t);
					if (!(temp.isType(TokenType.EOL) || temp.isType(TokenType.EOF))) {
						segment.add(temp);
						t++;
					} else {
						segment.add(temp);
						evaluateTokens(segment);

						while (segment.size() > 0) {
							newCB.add(segment.get(0));
							segment.remove(0);
						}
						t++;
					}
				}
				LexicalAnalyzer.else_ifs_count = 0;
				LexicalAnalyzer.codeblocks.clear();
			} else {
				while (LexicalAnalyzer.else_ifs_count > 0) {
					i = 0;
					condition_list = new ArrayList<Token>();
					token_list = LexicalAnalyzer.codeblocks.get(0);
					LexicalAnalyzer.codeblocks.remove(0);
					while (!(cur_token = token_list.get(i++)).isType(TokenType.THEN)) {
						if (cur_token.isType(TokenType.WHITESPACE) || cur_token.isType(TokenType.ELSE)
								|| cur_token.isType(TokenType.IF))
							continue;
						else if (cur_token.isType(TokenType.EOF) || cur_token.isType(TokenType.EOL))
							break;
						else
							condition_list.add(cur_token);
					}
					evaluateTokens(condition_list);
					condition = evaluateBoolean(condition_list);
					codeblock = LexicalAnalyzer.codeblocks.get(0);
					LexicalAnalyzer.codeblocks.remove(0);
					LexicalAnalyzer.else_ifs_count--;
					if (condition) {
						ArrayList<Token> segment = new ArrayList<Token>();
						ArrayList<Token> newCB = new ArrayList<Token>();

						int t = 0;

						while (t < codeblock.size()) {
							Token temp = codeblock.get(t);
							if (!(temp.isType(TokenType.EOL) || temp.isType(TokenType.EOF))) {
								segment.add(temp);
								t++;
							} else {
								segment.add(temp);
								evaluateTokens(segment);

								while (segment.size() > 0) {
									newCB.add(segment.get(0));
									segment.remove(0);
								}
								t++;
							}

							codeblock = newCB;
						}
						while (LexicalAnalyzer.else_ifs_count > 0) {
							LexicalAnalyzer.codeblocks.remove(0);
							LexicalAnalyzer.else_ifs_count--;
						}
						if (LexicalAnalyzer.has_else)
							LexicalAnalyzer.codeblocks.remove(0);
						LexicalAnalyzer.has_else = false;
						LexicalAnalyzer.codeblocks.clear();
					}

				}

				if (LexicalAnalyzer.has_else) {
					codeblock = LexicalAnalyzer.codeblocks.get(0);
					System.out.println("else codeblock: " + codeblock.toString());
					LexicalAnalyzer.codeblocks.remove(0);
					ArrayList<Token> segment = new ArrayList<Token>();
					ArrayList<Token> newCB = new ArrayList<Token>();
					int t = 0;
					while (t < codeblock.size()) {
						Token temp = codeblock.get(t);
						if (!(temp.isType(TokenType.EOL) || temp.isType(TokenType.EOF))) {
							segment.add(temp);
							t++;
						} else {
							segment.add(temp);
							evaluateTokens(segment);

							while (segment.size() > 0) {
								System.out.println(segment.get(0));
								newCB.add(segment.get(0));
								segment.remove(0);
							}
							t++;
						}
					}
					codeblock = newCB;
					LexicalAnalyzer.has_else = false;
				}
			}
			LexicalAnalyzer.not_adding_more_codeblock = false;
			Interpreter.codeblock = "inactive";
			LexicalAnalyzer.codeblocks.clear();
		}

		if (TokenType.containsTokenType(list, TokenType.LOOP)) {
			LexicalAnalyzer.not_adding_more_codeblock = true;
			int i = 1;
			ArrayList<Token> condition_list = new ArrayList<Token>();
			while (!((cur_token = list.get(i++)).isType(TokenType.EOL) || cur_token.isType(TokenType.EOF))) {
				if (cur_token.isType(TokenType.WHITESPACE) || cur_token.isType(TokenType.WHILE)
						|| cur_token.isType(TokenType.LOOP))
					continue;
				else if (cur_token.isType(TokenType.EOF) || cur_token.isType(TokenType.EOL))
					break;
				else
					condition_list.add(cur_token);
			}
			ArrayList<Token> codeblock = null;
			try {
				codeblock = LexicalAnalyzer.codeblocks.get(0);
			} catch (IndexOutOfBoundsException e) {
				throw new RuntimeError("No codeblock for loop", line_num);
			}
			LexicalAnalyzer.codeblocks.remove(0);
			while (evaluateBoolean(condition_list)) {

				ArrayList<Token> segment = new ArrayList<Token>();
				ArrayList<Token> newCB = new ArrayList<Token>();
				int t = 0;
				while (t < codeblock.size()) {
					Token temp = codeblock.get(t);
					if (!(temp.isType(TokenType.EOL) || temp.isType(TokenType.EOF))) {
						segment.add(temp);
						t++;
					} else {
						segment.add(temp);
						evaluateTokens(segment);
						while (segment.size() > 0) {
							newCB.add(segment.get(0));
							segment.remove(0);
						}
						t++;
					}
				}
				condition_list.clear();
				LexicalAnalyzer.token_list.clear();
				LexicalAnalyzer.setInput(Interpreter.loop_text);
				LexicalAnalyzer.lexicalSplit();
				LexicalAnalyzer.getNextLine();
				LexicalAnalyzer.secondLevelTokenizer();
				ArrayList<Token> temp_condition_list = LexicalAnalyzer.token_list;
				Token temp_loop = null;
				i = 1;
				while (!((temp_loop = temp_condition_list.get(i++)).isType(TokenType.EOL)
						|| temp_loop.isType(TokenType.EOF))) {
					if (temp_loop.isType(TokenType.WHITESPACE) || temp_loop.isType(TokenType.WHILE)
							|| temp_loop.isType(TokenType.LOOP))
						continue;
					else if (temp_loop.isType(TokenType.EOF) || temp_loop.isType(TokenType.EOL))
						break;
					else {
						condition_list.add(temp_loop);
					}

				}
			}
			LexicalAnalyzer.not_adding_more_codeblock = false;
		}

		for (int i = 0; i < list.size(); i++) {
			Token temp = list.get(i);
			if (temp.isType(TokenType.PARENTHESIS_CODEBLOCK)) {
				evaluateTokens((ArrayList<Token>) temp.value);

				list.remove(i);
				list.add(i, temp);
			}
		}

		for (int j = 0; j < list.size(); j++) {
			cur_token = list.get(j);
			if (TokenType.isOperator(cur_token.type)) {
				if (TokenType.containsTokenType(list, TokenType.STRING))
					orderOfStringOp(list, j);
				else
					orderOfOp(list, j);
				break;
			}
		}
		for (int i = 0; i < list.size(); i++) {
			cur_token = list.get(i);
			if (cur_token.isType(TokenType.VARIABLE) && TokenType.referredTo(variables, cur_token)) {
				int index_v = TokenType.getFirstVariableIndex(variables, cur_token);
				Token temp_var = variables.get(index_v);
				list.remove(i);
				list.add(i, temp_var);
			}
		}
		for (int i = 0; i < list.size(); i++) {
			cur_token = list.get(i);
			Token var = null;
			if (cur_token.isType(TokenType.VARIABLE)) {
				var = cur_token;

				if (TokenType.containsTokenType(list, TokenType.LOOP) || TokenType.containsTokenType(list, TokenType.IF)
						|| TokenType.containsTokenType(list, TokenType.OUTPUT))
					break;
				boolean hasEqual = false;
				ArrayList<Token> temp = new ArrayList<Token>();
				Token t = null;
				for (int j = i + 1; j < list.size(); j++) {
					t = list.get(j);
					if (t.isType(TokenType.EOL) || t.isType(TokenType.EOF))
						break;
					if (t.isType(TokenType.WHITESPACE))
						continue;
					if (t.isType(TokenType.EQUAL)) {
						hasEqual = true;
						continue;
					}
					temp.add(t);
				}
				if (TokenType.referredTo(variables, cur_token) && hasEqual) {
					int index_v = TokenType.getFirstVariableIndex(variables, cur_token);
					Token temp_var = variables.get(index_v);
					variables.remove(index_v);
					try {
						evaluateTokens(temp);
						temp_var.variableRedefinition(extractResult(temp));
						if (TokenType.basicVarReferredTo(Interpreter.WINDOW.getCurrentWatchedVariables(), temp_var)) {
							Interpreter.WINDOW.submitVarChanges(temp_var, TokenType
									.getFirstVariableIndex(Interpreter.WINDOW.getCurrentWatchedVariables(), temp_var));
						}
						variables.add(index_v, temp_var);
					} catch (RuntimeError e) {
						throw new RuntimeError(e.getMessage(), line_num);
					}
					continue;
				} else if (!TokenType.containsToken(variables, cur_token) && hasEqual) {
					System.out.println("variable list: " + variables.toString());
					System.out.println("this is running for: " + var);
					System.out.println("temp var: " + temp);
					evaluateTokens(temp);
					System.out.println("new_temp: " + temp);
					var.variableDefinition(extractResult(temp));
					System.out.println("new var: " + var);
					if (TokenType.basicVarReferredTo(Interpreter.WINDOW.getCurrentWatchedVariables(), var)) {
						Interpreter.WINDOW.submitVarChanges(var,
								TokenType.getFirstVariableIndex(Interpreter.WINDOW.getCurrentWatchedVariables(), var));
					}
					variables.add(var);
				}
			}
		}
		for (int i = 0; i < list.size(); i++) {
			cur_token = list.get(i);
			if (TokenType.isComparator(cur_token.type)) {
				orderOfComp(list, i);
				break;
			}
		}
		for (int i = 0; i < list.size(); i++) {
			cur_token = list.get(i);
			if (TokenType.isOperator(cur_token.type)) {
				orderOfOp(list, i);
				break;
			}
		}
		for (int i = 0; i < list.size(); i++) {
			cur_token = list.get(i);
			if (cur_token.isType(TokenType.OUTPUT)) {
				ArrayList<Token> temp = new ArrayList<Token>();
				while (i++ < list.size() - 1)
					temp.add(list.get(i));
				evaluateTokens(temp);
				String output = "";
				LexicalAnalyzer.stripWhitespace(temp);
				for (int e = 0; e < temp.size(); e++) {
					Token cur = temp.get(e);
					if (cur.isType(TokenType.EOL) || cur.isType(TokenType.EOF))
						break;
					else {
						if (cur.isType(TokenType.VARIABLE)) {
							if (TokenType.referredTo(variables, cur))
								cur = variables.get(TokenType.getFirstVariableIndex(variables, cur));
							else
								throw new SyntaxError("No reference to the variable", cur, line_num);
						}
						output += cur.output();

					}
				}
				Interpreter.WINDOW.output(output);
			}
		}
		return list;
	}

	public static void debugCodeBlockExecution(ArrayList<ArrayList<Token>> codeblocks, int line)
			throws SyntaxError, RuntimeError, Exception {

		if (line < codeblocks.size()) {
			ArrayList<Token> codeblock = codeblocks.get(line);
			evaluateTokens(codeblock);
		}

	}

	public static void evaluateTokens() throws SyntaxError, RuntimeError, Exception {
		if (onlyAddingVar) {
			for (int i = 0; i < token_list.size(); i++) {
				if (token_list.get(i).isType(TokenType.VARIABLE)) {
					if (!TokenType.containsToken(variables, token_list.get(i)))
						variables.add(token_list.get(i));
				}
			}
			return;
		}
		if (TokenType.containsTokenType(token_list, TokenType.IF)) {
			LexicalAnalyzer.not_adding_more_codeblock = true;
			Interpreter.codeblock = "inactive";

			int i = 1;
			ArrayList<Token> condition_list = new ArrayList<Token>();
			while (!(cur_token = token_list.get(i++)).isType(TokenType.THEN)) {
				if (cur_token.isType(TokenType.WHITESPACE) || cur_token.isType(TokenType.IF))
					continue;
				else if (cur_token.isType(TokenType.EOF) || cur_token.isType(TokenType.EOL))
					break;
				else
					condition_list.add(cur_token);
			}
			// System.out.println("temp1:" + condition_list);
			evaluateTokens(condition_list);

			boolean condition = evaluateBoolean(condition_list);
			System.out.println("codeblocks: " + LexicalAnalyzer.codeblocks);
			ArrayList<Token> codeblock = LexicalAnalyzer.codeblocks.get(0);
			LexicalAnalyzer.codeblocks.remove(0);
			System.out.println("if codeblock: " + codeblock.toString());
			if (condition) {
				// evaluateTokens(codeblock);
				System.out.println("codeblocks: " + LexicalAnalyzer.codeblocks);
				ArrayList<Token> segment = new ArrayList<Token>();
				ArrayList<Token> newCB = new ArrayList<Token>();
				int t = 0;
				while (t < codeblock.size()) {
					Token temp = codeblock.get(t);
					if (!(temp.isType(TokenType.EOL) || temp.isType(TokenType.EOF))) {
						segment.add(temp);
						t++;
					} else {
						segment.add(temp);
						evaluateTokens(segment);

						while (segment.size() > 0) {
							System.out.println(segment.get(0));
							newCB.add(segment.get(0));
							segment.remove(0);
						}
						t++;
					}
				}
				codeblock = newCB;
				LexicalAnalyzer.codeblocks.clear();
			} else {
				while (LexicalAnalyzer.else_ifs_count > 0) {
					i = 0;
					condition_list = new ArrayList<Token>();
					token_list = LexicalAnalyzer.codeblocks.get(0);
					LexicalAnalyzer.codeblocks.remove(0);
					while (!(cur_token = token_list.get(i++)).isType(TokenType.THEN)) {
						if (cur_token.isType(TokenType.WHITESPACE) || cur_token.isType(TokenType.ELSE)
								|| cur_token.isType(TokenType.IF))
							continue;
						else if (cur_token.isType(TokenType.EOF) || cur_token.isType(TokenType.EOL))
							break;
						else
							condition_list.add(cur_token);
					}
					System.out.println("condition_list: " + condition_list);
					evaluateTokens(condition_list);
					condition = evaluateBoolean(condition_list);
					System.out.println("condition: " + condition);
					codeblock = LexicalAnalyzer.codeblocks.get(0);
					LexicalAnalyzer.codeblocks.remove(0);
					LexicalAnalyzer.else_ifs_count--;
					System.out.println("if codeblock: " + codeblock.toString());
					if (condition) {
						ArrayList<Token> segment = new ArrayList<Token>();
						ArrayList<Token> newCB = new ArrayList<Token>();
						int t = 0;

						while (t < codeblock.size()) {
							Token temp = codeblock.get(t);
							if (!(temp.isType(TokenType.EOL) || temp.isType(TokenType.EOF))) {
								segment.add(temp);
								t++;
							} else {
								segment.add(temp);
								evaluateTokens(segment);

								while (segment.size() > 0) {
									System.out.println(segment.get(0));
									newCB.add(segment.get(0));
									segment.remove(0);
								}
								t++;
							}
						}
						codeblock = newCB;
						while (LexicalAnalyzer.else_ifs_count > 0) {
							LexicalAnalyzer.codeblocks.remove(0);
							LexicalAnalyzer.else_ifs_count--;
						}
						if (LexicalAnalyzer.has_else)
							LexicalAnalyzer.codeblocks.remove(0);
						LexicalAnalyzer.has_else = false;
						LexicalAnalyzer.codeblocks.clear();
					}

				}

				if (LexicalAnalyzer.has_else) {
					codeblock = LexicalAnalyzer.codeblocks.get(0);
					System.out.println("else codeblock: " + codeblock.toString());
					LexicalAnalyzer.codeblocks.remove(0);
					ArrayList<Token> segment = new ArrayList<Token>();
					ArrayList<Token> newCB = new ArrayList<Token>();
					int t = 0;
					while (t < codeblock.size()) {
						Token temp = codeblock.get(t);
						if (!(temp.isType(TokenType.EOL) || temp.isType(TokenType.EOF))) {
							segment.add(temp);
							t++;
						} else {
							segment.add(temp);
							evaluateTokens(segment);

							while (segment.size() > 0) {
								System.out.println(segment.get(0));
								newCB.add(segment.get(0));
								segment.remove(0);
							}
							t++;
						}
					}
					codeblock = newCB;
					LexicalAnalyzer.has_else = false;
				}
			}
			LexicalAnalyzer.not_adding_more_codeblock = false;
			Interpreter.codeblock = "inactive";
			LexicalAnalyzer.codeblocks.clear();
		}
		if (TokenType.containsTokenType(token_list, TokenType.LOOP)) {
			LexicalAnalyzer.not_adding_more_codeblock = true;
			int i = 1;
			// ArrayList<Token> original_condition_list = new ArrayList<Token>();
			ArrayList<Token> condition_list = new ArrayList<Token>();
			while (!((cur_token = token_list.get(i++)).isType(TokenType.EOL) || cur_token.isType(TokenType.EOF))) {
				if (cur_token.isType(TokenType.WHITESPACE) || cur_token.isType(TokenType.WHILE)
						|| cur_token.isType(TokenType.LOOP))
					continue;
				else if (cur_token.isType(TokenType.EOF) || cur_token.isType(TokenType.EOL))
					break;
				else {
					condition_list.add(cur_token);
				}

			}

			ArrayList<Token> codeblock = null;
			try {
				codeblock = LexicalAnalyzer.codeblocks.get(0);
			} catch (IndexOutOfBoundsException e) {
				throw new RuntimeError("No codeblock for loop", line_num);
			}
			LexicalAnalyzer.codeblocks.remove(0);
			while (evaluateBoolean(condition_list)) {

				ArrayList<Token> segment = new ArrayList<Token>();
				ArrayList<Token> newCB = new ArrayList<Token>();
				int t = 0;
				while (t < codeblock.size()) {
					Token temp = codeblock.get(t);
					if (!(temp.isType(TokenType.EOL) || temp.isType(TokenType.EOF))) {
						segment.add(temp);
						t++;
					} else {
						segment.add(temp);
						evaluateTokens(segment);
						while (segment.size() > 0) {
							System.out.println(segment.get(0));
							newCB.add(segment.get(0));
							segment.remove(0);
						}
						t++;
					}
				}
				condition_list.clear();
				LexicalAnalyzer.token_list.clear();
				LexicalAnalyzer.setInput(Interpreter.loop_text);
				LexicalAnalyzer.lexicalSplit();
				LexicalAnalyzer.getNextLine();
				LexicalAnalyzer.secondLevelTokenizer();
				ArrayList<Token> temp_condition_list = LexicalAnalyzer.token_list;
				Token temp_loop = null;
				i = 1;
				while (!((temp_loop = temp_condition_list.get(i++)).isType(TokenType.EOL)
						|| temp_loop.isType(TokenType.EOF))) {
					if (temp_loop.isType(TokenType.WHITESPACE) || temp_loop.isType(TokenType.WHILE)
							|| temp_loop.isType(TokenType.LOOP))
						continue;
					else if (temp_loop.isType(TokenType.EOF) || temp_loop.isType(TokenType.EOL))
						break;
					else {
						// original_condition_list.add(new Token(cur_token.type,cur_token.value));
						condition_list.add(temp_loop);
					}

				}
				System.out.println("Conditions: " + condition_list);
				// System.out.println("original: " + Arrays.toString(ocl));

				// new Scanner(System.in).nextLine();
			}
			LexicalAnalyzer.not_adding_more_codeblock = false;
		}

		// System.out.println("current line size: "+cur_line.size());
		for (int j = 0; j < token_list.size(); j++) {
			cur_token = token_list.get(j);
			if (TokenType.isOperator(cur_token.type)) {
				// DONE: ADD STRING CONCAT METHOD HERE
				if (TokenType.containsTokenType(token_list, TokenType.STRING))
					orderOfStringOp(j);
				else
					orderOfOp(j);
				break;
				// System.out.println(token_list.toString());
			}
		}
		for (int j = 0; j < token_list.size(); j++) {
			cur_token = token_list.get(j);
			if(cur_token.type!=null)
			if (TokenType.isComparator(cur_token.type)) {
				if (cur_token.isType(TokenType.EQUAL)
						&& getBeforeToken(j).values().iterator().next().isType(TokenType.VARIABLE))
					;
				else {
					orderOfComp(j);
					break;
				}
				// System.out.println(token_list.toString());
			}
		}

		for (int i = 0; i < token_list.size(); i++) {
			cur_token = token_list.get(i);
			if (cur_token.isType(TokenType.VARIABLE)) {
				boolean hasEqual = false;
				Token temp = null;
				for (int j = i + 1; j < token_list.size(); j++) {
					temp = token_list.get(j);
					if (temp.isType(TokenType.EOL) || temp.isType(TokenType.EOF))
						break;
					if (temp.isType(TokenType.WHITESPACE))
						continue;
					if (temp.isType(TokenType.EQUAL)) {
						hasEqual = true;
						continue;
					}
					break;
				}
				if (TokenType.referredTo(variables, cur_token) && hasEqual) {
					int index_v = TokenType.getFirstVariableIndex(variables, cur_token);
					Token temp_var = variables.get(index_v);
					variables.remove(index_v);
					try {
						temp_var.variableRedefinition(temp);
						if (TokenType.basicVarReferredTo(Interpreter.WINDOW.getCurrentWatchedVariables(), temp_var)) {
							Interpreter.WINDOW.submitVarChanges(temp_var, TokenType
									.getFirstVariableIndex(Interpreter.WINDOW.getCurrentWatchedVariables(), temp_var));
						}
						variables.add(index_v, temp_var);
					} catch (RuntimeError e) {
						throw new RuntimeError(e.getMessage(), line_num);
					}
					continue;
				} else if (!TokenType.referredTo(variables, cur_token) && hasEqual) {
					cur_token.variableDefinition(temp);
					if (TokenType.basicVarReferredTo(Interpreter.WINDOW.getCurrentWatchedVariables(), cur_token)) {
						Interpreter.WINDOW.submitVarChanges(cur_token, TokenType
								.getFirstVariableIndex(Interpreter.WINDOW.getCurrentWatchedVariables(), cur_token));
					}
					variables.add(cur_token);
				} else if (!TokenType.referredTo(variables, cur_token) && !hasEqual) {
					throw new SyntaxError("Unknown variable", cur_token, line_num);
				}
			}
		}
		for (int i = 0; i < token_list.size(); i++) {
			cur_token = token_list.get(i);
			if (cur_token.isType(TokenType.OUTPUT)) {
				ArrayList<Token> temp = new ArrayList<Token>();
				while (i++ < token_list.size() - 1)
					temp.add(token_list.get(i));
				evaluateTokens(temp);
				String output = "";
				LexicalAnalyzer.stripWhitespace(temp);
				for (int e = 0; e < temp.size(); e++) {
					if (temp.get(e).isType(TokenType.EOL))
						;
					else if (temp.get(e).isType(TokenType.EOF))
						break;
					else {
						output += temp.get(e).output();
					}
				}
				Interpreter.WINDOW.output(output);
			}
		}
	}

	@SuppressWarnings({ "unchecked", "null" })
	public static boolean evaluateBoolean(ArrayList<Token> list) throws RuntimeError, SyntaxError, Exception {
		Token temp = null;
		for (int i = 0; i < list.size(); i++) {
			temp = list.get(i);
			if (temp.isType(TokenType.PARENTHESIS_CODEBLOCK)) {
				evaluateBoolean((ArrayList<Token>) temp.value);
				list.remove(i);
				list.add(i, extractResult((ArrayList<Token>) temp.value));
			}
		}
		for (int i = 0; i < list.size(); i++) {
			temp = list.get(i);
			if (temp.isType(TokenType.VARIABLE)) {
				// boolean hasEqual = false;
				if (TokenType.referredTo(variables, temp)) {
					int index_v = TokenType.getFirstVariableIndex(variables, temp);
					Token temp_var = variables.get(index_v);
					list.remove(i);
					list.add(i, ((HashMap<String, Token>) temp_var.value).values().iterator().next());
				}
			}
		}
		for (int j = 0; j < list.size(); j++) {
			temp = list.get(j);
			if (TokenType.isOperator(temp.type)) {
				list = orderOfOp(list, j);
				break;
			}
		}

		for (int j = 0; j < list.size(); j++) {
			temp = list.get(j);
			// System.out.println(j);
			if (TokenType.isComparator(temp.type)) {
				if (temp.isType(TokenType.EQUAL)
						&& getBeforeToken(list, j).values().iterator().next().isType(TokenType.VARIABLE))
					;
				else {
					list = orderOfComp(list, j);
					break;
				}
			}
		}
		LexicalAnalyzer.stripWhitespace(list);
		System.out.println("evaluated list:" + list.toString());
		if (list.get(0).isType(TokenType.BOOLEAN))
			return (boolean) list.get(0).value;
		return (Boolean) null;
	}

	public static HashMap<Integer, Token> getBeforeToken(int indexOfOp) {
		int tempIndex = indexOfOp - 1;
		Token temp = token_list.get(tempIndex);
		HashMap<Integer, Token> result = new HashMap<Integer, Token>();
		result.put(tempIndex, temp);
		while (!temp.isType(TokenType.EOL) && tempIndex >= 0) {
			temp = token_list.get(tempIndex);
			if (temp.isType(TokenType.WHITESPACE))
				;
			else {
				result.clear();
				result.put(tempIndex, temp);
				return result;
			}
			tempIndex--;
		}
		return null;
	}

	public static HashMap<Integer, Token> getAfterToken(int indexOfOp) throws RuntimeError {
		if (indexOfOp == token_list.size() - 1)
			return null;
		else if (indexOfOp == token_list.size())
			throw new RuntimeError("Unable to get after token", line_num);
		int tempIndex = indexOfOp + 1;
		Token temp = token_list.get(tempIndex);
		HashMap<Integer, Token> result = new HashMap<Integer, Token>();
		result.put(tempIndex, temp);
		while (!temp.isType(TokenType.EOL) || !temp.isType(TokenType.EOF)) {
			temp = token_list.get(tempIndex);
			if (temp.isType(TokenType.WHITESPACE))
				;
			else {
				result.clear();
				result.put(tempIndex, temp);
				return result;
			}
			tempIndex++;
		}
		return null;
	}

	public static HashMap<Integer, Token> getBeforeToken(ArrayList<Token> list, int indexOfOp) {
		if (indexOfOp == 0)
			return null;
		int tempIndex = indexOfOp - 1;
		Token temp = list.get(tempIndex);
		HashMap<Integer, Token> result = new HashMap<Integer, Token>();
		result.put(tempIndex, temp);
		while (!temp.isType(TokenType.EOL) && tempIndex >= 0) {
			temp = list.get(tempIndex);
			if (temp.isType(TokenType.WHITESPACE))
				;
			else {
				result.clear();
				result.put(tempIndex, temp);
				return result;
			}
			tempIndex--;
		}
		return null;
	}

	public static HashMap<Integer, Token> getAfterToken(ArrayList<Token> list, int indexOfOp) {

		int tempIndex = indexOfOp + 1;
		if (tempIndex >= list.size())
			return null;

		Token temp = list.get(tempIndex);
		HashMap<Integer, Token> result = new HashMap<Integer, Token>();
		while (tempIndex < list.size() && (!temp.isType(TokenType.EOL) || !temp.isType(TokenType.EOF))) {
			temp = list.get(tempIndex);
			if (temp.isType(TokenType.WHITESPACE))
				;
			else {
				result.clear();
				result.put(tempIndex, temp);
				return result;
			}
			tempIndex++;
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	public static Token opNumTokens(int indexOfOp, TokenType op) throws Exception {
		HashMap<Integer, Token> beforeToken = getBeforeToken(indexOfOp);
		HashMap<Integer, Token> afterToken = getAfterToken(indexOfOp);
		// System.out.println("B4 T: " + beforeToken.toString());
		// System.out.println("AF T: " + afterToken.toString());
		int before_i = beforeToken.keySet().iterator().next();
		int after_i = afterToken.keySet().iterator().next();
		token_list.remove(before_i);
		token_list.remove(after_i - 1);
		token_list.remove(indexOfOp - 1);

		Token result = new Token(null);
		Token before = beforeToken.values().iterator().next();
		Token after = afterToken.values().iterator().next();
		System.out.println("Before:" + before);
		System.out.println("AFTER: " + after);
		if (before.isType(TokenType.VARIABLE) && TokenType.referredTo(variables, before)) {
			before = variables.get(TokenType.getFirstVariableIndex(variables, before));
			before = ((HashMap<String, Token>) before.value).values().iterator().next();
		}
		if (after.isType(TokenType.VARIABLE) && TokenType.referredTo(variables, after)) {
			after = variables.get(TokenType.getFirstVariableIndex(variables, after));
			after = ((HashMap<String, Token>) after.value).values().iterator().next();
		}
		if (before.isType(TokenType.FLOAT) || after.isType(TokenType.FLOAT))
			result.type = TokenType.FLOAT;
		else if (before.isType(TokenType.INTEGER) && after.isType(TokenType.INTEGER))
			result.type = TokenType.INTEGER;
		else
			throw new SyntaxError("Illegal Number Type: ", after, line_num);
		if (op.equals(TokenType.PLUS)) {
			if (result.isType(TokenType.INTEGER))
				result.value = (Integer) before.value + (Integer) after.value;
			else if (result.isType(TokenType.FLOAT))
				result.value = (before.isType(TokenType.FLOAT) ? (Double) before.value : (Integer) before.value)
						+ (after.isType(TokenType.FLOAT) ? (Double) after.value : (Integer) after.value);
			// System.out.println(result);
			token_list.add(beforeToken.keySet().iterator().next(), result);
			return result;
		} else if (op.equals(TokenType.MINUS)) {
			if (result.isType(TokenType.INTEGER))
				result.value = (Integer) before.value - (Integer) after.value;
			else if (result.isType(TokenType.FLOAT))
				result.value = (before.isType(TokenType.FLOAT) ? (Double) before.value : (Integer) before.value)
						- (after.isType(TokenType.FLOAT) ? (Double) after.value : (Integer) after.value);
			// System.out.println(result);
			token_list.add(beforeToken.keySet().iterator().next(), result);
			return result;
		} else if (op.equals(TokenType.MULTIPLY)) {
			if (result.isType(TokenType.INTEGER))
				result.value = (Integer) before.value * (Integer) after.value;
			else if (result.isType(TokenType.FLOAT))
				result.value = (before.isType(TokenType.FLOAT) ? (Double) before.value : (Integer) before.value)
						* (after.isType(TokenType.FLOAT) ? (Double) after.value : (Integer) after.value);
			// System.out.println(result);
			token_list.add(beforeToken.keySet().iterator().next(), result);
			return result;
		} else if (op.equals(TokenType.DIVIDE)) {
			if (result.isType(TokenType.INTEGER))
				result.value = (Integer) before.value / (Integer) after.value;
			else if (result.isType(TokenType.FLOAT))
				result.value = (before.isType(TokenType.FLOAT) ? (Double) before.value : (Integer) before.value)
						/ (after.isType(TokenType.FLOAT) ? (Double) after.value : (Integer) after.value);
			token_list.add(beforeToken.keySet().iterator().next(), result);
			return result;
		} else if (op.equals(TokenType.MODULO)) {
			if (result.isType(TokenType.INTEGER))
				result.value = (Integer) before.value % (Integer) after.value;
			else if (result.isType(TokenType.FLOAT))
				result.value = (before.isType(TokenType.FLOAT) ? (Double) before.value : (Integer) before.value)
						% (after.isType(TokenType.FLOAT) ? (Double) after.value : (Integer) after.value);
			token_list.add(beforeToken.keySet().iterator().next(), result);
			return result;
		}
		return null;
	}

	public static void orderOfOp(int indexOfStart) throws Exception {
		ArrayList<Token> operators = new ArrayList<Token>();
		for (int x = indexOfStart; x < token_list.size(); x++)
			if (TokenType.isOperator(token_list.get(x).type))
				operators.add(token_list.get(x));

		while (!operators.isEmpty()) {
			int firstMultiply = -1;
			int firstDivide = -1;

			if (TokenType.containsTokenType(operators, TokenType.MULTIPLY))
				firstMultiply = TokenType.getFirstTokenTypeIndex(operators, TokenType.MULTIPLY);
			if (TokenType.containsTokenType(operators, TokenType.DIVIDE))
				firstDivide = TokenType.getFirstTokenTypeIndex(operators, TokenType.DIVIDE);
			boolean m = firstMultiply == -1, n = firstDivide == -1;
			int cur_op_index = -1;
			if (!m && !n) {
				if (firstMultiply > (cur_op_index = firstDivide)) {
					operators.remove(cur_op_index);
					opNumTokens(TokenType.getFirstTokenTypeIndex(token_list, TokenType.DIVIDE), TokenType.DIVIDE);
				} else {
					cur_op_index = firstMultiply;
					operators.remove(cur_op_index);
					opNumTokens(TokenType.getFirstTokenTypeIndex(token_list, TokenType.MULTIPLY), TokenType.MULTIPLY);
				}
			} else if (m && !n) {
				cur_op_index = firstDivide;
				operators.remove(cur_op_index);
				opNumTokens(TokenType.getFirstTokenTypeIndex(token_list, TokenType.DIVIDE), TokenType.DIVIDE);
			} else if (!m && n) {
				cur_op_index = firstMultiply;
				operators.remove(cur_op_index);
				opNumTokens(TokenType.getFirstTokenTypeIndex(token_list, TokenType.MULTIPLY), TokenType.MULTIPLY);
			} else if (m && n) {
				opNumTokens(TokenType.getFirstTokenTypeIndex(token_list, operators.get(0).type), operators.get(0).type);
				operators.remove(0);
			}

		}

	}

	@SuppressWarnings("unchecked")
	public static Token opBoolTokens(int indexOfOp, TokenType op) throws SyntaxError, Exception, RuntimeError {
		HashMap<Integer, Token> afterToken = getAfterToken(indexOfOp);
	
		int after_i = afterToken.keySet().iterator().next();
		Token result = new Token(TokenType.BOOLEAN);
		Token after = afterToken.values().iterator().next();

		if (op == TokenType.NOT && (after.isType(TokenType.BOOLEAN) || TokenType.isComparator(after.type))) {
			if (TokenType.isComparator(after.type)) {
				token_list.remove(indexOfOp);
				HashMap<Integer, Token> beforeToken = getBeforeToken(indexOfOp);
				int before_i = beforeToken.keySet().iterator().next();
				result.value = !(Boolean) opBoolTokens(after_i - 1, after.type).value;
				token_list.remove(before_i);
				token_list.add(before_i, result);
				return result;
			} else {
				token_list.remove(after_i);
				token_list.remove(indexOfOp);
				result.value = !(Boolean) after.value;
				token_list.add(indexOfOp, result);
				return result;
			}
		}

		HashMap<Integer, Token> beforeToken = getBeforeToken(indexOfOp);
		int before_i = beforeToken.keySet().iterator().next();
		Token before = beforeToken.values().iterator().next();
		if (before.isType(TokenType.VARIABLE) && TokenType.referredTo(variables, before)) {
			before = variables.get(TokenType.getFirstVariableIndex(variables, before));
			before = ((HashMap<String, Token>) before.value).values().iterator().next();
		}
		if (after.isType(TokenType.VARIABLE) && TokenType.referredTo(variables, after)) {
			after = variables.get(TokenType.getFirstVariableIndex(variables, after));
			after = ((HashMap<String, Token>) after.value).values().iterator().next();
		}
		token_list.remove(before_i);
		token_list.remove(after_i - 1);
		token_list.remove(indexOfOp - 1);

		if (before.isType(TokenType.BOOLEAN) && after.isType(TokenType.BOOLEAN))
			;
		else if ((before.isType(TokenType.FLOAT) || before.isType(TokenType.INTEGER))
				&& (after.isType(TokenType.INTEGER) || after.isType(TokenType.FLOAT)))
			;
		else
			throw new SyntaxError("Illegal boolean arguments: ", after, line_num);
		switch (op) {
		case EQUAL:
			
			if (before.isType(TokenType.BOOLEAN) && after.isType(TokenType.BOOLEAN))
				result.value = (Boolean) before.value == (Boolean) after.value;
			else if ((before.isType(TokenType.FLOAT) || before.isType(TokenType.INTEGER))
					&& (after.isType(TokenType.INTEGER) || after.isType(TokenType.FLOAT)))
				result.value = ((before.isType(TokenType.FLOAT)) ? ((Double) before.value)
						: ((Integer) before.value)) == ((after.isType(TokenType.FLOAT)) ? ((Double) after.value)
								: ((Integer) after.value));
			token_list.add(beforeToken.keySet().iterator().next(), result);
			return result;
		case MORE:
			result.value = ((before.isType(TokenType.FLOAT)) ? ((Double) before.value)
					: ((Integer) before.value)) > ((after.isType(TokenType.FLOAT)) ? ((Double) after.value)
							: ((Integer) after.value));
			token_list.add(beforeToken.keySet().iterator().next(), result);
			return result;
		case MOREEQ:
			result.value = ((before.isType(TokenType.FLOAT)) ? ((Double) before.value)
					: ((Integer) before.value)) >= ((after.isType(TokenType.FLOAT)) ? ((Double) after.value)
							: ((Integer) after.value));
			token_list.add(beforeToken.keySet().iterator().next(), result);
			return result;
		case LESS:
			result.value = ((before.isType(TokenType.FLOAT)) ? ((Double) before.value)
					: ((Integer) before.value)) < ((after.isType(TokenType.FLOAT)) ? ((Double) after.value)
							: ((Integer) after.value));
			token_list.add(beforeToken.keySet().iterator().next(), result);
			return result;
		case LESSEQ:
			result.value = ((before.isType(TokenType.FLOAT)) ? ((Double) before.value)
					: ((Integer) before.value)) <= ((after.isType(TokenType.FLOAT)) ? ((Double) after.value)
							: ((Integer) after.value));
			token_list.add(beforeToken.keySet().iterator().next(), result);
			return result;
		default:
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	public static Token opBoolTokens(ArrayList<Token> list, int indexOfOp, TokenType op) throws SyntaxError, Exception {
		System.out.println("indexOfOp: " + indexOfOp);
		HashMap<Integer, Token> afterToken = getAfterToken(list, indexOfOp);

		int after_i = afterToken.keySet().iterator().next();
		Token result = new Token(TokenType.BOOLEAN);
		Token after = afterToken.values().iterator().next();
		// System.out.println(op);

		if (op == TokenType.NOT && (after.isType(TokenType.BOOLEAN) || TokenType.isComparator(after.type))) {
			if (TokenType.isComparator(after.type)) {
				list.remove(indexOfOp);
				HashMap<Integer, Token> beforeToken = getBeforeToken(list, indexOfOp);
				int before_i = beforeToken.keySet().iterator().next();
				// Token before = beforeToken.values().iterator().next();

				result.value = !(Boolean) opBoolTokens(list, after_i - 1, after.type).value;
				list.remove(before_i);
				list.add(before_i, result);
				return result;
			} else {
				list.remove(after_i);
				list.remove(indexOfOp);
				result.value = !(Boolean) after.value;
				list.add(indexOfOp, result);
				return result;
			}
			// System.out.println(result);
		}

		HashMap<Integer, Token> beforeToken = getBeforeToken(list, indexOfOp);
		int before_i = beforeToken.keySet().iterator().next();
		Token before = beforeToken.values().iterator().next();
		if (before.isType(TokenType.VARIABLE) && TokenType.referredTo(variables, before)) {
			before = variables.get(TokenType.getFirstVariableIndex(variables, before));
			before = ((HashMap<String, Token>) before.value).values().iterator().next();
		}
		if (after.isType(TokenType.VARIABLE) && TokenType.referredTo(variables, after)) {
			after = variables.get(TokenType.getFirstVariableIndex(variables, after));
			after = ((HashMap<String, Token>) after.value).values().iterator().next();
		}

		list.remove(before_i);
		list.remove(after_i - 1);
		list.remove(indexOfOp - 1);

		// System.out.println("Before:"+before);
		if (before.isType(TokenType.BOOLEAN) && after.isType(TokenType.BOOLEAN))
			;
		else if ((before.isType(TokenType.FLOAT) || before.isType(TokenType.INTEGER))
				&& (after.isType(TokenType.INTEGER) || after.isType(TokenType.FLOAT)))
			;
		else
			throw new SyntaxError("Illegal Number Type", after, line_num);
		switch (op) {
		case EQUAL:
			// System.out.println("before:"+before);
			// System.out.println("after:"+after);
			// System.out.println();
			if (before.isType(TokenType.BOOLEAN) && after.isType(TokenType.BOOLEAN))
				result.value = (Boolean) before.value == (Boolean) after.value;
			else if ((before.isType(TokenType.FLOAT) || before.isType(TokenType.INTEGER))
					&& (after.isType(TokenType.INTEGER) || after.isType(TokenType.FLOAT)))
				result.value = ((before.isType(TokenType.FLOAT)) ? ((Double) before.value)
						: ((Integer) before.value)) == ((after.isType(TokenType.FLOAT)) ? ((Double) after.value)
								: ((Integer) after.value));
			list.add(beforeToken.keySet().iterator().next(), result);
			return result;
		case MORE:
			result.value = ((before.isType(TokenType.FLOAT)) ? ((Double) before.value)
					: ((Integer) before.value)) > ((after.isType(TokenType.FLOAT)) ? ((Double) after.value)
							: ((Integer) after.value));
			// System.out.println(result);
			list.add(beforeToken.keySet().iterator().next(), result);
			return result;
		case MOREEQ:
			result.value = ((before.isType(TokenType.FLOAT)) ? ((Double) before.value)
					: ((Integer) before.value)) >= ((after.isType(TokenType.FLOAT)) ? ((Double) after.value)
							: ((Integer) after.value));
			// System.out.println(result);
			list.add(beforeToken.keySet().iterator().next(), result);
			return result;
		case LESS:
			result.value = ((before.isType(TokenType.FLOAT)) ? ((Double) before.value)
					: ((Integer) before.value)) < ((after.isType(TokenType.FLOAT)) ? ((Double) after.value)
							: ((Integer) after.value));
			// System.out.println(result);
			list.add(beforeToken.keySet().iterator().next(), result);
			return result;
		case LESSEQ:
			result.value = ((before.isType(TokenType.FLOAT)) ? ((Double) before.value)
					: ((Integer) before.value)) <= ((after.isType(TokenType.FLOAT)) ? ((Double) after.value)
							: ((Integer) after.value));
			// System.out.println(result);
			list.add(beforeToken.keySet().iterator().next(), result);
			return result;
		default:
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	public static void orderOfComp(int indexOfStart) throws SyntaxError, Exception, RuntimeError {
		ArrayList<Token> operators = new ArrayList<Token>();
		if (TokenType.containsTokenType(token_list, TokenType.OPEN_BRACKET)) {
			LexicalAnalyzer.secondLevelTokenizer(token_list);
		}

		if (TokenType.containsTokenType(token_list, TokenType.PARENTHESIS_CODEBLOCK)) {
			int index_o_p = TokenType.getFirstTokenTypeIndex(token_list, TokenType.PARENTHESIS_CODEBLOCK);
			Token parenthesis = token_list.get(index_o_p);
			ArrayList<Token> temp = (ArrayList<Token>) parenthesis.value;
			orderOfComp(temp, 0);
			parenthesis.value = temp;
			boolean result = evaluateBoolean(temp);
			token_list.remove(index_o_p);
			token_list.add(index_o_p, new Token(TokenType.BOOLEAN, result));
		}

		for (int x = indexOfStart; x < token_list.size(); x++)
			if (TokenType.isComparator(token_list.get(x).type))
				operators.add(token_list.get(x));
		// NXAO
		while (!operators.isEmpty()) {
			if (TokenType.containsTokenType(operators, TokenType.NOT)) {
				opBoolTokens(TokenType.getFirstTokenTypeIndex(token_list, TokenType.NOT), TokenType.NOT);
				operators.remove(TokenType.getFirstTokenTypeIndex(operators, TokenType.NOT));
				operators.clear();
				for (int x = indexOfStart; x < token_list.size(); x++)
					if (TokenType.isComparator(token_list.get(x).type))
						operators.add(token_list.get(x));
			} else {
				opBoolTokens(TokenType.getFirstTokenTypeIndex(token_list, operators.get(0).type),
						operators.get(0).type);
				operators.remove(0);
			}

		}

	}

	@SuppressWarnings("unchecked")
	public static ArrayList<Token> orderOfComp(ArrayList<Token> list, int indexOfStart)
			throws SyntaxError, Exception, RuntimeError {
		ArrayList<Token> operators = new ArrayList<Token>();
		// DONE: Handle parentheses
		if (TokenType.containsTokenType(list, TokenType.OPEN_BRACKET)) {
			LexicalAnalyzer.secondLevelTokenizer(list);
			evaluateTokens(list);
		}
		// System.out.println(list.toString());
		if (TokenType.containsTokenType(list, TokenType.PARENTHESIS_CODEBLOCK)) {
			System.out.println("codeblock is evaluating");
			int index_o_p = TokenType.getFirstTokenTypeIndex(list, TokenType.PARENTHESIS_CODEBLOCK);
			Token parenthesis = list.get(index_o_p);
			ArrayList<Token> temp = (ArrayList<Token>) parenthesis.value;
			orderOfComp(temp, 0);
			parenthesis.value = temp;
			System.out.println("evaluated list: " + temp);

			boolean result = evaluateBoolean(temp);
			System.out.println("extracted: " + result);
			list.remove(index_o_p);
			list.add(index_o_p, new Token(TokenType.BOOLEAN, result));
			System.out.println("updated List: " + list.toString());
		}

		for (int x = indexOfStart; x < list.size(); x++)
			if (TokenType.isComparator(list.get(x).type)) {

				// DONE: IGNORE THE VARIABLE REASSIGNMENTS
				HashMap<Integer, Token> before1 = getBeforeToken(list, x);
				// System.out.println(before1.toString());
				HashMap<Integer, Token> before2 = null;
				if (before1 != null)
					before2 = getBeforeToken(list, before1.keySet().iterator().next());

				if (list.get(x).isType(TokenType.EQUAL) && before1 != null
						&& before1.values().iterator().next().isType(TokenType.VARIABLE) && (before2 == null))
					;
				else
					operators.add(list.get(x));

			}
		// NXAO
		while (!operators.isEmpty()) {
			System.out.println("operating: " + list.toString());
			if (TokenType.containsTokenType(operators, TokenType.NOT)) {
				opBoolTokens(list, TokenType.getFirstTokenTypeIndex(list, TokenType.NOT), TokenType.NOT);
				operators.remove(TokenType.getFirstTokenTypeIndex(operators, TokenType.NOT));
				operators.clear();
				for (int x = indexOfStart; x < list.size(); x++)
					if (TokenType.isComparator(list.get(x).type))
						operators.add(list.get(x));
			} else {
				opBoolTokens(list, TokenType.getFirstTokenTypeIndex(list, operators.get(0).type),
						operators.get(0).type);
				operators.remove(0);
			}
		}
		return list;
	}

	public static ArrayList<Token> orderOfOp(ArrayList<Token> list, int indexOfStart) throws Exception {
		ArrayList<Token> operators = new ArrayList<Token>();
		for (int x = indexOfStart; x < list.size(); x++)
			if (TokenType.isOperator(list.get(x).type))
				operators.add(list.get(x));

		while (!operators.isEmpty()) {
			// System.out.println(token_list.toString());
			int firstMultiply = -1;
			int firstDivide = -1;

			if (TokenType.containsTokenType(operators, TokenType.MULTIPLY))
				firstMultiply = TokenType.getFirstTokenTypeIndex(operators, TokenType.MULTIPLY);
			if (TokenType.containsTokenType(operators, TokenType.DIVIDE))
				firstDivide = TokenType.getFirstTokenTypeIndex(operators, TokenType.DIVIDE);
			boolean m = firstMultiply == -1, n = firstDivide == -1;
			int cur_op_index = -1;
			if (!m && !n) {
				if (firstMultiply > (cur_op_index = firstDivide)) {
					operators.remove(cur_op_index);
					opNumTokens(list, TokenType.getFirstTokenTypeIndex(list, TokenType.DIVIDE), TokenType.DIVIDE);
				} else {
					cur_op_index = firstMultiply;
					operators.remove(cur_op_index);
					opNumTokens(list, TokenType.getFirstTokenTypeIndex(list, TokenType.MULTIPLY), TokenType.MULTIPLY);
				}
			} else if (m && !n) {
				cur_op_index = firstDivide;
				operators.remove(cur_op_index);
				opNumTokens(list, TokenType.getFirstTokenTypeIndex(list, TokenType.DIVIDE), TokenType.DIVIDE);
			} else if (!m && n) {
				cur_op_index = firstMultiply;
				operators.remove(cur_op_index);
				opNumTokens(list, TokenType.getFirstTokenTypeIndex(list, TokenType.MULTIPLY), TokenType.MULTIPLY);
			} else if (m && n) {
				opNumTokens(list, TokenType.getFirstTokenTypeIndex(list, operators.get(0).type), operators.get(0).type);
				operators.remove(0);
			}

		}
		return list;
	}

	/**
	 * To be used as stand-alone operations method for {@link Token} in an
	 * {@link ArrayList}.
	 * 
	 * @param list
	 * @param indexOfOp
	 * @param op
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public static Token opNumTokens(ArrayList<Token> list, int indexOfOp, TokenType op) throws Exception {
		HashMap<Integer, Token> beforeToken = getBeforeToken(list, indexOfOp);
		HashMap<Integer, Token> afterToken = getAfterToken(list, indexOfOp);
		// System.out.println("B4 T: " + beforeToken.toString());
		// System.out.println("AF T: " + afterToken.toString());
		int before_i = beforeToken.keySet().iterator().next();
		int after_i = afterToken.keySet().iterator().next();
		list.remove(before_i);
		list.remove(after_i - 1);
		list.remove(indexOfOp - 1);

		Token result = new Token(null);
		Token before = beforeToken.values().iterator().next();
		Token after = afterToken.values().iterator().next();

		if (before.isType(TokenType.VARIABLE) && TokenType.referredTo(variables, before)) {
			before = variables.get(TokenType.getFirstVariableIndex(variables, before));
			before = ((HashMap<String, Token>) before.value).values().iterator().next();
		}
		if (after.isType(TokenType.VARIABLE) && TokenType.referredTo(variables, after)) {
			after = variables.get(TokenType.getFirstVariableIndex(variables, after));
			after = ((HashMap<String, Token>) after.value).values().iterator().next();
		}
		System.out.println("before: " + before + " after: " + after);
		if (before.isType(TokenType.FLOAT) || after.isType(TokenType.FLOAT))
			result.type = TokenType.FLOAT;
		else if (before.isType(TokenType.INTEGER) && after.isType(TokenType.INTEGER))
			result.type = TokenType.INTEGER;
		else
			throw new SyntaxError("Illegal Number Type", after, line_num);
		if (op.equals(TokenType.PLUS)) {
			if (result.isType(TokenType.INTEGER))
				result.value = (Integer) before.value + (Integer) after.value;
			else if (result.isType(TokenType.FLOAT))
				result.value = (before.isType(TokenType.FLOAT) ? (Double) before.value : (Integer) before.value)
						+ (after.isType(TokenType.FLOAT) ? (Double) after.value : (Integer) after.value);
			// System.out.println(result);
			list.add(beforeToken.keySet().iterator().next(), result);
			return result;
		} else if (op.equals(TokenType.MINUS)) {
			if (result.isType(TokenType.INTEGER))
				result.value = (Integer) before.value - (Integer) after.value;
			else if (result.isType(TokenType.FLOAT))
				result.value = (before.isType(TokenType.FLOAT) ? (Double) before.value : (Integer) before.value)
						- (after.isType(TokenType.FLOAT) ? (Double) after.value : (Integer) after.value);
			// System.out.println(result);
			list.add(beforeToken.keySet().iterator().next(), result);
			return result;
		} else if (op.equals(TokenType.MULTIPLY)) {
			if (result.isType(TokenType.INTEGER))
				result.value = (Integer) before.value * (Integer) after.value;
			else if (result.isType(TokenType.FLOAT))
				result.value = (before.isType(TokenType.FLOAT) ? (Double) before.value : (Integer) before.value)
						* (after.isType(TokenType.FLOAT) ? (Double) after.value : (Integer) after.value);
			// System.out.println(result);
			list.add(beforeToken.keySet().iterator().next(), result);
			return result;
		} else if (op.equals(TokenType.DIVIDE)) {
			if (result.isType(TokenType.INTEGER))
				result.value = (Integer) before.value / (Integer) after.value;
			else if (result.isType(TokenType.FLOAT))
				result.value = (before.isType(TokenType.FLOAT) ? (Double) before.value : (Integer) before.value)
						/ (after.isType(TokenType.FLOAT) ? (Double) after.value : (Integer) after.value);
			list.add(beforeToken.keySet().iterator().next(), result);
			return result;
		} else if (op.equals(TokenType.MODULO)) {
			if (result.isType(TokenType.INTEGER))
				result.value = (Integer) before.value % (Integer) after.value;
			else if (result.isType(TokenType.FLOAT))
				result.value = (before.isType(TokenType.FLOAT) ? (Double) before.value : (Integer) before.value)
						% (after.isType(TokenType.FLOAT) ? (Double) after.value : (Integer) after.value);
			list.add(beforeToken.keySet().iterator().next(), result);
			return result;
		}
		return null;
	}

	public static void orderOfStringOp(int indexOfStart) throws RuntimeError, SyntaxError {
		ArrayList<Token> operators = new ArrayList<Token>();
		for (int x = indexOfStart; x < token_list.size(); x++)
			if (TokenType.isOperator(token_list.get(x).type))
				operators.add(token_list.get(x));
		if (TokenType.containsTokenType(operators, TokenType.MINUS)
				|| TokenType.containsTokenType(operators, TokenType.DIVIDE))
			throw new RuntimeError("Cannot minus or divide Strings", token_list.get(indexOfStart), line_num);
		while (!operators.isEmpty()) {
			opStringTokens(TokenType.getFirstTokenTypeIndex(token_list, operators.get(0).type), operators.get(0).type);
			operators.remove(0);
		}
	}

	public static void orderOfStringOp(ArrayList<Token> list, int indexOfStart) throws RuntimeError, SyntaxError {
		ArrayList<Token> operators = new ArrayList<Token>();
		for (int x = indexOfStart; x < list.size(); x++)
			if (TokenType.isOperator(list.get(x).type))
				operators.add(list.get(x));
		if (TokenType.containsTokenType(operators, TokenType.MINUS)
				|| TokenType.containsTokenType(operators, TokenType.DIVIDE))
			throw new RuntimeError("Cannot minus or divide Strings", list.get(indexOfStart), line_num);
		while (!operators.isEmpty()) {
			opStringTokens(list, TokenType.getFirstTokenTypeIndex(list, operators.get(0).type), operators.get(0).type);
			operators.remove(0);
		}
	}

	public static Token opStringTokens(int indexOfOp, TokenType op) throws RuntimeError, SyntaxError {
		HashMap<Integer, Token> afterToken = getAfterToken(token_list, indexOfOp);
		// System.out.println("B4 T: " + beforeToken.toString());
		// System.out.println("AF T: " + afterToken.toString());
		int after_i = afterToken.keySet().iterator().next();
		Token result = new Token(TokenType.STRING);
		Token after = afterToken.values().iterator().next();
		// System.out.println(op);

		HashMap<Integer, Token> beforeToken = getBeforeToken(token_list, indexOfOp);
		int before_i = beforeToken.keySet().iterator().next();
		Token before = beforeToken.values().iterator().next();

		token_list.remove(before_i);
		token_list.remove(after_i - 1);
		token_list.remove(indexOfOp - 1);

		// System.out.println("Before:"+before);
		if (before.isType(TokenType.STRING) && after.isType(TokenType.STRING))
			;
		else if ((before.isType(TokenType.STRING)) && (after.isType(TokenType.INTEGER)))
			;
		else
			throw new SyntaxError("Illegal String Type: ", after, line_num);
		switch (op) {
		case PLUS:
			// System.out.println("before:"+before);
			// System.out.println("after:"+after);
			// System.out.println();
			if (before.isType(TokenType.STRING) && after.isType(TokenType.STRING))
				result = before.concat(after);
			token_list.add(beforeToken.keySet().iterator().next(), result);
			return result;
		case MULTIPLY:
			result = before.concat(before);
			for (int i = 1; i < (Integer) after.value; i++)
				result = result.concat(before);
			// System.out.println(result);

			token_list.add(beforeToken.keySet().iterator().next(), result);
			return result;
		default:
			return null;
		}
	}

	public static Token opStringTokens(ArrayList<Token> list, int indexOfOp, TokenType op)
			throws RuntimeError, SyntaxError {
		HashMap<Integer, Token> afterToken = getAfterToken(list, indexOfOp);
		// System.out.println("B4 T: " + beforeToken.toString());
		// System.out.println("AF T: " + afterToken.toString());
		int after_i = afterToken.keySet().iterator().next();
		Token result = new Token(TokenType.STRING);
		Token after = afterToken.values().iterator().next();
		// System.out.println(op);

		HashMap<Integer, Token> beforeToken = getBeforeToken(list, indexOfOp);
		int before_i = beforeToken.keySet().iterator().next();
		Token before = beforeToken.values().iterator().next();

		list.remove(before_i);
		list.remove(after_i - 1);
		list.remove(indexOfOp - 1);

		// System.out.println("Before:"+before);
		if (before.isType(TokenType.STRING) && after.isType(TokenType.STRING))
			;
		else if ((before.isType(TokenType.STRING)) && (after.isType(TokenType.INTEGER)))
			;
		else
			throw new SyntaxError("Illegal String Type: ", after, line_num);
		switch (op) {
		case PLUS:
			// System.out.println("before:"+before);
			// System.out.println("after:"+after);
			// System.out.println();
			if (before.isType(TokenType.STRING) && after.isType(TokenType.STRING))
				result = before.concat(after);
			list.add(beforeToken.keySet().iterator().next(), result);
			return result;
		case MULTIPLY:
			result = before.concat(before);
			for (int i = 1; i < (Integer) after.value; i++)
				result = result.concat(before);
			// System.out.println(result);
			list.add(beforeToken.keySet().iterator().next(), result);
			return result;
		default:
			return null;
		}
	}

	public static Token extractResult(ArrayList<Token> list) throws RuntimeError {
		int result_count = 0;
		Token result = null;
		for (int i = 0; i < list.size(); i++) {
			Token temp = list.get(i);
			if (temp.isType(TokenType.EOL) || temp.isType(TokenType.EOF))
				break;
			if (temp.isType(TokenType.BOOLEAN) || temp.isType(TokenType.INTEGER) || temp.isType(TokenType.STRING)
					|| temp.isType(TokenType.FLOAT) || temp.isType(TokenType.VARIABLE)) {
				result_count++;
				result = temp;
			}
		}
		if (result_count == 1)
			return result;
		else
			throw new RuntimeError("More than one result shown", line_num);
	}

	public static void reset() {
		cur_token = null;
		if (variables != null)
			variables.clear();
		if (token_list != null)
			token_list.clear();
		if (lines != null)
			lines.clear();
		line_num = 1;
		onlyAddingVar = false;
	}

}