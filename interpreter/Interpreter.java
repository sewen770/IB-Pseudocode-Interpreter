/*
 * TODO

 * Basic:
 * - Parser's methods: checking, compilation errors, (exceptions?) 
 * - Resolutions of methods of other modules
 * - Selections
 * - Loops
 * 
 * Advanced:
 * - Debugger with trace table
 * 
 */

package interpreter;

import java.awt.Color;
import java.awt.Toolkit;
import java.util.ArrayList;

import javax.swing.UnsupportedLookAndFeelException;

import errors.Exception;
import errors.SyntaxError;

/**
 * The main class for the project to
 * 
 * @created: 17/01/2018 11:16 am
 * 
 * @author: Sewen Thy
 * 
 */
public class Interpreter {
	// ArrayList of lines:
	public static ArrayList<String> lines_list = new ArrayList<String>();
	public static ArrayList<ArrayList<Token>> token_lines = new ArrayList<ArrayList<Token>>();

	public static String input;

	public static String codeblock = "inactive";
	public static ArrayList<Token> lineToContinueFrom = null;
	public static Integer line_saved = null;
	public static String loop_text = null;

	public static int line_number = 1;
	static Window WINDOW;

	public static boolean debug = false;

	public static void main(String[] args) throws ClassNotFoundException, InstantiationException,
			IllegalAccessException, UnsupportedLookAndFeelException {
		WINDOW = new Window();
		java.awt.Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		WINDOW.setLocation(screenSize.width / 5, screenSize.height / 7);
		WINDOW.setVisible(true);
	}

	public static void run(String input) {
		try {
			reset();
			WINDOW.output("");
			setInput(input);
			lineMaker();
			for (int i = 0; i < lines_list.size(); i++) {
				line_number = i+1;
				String n = lines_list.get(i);
				LexicalAnalyzer.setInput(n);
				LexicalAnalyzer.setLineNumber(line_number);
				LexicalAnalyzer.lexicalSplit();
				LexicalAnalyzer.getNextLine();
				LexicalAnalyzer.secondLevelTokenizer();
				System.out.println("current line list: " + LexicalAnalyzer.token_list);
				if ((codeblock.equals("activeIF") || codeblock.equals("activeWHILE") || codeblock.equals("activeFROM"))
						&& lineToContinueFrom == null) {
					Expressionizer.line_num = line_number;
					lineToContinueFrom = LexicalAnalyzer.token_list;
					if (codeblock.equals("activeWHILE") || codeblock.equals("activeFROM")) {
						loop_text = n;
					}
					System.out.println("loop text: " + loop_text);
				}
				if (codeblock.equals("inactive")) {
					Expressionizer.token_list = LexicalAnalyzer.token_list;
					if (lineToContinueFrom != null) {
						Expressionizer.token_list = lineToContinueFrom;
						lineToContinueFrom = null;
					}
					Expressionizer.line_num = line_number;
					Expressionizer.evaluateTokens();
					LexicalAnalyzer.stripWhitespace(Expressionizer.token_list);
					LexicalAnalyzer.codeblockTokenizer();
				}
				System.out.println("DEBUG:" + debug);
				token_lines.add(LexicalAnalyzer.token_list);
				LexicalAnalyzer.nextLine();
				if (codeblock.equals("inactive")) {
					LexicalAnalyzer.resetCodeblocks();
					loop_text = null;
				}
			}
			if (codeblock.equals("activeIF") || codeblock.equals("activeWHILE") || codeblock.equals("activeFROM"))
				throw new SyntaxError("No end to codeblock", line_number);
			reset();
			debug = false;
		} catch (java.lang.Exception e) {
			e.printStackTrace();
			WINDOW.output(e.toString(), Color.RED);
			reset();
			debug = false;
		}
	}

	public static void runLine(String input) {
		try {
			reset();
			WINDOW.output("");
			setInput(input);
			lineMaker();
			Expressionizer.debug=true;
			Expressionizer.inCB_line=1;
			String n = lines_list.get(0);
			LexicalAnalyzer.setInput(n);
			LexicalAnalyzer.setLineNumber(line_number);
			LexicalAnalyzer.lexicalSplit();
			LexicalAnalyzer.getNextLine();
			LexicalAnalyzer.secondLevelTokenizer();
			System.out.println("current line list: " + LexicalAnalyzer.token_list);
			if ((codeblock.equals("activeIF") || codeblock.equals("activeWHILE") || codeblock.equals("activeFROM"))
					&& lineToContinueFrom == null) {
				lineToContinueFrom = LexicalAnalyzer.token_list;
				if (codeblock.equals("activeWHILE") || codeblock.equals("activeFROM")) {
					loop_text = n;
				}
			}
			if (codeblock.equals("inactive")) {
				Expressionizer.token_list = LexicalAnalyzer.token_list;
				if (lineToContinueFrom != null) {
					Expressionizer.token_list = lineToContinueFrom;
					lineToContinueFrom = null;
				}
				Expressionizer.line_num = line_number;
				Expressionizer.evaluateTokens();
				LexicalAnalyzer.stripWhitespace(Expressionizer.token_list);
				LexicalAnalyzer.codeblockTokenizer();
			}
			System.out.println("DEBUG:" + debug);
			token_lines.add(LexicalAnalyzer.token_list);
			LexicalAnalyzer.nextLine();
			if (codeblock.equals("inactive")) {
				LexicalAnalyzer.resetCodeblocks();
				loop_text = null;
			}

		} catch (java.lang.Exception e) {
			e.printStackTrace();
			WINDOW.output(e.toString(), Color.RED);
			reset();
			debug = false;
		}
	}

