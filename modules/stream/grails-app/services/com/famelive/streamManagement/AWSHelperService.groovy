package com.famelive.streamManagement

import com.amazonaws.auth.AWSCredentials
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.model.*
import com.famelive.common.util.FameLiveUtil
import org.codehaus.groovy.grails.commons.GrailsApplication

/**
 * Created by anil on 4/12/14.
 */
class AWSHelperService {
    GrailsApplication grailsApplication
    AmazonS3 s3
    String bucketName

    public void init() {
        AWSCredentials credentials = new BasicAWSCredentials(grailsApplication.config.famelive.aws.accessKey, grailsApplication.config.famelive.aws.secretKey);
        s3 = new AmazonS3Client(credentials);
        bucketName = grailsApplication.config.famelive.aws.eventTrailerBucketName

    }

    public String uploadTestFile(String folderPath) {
        String key = (folderPath + File.separator + "test.txt").toString()
        CannedAccessControlList acl = CannedAccessControlList.BucketOwnerFullControl
        ObjectMetadata metadata = new ObjectMetadata()
        metadata.setContentType("html/text")
        String msg = null
        try {
            PutObjectResult putObjectResult = s3.putObject(new PutObjectRequest(bucketName, key, FameLiveUtil.fetchInputStreamFromString("test file"), metadata));
            println 'file getETag>> ' + putObjectResult.getETag()
            s3.setObjectAcl(bucketName, key, acl)
            msg = 'file added at ' + folderPath
        } catch (Exception e) {
            println 'exception while uploading test object :' + key + ' in AWS S3 bucket :' + bucketName + ' Exp : ' + e
            msg = 'exception while uploading test object :' + key + ' in AWS S3 bucket :' + bucketName + ' Exp : ' + e
        }
        return msg
    }

    public String uploadFile(String key, String filePath) {
        FileInputStream fis = new FileInputStream(new File(filePath))
        CannedAccessControlList acl = CannedAccessControlList.PublicRead
        ObjectMetadata metadata = new ObjectMetadata()
        metadata.setContentType("Image/png")
        String msg = null
        try {
            PutObjectResult putObjectResult = s3.putObject(new PutObjectRequest(bucketName, key, fis, metadata));
            println 'file getETag>> ' + putObjectResult.getETag()
            s3.setObjectAcl(bucketName, key, acl)
            msg = 'file added at ' + key
        } catch (Exception e) {
            println 'exception while uploading test object :' + key + ' in AWS S3 bucket :' + bucketName + ' Exp : ' + e
            msg = 'exception while uploading test object :' + key + ' in AWS S3 bucket :' + bucketName + ' Exp : ' + e
        }
        return msg
    }

    public boolean isObjectExist(String key) {
        boolean status = false
        try {
            S3Object object = s3.getObject(new GetObjectRequest(bucketName, key));
            if (object != null) {
                System.out.println("Content-Type: " + object.getObjectMetadata().getContentType());
                InputStream contentStream = FameLiveUtil.fetchMultiTimeReadableInputStream(object.getObjectContent())
                println FameLiveUtil.fetchStringFromInputStream(contentStream)
                long objectSize = contentStream.available()
                println 'file size: ' + objectSize
                if (objectSize > 0) {
                    status = true
                }
            } else {
                println 'found no object: ' + key + " on AWS S3 bucket :" + bucketName
            }
        } catch (Exception e) {
            println 'Exception occured while checking, if object :' + key + ' exist on AWS S3 bucket :' + bucketName + ' Exp:' + e
        }
        return status
    }

    public boolean copyObject(String sourceKey, String targetKey) {
        boolean status = false
        try {
            CopyObjectResult copyObjectResult = s3.copyObject(bucketName, sourceKey, bucketName, targetKey)
            if (copyObjectResult != null) {
                status = true
            }
        } catch (Exception e) {
            println 'Exception occurred while copying source object, :' + sourceKey + ' as target object :' + targetKey + ' on AWS S3 bucket :' + bucketName + ' Exp:' + e
        }
        return status
    }

    public boolean deleteObject(String key) {
        boolean status = false
        try {
            s3.deleteObject(bucketName, key)
            status = true
        } catch (Exception e) {
            println 'exception while deleting object:' + key + ' from S3 bucket :' + bucketName + ' exp:' + e
        }
        return status
    }

    public boolean renameObject(String Key, String newKey) {
        boolean status = false
        try {
            if (copyObject(Key, newKey)) {
                status = deleteObject(Key)
            } else {
                println 'object :' + key + ' couldn\'t be copied'
            }
        } catch (Exception e) {
            println 'Exception occurred while renaming source object, :' + sourceKey + ' as target object :' + targetKey + ' on AWS S3 bucket :' + bucketName + ' Exp:' + e
        }
        return status
    }

    public ObjectMetadata fetchObjectContentMetaData(String key) {
        ObjectMetadata objectMetadata = null
        try {
            objectMetadata = s3.getObjectMetadata(bucketName, key)
            println 'getContentMD5 for ' + key + ' : ' + objectMetadata.getETag()
        } catch (Exception e) {
            println 'Exception occurred while fetching objectMetaData for key :' + key + ' on AWS S3 bucket :' + bucketName + ' Exp:' + e
        }
        return objectMetadata
    }

    public List<S3ObjectSummary> listObjectsSummaryWithPrefix(String prefix) {
        List<S3ObjectSummary> s3ObjectSummaries = []
        try {
            s3ObjectSummaries = s3.listObjects(bucketName, prefix).getObjectSummaries()
        } catch (Exception e) {
            println 'Exception occurred while listing objects with prefix :' + prefix + ' on AWS S3 bucket :' + bucketName + ' Exp:' + e
        }
        return s3ObjectSummaries
    }
}
