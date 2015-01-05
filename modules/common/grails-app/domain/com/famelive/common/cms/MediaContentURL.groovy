package com.famelive.common.cms
/**
 * Created by anil on 5/12/14.
 */
class MediaContentURL {
    MediaContent mediaContent
    String url
    String embedCode
    static constraints = {
        embedCode nullable: true
    }
}
