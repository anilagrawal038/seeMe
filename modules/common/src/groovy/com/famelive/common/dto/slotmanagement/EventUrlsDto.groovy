package com.famelive.common.dto.slotmanagement

import com.famelive.common.dto.ResponseDto

class EventUrlsDto extends ResponseDto {

    List<EventUrlDetailDto> eventUrls

    EventUrlsDto() {}

    EventUrlsDto(List<EventUrlDetailDto> eventUrlDetailDtoList) {
        this.eventUrls = eventUrlDetailDtoList
    }

    static EventUrlsDto createCommonResponseDto(List<EventUrlDetailDto> eventUrlDetailDtoList) {
        return new EventUrlsDto(eventUrlDetailDtoList)
    }
}
