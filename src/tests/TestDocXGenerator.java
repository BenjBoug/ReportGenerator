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
package tests;

import exception.GeneratorException;
import generator.AllGenerator;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import junit.framework.TestCase;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import report.Report;

/**
 *
 */
public class TestDocXGenerator extends TestCase {
	
	/**
	 * Test all generation of example report
	 * @throws IOException
	 * @throws GeneratorException 
	 */
	public static void testAllGeneration() throws IOException, GeneratorException {
		FileInputStream jsonInput = new FileInputStream("examples/json/test.json");
		String jsonText = IOUtils.toString(jsonInput);
		String outputPath = "examples/tests/";
		File dir = new File(outputPath);
		if (!dir.exists()) {
		    dir.mkdir(); 
		}
		outputPath+="test";
		Report report = new Report(jsonText, "examples/template/test.docx", outputPath);
		
		new AllGenerator(report).generate();

		File docxFile = new File(outputPath+".docx");
		File pdfFile = new File(outputPath+".pdf");
		File htmlFile = new File(outputPath+".html");
		
		assertTrue(docxFile.isFile());
		assertTrue(pdfFile.isFile());
		assertTrue(htmlFile.isFile());
		
		assertTrue(docxFile.delete());
		assertTrue(pdfFile.delete());
		assertTrue(htmlFile.delete());
		
		FileUtils.deleteDirectory(dir);
	}
	
	/**
	 * The JSON document is badly formatted.
	 * @throws IOException
	 */
	public static void testErroredJSON() throws IOException {
		/*FileInputStream jsonInput = new FileInputStream("resources/errored.json");
		String jsonText = IOUtils.toString(jsonInput);
		File template = new File("resources/test.docx");
		String outputPath = "resources/errored_json_result";
		assertEquals(GeneratorError.JSON_ERROR, ReportGenerator.generate(jsonText, template, outputPath));*/
	}
	
	/**
	 * Information is missing in the JSON document.
	 * @throws IOException
	 */
	public static void testMissingInformation() throws IOException {
		/*FileInputStream jsonInput = new FileInputStream("resources/missing-information.json");
		String jsonText = IOUtils.toString(jsonInput);
		File template = new File("resources/test.docx");
		String outputPath = "resources/missing_information_result";
		assertEquals(GeneratorError.IMAGES_MISSING, ReportGenerator.generate(jsonText, template, outputPath));*/
	}
	
	/**
	 * The kurtosis is missing in the JSON document.
	 * @throws IOException
	 */
	public static void testMissingValue() {
		// TODO Doesn't work at the moment as the generator can't detect if a value is missing.
		/*FileInputStream jsonInput = new FileInputStream("resources/missing-value.json");
		String jsonText = IOUtils.toString(jsonInput);
		File template = new File("resources/test.docx");
		String outputPath = "resources/missing_value_result";
		assertEquals(GeneratorError.IMAGES_MISSING, DocXGenerator.generate(jsonText, template, outputPath));*/
	}
}
