package com.famelive.common.command.slotmanagement

import com.famelive.common.command.PaginationCommand
import grails.validation.Validateable

@Validateable
class FetchMostPopularUsersCommand extends PaginationCommand {

    static constraints = {
        max nullable: true
        order nullable: true
        sort nullable: true
        offset nullable: true
    }
}
