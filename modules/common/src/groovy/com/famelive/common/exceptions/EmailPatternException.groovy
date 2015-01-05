package com.famelive.common.exceptions

class EmailPatternException extends ValidationException {

    EmailPatternException() {}

    EmailPatternException(String message) {
        super(message)
    }

    EmailPatternException(String message, Throwable cause) {
        super(message, cause)
    }

    EmailPatternException(Throwable cause) {
        super(cause)
    }
}
