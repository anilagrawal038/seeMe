package com.famelive.common

import grails.transaction.Transactional

@Transactional
class RabbitMQUtilService {

    public Boolean sendToRabbidMQ(String queueName, Map map) {
        log.info('********sendToRabbidMQ************map:'+map+'***queueName**'+queueName)
        Boolean flag = true
        try {
            rabbitSend queueName, map
        } catch (Exception e) {
            log.info 'sendToRabbidMQ::queueName::' + queueName + '::Map::' + map
            log.info 'Exception::' + e
            flag = false
        }
        return flag
    }
}
