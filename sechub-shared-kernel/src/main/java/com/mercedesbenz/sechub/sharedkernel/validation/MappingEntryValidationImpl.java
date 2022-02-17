// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sharedkernel.validation;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.springframework.stereotype.Component;

import com.mercedesbenz.sechub.sharedkernel.mapping.MappingEntry;

@Component
public class MappingEntryValidationImpl extends AbstractValidation<MappingEntry> implements MappingEntryValidation {

    private PatternValidation patternValidation = new PatternValidation();
    private ReplacementValidation replaceValidation = new ReplacementValidation();
    private CommentValidation commentValidation = new CommentValidation();

    @Override
    protected void setup(AbstractValidation<MappingEntry>.ValidationConfig config) {

    }

    @Override
    protected String getValidatorName() {
        return "mapping entery validation";
    }

    @Override
    protected void validate(ValidationContext<MappingEntry> context) {
        validateNotNull(context);
        if (context.isInValid()) {
            return;
        }
        MappingEntry mappingEntry = context.objectToValidate;
        context.result.addErrors(patternValidation.validate(mappingEntry.getPattern()));
        context.result.addErrors(replaceValidation.validate(mappingEntry.getReplacement()));
        context.result.addErrors(commentValidation.validate(mappingEntry.getComment()));

    }

    private class PatternValidation extends AbstractSimpleStringValidation {

        @Override
        protected void setup(AbstractValidation<String>.ValidationConfig config) {
            config.maxLength = 80;
        }

        @Override
        protected void validate(ValidationContext<String> context) {
            validateNotNull(context);
            validateMaxLength(context);

            if (context.isInValid()) {
                return;
            }

            try {
                Pattern.compile(context.objectToValidate);
            } catch (PatternSyntaxException e) {
                addErrorMessage(context, "pattern was invalid:" + e.getPattern());
            }
        }

        @Override
        protected String getValidatorName() {
            return "mapping pattern validation";
        }
    }

    private class ReplacementValidation extends AbstractSimpleStringValidation {

        @Override
        protected void setup(AbstractValidation<String>.ValidationConfig config) {
            config.maxLength = 80;
        }

        @Override
        protected void validate(ValidationContext<String> context) {
            validateNotNull(context);
            validateMaxLength(context);
        }

        @Override
        protected String getValidatorName() {
            return "mapping replacement validation";
        }

    }

    private class CommentValidation extends AbstractSimpleStringValidation {

        @Override
        protected void setup(AbstractValidation<String>.ValidationConfig config) {
            config.maxLength = 80;
        }

        @Override
        protected void validate(ValidationContext<String> context) {
            validateNotNull(context);
            validateMaxLength(context);
        }

        @Override
        protected String getValidatorName() {
            return "mapping comment validation";
        }

    }

}
