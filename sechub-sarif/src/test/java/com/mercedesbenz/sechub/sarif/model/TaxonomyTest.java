package com.mercedesbenz.sechub.sarif.model;

import static com.mercedesbenz.sechub.test.PojoTester.*;

import java.util.LinkedList;
import org.junit.jupiter.api.Test;

class TaxonomyTest {

	@Test
	void test_equals_and_hashcode() {
		/* @formatter:off */
        testBothAreEqualAndHaveSameHashCode(createExample(), createExample());

        testBothAreNOTEqual(createExample(), change(createExample(), (taxonomy) -> taxonomy.setGuid("25361018-c7c6-11ec-9fb2-f3f888797467")));
        testBothAreNOTEqual(createExample(), change(createExample(), (taxonomy) -> taxonomy.setName("other-name")));
        testBothAreNOTEqual(createExample(), change(createExample(), (taxonomy) -> taxonomy.setFullName("other-full-name")));
        testBothAreNOTEqual(createExample(), change(createExample(), (taxonomy) -> taxonomy.setProduct("other-product")));
        testBothAreNOTEqual(createExample(), change(createExample(), (taxonomy) -> taxonomy.setProductSuite("other-product-suite")));
        testBothAreNOTEqual(createExample(), change(createExample(), (taxonomy) -> taxonomy.setSemanticVersion("2.0.1")));
        testBothAreNOTEqual(createExample(), change(createExample(), (taxonomy) -> taxonomy.setVersion("2.1.1")));
        testBothAreNOTEqual(createExample(), change(createExample(), (taxonomy) -> taxonomy.setReleaseDateUtc("2019-02-04T12:08:25.943Z")));
        testBothAreNOTEqual(createExample(), change(createExample(), (taxonomy) -> taxonomy.setDownloadUri("https://www.otherUri.com/download")));
        testBothAreNOTEqual(createExample(), change(createExample(), (taxonomy) -> taxonomy.setInformationUri("https://www.otherUri.com/documentation")));
        testBothAreNOTEqual(createExample(), change(createExample(), (taxonomy) -> taxonomy.setOrganization("other-organization")));
        testBothAreNOTEqual(createExample(), change(createExample(), (taxonomy) -> taxonomy.setShortDescription(new Message("other"))));
        testBothAreNOTEqual(createExample(), change(createExample(), (taxonomy) -> taxonomy.setFullDescription(new Message("other"))));
        testBothAreNOTEqual(createExample(), change(createExample(), (taxonomy) -> taxonomy.setLanguage("de")));
        testBothAreNOTEqual(createExample(), change(createExample(), (taxonomy) -> taxonomy.setComprehensive(true)));
        testBothAreNOTEqual(createExample(), change(createExample(), (taxonomy) -> taxonomy.setMinimumRequiredLocalizedDataSemanticVersion("2.0")));
        testBothAreNOTEqual(createExample(), change(createExample(), (taxonomy) -> taxonomy.setProperties(null)));
        /* @formatter:on */

	}

	private Taxonomy createExample() {
		Taxonomy taxonomy = new Taxonomy();

		taxonomy.setGuid("d84e9e96-c7c5-11ec-be2f-9ff76f29cb3b");
		taxonomy.setName("name");
		taxonomy.setFullName("full-name");
		taxonomy.setProduct("product");
		taxonomy.setProductSuite("product-suite");
		taxonomy.setSemanticVersion("1.1.2-beta.12");
		taxonomy.setVersion("1.1.2");
		taxonomy.setReleaseDateUtc("2016-02-08T16:08:25.943Z");
		taxonomy.setDownloadUri("https://www.myUri.com/download/tool-1.1.2-beta.12.zip");
		taxonomy.setInformationUri("https://www.myUri.com/documentation.html");
		taxonomy.setOrganization("organization");
		taxonomy.setShortDescription(new Message());
		taxonomy.setFullDescription(new Message());
		taxonomy.setLanguage("en");
		taxonomy.setRules(new LinkedList<Rule>());
		taxonomy.setTaxa(new LinkedList<Taxon>());
		taxonomy.setComprehensive(false);
		taxonomy.setMinimumRequiredLocalizedDataSemanticVersion("1.1");
		taxonomy.setProperties(new PropertyBag());

		return taxonomy;
	}

}
