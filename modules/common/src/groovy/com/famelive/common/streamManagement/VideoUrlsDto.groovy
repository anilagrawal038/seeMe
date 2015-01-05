package com.famelive.common.streamManagement

import org.codehaus.groovy.grails.web.json.JSONArray
import org.codehaus.groovy.grails.web.json.JSONObject

class VideoUrlsDto {

    List<VideoUrlDetailDto> eventUrls = []
    String videoThumbnail

    VideoUrlsDto() {}

    VideoUrlsDto(List<VideoUrlDetailDto> eventUrlDetailDtoList) {
        this.eventUrls = eventUrlDetailDtoList
    }

    static VideoUrlsDto createCommonResponseDto(List<VideoUrlDetailDto> eventUrlDetailDtoList) {
        return new VideoUrlsDto(eventUrlDetailDtoList)
    }

    JSONObject toJson() {
        JSONObject jsonObject = new JSONObject()
        JSONArray streamUrls = new JSONArray()
        eventUrls.each { eventUrl ->
            streamUrls.add(eventUrl.toJson())
        }
        jsonObject.put('videoThumbnail', videoThumbnail)
        jsonObject.put('streamUrls', streamUrls)
        return jsonObject
    }
}
