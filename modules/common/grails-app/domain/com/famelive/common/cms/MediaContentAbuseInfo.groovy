package com.famelive.common.cms

import com.famelive.common.user.User

/**
 * Created by anil on 5/12/14.
 */
class MediaContentAbuseInfo {
    MediaContent mediaContent
    User user
    String comments
    Date date = new Date()
    boolean isCopyRightIssue = false
}
