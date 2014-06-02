package exception;

import generator.GeneratorError;

public class GeneratorException extends Exception {

	private static final long serialVersionUID = 1L;
	
	private GeneratorError error;
	
	public GeneratorException(GeneratorError error, String msg) {
		super(msg);
		this.error=error;
	}

	public GeneratorError getError() {
		return error;
	}
}
