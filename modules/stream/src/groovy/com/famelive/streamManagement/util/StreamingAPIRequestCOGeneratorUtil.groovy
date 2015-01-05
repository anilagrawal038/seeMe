package com.famelive.streamManagement.util

import com.famelive.common.slotmanagement.Event
import com.famelive.streamManagement.enums.APIRequestsDetails
import com.famelive.streamManagement.wowza.command.WowzaRecordLiveStreamRequestCO
import com.famelive.streamManagement.wowza.command.WowzaStopRecordingLiveStreamRequestCO

/**
 * Created by anil on 11/12/14.
 */
class StreamingAPIRequestCOGeneratorUtil {
    public static fetchRecordWowzaLiveStreamRequestCO(Event event) {
        WowzaRecordLiveStreamRequestCO wowzaRecordLiveStreamRequestCO = new WowzaRecordLiveStreamRequestCO()
        wowzaRecordLiveStreamRequestCO.eventId = event.id
        wowzaRecordLiveStreamRequestCO.eventUID = event.eventId
        wowzaRecordLiveStreamRequestCO.actionName = APIRequestsDetails.RECORD_WOWZA_LIVE_STREAM.action
        wowzaRecordLiveStreamRequestCO.init()
        wowzaRecordLiveStreamRequestCO.populateStreamRecorderConfig()
        return wowzaRecordLiveStreamRequestCO
    }

    public static fetchStopRecordingWowzaLiveStreamRequestCO(Event event) {
        WowzaStopRecordingLiveStreamRequestCO wowzaStopRecordingLiveStreamRequestCO = new WowzaStopRecordingLiveStreamRequestCO()
        wowzaStopRecordingLiveStreamRequestCO.eventId = event.id
        wowzaStopRecordingLiveStreamRequestCO.eventUID = event.eventId
        wowzaStopRecordingLiveStreamRequestCO.actionName = APIRequestsDetails.STOP_RECORDING_WOWZA_LIVE_STREAM.action
        wowzaStopRecordingLiveStreamRequestCO.init()
        return wowzaStopRecordingLiveStreamRequestCO
    }
}
