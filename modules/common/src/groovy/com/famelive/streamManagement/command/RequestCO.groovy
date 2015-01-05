package com.famelive.streamManagement.command

import com.famelive.common.enums.RequestType
import com.famelive.common.slotmanagement.Event
import grails.validation.Validateable

/**
 * Created by anil on 16/10/14.
 */

@Validateable
class RequestCO {
    String actionName
    String apiVersion
    String appKey
    RequestType requestMethod
    String contentType
    String clientIP
    boolean _isTestRequest = false
    long eventId = -1
    String eventUID

    void init() {
        if (eventId != -1) {
            eventUID = Event.get(eventId).eventId
        }
        if (eventUID != null) {
            eventId = Event.findByEventId(eventUID).id
        }
    }
}
