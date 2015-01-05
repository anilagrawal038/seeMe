package com.famelive.common.analytics

import com.famelive.common.slotmanagement.Event
import com.famelive.common.user.User
import com.famelive.common.util.FameLiveUtil
import net.sf.uadetector.ReadableUserAgent
import net.sf.uadetector.UserAgentStringParser
import net.sf.uadetector.service.UADetectorServiceFactory
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import javax.servlet.http.HttpServletRequest

/**
 * Created by anil on 12/12/14.
 */
class EventLiveStreamViewerMonitorService {
    private static final Log log = LogFactory.getLog(this)
    UserAgentStringParser parser = UADetectorServiceFactory.getResourceModuleParser();

    void makeEntryForEventLiveStreamViewer(HttpServletRequest request, long eventId, long userId) {
        Event event = Event.get(eventId)
        User user = User.get(userId)
        if (request == null || event == null) {
            return
        } else {
            // Get an UserAgentStringParser and analyze the requesting client
            ReadableUserAgent agent = parser.parse(request.getHeader("User-Agent"));
            EventLiveStreamViewers eventLiveStreamViewers = new EventLiveStreamViewers()
            try {
                eventLiveStreamViewers.userSessionID = request?.session?.id
                eventLiveStreamViewers.userIPAddress = request?.getRemoteAddr()
                eventLiveStreamViewers.locale = request?.locale?.displayName
                eventLiveStreamViewers.eventUID = event?.eventId
                eventLiveStreamViewers.userEMail = user?.email
                eventLiveStreamViewers.userDeviceOSFamily = agent?.operatingSystem?.familyName
                eventLiveStreamViewers.userDeviceOSProducer = agent?.operatingSystem?.producer
                eventLiveStreamViewers.userDeviceCategory = agent?.deviceCategory?.name
                eventLiveStreamViewers.userAgentType = agent?.type
                eventLiveStreamViewers.userAgentFamily = agent?.family?.name
                eventLiveStreamViewers.userAgentProducer = agent?.producer
                eventLiveStreamViewers.startTime = FameLiveUtil.fetchCurrentTime()
                eventLiveStreamViewers.eventStatus = event?.status
                eventLiveStreamViewers.save(flush: true, failOnError: true)
            } catch (Exception e) {
                log.warn('Exception occurred when making entry for eventLiveStreamViewer, exp:' + e)
                println 'Exception occurred when making entry for eventLiveStreamViewer, exp:' + e;
            }
        }
    }

    List<EventLiveStreamViewers> findAllLiveOrReadyEventLiveStreamViewers(long eventId) {
        List<EventLiveStreamViewers> eventLiveStreamViewersList = []
        Event event = Event.get(eventId)
        if (event == null) {
            //Do nothing
        } else {
            Set<EventViewers> eventLiveStreamViewersSet = []
            String eventUID = event.eventId
            Set<EventLiveStreamViewers> readyEventLiveStreamViewersSet = EventLiveStreamViewers.readyEventViewers(eventUID).list()
            readyEventLiveStreamViewersSet.each { readyEventLiveStreamViewers ->
                eventLiveStreamViewersSet.add(new EventViewers(readyEventLiveStreamViewers))
            }
            Set<EventLiveStreamViewers> liveEventLiveStreamViewersSet = EventLiveStreamViewers.liveEventViewers(eventUID).list()
            liveEventLiveStreamViewersSet.each { liveEventLiveStreamViewers ->
                eventLiveStreamViewersSet.add(new EventViewers(liveEventLiveStreamViewers))
            }
            eventLiveStreamViewersSet.each { eventLiveStreamViewers ->
                eventLiveStreamViewersList.add(EventLiveStreamViewers.findByViewId(eventLiveStreamViewers.viewId))
            }
        }
        return eventLiveStreamViewersList
    }

    int fetchLiveOrReadyEventLiveStreamViewersCount(long eventId) {
        int viewersCount = -1;
        Event event = Event.get(eventId)
        if (event == null) {
            //Do nothing
            viewersCount = 0
        } else {
            Set<EventViewers> eventLiveStreamViewersSet = []
            String eventUID = event.eventId
            Set<EventLiveStreamViewers> readyEventLiveStreamViewersSet = EventLiveStreamViewers.readyEventViewers(eventUID).list()
            readyEventLiveStreamViewersSet.each { readyEventLiveStreamViewers ->
                eventLiveStreamViewersSet.add(new EventViewers(readyEventLiveStreamViewers))
            }
            Set<EventLiveStreamViewers> liveEventLiveStreamViewersSet = EventLiveStreamViewers.liveEventViewers(eventUID).list()
            liveEventLiveStreamViewersSet.each { liveEventLiveStreamViewers ->
                eventLiveStreamViewersSet.add(new EventViewers(liveEventLiveStreamViewers))
            }
            viewersCount = eventLiveStreamViewersSet.size()
        }
        return viewersCount
    }
}
