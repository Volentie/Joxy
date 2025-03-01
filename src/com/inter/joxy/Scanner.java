package com.inter.joxy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.inter.joxy.TokenType.*;

public class Scanner {
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
		switch (c) {
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
					while (peek() != '\n' && !isAtEnd()) advance();
				} else {
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

			default:
				Joxy.error(line, "Unexpected character.");
				break;
		}
	}
	
	private boolean match(char expected) {
		if (isAtEnd()) return false;
		if (source.charAt(current) != expected) return false;
		
		current++;
		return true;
	}	
	
	// Don't consume the character.
	private char peek() {
		if (isAtEnd()) return '\0';
		return source.charAt(current);
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
