package com.famelive.common.chat

import com.famelive.common.enums.chat.ChatStatus
import com.famelive.common.slotmanagement.Event
import com.famelive.common.user.User

class UserChatStatus {

    Event event
    User user
    String channel
    ChatStatus status


    static constraints = {
        event nullable: false
        user nullable: false
        channel nullable: false
        status nullable: false
    }

    public static Map getChannelAndUserCount(Event event) {
        List chatChannelAndCount = createCriteria().get() {
            eq('event', event)
            projections {
                groupProperty('channel')
                rowCount('userCount')
            }
            order('userCount', 'asc')
            maxResults(1)
        }
        return getChannelAndCountMap(chatChannelAndCount)
    }

    public static Map getChannelAndCountMap(List chatChannelAndCount) {
        Map channelAndCountMap = [:]
        channelAndCountMap.put('chatChannel', chatChannelAndCount ? chatChannelAndCount[0] : null)
        channelAndCountMap.put('userCount', chatChannelAndCount ? chatChannelAndCount[1] : 0)
        return channelAndCountMap
    }

}
