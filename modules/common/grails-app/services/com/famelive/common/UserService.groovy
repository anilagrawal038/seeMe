package com.famelive.common

import com.famelive.common.command.timezone.ResetDefaultTimeZoneCommand
import com.famelive.common.command.usernamagement.*
import com.famelive.common.dto.timezone.ResetTimeZoneDto
import com.famelive.common.dto.usermanagement.*
import com.famelive.common.enums.usermanagement.SocialAccount
import com.famelive.common.enums.usermanagement.UserRegistrationType
import com.famelive.common.enums.usermanagement.UserRoles
import com.famelive.common.exceptions.*
import com.famelive.common.followermanagement.Follow
import com.famelive.common.timezone.Country
import com.famelive.common.timezone.Zone
import com.famelive.common.user.LinkedSocialAccount
import com.famelive.common.user.Role
import com.famelive.common.user.User
import com.famelive.common.user.UserRole
import com.famelive.common.util.FameLiveUtil

class UserService {

    def grailsApplication
    def commonMailService
    def springSecurityService
    def passwordEncoder

    ConfigurationDto fetchConfiguration() {
        Map config = grailsApplication.config
        return ConfigurationDto.createCommonResponseDto(config)
    }

    RegistrationDto registerUser(RegistrationCommand registrationCommand) throws CommonException {
        registrationCommand.validate()
        User user = createUserObject(registrationCommand)
        user.refresh()
        createUserRoleObject(user, UserRoles.USER)
        linkSocialAccount(user, registrationCommand?.medium?.socialAccount)
        sendRegistrationConfirmationEmail(user)
        return RegistrationDto.createCommonResponseDto(user)
    }

    void sendRegistrationConfirmationEmail(User user) {
        commonMailService.sendRegistrationMailToViewer(user)
        commonMailService.sendEmailVerificationMail(user)
    }

    User createUserObject(RegistrationCommand registrationCommand) {
        User user = new User()
        user.email = registrationCommand?.email
        user.password = (registrationCommand?.medium == UserRegistrationType.MANUAL) ? registrationCommand?.password : generatePassword(registrationCommand?.username)
        user.username = registrationCommand?.username
        user.fameName = registrationCommand?.fameName
        user.mobile = registrationCommand?.mobile
        if (registrationCommand.countryCode) {
            user.countryCode = Country.get(registrationCommand.countryCode)
        }
        user.imageName = registrationCommand?.imageName
        user.alternativeEmail = registrationCommand?.alternativeEmail
        user.bio = registrationCommand?.bio
        user.defaultTimeZone = Zone?.findByZoneName(registrationCommand?.defaultTimeZone)
        user.isAccountVerified = false
        user.verificationToken = FameLiveUtil.getRandomCode()
        user.registrationType = registrationCommand?.medium
        user.save(flush: true, failOnError: true)
    }

    void linkSocialAccount(User user, SocialAccount socialAccount, String token = null) {
        if (socialAccount) {
            LinkedSocialAccount linkedSocialAccount = LinkedSocialAccount.findByUserAndSocialAccount(user, socialAccount)
            if (linkedSocialAccount) {
                linkedSocialAccount.isActive = true
                linkedSocialAccount.token = token
                linkedSocialAccount.save(flush: true, failOnError: true)
            } else {
                LinkedSocialAccount linkedSocialAccountToUser = new LinkedSocialAccount(user: user, socialAccount: socialAccount, token: token).save(flush: true, failOnError: true)
                user.addToLinkedSocialAccounts(linkedSocialAccountToUser)
            }
        }
    }

    String generatePassword(String userName) {
        String salt = grailsApplication.config.social.registration.password.salt as String
        return userName + salt
    }

    UserRole createUserRoleObject(User user, UserRoles userRoles) {
        Role role = Role.findByAuthority(userRoles.value)
        return new UserRole(user: user, role: role).save(failOnError: true)
    }

    ChangePasswordDto changePassword(ChangePasswordCommand changePasswordCommand) throws CommonException {
        changePasswordCommand.validate()
        User user = findUserById(changePasswordCommand?.id)
        if (!passwordEncoder.isPasswordValid(user.password, changePasswordCommand.password, null)) {
            throw new PasswordNotEqualException()
        }
        saveUserPassword(user, changePasswordCommand?.newPassword)
        return ChangePasswordDto.createCommonResponseDto(user)
    }

    ForgotPasswordDto forgotPassword(ForgotPasswordCommand forgotPasswordCommand) throws CommonException {
        forgotPasswordCommand.validate()
        User user = findUserByEmail(forgotPasswordCommand?.email)
        user.forgotPasswordCode = FameLiveUtil.getPassword()
        if (user.save()) {
            commonMailService.sendForgotPasswordMail(user)
        } else {
            throw new CommonException("Unable to registerUser user while updating forgot password code")
        }
        return ForgotPasswordDto.createCommonResponseDto(user)
    }

