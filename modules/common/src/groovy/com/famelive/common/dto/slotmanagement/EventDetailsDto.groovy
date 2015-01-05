package com.famelive.common.dto.slotmanagement

import com.famelive.common.enums.slotmanagement.EventStatus
import com.famelive.common.enums.usermanagement.ReminderTime
import com.famelive.common.streamManagement.EventStreamInfo
import com.famelive.common.user.Reminder

class EventDetailsDto extends EventDetailDto {

    EventStatus eventStatus
    Long reminderId
    ReminderTime reminderTime

    EventDetailsDto() {}

    EventDetailsDto(EventStreamInfo eventStreamInfo, Reminder reminder = null) {
        super(eventStreamInfo.event)
        this.eventStatus = eventStreamInfo.event.status
        this.reminderId = reminder?.id
        this.reminderTime = reminder?.reminderTime
    }

    static EventDetailsDto createCommonResponseDto(EventStreamInfo eventStreamInfo, Reminder reminder = null) {
        return new EventDetailsDto(eventStreamInfo, reminder)
    }
}
