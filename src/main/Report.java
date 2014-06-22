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
package main;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import exception.GeneratorException;
import fr.opensagres.xdocreport.converter.Options;
import fr.opensagres.xdocreport.converter.XDocConverterException;
import fr.opensagres.xdocreport.core.XDocReportException;
import fr.opensagres.xdocreport.document.IXDocReport;
import fr.opensagres.xdocreport.document.images.FileImageProvider;
import fr.opensagres.xdocreport.document.images.IImageProvider;
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

		// Adds the data from the JSON to the report context:
		JSONObject textObject = (JSONObject)json.get("text");
		if(textObject != null) {
			computeText(textObject);
		}
		
		JSONObject imagesObject = (JSONObject)json.get("images");
		if(imagesObject != null) {
			computeImages(imagesObject);
		}
		
		JSONObject listObject = (JSONObject)json.get("list");
		if(listObject != null) {
			computeList(listObject);
		}

		// Links the FieldsMetaData to the report.
		report.setFieldsMetadata(metadata);
	}
	
	/**
	 * Adds the text data from the JSON to the report's context.
	 * @param textObject The JSON text object.
	 */
	@SuppressWarnings("rawtypes")
	private void computeText(JSONObject textObject) {
		// iterate on all the simple text
		Iterator<?> iter = textObject.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry entry = (Map.Entry)iter.next();
			// add the text to the report's context
			context.put(entry.getKey().toString(), entry.getValue().toString());
		}
	}

	/**
	 * Initializes the FieldsMetaData for all images,
	 * creates them with the paths from the JSON and adds it to the report's context.
	 * @param imagesObject The JSON images object.
	 */
	@SuppressWarnings("rawtypes")
	private void computeImages(JSONObject imagesObject) {
		// iterate on all images
		Iterator<?> iter = imagesObject.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry entry = (Map.Entry)iter.next();
			// set the FieldsMetaData
			metadata.addFieldAsImage(entry.getKey().toString());
			// create the image
			IImageProvider img = new FileImageProvider(new File(entry.getValue().toString()));
			img.setUseImageSize(true);
			// add it to the report's context
			context.put(entry.getKey().toString(), img);
		}
	}

	/**
	 * Initializes the FieldsMetaData with the key of the JSON data
	 * and adds the list to the report's context.
	 * @param listObject The JSON list object.
	 */
	@SuppressWarnings("rawtypes")
	private void computeList(JSONObject listObject) {
		// Iterates on all the list:
		Iterator<?> iter = listObject.entrySet().iterator();
		while (iter.hasNext()) {
			// Gets the key and the array:
			Map.Entry entry = (Map.Entry)iter.next();
			String key = entry.getKey().toString();
			JSONArray array = (JSONArray) entry.getValue();
	
			// Gets the FieldsMetaData, searching for all different keys on the Map:
			HashSet<String> metaData = new HashSet<String>();
			Iterator<?> iterArray = array.iterator();
			while (iterArray.hasNext()) {
				JSONObject map = (JSONObject) iterArray.next();
				Iterator<?> iterMap = map.entrySet().iterator();
				while (iterMap.hasNext()) {
					Map.Entry entryMap = (Map.Entry)iterMap.next();	
					metaData.add(entryMap.getKey().toString());
				}
			}
			
			// Sets the FieldsMetaData:
			Iterator<?> iterMetaData = metaData.iterator();
			while (iterMetaData.hasNext()) {
				metadata.addFieldAsList(key+"."+iterMetaData.next().toString());	
			}
	
			// Adds the array to the report's context.
			// array can be directly passed as a value for contextMap because it inherits from ArrayList.
			context.put(key, array);
		} 
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
