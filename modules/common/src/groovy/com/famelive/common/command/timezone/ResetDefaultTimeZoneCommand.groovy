package com.famelive.common.command.timezone

import com.famelive.common.command.AuthenticationTokenCommand
import com.famelive.common.exceptions.BlankTimeZoneException
import grails.validation.Validateable

@Validateable
class ResetDefaultTimeZoneCommand extends AuthenticationTokenCommand {

    String timeZone

    static constraints = {
        timeZone nullable: true, validator: { val, obj ->
            if (!val) {
                throw new BlankTimeZoneException()
            }
        }
    }
}
