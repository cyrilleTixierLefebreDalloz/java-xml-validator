package eu.els.sie.xml.validation;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXParseException;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class XmlValidationErrorHandler implements ErrorHandler {
	public enum LEVEL {WARN, ERROR, FATAL}

	public Map<LEVEL, List<String>> getReport() {
		return report;
	}

	// list of error messages encounterd during the validation, grouped by level of severity
	private final Map<LEVEL, List<String>> report = new EnumMap<>(LEVEL.class);

	public XmlValidationErrorHandler() {
		report.put(LEVEL.WARN, new ArrayList<>());
		report.put(LEVEL.ERROR, new ArrayList<>());
		report.put(LEVEL.FATAL, new ArrayList<>());
	}

	@Override
	public void warning(SAXParseException exception) {
		report.get(LEVEL.WARN).add(exception.getMessage());
	}

	@Override
	public void error(SAXParseException exception) {
		report.get(LEVEL.ERROR).add(exception.getMessage());
	}

	public void fatalError(SAXParseException exception) {
		report.get(LEVEL.FATAL).add(exception.getMessage());
	}
}
