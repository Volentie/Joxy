package com.inter.joxy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.inter.joxy.TokenType.*;

public class Scanner {
	private static final Map<String, TokenType> keywords;

	static {
		keywords = new HashMap<>();
		keywords.put("and", 	AND);
		keywords.put("class", 	CLASS);
		keywords.put("else", 	ELSE);
		keywords.put("false", 	FALSE);
		keywords.put("fun", 	FUN);
		keywords.put("for", 	FOR);
		keywords.put("if",		IF);
		keywords.put("nil", 	NIL);
		keywords.put("or", 		OR);
		keywords.put("print", 	PRINT);
		keywords.put("return", 	RETURN);
		keywords.put("super", 	SUPER);
		keywords.put("this", 	THIS);
		keywords.put("true", 	TRUE);
		keywords.put("var", 	VAR);
		keywords.put("while", 	WHILE);	
	}

	private final String source;
	private final List<Token> tokens = new ArrayList<>();
	private int start = 0;
	private int current = 0;
	private int line = 1;
	
	Scanner(String source) {
		this.source = source;
	}

	List<Token> scanTokens() {
		while (!isAtEnd()) {
			// We are at the beginning of the next lexeme.
			start = current;
			scanToken();
		}
		
		tokens.add(new Token(EOF, "", null, line));
		return tokens;
	}

	private void scanToken() {
		char c = advance();
		switch (c) { // c is the current character being processed, it is already consumed at this point.
			case '(': addToken(LEFT_PAREN); break;
			case ')': addToken(RIGHT_PAREN); break;
			case '{': addToken(LEFT_BRACE); break;
			case '}': addToken(RIGHT_BRACE); break;
			case ',': addToken(COMMA); break;
			case '.': addToken(DOT); break;
			case '-': addToken(MINUS); break;
			case '+': addToken(PLUS); break;
			case ';': addToken(SEMICOLON); break;
			case '*': addToken(STAR); break;
			case '!':
				addToken(match('=') ? BANG_EQUAL : BANG);
				break;
			case '=':
				addToken(match('=') ? EQUAL_EQUAL : EQUAL);
				break;
			case '<':
				addToken(match('=') ? LESS_EQUAL : LESS);
				break;
			case '>':
				addToken(match('=') ? GREATER_EQUAL : GREATER);
				break;
			case '/':
				if (match('/')) {
					// Peeks until the end of the line.
					// Using peek here instead of match because we don't want to consume the new line character, so we can increment the line count.
					while (peek() != '\n' && !isAtEnd()) advance();
				} else if (match('*')) {
					while(true) {
						if (isAtEnd()) {
							Joxy.error(line, "Unterminated block comment."); // Reports unfinished block comment
							return; // Exit
						}
						
						char pk = peek(); // Only call peek once (micro-optimization)

						if (pk == '*') {
							if (peekNext() == '/') {
								advance(); // Consumes *
								advance(); // Consumes /
								break;
							}
						}

						if (pk == '\n') line++;

						advance(); // Consumes the next input.
					}
				}
				else {
					addToken(SLASH);
				}
				break;

			case ' ':
			case '\r':
			case '\t':
				// Ignore whitespace.
				break;
			
			case '\n':
				line++;
				break;
			
			case '"': string(); break;

			default:
				if (isDigit(c)) {
					number();
				} else if (isAlpha(c)) {
					identifier();
				} else {
					Joxy.error(line, "Unexpected character.");
				}
				break;
		}
	}
	
	private void identifier() {
		while (isAlphaNumeric(peek())) advance();
		
		String text = source.substring(start, current);
		// Check if the identifier is a reserved word.
		TokenType type = keywords.get(text);
		// If it's not a reserved word, then it's an identifier.
		if (type == null) type = IDENTIFIER;
		addToken(type);
	}
	
	private boolean isAlpha(char c) {
		return (c >= 'a' && c <= 'z') ||
				(c >= 'A' && c <= 'Z') ||
				c == '_';
	}
	
	private boolean isAlphaNumeric(char c) {
		return isAlpha(c) || isDigit(c);
	}
	
	private void number() {
		while (isDigit(peek())) advance();
		
		// Look for a fractional part
		if (peek() == '.' && isDigit(peekNext())) {
			// Consume the "."
			advance();
			
			while (isDigit(peek())) advance();
		}

		addToken(NUMBER,
		Double.parseDouble(source.substring(start, current)));
	}
	
	private char peekNext() {
		// It's important to notice that I'm not consuming the current char, it just looks two characters ahead instead and returns the input;
		if (current + 1 >= source.length()) return '\0';
		return source.charAt(current + 1);
	}
	
	private void string() {
		while (!match('"')) {
			if (peek() == '\n') line++; // Supports multi-line strings.
			advance();
		};
		
		if (isAtEnd()) {
			Joxy.error(line, "Unterminated string.");
		}

		// Trim the surrounding quotes.
		addToken(STRING, source.substring(start + 1, current - 1));
	}
	
	// Input, consumes the next unprocessed character in the source file if it's equal to the expected character.
	private boolean match(char expected) {
		if (isAtEnd()) return false;
		if (source.charAt(current) != expected) return false;
		
		current++;
		return true;
	}
	
	// Similar to advance but it doesn't consume the character (lookahead).
	private char peek() {
		if (isAtEnd()) return '\0';
		return source.charAt(current);
	}
	
	private boolean isDigit(char c) {
		return c >= '0' && c <= '9';
	}
	
	private boolean isAtEnd() {
		return current >= source.length();
	}
	
	// Input, consumes the next (unprocessed) character in the source file.
	private char advance() {
		return source.charAt(current++);
	}
	
	// Overload method
	private void addToken(TokenType type) {
		addToken(type, null);
	}

	// Output, grabs the text of the current lexeme and creates a new token for it.
	private void addToken(TokenType type, Object literal) {
		String text = source.substring(start, current);
		tokens.add(new Token(type, text, literal, line));
	}

}
