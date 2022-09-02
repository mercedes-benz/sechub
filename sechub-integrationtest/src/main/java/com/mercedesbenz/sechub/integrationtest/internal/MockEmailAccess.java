// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.integrationtest.internal;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.mercedesbenz.sechub.integrationtest.api.MockEmailEntry;
import com.mercedesbenz.sechub.integrationtest.api.TestUser;
import com.mercedesbenz.sechub.integrationtest.api.TextSearchMode;
import com.mercedesbenz.sechub.test.SecHubTestURLBuilder;

public class MockEmailAccess {

    public static final int DEFAULT_TIMEOUT = 3;

    static MockEmailAccess mailAccess() {
        return new MockEmailAccess();
    }

    public List<Map<String, Object>> getMailsFor(TestUser user) {
        return getMailsFor(user.getEmail());
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public List<Map<String, Object>> getMailsFor(String mailAddress) {
        String url = getUrlBuilder().buildFetchEmailsFromMockMailServiceUrl(mailAddress);
        ResponseEntity<List> result = getTemplate().getForEntity(url, List.class);
        return result.getBody();
    }

    public List<MockEmailEntry> getMockMailListFor(String mailAddress) {
        return convertToMockMailList(getMailsFor(mailAddress));
    }

    private RestTemplate getTemplate() {
        return getContext().getTemplateForSuperAdmin().getTemplate();
    }

    private SecHubTestURLBuilder getUrlBuilder() {
        return getContext().getUrlBuilder();
    }

    private IntegrationTestContext getContext() {
        return IntegrationTestContext.get();
    }

    public MockEmailEntry findMailOrFail(TestUser toUser, String subject) {
        return findMailOrFail(toUser, subject, DEFAULT_TIMEOUT);
    }

    public MockEmailEntry findMailOrFail(String email, String subject) {
        return findMailOrFail(email, subject, DEFAULT_TIMEOUT);
    }

    public MockEmailEntry findMailOrFail(TestUser toUser, String subject, int maxSecondsToWait) {
        return findMailOrFail(toUser.getEmail(), subject, maxSecondsToWait);
    }

    public MockEmailEntry findMailOrFail(String email, String subject, int maxSecondsToWait) {
        return findMailOrFail(email, subject, TextSearchMode.EXACT, maxSecondsToWait);
    }

    public MockEmailEntry findMailOrFail(String email, String subjectSearch, TextSearchMode subjectSearchMode, int maxSecondsToWait) {
        MockEmailEntry found = null;
        List<MockEmailEntry> list = null;
        for (int i = 0; i < maxSecondsToWait; i++) {
            List<Map<String, Object>> listAsMap = getMailsFor(email);
            list = convertToMockMailList(listAsMap);

            for (MockEmailEntry message : list) {
                if (subjectSearch == null || subjectSearch.isEmpty()) {
                    /* Always found when no subject given */
                    found = message;
                    break;
                }
                if (message.subject == null) {
                    /* cannot be checked */
                    continue;
                }
                switch (subjectSearchMode) {
                case EXACT:
                    if (message.subject.equals(subjectSearch)) {
                        found = message;
                    }
                    break;
                case CONTAINS:
                    if (message.subject.indexOf(subjectSearch) != -1) {
                        found = message;
                    }
                    break;
                case REGULAR_EXPRESSON:
                    if (message.subject.matches(subjectSearch)) {
                        found = message;
                    }
                    break;
                default:
                    break;
                }
            }
            if (found != null) {
                break;
            }
            try {
                /* NOSONAR */Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if (found == null) {
            StringBuilder sb = new StringBuilder();
            sb.append("Did not find a mail containing:\n-emailaddress (TO/CC/BCC): ");
            sb.append(email);
            sb.append("\n-subject");
            switch (subjectSearchMode) {
            case CONTAINS:
                sb.append("(contains)");
                break;
            case EXACT:
                sb.append("(exact)");
                break;
            case REGULAR_EXPRESSON:
                sb.append("(regexp)");
                break;
            default:
                break;
            }
            sb.append(":'");
            sb.append(subjectSearch);
            sb.append("'\n\nFound mails for this email address:");
            for (MockEmailEntry message : list) {
                sb.append("\n").append(message.toString());
            }
            fail(sb.toString());
        }
        return found;
    }

    public static final List<MockEmailEntry> convertToMockMailList(List<Map<String, Object>> listAsMap) {
        List<MockEmailEntry> list;
        list = new ArrayList<>();
        for (Map<String, Object> map : listAsMap) {
            MockEmailEntry entry = new MockEmailEntry();
            entry.from = map.get("from").toString();
            entry.subject = map.get("subject").toString();
            entry.text = map.get("text").toString();
            Object to = map.get("to");
            if (to != null) {
                entry.to = to.toString();
            }
            Object cc = map.get("cc");
            if (cc != null) {
                entry.cc = cc.toString();
            }
            Object bcc = map.get("bcc");
            if (bcc != null) {
                entry.bcc = bcc.toString();
            }
            list.add(entry);
        }
        return list;
    }

    public void reset() {
        getTemplate().delete(getUrlBuilder().buildResetAllMockMailsUrl());
    }

}
