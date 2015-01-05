package com.famelive.streamManagement

import com.famelive.streamManagement.aws.s3.TestS3Services
import grails.plugin.springsecurity.annotation.Secured

@Secured(['permitAll'])
class TestStreamingController {
    AWSHelperService streamAWSHelperService

    def index() {
        println 'testing s3 going'
//        streamAWSHelperService.uploadTestFile("user/FAME0000011/event/E00000001/trailer/E00000001_trailer_0.mp4")
        streamAWSHelperService.uploadFile('user/FAME0000012/event/E00000002/trailerThumbnail/E00000002_trailer_0_thumbnail.png','/home/anil/famelive/filler_Images/ask-me-anything1.png')
        streamAWSHelperService.uploadFile('user/FAME0000012/event/E00000002/trailer/E00000002_trailer_0.mp4','/home/anil/famelive/filler_Images/video/staytuned.mp4')
//        println 'copyObject() test.txt > \'user/FAME0000011/event/E00000001/trailer/E00000001_trailer_0.mp4\''+streamAWSHelperService.copyObject("user/F001/event/E00000001/trailer/test.txt","user/FAME0000011/event/E00000001/trailer/E00000001_trailer_0.mp4")
//        println 'copyObject() test1.txt > test2.txt'+streamAWSHelperService.copyObject("user/F001/event/E00000001/trailer/test1.txt","user/F001/event/E00000001/trailer/test2.txt")
//        println 'deleteObject() test1.txt '+streamAWSHelperService.deleteObject("user/F001/event/E00000001/trailer/test1.txt")
//        println 'renameObject() test2.txt > test3.txt'+streamAWSHelperService.renameObject("user/F001/event/E00000001/trailer/test2.txt","user/F001/event/E00000001/trailer/test3.txt")
//        println 'isObjectExist() \'user/FAME0000011/event/E00000001/trailer/E00000001_trailer_0.mp4\': '+streamAWSHelperService.isObjectExist("user/FAME0000011/event/E00000001/trailer/E00000001_trailer_0.mp4")
        streamAWSHelperService.fetchObjectContentMetaData('user/FAME0000012/event/E00000002/trailerThumbnail/E00000002_trailer_0_thumbnail.png')
        streamAWSHelperService.fetchObjectContentMetaData('user/FAME0000012/event/E00000002/trailer/E00000002_trailer_0.mp4')
        println 'testing s3 done'
        render 'ok'
    }
}
