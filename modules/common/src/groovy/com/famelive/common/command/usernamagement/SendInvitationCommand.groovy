package com.famelive.common.command.usernamagement

import com.famelive.common.command.AuthenticationTokenCommand
import com.famelive.common.exceptions.BlankEmailException
import grails.validation.Validateable

@Validateable
class SendInvitationCommand extends AuthenticationTokenCommand {

    List<String> emailIds

    static constraints = {
        emailIds nullable: true, validator: { val, obj ->
            if (!val) {
                throw new BlankEmailException()
            } else if (!val.size()) {
                throw new BlankEmailException()
            }
        }
    }
}
