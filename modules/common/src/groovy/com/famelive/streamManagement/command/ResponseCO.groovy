package com.famelive.streamManagement.command

import com.famelive.common.constant.streamManagement.CommonStreamingConstants
import org.codehaus.groovy.grails.web.json.JSONObject

/**
 * Created by anil on 1/11/14.
 */
class ResponseCO {
    boolean success
    String message
    int status = CommonStreamingConstants.STREAMING_API_ERROR_CODE

    JSONObject toJson() {
        JSONObject jsonObject = new JSONObject()
        jsonObject.put("success", success)
        jsonObject.put("message", message)
        jsonObject.put("status", status)
//        jsonObject.put("data", )
        return jsonObject
    }
}
