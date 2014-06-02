/**
 * Copyright (C) 2013 Benjamin Bouguet, Paul Chaignon
 *
 * ReportGenerator is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * ReportGenerator is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package generator;

import main.Report;
import exception.GeneratorException;
import fr.opensagres.xdocreport.converter.ConverterTypeTo;
import fr.opensagres.xdocreport.converter.ConverterTypeVia;
import fr.opensagres.xdocreport.converter.Options;
import fr.opensagres.xdocreport.converter.XDocConverterException;
import fr.opensagres.xdocreport.core.XDocReportException;

/**
 * @author Benjamin Bouguet
 *
 */
public class PDFGenerator implements IGenerator {

	private Report report;
	
	/**
	 * Constructor
	 * @param report
	 */
	public PDFGenerator(Report report)
	{
		this.report=report;
	}
	
	@Override
	public void generate() throws GeneratorException {
		Options options = Options.getTo(ConverterTypeTo.PDF).via(ConverterTypeVia.XWPF);
		try {
			report.generate(options, report.getOutput()+".pdf");
		} catch (XDocConverterException e) {
			throw new GeneratorException(GeneratorError.PDF_CONVERTION_ERROR,"Error while converting the PDF:"+e.getMessage());
		} catch (XDocReportException e) {
			throw new GeneratorException(GeneratorError.PDF_GENERATION_ERROR,"Error while generating the PDF:"+e.getMessage());
		}
	}

}
