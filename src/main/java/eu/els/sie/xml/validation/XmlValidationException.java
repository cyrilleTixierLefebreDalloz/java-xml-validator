package eu.els.sie.xml.validation;

import eu.els.sie.xml.validation.XmlValidationErrorHandler.LEVEL;

import java.util.List;
import java.util.Map;

public class XmlValidationException extends Exception {
	private static final long serialVersionUID = -3380305551685784482L;

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
				msg.append(String.join("\n", levelMessages));
			}
			msg.append("\n");
		}

		return msg.toString();
	}
}
