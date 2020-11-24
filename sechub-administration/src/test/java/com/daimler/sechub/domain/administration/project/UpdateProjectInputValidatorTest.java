
//SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.administration.project;

import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;
import org.springframework.validation.Errors;

public class UpdateProjectInputValidatorTest {

 private UpdateProjectInputValidator validatorToTest;
 private ProjectJsonInputValidation validation;
 private Errors errors;
 private ProjectJsonInput input;

 @Before
 public void before() throws Exception {
     input = mock(ProjectJsonInput.class);
     validatorToTest = new UpdateProjectInputValidator();
     validation=mock(ProjectJsonInputValidation.class);
     when(validation.asInput(input)).thenReturn(input);

     errors=mock(Errors.class);

     validatorToTest.validation=validation;
 }


 @Test
 public void checkApiVersion_is_called_on_validation() {
     /* execute */
     validatorToTest.validate(input, errors);

     /* test */
     verify(validation).checkApiVersion(errors, input);
 }

 @Test
 public void checkWhitelist_is_called_on_validation() {
     /* execute */
     validatorToTest.validate(input, errors);

     /* test */
     verify(validation).checkWhitelist(errors, input);
 }

 @Test
 public void checkMetaData_is_called_on_validation() {
     /* execute */
     validatorToTest.validate(input, errors);

     /* test */
     verify(validation).checkMetaData(errors, input);
 }

}
