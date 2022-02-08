package com.daimler.sechub.adapter.support;

import static org.junit.jupiter.api.Assertions.*;

import java.net.URI;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.LinkedList;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class URIShrinkSupportTest {
    private URIShrinkSupport uriShrinkSupport;
    
    @BeforeEach
    public void before() throws Exception {
        uriShrinkSupport = new URIShrinkSupport();
    }
    
    @Test
    public void shrink_to_root_uri__null() {
    	/* prepare */
        URI uriToShrink = null;

        /* execute + test */
        assertNull(uriShrinkSupport.shrinkToRootURI(uriToShrink));
    }
    
    @Test
    public void shrink_to_root_uri__already_root_uri() {
    	/* prepare */
        URI uriToShrink = URI.create("https://example.org");
        URI expectedRootURI = URI.create("https://example.org");

        /* execute */
        URI actualRootURI = uriShrinkSupport.shrinkToRootURI(uriToShrink);
        
        /* test */
        assertEquals(expectedRootURI, actualRootURI);
    }
    
    @Test
    public void shrink_to_root_uri__query_parameter() {
    	/* prepare */
        URI uriToShrink = URI.create("https://example.org/test?test=1");
        URI expectedRootURI = URI.create("https://example.org");

        /* execute */
        URI actualRootURI = uriShrinkSupport.shrinkToRootURI(uriToShrink);
        
        /* test */
        assertEquals(expectedRootURI, actualRootURI);
    }
    
    @Test
    public void shrink_to_root_uri__fragment() {
        /* prepare */
    	URI uriToShrink = URI.create("https://example.org/test#abc");
        URI expectedRootURI = URI.create("https://example.org");

        /* execute */
        URI actualRootURI = uriShrinkSupport.shrinkToRootURI(uriToShrink);
        
        /* test */
        assertEquals(expectedRootURI, actualRootURI);
    }
    
    @Test
    public void shrink_to_root_uris__empty() {
        /* prepare */
    	Collection<URI> uris = new LinkedList<>();
        
    	/* execute */
        Collection<URI> rootURIs =  uriShrinkSupport.shrinkToRootURIs(uris);
        
        /* test */
        assertTrue(rootURIs.isEmpty());
    }
        
    @Test
    public void shrink_to_root_uris__mixed_uris() {
    	/* prepare */
        URI exampleOrg = URI.create("https://example.org");
        URI exampleCom = URI.create("https://example.com");
        
        Collection<URI> expectedRootURIs = new LinkedHashSet<>();
        expectedRootURIs.add(exampleOrg);
        expectedRootURIs.add(exampleCom);
        
        Collection<URI> uris = new LinkedList<>();
        uris.add(URI.create("https://example.org/test#abc"));
        uris.add(URI.create("https://example.org/test?test=1"));
        uris.add(null);
        uris.add(URI.create("https://example.com"));
        
        /* execute */
        Collection<URI> actualRootURIs =  uriShrinkSupport.shrinkToRootURIs(uris);
        
        /* test */
        assertEquals(expectedRootURIs.size(), actualRootURIs.size());
        assertEquals(expectedRootURIs, actualRootURIs);
    }
}
