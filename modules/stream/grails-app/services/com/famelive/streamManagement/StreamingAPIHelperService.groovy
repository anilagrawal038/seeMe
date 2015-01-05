package com.famelive.streamManagement

import com.amazonaws.services.s3.model.ObjectMetadata
import com.amazonaws.services.s3.model.S3ObjectSummary
import com.famelive.common.CommonMailService
import com.famelive.common.callBroker.RequestCallBroker
import com.famelive.common.callBroker.RequestCallBrokerAdapter
import com.famelive.common.callBroker.RequestCallBrokerResponse
import com.famelive.common.constant.streamManagement.CommonStreamingConstants
import com.famelive.common.enums.CallBrokerType
import com.famelive.common.enums.slotmanagement.EventStatus
import com.famelive.common.enums.streamManagement.StreamManagementConstantKeys
import com.famelive.common.enums.streamManagement.StreamingProtocols
import com.famelive.common.enums.usermanagement.UserRoles
import com.famelive.common.slotmanagement.Event
import com.famelive.common.slotmanagement.EventTrailer
import com.famelive.common.streamManagement.EventInStreamCredentials
import com.famelive.common.streamManagement.EventOutStreamCredentials
import com.famelive.common.streamManagement.EventStreamInfo
import com.famelive.common.streamManagement.WowzaChannel
import com.famelive.common.user.Role
import com.famelive.common.user.User
import com.famelive.common.user.UserRole
import com.famelive.common.util.FameLiveUtil
import com.famelive.common.util.streamManagement.StreamManagementUtil
import com.famelive.streamManagement.api.StreamingAPISecurityService
import com.famelive.streamManagement.command.LoginRequestCO
import com.famelive.streamManagement.command.LoginResponseCO
import com.famelive.streamManagement.command.*
import com.famelive.streamManagement.enums.APIRequestsDetails
import com.famelive.streamManagement.util.StreamMessagesUtil
import com.famelive.streamManagement.util.StreamingSessionUtil
import com.famelive.streamManagement.wowza.AdvanceSetting
import com.famelive.streamManagement.wowza.Publisher
import com.famelive.streamManagement.wowza.command.*
import grails.plugin.springsecurity.SpringSecurityService
import grails.transaction.Transactional
import org.codehaus.groovy.grails.commons.GrailsApplication

@Transactional
class StreamingAPIHelperService {
    RequestCallBrokerAdapter requestCallBrokerAdapter
    SchedulerHelperService schedulerHelperService
    GrailsApplication grailsApplication
    AWSHelperService streamAWSHelperService
    EventTrailerManagementService eventTrailerManagementService
    StreamingCMSService streamingCMSService
    StreamingAPISecurityService streamingAPISecurityService
    SpringSecurityService springSecurityService
    CommonMailService commonMailService
    def passwordEncoder

    ResponseCO login(LoginRequestCO apiLoginRequestCO) {
        apiLoginRequestCO.init()
        LoginResponseCO apiLoginResponseCO = new LoginResponseCO()
        apiLoginResponseCO.success = false
        apiLoginResponseCO.status = CommonStreamingConstants.STREAMING_API_ERROR_CODE
        User user = User.findByEmail(apiLoginRequestCO.email)
        boolean isAuthorized = false
        boolean isEligibleToAuthorize = false
        if (!user) {
            apiLoginResponseCO.message = StreamMessagesUtil.messageSource.getProperty("stream.login.failed.accountDoesNotExist")
        } else if (user.accountLocked) {
            apiLoginResponseCO.message = StreamMessagesUtil.messageSource.getProperty("stream.login.failed.accountBlocked")
        } else if (!user.isAccountVerified) {
            apiLoginResponseCO.message = StreamMessagesUtil.messageSource.getProperty("stream.login.failed.accountNotVerified")
        } else {
            isEligibleToAuthorize = true
        }
        if (isEligibleToAuthorize) {
            isAuthorized = streamingAPISecurityService.authenticateUser(apiLoginRequestCO.request, apiLoginRequestCO.email, apiLoginRequestCO.password)
        }
        if (isAuthorized) {
            apiLoginResponseCO.access_token = streamingAPISecurityService.loginAsUser(user)
            apiLoginResponseCO.email = user.email
            apiLoginResponseCO.userId = user.id
            apiLoginResponseCO.isAccountVerified = user?.isAccountVerified
            apiLoginResponseCO.roles = springSecurityService.authentication.authorities as String
            apiLoginResponseCO.message = StreamMessagesUtil.messageSource.getProperty("stream.login.success")
            apiLoginResponseCO.success = true
            apiLoginResponseCO.status = CommonStreamingConstants.STREAMING_API_SUCCESS_CODE
        } else if (isEligibleToAuthorize) {
            apiLoginResponseCO.message = StreamMessagesUtil.messageSource.getProperty("stream.login.failed.invalidAccountCredentials")
        }
        return apiLoginResponseCO
    }

    ResponseCO register(RegisterUserRequestCO registerUserRequestCO) {
        registerUserRequestCO.init()
        ResponseCO responseCO = new ResponseCO()
        responseCO.success = false
        responseCO.status = CommonStreamingConstants.STREAMING_API_ERROR_CODE
        User user = User.findByEmail(registerUserRequestCO.email)
        boolean isNewUser = true
        if (user != null) {
            responseCO.message = StreamMessagesUtil.messageSource.getProperty("stream.register.failed.accountExistWithEmail")
            isNewUser = false
        } else {
            user = User.findByUsername(registerUserRequestCO.username)
        }
        if (user != null && isNewUser) {
            responseCO.message = StreamMessagesUtil.messageSource.getProperty("stream.register.failed.accountExistWithUsername")
            isNewUser = false
        } else if (user == null && isNewUser) {
            user = new User(email: registerUserRequestCO.email, username: registerUserRequestCO.username, password: registerUserRequestCO.password, verificationToken: FameLiveUtil.randomString, isAccountVerified: false).save(flush: true, failOnError: true)
        }
        if (user == null) {
            responseCO.message = StreamMessagesUtil.messageSource.getProperty("stream.register.failed.accountCouldNotBeCreated")
        } else if (isNewUser) {
            new UserRole(user: user, role: Role.findByAuthority(UserRoles.USER.value)).save(flush: true, failOnError: true)
            responseCO.message = StreamMessagesUtil.messageSource.getProperty("stream.register.success")
            responseCO.success = true
            responseCO.status = CommonStreamingConstants.STREAMING_API_SUCCESS_CODE
            commonMailService.sendRegistrationMail(user)
        }
        return responseCO
    }

    ResponseCO verifyAccount(VerifyAccountRequestCO verifyAccountRequestCO) {
        verifyAccountRequestCO.init()
        ResponseCO responseCO = new ResponseCO()
        responseCO.success = false
        responseCO.status = CommonStreamingConstants.STREAMING_API_ERROR_CODE
        if (verifyAccountRequestCO.email == null || verifyAccountRequestCO.email.length() < 1) {
            verifyAccountRequestCO.email = StreamingSessionUtil.currentUser.email
        }
        User user = User.findByEmail(verifyAccountRequestCO.email)
        if (!user) {
            responseCO.message = StreamMessagesUtil.messageSource.getProperty("stream.verifyAccount.failed.accountDoesNotExist")
        } else if (user.accountLocked) {
            responseCO.message = StreamMessagesUtil.messageSource.getProperty("stream.verifyAccount.failed.accountBlocked")
        } else if (!verifyAccountRequestCO.verificationToken.equals(user.verificationToken)) {
            responseCO.message = StreamMessagesUtil.messageSource.getProperty("stream.verifyAccount.failed.invalidVerificationToken")
        } else {
            responseCO.message = StreamMessagesUtil.messageSource.getProperty("stream.verifyAccount.success")
            responseCO.success = true
            responseCO.status = CommonStreamingConstants.STREAMING_API_SUCCESS_CODE
            user.setIsAccountVerified(true)
            user.save(flush: true)
        }
        return responseCO
    }

