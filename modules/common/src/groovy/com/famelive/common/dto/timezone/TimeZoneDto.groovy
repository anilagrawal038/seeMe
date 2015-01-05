package com.famelive.common.dto.timezone

import com.famelive.common.dto.ResponseDto
import com.famelive.common.timezone.Zone

class TimeZoneDto extends ResponseDto {

    Long id
    String countryCode
    String zoneName

    TimeZoneDto() {}

    TimeZoneDto(Zone zone) {
        this.id = zone?.id
        this.countryCode = zone?.country?.code
        this.zoneName = zone?.zoneName
    }

    static TimeZoneDto createCommonResponseDto(Zone zone) {
        return new TimeZoneDto(zone)
    }
}
