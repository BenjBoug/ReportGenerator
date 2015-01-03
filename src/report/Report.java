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
package report;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Map;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import report.builder.BuilderFactory;

import exception.GeneratorException;
import fr.opensagres.xdocreport.converter.Options;
import fr.opensagres.xdocreport.converter.XDocConverterException;
import fr.opensagres.xdocreport.core.XDocReportException;
import fr.opensagres.xdocreport.document.IXDocReport;
import fr.opensagres.xdocreport.document.registry.XDocReportRegistry;
import fr.opensagres.xdocreport.template.IContext;
import fr.opensagres.xdocreport.template.TemplateEngineKind;
import fr.opensagres.xdocreport.template.formatter.FieldsMetadata;
import generator.GeneratorError;

/**
 * @author Benjamin Bouguet
 *
 */
public class Report {

	private IContext context;
	private FieldsMetadata metadata;
	private IXDocReport report;
	private File template = null;
	private String output = null;
	
	/**
	 * Construct a report
	 * @param output the path to the new report without extension
	 * @param template the path to the template
	 * @throws GeneratorException raise if the template is not a file
	 * @throws IOException 
	 */
	public Report(String jsonText, String template, String output) throws GeneratorException
	{
		this.output=output;
		this.template = new File(template);
		
		if (!this.template.isFile())
			throw new GeneratorException(GeneratorError.PARAMETER_ERROR, "Template file does not exists.");
		
		buildFromJson(jsonText);
	}


	/**
	 * Initialize the IXDocReport struct with the JSON
	 * @param jsonText The JSON text.
	 * @throws IOException If the template or output file can't be opened or written.
	 * @throws GeneratorException 
	 */
	private void buildFromJson(String jsonText) throws GeneratorException
	{
		// Parses the JSON text:
		JSONObject json;
		try {
			JSONParser parser = new JSONParser();
			json = (JSONObject)parser.parse(jsonText);
		} catch(ParseException e) {
			throw new GeneratorException(GeneratorError.JSON_ERROR,"Error while parsing the JSON file:"+e.toString());
		}

		// Initializes the template file and creates the report object:
		try {
			InputStream in = new FileInputStream(template);
			report = XDocReportRegistry.getRegistry().loadReport(in, TemplateEngineKind.Velocity);
		} catch (XDocReportException e) {
			throw new GeneratorException(GeneratorError.TEMPLATE_ERROR,"Error with the template file:"+e.toString());
		} catch (FileNotFoundException e) {
			throw new GeneratorException(GeneratorError.PARAMETER_ERROR, "Template file does not exists.");
		} catch (IOException e) {
			throw new GeneratorException(GeneratorError.IO_ERROR, "I/O error...");
		}

		// Creates the FieldsMetadata.
		metadata = new FieldsMetadata();

		// Creates the report context:
		try {
			context = report.createContext();
		} catch (XDocReportException e) {
			throw new GeneratorException(GeneratorError.CONTEXT_ERROR,"Error while creating the context:"+e.toString());
		}

		Iterator<?> iter = json.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry<?,?> entry = (Map.Entry<?,?>)iter.next();
			BuilderFactory.getBuilder(entry.toString()).build((JSONObject)entry.getValue(), metadata, context);
		}


		// Links the FieldsMetaData to the report.
		report.setFieldsMetadata(metadata);
	}
	
	/**
	 * Generate the document
	 * @param out
	 * @throws XDocReportException
	 * @throws IOException
	 */
	public void generate(String out) throws XDocReportException
	{
		try {
			report.process(context, new FileOutputStream(out));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}
	/**
	 * Generate the document with options
	 * @param opt the option for this document
	 * @param out the path of the new report WITH the extension
	 * @throws XDocConverterException
	 * @throws XDocReportException
	 */
	public void generate(Options opt, String out) throws XDocConverterException, XDocReportException
	{
		try {
			report.convert(context, opt, new FileOutputStream(out));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}
	
	/**
	 * Get the path of the new report without extension
	 * @return a string with the path of the ouput file without extension
	 */
	public final String getOutput()
	{
		return output;
	}

	/**
	 * Get the template path
	 * @return a string with the path of the teplate file
	 */
	public final String getTemplate()
	{
		return template.getAbsolutePath();
	}	
	
}
