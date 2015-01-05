package com.famelive.common.constant

import com.famelive.common.enums.CallBrokerType

class CommonConstants {
    final static String MESSAGES_PATH = "/WEB-INF/common_messages"
    final static String MESSAGES_FILE_EXTENSION = "properties"
    final static Integer DEFAULT_EVENT_DURATION = 10

    public static final String INPUT_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss z"
    public static final List EVENT_SLOT_DURATIONS = [5, 10, 20, 40, 60]

    public static CallBrokerType DEFAULT_REQUEST_CALL_BROKER = CallBrokerType.API_CALL_BROKER
//    public static String DEFAULT_API_CALL_BROKER_URL = "http://famelive-api.qa3.intelligrape.net/api/v1"
    public static String DEFAULT_API_CALL_BROKER_URL = "http://fl.zz:8081/fameLive/api/v1"
    public static String DEFAULT_API_CALL_BROKER_USER_AGENT = "Mozilla/5.0"

    //pushNotification
    final static String WOWZA_API_TEST_RESPONSE_PATH = "/WEB-INF/wowzaAPITestResponses"
    final static Integer MAX_USERS_IN_SINGLE_CHATCHANNEL = 500
}
