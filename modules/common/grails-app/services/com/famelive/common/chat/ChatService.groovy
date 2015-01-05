package com.famelive.common.chat

import com.famelive.common.RabbitMQUtilService
import com.famelive.common.command.chat.JoinChatCommand
import com.famelive.common.command.chat.SendChatMessageCommand
import com.famelive.common.constant.CommonConstants
import com.famelive.common.constant.RabbitMQConstants
import com.famelive.common.dto.chat.JoinChatDto
import com.famelive.common.dto.chat.SendChatMessageDto
import com.famelive.common.enums.chat.ChatStatus
import com.famelive.common.exceptions.CommonException
import com.famelive.common.slotmanagement.Event
import com.famelive.common.user.User
import grails.transaction.Transactional

@Transactional
class ChatService {

    def eventService
    RabbitMQUtilService rabbitMQUtilService

    JoinChatDto joinChat(JoinChatCommand fetchChatChannelCommand) throws CommonException {
        fetchChatChannelCommand?.validate()
        Event event = Event.get(fetchChatChannelCommand.eventId)
        Map channelAndUserCount = UserChatStatus.getChannelAndUserCount(event)
        String chatChannel = getChatChannelForNewUser(channelAndUserCount, event)
        String userChannelGroup = addChannelToUserGroup(chatChannel, fetchChatChannelCommand)
        saveUserChatStatus(event, chatChannel, fetchChatChannelCommand)
        JoinChatDto fetchChatChannelDto = new JoinChatDto(groupName: userChannelGroup)
        return fetchChatChannelDto
    }

    public String getChatChannelForNewUser(Map channelAndUserCount, Event event) {
        Integer userCount = channelAndUserCount.userCount
        String chatChannel = (channelAndUserCount.chatChannel) ? (channelAndUserCount.chatChannel) : (event.activeChannel)
        if (userCount >= CommonConstants.MAX_USERS_IN_SINGLE_CHATCHANNEL) {
            chatChannel = eventService.addChannelToEvent(event)
        }
        return chatChannel
    }

    public void saveUserChatStatus(Event event, String chatChannel, JoinChatCommand channelCommand) {
        User user = User.get(channelCommand.id)
        UserChatStatus userChatStatus = new UserChatStatus(event: event, channel: chatChannel, user: user, status: ChatStatus.ACTIVE)
        userChatStatus.save(failOnError: true, flush: true)
    }

    SendChatMessageDto sendChatMessage(SendChatMessageCommand sendChatMessageCommand) throws CommonException {
        sendChatMessageCommand?.validate()
        Map chatMessageInfo = getChatMessageMap(sendChatMessageCommand)
        List<String> eventChannels = Event.getEventChannels(sendChatMessageCommand.eventId)
        sendMessageToPubnub(chatMessageInfo, eventChannels)
        saveChatMessageInfo(chatMessageInfo)
        return (new SendChatMessageDto(eventId: sendChatMessageCommand.eventId))
    }

    public Map getChatMessageMap(SendChatMessageCommand sendChatMessageCommand) {
        User user = User.get(sendChatMessageCommand.id)
        Map chatMessageMap = [:]
        chatMessageMap.messageId = sendChatMessageCommand.messageId
        chatMessageMap.message = sendChatMessageCommand.message
        chatMessageMap.timeStamp = sendChatMessageCommand.timeStamp
        chatMessageMap.userName = user.username
        chatMessageMap.fameName = user.fameName
        chatMessageMap.imageName = user.imageName ? user.imageName : ''
        return chatMessageMap
    }

    public sendMessageToPubnub(Map chatMessageMap, List<String> eventChannels) {
        eventChannels.each { String eventChannel ->
            sendMessageByRabbidMQ([channel: eventChannel, chatMessageMap: chatMessageMap])
        }
    }

    public void saveChatMessageInfo(Map chatMessageInfo) {
        saveMessageByRabbidMQ(chatMessageInfo)
    }

    public void saveMessageByRabbidMQ(Map chatMessageInfo) {
        rabbitMQUtilService.sendToRabbidMQ(RabbitMQConstants.RABBITMQ_SAVE_CHAT, chatMessageInfo)
    }

    public void sendMessageByRabbidMQ(Map channelAndMessageInfo) {
        log.info 'Before send Map::' + channelAndMessageInfo
        rabbitMQUtilService.sendToRabbidMQ(RabbitMQConstants.RABBITMQ_CHAT, channelAndMessageInfo)
    }

    public String addChannelToUserGroup(String chatChannel, JoinChatCommand fetchChatChannelCommand) {
        String userChannelGroup = User.get(fetchChatChannelCommand.id).channelGroup
        log.info 'userChannelGroup::' + userChannelGroup
        rabbitMQUtilService.sendToRabbidMQ(RabbitMQConstants.RABBITMQ_CHANNEL_GROUP, [channel: chatChannel, group: userChannelGroup])
        return userChannelGroup
    }
}
