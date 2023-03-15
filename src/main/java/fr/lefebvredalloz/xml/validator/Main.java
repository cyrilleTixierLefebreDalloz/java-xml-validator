package eu.els.sie.xml.validation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;

import static java.lang.String.format;

public class Main {
	private static final Logger logger = LoggerFactory.getLogger(XmlValidator.class);

	public static void main(String[] args) throws XmlValidationException, IOException {
		if (args.length != 2) {
			logger.error("Please supply exactly two argument, the path to the XML file and the path to the validation schema.");
			return;
		}
		final byte[] xmlContent = new FileInputStream(args[0]).readAllBytes();
		try {
			if (XmlValidator.validate(xmlContent, args[1], null)) {
				logger.info(format("XML %s is valid against schema %s", args[0], args[1]));
			}
		} catch (XmlValidationException e) {
			throw new XmlValidationException(e.getMessage());
		}
	}
}
