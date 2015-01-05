package com.famelive.common.dto.timezone

import com.famelive.common.dto.ResponseDto
import com.famelive.common.timezone.Country

class FetchCountryListDto extends ResponseDto {

    List<CountryDto> countryDtoList

    FetchCountryListDto() {}

    FetchCountryListDto(List<Country> countries) {
        this.countryDtoList = populateCountryListDto(countries)
    }

    static FetchCountryListDto createCommonResponseDto(List<Country> countries) {
        return new FetchCountryListDto(countries)
    }

    static List<CountryDto> populateCountryListDto(List<Country> countries) {
        List<CountryDto> countryDtoList = []
        countries.each { Country country ->
            countryDtoList << CountryDto.createCommonResponseDto(country)
        }
        return countryDtoList
    }
}
