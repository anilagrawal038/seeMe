package com.famelive.streamManagement

import com.famelive.common.callBroker.RequestCallBrokerAdapter
import com.famelive.common.enums.usermanagement.UserRoles
import com.famelive.streamManagement.api.StreamingAPISecurityService
import com.famelive.streamManagement.command.LoginRequestCO
import com.famelive.streamManagement.command.*
import com.famelive.streamManagement.wowza.command.*
import grails.plugin.springsecurity.SpringSecurityService
import grails.plugin.springsecurity.annotation.Secured
import groovy.json.JsonSlurper
import org.codehaus.groovy.grails.web.json.JSONObject

import javax.management.relation.RoleList
import javax.servlet.http.HttpServletRequest
import java.nio.file.AccessDeniedException

//@Transactional
class APIV1Service implements APIService {
    RequestCallBrokerAdapter requestCallBrokerAdapter
    SchedulerHelperService schedulerHelperService
    StreamingAPIHelperService streamingAPIHelperService
    SpringSecurityService springSecurityService
    StreamingAPISecurityService streamingAPISecurityService

    ResponseCO login(JSONObject json, HttpServletRequest request) {
        def jsonSlurper = new JsonSlurper().parseText(json.toString())
        LoginRequestCO apiLoginRequestCO = new LoginRequestCO(jsonSlurper)
        apiLoginRequestCO.request = request
        streamingAPIHelperService.login(apiLoginRequestCO)
    }

    ResponseCO register(JSONObject json, HttpServletRequest request) {
        def jsonSlurper = new JsonSlurper().parseText(json.toString())
        RegisterUserRequestCO registerUserRequestCO = new RegisterUserRequestCO(jsonSlurper)
        registerUserRequestCO.request = request
        streamingAPIHelperService.register(registerUserRequestCO)
    }

    ResponseCO verifyAccount(JSONObject json, HttpServletRequest request) {
        def jsonSlurper = new JsonSlurper().parseText(json.toString())
        VerifyAccountRequestCO verifyAccountRequestCO = new VerifyAccountRequestCO(jsonSlurper)
        verifyAccountRequestCO.request = request
        streamingAPIHelperService.verifyAccount(verifyAccountRequestCO)
    }

    ResponseCO forgotPassword(JSONObject json, HttpServletRequest request) {
        def jsonSlurper = new JsonSlurper().parseText(json.toString())
        ForgotPasswordRequestCO forgotPasswordRequestCO = new ForgotPasswordRequestCO(jsonSlurper)
        forgotPasswordRequestCO.request = request
        streamingAPIHelperService.forgotPassword(forgotPasswordRequestCO)
    }
    ResponseCO resendVerificationToken(JSONObject json, HttpServletRequest request) {
        def jsonSlurper = new JsonSlurper().parseText(json.toString())
        ResendVerificationTokenRequestCO resendVerificationTokenRequestCO = new ResendVerificationTokenRequestCO(jsonSlurper)
        resendVerificationTokenRequestCO.request = request
        streamingAPIHelperService.resendVerificationToken(resendVerificationTokenRequestCO)
    }

    ResponseCO changePassword(JSONObject json, HttpServletRequest request) {
        ResponseCO responseCO = null
        if (streamingAPISecurityService.isUserHasRole(UserRoles.USER)) {
            def jsonSlurper = new JsonSlurper().parseText(json.toString())
            ChangePasswordRequestCO changePasswordRequestCO = new ChangePasswordRequestCO(jsonSlurper)
            changePasswordRequestCO.request = request
            responseCO = streamingAPIHelperService.changePassword(changePasswordRequestCO)
        } else {
            throw AccessDeniedException()
        }
        return responseCO
    }

    ResponseCO logout(JSONObject json, HttpServletRequest request) {
        ResponseCO responseCO = null
        if (streamingAPISecurityService.isUserHasRole(UserRoles.USER)) {
            def jsonSlurper = new JsonSlurper().parseText(json.toString())
            LogoutRequestCO logoutRequestCO = new LogoutRequestCO(jsonSlurper)
            logoutRequestCO.request=request
            responseCO = streamingAPIHelperService.logout(logoutRequestCO)
        } else {
            throw AccessDeniedException()
        }
        return responseCO
    }

