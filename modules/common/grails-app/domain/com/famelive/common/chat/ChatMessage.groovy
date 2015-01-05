package com.famelive.common.chat

class ChatMessage {

    static mapWith = "mongo"

    Map chatInfo

    static constraints = {
    }

    static void saveChat(Map chatInfo) {
        try {
            new ChatMessage(chatInfo: chatInfo).save(failOnError: true)
        } catch (Exception e) {
        }
    }
}
