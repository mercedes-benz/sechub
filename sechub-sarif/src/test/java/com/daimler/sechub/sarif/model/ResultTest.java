package com.daimler.sechub.sarif.model;

import static org.junit.Assert.*;

import java.util.LinkedList;
import java.util.List;

import org.junit.Test;

public class ResultTest {

    @Test
    public void values_are_null() {
        /* prepare */
        Result result = new Result(null, null);

        /* execute */
        String ruleId = result.getRuleId();
        Message message = result.getMessage();

        /* test */
        assertEquals(ruleId, null);
        assertEquals(message, null);
    }

    @Test
    public void values_are_not_null() {
        /* prepare */
        Result result = new Result("123abc", new Message());

        /* execute */
        String ruleId = result.getRuleId();
        Message message = result.getMessage();

        /* test */
        assertEquals(ruleId, "123abc");
        assertEquals(message, new Message());
    }

    @Test
    public void test_setters() {
        /* prepare */
        Result result = new Result();

        /* execute */
        result.setRuleId("123abc");
        result.setMessage(new Message());
        result.setLocations(new LinkedList<Location>());
        result.setProperties(new PropertyBag());

        /* test */
        assertEquals(result.getRuleId(), "123abc");
        assertEquals(result.getMessage(), new Message());
        assertEquals(result.getLocations(), new LinkedList<Location>());
        assertEquals(result.getProperties(), new PropertyBag());
    }

    @Test
    public void test_add_null_as_addtionalProperties() {
        /* prepare */
        Result result = new Result();

        /* execute */
        result.addAdditionalProperty(null, null);
        result.addAdditionalProperty("key", null);
        result.addAdditionalProperty(null, "value");
        PropertyBag properties = result.getProperties();

        /* test */
        assertTrue(properties.getAdditionalProperties().isEmpty());
    }

    @Test
    public void test_addAdditionalProperties_method() {
        /* prepare */
        Result result = new Result();

        /* execute */
        result.addAdditionalProperty("first", "this");
        result.addAdditionalProperty("second", "that");
        PropertyBag properties = result.getProperties();

        /* test */
        assertEquals(properties.getAdditionalProperties().size(), 2);
    }

    @Test
    public void test_add_null_as_location() {
        /* prepare */
        Result result = new Result();

        /* execute */
        result.addLocation(null);
        List<Location> locations = result.getLocations();

        /* test */
        assertTrue(locations.isEmpty());
    }

    @Test
    public void test_addLocation_method() {
        /* prepare */
        Result result = new Result();

        /* execute */
        result.addLocation(buildLocation());
        List<Location> locations = result.getLocations();

        /* test */
        assertEquals(locations.size(), 1);
    }

    private Location buildLocation() {
        Location location = new Location();
        ArtifactLocation artifactLocation = new ArtifactLocation("file:///home/user/test/directory",
                "path/to/fileWithFinding.txt");
        PhysicalLocation physicalLocation = new PhysicalLocation();
        physicalLocation.setArtifactLocation(artifactLocation);
        location.setPhysicalLocation(physicalLocation);

        return location;
    }

}
