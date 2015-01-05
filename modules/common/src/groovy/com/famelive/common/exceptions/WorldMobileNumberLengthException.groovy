package com.famelive.common.exceptions

class WorldMobileNumberLengthException extends ValidationException {

    WorldMobileNumberLengthException() {}

    WorldMobileNumberLengthException(String message) {
        super(message)
    }

    WorldMobileNumberLengthException(String message, Throwable cause) {
        super(message, cause)
    }

    WorldMobileNumberLengthException(Throwable cause) {
        super(cause)
    }
}