    ResponseCO forgotPassword(ForgotPasswordRequestCO forgotPasswordRequestCO) {
        forgotPasswordRequestCO.init()
        ResponseCO responseCO = new ResponseCO()
        responseCO.success = false
        responseCO.status = CommonStreamingConstants.STREAMING_API_ERROR_CODE
        User user = User.findByEmail(forgotPasswordRequestCO.email)
        if (!user) {
            responseCO.message = StreamMessagesUtil.messageSource.getProperty("stream.forgotPassword.failed.accountDoesNotExist")
        } else if (user.accountLocked) {
            responseCO.message = StreamMessagesUtil.messageSource.getProperty("stream.forgotPassword.failed.accountBlocked")
        } else if (!user.isAccountVerified) {
            responseCO.message = StreamMessagesUtil.messageSource.getProperty("stream.forgotPassword.failed.accountNotVerified")
        } else {
            responseCO.message = StreamMessagesUtil.messageSource.getProperty("stream.forgotPassword.success")
            responseCO.success = true
            responseCO.status = CommonStreamingConstants.STREAMING_API_SUCCESS_CODE
            String randomPassword = FameLiveUtil.randomPassword
            user.setPassword(randomPassword)
            user.save(flush: true)
            user.forgotPasswordCode = randomPassword
            commonMailService.sendForgotPasswordMail(user)
        }
        return responseCO
    }

    ResponseCO resendVerificationToken(ResendVerificationTokenRequestCO resendVerificationTokenRequestCO) {
        resendVerificationTokenRequestCO.init()
        ResponseCO responseCO = new ResponseCO()
        responseCO.success = false
        responseCO.status = CommonStreamingConstants.STREAMING_API_ERROR_CODE
        User user = User.findByEmail(resendVerificationTokenRequestCO.email)
        if (!user) {
            responseCO.message = StreamMessagesUtil.messageSource.getProperty("stream.resendVerificationToken.failed.accountDoesNotExist")
        } else if (user.accountLocked) {
            responseCO.message = StreamMessagesUtil.messageSource.getProperty("stream.resendVerificationToken.failed.accountBlocked")
        } else if (user.isAccountVerified) {
            responseCO.message = StreamMessagesUtil.messageSource.getProperty("stream.resendVerificationToken.failed.accountAlreadyVerified")
        } else {
            responseCO.message = StreamMessagesUtil.messageSource.getProperty("stream.resendVerificationToken.success")
            responseCO.success = true
            responseCO.status = CommonStreamingConstants.STREAMING_API_SUCCESS_CODE
            if (user.verificationToken == null || user.verificationToken.length() < 1) {
                user.verificationToken = FameLiveUtil.getRandomString()
            }
            user.save(flush: true)
            commonMailService.sendEmailVerificationMail(user)
        }
        return responseCO
    }

    ResponseCO changePassword(ChangePasswordRequestCO changePasswordRequestCO) {
        changePasswordRequestCO.init()
        ResponseCO responseCO = new ResponseCO()
        responseCO.success = false
        responseCO.status = CommonStreamingConstants.STREAMING_API_ERROR_CODE
        User user = StreamingSessionUtil.currentUser
        if (!changePasswordRequestCO.newPassword.equals(changePasswordRequestCO.confirmPassword)) {
            responseCO.message = StreamMessagesUtil.messageSource.getProperty("stream.changePassword.failed.newPasswordAndConformPasswordNotMatched")
        } else if (!user.email.equals(changePasswordRequestCO.email)) {
            responseCO.message = StreamMessagesUtil.messageSource.getProperty("stream.changePassword.failed.invalidEmailId")
        } else if (!user) {
            responseCO.message = StreamMessagesUtil.messageSource.getProperty("stream.changePassword.failed.accountDoesNotExist")
        } else if (!passwordEncoder.isPasswordValid(user.password, changePasswordRequestCO.oldPassword, null)) {
            responseCO.message = StreamMessagesUtil.messageSource.getProperty("stream.changePassword.failed.invalidExistingPassword")
        } else if (user.accountLocked) {
            responseCO.message = StreamMessagesUtil.messageSource.getProperty("stream.changePassword.failed.accountBlocked")
        } else if (!user.isAccountVerified) {
            responseCO.message = StreamMessagesUtil.messageSource.getProperty("stream.changePassword.failed.accountNotVerified")
        } else {
            responseCO.message = StreamMessagesUtil.messageSource.getProperty("stream.changePassword.success")
            responseCO.success = true
            responseCO.status = CommonStreamingConstants.STREAMING_API_SUCCESS_CODE
            user.setPassword(changePasswordRequestCO.newPassword)
            user.save(flush: true)
            user.forgotPasswordCode = changePasswordRequestCO.newPassword
            commonMailService.sendChangePasswordMail(user)
        }
        return responseCO
    }

    ResponseCO logout(LogoutRequestCO logoutRequestCO) {
        ResponseCO responseCO=new ResponseCO()
            boolean isLoggedOut = streamingAPISecurityService.logoutUser(logoutRequestCO.request, logoutRequestCO.response)
            if (isLoggedOut) {
                responseCO.message = StreamMessagesUtil.messageSource.getProperty("stream.logout.success")
                responseCO.success = true
                responseCO.status = CommonStreamingConstants.STREAMING_API_SUCCESS_CODE
            } else {
                responseCO.message = StreamMessagesUtil.messageSource.getProperty("stream.logout.failed.someErrorOccurred")
                responseCO.success = false
                responseCO.status = CommonStreamingConstants.STREAMING_API_ERROR_CODE
            }
        return responseCO
    }

    ResponseCO getWowzaApplications(WowzaGetApplicationsRequestCO wowzaGetApplicationsRequestCO) {
        RequestCallBroker requestCallBroker = requestCallBrokerAdapter.getCallBroker(CallBrokerType.WOWZA_CALL_BROKER)
        RequestCallBrokerResponse requestCallBrokerResponse = requestCallBroker.execute(wowzaGetApplicationsRequestCO)
        ResponseCO responseCO = new ResponseCO()
        if (requestCallBrokerResponse.statusCode == APIRequestsDetails.GET_WOWZA_APPLICATIONS.successCode) {
            if (requestCallBrokerResponse.responseStream) {
                responseCO = new WowzaGetApplicationsResponseCO(requestCallBrokerResponse.responseStream, wowzaGetApplicationsRequestCO)
                responseCO.success = true
                responseCO.status = CommonStreamingConstants.STREAMING_API_SUCCESS_CODE
                responseCO.message = StreamMessagesUtil.messageSource.getProperty("stream.getWowzaApplications.success")
            } else {
                responseCO.success = false
                responseCO.message = StreamMessagesUtil.messageSource.getProperty("stream.getWowzaApplications.failed.noReadableResponseFromServer")

            }
        } else {
            responseCO.success = false
            responseCO.message = StreamMessagesUtil.messageSource.getProperty("stream.getWowzaApplications.failed.invalidServerResponse")
        }
        return responseCO
    }

    ResponseCO startWowzaApplication(WowzaStartApplicationRequestCO wowzaStartApplicationRequestCO) {
        RequestCallBroker requestCallBroker = requestCallBrokerAdapter.getCallBroker(CallBrokerType.WOWZA_CALL_BROKER)
        RequestCallBrokerResponse requestCallBrokerResponse = requestCallBroker.execute(wowzaStartApplicationRequestCO)
        ResponseCO responseCO = new ResponseCO()
        if (requestCallBrokerResponse.statusCode == APIRequestsDetails.START_WOWZA_APPLICATION.successCode) {
            if (requestCallBrokerResponse.responseStream) {
                responseCO.message = StreamMessagesUtil.messageSource.getProperty("stream.startWowzaApplication.success")
            } else {
                responseCO.message = responseCO.message = StreamMessagesUtil.messageSource.getProperty("stream.startWowzaApplication.failed.noReadableResponseFromServer")
            }
            responseCO.success = true
            responseCO.status = CommonStreamingConstants.STREAMING_API_SUCCESS_CODE
        } else {
            responseCO.success = false
            responseCO.message = StreamMessagesUtil.messageSource.getProperty("stream.startWowzaApplication.failed.invalidServerResponse")
        }
        return responseCO
    }

