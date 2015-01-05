package com.famelive.common.cms

import com.famelive.common.user.User

/**
 * Created by anil on 5/12/14.
 */
class MediaContentRatingInfo {
    MediaContent mediaContent
    User user
    long rating //1-5
    String comments
    Date date = new Date()

    static constraints = {
        comments nullable: true
    }
}
