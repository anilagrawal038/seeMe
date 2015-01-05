package com.famelive.common.dto.slotmanagement

import com.famelive.common.dto.ResponseDto
import com.famelive.common.slotmanagement.Event
import com.famelive.common.user.User

class FetchEventsDto extends ResponseDto {
    List<EventDetailDto> eventDetailDtos
    Integer eventCount

    FetchEventsDto() {}

    FetchEventsDto(List<Event> eventList, User user = null) {
        this.eventDetailDtos = populateFetchEventDetailsDto(eventList, user)
    }

    FetchEventsDto(List<Event> eventList, Integer eventCount) {
        this.eventDetailDtos = populateFetchEventDetailsDto(eventList)
        this.eventCount = eventCount
    }

    static FetchEventsDto createCommonResponseDto(List<Event> eventList, User user = null) {
        return new FetchEventsDto(eventList, user)
    }

    static FetchEventsDto createCommonResponseDto(List<Event> eventList, Integer eventCount) {
        return new FetchEventsDto(eventList, eventCount)
    }

    static List<EventDetailDto> populateFetchEventDetailsDto(List<Event> eventList, User user = null) {
        List<EventDetailDto> eventDetailDtos = []
        if (eventList) {
            eventList.each { Event event ->
                eventDetailDtos << new EventDetailDto(event, user)
            }
        }
        return eventDetailDtos
    }

}
