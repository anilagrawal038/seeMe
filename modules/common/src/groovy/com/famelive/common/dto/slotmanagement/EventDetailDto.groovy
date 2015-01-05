package com.famelive.common.dto.slotmanagement

import com.famelive.common.dto.usermanagement.UserProfileDto
import com.famelive.common.followermanagement.Follow
import com.famelive.common.slotmanagement.Event
import com.famelive.common.user.User

class EventDetailDto {
    Long eventId
    String eventUId
    String name
    String description
    Long duration
    Date startTime
    Date endTime
    String imageName
    FetchGenreDto genreDtos
    UserProfileDto user
    Boolean isFollowerEvent

    EventDetailDto() {}

    EventDetailDto(Event event, User user = null) {
        this.eventId = event.id
        this.eventUId = event?.getEventId()
        this.name = event?.name
        this.description = event?.description
        this.duration = event?.duration
        this.startTime = event?.startTime
        this.endTime = event?.endTime
        this.imageName = event?.imageName
        this.isFollowerEvent = user ? Follow.isUserFollowingPerformer(user, event?.user) : false
        this.genreDtos = FetchGenreDto.createCommonResponseDto(event?.genres as List)
        this.user = new UserProfileDto(event?.user)
    }
}