    ResponseCO restartWowzaApplication(WowzaRestartApplicationRequestCO wowzaRestartApplicationRequestCO) {
        RequestCallBroker requestCallBroker = requestCallBrokerAdapter.getCallBroker(CallBrokerType.WOWZA_CALL_BROKER)
        ResponseCO responseCO = new ResponseCO()

        /*RequestCallBrokerResponse requestCallBrokerResponse = requestCallBroker.execute(wowzaRestartApplicationRequestCO)
        if (requestCallBrokerResponse.statusCode == APIRequestsDetails.RESTART_WOWZA_APPLICATION.successCode) {
            if (requestCallBrokerResponse.responseStream) {
                responseCO.message = StreamMessagesUtil.messageSource.getProperty("stream.restartWowzaApplication.success")
            } else {
                responseCO.message = responseCO.message = StreamMessagesUtil.messageSource.getProperty("stream.restartWowzaApplication.failed.noReadableResponseFromServer")
            }
            responseCO.success = true
            responseCO.status=CommonStreamingConstants.STREAMING_API_SUCCESS_CODE
        } else {
            responseCO.success = false
            responseCO.message = StreamMessagesUtil.messageSource.getProperty("stream.restartWowzaApplication.failed.invalidServerResponse")
        }*/

        //Wowza Restart application api not working, so we are using stop and then start api to achieve the same
        wowzaRestartApplicationRequestCO.actionName = APIRequestsDetails.STOP_WOWZA_APPLICATION.action
        wowzaRestartApplicationRequestCO.action = "shutdown"
        RequestCallBrokerResponse requestCallBrokerResponse = requestCallBroker.execute(wowzaRestartApplicationRequestCO)
        if (requestCallBrokerResponse.statusCode == APIRequestsDetails.STOP_WOWZA_APPLICATION.successCode) {
            wowzaRestartApplicationRequestCO.actionName = APIRequestsDetails.START_WOWZA_APPLICATION.action
            wowzaRestartApplicationRequestCO.action = "start"
            requestCallBrokerResponse = requestCallBroker.execute(wowzaRestartApplicationRequestCO)
            if (requestCallBrokerResponse.statusCode == APIRequestsDetails.START_WOWZA_APPLICATION.successCode) {
                responseCO.message = StreamMessagesUtil.messageSource.getProperty("stream.restartWowzaApplication.success")
                responseCO.success = true
                responseCO.status = CommonStreamingConstants.STREAMING_API_SUCCESS_CODE
            } else {
                responseCO.message = StreamMessagesUtil.messageSource.getProperty("stream.restartWowzaApplication.failed.stoppedButNotStarted")
                responseCO.success = false
            }
        } else {
            responseCO.success = false
            responseCO.message = StreamMessagesUtil.messageSource.getProperty("stream.restartWowzaApplication.failed.invalidServerResponse")
        }
        return responseCO
    }

    ResponseCO stopWowzaApplication(WowzaStopApplicationRequestCO wowzaStopApplicationRequestCO) {
        RequestCallBroker requestCallBroker = requestCallBrokerAdapter.getCallBroker(CallBrokerType.WOWZA_CALL_BROKER)
        RequestCallBrokerResponse requestCallBrokerResponse = requestCallBroker.execute(wowzaStopApplicationRequestCO)
        ResponseCO responseCO = new ResponseCO()
        if (requestCallBrokerResponse.statusCode == APIRequestsDetails.STOP_WOWZA_APPLICATION.successCode) {
            if (requestCallBrokerResponse.responseStream) {
                responseCO.message = StreamMessagesUtil.messageSource.getProperty("stream.stopWowzaApplication.success")
            } else {
                responseCO.message = responseCO.message = StreamMessagesUtil.messageSource.getProperty("stream.stopWowzaApplication.failed.noReadableResponseFromServer")
            }
            responseCO.success = true
            responseCO.status = CommonStreamingConstants.STREAMING_API_SUCCESS_CODE
        } else {
            responseCO.success = false
            responseCO.message = StreamMessagesUtil.messageSource.getProperty("stream.stopWowzaApplication.failed.invalidServerResponse")
        }
        return responseCO
    }

    ResponseCO addWowzaPublisher(WowzaAddPublisherRequestCO wowzaAddPublisherRequestCO) {
        RequestCallBroker requestCallBroker = requestCallBrokerAdapter.getCallBroker(CallBrokerType.WOWZA_CALL_BROKER)
        RequestCallBrokerResponse requestCallBrokerResponse = requestCallBroker.execute(wowzaAddPublisherRequestCO)
        ResponseCO wowzaAddPublisherResponseCO = new ResponseCO()
        if (requestCallBrokerResponse.statusCode == APIRequestsDetails.ADD_WOWZA_PUBLISHER.successCode) {
            if (requestCallBrokerResponse.responseStream) {
                wowzaAddPublisherResponseCO = new WowzaAddPublisherResponseCO(requestCallBrokerResponse.responseStream, wowzaAddPublisherRequestCO)
                wowzaAddPublisherResponseCO.message = StreamMessagesUtil.messageSource.getProperty("stream.addWowzaPublisher.success")
            } else {
                wowzaAddPublisherResponseCO.message = StreamMessagesUtil.messageSource.getProperty("stream.addWowzaPublisher.failed.noReadableResponseFromServer")
            }
            wowzaAddPublisherResponseCO.success = true
            wowzaAddPublisherResponseCO.status = CommonStreamingConstants.STREAMING_API_SUCCESS_CODE
        } else {
            wowzaAddPublisherResponseCO.success = false
            wowzaAddPublisherResponseCO.message = StreamMessagesUtil.messageSource.getProperty("stream.addWowzaPublisher.failed.invalidServerResponse")
        }
        return wowzaAddPublisherResponseCO
    }

    ResponseCO removeWowzaPublisher(WowzaRemovePublisherRequestCO wowzaRemovePublisherRequestCO) {
        RequestCallBroker requestCallBroker = requestCallBrokerAdapter.getCallBroker(CallBrokerType.WOWZA_CALL_BROKER)
        RequestCallBrokerResponse requestCallBrokerResponse = requestCallBroker.execute(wowzaRemovePublisherRequestCO)
        ResponseCO wowzaRemovePublisherResponseCO = new ResponseCO()
        if (requestCallBrokerResponse.statusCode == APIRequestsDetails.REMOVE_WOWZA_PUBLISHER.successCode) {
            if (requestCallBrokerResponse.responseStream) {
                wowzaRemovePublisherResponseCO = new WowzaRemovePublisherResponseCO(requestCallBrokerResponse.responseStream,)
                wowzaRemovePublisherResponseCO.message = StreamMessagesUtil.messageSource.getProperty("stream.removeWowzaPublisher.success")
            } else {
                wowzaRemovePublisherResponseCO.message = StreamMessagesUtil.messageSource.getProperty("stream.removeWowzaPublisher.failed.noReadableResponseFromServer")
            }
            wowzaRemovePublisherResponseCO.success = true
            wowzaRemovePublisherResponseCO.status = CommonStreamingConstants.STREAMING_API_SUCCESS_CODE
        } else {
            wowzaRemovePublisherResponseCO.success = false
            wowzaRemovePublisherResponseCO.message = StreamMessagesUtil.messageSource.getProperty("stream.removeWowzaPublisher.failed.invalidServerResponse")
        }
        return wowzaRemovePublisherResponseCO
    }

