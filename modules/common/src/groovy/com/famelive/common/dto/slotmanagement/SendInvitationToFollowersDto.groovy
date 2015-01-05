package com.famelive.common.dto.slotmanagement

import com.famelive.common.dto.ResponseDto

class SendInvitationToFollowersDto extends ResponseDto {

    SendInvitationToFollowersDto() {
    }

    static SendInvitationToFollowersDto createCommonResponseDto() {
        return new SendInvitationToFollowersDto()
    }
}
