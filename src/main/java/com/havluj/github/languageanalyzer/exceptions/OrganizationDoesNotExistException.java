package com.havluj.github.languageanalyzer.exceptions;

import lombok.experimental.StandardException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.BAD_REQUEST, reason = "Organization not found")
@StandardException
public class OrganizationDoesNotExistException extends RuntimeException {

}
