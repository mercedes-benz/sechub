// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sharedkernel.usecases.encryption;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.mercedesbenz.sechub.sharedkernel.Step;
import com.mercedesbenz.sechub.sharedkernel.usecases.UseCaseDefinition;
import com.mercedesbenz.sechub.sharedkernel.usecases.UseCaseGroup;
import com.mercedesbenz.sechub.sharedkernel.usecases.UseCaseIdentifier;

/* @formatter:off */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@UseCaseDefinition(
		id=UseCaseIdentifier.UC_ENCRYPTION_CLEANUP,
		group=UseCaseGroup.ENCRYPTION,
		apiName="systemStartsEncryptionCleanup",
		title="SecHub does cleanup encryption",
		description="""
		        Secub does an ecnryption cleanup.

		        Inside relevant domains the encryption situation will be checked and
		        old encryption setup, which is no longer necessary, will be dropped.

		        For example: When encryption was done with formerly via ENV variable
		        `SECRET_1_AES_256` and the new one setup is using `SECRET_2_AES_256` and
		        all jobs have been migrated to the new encryption, the cipher setup
		        using `SECRET_1_AES_256` will become obsolete and will be automatically
		        removed. After the remove is done, there is no longer a need to
		        start the server with `SECRET_1_AES_256`, but only with `SECRET_2_AES_256` ...

		        """)
public @interface UseCaseEncryptionCleanup{

	Step value();
}
/* @formatter:on */
