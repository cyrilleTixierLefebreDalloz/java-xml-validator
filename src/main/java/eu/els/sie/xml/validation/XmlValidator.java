package eu.els.sie.xml.validation;

import com.thaiopensource.resolver.Resolver;
import com.thaiopensource.resolver.catalog.CatalogResolver;
import com.thaiopensource.util.PropertyMap;
import com.thaiopensource.util.PropertyMapBuilder;
import com.thaiopensource.validate.ValidateProperty;
import com.thaiopensource.validate.ValidationDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class XmlValidator {

	private static final Logger logger = LoggerFactory.getLogger(XmlValidator.class);
	private static final ClassLoader CLASS_LOADER = XmlValidator.class.getClassLoader();
	private static final String DTD_EXTENSION = "dtd";

	private static final String EXTERNAL_GENERAL_ENTITIES = "http://xml.org/sax/features/external-general-entities";
	private static final String EXTERNAL_PARAMETER_ENTITIES = "http://xml.org/sax/features/external-parameter-entities";

	private static final String EXCEPTION_PARSER_CONFIGURATION = "SAX parser configuration error";
	private static final String EXCEPTION_INVALID_XML = "XML is not valid";
	private static final String EXCEPTION_INVALID_XML_AGAINST_MODEL = "XML is not valid against model";
	private static final String EXCEPTION_XML_NOT_WELL_FORMED = "XML is not well formed";
	private static final String EXCEPTION_INPUT_STREAM_NULL = "Input stream cannot be null";
	private static final String EXCEPTION_READING_INPUT_STREAM = "Error while reading input stream";
	private static final String EXCEPTION_LOADING_SCHEMA = "Error while loading schema";
	private static final String EXCEPTION_PARSING_SCHEMA = "Error while parsing schema";
	private static final String EXCEPTION_ADDING_DOCTYPE = "Problem while adding doctype";

	private XmlValidator() {
		throw new IllegalStateException("Utility class");
	}

	// Declare handler for classpath protocol
	// classpath protocol is needed to resolve schema import within a schema included as maven dependency
	static {
		System.setProperty("java.protocol.handler.pkgs", "eu.els.sie.xml.validation");
	}

	/**
	 * Check if the input XML is well formed
	 * The parser throws an exception if the document is not well formed
	 * @param xml
	 * @throws XmlValidationException
	 */
	public static void checkXmlWellFormedness(byte[] xml) throws XmlValidationException {
		try {
			SAXParserFactory factory = SAXParserFactory.newInstance();
			factory.setFeature(EXTERNAL_GENERAL_ENTITIES, false);
			SAXParser parser = factory.newSAXParser();
			DefaultHandler handler = new DefaultHandler();
			InputStream inputStream = new ByteArrayInputStream(xml);
			parser.parse(new InputSource(inputStream), handler);
		} catch (ParserConfigurationException e) {
			throw new XmlValidationException(EXCEPTION_PARSER_CONFIGURATION + ": " + e);
		} catch (SAXException e) {
			throw new XmlValidationException(EXCEPTION_XML_NOT_WELL_FORMED + ": " + e);
		} catch (IOException e) {
			throw new XmlValidationException(EXCEPTION_READING_INPUT_STREAM + ": " + e);
		}
	}

	/**
	 * Validate any XML document with RNG, RNC, Schematron1.5, Iso-Schematron, NVDL, DTD
	 *
	 * @param xml XML document as a byte array
	 *
	 * @param schemaUri URI of the schema, it might use cp protocol :
	 * cp:/path/to/model.rng
	 *
	 * @param catalogPaths list of catalogs without protocol, to be loaded with
	 * classLoader (no cp protocol) Any relative URI or cp:/ URI will work within
	 * xmlURI/schemaUri but not within catalogsPath
	 */
	public static void validate(byte[] xml, String schemaUri, List<String> catalogPaths) throws XmlValidationException {
		// DTD schema validation
		if (schemaUri.toLowerCase().endsWith(DTD_EXTENSION)) {
			InputStream xmlInputStream = new ByteArrayInputStream(xml);
			byte[] xmlWithDoctype = addDoctypeSystem(xmlInputStream, schemaUri);
			validateXmlWithDTD(xmlWithDoctype);
		}
		// Other schema validation
		else {
			validateXmlWithSchema(xml, schemaUri, catalogPaths);
		}
	}

	/**
	 * Validate XML containing doctype with DTD
	 * @param xml XML document as a byte array
	 * @throws XmlValidationException
	 */
	public static void validateXmlWithDTD(byte[] xml) throws XmlValidationException {
		try {
			SAXParserFactory factory = SAXParserFactory.newInstance();
//			factory.setFeature(EXTERNAL_GENERAL_ENTITIES, false);
//			factory.setFeature(EXTERNAL_PARAMETER_ENTITIES, false);
			factory.setValidating(true);
			factory.setNamespaceAware(true);
			SAXParser parser = factory.newSAXParser();
			XMLReader reader = parser.getXMLReader();
			ErrorHandlerImpl errorHandler = new ErrorHandlerImpl();
			reader.setErrorHandler(errorHandler);
			InputStream inputStream = new ByteArrayInputStream(xml);
			reader.parse(new InputSource(inputStream));
		} catch (ParserConfigurationException e) {
			throw new XmlValidationException(EXCEPTION_PARSER_CONFIGURATION + ": " + e);
		} catch (SAXException e) {
			throw new XmlValidationException(EXCEPTION_INVALID_XML + ": " + e);
		} catch (IOException e) {
			throw new XmlValidationException(EXCEPTION_READING_INPUT_STREAM + ": " + e);
		}
	}

	/**
	 * Validate XML document with RNG, RNC, Schematron1.5, Iso-Schematron or NVDL
	 * @param xml
	 * @param schemaUri
	 * @param catalogPaths
	 * @throws XmlValidationException
	 */
	public static void validateXmlWithSchema(byte[] xml, String schemaUri, List<String> catalogPaths) throws XmlValidationException {
		XmlValidationErrorHandler errorHandler = new XmlValidationErrorHandler();
		ValidationDriver validationDriver = new ValidationDriver(createPropertyMap(catalogPaths, errorHandler));
		InputStream xmlInputStream = new ByteArrayInputStream(xml);
		InputSource xmlInputSource = new InputSource(xmlInputStream);
		try {
			validationDriver.loadSchema(new InputSource(schemaUri));
		} catch (IOException e) {
			throw new XmlValidationException(EXCEPTION_LOADING_SCHEMA + " " + schemaUri + ": " + e);
		} catch (SAXException e) {
			throw new XmlValidationException(EXCEPTION_PARSING_SCHEMA + " " + schemaUri + ": " + e);
		}
		try {
			if (!validationDriver.validate(xmlInputSource)) {
				throw new XmlValidationException(EXCEPTION_INVALID_XML_AGAINST_MODEL + " " + schemaUri, errorHandler.getReport());
			}
		} catch (IOException e) {
			throw new XmlValidationException(EXCEPTION_READING_INPUT_STREAM + " " + xmlInputSource.getSystemId() + e, errorHandler.getReport());
		} catch (SAXException e) {
			throw new XmlValidationException(EXCEPTION_INVALID_XML + " " + xmlInputSource.getSystemId(), errorHandler.getReport());
		}
	}

	/**
	 * Add doctype to input XML
	 * @param input input document as an input stream
	 * @return xml byte array with doctype
	 */
	public static byte[] addDoctypeSystem(InputStream input, String doctypeSystemValue) throws XmlValidationException {
		try{
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setFeature(EXTERNAL_GENERAL_ENTITIES, false);
			factory.setFeature(EXTERNAL_PARAMETER_ENTITIES, false);
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document document = builder.parse(input);
			//  Create transformer
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			//  Set doctype
			transformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, doctypeSystemValue);

			Source source = new DOMSource(document);
			StreamResult result = new StreamResult(new ByteArrayOutputStream());
			transformer.transform(source, result);
			return ((ByteArrayOutputStream)result.getOutputStream()).toByteArray();
		} catch (TransformerException e) {
			throw new XmlValidationException(EXCEPTION_ADDING_DOCTYPE + ": " + e);
		} catch (IllegalArgumentException e) {
			throw new XmlValidationException(EXCEPTION_INPUT_STREAM_NULL + ": " + e);
		} catch (IOException e) {
			throw new XmlValidationException(EXCEPTION_READING_INPUT_STREAM + ": " + e);
		} catch (SAXException e) {
			throw new XmlValidationException(EXCEPTION_XML_NOT_WELL_FORMED + ": " + e);
		} catch (ParserConfigurationException e) {
			throw new XmlValidationException(EXCEPTION_PARSER_CONFIGURATION + ": " + e);
		}
	}

	private static PropertyMap createPropertyMap(List<String> catalogPaths, XmlValidationErrorHandler errHandler) throws XmlValidationException {
		PropertyMapBuilder propertyMapBuilder = new PropertyMapBuilder();

		// Set PropertyMap RESOLVER with catalogs
		if (catalogPaths != null) {
			List<String> catalogs = new ArrayList<>();
			for (String catalogUri : catalogPaths) {
				try {
					catalogs.add(CLASS_LOADER.getResource(catalogUri).toURI().toASCIIString());
				} catch (URISyntaxException e) {
					throw new XmlValidationException("Bad URI syntax for catalog " + catalogUri, errHandler.getReport());
				}
			}
			Resolver catalogResolver = new CatalogResolver(catalogs);
			propertyMapBuilder.put(ValidateProperty.RESOLVER, catalogResolver);
		}

		propertyMapBuilder.put(ValidateProperty.ERROR_HANDLER, errHandler);

		return propertyMapBuilder.toPropertyMap();
	}

	private static class ErrorHandlerImpl implements ErrorHandler {
		@Override
		public void warning(SAXParseException exception) throws SAXException {
			logger.warn(exception.getMessage()); // do nothing
		}

		@Override
		public void error(SAXParseException exception) throws SAXException {
			logger.error(exception.getMessage());
			throw exception;
		}

		@Override
		public void fatalError(SAXParseException exception) throws SAXException {
			logger.error(exception.getMessage());
			throw exception;
		}
	}
}
