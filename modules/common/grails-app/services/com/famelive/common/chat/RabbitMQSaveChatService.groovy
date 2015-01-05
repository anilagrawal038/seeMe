package com.famelive.common.chat

import com.famelive.common.constant.RabbitMQConstants
import grails.transaction.Transactional

@Transactional
class RabbitMQSaveChatService {

    static rabbitQueue = RabbitMQConstants.RABBITMQ_SAVE_CHAT

    void handleMessage(Map chatInfo) {
        ChatMessage.saveChat(chatInfo)
    }
}
