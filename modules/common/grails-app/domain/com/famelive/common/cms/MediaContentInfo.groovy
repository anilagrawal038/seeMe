package com.famelive.common.cms

import com.famelive.common.user.User

/**
 * Created by anil on 5/12/14.
 */
class MediaContentInfo {
    MediaContent mediaContent
    User uploadedBy
    boolean isDeleted = false
    String copyrightInfo
    String tags
    Date uploadDate = new Date()
    long abuseCount = 0
    long likeCount = 0
    String source
    long rating = 0
    boolean haveRegionalAccessLimitations = false
    boolean haveUserAccessLimitations = false
    Locale language = Locale.ENGLISH
    boolean isAllowComments = true

    static constraints = {
        copyrightInfo nullable: true
        tags nullable: true
        source nullable: true
    }
}
