package com.famelive.streamManagement.wowza.command

import com.famelive.common.enums.RequestType
import com.famelive.common.slotmanagement.Event
import com.famelive.common.streamManagement.WowzaChannel
import org.codehaus.groovy.grails.orm.hibernate.cfg.GrailsHibernateUtil

/**
 * Created by anil on 11/12/14.
 */
class WowzaStopRecordingLiveStreamRequestCO extends WowzaRequestCO {
    {
        requestMethod = RequestType.PUT
        contentType = "application/json"
    }
    long eventId //mandatory
    String recordingAction = 'stopRecording'  //available options['stopRecording','splitRecording']

    String getURL() {

        Event event = Event.findById(eventId)
        WowzaChannel channel = GrailsHibernateUtil.unwrapIfProxy(event?.bookedChannelSlot)?.channel
        //http://54.68.108.229:8087/v2/servers/_defaultServer_/vhosts/{vhostName}/applications/{appName}/instances/{instanceName}/streamrecorders/{recorderName}/actions/stopRecording
        return "http://" + serverIP + ":" + serverPort + "/" + wowzaAPIVersion + "/servers/" + serverName + "/vhosts/" + vhostName + "/applications/" + channel.name + "/instances/" + appInstances + '/streamrecorders/' + event.eventId + '/actions/' + recordingAction
    }
}
