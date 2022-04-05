package lookoutyourwindows.server.service;

import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import com.amazonaws.util.IOUtils;
import lookoutyourwindows.server.exception.ImageException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.SynchronousQueue;

@Service
public class AwsS3Service {

    private final AmazonS3 amazonS3;
    private final String bucketName;

    @Autowired
    public AwsS3Service(@Value("${cloud.aws.s3.bucket}") String bucketName,
                        AmazonS3 amazonS3) {
        this.bucketName = bucketName;
        this.amazonS3 = amazonS3;
    }


    public String uploadFile(String username, MultipartFile multipartFile) {
        DateTimeFormatter dtf = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        String currentTime = LocalDateTime.now().withNano(0).format(dtf);
        String fileName = currentTime.concat("." + extractExt(multipartFile.getOriginalFilename()));
        return uploadFile(username, multipartFile, fileName);
    }


    public String uploadFile(String username, MultipartFile multipartFile, String uploadFileName) {
        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setContentLength(multipartFile.getSize());
        objectMetadata.setContentType(multipartFile.getContentType());

        try(InputStream inputStream = multipartFile.getInputStream()) {
            PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, username + "/" + uploadFileName,
                                                                     inputStream, objectMetadata);
            amazonS3.putObject(putObjectRequest);
        } catch (IOException ex) {
            throw new ImageException("Failed to upload Image");
        }
        return uploadFileName;
    }


    public byte[] downloadFile(String username, String downloadFileName) throws IOException {
        GetObjectRequest getObjectRequest = new GetObjectRequest(bucketName, username + "/" + downloadFileName);
        S3Object s3Object = amazonS3.getObject(getObjectRequest);

        S3ObjectInputStream objectInputStream = s3Object.getObjectContent();

        return IOUtils.toByteArray(objectInputStream);
    }


    public List<S3ObjectSummary> listFiles(String username) {
        ListObjectsRequest listObjectsRequest = new ListObjectsRequest().withBucketName(bucketName)
                                                                        .withPrefix(username + "/")
                                                                        .withDelimiter("/");
        ObjectListing objectListing = amazonS3.listObjects(listObjectsRequest);

        List<S3ObjectSummary> result = objectListing.getObjectSummaries();
        Collections.reverse(result); // Sort by most recent

        return result;
    }


    public List<S3ObjectSummary> listOutputFiles(String username, String originalImage) {
        ListObjectsRequest listObjectsRequest = new ListObjectsRequest().withBucketName(bucketName)
                                                                        .withPrefix(username + "/" + originalImage + "/")
                                                                        .withDelimiter("/");
        ObjectListing objectListing = amazonS3.listObjects(listObjectsRequest);

        return objectListing.getObjectSummaries();
    }


    public String deleteFile(String username, String deleteFileName) {
        DeleteObjectRequest deleteObjectRequest = new DeleteObjectRequest(bucketName, username + "/" + deleteFileName);
        try {
            amazonS3.deleteObject(deleteObjectRequest);
        } catch (SdkClientException ex) {
            ex.printStackTrace();
        }
        return "Delete \"" + deleteFileName +"\" successfully.";
    }


    private String extractExt(String originalFileName) {
        int index = originalFileName.lastIndexOf(".");
        if (index == -1) {
            return  "";
        }
        return originalFileName.substring(index + 1);
    }
}