	public static void runLine(String input, int line_num) {
		if(line_num>=lines_list.size())
			throw new IllegalArgumentException();
		try {

			String n = lines_list.get(line_num);
			LexicalAnalyzer.setInput(n);
			LexicalAnalyzer.setLineNumber(line_number);
			LexicalAnalyzer.lexicalSplit();
			LexicalAnalyzer.getNextLine();
			LexicalAnalyzer.secondLevelTokenizer();
			System.out.println("current line list: " + LexicalAnalyzer.token_list);
			if ((codeblock.equals("activeIF") || codeblock.equals("activeWHILE") || codeblock.equals("activeFROM"))
					&& lineToContinueFrom == null) {
				lineToContinueFrom = LexicalAnalyzer.token_list;
				if (codeblock.equals("activeWHILE") || codeblock.equals("activeFROM")) {
					loop_text = n;
				}
				System.out.println("loop text: " + loop_text);
			}
			if (codeblock.equals("inactive")) {
				Expressionizer.token_list = LexicalAnalyzer.token_list;
				if (lineToContinueFrom != null) {
					Expressionizer.token_list = lineToContinueFrom;
					lineToContinueFrom = null;
				}
				Expressionizer.line_num = line_number;
				Expressionizer.evaluateTokens();
				LexicalAnalyzer.stripWhitespace(Expressionizer.token_list);
				LexicalAnalyzer.codeblockTokenizer();
			}
			System.out.println("DEBUG:" + debug);
			token_lines.add(LexicalAnalyzer.token_list);
			LexicalAnalyzer.nextLine();
			if (codeblock.equals("inactive")) {
				LexicalAnalyzer.resetCodeblocks();
				loop_text = null;
			}

		} catch (java.lang.Exception e) {
			e.printStackTrace();
			WINDOW.output(e.toString(), Color.RED);
			reset();
			debug = false;
		}
	}

	
	public static ArrayList<Token> getVariableList(String input) throws Exception {
		reset();
		Expressionizer.onlyAddingVar = true;
		setInput(input);
		lineMaker();
		ArrayList<Token> variables = null;
		for (int i = 0; i < lines_list.size(); i++) {
			line_number = i;
			String n = lines_list.get(i);
			LexicalAnalyzer.setInput(n);
			LexicalAnalyzer.setLineNumber(line_number);
			LexicalAnalyzer.lexicalSplit();
			LexicalAnalyzer.getNextLine();
			LexicalAnalyzer.secondLevelTokenizer();
			Expressionizer.line_num = line_number;
			Expressionizer.token_list = LexicalAnalyzer.token_list;
			Expressionizer.evaluateTokens();
			LexicalAnalyzer.stripWhitespace(Expressionizer.token_list);
			LexicalAnalyzer.codeblockTokenizer();
			token_lines.add(LexicalAnalyzer.token_list);
			System.out.println("variables list: " + Expressionizer.variables);
			variables = Expressionizer.variables;
			LexicalAnalyzer.nextLine();
		}
		return variables;
	}

	/**
	 * This method manages the inputs into an {@link ArrayList<String>} with
	 * attachment of EOL and EOF for better integration with the
	 * {@link LexicalAnalyzer}.
	 */
	public static void lineMaker() {
		// DONE: ATTACHING EOL AND EOF STRINGS
		String[] n = input.split("\n");
		for (int i = 0; i < n.length; i++) {
			String line = n[i];
			if (i != n.length - 1)
				line += " EOL";
			else
				line += " EOF";
			lines_list.add(line);
		}
	}

	public static void interruptSleep() {
		Thread.currentThread().interrupt();
	}

	public static void setInput(String input) {
		Interpreter.input = input;
	}

	public static void reset() {
		line_number = 1;
		setInput("");
		lines_list.clear();
		LexicalAnalyzer.reset();
		Expressionizer.reset();
		codeblock = "inactive";
	}
}
