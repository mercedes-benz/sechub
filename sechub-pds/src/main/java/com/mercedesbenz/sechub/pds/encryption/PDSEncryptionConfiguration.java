package com.mercedesbenz.sechub.pds.encryption;

import static com.mercedesbenz.sechub.pds.usecase.PDSDocumentationScopeConstants.*;

import java.util.List;

import javax.crypto.SealedObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.mercedesbenz.sechub.commons.core.environment.SecureEnvironmentVariableKeyValueRegistry;
import com.mercedesbenz.sechub.commons.core.security.CryptoAccess;
import com.mercedesbenz.sechub.pds.PDSMustBeDocumented;
import com.mercedesbenz.sechub.pds.commons.core.PDSProfiles;

import jakarta.annotation.PostConstruct;

@Component
public class PDSEncryptionConfiguration {

    private static final Logger LOG = LoggerFactory.getLogger(PDSEncryptionConfiguration.class);

    private static final String KEY_ROOT_PATH = "pds.encryption.";
    private static final String KEY_SECRET_KEY = KEY_ROOT_PATH + "secret-key";
    private static final String KEY_ALGORITHM = KEY_ROOT_PATH + "algorithm";
    private static final String ENV_ALGORITHM = KEY_ALGORITHM.toUpperCase().replace('.', '_');

    @PDSMustBeDocumented(value = "The secret key used for encryption. It must be base64 encoded, otherwise it is not accepted.", scope = SCOPE_ENCRYPTION, secret = true)
    @Value("${" + KEY_SECRET_KEY + ":}")
    String secretKeyAsString;

    @PDSMustBeDocumented(value = "The encryption type. Allowed values are: NONE, AES_GCM_SIV_128 or AES_GCM_SIV_256", scope = SCOPE_ENCRYPTION)
    @Value("${" + KEY_ALGORITHM + ":NONE}")
    String algorithmAsString;

    @Autowired
    private Environment springEnvironment;

    private PDSCipherAlgorithm algorithm;
    private SealedObject sealedSecretKey;

    @PostConstruct
    void init() throws PDSEncryptionException {
        boolean secretKeyWasEmpty = secretKeyAsString == null || secretKeyAsString.isBlank();
        sealedSecretKey = CryptoAccess.CRYPTO_STRING.seal(secretKeyAsString);
        secretKeyAsString = null; // reset

        try {
            algorithm = PDSCipherAlgorithm.valueOf(algorithmAsString.toUpperCase());
        } catch (RuntimeException e) {
            throw new PDSEncryptionException("Algorithm '" + algorithmAsString + "' not supported. Please set " + ENV_ALGORITHM
                    + " to one of the following values: " + List.of(PDSCipherAlgorithm.values()), e);
        }
        handleSecretKeyEmptyOrNot(secretKeyWasEmpty);
    }

    private void handleSecretKeyEmptyOrNot(boolean secretKeyWasEmpty) throws PDSEncryptionException {
        switch (algorithm) {
        case NONE:
            if (!secretKeyWasEmpty) {
                LOG.warn("You used a non empty secret key for cipher algorithm {}. Did you forget to change the algorithm type?", algorithm);
            }
            break;
        default:
            if (secretKeyWasEmpty) {
                throw new PDSEncryptionException("The cipher algorithm " + algorithm + " does not allow an empty secret key!");
            }
        }
    }

    public byte[] getSecretKeyBytes() {
        return CryptoAccess.CRYPTO_STRING.unseal(sealedSecretKey).getBytes();
    }

    public PDSCipherAlgorithm getAlgorithm() {
        return algorithm;
    }

    public void registerOnlyAllowedAsEnvironmentVariables(SecureEnvironmentVariableKeyValueRegistry registry) {
        if (springEnvironment != null && springEnvironment.matchesProfiles(PDSProfiles.INTEGRATIONTEST)) {
            /*
             * on integration test we accept credentials from configuration file or as
             * system properties - not marked as sensitive
             */
            return;
        }
        registry.register(registry.newEntry().key(KEY_ALGORITHM).notNullValue(algorithmAsString));
        registry.register(registry.newEntry().key(KEY_SECRET_KEY).nullableValue(CryptoAccess.CRYPTO_STRING.unseal(sealedSecretKey)));

    }

    /**
     * Creates a encryption configuration which can be used by AsciiDoc generator
     *
     * @param algorithm         algorithm to use for encryption
     * @param secretKeyAsString secret key as plain string
     * @return configuration, never <code>null</code>
     */
    public static PDSEncryptionConfiguration create(PDSCipherAlgorithm algorithm, String secretKeyAsString) {
        PDSEncryptionConfiguration config = new PDSEncryptionConfiguration();
        config.algorithmAsString = algorithm.name();
        config.secretKeyAsString = secretKeyAsString;
        return config;
    }

}
