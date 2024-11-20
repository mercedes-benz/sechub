// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sharedkernel.validation;

import static com.mercedesbenz.sechub.sharedkernel.validation.AssertValidation.*;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.mercedesbenz.sechub.sharedkernel.error.NotAcceptableException;

/**
 * This component is convenient point to handle assertions on simple user input
 * data. For complex data we got dedicated spring validators - and a
 * corresponding integration test case to check that validation really works
 * (You can easly have a situation where you add @Validated but no validator is
 * used). <br>
 * <br>
 * But some simple strings cannot be validated this way - for this we simply use
 * this assertion component which contains simple assert methods.
 *
 * @author Albert Tregnaghi
 *
 */
@Component
public class UserInputAssertion {

    @Autowired
    UserIdValidation userIdValidation;

    @Autowired
    ProjectIdValidation projectIdValidation;

    @Autowired
    JobUUIDValidation jobUUIDValidation;

    @Autowired
    OneTimeTokenValidation oneTimeTokenValidation;

    @Autowired
    ProjectDescriptionValidation projectDescriptionValidation;

    @Autowired
    EmailValidation emailValidation;

    @Autowired
    Sha256ChecksumValidation sha256CheckSumValidation;

    @Autowired
    TemplateIdValidation templateIdValidation;

    @Autowired
    AssetIdValidation assetIdValidation;

    @Autowired
    AssetFileNameValidation assetFilenameValidation;

    /**
     * Asserts this is a valid project id. If not, a {@link NotAcceptableException}
     * will be thrown
     *
     * @param projectId
     */
    public void assertIsValidProjectId(String projectId) {
        assertValid(projectId, projectIdValidation, "Project ID is not valid");
    }

    public void assertIsValidProjectDescription(String description) {
        assertValid(description, projectDescriptionValidation, "Project description is not valid");
    }

    /**
     * Asserts this is a valid job uuid. If not, a {@link NotAcceptableException}
     * will be thrown
     *
     * @param jobUUID
     */
    public void assertIsValidJobUUID(UUID jobUUID) {
        assertValid(jobUUID, jobUUIDValidation, "Job UUID is not valid");
    }

    /**
     * Asserts this is a valid one time token. If not, a
     * {@link NotAcceptableException} will be thrown
     *
     * @param oneTimeToken
     */
    public void assertIsValidOneTimeToken(String oneTimeToken) {
        assertValid(oneTimeToken, oneTimeTokenValidation, "Not a valid one time token");
    }

    /**
     * Asserts this is a valid user id. If not, a {@link NotAcceptableException}
     * will be thrown
     *
     * @param userId
     */
    public void assertIsValidUserId(String userId) {
        assertValid(userId, userIdValidation, "User is not valid");
    }

    /**
     * Asserts this is a valid email address. If not, a
     * {@link NotAcceptableException} will be thrown
     *
     * @param userId
     */
    public void assertIsValidEmailAddress(String emailAddress) {
        assertValid(emailAddress, emailValidation, "Email address is not valid");
    }

    /**
     * Asserts this is a valid sha256 checksum. If not, a
     * {@link NotAcceptableException} will be thrown
     *
     * @param sha256
     */
    public void assertIsValidSha256Checksum(String sha256) {
        assertValid(sha256, sha256CheckSumValidation, "Sha256 checksum is not valid");
    }

    /**
     * Asserts this is a valid template id. If not, a {@link NotAcceptableException}
     * will be thrown
     *
     * @param templateId
     */
    public void assertIsValidTemplateId(String templateId) {
        assertValid(templateId, templateIdValidation, "Template id is not valid");
    }

    /**
     * Asserts this is a valid asset id. If not, a {@link NotAcceptableException}
     * will be thrown
     *
     * @param assetId
     */
    public void assertIsValidAssetId(String assetId) {
        assertValid(assetId, assetIdValidation, "Asset id is not valid");
    }

    /**
     * Asserts this is an valid asset filename. If not, a
     * {@link NotAcceptableException} will be thrown
     *
     * @param fileName
     */
    public void assertIsValidAssetFileName(String fileName) {
        assertValid(fileName, assetFilenameValidation, "Asset filename is not valid");
    }

}
