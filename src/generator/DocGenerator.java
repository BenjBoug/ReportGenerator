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

import report.Report;
import exception.GeneratorException;
import fr.opensagres.xdocreport.core.XDocReportException;

/**
 * @author Benjamin Bouguet
 *
 */
public class DocGenerator implements IGenerator {
	
	private Report report;
	
	/**
	 * @param report
	 */
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

		try {
			report.generate(report.getOutput()+"."+ext);
		} catch (XDocReportException e) {
			throw new GeneratorException(GeneratorError.DOCX_GENERATION_ERROR,"Error while generating the DOCX file:"+e.getMessage());
		}
	}

}
