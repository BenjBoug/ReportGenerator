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

import exception.GeneratorException;
import main.Report;

/**
 * Generate the document with al format available
 * @author Benjamin Bouguet
 */
public class AllGenerator implements IGenerator {
	
	private Report report;
	
	/**
	 * Constructor
	 * @param report The report to generate
	 */
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
