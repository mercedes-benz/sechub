// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.assistant.ai.openai;

import java.net.URI;

import javax.crypto.SealedObject;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;

import com.mercedesbenz.sechub.commons.core.doc.Description;
import com.mercedesbenz.sechub.commons.core.doc.MustBeDocumented;
import com.mercedesbenz.sechub.commons.core.security.CryptoAccess;

@MustBeDocumented(scope = "Open AI properties")
@ConfigurationProperties(prefix = OpenAIProperties.PREFIX)
public class OpenAIProperties implements OpenAISetup {

    public static final String PREFIX = "sechub.assistant.ai.openai";

    private final CryptoAccess<String> cryptoAccess = CryptoAccess.CRYPTO_STRING;
    private final SealedObject sealedApiToken;
    private final boolean enabled;
    private final URI completionsUri;

    private final String model;

    /* @formatter:off */
    @ConstructorBinding
    public OpenAIProperties(
            @Description("API token")
            String apiToken,

            @Description("Model to use")
            String model,

            @Description("Completions URI for open AI. Examples: https://api.openai.com/v1/chat/completions or https://your-apigateway.example.com/connect/opeanai/chat-gpt40o/chat/completions?version=2024-02-01")
            URI completionsUri) {

        /* @formatter:on */
        this.sealedApiToken = cryptoAccess.seal(apiToken);
        this.model = model;
        this.completionsUri = completionsUri;
        this.enabled = completionsUri != null && apiToken != null && !apiToken.isBlank();
    }

    @Override
    public URI getCompletionsUri() {
        return completionsUri;
    }

    @Override
    public String getApiToken() {
        return cryptoAccess.unseal(sealedApiToken);
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public String getModel() {
        return model;
    }

}
