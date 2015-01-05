package com.famelive.common.dto.timezone

import com.famelive.common.dto.ResponseDto
import com.famelive.common.timezone.Zone

class FetchTimeZoneListDto extends ResponseDto {

    List<TimeZoneDto> timeZoneDtos

    FetchTimeZoneListDto() {}

    FetchTimeZoneListDto(List<Zone> zoneList) {
        this.timeZoneDtos = populateTimeZoneListDto(zoneList)
    }

    static FetchTimeZoneListDto createCommonResponseDto(List<Zone> zoneList) {
        return new FetchTimeZoneListDto(zoneList)
    }

    static List<TimeZoneDto> populateTimeZoneListDto(List<Zone> zoneList) {
        List<TimeZoneDto> timeZoneDtos = []
        zoneList.each { Zone zone ->
            timeZoneDtos << TimeZoneDto.createCommonResponseDto(zone)
        }
        return timeZoneDtos
    }
}
