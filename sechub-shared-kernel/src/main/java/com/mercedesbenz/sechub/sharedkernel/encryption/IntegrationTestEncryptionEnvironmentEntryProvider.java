package com.mercedesbenz.sechub.sharedkernel.encryption;

import java.nio.charset.Charset;
import java.util.Base64;
import java.util.Map;
import java.util.TreeMap;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.mercedesbenz.sechub.sharedkernel.Profiles;

@Component
@Profile(Profiles.INTEGRATIONTEST)
public class IntegrationTestEncryptionEnvironmentEntryProvider implements EncryptionEnvironmentEntryProvider {

    private Map<String, String> encryptionEntryMap = new TreeMap<>();

    public IntegrationTestEncryptionEnvironmentEntryProvider() {
        addPasswordAs64ForEnvironmentVariable("INTEGRATION_TEST_SECRET_1_AES_256", "123456789012345678901234567890AX");
    }

    private void addPasswordAs64ForEnvironmentVariable(String environmentVariableName, String plainTextPassword) {
        String base64Value = Base64.getEncoder().encodeToString(plainTextPassword.getBytes(Charset.forName("UTF-8")));
        encryptionEntryMap.put(environmentVariableName, base64Value);
    }

    @Override
    public String getBase64EncodedEnvironmentEntry(String environmentVariableName) {
        String result = encryptionEntryMap.get(environmentVariableName);
        if (result == null) {
            throw new IllegalStateException("Integration test setup has no entry for variable:" + environmentVariableName);
        }
        return result;
    }

}
