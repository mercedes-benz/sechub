// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.core.environment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.crypto.SealedObject;

import com.mercedesbenz.sechub.commons.core.security.CryptoAccess;

/**
 * A registry which can contain entries where information about a key and a
 * corresponding environment variable are hold. The value is stored encrypted.
 *
 * The information from this registry can be used later e.g. to check if the key
 * was injected as environment variable or not.
 *
 * @author Albert Tregnaghi
 *
 */
public class SecureEnvironmentVariableKeyValueRegistry {

    private static final CryptoAccess<String> CRYPTO_ACCESS = CryptoAccess.CRYPTO_STRING;

    private List<EnvironmentVariableKeyValueEntry> entries = new ArrayList<>();

    public void register(EnvironmentVariableKeyValueEntryBuilder entry) {
        entries.add(entry.build());
    }

    public EnvironmentVariableKeyValueEntryBuilder newEntry() {
        return new EnvironmentVariableKeyValueEntryBuilder();
    }

    public List<EnvironmentVariableKeyValueEntry> getEntries() {
        return Collections.unmodifiableList(entries);
    }

    public static class EnvironmentVariableKeyValueEntryBuilder {
        private String key;
        private String variableName;
        private SealedObject sealedObjectForNotNullValue;
        private SealedObject sealedObjectForNullablelValue;

        private EnvironmentVariableKeyValueEntryBuilder() {

        }

        public EnvironmentVariableKeyValueEntryBuilder key(String key) {
            this.key = key;
            return this;
        }

        public EnvironmentVariableKeyValueEntryBuilder notNullValue(String value) {
            this.sealedObjectForNotNullValue = CRYPTO_ACCESS.seal(value);
            return this;
        }

        public EnvironmentVariableKeyValueEntryBuilder nullableValue(String value) {
            this.sealedObjectForNullablelValue = CRYPTO_ACCESS.seal(value);
            return this;
        }

        public EnvironmentVariableKeyValueEntryBuilder variable(String variableName) {
            this.variableName = variableName;
            return this;
        }

        /**
         * Creates the configured entry
         *
         * @throws IllegalArgumentException if key is empty or blank, key contains
         *                                  spaces or value is not allowed to be null,
         *                                  but is null null
         * @return entry
         */
        public EnvironmentVariableKeyValueEntry build() {
            if (key == null || key.isBlank()) {
                throw new IllegalArgumentException("key not defined");
            }
            if (key.contains(" ")) {
                throw new IllegalArgumentException("spaces not allowed for key!");
            }

            if (sealedObjectForNullablelValue == null && sealedObjectForNotNullValue == null) {
                throw new IllegalArgumentException("No value defined at all, you have to use either nullableValue() or notNullValue() methods to setup!");
            }
            if (sealedObjectForNullablelValue != null && sealedObjectForNotNullValue != null) {
                throw new IllegalArgumentException(
                        "Two ways of value definition used. Please use either nullableValue() or notNullValue() method, but not both!");
            }

            SealedObject sealedObjectToUse = null;

            if (sealedObjectForNotNullValue != null) {
                boolean notDefined = CRYPTO_ACCESS.unseal(sealedObjectForNotNullValue) == null;
                if (notDefined) {
                    throw new IllegalArgumentException("Usage failure: the sealed object for a 'not null value' was null!");
                }
                sealedObjectToUse = sealedObjectForNotNullValue;
            }

            if (sealedObjectForNullablelValue != null) {
                sealedObjectToUse = sealedObjectForNullablelValue;
            }

            EnvironmentVariableKeyValueEntry entry = new EnvironmentVariableKeyValueEntry();
            entry.key = key;
            entry.sealedObjectToUse = sealedObjectToUse;
            entry.variableName = variableName;

            if (entry.variableName == null) {
                entry.variableName = key.toUpperCase().replace('.', '_').replace('-', '_');
            }
            return entry;
        }

    }

    public static class EnvironmentVariableKeyValueEntry {
        private String key;
        private String variableName;
        private SealedObject sealedObjectToUse;

        private EnvironmentVariableKeyValueEntry() {
        }

        public String getKey() {
            return key;
        }

        public String getVariableName() {
            return variableName;
        }

        public String getValue() {
            if (sealedObjectToUse == null) {
                return null;
            }
            return CRYPTO_ACCESS.unseal(sealedObjectToUse);
        }
    }
}
