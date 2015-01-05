package com.famelive.streamManagement.command

import org.codehaus.groovy.grails.web.json.JSONObject

/**
 * Created by anil on 8/12/14.
 */
class CheckIsEventTrailerUploadedSuccessfullyResponseCO extends ResponseCO {
    long trailerFileSize
    long trailerThumbnailFileSize

    JSONObject toJson() {
        JSONObject jsonObject = new JSONObject()
        jsonObject.put("success", success)
        jsonObject.put("message", message)
        jsonObject.put('status', status)
        JSONObject data = new JSONObject()
        data.put('trailerFileSize', trailerFileSize)
        data.put('trailerThumbnailFileSize', trailerThumbnailFileSize)
        jsonObject.put('data', data)
        return jsonObject
    }
}
