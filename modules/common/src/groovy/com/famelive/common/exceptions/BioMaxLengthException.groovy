package com.famelive.common.exceptions

class BioMaxLengthException extends ValidationException {

    BioMaxLengthException() {}

    BioMaxLengthException(String message) {
        super(message)
    }

    BioMaxLengthException(String message, Throwable cause) {
        super(message, cause)
    }

    BioMaxLengthException(Throwable cause) {
        super(cause)
    }
}
