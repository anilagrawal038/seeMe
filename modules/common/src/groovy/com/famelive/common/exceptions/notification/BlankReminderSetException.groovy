package com.famelive.common.exceptions.notification

import com.famelive.common.exceptions.ValidationException

class BlankReminderSetException extends ValidationException {

    BlankReminderSetException() {}

    BlankReminderSetException(String message) {
        super(message)
    }

    BlankReminderSetException(String message, Throwable cause) {
        super(message, cause)
    }

    BlankReminderSetException(Throwable cause) {
        super(cause)
    }
}
