package com.famelive.streamManagement

import com.famelive.streamManagement.admin.StreamingAdminService
import com.famelive.streamManagement.admin.dto.ChannelDetailsDTO
import com.famelive.streamManagement.admin.dto.ChannelInfoDTO
import com.famelive.streamManagement.admin.dto.LiveStreamDetailsDTO
import grails.plugin.springsecurity.SpringSecurityService
import grails.plugin.springsecurity.annotation.Secured
import org.codehaus.groovy.grails.commons.DefaultGrailsApplication
import org.springframework.security.core.userdetails.UserDetailsService


//@Secured(['permitAll'])
class StreamingAdminController {
    DefaultGrailsApplication grailsApplication
    UserDetailsService userDetailsService
    SpringSecurityService springSecurityService
    StreamingAdminService streamingAdminService

    @Secured(['ROLE_SUPER_ADMIN'])
    def index() {
        //@Secured(['ROLE_SUPER_ADMIN'])
        println "admin page"
//        render "abcd"
        return redirect(controller: 'streamingAdmin', action: 'fetchChannels')
    }

    @Secured(['ROLE_SUPER_ADMIN'])
    def fetchChannels() {
        List<ChannelInfoDTO> channelInfoDTOs = streamingAdminService.fetchChannels()
        render(view: 'fetchChannels', model: [channelInfoDTOs: channelInfoDTOs])
    }

    @Secured(['ROLE_SUPER_ADMIN'])
    def fetchChannelDetails() {
        ChannelDetailsDTO channelDetailsDTO = streamingAdminService.fetchChannelDetails(Long.parseLong(params.channelId))
        render(view: 'fetchChannelDetails', model: [channelDetailsDTO: channelDetailsDTO])
    }

    @Secured(['ROLE_SUPER_ADMIN'])
    def fetchBookedSlotsForChannel(long channelId) {
    }

    @Secured(['ROLE_SUPER_ADMIN'])
    def fetchLiveStreamDetails(long eventStreamInfoId) {

    }

    @Secured(['ROLE_SUPER_ADMIN'])
    def fetchLiveStreamDetailsForChannel(long channelId) {
        LiveStreamDetailsDTO liveStreamDetailsDTO = streamingAdminService.fetchLiveStreamDetailsForChannel(Long.parseLong(params.channelId))
        render(view: 'fetchLiveStreamDetailsForChannel', model: [liveStreamDetailsDTO: liveStreamDetailsDTO])
    }
}

