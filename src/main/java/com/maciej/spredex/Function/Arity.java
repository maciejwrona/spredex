package com.maciej.spredex.Function;

public sealed interface Arity {
	boolean matches(int numberOfArguments);

	record Fixed(int expectedNumberOfArguments) implements Arity {
		@Override
		public boolean matches(int numberOfArguments) {
			return expectedNumberOfArguments == numberOfArguments;
		}

		@Override
		public String toString() {
			return Integer.toString(expectedNumberOfArguments);
		}
	}

	record Minimum(int minimumArguments) implements Arity {
		@Override
		public boolean matches(int numberOfArguments) {
			return numberOfArguments >= minimumArguments;
		}

		@Override
		public String toString() {
			return "at least " + Integer.toString(minimumArguments);
		}
	}

	record Any() implements Arity {
		@Override
		public boolean matches(int numberOfArguments) {
			return true;
		}

		@Override
		public String toString() {
			return "any number";
		}
	}
}

