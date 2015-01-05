package com.famelive.common.enums.cms

/**
 * Created by anil on 5/12/14.
 */
enum MediaCategory {
    GENERAL('general'),
    COMEDY('comedy'),
    DRAMA('drama'),
    TRAILER_TEASER('trailer_teaser')

    String value

    MediaCategory(String value) {
        this.value = value
    }

}