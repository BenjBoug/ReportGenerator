/**
 * Copyright (C) 2013 Benjamin Bouguet, Paul Chaignon
 *
 * DocXGenerator is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * DocXGenerator is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package generator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.cli.*;
import org.apache.commons.io.IOUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import fr.opensagres.xdocreport.converter.ConverterTypeTo;
import fr.opensagres.xdocreport.converter.ConverterTypeVia;
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

public class ReportGenerator {
	private static IContext contextMap;
	private static FieldsMetadata metadata;
	private static IXDocReport report;
	private static File template = null;
	private static String outputPath = null;

	/**
	 * Entry point for the program.
	 * Usage: java DocXGenerator templateFilePath outputFilePath.
	 * Takes the variables as JSON on the standard input.
	 * @param args Arguments from the command line.
	 */
	@SuppressWarnings("static-access")
	public static void main(String[] args) {
		CommandLine line = null ;
		
		   org.apache.commons.cli.Options options = new org.apache.commons.cli.Options();
		   options.addOption("h", "help", false, "prints the help content");
		   options.addOption(OptionBuilder.withArgName("json-file").hasArg().withDescription("input file with the JSON").withLongOpt("input").create("i"));
		   options.addOption(OptionBuilder.withArgName("file").hasArg().withDescription("output directory").withLongOpt("output").create("o"));
		   options.addOption(OptionBuilder.withArgName("name").isRequired().hasArg().withDescription("output file name (witout extension)").withLongOpt("name").create("n"));
		   options.addOption(OptionBuilder.withArgName("file").hasArg().isRequired().withDescription("template file").withLongOpt("template").create("t"));
		   options.addOption(new Option( "pdf", "generate output in pdf format" ));
		   options.addOption(new Option( "docx",  "generate output in docx format" ));
		   options.addOption(new Option( "html", "generate output in html format" ));
		   options.addOption(new Option( "a", "all" , false, "generate output in all format (default)" ));
		   
		   try{
		      CommandLineParser parser = new GnuParser();
		      line = parser.parse(options, args);
		   }
		   catch(MissingOptionException e){
		      boolean help = false;
		      try{
		    	 org.apache.commons.cli.Options helpOptions = new org.apache.commons.cli.Options();
		         helpOptions.addOption("h", "help", false, "prints the help content");
		         CommandLineParser parser = new PosixParser();
		         line = parser.parse(helpOptions, args);
		         if(line.hasOption("h")) help = true;
		      }
		      catch(Exception ex){ }
		      if(!help) System.err.println(e.getMessage());
		      HelpFormatter formatter = new HelpFormatter();
		      formatter.printHelp("ReportGenerator", options,true);
		      System.exit(1);
		   } catch(MissingArgumentException e){
		      System.err.println(e.getMessage());
		      HelpFormatter formatter = new HelpFormatter();
		      formatter.printHelp("ReportGenerator", options,true);
		      System.exit(1);
		   } catch(org.apache.commons.cli.ParseException e){
		      System.err.println("Error while parsing the command line: "+e.getMessage());
		      System.exit(1);
		   } catch(Exception e){
		       e.printStackTrace();
		    }
		
		
		GeneratorError result = GeneratorError.NO_ERROR;
		try {
			String directory = line.getOptionValue("output", "./");
			if (!directory.endsWith(File.separator))
				directory += File.separator;
			String filename = line.getOptionValue("name", "output");
			outputPath=directory+filename;
			template = new File(line.getOptionValue("template"));
			String jsonText=null;
			if (!line.hasOption("input"))
			{
				System.out.println("didnt have");
				// Initializes the input with the standard input
				jsonText = IOUtils.toString(System.in, "UTF-8");
				result = buildReportWithJSON(jsonText);
			}
			else
			{
				FileInputStream inputStream = new FileInputStream(line.getOptionValue("input"));
			    try {
			        jsonText = IOUtils.toString(inputStream);
			    } finally {
			        inputStream.close();
			    }
			}
			
			buildReportWithJSON(jsonText);
			
			if (line.hasOption("all"))
			{
				generateDocx();
				generateHTML();
				generatePdf();
			}
			else
			{
				if (line.hasOption("html"))
					generateHTML();
				if (line.hasOption("pdf"))
					generatePdf();
				if (line.hasOption("docx"))
					generateDocx();
			}
		} catch (IOException e) {
			System.err.println("Error: "+e.getMessage());
			System.exit(GeneratorError.IO_ERROR.getCode());
		} catch (GeneratorException e) {
			System.err.println("Error: "+e.getMessage());
			System.exit(e.getError().getCode());
		}
		System.exit(result.getCode());
	}

	/**
	 * Adds the text data from the JSON to the report's context.
	 * @param textObject The JSON text object.
	 */
	@SuppressWarnings("rawtypes")
	private static void computeText(JSONObject textObject) {
		// iterate on all the simple text
		Iterator<?> iter = textObject.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry entry = (Map.Entry)iter.next();
			// add the text to the report's context
			contextMap.put(entry.getKey().toString(), entry.getValue().toString());
		}
	}

	/**
	 * Initializes the FieldsMetaData for all images,
	 * creates them with the paths from the JSON and adds it to the report's context.
	 * @param imagesObject The JSON images object.
	 */
	@SuppressWarnings("rawtypes")
	private static void computeImages(JSONObject imagesObject) {
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
			contextMap.put(entry.getKey().toString(), img);
		}
	}

	/**
	 * Initializes the FieldsMetaData with the key of the JSON data
	 * and adds the list to the report's context.
	 * @param listObject The JSON list object.
	 */
	@SuppressWarnings("rawtypes")
	private static void computeList(JSONObject listObject) {
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
			contextMap.put(key, array);
		} 
	}
	
	/**
	 * Generates the DOCX and PDF reports with the data in the context.
	 * @param jsonText The JSON text.
	 * @param template The template DOCX file.
	 * @return An error code or 0 if all went well.
	 * @throws IOException If the template or output file can't be opened or written.
	 * @throws GeneratorException 
	 */
	public static GeneratorError buildReportWithJSON(String jsonText) throws IOException, GeneratorException {
		// Parses the JSON text:
		JSONObject json;
		try {
			JSONParser parser = new JSONParser();
			json = (JSONObject)parser.parse(jsonText);
		} catch(ParseException e) {
			throw new GeneratorException(GeneratorError.JSON_ERROR,"Error while parsing the JSON file:"+e.toString());
		}

		// Initializes the template file and creates the report object:
		InputStream in = new FileInputStream(template);
		try {
			report = XDocReportRegistry.getRegistry().loadReport(in, TemplateEngineKind.Velocity);
		} catch (XDocReportException e) {
			throw new GeneratorException(GeneratorError.TEMPLATE_ERROR,"Error with the template file:"+e.toString());
		}

		// Creates the FieldsMetadata.
		metadata = new FieldsMetadata();

		// Creates the report context:
		try {
			contextMap = report.createContext();
		} catch (XDocReportException e) {
			throw new GeneratorException(GeneratorError.CONTEXT_ERROR,"Error while creating the context:"+e.toString());
		}

		// Adds the data from the JSON to the report context:
		JSONObject textObject = (JSONObject)json.get("text");
		if(textObject == null) {
			throw new GeneratorException(GeneratorError.TEXT_MISSING,"Text element missing in JSON.");
		}
		computeText(textObject);
		JSONObject imagesObject = (JSONObject)json.get("images");
		if(imagesObject == null) {
			throw new GeneratorException(GeneratorError.IMAGES_MISSING,"Images element missing in JSON.");
		}
		computeImages(imagesObject);
		JSONObject listObject = (JSONObject)json.get("list");
		if(listObject == null) {
			throw new GeneratorException(GeneratorError.LIST_MISSING,"List element missing in JSON.");
		}
		computeList(listObject);

		// Links the FieldsMetaData to the report.
		report.setFieldsMetadata(metadata);
		
		return GeneratorError.NO_ERROR;
	}

	/**
	 * Generates the DOCX report with the data in the context.
	 * @param outputFile The path to the file which has to be written (without the extension).
	 * @return A generator error code or NO_ERROR if all went well.
	 * @see ErrorGenerator
	 * @throws IOException If the ouput file can't be opened or if it can't be written.
	 * @throws GeneratorException 
	 */
	private static GeneratorError generateDocx() throws IOException, GeneratorException {
		File outputFile = new File(outputPath+".docx");
		OutputStream out = new FileOutputStream(outputFile);
		try {
			report.process(contextMap, out);
		} catch (XDocReportException e) {
			throw new GeneratorException(GeneratorError.DOCX_GENERATION_ERROR,"Error while generating the DOCX file:"+e.getMessage());
		}
		return GeneratorError.NO_ERROR;
	}

	/**
	 * Generates the PDF report with the data in the context.
	 * @param outputFile The path to the file which has to be written (without the extension).
	 * @return A generator error code or NO_ERROR if all went well.
	 * @see GeneratorError
	 * @throws IOException If the ouput file can't be opened or if it can't be written.
	 * @throws GeneratorException 
	 */
	private static GeneratorError generatePdf() throws IOException, GeneratorException {
		File outputFile = new File(outputPath+".pdf");
		OutputStream out = new FileOutputStream(outputFile);
		Options options = Options.getTo(ConverterTypeTo.PDF).via(ConverterTypeVia.XWPF);
		try {
			report.convert(contextMap, options, out);
		} catch (XDocConverterException e) {
			throw new GeneratorException(GeneratorError.PDF_CONVERTION_ERROR,"Error while converting the PDF:"+e.getMessage());
		} catch (XDocReportException e) {
			throw new GeneratorException(GeneratorError.PDF_GENERATION_ERROR,"Error while generating the PDF:"+e.getMessage());
		}
		return GeneratorError.NO_ERROR;
	}
	
	/**
	 * Generates the HTML report with the data in the context.
	 * @param outputFile The path to the file which has to be written (without the extension).
	 * @return A generator error code or NO_ERROR if all went well.
	 * @see GeneratorError
	 * @throws IOException If the ouput file can't be opened or if it can't be written.
	 * @throws GeneratorException 
	 */
	private static GeneratorError generateHTML() throws IOException, GeneratorException {
		File outputFile = new File(outputPath+".html");
		OutputStream out = new FileOutputStream(outputFile);
		Options options = Options.getTo(ConverterTypeTo.XHTML).via(ConverterTypeVia.XWPF);
		try {
			report.convert(contextMap, options, out);
		} catch (XDocConverterException e) {
			throw new GeneratorException(GeneratorError.HTML_CONVERTION_ERROR,"Error while converting the HTML:"+e.getMessage());
		} catch (XDocReportException e) {
			throw new GeneratorException(GeneratorError.HTML_GENERATION_ERROR,"Error while generating the HTML:"+e.getMessage());
		}
		return GeneratorError.NO_ERROR;
	}
}
