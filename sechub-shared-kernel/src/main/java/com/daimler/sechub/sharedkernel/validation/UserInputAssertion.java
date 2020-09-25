// SPDX-License-Identifier: MIT
package com.daimler.sechub.sharedkernel.validation;

import static com.daimler.sechub.sharedkernel.validation.AssertValidation.*;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.daimler.sechub.sharedkernel.error.NotAcceptableException;

/**
 * This component is convenient point to handle assertions on simple user input data.
 * For complex data we got dedicated spring validators - and a corresponding integration test case to check that
 * validation really works (You can easly have a situation where you add @Validated but no validator is used).
 * <br><br>
 * But some simple strings cannot be validated this way - for this we simply use this assertion component which contains
 * simple check / assert methods.
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


	/**
	 * Asserts this is a valid project id. If not a {@link NotAcceptableException} will be thrown
	 * @param projectId
	 */
	public void isValidProjectId(String projectId) {
		assertValid(projectId,projectIdValidation, "Project ID is not valid");
	}

	public void isvalidProjectDescription(String description) {
		assertValid(description, projectDescriptionValidation, "Project description is not valid");
	}


	/**
	 * Asserts this is a valid job uuid. If not a {@link NotAcceptableException} will be thrown
	 * @param jobUUID
	 */
	public void isValidJobUUID(UUID jobUUID) {
		assertValid(jobUUID, jobUUIDValidation, "Job UUID is not valid");
	}

	/**
	 * Asserts this is a valid one time token. If not a {@link NotAcceptableException} will be thrown
	 * @param oneTimeToken
	 */
	public void isValidOneTimeToken(String oneTimeToken) {
		assertValid(oneTimeToken, oneTimeTokenValidation, "Not a valid one time token");
	}

	/**
	 * Asserts this is a valid user id. If not a {@link NotAcceptableException} will be thrown
	 * @param userId
	 */
	public void isValidUserId(String userId) {
		assertValid(userId,  userIdValidation, "User is not valid");
	}

	/**
	 * Asserts this is a valid email address. If not a {@link NotAcceptableException} will be thrown
	 * @param userId
	 */
	public void isValidEmailAddress(String emailAdress) {
		assertValid(emailAdress,  emailValidation, "Email address is not valid");
	}

}
