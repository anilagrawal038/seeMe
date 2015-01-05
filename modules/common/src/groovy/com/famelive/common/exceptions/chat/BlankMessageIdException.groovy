package com.famelive.common.exceptions.chat

import com.famelive.common.exceptions.ValidationException

class BlankMessageIdException extends ValidationException {

    BlankMessageIdException() {}

    BlankMessageIdException(String message) {
        super(message)
    }

    BlankMessageIdException(String message, Throwable cause) {
        super(message, cause)
    }

    BlankMessageIdException(Throwable cause) {
        super(cause)
    }
}
