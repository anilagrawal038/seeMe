package com.famelive.common.command.timezone

import com.famelive.common.command.AuthenticationTokenCommand
import grails.validation.Validateable

@Validateable
class FetchCountryListCommand extends AuthenticationTokenCommand {

    static constraints = {
    }
}
