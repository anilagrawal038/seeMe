package com.famelive.common.command.slotmanagement

import com.famelive.common.command.PaginationCommand
import com.famelive.common.enums.usermanagement.EventLocation
import grails.validation.Validateable

@Validateable
class FetchEventsCommand extends PaginationCommand {

    String memberName
    String email
    Date startDateTime
    Date endDateTime
    Long genreId
    EventLocation eventLocation
    String locationName
    String eventTitle

    static constraints={
        max nullable: true
        order nullable: true
        sort nullable: true
        offset nullable: true
    }
}
