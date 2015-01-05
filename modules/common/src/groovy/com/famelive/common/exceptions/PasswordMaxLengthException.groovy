package com.famelive.common.exceptions

class PasswordMaxLengthException extends ValidationException {

    PasswordMaxLengthException() {}

    PasswordMaxLengthException(String message) {
        super(message)
    }

    PasswordMaxLengthException(String message, Throwable cause) {
        super(message, cause)
    }

    PasswordMaxLengthException(Throwable cause) {
        super(cause)
    }
}
