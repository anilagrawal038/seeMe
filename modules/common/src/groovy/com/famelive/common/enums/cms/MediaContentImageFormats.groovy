package com.famelive.common.enums.cms

/**
 * Created by anil on 10/12/14.
 */
enum MediaContentImageFormats {
    JPEG('jpeg'),
    PNG('png'),
    JPG('jpg'),
    GIF('gif')

    String value

    MediaContentImageFormats(String value) {
        this.value = value
    }
}