    void updateWowzaInputStreamFillerVideoDetail(ChangeEventStateRequestCO changeEventStateToReadyRequestCO) {
        Event event = Event.findByEventId(changeEventStateToReadyRequestCO.eventUID)
        WowzaChannel channel = EventStreamInfo.findByEvent(event).wowzaChannel
        WowzaUpdateApplicationModuleRequestCO wowzaUpdateApplicationModuleRequestCO = new WowzaUpdateApplicationModuleRequestCO()
        wowzaUpdateApplicationModuleRequestCO.serverIP = channel.serverIP
        wowzaUpdateApplicationModuleRequestCO.applicationName = channel.name
        wowzaUpdateApplicationModuleRequestCO.newValue = changeEventStateToReadyRequestCO.eventUID
        wowzaUpdateApplicationModuleRequestCO.actionName = "updateLoopUntilLiveSourceStreamsWowzaApplicationModule"
        System.out.println(updateLoopUntilLiveSourceStreamsWowzaApplicationModule(wowzaUpdateApplicationModuleRequestCO))
    }

    void resetWowzaInputStreamFillerVideoDetail(ChangeEventStateRequestCO changeEventStateToReadyRequestCO) {
        Event event = Event.findByEventId(changeEventStateToReadyRequestCO.eventUID)
        WowzaChannel channel = EventStreamInfo.findByEvent(event).wowzaChannel
        WowzaUpdateApplicationModuleRequestCO wowzaUpdateApplicationModuleRequestCO = new WowzaUpdateApplicationModuleRequestCO()
        wowzaUpdateApplicationModuleRequestCO.serverIP = channel.serverIP
        wowzaUpdateApplicationModuleRequestCO.applicationName = channel.name
        wowzaUpdateApplicationModuleRequestCO.newValue = StreamManagementUtil.fetchStreamManagementConstantForChannel(channel, StreamManagementConstantKeys.DEFAULT_STREAM_FOR_FILLER_VIDEO)
        wowzaUpdateApplicationModuleRequestCO.actionName = "updateLoopUntilLiveSourceStreamsWowzaApplicationModule"
        System.out.println(updateLoopUntilLiveSourceStreamsWowzaApplicationModule(wowzaUpdateApplicationModuleRequestCO))
    }

    ResponseCO changeEventStateToReady(ChangeEventStateRequestCO changeEventStateRequestCO) {
        changeEventStateRequestCO.init()
        boolean isStateChanged
        ResponseCO responseCO = new ResponseCO()
        Event event = Event.findByEventId(changeEventStateRequestCO.eventUID)
        if (event != null) {
            isStateChanged = schedulerHelperService.changeEventStateToReady(changeEventStateRequestCO)
        }
        if (isStateChanged) {
            responseCO.success = true
            responseCO.status = CommonStreamingConstants.STREAMING_API_SUCCESS_CODE
            responseCO.message = StreamMessagesUtil.messageSource.getProperty("stream.changeEventStateToReady.success")
        } else {
            responseCO.success = false
            responseCO.message = StreamMessagesUtil.messageSource.getProperty("stream.changeEventStateToReady.failed.someErrorOccurred")
        }
        return responseCO
    }

    ResponseCO changeEventStateToPaused(ChangeEventStateRequestCO changeEventStateRequestCO) {
        changeEventStateRequestCO.init()
        boolean isStateChanged
        ResponseCO responseCO = new ResponseCO()
        Event event = Event.findByEventId(changeEventStateRequestCO.eventUID)
        if (event != null) {
            isStateChanged = schedulerHelperService.changeEventStateToPaused(changeEventStateRequestCO)
        }
        if (isStateChanged) {
            responseCO.success = true
            responseCO.status = CommonStreamingConstants.STREAMING_API_SUCCESS_CODE
            responseCO.message = StreamMessagesUtil.messageSource.getProperty("stream.changeEventStateToPaused.success")
        } else {
            responseCO.success = false
            responseCO.message = StreamMessagesUtil.messageSource.getProperty("stream.changeEventStateToPaused.failed.someErrorOccurred")
        }
        return responseCO
    }

    ResponseCO changeEventStateToComplete(ChangeEventStateRequestCO changeEventStateRequestCO) {
        changeEventStateRequestCO.init()
        boolean isStateChanged
        ResponseCO responseCO = new ResponseCO()
        Event event = Event.findByEventId(changeEventStateRequestCO.eventUID)
        if (event != null) {
            isStateChanged = schedulerHelperService.changeEventStateToPassed(changeEventStateRequestCO)
        }
        if (isStateChanged) {
            responseCO.success = true
            responseCO.status = CommonStreamingConstants.STREAMING_API_SUCCESS_CODE
            responseCO.message = StreamMessagesUtil.messageSource.getProperty("stream.changeEventStateToComplete.success")
        } else {
            responseCO.success = false
            responseCO.message = StreamMessagesUtil.messageSource.getProperty("stream.changeEventStateToComplete.failed.someErrorOccurred")
        }
        return responseCO
    }

    ResponseCO changeEventStateToLive(ChangeEventStateRequestCO changeEventStateRequestCO) {
        changeEventStateRequestCO.init()
        boolean isStateChanged
        ResponseCO responseCO = new ResponseCO()
        Event event = Event.findByEventId(changeEventStateRequestCO.eventUID)
        if (event != null) {
            isStateChanged = schedulerHelperService.changeEventStateToLive(changeEventStateRequestCO)
        }
        if (isStateChanged) {
            responseCO.success = true
            responseCO.status = CommonStreamingConstants.STREAMING_API_SUCCESS_CODE
            responseCO.message = StreamMessagesUtil.messageSource.getProperty("stream.changeEventStateToLive.success")
        } else {
            responseCO.success = false
            responseCO.message = StreamMessagesUtil.messageSource.getProperty("stream.changeEventStateToLive.failed.someErrorOccurred")
        }
        return responseCO
    }

    ResponseCO fetchInStreamCredentials(FetchStreamCredentialsRequestCO fetchStreamCredentialsRequestCO) {
        fetchStreamCredentialsRequestCO.init()
        Event event = Event.findByEventId(fetchStreamCredentialsRequestCO.eventUID)
        Publisher inStreamPublisher
        FetchStreamCredentialsResponseCO fetchStreamCredentialsResponseCO = new FetchStreamCredentialsResponseCO()

        EventInStreamCredentials eventInStreamCredentials = EventInStreamCredentials.findByEvent(event)
        if (event == null) {
            fetchStreamCredentialsResponseCO.message = StreamMessagesUtil.messageSource.getProperty("stream.fetchInStreamCredentials.failed.invalidEventID")
        } else if (!isEventStreamCredentialsCanBeProvided(event)) {
            fetchStreamCredentialsResponseCO.message = StreamMessagesUtil.messageSource.getProperty("stream.fetchInStreamCredentials.failed.credentialsCanNotBeProvided")
        } else if (eventInStreamCredentials) {
            inStreamPublisher = new Publisher(username: eventInStreamCredentials.username, password: eventInStreamCredentials.password)
        } else {
            EventStreamInfo eventStreamInfo = EventStreamInfo.findByEvent(event)
            WowzaChannel wowzaChannel = eventStreamInfo.wowzaChannel
            inStreamPublisher = schedulerHelperService.addPublisher(wowzaChannel)
            if (inStreamPublisher) {
                eventInStreamCredentials = new EventInStreamCredentials(username: inStreamPublisher.username, password: inStreamPublisher.password, event: event, validFrom: event.startTime, validTill: event.endTime)
                eventInStreamCredentials.save(failOnError: true, flush: true)
            }
        }
        if (inStreamPublisher) {
            fetchStreamCredentialsResponseCO.success = true
            fetchStreamCredentialsResponseCO.status = CommonStreamingConstants.STREAMING_API_SUCCESS_CODE
            fetchStreamCredentialsResponseCO.message = StreamMessagesUtil.messageSource.getProperty("stream.fetchInStreamCredentials.success")
            fetchStreamCredentialsResponseCO.publisher = inStreamPublisher
        } else {
            fetchStreamCredentialsResponseCO.success = false
            if (fetchStreamCredentialsResponseCO.message == null) {
                fetchStreamCredentialsResponseCO.message = StreamMessagesUtil.messageSource.getProperty("stream.fetchInStreamCredentials.failed.credentialsCouldNotBeGenerated")
            }
        }

        return fetchStreamCredentialsResponseCO
    }

