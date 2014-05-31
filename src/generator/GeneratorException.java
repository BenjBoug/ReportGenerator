package generator;

public class GeneratorException extends Exception {

	private GeneratorError error;
	
	public GeneratorException(GeneratorError error, String msg) {
		super(msg);
		this.error=error;
	}

	public GeneratorError getError() {
		return error;
	}
}
