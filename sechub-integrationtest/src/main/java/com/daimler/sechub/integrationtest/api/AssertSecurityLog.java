// SPDX-License-Identifier: MIT
package com.daimler.sechub.integrationtest.api;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import com.daimler.sechub.sharedkernel.logging.SecurityLogData;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class AssertSecurityLog {

    private List<SecurityLogData> logs;

    public static AssertSecurityLog assertSecurityLog() {
        return new AssertSecurityLog();
    }

    private AssertSecurityLog() {
        logs = TestAPI.getSecurityLogs();
    }

    public AssertSecurityLog hasEntries(int expectedAmount) {
        int amount = logs.size();
        if (expectedAmount != amount) {

            String json = null;
            ObjectMapper mapper = new ObjectMapper();
            try {
                json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(logs);
            } catch (JsonProcessingException e) {
                throw new IllegalStateException(e);
            }

            assertEquals("Security log does not contain expected amount of log entries:" + expectedAmount + " but:" + amount
                    + ". Did you clear the log at test start correctly by TestAPI.clearSecurityLogs() ?\n" + json, expectedAmount, amount);
        }
        return this;
    }

    /**
     * Assert given entry exists
     * 
     * @param pos must be greater or equal zero
     * @return
     */
    public AssertSecurityLogDataEntry entry(int pos) {
        if (pos < 0) {
            throw new IllegalArgumentException("pos may not be lower than zero!");
        }

        if (pos >= logs.size()) {
            fail("cannot fetch pos " + pos + " because log does only contain " + logs.size() + " entries");
        }
        return new AssertSecurityLogDataEntry(logs.get(pos));
    }

    public class AssertSecurityLogDataEntry {

        private SecurityLogData data;

        public AssertSecurityLogDataEntry(SecurityLogData securityLogData) {
            Objects.requireNonNull(securityLogData);
            data = securityLogData;
        }
        
        public AssertSecurityLogDataEntry hasOneOfGivenClientIps(String ...expectedClientIps) {
            List<String> clientIPs = Arrays.asList(expectedClientIps);
            String clientIp = data.getClientIp();
            if (! clientIPs.contains(clientIp)){
                fail("Data did contain clientIp:"+clientIp+". It is not one of expected client IPs:"+clientIPs);
            }
            return this;
        }

        public AssertSecurityLogDataEntry hasRequestURI(String uri) {
            assertEquals("URI not as expected", uri, data.getRequestURI());
            return this;
        }

        public AssertSecurityLog and() {
            return AssertSecurityLog.this;
        }

        public AssertSecurityLogDataEntry hasMessageContaining(String... expectedParts) {
            String message = data.getMessage();
            if (message == null) {
                fail("Message was null!");
            }
            for (String expectedPart : expectedParts) {
                if (!message.contains(expectedPart)) {
                    fail("Expected part '" + expectedPart + "' was not found in message '" + message + "'");
                }
            }
            return this;
        }

        public AssertSecurityLogDataEntry hasMessageParameters(int amount) {
            List<Object> parameters = data.getMessageParameters();
            if (parameters == null) {
                throw new IllegalArgumentException();
            }
            assertEquals(amount, parameters.size());
            return this;
        }

        public AssertSecurityLogDataEntry hasMessageParameterContainingStrings(int pos, String... expectedParts) {
            Objects.nonNull(expectedParts);
            if (pos < 0) {
                throw new IllegalArgumentException("pos must be >=0!");
            }
            List<Object> parameters = data.getMessageParameters();
            if (parameters == null) {
                throw new IllegalStateException("no parameters found");
            }
            if (parameters.size() <= pos) {
                throw new IllegalArgumentException("wrong pos:" + pos + ", have only " + parameters.size() + " parameters");
            }

            Object parameter = parameters.get(pos);
            assertNotNull("parameter at pos:" + pos + " is null!");
            String parameterString = parameter.toString();

            for (String expectedPart : expectedParts) {
                if (!parameterString.contains(expectedPart)) {
                    fail("Expected part '" + expectedPart + "' was not found in parameter " + pos + " which is '" + parameterString + "'");
                }
            }
            return this;
        }

        public AssertSecurityLogDataEntry hasHTTPHeader(String headerName, String expectedValue) {
            String headerValue = data.getHttpHeaders().get(headerName);
            if (headerValue == null) {
                try {
                    String headerJSON = new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(data.getHttpHeaders());
                    fail("Header with name:" + headerName + " was not found inside headers:\n" + headerJSON);
                } catch (JsonProcessingException e) {
                    throw new IllegalStateException(e);
                }
            }
            assertEquals("header value not as expected for " + headerName, headerValue, expectedValue);
            return this;
        }

    }

}