    ResponseCO fetchOutStreamCredentials(FetchStreamCredentialsRequestCO fetchStreamCredentialsRequestCO) {
        fetchStreamCredentialsRequestCO.init()
        Event event = Event.findByEventId(fetchStreamCredentialsRequestCO.eventUID)
        Publisher outStreamPublisher
        FetchStreamCredentialsResponseCO fetchStreamCredentialsResponseCO = new FetchStreamCredentialsResponseCO()

        EventOutStreamCredentials eventOutStreamCredentials = EventOutStreamCredentials.findByEvent(event)
        if (event == null) {
            fetchStreamCredentialsResponseCO.message = StreamMessagesUtil.messageSource.getProperty("stream.fetchOutStreamCredentials.failed.invalidEventID")
        } else if (!isEventStreamCredentialsCanBeProvided(event)) {
            fetchStreamCredentialsResponseCO.message = StreamMessagesUtil.messageSource.getProperty("stream.fetchOutStreamCredentials.failed.credentialsCanNotBeProvided")
        } else if (eventOutStreamCredentials) {
            outStreamPublisher = new Publisher(username: eventOutStreamCredentials.username, password: eventOutStreamCredentials.password)
        } else {
            EventStreamInfo eventStreamInfo = EventStreamInfo.findByEvent(event)
            WowzaChannel wowzaChannel = eventStreamInfo.wowzaChannel
            outStreamPublisher = schedulerHelperService.addPublisher(wowzaChannel)
            if (outStreamPublisher) {
                eventOutStreamCredentials = new EventOutStreamCredentials(username: outStreamPublisher.username, password: outStreamPublisher.password, event: event, validFrom: event.startTime, validTill: event.endTime)
                eventOutStreamCredentials.save(failOnError: true, flush: true)
            }
        }
        if (outStreamPublisher) {
            fetchStreamCredentialsResponseCO.success = true
            fetchStreamCredentialsResponseCO.status = CommonStreamingConstants.STREAMING_API_SUCCESS_CODE
            fetchStreamCredentialsResponseCO.message = StreamMessagesUtil.messageSource.getProperty("stream.fetchOutStreamCredentials.success")
            fetchStreamCredentialsResponseCO.publisher = outStreamPublisher
        } else {
            fetchStreamCredentialsResponseCO.success = false
            if (fetchStreamCredentialsResponseCO.message == null) {
                fetchStreamCredentialsResponseCO.message = StreamMessagesUtil.messageSource.getProperty("stream.fetchOutStreamCredentials.failed.credentialsCouldNotBeGenerated")
            }
        }
        return fetchStreamCredentialsResponseCO
    }

    boolean isEventStreamCredentialsCanBeProvided(Event event) {
        boolean status
        if (event.status == EventStatus.READY || event.status == EventStatus.ON_AIR || event.status == EventStatus.PAUSED) {
            status = true
        }
        return status
    }

    ResponseCO fetchAdvanceWowzaApplicationConfiguration(WowzaFetchAdvanceApplicationConfigurationRequestCO wowzaFetchAdvanceApplicationConfigurationRequestCO) {
        RequestCallBroker requestCallBroker = requestCallBrokerAdapter.getCallBroker(CallBrokerType.WOWZA_CALL_BROKER)
        RequestCallBrokerResponse requestCallBrokerResponse = requestCallBroker.execute(wowzaFetchAdvanceApplicationConfigurationRequestCO)
        ResponseCO wowzaFetchAdvanceApplicationConfigurationResponseCO = new ResponseCO()
        if (requestCallBrokerResponse.statusCode == APIRequestsDetails.FETCH_ADVANCE_WOWZA_APPLICATION_CONFIGURATION.successCode) {
            if (requestCallBrokerResponse.responseStream) {
                wowzaFetchAdvanceApplicationConfigurationResponseCO = new WowzaFetchAdvanceApplicationConfigurationResponseCO(requestCallBrokerResponse.responseStream, wowzaFetchAdvanceApplicationConfigurationRequestCO._isTestRequest)
                wowzaFetchAdvanceApplicationConfigurationResponseCO.message = StreamMessagesUtil.messageSource.getProperty("stream.fetchAdvanceWowzaApplicationConfiguration.success")
            } else {
                wowzaFetchAdvanceApplicationConfigurationResponseCO.message = wowzaFetchAdvanceApplicationConfigurationResponseCO.message = StreamMessagesUtil.messageSource.getProperty("stream.fetchAdvanceWowzaApplicationConfiguration.failed.noReadableResponseFromServer")
            }
            wowzaFetchAdvanceApplicationConfigurationResponseCO.success = true
            wowzaFetchAdvanceApplicationConfigurationResponseCO.status = CommonStreamingConstants.STREAMING_API_SUCCESS_CODE
        } else {
            wowzaFetchAdvanceApplicationConfigurationResponseCO.success = false
            wowzaFetchAdvanceApplicationConfigurationResponseCO.message = StreamMessagesUtil.messageSource.getProperty("stream.fetchAdvanceWowzaApplicationConfiguration.failed.invalidServerResponse")
        }
        return wowzaFetchAdvanceApplicationConfigurationResponseCO
    }

    ResponseCO updateLoopUntilLiveSourceStreamsWowzaApplicationModule(WowzaUpdateApplicationModuleRequestCO wowzaUpdateApplicationModuleRequestCO) {
        WowzaFetchAdvanceApplicationConfigurationRequestCO wowzaFetchAdvanceApplicationConfigurationRequestCO = new WowzaFetchAdvanceApplicationConfigurationRequestCO()
        wowzaFetchAdvanceApplicationConfigurationRequestCO.setActionName("fetchAdvanceWowzaApplicationConfiguration")
        wowzaFetchAdvanceApplicationConfigurationRequestCO.setApplicationName(wowzaUpdateApplicationModuleRequestCO.applicationName)
        WowzaFetchAdvanceApplicationConfigurationResponseCO wowzaFetchAdvanceApplicationConfigurationResponseCO = fetchAdvanceWowzaApplicationConfiguration(wowzaFetchAdvanceApplicationConfigurationRequestCO)
        AdvanceSetting loopUntilLiveSourceStreamsSetting = wowzaFetchAdvanceApplicationConfigurationResponseCO.advanceSettings.find { advanceSetting ->
            if (advanceSetting.name.equals("loopUntilLiveSourceStreams")) {
                return true
            }
        }
        wowzaUpdateApplicationModuleRequestCO.advanceSetting = loopUntilLiveSourceStreamsSetting
        wowzaUpdateApplicationModuleRequestCO.advanceSetting.value = wowzaUpdateApplicationModuleRequestCO.newValue
        wowzaUpdateApplicationModuleRequestCO.modules = wowzaFetchAdvanceApplicationConfigurationResponseCO.modules
        wowzaUpdateApplicationModuleRequestCO.restURI = wowzaFetchAdvanceApplicationConfigurationResponseCO.restURI
        wowzaUpdateApplicationModuleRequestCO.version = wowzaFetchAdvanceApplicationConfigurationResponseCO.version
        wowzaUpdateApplicationModuleRequestCO.serverName = wowzaFetchAdvanceApplicationConfigurationResponseCO.serverName
        wowzaUpdateApplicationModuleRequestCO.saveFieldList = wowzaFetchAdvanceApplicationConfigurationResponseCO.saveFieldList

        RequestCallBroker requestCallBroker = requestCallBrokerAdapter.getCallBroker(CallBrokerType.WOWZA_CALL_BROKER)
        RequestCallBrokerResponse requestCallBrokerResponse = requestCallBroker.execute(wowzaUpdateApplicationModuleRequestCO)
        ResponseCO responseCO = new ResponseCO()
        if (requestCallBrokerResponse.statusCode == APIRequestsDetails.UPDATE_LOOP_UNTIL_LIVE_SOURCE_STREAMS_WOWZA_APPLICATION_MODULE.successCode) {
            if (requestCallBrokerResponse.responseStream) {
                responseCO.message = StreamMessagesUtil.messageSource.getProperty("stream.updateLoopUntilLiveSourceStreamsWowzaApplicationModule.success")
            } else {
                responseCO.message = wowzaFetchAdvanceApplicationConfigurationResponseCO.message = StreamMessagesUtil.messageSource.getProperty("stream.updateLoopUntilLiveSourceStreamsWowzaApplicationModule.failed.noReadableResponseFromServer")
            }
            //Going to restart wowza application
            WowzaRestartApplicationRequestCO wowzaRestartApplicationRequestCO = new WowzaRestartApplicationRequestCO(actionName: "startWowzaApplication", applicationName: wowzaFetchAdvanceApplicationConfigurationRequestCO.applicationName)
            responseCO.message += " & " + restartWowzaApplication(wowzaRestartApplicationRequestCO)
            System.out.println(responseCO.message)
            responseCO.success = true
            responseCO.status = CommonStreamingConstants.STREAMING_API_SUCCESS_CODE
        } else {
            responseCO.success = false
            responseCO.message = StreamMessagesUtil.messageSource.getProperty("stream.updateLoopUntilLiveSourceStreamsWowzaApplicationModule.failed.invalidServerResponse")
        }
        return responseCO
    }

