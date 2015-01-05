package com.famelive.streamManagement.wowza.command

import com.famelive.common.constant.CommonConstants
import com.famelive.common.constant.streamManagement.CommonStreamingConstants
import com.famelive.streamManagement.command.RequestCO
import com.famelive.streamManagement.enums.APIRequestsDetails
import grails.converters.JSON
import grails.util.GrailsWebUtil
import org.codehaus.groovy.grails.web.json.JSONObject

/**
 * Created by anil on 29/10/14.
 */
class WowzaAddPublisherResponseCO extends WowzaResponseCO {

    WowzaAddPublisherResponseCO(InputStream inputStream, RequestCO requestCO) {
        if (requestCO._isTestRequest) {
            APIRequestsDetails requestActionDetails = APIRequestsDetails.ADD_WOWZA_PUBLISHER
            File responseFile = GrailsWebUtil.currentApplication().mainContext.getResource(CommonConstants.WOWZA_API_TEST_RESPONSE_PATH + "/" + requestActionDetails.testResponseFile).getFile()
            inputStream = (InputStream) new FileInputStream(responseFile)
        }
        JSONObject jsonObject = JSON.parse(inputStream, "UTF8")
        try {
            this.success = Boolean.parseBoolean(jsonObject.get("success"))
            this.message = jsonObject.get("message")
            if (this.success) {
                this.status = CommonStreamingConstants.STREAMING_API_SUCCESS_CODE
            } else {
                this.status = CommonStreamingConstants.STREAMING_API_ERROR_CODE
            }
        } catch (Exception e) {
        }

    }
}
