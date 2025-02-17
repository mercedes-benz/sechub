// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.plugin.model;

import com.mercedesbenz.sechub.commons.model.JSONConverter;
import com.mercedesbenz.sechub.commons.model.SecHubFinding;
import com.mercedesbenz.sechub.commons.model.SecHubReportModel;
import com.mercedesbenz.sechub.commons.model.SecHubResult;
import com.mercedesbenz.sechub.plugin.TestFileReader;
import org.junit.Before;
import org.junit.Test;

import java.util.Iterator;
import java.util.List;

import static org.junit.Assert.*;

public class SecHubFindingoWebScanDataProviderTest {

    private SecHubFindingoWebScanDataProvider providerToTest;

    @Before
    public void before(){
        providerToTest = new SecHubFindingoWebScanDataProvider();
    }

    @Test
    public void finding_null_getWebRequestDescription(){
        /* execute */
        String text = providerToTest.getWebRequestDescription(null);

        /* test */
        assertNotNull(text);
        assertEquals("No SecHub finding available",text);
    }

    @Test
    public void finding_not_null_but_no_web_report_data_getWebRequestDescription(){
        /* prepare */
        SecHubFinding sechubFinding = new SecHubFinding();

        /* execute */
        String text = providerToTest.getWebRequestDescription(sechubFinding);

        /* test */
        assertNotNull(text);
        assertEquals("No SecHub web report data available",text);
    }
    @Test
    public void finding_web_data_getWebRequestDescription(){
        /* prepare */
        String json = TestFileReader.readTextFile("./src/test/resources/report/report_pds-solution_zap_mocked.json");
        SecHubReportModel model = JSONConverter.get().fromJSON(SecHubReportModel.class, json);

        SecHubResult result = model.getResult();
        List<SecHubFinding> findings = result.getFindings();
        // check precondition
        assertTrue(findings.size()>0);

        Iterator<SecHubFinding> it = findings.iterator();
        SecHubFinding firstFinding = it.next();


        /* execute */
        String text = providerToTest.getWebRequestDescription(firstFinding);

        /* test */
        assertNotNull(text);
        System.out.println(text);
        String expected= """
                Method: GET
                URL: http://localhost:3000/rest/products/search?q=%27%28
                               
                Headers:
                ------------------------------------------------
                Accept=application/json, text/plain, */*
                Accept-Language=en-US,en;q=0.5
                Connection=keep-alive
                Host=localhost:3000
                Referer=http://localhost:3000/
                User-Agent=Mozilla/5.0 (X11; Linux x86_64; rv:108.0) Gecko/20100101 Firefox/108.0
                               
                               
                Body:
                ------------------------------------------------
                """;
        assertEquals(expected.trim(), text.trim());
    }
    @Test
    public void finding_web_data_getWebAttackDescription(){
        /* prepare */
        String json = TestFileReader.readTextFile("./src/test/resources/report/report_pds-solution_zap_mocked.json");
        SecHubReportModel model = JSONConverter.get().fromJSON(SecHubReportModel.class, json);

        SecHubResult result = model.getResult();
        List<SecHubFinding> findings = result.getFindings();
        // check precondition
        assertTrue(findings.size()>0);

        Iterator<SecHubFinding> it = findings.iterator();
        SecHubFinding firstFinding = it.next();


        /* execute */
        String text = providerToTest.getWebAttackDescription(firstFinding);

        /* test */
        assertNotNull(text);
        System.out.println(text);
        String expected= """
                Attack vector: '(
                                
                Body location: 3
                                
                Evidence:
                ------------------------------------------------
                SQLITE_ERROR""";
        assertEquals(expected.trim(), text.trim());
    }

    @Test
    public void finding_web_data_getWebResponseDescription(){
        /* prepare */
        String json = TestFileReader.readTextFile("./src/test/resources/report/report_pds-solution_zap_mocked.json");
        SecHubReportModel model = JSONConverter.get().fromJSON(SecHubReportModel.class, json);

        SecHubResult result = model.getResult();
        List<SecHubFinding> findings = result.getFindings();
        // check precondition
        assertTrue(findings.size()>0);

        Iterator<SecHubFinding> it = findings.iterator();
        SecHubFinding firstFinding = it.next();


        /* execute */
        String text = providerToTest.getWebResponseDescription(firstFinding);

        /* test */
        assertNotNull(text);
        System.out.println(text);
        String expected= """
                HTTP/1.1 500 Internal Server Error
                              
                Headers:
                ------------------------------------------------
                Access-Control-Allow-Origin=*
                Connection=keep-alive
                Content-Length=1102
                Content-Type=application/json; charset=utf-8
                Date=Wed, 14 Dec 2022 08:12:53 GMT
                Feature-Policy=payment 'self'
                Keep-Alive=timeout=5
                Vary=Accept-Encoding
                X-Content-Type-Options=nosniff
                X-Frame-Options=SAMEORIGIN
                              
                              
                Body:
                ------------------------------------------------
                Text:
                {
                  "error": {
                    "message": "SQLITE_ERROR: near \\"(\\": syntax error",
                    "stack": "SequelizeDatabaseError: SQLITE_ERROR: near \\"(\\": syntax error\\n    at Query.formatError (/juice-shop/node_modules/sequelize/lib/dialects/sqlite/query.js:403:16)\\n    at Query._handleQueryResponse (/juice-shop/node_modules/sequelize/lib/dialects/sqlite/query.js:72:18)\\n    at afterExecute (/juice-shop/node_modules/sequelize/lib/dialects/sqlite/query.js:238:27)\\n    at Statement.errBack (/juice-shop/node_modules/sqlite3/lib/sqlite3.js:14:21)",
                    "name": "SequelizeDatabaseError",
                    "parent": {
                      "errno": 1,
                      "code": "SQLITE_ERROR",
                      "sql": "SELECT * FROM Products WHERE ((name LIKE '%'(%' OR description LIKE '%'(%') AND deletedAt IS NULL) ORDER BY name"
                    },
                    "original": {
                      "errno": 1,
                      "code": "SQLITE_ERROR",
                      "sql": "SELECT * FROM Products WHERE ((name LIKE '%'(%' OR description LIKE '%'(%') AND deletedAt IS NULL) ORDER BY name"
                    },
                    "sql": "SELECT * FROM Products WHERE ((name LIKE '%'(%' OR description LIKE '%'(%') AND deletedAt IS NULL) ORDER BY name"
                  }
                }""";
        assertEquals(expected.trim(), text.trim());
    }
}
