package com.famelive.common.streamManagement

import org.codehaus.groovy.grails.web.json.JSONObject

class VideoUrlDetailDto {

    String resolution
    String url

    JSONObject toJson() {
        JSONObject jsonObject = new JSONObject()
        jsonObject.put('resolution', resolution)
        jsonObject.put('url', url)
        return jsonObject
    }
}
