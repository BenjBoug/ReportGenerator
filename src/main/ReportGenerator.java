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

import exception.GeneratorException;
import generator.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.MissingArgumentException;
import org.apache.commons.cli.MissingOptionException;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.IOUtils;

import report.Report;

/**
 * Main class of ReportGeneration
 */
public class ReportGenerator {
	/**
	 * Entry point for the program.
	 * Takes the variables as JSON on the standard input.
	 * @param args Arguments from the command line.
	 */
	public static void main(String[] args) {
		CommandLine cmd = createOptions(args);

		GeneratorError result = GeneratorError.NO_ERROR;
		try {
			//Build the output name, by default ./output
			String directory = cmd.getOptionValue("output", "./");
			if (!directory.endsWith(File.separator))
				directory += File.separator;
			String filename = cmd.getOptionValue("name", "output");
			String output=directory+filename;

			//Get the JSON from file if given, or get it from the standard input.
			String jsonText=null;
			if (!cmd.hasOption("input"))
			{
				// Initializes the input with the standard input
				jsonText = IOUtils.toString(System.in, "UTF-8");
			}
			else // read the file
			{
				FileInputStream inputStream = new FileInputStream(cmd.getOptionValue("input"));
				try {
					jsonText = IOUtils.toString(inputStream);
				} finally {
					inputStream.close();
				}
			}

			//Build the report object
			Report report = new Report(jsonText,cmd.getOptionValue("template"),output);

			//Generate the document
			if (cmd.hasOption("all"))
			{
				new AllGenerator(report).generate();
			}
			else
			{
				if (cmd.hasOption("html"))
					new HTMLGenerator(report).generate();
				if (cmd.hasOption("pdf"))
					new PDFGenerator(report).generate();
				if (cmd.hasOption("doc"))
					new DocGenerator(report).generate();
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
	 * Creates the options for the given arguments
	 * @param args
	 * @return the CommandLine with the options
	 */
	@SuppressWarnings("static-access")
	public static CommandLine createOptions(String[] args)
	{
		CommandLine cmd=null;
		//create the options
		Options options = new Options();
		options.addOption("h", "help", false, "prints the help content");
		options.addOption(OptionBuilder.withArgName("json-file").hasArg().withDescription("input file with the JSON").withLongOpt("input").create("i"));
		options.addOption(OptionBuilder.withArgName("file").hasArg().withDescription("output directory (Default: ./)").withLongOpt("output").create("o"));
		options.addOption(OptionBuilder.withArgName("name").hasArg().withDescription("output file name (witout extension)").withLongOpt("name").create("n"));
		options.addOption(OptionBuilder.withArgName("file").hasArg().isRequired().withDescription("template file").withLongOpt("template").create("t"));
		options.addOption(new Option( "pdf", "generate output in pdf format" ));
		options.addOption(new Option( "doc",  "generate output in doc format (.odt or .docx, depend of the template format)" ));
		options.addOption(new Option( "html", "generate output in html format" ));
		options.addOption(new Option( "a", "all" , false, "generate output in all format (default)" ));

		//parse it
		try{
			CommandLineParser parser = new GnuParser();
			cmd = parser.parse(options, args);
		}
		catch(MissingOptionException e){
			displayHelp(options);
			System.exit(1);
		} catch(MissingArgumentException e){
			displayHelp(options);
			System.exit(1);
		} catch(ParseException e){
			System.err.println("Error while parsing the command line: "+e.getMessage());
			System.exit(1);
		} catch(Exception e){
			e.printStackTrace();
		}
		return cmd;
	}
	/**
	 * Display the help
	 * @param options
	 */
	public static void displayHelp(Options options)
	{
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp("ReportGenerator", options,true);		
	}
}
