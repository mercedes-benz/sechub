package com.daimler.sechub.sarif.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
class RuleTest {

    @Test
    void values_are_null() {
        /* prepare */
        Rule rule = new Rule();

        /* execute */
        String id = rule.getId();
        String name = rule.getName();
        Message shortDescription = rule.getShortDescription();
        Message fullDescription = rule.getFullDescription();
        Message help = rule.getHelp();
        Properties properties = rule.getProperties();

        /* test */
        assertEquals(id, null);
        assertEquals(name, null);
        assertEquals(shortDescription, null);
        assertEquals(fullDescription, null);
        assertEquals(help, null);
        assertEquals(properties, null);
    }

    @Test
    void values_are_not_null() {
        /* prepare */
        Rule rule = new Rule("123", "rule-name", new Message(), new Message(), new Message(), new Properties());

        /* execute */
        String id = rule.getId();
        String name = rule.getName();
        Message shortDescription = rule.getShortDescription();
        Message fullDescription = rule.getFullDescription();
        Message help = rule.getHelp();
        Properties properties = rule.getProperties();

        /* test */
        assertEquals(id, "123");
        assertEquals(name, "rule-name");
        assertEquals(shortDescription, new Message());
        assertEquals(fullDescription, new Message());
        assertEquals(help, new Message());
        assertEquals(properties, new Properties());
    }

    @Test
    void test_setters() {
        /* prepare */
        Rule rule = new Rule("123", "rule-name", new Message(), new Message(), new Message(), new Properties());

        /* execute */
        rule.setId("123");
        rule.setName("rule-name");
        rule.setShortDescription(new Message());
        rule.setFullDescription(new Message());
        rule.setHelp(new Message());
        rule.setProperties(new Properties());

        /* test */
        assertEquals(rule.getId(), "123");
        assertEquals(rule.getName(), "rule-name");
        assertEquals(rule.getShortDescription(), new Message());
        assertEquals(rule.getFullDescription(), new Message());
        assertEquals(rule.getHelp(), new Message());
        assertEquals(rule.getProperties(), new Properties());
    }

}
