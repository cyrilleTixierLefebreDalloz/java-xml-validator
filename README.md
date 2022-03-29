# java-xml-validator
Java program and API to validate XML with different kind of grammars : XSD, DTD, RNG, RNC, Schematron, NVDL

To run the program, you need to :
1. Clone the project
2. Run mvn clean install
3. Edit the configuration of your main class to add two arguments :
    * The XML file path
    * The schema file path
4. Run the main class in your IDE 
5. Or you can also run the program in command line from the target directory :
```
java -jar .\target\xml-validator-1.00.00-SNAPSHOT-jar-with-dependencies.jar <xml_file_path> <schema_file_path>
```