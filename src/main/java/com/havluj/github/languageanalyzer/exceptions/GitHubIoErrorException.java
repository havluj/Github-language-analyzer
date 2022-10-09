package com.havluj.github.languageanalyzer.exceptions;

import lombok.experimental.StandardException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.INTERNAL_SERVER_ERROR, reason = "Could not connect to GitHub.")
@StandardException
public class GitHubIoErrorException extends RuntimeException {
}