    ResponseCO fetchWowzaIncomingStreamInfo(WowzaFetchInStreamInfoRequestCO wowzaFetchInStreamInfoRequestCO) {
        RequestCallBroker requestCallBroker = requestCallBrokerAdapter.getCallBroker(CallBrokerType.WOWZA_CALL_BROKER)
        RequestCallBrokerResponse requestCallBrokerResponse = requestCallBroker.execute(wowzaFetchInStreamInfoRequestCO)
        ResponseCO wowzaFetchInStreamInfoResponseCO = new ResponseCO()
        if (requestCallBrokerResponse.statusCode == APIRequestsDetails.FETCH_ADVANCE_WOWZA_APPLICATION_CONFIGURATION.successCode) {
            if (requestCallBrokerResponse.responseStream) {
                wowzaFetchInStreamInfoResponseCO = new WowzaFetchInStreamInfoResponseCO(requestCallBrokerResponse.responseStream, wowzaFetchInStreamInfoRequestCO._isTestRequest)
                wowzaFetchInStreamInfoResponseCO.message = StreamMessagesUtil.messageSource.getProperty("stream.fetchWowzaIncomingStreamInfo.success")
            } else {
                wowzaFetchInStreamInfoResponseCO.message = StreamMessagesUtil.messageSource.getProperty("stream.fetchWowzaIncomingStreamInfo.failed.noReadableResponseFromServer")
            }
            wowzaFetchInStreamInfoResponseCO.success = true
            wowzaFetchInStreamInfoResponseCO.status = CommonStreamingConstants.STREAMING_API_SUCCESS_CODE
        } else {
            wowzaFetchInStreamInfoResponseCO.success = false
            wowzaFetchInStreamInfoResponseCO.message = StreamMessagesUtil.messageSource.getProperty("stream.fetchWowzaIncomingStreamInfo.failed.invalidServerResponse")
        }
        return wowzaFetchInStreamInfoResponseCO
    }

    ResponseCO fetchWowzaOutgoingStreamInfo(WowzaFetchOutStreamInfoRequestCO wowzaFetchOutStreamInfoRequestCO) {
        RequestCallBroker requestCallBroker = requestCallBrokerAdapter.getCallBroker(CallBrokerType.WOWZA_CALL_BROKER)
        RequestCallBrokerResponse requestCallBrokerResponse = requestCallBroker.execute(wowzaFetchOutStreamInfoRequestCO)
        ResponseCO wowzaFetchOutStreamInfoResponseCO = new ResponseCO()
        if (requestCallBrokerResponse.statusCode == APIRequestsDetails.FETCH_WOWZA_OUTGOING_STREAM_INFO.successCode) {
            if (requestCallBrokerResponse.responseStream) {
                wowzaFetchOutStreamInfoResponseCO = new WowzaFetchOutStreamInfoResponseCO(requestCallBrokerResponse.responseStream, wowzaFetchOutStreamInfoRequestCO._isTestRequest)
                wowzaFetchOutStreamInfoResponseCO.message = StreamMessagesUtil.messageSource.getProperty("stream.fetchWowzaOutgoingStreamInfo.success")
            } else {
                wowzaFetchOutStreamInfoResponseCO.message = StreamMessagesUtil.messageSource.getProperty("stream.fetchWowzaOutgoingStreamInfo.failed.noReadableResponseFromServer")
            }
            wowzaFetchOutStreamInfoResponseCO.success = true
            wowzaFetchOutStreamInfoResponseCO.status = CommonStreamingConstants.STREAMING_API_SUCCESS_CODE
        } else {
            wowzaFetchOutStreamInfoResponseCO.success = false
            wowzaFetchOutStreamInfoResponseCO.message = StreamMessagesUtil.messageSource.getProperty("stream.fetchWowzaOutgoingStreamInfo.failed.invalidServerResponse")
        }
        return wowzaFetchOutStreamInfoResponseCO
    }

    ResponseCO fetchWowzaIncomingStreamStatistics(WowzaFetchInStreamStatisticsRequestCO wowzaFetchInStreamStatisticsRequestCO) {
        RequestCallBroker requestCallBroker = requestCallBrokerAdapter.getCallBroker(CallBrokerType.WOWZA_CALL_BROKER)
        RequestCallBrokerResponse requestCallBrokerResponse = requestCallBroker.execute(wowzaFetchInStreamStatisticsRequestCO)
        ResponseCO wowzaFetchInStreamStatisticsResponseCO = new ResponseCO()
        if (requestCallBrokerResponse.statusCode == APIRequestsDetails.FETCH_WOWZA_INCOMING_STREAM_STATISTICS.successCode) {
            if (requestCallBrokerResponse.responseStream) {
                wowzaFetchInStreamStatisticsResponseCO = new WowzaFetchInStreamStatisticsResponseCO(requestCallBrokerResponse.responseStream, wowzaFetchInStreamStatisticsRequestCO._isTestRequest)
                wowzaFetchInStreamStatisticsResponseCO.message = StreamMessagesUtil.messageSource.getProperty("stream.fetchWowzaIncomingStreamStatistics.success")
            } else {
                wowzaFetchInStreamStatisticsResponseCO.message = StreamMessagesUtil.messageSource.getProperty("stream.fetchWowzaIncomingStreamStatistics.failed.noReadableResponseFromServer")
            }
            wowzaFetchInStreamStatisticsResponseCO.success = true
            wowzaFetchInStreamStatisticsResponseCO.status = CommonStreamingConstants.STREAMING_API_SUCCESS_CODE
        } else {
            wowzaFetchInStreamStatisticsResponseCO.success = false
            wowzaFetchInStreamStatisticsResponseCO.message = StreamMessagesUtil.messageSource.getProperty("stream.fetchWowzaIncomingStreamStatistics.failed.invalidServerResponse")
        }
        return wowzaFetchInStreamStatisticsResponseCO
    }

