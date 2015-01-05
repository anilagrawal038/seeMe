package com.famelive.common.dto.slotmanagement

import com.famelive.common.dto.ResponseDto
import com.famelive.common.dto.usermanagement.UserProfileDto
import com.famelive.common.user.User

class FetchMostPopularUsersDto extends ResponseDto {

    List<UserProfileDto> mostPopularUserDtos

    FetchMostPopularUsersDto() {}

    FetchMostPopularUsersDto(List<User> performerList) {
        this.mostPopularUserDtos = populateMostPopularUsersDto(performerList)
    }

    static FetchMostPopularUsersDto createCommonResponseDto(List<User> performerList) {
        return new FetchMostPopularUsersDto(performerList)
    }

    static List<UserProfileDto> populateMostPopularUsersDto(List<User> performerList) {
        List<UserProfileDto> mostPopularUserDtos = []
        if (performerList) {
            performerList.each { User user ->
                mostPopularUserDtos << new UserProfileDto(user)
            }
        }
        return mostPopularUserDtos
    }

}
