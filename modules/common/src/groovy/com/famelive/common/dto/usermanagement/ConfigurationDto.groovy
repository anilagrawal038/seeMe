package com.famelive.common.dto.usermanagement

import com.famelive.common.constant.CommonConstants
import com.famelive.common.constant.Constraints
import com.famelive.common.dto.ResponseDto
import com.famelive.common.enums.usermanagement.UserRegistrationType

class ConfigurationDto extends ResponseDto {

    //User Management
    Integer usernameMaxSize
    Integer fameNameMaxSize
    Integer mobileNumberSize
    String mobileNumberPattern
    String emailPattern
    String passwordPattern
    Integer passwordMinSize

    //User type
    UserRegistrationType facebook
    UserRegistrationType gPlus
    UserRegistrationType manual

    //Cloudinary
    String cloudName
    String apiKey
    String apiSecret
    String folder
    String mimeType
    String baseUrl
    String userImageFolder
    String userImageSize
    String eventImageFolder
    String eventPosterSize
    String flagFolder

    //Slot Management
    Integer eventNameMaxSize
    Integer eventDescriptionMaxSize
    String eventSlotDuration

    //Other
    String dateFormat

    String termsAndConditionUrl

    ConfigurationDto() {}

    ConfigurationDto(Map configMap) {
        this.usernameMaxSize = Constraints.USERNAME_MAX_SIZE
        this.fameNameMaxSize = Constraints.FAMENAME_MAX_SIZE
        this.mobileNumberSize = Constraints.INDIA_MOBILE_NUMBER_SIZE
        this.mobileNumberPattern = Constraints.MOBILE_NUMBER_PATTERN
        this.emailPattern = Constraints.EMAIL_PATTERN
        this.passwordPattern = Constraints.PASSWORD_PATTERN
        this.passwordMinSize = Constraints.PASSWORD_MIN_SIZE
        this.facebook = UserRegistrationType.FACEBOOK
        this.gPlus = UserRegistrationType.G_PLUS
        this.manual = UserRegistrationType.MANUAL
        this.eventNameMaxSize = Constraints.EVENT_NAME_MAX_SIZE
        this.eventDescriptionMaxSize = Constraints.EVENT_DESCRIPTION_MAX_SIZE
        this.eventSlotDuration = CommonConstants.EVENT_SLOT_DURATIONS.toString()
        this.dateFormat = CommonConstants.INPUT_DATE_FORMAT
        this.cloudName = configMap.cloudinary.config.cloud_name
        this.apiKey = configMap.cloudinary.config.api_key
        this.apiSecret = configMap.cloudinary.config.api_secret
        this.folder = configMap.cloudinary.config.folder
        this.mimeType = configMap.cloudinary.config.mimeType
        this.baseUrl = configMap.cloudinary.config.baseUrl
        this.flagFolder = configMap.cloudinary.config.flagFolder
        this.userImageFolder = configMap.cloudinary.config.userImageFolder
        this.userImageSize = configMap.cloudinary.config.userImageSize
        this.eventImageFolder = configMap.cloudinary.config.eventImageFolder
        this.eventPosterSize = configMap.cloudinary.config.eventPosterSize
        this.termsAndConditionUrl = populateTermsAndConditionUrl(configMap)
    }

    static ConfigurationDto createCommonResponseDto(Map map) {
        return new ConfigurationDto(map)
    }

    static String populateTermsAndConditionUrl(Map configMap) {
        return configMap.famelive.adminPanel.homepage.url + "/public/termsAndCondition"
    }

}
