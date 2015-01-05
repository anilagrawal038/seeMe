package com.famelive.common.exceptions.chat

import com.famelive.common.exceptions.ValidationException


class BlankMessageException extends ValidationException {

    BlankMessageException() {}

    BlankMessageException(String message) {
        super(message)
    }

    BlankMessageException(String message, Throwable cause) {
        super(message, cause)
    }

    BlankMessageException(Throwable cause) {
        super(cause)
    }
}