    ResponseCO resetWowzaIncomingStream(WowzaDoActionOnInStreamRequestCO wowzaDoActionOnInStreamRequestCO) {
        wowzaDoActionOnInStreamRequestCO.setInStreamAction("resetStream")
        RequestCallBroker requestCallBroker = requestCallBrokerAdapter.getCallBroker(CallBrokerType.WOWZA_CALL_BROKER)
        RequestCallBrokerResponse requestCallBrokerResponse = requestCallBroker.execute(wowzaDoActionOnInStreamRequestCO)
        ResponseCO responseCO = new ResponseCO()
        if (requestCallBrokerResponse.statusCode == APIRequestsDetails.RESET_WOWZA_INCOMING_STREAM.successCode) {
            if (requestCallBrokerResponse.responseStream) {
                responseCO.message = StreamMessagesUtil.messageSource.getProperty("stream.resetWowzaIncomingStream.success")
            } else {
                responseCO.message = StreamMessagesUtil.messageSource.getProperty("stream.resetWowzaIncomingStream.failed.noReadableResponseFromServer")
            }
            responseCO.success = true
            responseCO.status = CommonStreamingConstants.STREAMING_API_SUCCESS_CODE
        } else {
            responseCO.success = false
            responseCO.message = StreamMessagesUtil.messageSource.getProperty("stream.resetWowzaIncomingStream.failed.invalidServerResponse")
        }
        return responseCO
    }

    ResponseCO disconnectWowzaIncomingStream(WowzaDoActionOnInStreamRequestCO wowzaDoActionOnInStreamRequestCO) {
        wowzaDoActionOnInStreamRequestCO.setInStreamAction("disconnectStream")
        RequestCallBroker requestCallBroker = requestCallBrokerAdapter.getCallBroker(CallBrokerType.WOWZA_CALL_BROKER)
        RequestCallBrokerResponse requestCallBrokerResponse = requestCallBroker.execute(wowzaDoActionOnInStreamRequestCO)
        ResponseCO responseCO = new ResponseCO()
        if (requestCallBrokerResponse.statusCode == APIRequestsDetails.DISCONNECT_WOWZA_INCOMING_STREAM.successCode) {
            if (requestCallBrokerResponse.responseStream) {
                responseCO.message = StreamMessagesUtil.messageSource.getProperty("stream.disconnectWowzaIncomingStream.success")
            } else {
                responseCO.message = StreamMessagesUtil.messageSource.getProperty("stream.disconnectWowzaIncomingStream.failed.noReadableResponseFromServer")
            }
            responseCO.success = true
            responseCO.status = CommonStreamingConstants.STREAMING_API_SUCCESS_CODE
        } else {
            responseCO.success = false
            responseCO.message = StreamMessagesUtil.messageSource.getProperty("stream.disconnectWowzaIncomingStream.failed.invalidServerResponse")
        }
        return responseCO
    }

    ResponseCO fetchEventTrailerUploadDetails(FetchEventTrailerUploadDetailsRequestCO fetchEventTrailerUploadDetailsRequestCO) {
        fetchEventTrailerUploadDetailsRequestCO.init()
        Event event = Event.findByEventId(fetchEventTrailerUploadDetailsRequestCO.eventUID)
        FetchEventTrailerUploadDetailsResponseCO fetchEventTrailerUploadDetailsResponseCO = new FetchEventTrailerUploadDetailsResponseCO()
        if (event == null) {
            fetchEventTrailerUploadDetailsResponseCO.message = StreamMessagesUtil.messageSource.getProperty("stream.fetchEventTrailerUploadDetails.failed.invalidEventID")
            fetchEventTrailerUploadDetailsResponseCO.success = false
            fetchEventTrailerUploadDetailsResponseCO.status = CommonStreamingConstants.STREAMING_API_ERROR_CODE
        } else {
            //Fetching trailer key
            String trailerPath = event.fetchTrailerPath()
            List<S3ObjectSummary> trailerObjects = streamAWSHelperService.listObjectsSummaryWithPrefix(trailerPath)
            String trailerKey = trailerPath + '/' + event.fetchTrailerName(0)
            if (trailerObjects.size() > 0) {
                S3ObjectSummary firstListedTrailer = trailerObjects.find { s3ObjectSummary ->
                    s3ObjectSummary.key.equals(trailerKey)
                }
                if (firstListedTrailer != null) {
                    streamAWSHelperService.renameObject(trailerKey, trailerPath + '/' + event.fetchTrailerName(trailerObjects.size()))
                }
            }

            //Fetching thumbnail key
            String trailerThumbnailPath = event.fetchTrailerThumbnailPath()
            List<S3ObjectSummary> trailerThumbnailObjects = streamAWSHelperService.listObjectsSummaryWithPrefix(trailerThumbnailPath)
            String trailerThumbnailKey = trailerThumbnailPath + '/' + event.fetchTrailerThumbnailName(0)
            if (trailerThumbnailObjects.size() > 0) {
                S3ObjectSummary firstListedTrailerThumbnail = trailerThumbnailObjects.find { s3ObjectSummary ->
                    s3ObjectSummary.key.equals(trailerThumbnailKey)
                }
                if (firstListedTrailerThumbnail != null) {
                    streamAWSHelperService.renameObject(trailerThumbnailKey, trailerThumbnailPath + '/' + event.fetchTrailerThumbnailName(trailerObjects.size()))
                    //We always rename thumbnail on the basis of trailer id
                }
            }
            fetchEventTrailerUploadDetailsResponseCO.trailerKey = trailerKey
            fetchEventTrailerUploadDetailsResponseCO.thumbnailKey = trailerThumbnailKey
            fetchEventTrailerUploadDetailsResponseCO.bucketName = (String) grailsApplication.config.famelive.aws.eventTrailerBucketName
            fetchEventTrailerUploadDetailsResponseCO.s3AccessKey = FameLiveUtil.encodeBase64(grailsApplication.config.famelive.aws.accessKey)
            fetchEventTrailerUploadDetailsResponseCO.s3SecretKey = FameLiveUtil.encodeBase64(grailsApplication.config.famelive.aws.secretKey)
            fetchEventTrailerUploadDetailsResponseCO.success = true
            fetchEventTrailerUploadDetailsResponseCO.status = CommonStreamingConstants.STREAMING_API_SUCCESS_CODE
            fetchEventTrailerUploadDetailsResponseCO.message = StreamMessagesUtil.messageSource.getProperty("stream.fetchEventTrailerUploadDetails.success")
        }
        return fetchEventTrailerUploadDetailsResponseCO
    }

