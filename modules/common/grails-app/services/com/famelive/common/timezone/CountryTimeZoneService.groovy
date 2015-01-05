package com.famelive.common.timezone

import com.famelive.common.command.timezone.FetchCountryListCommand
import com.famelive.common.command.timezone.FetchTimeZoneListCommand
import com.famelive.common.dto.timezone.FetchCountryListDto
import com.famelive.common.dto.timezone.FetchTimeZoneListDto
import com.famelive.common.exceptions.CommonException

class CountryTimeZoneService {

    FetchCountryListDto fetchCountryList(FetchCountryListCommand fetchCountryListCommand) throws CommonException {
        List<Country> countries = Country.list()
        FetchCountryListDto fetchCountryListDto = FetchCountryListDto.createCommonResponseDto(countries)
        return fetchCountryListDto
    }

    FetchTimeZoneListDto fetchTimeZoneList(FetchTimeZoneListCommand fetchTimeZoneListCommand) throws CommonException {
        List<Zone> zoneList = Zone.list()
        FetchTimeZoneListDto fetchTimeZoneListDto = FetchTimeZoneListDto.createCommonResponseDto(zoneList)
        return fetchTimeZoneListDto
    }
}
