package com.famelive.common.exceptions.notification

import com.famelive.common.exceptions.ValidationException

class BlankReminderIdException extends ValidationException {

    BlankReminderIdException() {}

    BlankReminderIdException(String message) {
        super(message)
    }

    BlankReminderIdException(String message, Throwable cause) {
        super(message, cause)
    }

    BlankReminderIdException(Throwable cause) {
        super(cause)
    }
}
