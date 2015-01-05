package com.famelive.common.command.notification

import com.famelive.common.command.AuthenticationTokenCommand
import com.famelive.common.enums.usermanagement.ReminderTime
import com.famelive.common.exceptions.notification.BlankReminderIdException
import com.famelive.common.exceptions.notification.BlankReminderTimeException
import com.famelive.common.exceptions.slotmanagement.BlankEventIdException
import com.famelive.common.exceptions.slotmanagement.EventNotFoundException
import com.famelive.common.slotmanagement.Event
import grails.validation.Validateable

@Validateable
class ReminderCommand extends AuthenticationTokenCommand {

    Long reminderId
    Long eventId
    ReminderTime reminderTime
    Boolean isSet

    static constraints = {
        reminderId nullable: true, validator: { val, obj ->
            if (!obj.isSet && !val) {
                throw new BlankReminderIdException()
            }
        }
        eventId nullable: true, validator: { val, obj ->
            if (!val) {
                throw new BlankEventIdException()
            } else if (!Event.get(val)) {
                throw new EventNotFoundException()
            }
        }
        reminderTime nullable: true, validator: { val, obj ->
            if (obj.reminderId && !val) {
                throw new BlankReminderTimeException()
            }
        }
        isSet nullable: true
    }
}
