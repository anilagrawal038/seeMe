package com.famelive.common.analytics

import com.famelive.common.enums.slotmanagement.EventStatus
import org.apache.commons.lang.builder.HashCodeBuilder

/**
 * Created by anil on 11/12/14.
 */
class EventLiveStreamViewers implements Serializable {
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
    long viewId

    def beforeInsert() {
        List<EventLiveStreamViewers> lastViewId = EventLiveStreamViewers.list([sort: 'viewId', order: 'desc', max: 1])
        if (lastViewId)
            viewId = (lastViewId.pop().viewId as long) + 1 as long
        else
            viewId = 0
    }

    boolean equals(other) {
        boolean isEqual = false
        if (!(other instanceof EventLiveStreamViewers)) {
            isEqual = false
        } else {
            if (userEMail == null || userEMail.length() <= 0) {
                isEqual = other.eventUID == eventUID && other.userSessionID == userSessionID && other.eventStatus == eventStatus
            } else {
                isEqual = other.eventUID == eventUID && other.userEMail == userEMail && other.eventStatus == eventStatus
            }
        }
        return isEqual
    }

    int hashCode() {
        def builder = new HashCodeBuilder()
        builder.append eventUID
        if (userEMail == null || userEMail.length() <= 0) {
            builder.append userSessionID
        } else {
            builder.append userEMail
        }
        builder.append eventStatus
        builder.toHashCode()
    }

    static mapping = { id composite: ['eventUID', 'userSessionID', 'eventStatus'] }

    static constraints = {
        userEMail validator: { userEMail, object ->
            if (userEMail == null) {
                return true
            } else if (EventLiveStreamViewers.findByEventUIDAndEventStatusAndUserEMail(object.eventUID, object.eventStatus, userEMail) == null) {
                return true
            }
            return false
        }
        userIPAddress nullable: true
        userDeviceOSFamily nullable: true
        userDeviceOSProducer nullable: true
        userDeviceCategory nullable: true
        userAgentType nullable: true
        userAgentFamily nullable: true
        userAgentProducer nullable: true
        locale nullable: true
        endTime nullable: true
    }

    static namedQueries = {
        upCommingEventViewers { String eventUID ->
            eq('eventStatus', EventStatus.UP_COMING.toString())
            eq('eventUID', eventUID)
        }
        readyEventViewers { String eventUID ->
            eq('eventStatus', EventStatus.READY.toString())
            eq('eventUID', eventUID)
        }
        liveEventViewers { String eventUID ->
            eq('eventStatus', EventStatus.ON_AIR.toString())
            eq('eventUID', eventUID)
        }
        passedEventViewers { String eventUID ->
            eq('eventStatus', EventStatus.COMPLETED.toString())
            eq('eventUID', eventUID)
        }
    }

}

