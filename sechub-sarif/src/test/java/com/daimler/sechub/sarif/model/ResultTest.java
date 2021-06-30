// SPDX-License-Identifier: MIT
package com.daimler.sechub.sarif.model;

import static com.daimler.sechub.test.PojoTester.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;

class ResultTest {

    @Test
    void constructor_params_null() {
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
    void custructor_params_defined() {
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
    void setters() {
        /* prepare */
        Result result = new Result();

        /* execute */
        testSetterAndGetter(result);
    }

    @Test
    void setter() {
        testSetterAndGetter(createExample());
    }

    @Test
    void equals_and_hashcode() {

        /* @formatter:off */
        testBothAreEqualAndHaveSameHashCode(createExample(), createExample());
        testBothAreNOTEqual(createExample(), change(createExample(), (result) -> result.setLevel(Level.ERROR)));
        testBothAreNOTEqual(createExample(), change(createExample(), (result) -> result.setMessage(new Message("other"))));
        testBothAreNOTEqual(createExample(), change(createExample(), (result) -> result.setProperties(change(new PropertyBag(), (bag) -> bag.put("key","value")))));
        testBothAreNOTEqual(createExample(), change(createExample(), (result) -> result.setRuleId("other")));
        testBothAreNOTEqual(createExample(), change(createExample(), (result) -> result.setRuleIndex(42)));
        testBothAreNOTEqual(createExample(), change(createExample(), (result) -> result.setLocations(Collections.singletonList(createLocation()))));
        /* @formatter:on */

    }

    private Result createExample() {
        return new Result();
    }

    @Test
    void add_null_as_addtionalProperties() {
        /* prepare */
        Result result = new Result();

        /* execute */
        PropertyBag properties = result.getProperties();
        properties.put(null, null);
        properties.put("key", null);
        properties.put(null, "value");

        /* test */
        assertTrue(properties.isEmpty());
    }

    @Test
    void property_bag_from_new_result_not_null() {
        /* prepare */
        Result result = new Result();

        /* execute */
        PropertyBag properties = result.getProperties();

        /* test */
        assertNotNull(properties);
    }

    @Test
    void add_null_as_location() {
        /* prepare */
        Result result = new Result();

        /* execute */
        result.addLocation(null);
        List<Location> locations = result.getLocations();

        /* test */
        assertTrue(locations.isEmpty());
    }

    @Test
    void addLocation_method() {
        /* prepare */
        Result result = new Result();

        /* execute */
        result.addLocation(createLocation());
        List<Location> locations = result.getLocations();

        /* test */
        assertEquals(locations.size(), 1);
    }

    private Location createLocation() {
        Location location = new Location();
        ArtifactLocation artifactLocation = new ArtifactLocation("file:///home/user/test/directory", "path/to/fileWithFinding.txt");
        PhysicalLocation physicalLocation = new PhysicalLocation();
        physicalLocation.setArtifactLocation(artifactLocation);
        location.setPhysicalLocation(physicalLocation);

        return location;
    }

}
