package com.famelive.common.dto.chat

class SendChatMessageDto {

    String eventId

    SendChatMessageDto() {}

    static JoinChatDto createCommonResponseDto() {
        return new JoinChatDto()
    }
}
