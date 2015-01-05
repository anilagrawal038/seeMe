package com.famelive.streamManagement

import grails.transaction.Transactional

@Transactional
class StreamBootStrapService {
    AWSHelperService streamAWSHelperService

    /*def bootStrapDummyEventData() {
        Event event = new Event(eventUID: "EVENT00000X01", name: "demo event X01", description: "demo event description X01", startTime: DateTimeUtil.addMinutesToDate(new Date(), 15), duration: 30, status: EventStatus.PENDING, user: User.findByEmail("performer1"), genres: [Genre.findById(1)])
        event = event.save()
        //performer
    }*/

    def instantiateAWSHelperService() {
        streamAWSHelperService.init()
    }
}
