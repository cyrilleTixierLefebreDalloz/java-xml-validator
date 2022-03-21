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
import java.util.Objects;

public class XmlValidator {

	private static final Logger logger = LoggerFactory.getLogger(XmlValidator.class);
	private static final ClassLoader CL = XmlValidator.class.getClassLoader();

	private XmlValidator() {
		throw new IllegalStateException("Utility class");
	}

	// Declare handler for cp protocol
	// cp protocol is needed to resolve schema import within a schema included as maven dependency
	static {
		System.setProperty("java.protocol.handler.pkgs", "eu.els.sie.xml.validation");
	}

	/**
	 * Check if the input XML is well-formed
	 * The parser throws an exception if the document contains a doctype or is not well-formed
	 * @param xml
	 * @throws XmlValidationException
	 */
	public static void checkXMLWellFormedness(byte[] xml) throws XmlValidationException {
		try {
			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser parser = factory.newSAXParser();
			DefaultHandler handler = new DefaultHandler();
			InputStream inputStream = new ByteArrayInputStream(xml);
			parser.parse(new InputSource(inputStream), handler);
		} catch (ParserConfigurationException e) {
			throw new XmlValidationException(String.format("SAX parser configuration error: %s", e));
		} catch (SAXException e) {
			throw new XmlValidationException(String.format("Input stream is not well-formed: %s", e));
		} catch (IOException e) {
			throw new XmlValidationException(String.format("Error reading input stream: %s", e));
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
	 * @param catalogsPath list of catalogs without protocol, to be loaded with
	 * classLoader (no cp protocol) Any relative URI or cp:/ URI will work within
	 * xmlURI/schemaUri but not within catalogsPath
	 */
	public static void validate(byte[] xml, String schemaUri, List<String> catalogsPath)
			throws XmlValidationException, IOException {
		XmlValidationErrorHandler errorHandler = new XmlValidationErrorHandler();
		ValidationDriver validationDriver = new ValidationDriver(createPropertyMap(catalogsPath, errorHandler));

		InputStream xmlInputStream = new ByteArrayInputStream(xml);
		InputSource xmlInputSource = new InputSource(xmlInputStream);

		// DTD schema validation
		if (schemaUri.toLowerCase().endsWith("dtd")) {
			byte[] xmlWithDoctype = addDoctypeSystem(xmlInputStream, schemaUri);
			try {
				validateXmlWithDTD(xmlWithDoctype);
			} catch (XmlValidationException e) {
				throw new XmlValidationException(String.format("XML invalid against model '%s': %s", schemaUri, e),
						errorHandler.getReport());
			}
		}

		// Other schema validation
		else {
			try {
				validationDriver.loadSchema(new InputSource(schemaUri));
			} catch (IOException e) {
				throw new XmlValidationException(String.format("Error loading schema from URI '%s': %s", schemaUri, e));
			} catch (SAXException e) {
				throw new XmlValidationException(String.format("Schema parse error for '%s': %s", schemaUri, e));
			}
			try {
				if (!validationDriver.validate(xmlInputSource)) {
					throw new XmlValidationException(String.format("XML invalid against model '%s'", schemaUri),
							errorHandler.getReport());
				}
			} catch (IOException e) {
				throw new XmlValidationException(
						String.format("XML read error from InputSource '%s': %s", xmlInputSource.getSystemId(), e),
						errorHandler.getReport());
			} catch (SAXException e) {
				throw new XmlValidationException(String.format("XML parse error for '%s'", xmlInputSource.getSystemId()),
						errorHandler.getReport());
			}
		}
	}

	/**
	 * Validate XML containing doctype with dtd
	 * @param xml XML document as a byte array
	 * @throws XmlValidationException
	 */
	public static void validateXmlWithDTD(byte[] xml) throws XmlValidationException {
		try {
			SAXParserFactory factory = SAXParserFactory.newInstance();
			factory.setValidating(true);
			factory.setNamespaceAware(true);
			SAXParser parser = factory.newSAXParser();
			XMLReader reader = parser.getXMLReader();
			reader.setErrorHandler(
					new ErrorHandler() {
						public void warning(SAXParseException e) {
							logger.warn(e.getMessage()); // do nothing
						}

						public void error(SAXParseException e) throws SAXException {
							logger.error(e.getMessage());
							throw e;
						}

						public void fatalError(SAXParseException e) throws SAXException {
							logger.error(e.getMessage());
							throw e;
						}
					}
			);
			InputStream inputStream = new ByteArrayInputStream(xml);
			reader.parse(new InputSource(inputStream));
		} catch (ParserConfigurationException e) {
			throw new XmlValidationException(String.format("SAX parser configuration error: %s", e));
		} catch (SAXException e) {
			throw new XmlValidationException(String.format("Input stream is not valid: %s", e));
		} catch (IOException e) {
			throw new XmlValidationException(String.format("Error reading input stream: %s", e));
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
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document document = builder.parse(input);
			//  Create transformer
			Transformer transformer = TransformerFactory.newInstance().newTransformer();
			//  Set doctype
			transformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, doctypeSystemValue);

			Source source = new DOMSource(document);
			StreamResult result = new StreamResult(new ByteArrayOutputStream());
			transformer.transform(source, result);
			return ((ByteArrayOutputStream)result.getOutputStream()).toByteArray();
		} catch (TransformerException e) {
			throw new XmlValidationException(String.format("Problem while adding doctype : %s", e));
		} catch (IllegalArgumentException e) {
			throw new XmlValidationException(String.format("Input stream cannot be null: %s", e));
		} catch (IOException e) {
			throw new XmlValidationException(String.format("Error reading input stream: %s", e));
		} catch (SAXException e) {
			throw new XmlValidationException(String.format("Input stream is not well-formed: %s", e));
		} catch (ParserConfigurationException e) {
			throw new XmlValidationException(String.format("SAX parser configuration error: %s", e));
		}
	}

	private static PropertyMap createPropertyMap(List<String> catalogsPath, XmlValidationErrorHandler errHandler)
			throws XmlValidationException {
		PropertyMapBuilder propertyMapBuilder = new PropertyMapBuilder();

		// Set PropertyMap RESOLVER with catalogs
		if (catalogsPath != null) {
			List<String> catalogs = new ArrayList<>();
			for (String catalogUri : catalogsPath) {
				try {
					catalogs.add(Objects.requireNonNull(CL.getResource(catalogUri)).toURI().toASCIIString());
				} catch (URISyntaxException e) {
					throw new XmlValidationException(String.format("Bad URI syntax for catalog '%s'", catalogUri),
							errHandler.getReport());
				}
			}
			Resolver catalogResolver = new CatalogResolver(catalogs);
			propertyMapBuilder.put(ValidateProperty.RESOLVER, catalogResolver);
		}

		propertyMapBuilder.put(ValidateProperty.ERROR_HANDLER, errHandler);

		return propertyMapBuilder.toPropertyMap();
	}
}
