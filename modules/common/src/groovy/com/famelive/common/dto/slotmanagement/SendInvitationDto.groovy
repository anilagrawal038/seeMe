package com.famelive.common.dto.slotmanagement

import com.famelive.common.dto.ResponseDto

class SendInvitationDto extends ResponseDto {

    SendInvitationDto() {
    }

    static SendInvitationDto createCommonResponseDto() {
        return new SendInvitationDto()
    }
}
