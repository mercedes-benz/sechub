// SPDX-License-Identifier: MIT
package com.daimler.sechub.sharedkernel.error;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_ACCEPTABLE)
public class AlreadyExistsException extends RuntimeException {

	private static final long serialVersionUID = 9184322887033026055L;

	public AlreadyExistsException() {
		this("Object does already exist");
	}
	
	public AlreadyExistsException(String message) {
		super(message);
	}

}
