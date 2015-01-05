package com.famelive.streamManagement

import com.famelive.common.cms.*
import com.famelive.common.enums.cms.MediaCategory
import com.famelive.common.enums.cms.MediaContentImageFormats
import com.famelive.common.enums.cms.MediaContentType
import com.famelive.common.enums.cms.MediaContentVideoFormats
import com.famelive.common.enums.streamManagement.StreamManagementConstantKeys
import com.famelive.common.enums.streamManagement.StreamingProtocols
import com.famelive.common.slotmanagement.Event
import com.famelive.common.slotmanagement.EventTrailer
import com.famelive.common.streamManagement.WowzaChannel
import com.famelive.common.streamManagement.WowzaChannelServerDetails
import com.famelive.common.util.streamManagement.StreamManagementUtil
import grails.transaction.Transactional
import org.codehaus.groovy.grails.commons.GrailsApplication
import org.codehaus.groovy.grails.orm.hibernate.cfg.GrailsHibernateUtil

@Transactional
class EventTrailerManagementService {
    GrailsApplication grailsApplication

    String fetchEventTrailerUrl(Event event, StreamingProtocols streamingProtocols) {
        if (streamingProtocols == null) {
            streamingProtocols = StreamingProtocols.valueOf(StreamManagementUtil.fetchStreamManagementConstantDefaultValue(StreamManagementConstantKeys.DEFAULT_STREAMING_PROTOCOL_FOR_WEB))
        }
        WowzaChannel channel = GrailsHibernateUtil.unwrapIfProxy(event.bookedChannelSlot).channel
        WowzaChannelServerDetails wowzaChannelServerDetails = WowzaChannelServerDetails.findByChannel(channel)
        String urlPath = channel.fetchStreamPath() + "/" + wowzaChannelServerDetails.appInstances + "/" + grailsApplication.config.famelive.wowza.eventTrailerS3Prefix + "/" + grailsApplication.config.famelive.aws.eventTrailerBucketName + "/" + event.fetchTrailerPath() + "/" + event.fetchTrailerName(0)
        String url = "";
        switch (streamingProtocols) {
            case StreamingProtocols.RTMP:
                url = "rtmp://" + urlPath;
                break;
            case StreamingProtocols.RTSP:
                url = "rtsp://" + urlPath;
                break;
            case StreamingProtocols.HLS:
                url = "http://" + urlPath + '/playlist.m3u8';
                break;
        }
        return url
    }

    String fetchEventTrailerThumbnailUrl(Event event) {
        //http://ufame.s3.amazonaws.com/user/FAME0000011/event/E00000001/trailerThumbnail/E00000001_tralier_0_thumbnail.png
        String url = 'http://' + grailsApplication.config.famelive.aws.eventTrailerBucketName + '.' + grailsApplication.config.famelive.aws.url + "/" + event.fetchTrailerThumbnailPath() + "/" + event.fetchTrailerThumbnailName(0)
        return url
    }

    void makeEntriesForEventTrailerUrls(Event event, MediaContent mediaContent) {
        new MediaContentVideoURL(mediaContent: mediaContent, streamingProtocol: StreamingProtocols.RTMP, resolution: 0, url: fetchEventTrailerUrl(event, StreamingProtocols.RTMP)).save(flush: true, failOnError: true)
        new MediaContentVideoURL(mediaContent: mediaContent, streamingProtocol: StreamingProtocols.HLS, resolution: 0, url: fetchEventTrailerUrl(event, StreamingProtocols.HLS)).save(flush: true, failOnError: true)
        new MediaContentVideoURL(mediaContent: mediaContent, streamingProtocol: StreamingProtocols.RTSP, resolution: 0, url: fetchEventTrailerUrl(event, StreamingProtocols.RTSP)).save(flush: true, failOnError: true)
    }

    void makeEntriesForEventTrailerThumbnailUrls(Event event, MediaContent mediaContent) {
        new MediaContentImageURL(mediaContent: mediaContent, url: fetchEventTrailerThumbnailUrl(event)).save(flush: true, failOnError: true)
    }

    @Transactional
    boolean makeEntryForEventTrailer(Event event, long trailerSize) {
        boolean status = false
        try {
            //We are asuming only one trailer possible at a time at index 0
            EventTrailer eventTeasers = EventTrailer.findByEvent(event)
            if (eventTeasers == null) {
                eventTeasers = new EventTrailer(event: event)
                MediaContent mediaContent = new MediaContent(name: event.fetchTrailerName(0), title: 'Trailer of ' + event.name, description: 'Trailer of ' + event.name, mediaContentType: MediaContentType.VIDEO, mediaCategory: MediaCategory.TRAILER_TEASER).save(flush: true, failOnError: true)
                new MediaContentAccessDetails(mediaContent: mediaContent, bucketName: grailsApplication.config.famelive.aws.eventTrailerBucketName, folderPath: event.fetchTrailerPath(), fileName: event.fetchTrailerName(0), securityPolicy: Event.fetchTrailerAccessPermission()).save(flush: true, failOnError: true)
                new MediaContentInfo(mediaContent: mediaContent, uploadedBy: event.user).save(flush: true, failOnError: true)
                new MediaContentVideoTechSpecs(mediaContent: mediaContent, size: trailerSize, videoType: MediaContentVideoFormats.MP4)
                makeEntriesForEventTrailerUrls(event, mediaContent)
                eventTeasers.addToTrailers(mediaContent)
                eventTeasers.save(flush: true, failOnError: true)
            }
            status = true
        } catch (Exception e) {
            println 'Exception occurred when making entry for event trailer :' + event.name + ' exp:' + e
        }
        return status
    }


    @Transactional
    boolean makeEntryForEventTrailerThumbnail(Event event, long trailerThumbnailSize) {
        boolean status = false
        try {
            MediaContent trailer = MediaContent.findByName(event.fetchTrailerName(0))
            if (trailer.thumbnail == null) {
                MediaContent mediaContent = new MediaContent(name: event.fetchTrailerThumbnailName(0), title: 'Trailer thumbnail of ' + event.name, description: 'Trailer thumbnail of ' + event.name, mediaContentType: MediaContentType.IMAGE, mediaCategory: MediaCategory.TRAILER_TEASER).save(flush: true, failOnError: true)
                new MediaContentAccessDetails(mediaContent: mediaContent, bucketName: grailsApplication.config.famelive.aws.eventTrailerBucketName, folderPath: event.fetchTrailerThumbnailPath(), fileName: event.fetchTrailerThumbnailName(0), securityPolicy: Event.fetchTrailerThumbnailAccessPermission()).save(flush: true, failOnError: true)
                new MediaContentInfo(mediaContent: mediaContent, uploadedBy: event.user).save(flush: true, failOnError: true)
                new MediaContentImageTechSpecs(mediaContent: mediaContent, size: trailerThumbnailSize, imageType: MediaContentImageFormats.PNG)
                makeEntriesForEventTrailerThumbnailUrls(event, mediaContent)
                if (trailer != null) {
                    trailer.setThumbnail(mediaContent)
                    trailer.save(flush: true, failOnError: true)
                    status = true
                }
            } else {
                status = true
            }
        } catch (Exception e) {
            println 'Exception occurred when making entry for event trailer thumbnail:' + event.name + ' exp:' + e
        }
        return status
    }

}
