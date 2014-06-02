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

import java.io.File;
import java.io.IOException;

import main.Report;

import org.apache.poi.xwpf.converter.core.FileImageExtractor;
import org.apache.poi.xwpf.converter.core.FileURIResolver;
import org.apache.poi.xwpf.converter.xhtml.XHTMLOptions;

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
public class HTMLGenerator implements IGenerator {
	
	private Report report;
	
	/**
	 * @param report
	 */
	public HTMLGenerator(Report report)
	{
		this.report=report;
	}

	@Override
	public void generate() throws GeneratorException {
		XHTMLOptions optionsHTML = XHTMLOptions.create();
        // Extract image
        File imageFolder = new File(report.getOutput() + "/images/");
        optionsHTML.setExtractor(new FileImageExtractor(imageFolder));

        optionsHTML.URIResolver(new FileURIResolver(imageFolder));
        
		Options options = Options.getTo(ConverterTypeTo.XHTML).via(ConverterTypeVia.XWPF).subOptions(optionsHTML);
		try {
			report.generate(options, report.getOutput()+".html");
		} catch (XDocConverterException e) {
			throw new GeneratorException(GeneratorError.HTML_CONVERTION_ERROR,"Error while converting the HTML:"+e.getMessage());
		} catch (XDocReportException e) {
			throw new GeneratorException(GeneratorError.HTML_GENERATION_ERROR,"Error while generating the HTML:"+e.getMessage());
		}
	}

}
