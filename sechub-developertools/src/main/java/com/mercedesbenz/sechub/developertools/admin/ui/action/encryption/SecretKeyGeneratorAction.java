// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.developertools.admin.ui.action.encryption;

import java.awt.event.ActionEvent;
import java.util.Base64;
import java.util.Base64.Encoder;
import java.util.random.RandomGenerator;
import java.util.random.RandomGeneratorFactory;

import com.mercedesbenz.sechub.developertools.admin.ui.UIContext;
import com.mercedesbenz.sechub.developertools.admin.ui.action.AbstractUIAction;

public class SecretKeyGeneratorAction extends AbstractUIAction {
    private static final long serialVersionUID = 1L;

    private RandomGenerator randomGenerator;

    public SecretKeyGeneratorAction(UIContext context) {
        super("Secret key generator", context);
        randomGenerator = RandomGeneratorFactory.of("SecureRandom").create();
    }

    @Override
    public void execute(ActionEvent e) {

        byte[] random256Bit = new byte[32];
        byte[] random128Bit = new byte[16];

        randomGenerator.nextBytes(random256Bit);
        randomGenerator.nextBytes(random128Bit);

        Encoder encoder = Base64.getEncoder();
        String random256BitBase64encoded = encoder.encodeToString(random256Bit);
        String random128BitBase64encoded = encoder.encodeToString(random128Bit);

        String output = """
                Generated random keys
                (base64 encoded - ready to use as environment variables for encryption )

                256 bit: %s

                128 bit: %s


                """.formatted(random256BitBase64encoded, random128BitBase64encoded);

        outputAsTextOnSuccess(output);
    }

}
