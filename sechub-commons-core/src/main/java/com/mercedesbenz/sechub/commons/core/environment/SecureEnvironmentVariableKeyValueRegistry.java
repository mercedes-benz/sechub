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
        private SealedObject value;

        private EnvironmentVariableKeyValueEntryBuilder() {

        }

        public EnvironmentVariableKeyValueEntryBuilder key(String key) {
            this.key = key;
            return this;
        }

        public EnvironmentVariableKeyValueEntryBuilder value(String value) {
            this.value = CryptoAccess.CRYPTO_STRING.seal(value);
            return this;
        }

        public EnvironmentVariableKeyValueEntryBuilder variable(String variableName) {
            this.variableName = variableName;
            return this;
        }

        public EnvironmentVariableKeyValueEntry build() {
            if (key == null || key.isBlank()) {
                throw new IllegalArgumentException("key not defined");
            }
            if (key.contains(" ")) {
                throw new IllegalArgumentException("spaces not allowed for key!");
            }
            EnvironmentVariableKeyValueEntry entry = new EnvironmentVariableKeyValueEntry();
            entry.key = key;
            entry.value = value;
            entry.variableName = variableName;

            if (entry.variableName == null) {
                entry.variableName = key.toUpperCase().replace('.', '_');
            }
            return entry;
        }

    }

    public static class EnvironmentVariableKeyValueEntry {
        private String key;
        private String variableName;
        private SealedObject value;

        private EnvironmentVariableKeyValueEntry() {

        }

        public String getKey() {
            return key;
        }

        public String getVariableName() {
            return variableName;
        }

        public String getValue() {
            if (value == null) {
                return null;
            }
            return CryptoAccess.CRYPTO_STRING.unseal(value);
        }
    }
}
