package generator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import main.Report;

import exception.GeneratorException;
import fr.opensagres.xdocreport.converter.ConverterTypeTo;
import fr.opensagres.xdocreport.converter.ConverterTypeVia;
import fr.opensagres.xdocreport.converter.Options;
import fr.opensagres.xdocreport.converter.XDocConverterException;
import fr.opensagres.xdocreport.core.XDocReportException;

public class HTMLGenerator implements IGenerator {
	
	private Report report;
	
	public HTMLGenerator(Report report)
	{
		this.report=report;
	}

	@Override
	public void generate() throws GeneratorException {
		File outputFile = new File(report.getOutput()+".html");
		OutputStream out = null;
		try {
			out = new FileOutputStream(outputFile);
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
		Options options = Options.getTo(ConverterTypeTo.XHTML).via(ConverterTypeVia.XWPF);
		try {
			report.convert(options, out);
		} catch (XDocConverterException e) {
			throw new GeneratorException(GeneratorError.HTML_CONVERTION_ERROR,"Error while converting the HTML:"+e.getMessage());
		} catch (XDocReportException e) {
			throw new GeneratorException(GeneratorError.HTML_GENERATION_ERROR,"Error while generating the HTML:"+e.getMessage());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
