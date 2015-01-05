package com.famelive.streamManagement.wowza.command

import com.famelive.common.enums.RequestType
import com.famelive.common.slotmanagement.Event
import com.famelive.common.streamManagement.WowzaChannel
import com.famelive.streamManagement.wowza.StreamRecorderConfig
import org.codehaus.groovy.grails.orm.hibernate.cfg.GrailsHibernateUtil

/**
 * Created by anil on 10/12/14.
 */
class WowzaRecordLiveStreamRequestCO extends WowzaRequestCO {
    {
        requestMethod = RequestType.POST
        contentType = "application/json"
    }
    String streamName //one of eventId or streamName is mandatory
    StreamRecorderConfig streamRecorderConfig

    String getURL() {
        Event event = Event.findById(eventId)
        if (event == null) {
            event = Event.findByEventId(streamName)
        }
        WowzaChannel channel = GrailsHibernateUtil.unwrapIfProxy(event?.bookedChannelSlot)?.channel
        //http://54.68.108.229:8087/v2/servers/WowzaStreamingEngine/vhosts/_defaultVHost_/applications/ch1/instances/_definst_/streamrecorders/E00000001
        return "http://" + serverIP + ":" + serverPort + "/" + wowzaAPIVersion + "/servers/" + serverName + "/vhosts/" + vhostName + "/applications/" + channel.name + "/instances/" + appInstances + '/streamrecorders/' + event.eventId
    }

    void populateStreamRecorderConfig() {
        streamRecorderConfig = new StreamRecorderConfig(this)
    }

    String getBody() {
        return streamRecorderConfig.toJson().toString()
        /*JSONObject body = new JSONObject()
        body.put("restURI", getURL())
        JSONArray streamRecorder = [streamRecorderConfig.toJson()] as JSONArray
        body.put("streamrecorder", streamRecorder)
        body.put("saveFieldList", streamRecorderConfig.saveFieldList)
        body.put("version", streamRecorderConfig.version)
        return body.toString();*/
    }
}
