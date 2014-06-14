ReportGenerator
===============

ReportGenerator is a simple command line tool to generate document in DocX, PDF and HTML from a template file and JSON data.

You just have to give the path of a template file ([how to build it](https://code.google.com/p/xdocreport/wiki/DocxDesignReport)) and give 
the data in JSON format to create the merged document in the format you want.

Usage
===============

```
usage: ReportGenerator [-a] [-doc] [-h] [-html] [-i <json-file>] [-n <name>] [-o <file>] [-pdf] -t <file>
 -a,--all                 generate output file in all format (default)
 -doc                     generate output file in DocX format
 -h,--help                prints the help content
 -html                    generate output in HTML format
 -i,--input <json-file>   path of input file with the JSON
 -n,--name <name>         output file name witout extension (default: output)
 -o,--output <file>       path of output directory (Default: ./)
 -pdf                     generate output in PDF format
 -t,--template <file>     path of template file
```
 
Examples
===============

```
//generate the template test.docx in docx format
 ./ReportGenerator.jar -t examples/template/test.docx -doc
```
 
```
 //generate the template test.docx in pdf and HTML format
 ./ReportGenerator.jar -t examples/template/test.docx -pdf -html
```
 
```
 //generate the template test.docx in all format
 ./ReportGenerator.jar -t examples/template/test.docx -all
```
 
```
 //generate the template test.docx in docx format with a name (without extension)
 ./ReportGenerator.jar -t examples/template/test.docx -doc -n "name"
```
 
```
 //generate the template test.docx in docx format in a specified folder
 ./ReportGenerator.jar -t examples/template/test.docx -doc -o "folder/"
```
 
 References
===============

 - [XDocReport](https://code.google.com/p/xdocreport/)
   - The main library used for merging template and data, and for generating document.
 - [simple-json](https://code.google.com/p/json-simple/)
   - Used for decoding the JSON in input.
 - [VaRSoft](https://github.com/pchaigno/ProjetVaR)
   - The school project where this command line tool was wrote and used at the beginning.
