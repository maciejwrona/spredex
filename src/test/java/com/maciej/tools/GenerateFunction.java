package com.maciej.tools;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

public class GenerateFunction {
	private final static String outputDir = "src/main/java/com/maciej/spredex/Function/Models/";

	public static void main(String args[]) throws IOException {
		if (args.length != 1) {
			System.out.println("Usage: GenerateFunction <function_name>");
			return;
		}

		generateFunction(args[0]);
	}

	private static void generateFunction(String functionName) throws IOException {
		String path = outputDir + functionName + ".java";
		File functionFile = new File(path);

		if (functionFile.exists()) {
			throw new IOException(path + " already exists.");
		}

		PrintWriter writer = new PrintWriter(path, "UTF-8");
		writer.println("package com.maciej.spredex.Function.Models;");
		writer.println();

		defineImports(writer);
		writer.println();

		writer.println("public class " + functionName + " extends SpredexFunction {");

		defineConstructor(writer, functionName);
		writer.println();

		defineCallFunction(writer);

		writer.println("}");
		writer.close();
	}

	private static void defineImports(PrintWriter writer) {
		writer.println("import java.util.List;");
		writer.println();
		writer.println("import com.maciej.spredex.CellLoc;");
		writer.println("import com.maciej.spredex.Function.Arity;");
		writer.println("import com.maciej.spredex.Function.SpredexFunction;");
		writer.println("import com.maciej.spredex.Sheet.Sheet;");
		writer.println("import com.maciej.spredex.Interpreter.Interpreter;");
	}

	private static void defineConstructor(PrintWriter writer, String functionName) {
		writer.println("	public " + functionName + "() {");
		writer.println("		super(\"" + functionName.toUpperCase() + "\", new Arity());");
		writer.println("	}");
	}

	private static void defineCallFunction(PrintWriter writer) {
		writer.println("	@Override");
		writer.println("	public Object call(List<Object> arguments, CellLoc location, Sheet sheet) {");
		writer.println();
		writer.println("	}");
	}
}
