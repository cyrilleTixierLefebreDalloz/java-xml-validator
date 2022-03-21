package eu.els.sie.xml.validation;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class XmlValidatorTest {

	private static final List<String> xmlCatalogsPathLoadedFromDependency = Arrays.asList(
			"sample/models/main/catalogs/catalog-for-dependency.xml",
			"sample/models/main/catalogs/catalog-for-dtd.xml");

	private static final List<String> xmlCatalogsPathLocal = List.of("catalog-local.xml");

	private static final ClassLoader CL = XmlValidatorTest.class.getClassLoader();

	byte[] getResourceContentFromResourcePath(String resourcePath) throws IOException {
		byte[] xmlContent = new FileInputStream(CL.getResource(resourcePath).getPath()).readAllBytes();
		return xmlContent;
	}

	/* ================================== */
	/* CHECK XML WELL-FORMEDNESS */
	/* ================================== */

	@Test
	void GIVEN_valid_xml_WHEN_check_wellFormedness_THEN_success() {
		try {
			String xmlResourcePath = "sample/models/test/nvdl/book-augmented-valid-nvdl.xml";
			byte[] xmlResourceContent = getResourceContentFromResourcePath(xmlResourcePath);

			XmlValidator.checkXMLWellFormedness(xmlResourceContent);
		} catch (Exception e) {
			fail("No Exception expected: " + e.getMessage());
		}
	}

	@Test
	void GIVEN_not_wellformed_xml_WHEN_check_wellFormedness_THEN_exception() throws IOException {
		String xmlResourcePath = "sample/models/test/book-not-wellformed.xml";
		byte[] xmlResourceContent = getResourceContentFromResourcePath(xmlResourcePath);

		Throwable expThatWasThrown = assertThrows(XmlValidationException.class,
				() -> XmlValidator.checkXMLWellFormedness(xmlResourceContent));;
		assertTrue(expThatWasThrown.getMessage().contains("Input stream is not well-formed"));
	}

	/* ================================== */
	/* ADD DOCTYPE */
	/* ================================== */

	@Test
	void GIVEN_valid_xml_WHEN_add_doctype_THEN_success() {
		try (InputStream in = CL.getResourceAsStream("sample/models/test/dtd/book-valid-dtd.xml")) {
			XmlValidator.addDoctypeSystem(in, "test");
		} catch (IOException | XmlValidationException e) {
			fail("No Exception expected");
		}
	}

	@Test
	void GIVEN_not_exists_xml_WHEN_add_doctype_THEN_exception() {
		InputStream in = CL.getResourceAsStream("sample/models/test/file_not_exists.xml");
		Throwable expThatWasThrown = assertThrows(XmlValidationException.class,
				() -> XmlValidator.addDoctypeSystem(in, "test"));;
		assertTrue(expThatWasThrown.getMessage().contains("Input stream cannot be null"));
	}

	@Test
	void GIVEN_not_wellformed_xml_WHEN_add_doctype_THEN_exception() {
		InputStream in = CL.getResourceAsStream("sample/models/test/book-not-wellformed.xml");
		Throwable expThatWasThrown = assertThrows(XmlValidationException.class,
				() -> XmlValidator.addDoctypeSystem(in, "test"));;
		assertTrue(expThatWasThrown.getMessage().contains("Input stream is not well-formed"));
	}

	/* ================================== */
	/* RELAX NG Compact */
	/* ================================== */

	@Test
	@Disabled("RNC not working")
	void GIVEN_xml_and_simple_rnc_and_no_catalog_WHEN_validate_THEN_success() {
		try {
			String xmlResourcePath = "sample/models/test/rng/simple-book-valid-rnc.xml";
			byte[] xmlResourceContent = getResourceContentFromResourcePath(xmlResourcePath);

			XmlValidator.validate(xmlResourceContent, "cp:/xml-multi-models-sample/main/grammars/rnc/simple-book.rnc", null);
		} catch (Exception e) {
			fail("No Exception expected: " + e.getMessage());
		}
	}

	/* ================================== */
	/* RELAX NG XML */
	/* ================================== */

	@Test
	void GIVEN_valid_xml_and_simple_rng_and_no_catalog_WHEN_validate_THEN_success() {
		try {
			String xmlResourcePath = "sample/models/test/rng/simple-book-valid-rng.xml";
			byte[] xmlResourceContent = getResourceContentFromResourcePath(xmlResourcePath);

			XmlValidator.validate(xmlResourceContent, "cp:/xml-multi-models-sample/main/grammars/rng/simple-book/simple-book.rng", null);
		} catch (Exception e) {
			fail("No Exception expected: " + e.getMessage());
		}
	}

	@Test
	void GIVEN_invalid_xml_and_simple_rng_and_no_catalog_WHEN_validate_THEN_exception() throws IOException {
		String xmlResourcePath = "sample/models/test/rng/simple-book-invalid-rng.xml";
		byte[] xmlResourceContent = getResourceContentFromResourcePath(xmlResourcePath);

		Throwable expThatWasThrown = assertThrows(XmlValidationException.class,
				() -> XmlValidator.validate(xmlResourceContent, "cp:/xml-multi-models-sample/main/grammars/rng/simple-book/simple-book.rng", null));
		assertTrue(expThatWasThrown.getMessage().contains("XML invalid against model"));
	}

	@Test
	void GIVEN_valid_xml_and_rng_with_include_and_no_catalog_WHEN_validate_THEN_success() {
		try {
			String xmlResourcePath = "sample/models/test/rng/book-valid-rng.xml";
			byte[] xmlResourceContent = getResourceContentFromResourcePath(xmlResourcePath);

			XmlValidator.validate(xmlResourceContent, "cp:/xml-multi-models-sample/main/grammars/rng/book/book.rng", null);
		} catch (Exception e) {
			fail("No Exception expected: " + e.getMessage());
		}
	}

	@Test
	void GIVEN_valid_xml_with_doctype_and_rng_with_include_and_no_catalog_WHEN_validate_THEN_success() {
		try {
			String xmlResourcePath = "sample/models/test/rng/book-with-doctype-valid-rng.xml";
			byte[] xmlResourceContent = getResourceContentFromResourcePath(xmlResourcePath);

			XmlValidator.validate(xmlResourceContent, "cp:/xml-multi-models-sample/main/grammars/rng/book/book.rng", null);
		} catch (Exception e) {
			fail("No Exception expected: " + e.getMessage());
		}
	}

	@Test
	void GIVEN_valid_xml_with_doctype_with_entities_and_rng_with_include_and_no_catalog_WHEN_validate_THEN_success() {
		try {
			String xmlResourcePath = "sample/models/test/rng/book-with-doctype-with-entities-valid-rng.xml";
			byte[] xmlResourceContent = getResourceContentFromResourcePath(xmlResourcePath);

			XmlValidator.validate(xmlResourceContent, "cp:/xml-multi-models-sample/main/grammars/rng/book/book.rng", null);
		} catch (Exception e) {
			fail("No Exception expected: " + e.getMessage());
		}
	}

	@Test
	void GIVEN_valid_xml_with_doctype_with_unparsed_entity_and_rng_with_include_and_no_catalog_WHEN_validate_THEN_success() {
		try {
			String xmlResourcePath = "sample/models/test/rng/book-with-doctype-with-unparsed-entities-valid-rng.xml";
			byte[] xmlResourceContent = getResourceContentFromResourcePath(xmlResourcePath);

			XmlValidator.validate(xmlResourceContent, "cp:/xml-multi-models-sample/main/grammars/rng/book/book.rng", null);
		} catch (Exception e) {
			fail("No Exception expected: " + e.getMessage());
		}
	}

	@Test
	@DisplayName("valid XML with DOCTYPE needs catalog with unparsed entity - RNG with include - xmlCatalogsPathLocal")
	void GIVEN_valid_xml_with_doctype_and_invalid_path_local_catalog_for_entities_and_rng_with_include_WHEN_validate_THEN_exception() throws IOException {
		// NB: validationDriver.validate() throws a NPE if the the path to the DTD is
		// invalid
		String xmlResourcePath = "sample/models/test/rng/book-with-doctype-needs-catalog-with-entities-valid-rng.xml";
		byte[] xmlResourceContent = getResourceContentFromResourcePath(xmlResourcePath);

		assertThrows(NullPointerException.class,
				() -> XmlValidator.validate(xmlResourceContent, "cp:/xml-multi-models-sample/main/grammars/rng/book/book.rng", xmlCatalogsPathLocal));
	}

	@Test
	@Disabled("Does not work, throws a NullPointerException")
	@DisplayName("Relax NG Validation : valid XML with DOCTYPE needs catalog with unparsed entity - RNG with include - xmlCatalogsPathLoadedFromDependency")
	void GIVEN_valid_xml_with_doctype_and_cp_catalog_for_entities_and_rng_with_include_WHEN_validate_THEN_success() {
		try {
			String xmlResourcePath = "sample/models/test/rng/book-with-doctype-needs-catalog-with-entities-valid-rng.xml";
			byte[] xmlResourceContent = getResourceContentFromResourcePath(xmlResourcePath);
			XmlValidator.validate(xmlResourceContent, "cp:/xml-multi-models-sample/main/grammars/rng/book/book.rng", xmlCatalogsPathLoadedFromDependency);
		} catch (Exception e) {
			fail("No Exception expected: " + e.getMessage());
		}
	}

	/* ================================== */
	/* DTD */
	/* ================================== */

	@Test
	@DisplayName("DTD Validation : valid XML - Simple DTD - no Catalog")
	void GIVEN_valid_xml_and_simple_dtd_and_no_catalog_WHEN_validate_THEN_no_exception() {
		try {
			String xmlResourcePath = "sample/models/test/dtd/book-valid-dtd.xml";
			byte[] xmlResourceContent = getResourceContentFromResourcePath(xmlResourcePath);

			XmlValidator.validate(xmlResourceContent, "cp:/xml-multi-models-sample/main/grammars/dtd/book/book.dtd", null);
		} catch (Exception e) {
			fail("No Exception expected: " + e.getMessage());
		}
	}

	@Test
	@DisplayName("DTD Validation : invalid XML - Simple DTD - no Catalog")
	void GIVEN_invalid_xml_and_simple_dtd_and_no_catalog_WHEN_validate_THEN_exception() throws IOException {
		String xmlResourcePath = "sample/models/test/dtd/book-invalid-dtd.xml";
		byte[] xmlResourceContent = getResourceContentFromResourcePath(xmlResourcePath);

		Throwable expThatWasThrown = assertThrows(XmlValidationException.class,
				() -> XmlValidator.validate(xmlResourceContent, "cp:/xml-multi-models-sample/main/grammars/dtd/book/book.dtd", null));
		assertTrue(expThatWasThrown.getMessage().contains("XML invalid against model"));
	}

	/* ================================== */
	/* XSD */
	/* ================================== */

	@Test
	void GIVEN_valid_xml_and_simple_xsd_and_no_catalog_WHEN_validate_() {
		try {
			String xmlResourcePath = "sample/models/test/xsd/simple-book-valid-xsd.xml";
			byte[] xmlResourceContent = getResourceContentFromResourcePath(xmlResourcePath);

			XmlValidator.validate(xmlResourceContent, "cp:/xml-multi-models-sample/main/grammars/xsd/simple-book.xsd", null);
		} catch (Exception e) {
			fail("No Exception expected: " + e.getMessage());
		}
	}

	@Test
	void GIVEN_invalid_xml_and_simple_xsd_and_no_catalog_WHEN_validate_THEN_exception() throws IOException {
		String xmlResourcePath = "sample/models/test/xsd/simple-book-invalid-xsd.xml";
		byte[] xmlResourceContent = getResourceContentFromResourcePath(xmlResourcePath);

		Throwable expThatWasThrown = assertThrows(XmlValidationException.class,
				() -> XmlValidator.validate(xmlResourceContent, "cp:/xml-multi-models-sample/main/grammars/xsd/simple-book.xsd", null));
		assertTrue(expThatWasThrown.getMessage().contains("XML invalid against model"));
	}

	@Test
	void GIVEN_valid_xml_and_xsd_with_inclusion_and_no_catalog_WHEN_validate_THEN_success() {
		try {
			String xmlResourcePath = "sample/models/test/xsd/book-valid-xsd.xml";
			byte[] xmlResourceContent = getResourceContentFromResourcePath(xmlResourcePath);

			XmlValidator.validate(xmlResourceContent, "cp:/xml-multi-models-sample/main/grammars/xsd/book/book.xsd", null);
		} catch (Exception e) {
			fail("No Exception expected: " + e.getMessage());
		}
	}

	@Test
	@DisplayName("Test XSD Validation : invalid XML - XSD with inclusion - no Catalog")
	void GIVEN_invalid_xml_and_xsd_with_inclusion_WHEN_validate_THEN_exception() throws IOException {
		String xmlResourcePath = "sample/models/test/xsd/book-invalid-xsd.xml";
		byte[] xmlResourceContent = getResourceContentFromResourcePath(xmlResourcePath);

		Throwable expThatWasThrown = assertThrows(XmlValidationException.class,
				() -> XmlValidator.validate(xmlResourceContent, "cp:/xml-multi-models-sample/main/grammars/xsd/book/book.xsd", null));
		assertTrue(expThatWasThrown.getMessage().contains("XML invalid against model"));
	}

	/* ================================== */
	/* Schematron */
	/* ================================== */

	@Test
	@DisplayName("Test Schematron Validation : valid XML - simple schematron 1.5 no inclusion - no catalog")
	void validationRNG_xmlValid_sch15Simple_noCatalog() {
		try {
			String xmlResourcePath = "sample/models/test/sch/book-invalid-sch.xml";
			byte[] xmlResourceContent = getResourceContentFromResourcePath(xmlResourcePath);

			XmlValidator.validate(xmlResourceContent, "cp:/xml-multi-models-sample/main/grammars/sch/book-sch1.5.sch", null);
		} catch (Exception e) {
			fail("No Exception expected: " + e.getMessage());
		}
	}

	@Test
	@DisplayName("Test Schematron Validation : Invalid XML - simple schematron no inclusion - no catalog")
	void validationRNG_xmlInvalid_schSimple_noCatalog() throws IOException {
		String xmlResourcePath = "sample/models/test/sch/book-invalid-sch.xml";
		byte[] xmlResourceContent = getResourceContentFromResourcePath(xmlResourcePath);

		Throwable expThatWasThrown = assertThrows(XmlValidationException.class,
				() -> XmlValidator.validate(xmlResourceContent, "cp:/xml-multi-models-sample/main/grammars/sch/book-no-xslt.sch", null));
		assertTrue(expThatWasThrown.getMessage().contains("XML invalid against model"));
	}

	@Test
	@DisplayName("Test Schematron Validation : invalid XML - schematron with xsl:include 1 level - no catalog")
	void validationRNG_xmlInvalid_schWithXslInclude1Level_noCatalog() throws IOException {
		String xmlResourcePath = "sample/models/test/sch/book-invalid-sch.xml";
		byte[] xmlResourceContent = getResourceContentFromResourcePath(xmlResourcePath);

		Throwable expThatWasThrown = assertThrows(XmlValidationException.class,
				() -> XmlValidator.validate(xmlResourceContent, "cp:/xml-multi-models-sample/main/grammars/sch/book-xslt-include-1-level.sch", null));
		assertTrue(expThatWasThrown.getMessage().contains("XML invalid against model"));
	}

	@Test
	@DisplayName("Test Schematron Validation : valid XML - schematron with xsl:include 2 levels - no catalog")
	void validationRNG_xmlValid_schWithXslInclude2Levels_noCatalog() {
		try {
			String xmlResourcePath = "sample/models/test/sch/book-valid-sch.xml";
			byte[] xmlResourceContent = getResourceContentFromResourcePath(xmlResourcePath);

			XmlValidator.validate(xmlResourceContent, "cp:/xml-multi-models-sample/main/grammars/sch/book-xslt-include-2-levels.sch", null);
		} catch (Exception e) {
			fail("No Exception expected: " + e.getMessage());
		}
	}

	@Test
	@DisplayName("Test Schematron Validation : valid XML - schematron matching text node - no catalog")
	void validationRNG_xmlValid_schMatchingTextNode_noCatalog() {
		try {
			String xmlResourcePath = "sample/models/test/sch/book-valid-sch.xml";
			byte[] xmlResourceContent = getResourceContentFromResourcePath(xmlResourcePath);

			XmlValidator.validate(xmlResourceContent, "cp:/xml-multi-models-sample/main/grammars/sch/match-text.sch", null);
		} catch (Exception e) {
			fail("No Exception expected: " + e.getMessage());
		}
	}

	/* ================================== */
	/* NVDL */
	/* ================================== */

	@Test
	@DisplayName("Test NVDL Validation : valid XML - NVDL with dependencies (rng + rnc calling dependency:/ + sch) - xmlCatalogsPathLocal")
	void validationNVDL_xmlValid_nvdlWithDependencies_xmlCatalogsPathLocal() {
		try {
			String xmlResourcePath = "sample/models/test/nvdl/book-augmented-valid-nvdl.xml";
			byte[] xmlResourceContent = getResourceContentFromResourcePath(xmlResourcePath);

			XmlValidator.validate(xmlResourceContent, "cp:/xml-multi-models-sample/main/grammars/nvdl/book-augmented.nvdl", xmlCatalogsPathLocal);
		} catch (Exception e) {
			fail("No Exception expected: " + e.getMessage());
		}
	}

	@Test
	@DisplayName("Test NVDL Validation : invalid XML - EE NVDL (rng + sch)")
	void validationNVDL_xmlInvalid_nvdl_ee() throws IOException {
		String xmlResourcePath = "sample/models/test/nvdl/book-augmented-invalid-nvdl.xml";
		byte[] xmlResourceContent = getResourceContentFromResourcePath(xmlResourcePath);

		Throwable expThatWasThrown = assertThrows(XmlValidationException.class,
				() -> XmlValidator.validate(xmlResourceContent, "cp:/xml-multi-models-sample/main/grammars/nvdl/book-augmented.nvdl", xmlCatalogsPathLocal));
		assertTrue(expThatWasThrown.getMessage().contains("XML invalid against model"));
	}

	@Test
	@DisplayName("Test NVDL Validation : valid XML - simple NVDL (rng + sch) - no catalog")
	void validationNVDL_xmlValid_nvdlSimple_noCatalog() {
		try {
			String xmlResourcePath = "sample/models/test/nvdl/book-valid-nvdl.xml";
			byte[] xmlResourceContent = getResourceContentFromResourcePath(xmlResourcePath);

			XmlValidator.validate(xmlResourceContent, "cp:/xml-multi-models-sample/main/grammars/nvdl/book-and-schematron.nvdl", null);
		} catch (Exception e) {
			fail("No Exception expected: " + e.getMessage());
		}
	}

	@Test
	@DisplayName("Test NVDL Validation : invalid XML - simple NVDL (rng + sch) - no catalog")
	void validationNVDL_xmlInvalid_nvdlSimple_noCatalog() throws IOException {
		String xmlResourcePath = "sample/models/test/nvdl/book-invalid-nvdl.xml";
		byte[] xmlResourceContent = getResourceContentFromResourcePath(xmlResourcePath);

		Throwable expThatWasThrown = assertThrows(XmlValidationException.class,
				() -> XmlValidator.validate(xmlResourceContent, "cp:/xml-multi-models-sample/main/grammars/nvdl/book-and-schematron.nvdl", null));
		assertTrue(expThatWasThrown.getMessage().contains("XML invalid against model"));
	}


	@Test
	@DisplayName("Test NVDL Validation : valid XML - NVDL with dependencies (rng + rnc calling dependency:/ + sch) - xmlCatalogsPathLoadedFromDependency")
	void validationNVDL_xmlValid_nvdlWithDependencies_xmlCatalogsPathLoadedFromDependency() {
		try {
			String xmlResourcePath = "sample/models/test/nvdl/book-augmented-valid-nvdl.xml";
			byte[] xmlResourceContent = getResourceContentFromResourcePath(xmlResourcePath);

			XmlValidator.validate(xmlResourceContent, "cp:/xml-multi-models-sample/main/grammars/nvdl/book-augmented.nvdl", xmlCatalogsPathLoadedFromDependency);
		} catch (Exception e) {
			fail("No Exception expected: " + e.getMessage());
		}
	}

	@Test
	void GIVEN_instantiate_new_class_THEN_throw_Exception() throws NoSuchMethodException, SecurityException {

		Constructor<XmlValidator> c;
		c = XmlValidator.class.getDeclaredConstructor();
		try {
			c.setAccessible(true);
			c.newInstance();
			fail("Should raise an Exception");
		} catch (Exception e) {
			Assertions.assertEquals(IllegalStateException.class, e.getCause().getClass());
		}
	}

}