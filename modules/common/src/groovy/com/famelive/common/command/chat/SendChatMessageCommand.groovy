package com.famelive.common.command.chat

import com.famelive.common.command.AuthenticationTokenCommand
import com.famelive.common.exceptions.chat.BlankMessageException
import com.famelive.common.exceptions.chat.BlankMessageIdException
import com.famelive.common.exceptions.chat.BlankTimeStampException
import com.famelive.common.exceptions.slotmanagement.BlankEventIdException
import com.famelive.common.exceptions.slotmanagement.EventNotFoundException
import com.famelive.common.slotmanagement.Event
import grails.validation.Validateable

@Validateable
class SendChatMessageCommand extends AuthenticationTokenCommand {

    Long eventId
    String message
    String messageId
    long timeStamp

    static constraints = {

        eventId nullable: true, validator: { val, obj ->
            if (!val) {
                throw new BlankEventIdException()
            } else if (!Event.get(val)) {
                throw new EventNotFoundException()
            }
        }
        message nullable: true, validator: { val, obj ->
            if (!val) {
                throw new BlankMessageException()
            }
        }
        messageId nullable: true, validator: { val, obj ->
            if (!val) {
                throw new BlankMessageIdException()
            }
        }
        timeStamp nullable: true, validator: { val, obj ->
            if (!val) {
                throw new BlankTimeStampException()
            }
        }
    }
}