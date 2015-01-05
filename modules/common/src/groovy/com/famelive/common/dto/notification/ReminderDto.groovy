package com.famelive.common.dto.notification

import com.famelive.common.dto.ResponseDto
import com.famelive.common.enums.usermanagement.ReminderTime
import com.famelive.common.user.Reminder

class ReminderDto extends ResponseDto {

    Long reminderId
    Boolean isSet
    ReminderTime reminderTime

    ReminderDto() {
    }

    ReminderDto(Reminder reminder) {
        this.reminderId = reminder?.id
        this.isSet = reminder?.isActive
        this.reminderTime = reminder?.reminderTime
    }

    static ReminderDto createCommonResponseDto(Reminder reminder) {
        return new ReminderDto(reminder)
    }
}
