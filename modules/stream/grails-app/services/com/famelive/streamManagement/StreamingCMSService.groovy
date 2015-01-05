package com.famelive.streamManagement

import com.famelive.common.cms.MediaContent
import com.famelive.common.cms.MediaContentImageURL
import com.famelive.common.cms.MediaContentVideoURL
import com.famelive.common.enums.streamManagement.StreamingProtocols
import com.famelive.common.streamManagement.VideoUrlDetailDto
import com.famelive.common.streamManagement.VideoUrlsDto
import grails.transaction.Transactional

@Transactional
class StreamingCMSService {

    VideoUrlsDto fetchVideoUrls(MediaContent mediaContent, StreamingProtocols streamingProtocols) {
        List<MediaContentVideoURL> mediaContentVideoURLs = MediaContentVideoURL.findAllByMediaContentAndStreamingProtocol(mediaContent, streamingProtocols)
        VideoUrlsDto eventUrlsDto = new VideoUrlsDto()
        eventUrlsDto.eventUrls = []
        if (mediaContentVideoURLs != null && mediaContentVideoURLs.size() > 0) {
            mediaContentVideoURLs.each { mediaContentVideoURL ->
                eventUrlsDto.eventUrls.add(new VideoUrlDetailDto(resolution: mediaContentVideoURL.resolution, url: mediaContentVideoURL.url))
            }
        }
        eventUrlsDto.videoThumbnail = MediaContentImageURL.findByMediaContent(mediaContent.thumbnail).url
        return eventUrlsDto
    }
}
