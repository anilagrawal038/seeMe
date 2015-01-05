package com.famelive.common.command.usernamagement

import com.famelive.common.command.RequestCommand
import com.famelive.common.constant.Constraints
import com.famelive.common.enums.usermanagement.UserRegistrationType
import com.famelive.common.exceptions.*
import com.famelive.common.timezone.Country
import com.famelive.common.user.User
import grails.validation.Validateable

@Validateable
class RegistrationCommand extends RequestCommand {
    String email
    String password
    String username
    String fameName
    String mobile
    String imageName
    Long countryCode
    String defaultTimeZone
    String alternativeEmail
    String bio
    UserRegistrationType medium

    static constraints = {
        username nullable: true, validator: { val, obj ->
            if (!val) {
                throw new BlankUserNameException()
            } else if (val.length() > Constraints.USERNAME_MAX_SIZE) {
                throw new UserNameMaxLengthException()
            }
        }
        password nullable: true, validator: { val, obj ->
            if (obj.medium == UserRegistrationType.MANUAL) {
                if (!val) {
                    throw new BlankPasswordException()
                } else if (val.length() < Constraints.PASSWORD_MIN_SIZE) {
                    throw new PasswordMinLengthException()
                } else if (val.length() >= Constraints.PASSWORD_MAX_SIZE) {
                    throw new PasswordMaxLengthException()
                } else if (!val.matches(Constraints.PASSWORD_PATTERN)) {
                    throw new PasswordPatternException()
                }
            }
        }
        email nullable: true, validator: { val, obj ->
            if (!val) {
                throw new BlankEmailException()
            } else if (User.countByEmail(val)) {
                throw new UniqueEmailException()
            } else if (!val.matches(Constraints.EMAIL_PATTERN)) {
                throw new EmailPatternException()
            }
        }
        mobile nullable: true, validator: { val, obj ->
            if (val) {
                if (!val.matches(Constraints.MOBILE_NUMBER_PATTERN)) {
                    throw new EmailPatternException()
                } else if (obj.countryCode && (Country?.get(obj.countryCode)?.code == Constraints.INDIA_COUNTRY_CODE) && (val.length() != Constraints.INDIA_MOBILE_NUMBER_SIZE)) {
                    throw new MobileNumberLengthException()
                } else if (obj.countryCode && val.length() >= Constraints.WORLD_MOBILE_NUMBER_SIZE) {
                    throw new WorldMobileNumberLengthException()
                }
            }
        }
        countryCode nullable: true
        fameName nullable: true, validator: { val, obj ->
            if (!val) {
                throw new BlankFameNameException()
            } else if (val.length() > Constraints.FAMENAME_MAX_SIZE) {
                throw new FameNameMaxLengthException()
            } else if (User.countByFameName(val)) {
                throw new UniqueFameNameException()
            }
        }
        medium nullable: true, validator: { val, obj ->
            if (!val) {
                throw new BlankUserRegistrationTypeException()
            }
        }
        bio nullable: true, validator: { val, obj ->
            if (val) {
                if (val.length() >= Constraints.BIO_MAX_SIZE) {
                    throw new BioMaxLengthException()
                }
            }
        }
        alternativeEmail nullable: true, validator: { val, obj ->
            if (val) {
                if (!val.matches(Constraints.EMAIL_PATTERN)) {
                    throw new ContactEmailPatternException()
                }
            }
        }
        defaultTimeZone nullable: true, validator: { val, obj ->
            if (!val) {
                throw new BlankTimeZoneException()
            }
        }
    }
}
