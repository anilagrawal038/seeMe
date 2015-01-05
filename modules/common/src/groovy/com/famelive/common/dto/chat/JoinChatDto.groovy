package com.famelive.common.dto.chat

class JoinChatDto {

    String groupName

    JoinChatDto() {}

    static JoinChatDto createCommonResponseDto() {
        return new JoinChatDto()
    }
}