    User saveUserPassword(User user, String newPassword) {
        user.password = newPassword
        user.save(flush: true, failOnError: true)
        return user
    }

    UserProfileDto updateUserProfile(UserProfileCommand userProfileCommand) throws CommonException {
        userProfileCommand?.validate()
        User user = findUserById(userProfileCommand?.id)
        saveUserProfileDetails(user, userProfileCommand)
        return UserProfileDto.createCommonResponseDto(user)
    }

    User saveUserProfileDetails(User user, UserProfileCommand userProfileCommand) {
        user.fameName = userProfileCommand.fameName
        user.username = userProfileCommand.username
        user.mobile = userProfileCommand.mobile
        user.alternativeEmail = userProfileCommand?.alternativeEmail
        user.bio = userProfileCommand?.bio
        user.imageName = userProfileCommand.imageName
        user.save(flush: true, failOnError: true)
        return user
    }

    UserProfileDto fetchUserProfileData(FetchUserProfileCommand fetchUserProfileCommand) throws CommonException {
        fetchUserProfileCommand?.validate()
        User user = findUserById(fetchUserProfileCommand?.id)
        return UserProfileDto.createCommonResponseDto(user)
    }

    FetchSocialAccountDto fetchSocialAccounts(FetchUserSocialAccountCommand fetchUserSocialAccountCommand) throws CommonException {
        fetchUserSocialAccountCommand?.validate()
        User user = findUserById(fetchUserSocialAccountCommand?.id)
        return FetchSocialAccountDto.createCommonResponseDto(user)
    }

    UserProfileDto fetchPerformerDetail(FetchPerformerProfileCommand fetchPerformerProfileCommand) throws CommonException {
        fetchPerformerProfileCommand?.validate()
        User performer = User.findByFameId(fetchPerformerProfileCommand?.fameId)
        Follow follow = null
        if (fetchPerformerProfileCommand?.id) {
            User follower = findUserById(fetchPerformerProfileCommand?.id)
            follow = Follow.findByPerformerAndFollower(performer, follower)
        }
        UserProfileDto userProfileDto = UserProfileDto.createCommonResponseDto(performer, follow)
        return userProfileDto
    }

    User findUserById(Long id) throws UserNotFoundException {
        User user = User.get(id)
        if (!user) {
            throw new UserNotFoundException()
        }
        return user
    }

    User findUserByEmail(String email) throws UserNotFoundException {
        User user = User.findByEmail(email)
        if (!user) {
            throw new UserNotFoundException()
        }
        return user
    }


    UsersDto fetchUserList(UserSearchCommand userSearchCommand) {
        def userFilteredData = User.filteredList(userSearchCommand)
        Integer userCount = userFilteredData?.count()
        List<UserProfileDto> profileDtoList = populateUserProfileDtoList(userFilteredData, userSearchCommand)
        UsersDto usersDto = UsersDto.createCommonResponseDto(profileDtoList, userCount)
        return usersDto
    }

    List<UserProfileDto> populateUserProfileDtoList(def userFilteredData, UserSearchCommand userSearchCommand) {
        List<UserProfileDto> profileDtoList = []
        userFilteredData?.list(userSearchCommand.properties)?.each { User user ->
            profileDtoList << new UserProfileDto(user)
        }
        return profileDtoList
    }

    ValidateForgotPasswordCodeDto validateForgotPasswordCode(ValidateForgotPasswordCodeCommand commonValidateForgotPasswordCodeCommand) throws CommonException {
        commonValidateForgotPasswordCodeCommand.validate()
        User user = User.findByEmail(commonValidateForgotPasswordCodeCommand.email)
        if (user.forgotPasswordCode != commonValidateForgotPasswordCodeCommand.code) {
            throw new InvalidForgotPasswordCodeException()
        }
        return ValidateForgotPasswordCodeDto.createCommonResponseDto(user)
    }

    UpdateForgotPasswordDto updateForgotPassword(UpdateForgotPasswordCommand commonUpdateForgotPasswordCommand) throws CommonException {
        commonUpdateForgotPasswordCommand.validate()
        User user = User.findByEmail(commonUpdateForgotPasswordCommand.email)
        saveUserPassword(user, commonUpdateForgotPasswordCommand.password)
        return UpdateForgotPasswordDto.createCommonResponseDto()
    }

    UserImageDto saveUserImageName(UserImageCommand userImageCommand) throws CommonException {
        userImageCommand.validate()
        User user = findUserByEmail(userImageCommand?.email)
        user.imageName = userImageCommand?.imageName
        user.save(flush: true, failOnError: true)
        return UserImageDto.createCommonResponseDto()
    }

    FameNameAvailabilityDto checkAvailability(FameNameCommand fameNameCommand) throws CommonException {
        fameNameCommand.validate()
        return FameNameAvailabilityDto.createCommonResponseDto()
    }

