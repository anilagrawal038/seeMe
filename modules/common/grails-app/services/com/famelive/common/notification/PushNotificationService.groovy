package com.famelive.common.notification

import com.famelive.common.RabbitMQUtilService
import com.famelive.common.constant.RabbitMQConstants
import grails.transaction.Transactional

@Transactional
class PushNotificationService {

    def grailsApplication
    RabbitMQUtilService rabbitMQUtilService
    public Boolean sendDataToRabbidMQ(Map chatInfo) {
        log.info('******************sendDataToRabbidMQ**************map:'+chatInfo)
        Boolean pushStatus = true
        try {
            sendMessageQueue(chatInfo)
        } catch (Exception e) {
            log.info 'Exception' + e
            pushStatus = false
        }
        return pushStatus
    }

    public sendMessageQueue(Map chatInfo) {
        log.info('**********sendMessageQueue***********map:'+chatInfo)
        List<String> channels = chatInfo.channels
        log.info('**********channels***********'+channels)
        channels.each { String channel ->
            chatInfo.put('channels', [channel])
            rabbitMQUtilService.sendToRabbidMQ(RabbitMQConstants.RABBITMQ_PUSH_NOTIFICATION, chatInfo)
        }
    }
}
