package com.famelive.streamManagement.wowza

import com.famelive.common.slotmanagement.Event
import com.famelive.common.streamManagement.WowzaChannel
import com.famelive.streamManagement.wowza.command.WowzaRecordLiveStreamRequestCO
import org.codehaus.groovy.grails.orm.hibernate.cfg.GrailsHibernateUtil
import org.codehaus.groovy.grails.web.json.JSONArray
import org.codehaus.groovy.grails.web.json.JSONObject

/**
 * Created by anil on 28/11/14.
 */
/*

post /v2/servers/{serverName}/vhosts/{vhostName}/applications/{appName}/instances/{instanceName}/streamrecorders/{recorderName}

    Creates a new Stream Recorder and starts recording



http://54.68.108.229:8087/v2/servers/WowzaStreamingEngine/vhosts/_defaultVHost_/applications/ch1/instances/_definst_/streamrecorders/E00000001

 */

class StreamRecorderConfig {

    StreamRecorderConfig(WowzaRecordLiveStreamRequestCO wowzaRecordLiveStreamRequestCO) {
        Event event = Event.findById(wowzaRecordLiveStreamRequestCO.eventId)
        WowzaChannel channel = GrailsHibernateUtil.unwrapIfProxy(event.bookedChannelSlot).channel
        if (event != null) {
            instanceName = wowzaRecordLiveStreamRequestCO.appInstances
            serverName = wowzaRecordLiveStreamRequestCO.serverName
            recorderName = event?.getEventId()
            restURI = wowzaRecordLiveStreamRequestCO.getURL()
            outputPath = channel.contentPath
            applicationName = channel.name
            recorderErrorString = "Recorder " + event?.getEventId() + ' on application ' + channel.name + ' faced some issue'
            version = wowzaRecordLiveStreamRequestCO.wowzaAPIVersion
            baseFile = event?.getEventId() + ".mp4"
        }
    }
    String instanceName = "_definst_"
    String fileVersionDelegateName = "com.wowza.wms.livestreamrecord.manager.StreamRecorderFileVersionDelegate"
    String serverName = "WowzaStreamingEngine"
    String recorderName = "E00000001"
    long currentSize = 0
    String restURI = "http://54.68.108.229:8087/v2/servers/WowzaStreamingEngine/vhosts/_defaultVHost_/applications/ch1/instances/_definst_/streamrecorders/E00000001"
    String segmentSchedule = "0 * * * * *"
    boolean startOnKeyFrame = true
    String outputPath = "/usr/local/WowzaStreamingEngine/content"
    String currentFile = "default.mp4"
    JSONArray saveFieldList = [] as JSONArray
    boolean recordData = false
    String applicationName = "ch1"
    boolean moveFirstVideoFrameToZero = true
    String recorderErrorString = ""
    long segmentSize = 10485760
    boolean defaultRecorder = false
    String version = "v2"
    String baseFile = "E00000001.mp4"
    long segmentDuration = 900000
    String recordingStartTime = ""
    String fileTemplate = '''${BaseFileName}_${RecordingStartTime}_${SegmentNumber}'''
    String segmentationType = "None"
    long currentDuration = 0
    String fileFormat = "MP4"
    String recorderState = "Waiting for stream"
    String option = "Append to existing file"

    JSONObject toJson() {
        JSONObject jsonObject = new JSONObject()
        jsonObject.put('instanceName', instanceName)
        jsonObject.put('fileVersionDelegateName', fileVersionDelegateName)
        jsonObject.put('serverName', serverName)
        jsonObject.put('recorderName', recorderName)
        jsonObject.put('currentSize', currentSize)
        jsonObject.put('restURI', restURI)
        jsonObject.put('segmentSchedule', segmentSchedule)
        jsonObject.put('startOnKeyFrame', startOnKeyFrame)
        jsonObject.put('outputPath', outputPath)
        jsonObject.put('currentFile', currentFile)
        jsonObject.put('saveFieldList', saveFieldList)
        jsonObject.put('recordData', recordData)
        jsonObject.put('applicationName', applicationName)
        jsonObject.put('moveFirstVideoFrameToZero', moveFirstVideoFrameToZero)
        jsonObject.put('recorderErrorString', recorderErrorString)
        jsonObject.put('segmentSize', segmentSize)
        jsonObject.put('defaultRecorder', defaultRecorder)
        jsonObject.put('version', version)
        jsonObject.put('baseFile', baseFile)
        jsonObject.put('segmentDuration', segmentDuration)
        jsonObject.put('recordingStartTime', recordingStartTime)
        jsonObject.put('fileTemplate', fileTemplate)
        jsonObject.put('segmentationType', segmentationType)
        jsonObject.put('currentDuration', currentDuration)
        jsonObject.put('fileFormat', fileFormat)
        jsonObject.put('recorderState', recorderState)
        jsonObject.put('option', option)

        return jsonObject

        /*
        JSON.parse('''
            {
                "instanceName": "_definst_",
                "fileVersionDelegateName": "com.wowza.wms.livestreamrecord.manager.StreamRecorderFileVersionDelegate",
                "serverName": "WowzaStreamingEngine",
                "recorderName": "E00000001",
                "currentSize": 0,
                "restURI": "http://54.68.108.229:8087/v2/servers/WowzaStreamingEngine/vhosts/_defaultVHost_/applications/ch1/instances/_definst_/streamrecorders/E00000001",
                "segmentSchedule": "0 * * * * *",
                "startOnKeyFrame": true,
                "outputPath": "/usr/local/WowzaStreamingEngine/content",
                "currentFile": "default.mp4",
                "saveFieldList": [],
                "recordData": false,
                "applicationName": "h1",
                "moveFirstVideoFrameToZero": true,
                "recorderErrorString": "",
                "segmentSize": 10485760,
                "defaultRecorder": false,
                "version": "v2",
                "baseFile": "E00000001.mp4",
                "segmentDuration": 900000,
                "recordingStartTime": "",
                "fileTemplate": "${BaseFileName}_${RecordingStartTime}_${SegmentNumber}",
                "segmentationType": "None",
                "currentDuration": 0,
                "fileFormat": "MP4",
                "recorderState": "Waiting for stream",
                "option": "Append to existing file"
            }
        ''') as JSONObject
        */
    }

}
