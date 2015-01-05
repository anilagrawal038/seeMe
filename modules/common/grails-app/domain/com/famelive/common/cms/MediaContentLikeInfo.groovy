package com.famelive.common.cms

import com.famelive.common.user.User

/**
 * Created by anil on 5/12/14.
 */
class MediaContentLikeInfo {
    MediaContent mediaContent
    User user
    Date date = new Date()
    String comments

    static constraints = {
        comments nullable: true
    }
}