    void blockOrUnBlockUserAccount(FetchUserProfileCommand fetchUserProfileCommand) throws CommonException {
        fetchUserProfileCommand?.validate()
        changeUserAccountStatus(fetchUserProfileCommand)
    }

    User changeUserAccountStatus(FetchUserProfileCommand fetchUserProfileCommand) throws CommonException {
        User user = findUserById(fetchUserProfileCommand?.id)
        user.accountLocked = !user.accountLocked
        user.save(flush: true, failOnError: true)
    }

    SocialAccountDto addSocialAccount(UserSocialAccountCommand addUserSocialAccountCommand) throws CommonException {
        addUserSocialAccountCommand.validate()
        User user = findUserById(addUserSocialAccountCommand?.id)
        linkSocialAccount(user, addUserSocialAccountCommand?.socialAccount, addUserSocialAccountCommand?.token)
        SocialAccountDto addSocialAccountDto = SocialAccountDto.createCommonResponseDto(user)
        return addSocialAccountDto
    }

    SocialAccountDto removeSocialAccount(UserSocialAccountCommand addUserSocialAccountCommand) throws CommonException {
        addUserSocialAccountCommand.validate()
        User user = findUserById(addUserSocialAccountCommand?.id)
        deActiveUserSocialAccount(user, addUserSocialAccountCommand?.socialAccount)
        SocialAccountDto addSocialAccountDto = SocialAccountDto.createCommonResponseDto(user)
        return addSocialAccountDto
    }

    void deActiveUserSocialAccount(User user, SocialAccount socialAccount) throws CommonException {
        LinkedSocialAccount linkedSocialAccount = LinkedSocialAccount.findByUserAndSocialAccount(user, socialAccount)
        if (socialAccount) {
            linkedSocialAccount.isActive = false
            linkedSocialAccount.save(flush: true, failOnError: true)
        } else {
            throw new InvalidUserSocialAccountException()
        }
    }

    ChangeEmailDto changeEmail(ChangeEmailCommand changeEmailCommand) throws CommonException {
        changeEmailCommand.validate()
        User user = changeEmailAddress(changeEmailCommand)
        commonMailService.sendEmailVerificationMail(user)
        ChangeEmailDto changeEmailDto = ChangeEmailDto.createCommonResponseDto(user)
        return changeEmailDto
    }

    User changeEmailAddress(ChangeEmailCommand changeEmailCommand) {
        User user = findUserById(changeEmailCommand?.id)
        user.email = changeEmailCommand?.email
        user.isAccountVerified = false
        user.verificationToken = FameLiveUtil.getRandomCode()
        user.save(flush: true, failOnError: true)
    }

    CheckUserAccountDto checkUserAccount(CheckUserAccountCommand checkUserAccountCommand) throws CommonException {
        checkUserAccountCommand.validate()
        User user = findUserById(checkUserAccountCommand?.id)
        CheckUserAccountDto checkUserAccountDto = CheckUserAccountDto.createCommonResponseDto(user)
        return checkUserAccountDto
    }

    VerifyUserEmailDto verifyEmail(VerifyUserEmailCommand verifyUserEmailCommand) throws CommonException {
        verifyUserEmailCommand.validate()
        User user = findUserById(verifyUserEmailCommand?.id)
        user.isAccountVerified = true
        user.save(flush: true, failOnError: true)
        VerifyUserEmailDto verifyUserEmailDto = VerifyUserEmailDto.createCommonResponseDto(user)
        return verifyUserEmailDto
    }

    SendEmailVerificationCodeDto sendEmailVerificationCode(SendEmailVerificationCodeCommand sendEmailVerificationCodeCommand) throws CommonException {
        User user = findUserById(sendEmailVerificationCodeCommand?.id)
        commonMailService.sendEmailVerificationMail(user)
        SendEmailVerificationCodeDto sendEmailVerificationCodeDto = SendEmailVerificationCodeDto.createCommonResponseDto(user)
        return sendEmailVerificationCodeDto
    }

    ResetTimeZoneDto resetDefaultTimeZone(ResetDefaultTimeZoneCommand resetDefaultTimeZoneCommand) throws CommonException {
        resetDefaultTimeZoneCommand.validate()
        Zone zone = Zone?.findByZoneName(resetDefaultTimeZoneCommand?.timeZone)
        if (!zone) {
            throw new BlankTimeZoneException()
        }
        setTimeZoneOfUser(resetDefaultTimeZoneCommand.id, zone)
        ResetTimeZoneDto resetTimeZoneDto = ResetTimeZoneDto.createCommonResponseDto()
        return resetTimeZoneDto
    }

    void setTimeZoneOfUser(Long id, Zone zone) throws CommonException {
        User user = findUserById(id)
        user.defaultTimeZone = zone
        user.save(flush: true, failOnError: true)
    }
}
