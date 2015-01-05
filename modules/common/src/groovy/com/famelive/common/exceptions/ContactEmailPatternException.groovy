package com.famelive.common.exceptions

class ContactEmailPatternException extends ValidationException {

    ContactEmailPatternException() {}

    ContactEmailPatternException(String message) {
        super(message)
    }

    ContactEmailPatternException(String message, Throwable cause) {
        super(message, cause)
    }

    ContactEmailPatternException(Throwable cause) {
        super(cause)
    }
}
