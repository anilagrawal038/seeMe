package com.famelive.common.exceptions

class BlankTimeZoneException extends ValidationException {

    BlankTimeZoneException() {}

    BlankTimeZoneException(String message) {
        super(message)
    }

    BlankTimeZoneException(String message, Throwable cause) {
        super(message, cause)
    }

    BlankTimeZoneException(Throwable cause) {
        super(cause)
    }
}
