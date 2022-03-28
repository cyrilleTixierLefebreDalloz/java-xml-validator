package eu.els.sie.xml.validation;

import eu.els.sie.xml.validation.XmlValidationErrorHandler.LEVEL;

import java.util.List;
import java.util.Map;

import static java.lang.String.join;

public class XmlValidationException extends Exception {

	private final Map<LEVEL, List<String>> report;

	public XmlValidationException(String message) {
		super(message);
		report = null;
	}

	public XmlValidationException(String message, Map<LEVEL, List<String>> validationReport) {
		super(message);
		report = validationReport;
	}

	@Override
	public String getMessage() {
		StringBuilder msg = new StringBuilder();
		msg.append(super.getMessage());
		msg.append("\n");
		if (report != null) {
			for (List<String> levelMessages : report.values()) {
				msg.append(join("\n", levelMessages));
			}
			msg.append("\n");
		}

		return msg.toString();
	}
}
