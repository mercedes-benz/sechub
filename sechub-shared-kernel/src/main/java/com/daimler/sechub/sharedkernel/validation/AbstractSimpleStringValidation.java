// SPDX-License-Identifier: MIT
package com.daimler.sechub.sharedkernel.validation;

public abstract class AbstractSimpleStringValidation extends AbstractValidation<String> {

	protected void validateNoUpperCaseCharacters(ValidationContext<String> context) {
		String string = context.objectToValidate;
		if (string == null) {
			return;
		}
		if (string.isEmpty()) {
			return;
		}
		for (char c : string.toCharArray()) {
			if (Character.isUpperCase(c)) {
				context.addError("Uppercase character not allowed, but found '" + c + "'.");
				return;
			}
		}
	}
	final
	protected void validateWithoutWhitespaces(ValidationContext<String> context) {
		String string = context.objectToValidate;
		if (string == null) {
			return;
		}
		if (string.isEmpty()) {
			return;
		}
		for (char c : string.toCharArray()) {
			if (Character.isWhitespace(c)) {
				context.addError("Whitespace detected inside string");
				return;
			}
		}
	}

	protected void validateOnlyAlphabeticDigitOrAllowedParts(ValidationContext<String> context, char ... alsoAllowed) {
		String string = context.objectToValidate;
		if (string == null) {
			return;
		}
		if (string.isEmpty()) {
			return;
		}
		for (char c : string.toCharArray()) {
			if (Character.isDigit(c)) {
				continue;
			}
			if (Character.isAlphabetic(c)) {
				continue;
			}
			boolean ok = false;
			for (char allowed : alsoAllowed) {
				if (c == allowed) {
					ok=true;
					continue;
				}
			}
			if (ok) {
				continue;
			}
			context.addError("Character must be one of alloweds, but found '" + c + "'.");
			return;
		}
	}

	protected void validateNotContainingCharackters(ValidationContext<String> context, char... chars) {
		String string = context.objectToValidate;
		if (string == null) {
			return;
		}
		if (string.isEmpty()) {
			return;
		}
		for (char c : string.toCharArray()) {
			for (char denied : chars) {
				if (c == denied) {
					context.addError("Character now not allowed here, but found '" + c + "'.");
					return;
				}
			}
		}
	}

	protected void validateSameLengthWhenTrimmed(ValidationContext<String> context) {
		String string = context.objectToValidate;
		if (string == null) {
			return;
		}
		String trimmedName = string.trim();
		boolean sameLength = trimmedName.length() == string.length();
		if (sameLength) {
			return;
		}
		context.addError("Please remove whitespaces at beginning and at the end");
	}

	protected void validateLength(ValidationContext<String> context) {
		validateMinLength(context);
		validateMaxLength(context);
	}

	protected void validateMaxLength(ValidationContext<String> context) {
		String string = context.objectToValidate;
		if (getMaxLength() <= 0) {
			return;
		}
		if (string == null) {
			context.addError("String is null, so max length " + getMaxLength() + " not valid");
			return;
		}
		boolean validMaxLength = string.length() <= getMaxLength();
		if (!validMaxLength) {
			context.addError("Maximum size is " + getMaxLength() + " but was " + string.length());
		}
	}

	protected void validateMinLength(ValidationContext<String> context) {
		if (getMinLength() <= 0) {
			return;
		}
		String string = context.objectToValidate;
		if (string == null) {
			context.addError("String is null, so min length " + getMinLength() + " not valid");
			return;
		}
		boolean validMinLength = string.length() >= getMinLength();
		if (!validMinLength) {
			context.addError("Minimum size is " + getMinLength() + " but was " + string.length());
		}
	}
}
