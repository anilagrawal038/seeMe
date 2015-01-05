package com.famelive.common.analytics

import org.apache.commons.lang.builder.HashCodeBuilder

/**
 * Created by anil on 15/12/14.
 */

//This class is used to compare two viewers on the basis of email/sessionId and eventUID (and the the event status)

class EventViewers {
    long viewId
    String eventUID
    String userSessionID
    String userIPAddress
    String userEMail
    String userDeviceOSFamily //agent.operatingSystem.familyName
    String userDeviceOSProducer //agent.operatingSystem.producer
    String userDeviceCategory //agent.deviceCategory.name
    String userAgentType //agent.type
    String userAgentFamily //agent.family.name
    String userAgentProducer //agent.producer
    Date startTime
    Date endTime
    String locale
    String eventStatus

    EventViewers(EventLiveStreamViewers eventLiveStreamViewers) {
        this.viewId = eventLiveStreamViewers.viewId
        this.eventUID = eventLiveStreamViewers.eventUID
        this.userSessionID = eventLiveStreamViewers.userSessionID
        this.userIPAddress = eventLiveStreamViewers.userIPAddress
        this.userEMail = eventLiveStreamViewers.userEMail
        this.userDeviceOSFamily = eventLiveStreamViewers.userDeviceOSFamily
        this.userDeviceOSProducer = eventLiveStreamViewers.userDeviceOSProducer
        this.userDeviceCategory = eventLiveStreamViewers.userDeviceCategory
        this.userAgentType = eventLiveStreamViewers.userAgentType
        this.userAgentFamily = eventLiveStreamViewers.userAgentFamily
        this.userAgentProducer = eventLiveStreamViewers.userAgentProducer
        this.startTime = eventLiveStreamViewers.startTime
        this.endTime = eventLiveStreamViewers.endTime
        this.locale = eventLiveStreamViewers.locale
        this.eventStatus = eventLiveStreamViewers.eventStatus
    }

    boolean equals(other) {
        boolean isEqual = false
        if (!(other instanceof EventViewers)) {
            isEqual = false
        } else {
            if (userEMail == null || userEMail.length() <= 0) {
                isEqual = other.eventUID == eventUID && other.userSessionID == userSessionID
            } else {
                isEqual = other.eventUID == eventUID && other.userEMail == userEMail
            }
        }
        return isEqual
    }

    int hashCode() {
        def builder = new HashCodeBuilder()
        builder.append eventUID
//        builder.append userSessionID
        if (userEMail == null || userEMail.length() <= 0) {
            builder.append userSessionID
        } else {
            builder.append userEMail
        }
        builder.toHashCode()
    }

}