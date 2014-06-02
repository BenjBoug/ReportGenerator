package generator;

import fr.opensagres.xdocreport.core.XDocReportException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import exception.GeneratorException;
import main.Report;

public class DocGenerator implements IGenerator {
	
	private Report report;
	
	public DocGenerator(Report report)
	{
		this.report=report;
	}

	@Override
	public void generate() throws GeneratorException {
		//get the extension of the template
		String ext="";
		String templateFile = report.getTemplate();
		int i = templateFile.lastIndexOf('.');
		if (i >= 0) {
		    ext = templateFile.substring(i+1);
		}
		
		File outputFile = new File(report.getOutput()+"."+templateFile);
		OutputStream out=null;
		try {
			out = new FileOutputStream(outputFile);
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			report.process(out);
		} catch (XDocReportException e) {
			throw new GeneratorException(GeneratorError.DOCX_GENERATION_ERROR,"Error while generating the DOCX file:"+e.getMessage());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
