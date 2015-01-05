package com.famelive.common.dto.timezone

import com.famelive.common.dto.ResponseDto

class ResetTimeZoneDto extends ResponseDto {

    ResetTimeZoneDto() {}


    static ResetTimeZoneDto createCommonResponseDto() {
        return new ResetTimeZoneDto()
    }
}
