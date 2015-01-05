package com.famelive.common.cms

/**
 * Created by anil on 5/12/14.
 */
class MediaContentAccessDetails {
    //We are assuming all data stored on AWS S3
    MediaContent mediaContent
    String bucketName
    String folderPath
    String fileName
    String securityPolicy
}
