package generator;

import exception.GeneratorException;
import main.Report;

public class AllGenerator implements IGenerator {
	
	private Report report;
	
	public AllGenerator(Report report)
	{
		this.report=report;
	}

	@Override
	public void generate() throws GeneratorException {
		new HTMLGenerator(report).generate();
		new PDFGenerator(report).generate();
		new DocGenerator(report).generate();
	}

}
