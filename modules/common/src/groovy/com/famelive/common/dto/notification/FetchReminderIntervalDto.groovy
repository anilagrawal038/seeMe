package com.famelive.common.dto.notification

import com.famelive.common.dto.ResponseDto
import com.famelive.common.enums.usermanagement.ReminderTime

class FetchReminderIntervalDto extends ResponseDto {

    List<ReminderIntervalDetailDto> reminderIntervalDetailDtos

    FetchReminderIntervalDto() {
        reminderIntervalDetailDtos = populateReminderIntervalDetailDto()
    }

    static FetchReminderIntervalDto createCommonResponseDto() {
        return new FetchReminderIntervalDto()
    }

    static List<ReminderIntervalDetailDto> populateReminderIntervalDetailDto() {
        List<ReminderIntervalDetailDto> reminderIntervalDetailDtos = []
        ReminderTime.values().each { ReminderTime reminderTime ->
            reminderIntervalDetailDtos << ReminderIntervalDetailDto?.createCommonResponseDto(reminderTime)
        }
        return reminderIntervalDetailDtos
    }
}
