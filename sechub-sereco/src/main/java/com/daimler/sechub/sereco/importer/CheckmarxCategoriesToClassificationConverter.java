// SPDX-License-Identifier: MIT
package com.daimler.sechub.sereco.importer;

import org.springframework.util.StringUtils;

import com.daimler.sechub.sereco.metadata.SerecoClassification;

public class CheckmarxCategoriesToClassificationConverter {

    public SerecoClassification convert(String categories, SerecoClassification classification) {
        if (categories == null || categories.isEmpty()) {
            return classification;
        }

        // Examples:
        // OWASP Top 10 2013;A7-Missing Function Level Access Control,OWASP Top 10
        // 2017;A5-Broken Access Control
        // PCI DSS v3.2;PCI DSS (3.2) - 6.5.8 - Improper access control,OWASP Top 10
        // 2013;A4-Insecure Direct Object References,OWASP Top 10 2017;A5-Broken Access
        // Control
        String[] splitted = categories.split(",");
        for (String split : splitted) {
            inspect(split, classification);

        }
        return classification;
    }

    private void inspect(String split, SerecoClassification classification) {
        if (split == null || split.isEmpty()) {
            return;
        }
        /* e.g. "PCI DSS v3.2;PCI DSS (3.2) - 6.5.8 - Improper access control" */
        String[] keyValue = StringUtils.split(split, ";");
        if (keyValue == null || keyValue.length < 2) {
            return;
        }
        String key = keyValue[0].toUpperCase();
        String value = keyValue[1];
        if (key == null || key.isEmpty()) {
            return;
        }
        if (value == null || value.isEmpty()) {
            return;
        }
        handleOWASP(key, value, classification);
        handlePCI(key, value, classification);
        handleFISMA(key, value, classification);
        handleNIST(key, value, classification);
    }

    private void handleFISMA(String key, String value, SerecoClassification classification) {
        // "FISMA 2014;Identification And Authentication,NIST SP 800-53;AC-3 Access
        // Enforcement (P1)
        if (!key.contains("FISMA")) {
            return;
        }
        classification.setFisma(fetchFISMAIdentifier(value));
    }

    private String fetchFISMAIdentifier(String number) {
        if (number == null) {
            return "";
        }
        return number;
    }

    private void handleNIST(String key, String value, SerecoClassification classification) {
        // "FISMA 2014;Identification And Authentication,NIST SP 800-53;AC-3 Access
        // Enforcement (P1)
        if (!key.contains("NIST")) {
            return;
        }
        classification.setNist(fetchNISTIdentifier(value));
    }

    private String fetchNISTIdentifier(String number) {
        // "FISMA 2014;Identification And Authentication,NIST SP 800-53;AC-3 Access
        // Enforcement (P1)
        if (number == null) {
            return "";
        }
        int index = number.indexOf(' ');
        if (index < 0) {
            /* when only AC-3 */
            return number;
        }
        return number.substring(0, index);
    }

    private void handleOWASP(String key, String value, SerecoClassification classification) {
        /* e.g. OWASP Top 10 2013;A7-Missing Function Level Access Control */
        if (!key.contains("OWASP")) {
            return;
        }
        /*
         * if there are multiple values from owasp we just use the last one - checkmarx
         * does order them newest last...
         */
        classification.setOwasp(fetchOWASPIdentifier(value));
    }

    private String fetchOWASPIdentifier(String number) {
        if (number == null) {
            return "";
        }
        int index = number.indexOf('-');
        if (index < 0) {
            /* when only A5 */
            return number;
        }
        return number.substring(0, index);

    }

    private void handlePCI(String key, String value, SerecoClassification classification) {
        /* e.g. "PCI DSS v3.2;PCI DSS (3.2) - 6.5.8 - Improper access control" */
        if (!key.contains("PCI")) {
            return;
        }
        handlePCI31(key, value, classification);
        handlePCI32(key, value, classification);

    }

    private String fetchPCINumber(String number) {
        if (number == null) {
            return "";
        }
        int index = number.indexOf('-');
        if (index == -1) {
            return "";
        }
        int nextIndex = number.indexOf('-', index + 1);
        if (nextIndex == -1) {
            return "";
        }
        return number.substring(index + 1, nextIndex).trim();

    }

    private void handlePCI31(String key, String value, SerecoClassification classification) {
        if (key.indexOf("V3.1") == -1) {
            return;
        }
        classification.setPci31(fetchPCINumber(value));
    }

    private void handlePCI32(String key, String value, SerecoClassification classification) {
        if (key.indexOf("V3.2") == -1) {
            return;
        }
        classification.setPci32(fetchPCINumber(value));

    }

}
