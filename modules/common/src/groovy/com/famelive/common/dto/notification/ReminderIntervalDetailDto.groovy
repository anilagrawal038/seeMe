package com.famelive.common.dto.notification

import com.famelive.common.dto.ResponseDto
import com.famelive.common.enums.usermanagement.ReminderTime

class ReminderIntervalDetailDto extends ResponseDto {

    String reminder
    String displayText

    ReminderIntervalDetailDto() {}

    ReminderIntervalDetailDto(ReminderTime reminderTime) {
        this.reminder = reminderTime as String
        this.displayText = reminderTime.value
    }

    static ReminderIntervalDetailDto createCommonResponseDto(ReminderTime reminderTime) {
        return new ReminderIntervalDetailDto(reminderTime)
    }
}