    ResponseCO getWowzaApplications(JSONObject json, HttpServletRequest request) {
        ResponseCO responseCO = null
        if (streamingAPISecurityService.isUserHasRole(UserRoles.SUPER_ADMIN)) {
            def jsonSlurper = new JsonSlurper().parseText(json.toString())
            WowzaGetApplicationsRequestCO wowzaGetApplicationsRequestCO = new WowzaGetApplicationsRequestCO(jsonSlurper)
            wowzaGetApplicationsRequestCO.init()
            responseCO = streamingAPIHelperService.getWowzaApplications(wowzaGetApplicationsRequestCO)
        } else {
            throw AccessDeniedException()
        }
        return responseCO
    }

    ResponseCO addWowzaPublisher(JSONObject json, HttpServletRequest request) {
        ResponseCO responseCO = null
        if (streamingAPISecurityService.isUserHasRole(UserRoles.SUPER_ADMIN)) {
            def jsonSlurper = new JsonSlurper().parseText(json.toString())
            WowzaAddPublisherRequestCO wowzaAddPublisherRequestCO = new WowzaAddPublisherRequestCO(jsonSlurper)
            wowzaAddPublisherRequestCO.init()
            responseCO = streamingAPIHelperService.addWowzaPublisher(wowzaAddPublisherRequestCO)
        } else {
            throw AccessDeniedException()
        }
        return responseCO

    }

    ResponseCO removeWowzaPublisher(JSONObject json, HttpServletRequest request) {
        ResponseCO responseCO = null
        if (streamingAPISecurityService.isUserHasRole(UserRoles.SUPER_ADMIN)) {
            def jsonSlurper = new JsonSlurper().parseText(json.toString())
            WowzaRemovePublisherRequestCO wowzaRemovePublisherRequestCO = new WowzaRemovePublisherRequestCO(jsonSlurper)
            wowzaRemovePublisherRequestCO.init()
            responseCO = streamingAPIHelperService.removeWowzaPublisher(wowzaRemovePublisherRequestCO)
        } else {
            throw AccessDeniedException()
        }
        return responseCO

    }

    ResponseCO changeEventStateToReady(JSONObject json, HttpServletRequest request) {
        ResponseCO responseCO = null
        if (streamingAPISecurityService.isUserHasRole(UserRoles.SUPER_ADMIN)) {
            def jsonSlurper = new JsonSlurper().parseText(json.toString())
            ChangeEventStateRequestCO changeEventStateRequestCO = new ChangeEventStateRequestCO(jsonSlurper)
            responseCO = streamingAPIHelperService.changeEventStateToReady(changeEventStateRequestCO)
        } else {
            throw AccessDeniedException()
        }
        return responseCO

    }

    ResponseCO changeEventStateToPaused(JSONObject json, HttpServletRequest request) {
        ResponseCO responseCO = null
        if (streamingAPISecurityService.isUserHasRole(UserRoles.SUPER_ADMIN)) {
            def jsonSlurper = new JsonSlurper().parseText(json.toString())
            ChangeEventStateRequestCO changeEventStateRequestCO = new ChangeEventStateRequestCO(jsonSlurper)
            responseCO = streamingAPIHelperService.changeEventStateToPaused(changeEventStateRequestCO)
        } else {
            throw AccessDeniedException()
        }
        return responseCO

    }

    ResponseCO changeEventStateToComplete(JSONObject json, HttpServletRequest request) {
        ResponseCO responseCO = null
        if (streamingAPISecurityService.isUserHasRole(UserRoles.SUPER_ADMIN)) {
            def jsonSlurper = new JsonSlurper().parseText(json.toString())
            ChangeEventStateRequestCO changeEventStateRequestCO = new ChangeEventStateRequestCO(jsonSlurper)
            responseCO = streamingAPIHelperService.changeEventStateToComplete(changeEventStateRequestCO)
        } else {
            throw AccessDeniedException()
        }
        return responseCO

    }

    ResponseCO changeEventStateToLive(JSONObject json, HttpServletRequest request) {
        ResponseCO responseCO = null
        if (streamingAPISecurityService.isUserHasRole(UserRoles.SUPER_ADMIN)) {
            def jsonSlurper = new JsonSlurper().parseText(json.toString())
            ChangeEventStateRequestCO changeEventStateRequestCO = new ChangeEventStateRequestCO(jsonSlurper)
            responseCO = streamingAPIHelperService.changeEventStateToLive(changeEventStateRequestCO)
        } else {
            throw AccessDeniedException()
        }
        return responseCO

    }

