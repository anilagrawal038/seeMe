package com.famelive.streamManagement.command

import com.famelive.common.slotmanagement.Event
import org.codehaus.groovy.grails.web.json.JSONObject

/**
 * Created by anil on 5/12/14.
 */
class FetchEventTrailerUploadDetailsResponseCO extends ResponseCO {
    String bucketName
    String trailerKey
    String thumbnailKey
    String trailerAccessPermission = Event.fetchTrailerAccessPermission()
    String thumbnailAccessPermission = Event.fetchTrailerThumbnailAccessPermission()
    String s3AccessKey
    String s3SecretKey

    JSONObject toJson() {
        JSONObject jsonObject = new JSONObject()
        jsonObject.put("success", success)
        jsonObject.put("message", message)
        jsonObject.put('status', status)
        JSONObject data = new JSONObject()
        data.put('bucketName', bucketName)
        data.put('trailerKey', trailerKey)
        data.put('thumbnailKey', thumbnailKey)
        data.put('trailerAccessPermission', trailerAccessPermission)
        data.put('thumbnailAccessPermission', thumbnailAccessPermission)
        data.put('s3AccessKey', s3AccessKey)
        data.put('s3SecretKey', s3SecretKey)
        jsonObject.put('data', data)
        return jsonObject
    }
}
