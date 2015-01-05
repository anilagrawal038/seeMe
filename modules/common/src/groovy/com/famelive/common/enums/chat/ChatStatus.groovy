package com.famelive.common.enums.chat

public enum ChatStatus {

    ACTIVE('Active'),
    INACTIVE('InActive')

    String status

    ChatStatus(String status) {
        this.status = status
    }
}