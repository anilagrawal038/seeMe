package com.famelive.common.command.chat

import com.famelive.common.command.AuthenticationTokenCommand
import grails.validation.Validateable

@Validateable
class JoinChatCommand extends AuthenticationTokenCommand {

    Long eventId

    static constraints = {
        eventId nullable: false
    }
}