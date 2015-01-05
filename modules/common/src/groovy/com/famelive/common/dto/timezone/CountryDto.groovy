package com.famelive.common.dto.timezone

import com.famelive.common.dto.ResponseDto
import com.famelive.common.timezone.Country

class CountryDto extends ResponseDto {

    Long id
    String countryCode
    String countryName

    CountryDto() {}

    CountryDto(Country country) {
        this.id = country?.id
        this.countryCode = country?.code
        this.countryName = country?.name
    }

    static CountryDto createCommonResponseDto(Country country) {
        return new CountryDto(country)
    }
}