    ResponseCO fetchInStreamCredentials(JSONObject json, HttpServletRequest request) {
        ResponseCO responseCO = null
        if (streamingAPISecurityService.isUserHasRole(UserRoles.SUPER_ADMIN)) {
            def jsonSlurper = new JsonSlurper().parseText(json.toString())
            FetchStreamCredentialsRequestCO fetchStreamCredentialsRequestCO = new FetchStreamCredentialsRequestCO(jsonSlurper)
            responseCO = streamingAPIHelperService.fetchInStreamCredentials(fetchStreamCredentialsRequestCO)
        } else {
            throw AccessDeniedException()
        }
        return responseCO

    }

    ResponseCO fetchOutStreamCredentials(JSONObject json, HttpServletRequest request) {
        ResponseCO responseCO = null
        if (streamingAPISecurityService.isUserHasRole(UserRoles.SUPER_ADMIN)) {
            def jsonSlurper = new JsonSlurper().parseText(json.toString())
            FetchStreamCredentialsRequestCO fetchStreamCredentialsRequestCO = new FetchStreamCredentialsRequestCO(jsonSlurper)
            responseCO = streamingAPIHelperService.fetchOutStreamCredentials(fetchStreamCredentialsRequestCO)
        } else {
            throw AccessDeniedException()
        }
        return responseCO

    }

    ResponseCO startWowzaApplication(JSONObject json, HttpServletRequest request) {
        ResponseCO responseCO = null
        if (streamingAPISecurityService.isUserHasRole(UserRoles.SUPER_ADMIN)) {
            def jsonSlurper = new JsonSlurper().parseText(json.toString())
            WowzaStartApplicationRequestCO wowzaStartApplicationRequestCO = new WowzaStartApplicationRequestCO(jsonSlurper)
            wowzaStartApplicationRequestCO.init()
            responseCO = streamingAPIHelperService.startWowzaApplication(wowzaStartApplicationRequestCO)
        } else {
            throw AccessDeniedException()
        }
        return responseCO

    }

    ResponseCO stopWowzaApplication(JSONObject json, HttpServletRequest request) {
        ResponseCO responseCO = null
        if (streamingAPISecurityService.isUserHasRole(UserRoles.SUPER_ADMIN)) {
            def jsonSlurper = new JsonSlurper().parseText(json.toString())
            WowzaStopApplicationRequestCO wowzaStopApplicationRequestCO = new WowzaStopApplicationRequestCO(jsonSlurper)
            wowzaStopApplicationRequestCO.init()
            responseCO = streamingAPIHelperService.stopWowzaApplication(wowzaStopApplicationRequestCO)
        } else {
            throw AccessDeniedException()
        }
        return responseCO

    }

    ResponseCO restartWowzaApplication(JSONObject json, HttpServletRequest request) {
        ResponseCO responseCO = null
        if (streamingAPISecurityService.isUserHasRole(UserRoles.SUPER_ADMIN)) {
            def jsonSlurper = new JsonSlurper().parseText(json.toString())
            WowzaRestartApplicationRequestCO wowzaRestartApplicationRequestCO = new WowzaRestartApplicationRequestCO(jsonSlurper)
            wowzaRestartApplicationRequestCO.init()
            responseCO = streamingAPIHelperService.restartWowzaApplication(wowzaRestartApplicationRequestCO)
        } else {
            throw AccessDeniedException()
        }
        return responseCO

    }

    ResponseCO fetchAdvanceWowzaApplicationConfiguration(JSONObject json, HttpServletRequest request) {
        ResponseCO responseCO = null
        if (streamingAPISecurityService.isUserHasRole(UserRoles.SUPER_ADMIN)) {
            def jsonSlurper = new JsonSlurper().parseText(json.toString())
            WowzaFetchAdvanceApplicationConfigurationRequestCO wowzaFetchAdvanceApplicationConfigurationRequestCO = new WowzaFetchAdvanceApplicationConfigurationRequestCO(jsonSlurper)
            wowzaFetchAdvanceApplicationConfigurationRequestCO.init()
            responseCO = streamingAPIHelperService.fetchAdvanceWowzaApplicationConfiguration(wowzaFetchAdvanceApplicationConfigurationRequestCO)
        } else {
            throw AccessDeniedException()
        }
        return responseCO

    }

