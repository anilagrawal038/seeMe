package com.famelive.common.enums.cms

/**
 * Created by anil on 5/12/14.
 */
enum MediaContentType {
    IMAGE('image'),
    VIDEO('video'),
    TEXT('text'),
    OTHER('other')

    String value

    MediaContentType(String value) {
        this.value = value
    }

}