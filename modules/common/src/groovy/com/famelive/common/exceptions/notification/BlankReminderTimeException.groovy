package com.famelive.common.exceptions.notification

import com.famelive.common.exceptions.ValidationException

class BlankReminderTimeException extends ValidationException {

    BlankReminderTimeException() {}

    BlankReminderTimeException(String message) {
        super(message)
    }

    BlankReminderTimeException(String message, Throwable cause) {
        super(message, cause)
    }

    BlankReminderTimeException(Throwable cause) {
        super(cause)
    }
}
