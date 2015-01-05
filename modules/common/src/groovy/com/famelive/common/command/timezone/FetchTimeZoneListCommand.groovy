package com.famelive.common.command.timezone

import com.famelive.common.command.AuthenticationTokenCommand
import grails.validation.Validateable

@Validateable
class FetchTimeZoneListCommand extends AuthenticationTokenCommand {

    static constraints = {
    }
}