    ResponseCO updateLoopUntilLiveSourceStreamsWowzaApplicationModule(JSONObject json, HttpServletRequest request) {
        ResponseCO responseCO = null
        if (streamingAPISecurityService.isUserHasRole(UserRoles.SUPER_ADMIN)) {
            def jsonSlurper = new JsonSlurper().parseText(json.toString())
            WowzaUpdateApplicationModuleRequestCO wowzaUpdateApplicationModuleRequestCO = new WowzaUpdateApplicationModuleRequestCO(jsonSlurper)
            wowzaUpdateApplicationModuleRequestCO.init()
            responseCO = streamingAPIHelperService.updateLoopUntilLiveSourceStreamsWowzaApplicationModule(wowzaUpdateApplicationModuleRequestCO)
        } else {
            throw AccessDeniedException()
        }
        return responseCO

    }

    ResponseCO fetchWowzaIncomingStreamInfo(JSONObject json, HttpServletRequest request) {
        ResponseCO responseCO = null
        if (streamingAPISecurityService.isUserHasRole(UserRoles.SUPER_ADMIN)) {
            def jsonSlurper = new JsonSlurper().parseText(json.toString())
            WowzaFetchInStreamInfoRequestCO wowzaFetchInStreamInfoRequestCO = new WowzaFetchInStreamInfoRequestCO(jsonSlurper)
            wowzaFetchInStreamInfoRequestCO.init()
            responseCO = streamingAPIHelperService.fetchWowzaIncomingStreamInfo(wowzaFetchInStreamInfoRequestCO)
        } else {
            throw AccessDeniedException()
        }
        return responseCO

    }

    ResponseCO fetchWowzaOutgoingStreamInfo(JSONObject json, HttpServletRequest request) {
        ResponseCO responseCO = null
        if (streamingAPISecurityService.isUserHasRole(UserRoles.SUPER_ADMIN)) {
            def jsonSlurper = new JsonSlurper().parseText(json.toString())
            WowzaFetchOutStreamInfoRequestCO wowzaFetchOutStreamInfoRequestCO = new WowzaFetchOutStreamInfoRequestCO(jsonSlurper)
            wowzaFetchOutStreamInfoRequestCO.init()
            responseCO = streamingAPIHelperService.fetchWowzaOutgoingStreamInfo(wowzaFetchOutStreamInfoRequestCO)
        } else {
            throw AccessDeniedException()
        }
        return responseCO

    }

    ResponseCO fetchWowzaIncomingStreamStatistics(JSONObject json, HttpServletRequest request) {
        ResponseCO responseCO = null
        if (streamingAPISecurityService.isUserHasRole(UserRoles.SUPER_ADMIN)) {
            def jsonSlurper = new JsonSlurper().parseText(json.toString())
            WowzaFetchInStreamStatisticsRequestCO wowzaFetchInStreamStatisticsRequestCO = new WowzaFetchInStreamStatisticsRequestCO(jsonSlurper)
            wowzaFetchInStreamStatisticsRequestCO.init()
            responseCO = streamingAPIHelperService.fetchWowzaIncomingStreamStatistics(wowzaFetchInStreamStatisticsRequestCO)
        } else {
            throw AccessDeniedException()
        }
        return responseCO

    }

    ResponseCO resetWowzaIncomingStream(JSONObject json, HttpServletRequest request) {
        ResponseCO responseCO = null
        if (streamingAPISecurityService.isUserHasRole(UserRoles.SUPER_ADMIN)) {
            def jsonSlurper = new JsonSlurper().parseText(json.toString())
            WowzaDoActionOnInStreamRequestCO wowzaDoActionOnInStreamRequestCO = new WowzaDoActionOnInStreamRequestCO(jsonSlurper)
            wowzaDoActionOnInStreamRequestCO.init()
            responseCO = streamingAPIHelperService.resetWowzaIncomingStream(wowzaDoActionOnInStreamRequestCO)
        } else {
            throw AccessDeniedException()
        }
        return responseCO

    }

