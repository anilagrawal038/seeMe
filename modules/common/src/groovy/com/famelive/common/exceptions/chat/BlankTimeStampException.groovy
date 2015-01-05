package com.famelive.common.exceptions.chat

import com.famelive.common.exceptions.ValidationException

class BlankTimeStampException extends ValidationException {

    BlankTimeStampException() {}

    BlankTimeStampException(String message) {
        super(message)
    }

    BlankTimeStampException(String message, Throwable cause) {
        super(message, cause)
    }

    BlankTimeStampException(Throwable cause) {
        super(cause)
    }
}
