package main;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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

public class Report {

	private IContext context;
	private FieldsMetadata metadata;
	private IXDocReport report;
	private File template = null;
	private String output = null;
	
	public Report(String output, String template) throws GeneratorException
	{
		this.template = new File(template);
		
		if (!this.template.isFile())
			throw new GeneratorException(GeneratorError.PARAMETER_ERROR, "Template file does not exists.");
	}


	/**
	 * Generates the DOCX and PDF reports with the data in the context.
	 * @param jsonText The JSON text.
	 * @param template The template DOCX file.
	 * @return An error code or 0 if all went well.
	 * @throws IOException If the template or output file can't be opened or written.
	 * @throws GeneratorException 
	 */
	public void buildFromJson(String jsonText) throws GeneratorException, IOException
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
			context = report.createContext();
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
	
	public void convert(Options opt, OutputStream out) throws XDocConverterException, XDocReportException, IOException
	{
		report.convert(context, opt, out);
	}
	
	public void process(OutputStream out) throws XDocReportException, IOException
	{
		report.process(context, out);
	}
	
	public final IXDocReport getXDocReport()
	{
		return report;
	}
	
	public final String getOutput()
	{
		return output;
	}
	
	public IContext getContext() {
		return context;
	}

	public FieldsMetadata getMetadata() {
		return metadata;
	}

	public final String getTemplate()
	{
		return template.getAbsolutePath();
	}	
	
}