    ResponseCO disconnectWowzaIncomingStream(JSONObject json, HttpServletRequest request) {
        ResponseCO responseCO = null
        if (streamingAPISecurityService.isUserHasRole(UserRoles.SUPER_ADMIN)) {
            def jsonSlurper = new JsonSlurper().parseText(json.toString())
            WowzaDoActionOnInStreamRequestCO wowzaDoActionOnInStreamRequestCO = new WowzaDoActionOnInStreamRequestCO(jsonSlurper)
            wowzaDoActionOnInStreamRequestCO.init()
            responseCO = streamingAPIHelperService.disconnectWowzaIncomingStream(wowzaDoActionOnInStreamRequestCO)
        } else {
            throw AccessDeniedException()
        }
        return responseCO

    }

    ResponseCO fetchEventTrailerUploadDetails(JSONObject json, HttpServletRequest request) {
        ResponseCO responseCO = null
        if (streamingAPISecurityService.isUserHasRole(UserRoles.SUPER_ADMIN)) {
            def jsonSlurper = new JsonSlurper().parseText(json.toString())
            FetchEventTrailerUploadDetailsRequestCO fetchEventTrailerUploadDetailsRequestCO = new FetchEventTrailerUploadDetailsRequestCO(jsonSlurper)
            responseCO = streamingAPIHelperService.fetchEventTrailerUploadDetails(fetchEventTrailerUploadDetailsRequestCO)
        } else {
            throw AccessDeniedException()
        }
        return responseCO

    }

    ResponseCO checkIsEventTrailerUploadedSuccessfully(JSONObject json, HttpServletRequest request) {
        ResponseCO responseCO = null
        if (streamingAPISecurityService.isUserHasRole(UserRoles.SUPER_ADMIN)) {
            def jsonSlurper = new JsonSlurper().parseText(json.toString())
            CheckIsEventTrailerUploadedSuccessfullyRequestCO checkIsEventTrailerUploadedSuccessfullyRequestCO = new CheckIsEventTrailerUploadedSuccessfullyRequestCO(jsonSlurper)
            responseCO = streamingAPIHelperService.checkIsEventTrailerUploadedSuccessfully(checkIsEventTrailerUploadedSuccessfullyRequestCO)
        } else {
            throw AccessDeniedException()
        }
        return responseCO

    }

    ResponseCO fetchEventTrailerDetails(JSONObject json, HttpServletRequest request) {
        ResponseCO responseCO = null
        if (streamingAPISecurityService.isUserHasRole(UserRoles.SUPER_ADMIN)) {
            def jsonSlurper = new JsonSlurper().parseText(json.toString())
            FetchEventTrailerDetailsRequestCO fetchEventTrailerDetailsRequestCO = new FetchEventTrailerDetailsRequestCO(jsonSlurper)
            responseCO = streamingAPIHelperService.fetchEventTrailerDetails(fetchEventTrailerDetailsRequestCO)
        } else {
            throw AccessDeniedException()
        }
        return responseCO
    }

    ResponseCO recordWowzaLiveStream(JSONObject json, HttpServletRequest request) {
        ResponseCO responseCO = null
        if (streamingAPISecurityService.isUserHasRole(UserRoles.SUPER_ADMIN)) {
            def jsonSlurper = new JsonSlurper().parseText(json.toString())
            WowzaRecordLiveStreamRequestCO wowzaRecordLiveStreamRequestCO = new WowzaRecordLiveStreamRequestCO(jsonSlurper)
            wowzaRecordLiveStreamRequestCO.init()
            wowzaRecordLiveStreamRequestCO.populateStreamRecorderConfig()
            responseCO = streamingAPIHelperService.recordWowzaLiveStream(wowzaRecordLiveStreamRequestCO)
        } else {
            throw AccessDeniedException()
        }
        return responseCO


    }

    ResponseCO stopRecordingWowzaLiveStream(JSONObject json, HttpServletRequest request) {
        ResponseCO responseCO = null
        if (streamingAPISecurityService.isUserHasRole(UserRoles.SUPER_ADMIN)) {
            def jsonSlurper = new JsonSlurper().parseText(json.toString())
            WowzaStopRecordingLiveStreamRequestCO wowzaStopRecordingLiveStreamRequestCO = new WowzaStopRecordingLiveStreamRequestCO(jsonSlurper)
            wowzaStopRecordingLiveStreamRequestCO.init()
            responseCO = streamingAPIHelperService.stopRecordingWowzaLiveStream(wowzaStopRecordingLiveStreamRequestCO)
        } else {
            throw AccessDeniedException()
        }
        return responseCO
    }
}
