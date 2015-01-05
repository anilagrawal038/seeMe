package com.famelive.common.exceptions.slotmanagement

import com.famelive.common.exceptions.CommonException

class EventStreamInfoNotFoundException extends CommonException {

    EventStreamInfoNotFoundException() {}

    EventStreamInfoNotFoundException(String message) {
        super(message)
    }

    EventStreamInfoNotFoundException(String message, Throwable cause) {
        super(message, cause)
    }

    EventStreamInfoNotFoundException(Throwable cause) {
        super(cause)
    }
}
