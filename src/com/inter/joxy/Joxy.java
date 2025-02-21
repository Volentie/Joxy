package com.inter.joxy;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Joxy {
	static boolean hadError = false;

	public static void main(String[] args) throws IOException {
		// Limit the argument list to 1.
		if (args.length > 1) {
			System.out.println("Usage: joxy [script]");
			System.exit(64); // Command line usage error.
		} else if (args.length == 1) {
			runFile(args[0]); // Run from source.
		} else {
			runPrompt(); // Run interactively.
		}
	}
	
	private static void runFile(String path) throws IOException {
		// Read all the bytes from the file, then run it.
		byte[] bytes = Files.readAllBytes(Paths.get(path));
		run(new String(bytes, Charset.defaultCharset()));
		
		// Indicate an error in the exit code.
		if (hadError) System.exit(65); // Data format error.
	}
	
	private static void runPrompt() throws IOException {
		// Creates an input stream reader so we can read input from the prompt.
		InputStreamReader input = new InputStreamReader(System.in);
		BufferedReader reader = new BufferedReader(input);
		
		for (;;) {
			System.out.print("> ");
			String line = reader.readLine();
			if (line == null) break;
			run(line);
			// Reset the flag so it doesn't kill user's entire session
			hadError = false;
		}
	}

	private static void run(String source) {
		Scanner scanner = new Scanner(source);
		List<Token> tokens = scanner.scanTokens();
		
		for (Token token : tokens) {
			System.out.println(token);
		}
	}
	
	static void error(int line, String message) {
		report(line, "", message);
	}
	
	private static void report(int line, String where, String message) {
		System.err.println("[line " + line + "] Error" + where + ": " + message);
		hadError = true;
	}
}