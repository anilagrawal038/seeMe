package com.famelive.common.user

import com.famelive.common.enums.usermanagement.ReminderTime
import com.famelive.common.slotmanagement.Event

class Reminder {

    User user
    Event event
    ReminderTime reminderTime
    Boolean isActive = true

    static constraints = {
        user nullable: false
        event nullable: false
        reminderTime nullable: false
    }
}