    ResponseCO checkIsEventTrailerUploadedSuccessfully(CheckIsEventTrailerUploadedSuccessfullyRequestCO checkIsEventTrailerUploadedSuccessfullyRequestCO) {
        checkIsEventTrailerUploadedSuccessfullyRequestCO.init()
        Event event = Event.findByEventId(checkIsEventTrailerUploadedSuccessfullyRequestCO.eventUID)
        CheckIsEventTrailerUploadedSuccessfullyResponseCO checkIsEventTrailerUploadedSuccessfullyResponseCO = new CheckIsEventTrailerUploadedSuccessfullyResponseCO()
        checkIsEventTrailerUploadedSuccessfullyResponseCO.success = false
        checkIsEventTrailerUploadedSuccessfullyResponseCO.status = CommonStreamingConstants.STREAMING_API_ERROR_CODE
        if (event == null) {
            checkIsEventTrailerUploadedSuccessfullyResponseCO.message = StreamMessagesUtil.messageSource.getProperty("stream.checkIsEventTrailerUploadedSuccessfully.failed.invalidEventID")
        } else if (checkIsEventTrailerUploadedSuccessfullyRequestCO.trailerContentETag == null || checkIsEventTrailerUploadedSuccessfullyRequestCO.trailerContentETag.length() <= 0) {
            checkIsEventTrailerUploadedSuccessfullyResponseCO.message = StreamMessagesUtil.messageSource.getProperty("stream.checkIsEventTrailerUploadedSuccessfully.failed.blankTrailerContentETag")
        } else if (checkIsEventTrailerUploadedSuccessfullyRequestCO.thumbnailContentETag == null || checkIsEventTrailerUploadedSuccessfullyRequestCO.thumbnailContentETag.length() <= 0) {
            checkIsEventTrailerUploadedSuccessfullyResponseCO.message = StreamMessagesUtil.messageSource.getProperty("stream.checkIsEventTrailerUploadedSuccessfully.failed.blankTrailerThumbnailContentETag")
        } else {
            String trailerPath = event.fetchTrailerPath()
            String trailerKey = trailerPath + '/' + event.fetchTrailerName(0)
            ObjectMetadata objectMetadata = streamAWSHelperService.fetchObjectContentMetaData(trailerKey)
            if (objectMetadata.getETag().equals(checkIsEventTrailerUploadedSuccessfullyRequestCO.trailerContentETag)) {
                if (eventTrailerManagementService.makeEntryForEventTrailer(event, objectMetadata.getContentLength())) {
                    checkIsEventTrailerUploadedSuccessfullyResponseCO.trailerFileSize = objectMetadata.getContentLength()
                } else {
                    checkIsEventTrailerUploadedSuccessfullyResponseCO.message = StreamMessagesUtil.messageSource.getProperty("stream.checkIsEventTrailerUploadedSuccessfully.failed.trailerMediaContentEntryFailed")
                }
            } else {
                checkIsEventTrailerUploadedSuccessfullyResponseCO.message = StreamMessagesUtil.messageSource.getProperty("stream.checkIsEventTrailerUploadedSuccessfully.failed.invalidTrailerContentETag")
            }

            String trailerThumbnailPath = event.fetchTrailerThumbnailPath()
            String trailerThumbnailKey = trailerThumbnailPath + '/' + event.fetchTrailerThumbnailName(0)
            ObjectMetadata thumbnailMetadata = streamAWSHelperService.fetchObjectContentMetaData(trailerThumbnailKey)
            if (thumbnailMetadata.getETag().equals(checkIsEventTrailerUploadedSuccessfullyRequestCO.thumbnailContentETag)) {
                if (eventTrailerManagementService.makeEntryForEventTrailerThumbnail(event, thumbnailMetadata.getContentLength())) {
                    checkIsEventTrailerUploadedSuccessfullyResponseCO.trailerThumbnailFileSize = thumbnailMetadata.getContentLength()
                    checkIsEventTrailerUploadedSuccessfullyResponseCO.success = true
                    checkIsEventTrailerUploadedSuccessfullyResponseCO.status = CommonStreamingConstants.STREAMING_API_SUCCESS_CODE
                    checkIsEventTrailerUploadedSuccessfullyResponseCO.message = StreamMessagesUtil.messageSource.getProperty("stream.checkIsEventTrailerUploadedSuccessfully.success")
                } else {
                    checkIsEventTrailerUploadedSuccessfullyResponseCO.message = StreamMessagesUtil.messageSource.getProperty("stream.checkIsEventTrailerUploadedSuccessfully.failed.trailerThumbnailMediaContentEntryFailed")
                }
            } else {
                checkIsEventTrailerUploadedSuccessfullyResponseCO.message = StreamMessagesUtil.messageSource.getProperty("stream.checkIsEventTrailerUploadedSuccessfully.failed.invalidTrailerThumbnailContentETag")
            }
        }
        return checkIsEventTrailerUploadedSuccessfullyResponseCO
    }

    ResponseCO fetchEventTrailerDetails(FetchEventTrailerDetailsRequestCO fetchEventTrailerDetailsRequestCO) {
        fetchEventTrailerDetailsRequestCO.init()
        Event event = Event.findByEventId(fetchEventTrailerDetailsRequestCO.eventUID)
        FetchEventTrailerDetailsResponseCO fetchEventTrailerDetailsResponseCO = new FetchEventTrailerDetailsResponseCO()
        fetchEventTrailerDetailsResponseCO.success = false
        fetchEventTrailerDetailsResponseCO.status = CommonStreamingConstants.STREAMING_API_ERROR_CODE
        if (event == null) {
            fetchEventTrailerDetailsResponseCO.message = StreamMessagesUtil.messageSource.getProperty("stream.fetchEventTrailerDetails.failed.invalidEventID")
        } else {
            EventTrailer eventTrailer = EventTrailer.findByEvent(event)
            if (eventTrailer == null || eventTrailer?.trailers.size() <= 0) {
                fetchEventTrailerDetailsResponseCO.message = StreamMessagesUtil.messageSource.getProperty("stream.fetchEventTrailerDetails.failed.trailerNotFound")
            } else {
                eventTrailer?.trailers.each { trailer ->
                    fetchEventTrailerDetailsResponseCO.trailers.add(streamingCMSService.fetchVideoUrls(trailer, StreamingProtocols.HLS))
                }
                fetchEventTrailerDetailsResponseCO.success = true
                fetchEventTrailerDetailsResponseCO.status = CommonStreamingConstants.STREAMING_API_SUCCESS_CODE
                fetchEventTrailerDetailsResponseCO.message = StreamMessagesUtil.messageSource.getProperty("stream.fetchEventTrailerDetails.success")
            }
        }
        return fetchEventTrailerDetailsResponseCO
    }

    ResponseCO recordWowzaLiveStream(WowzaRecordLiveStreamRequestCO wowzaRecordLiveStreamRequestCO) {
        RequestCallBroker requestCallBroker = requestCallBrokerAdapter.getCallBroker(CallBrokerType.WOWZA_CALL_BROKER)
        RequestCallBrokerResponse requestCallBrokerResponse = requestCallBroker.execute(wowzaRecordLiveStreamRequestCO)
        ResponseCO responseCO = new ResponseCO()
        if (requestCallBrokerResponse.statusCode == APIRequestsDetails.RECORD_WOWZA_LIVE_STREAM.successCode) {
            if (requestCallBrokerResponse.responseStream) {
                responseCO.message = StreamMessagesUtil.messageSource.getProperty("stream.recordWowzaLiveStream.success")
            } else {
                responseCO.message = StreamMessagesUtil.messageSource.getProperty("stream.recordWowzaLiveStream.failed.noReadableResponseFromServer")
            }
            responseCO.success = true
            responseCO.status = CommonStreamingConstants.STREAMING_API_SUCCESS_CODE
        } else {
            responseCO.success = false
            responseCO.message = StreamMessagesUtil.messageSource.getProperty("stream.recordWowzaLiveStream.failed.invalidServerResponse")
        }
        return responseCO
    }

    ResponseCO stopRecordingWowzaLiveStream(WowzaStopRecordingLiveStreamRequestCO wowzaStopRecordingLiveStreamRequestCO) {
        RequestCallBroker requestCallBroker = requestCallBrokerAdapter.getCallBroker(CallBrokerType.WOWZA_CALL_BROKER)
        RequestCallBrokerResponse requestCallBrokerResponse = requestCallBroker.execute(wowzaStopRecordingLiveStreamRequestCO)
        ResponseCO responseCO = new ResponseCO()
        if (requestCallBrokerResponse.statusCode == APIRequestsDetails.STOP_RECORDING_WOWZA_LIVE_STREAM.successCode) {
            if (requestCallBrokerResponse.responseStream) {
                responseCO.message = StreamMessagesUtil.messageSource.getProperty("stream.stopRecordingWowzaLiveStream.success")
            } else {
                responseCO.message = StreamMessagesUtil.messageSource.getProperty("stream.stopRecordingWowzaLiveStream.failed.noReadableResponseFromServer")
            }
            responseCO.success = true
            responseCO.status = CommonStreamingConstants.STREAMING_API_SUCCESS_CODE
        } else {
            responseCO.success = false
            responseCO.message = StreamMessagesUtil.messageSource.getProperty("stream.stopRecordingWowzaLiveStream.failed.invalidServerResponse")
        }
        return responseCO
    }

}
