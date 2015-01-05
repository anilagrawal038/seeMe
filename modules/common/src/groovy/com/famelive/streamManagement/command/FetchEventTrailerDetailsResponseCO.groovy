package com.famelive.streamManagement.command

import com.famelive.common.streamManagement.VideoUrlsDto
import org.codehaus.groovy.grails.web.json.JSONArray
import org.codehaus.groovy.grails.web.json.JSONObject

/**
 * Created by anil on 9/12/14.
 */
class FetchEventTrailerDetailsResponseCO extends ResponseCO {
    List<VideoUrlsDto> trailers = []
    String eventUID

    JSONObject toJson() {
        JSONObject jsonObject = new JSONObject()
        jsonObject.put("success", success)
        jsonObject.put("message", message)
        jsonObject.put('status', status)
        JSONObject data = new JSONObject()
        data.put('eventUID', eventUID)
        JSONArray trailersUrl = new JSONArray()
        trailers.each { trailer ->
            trailersUrl.add(trailer.toJson())
        }
        data.put('trailers', trailersUrl)
        jsonObject.put('data', data)
        return jsonObject
    }
}