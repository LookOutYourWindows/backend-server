package lookoutyourwindows.server.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ImageService {

    private final AwsS3Service awsS3Service;
    private final AwsSqsService awsSqsService;

    public String uploadImageWithName(String username, MultipartFile multipartFile, String FileName) {
        String uploadFileName = awsS3Service.uploadFile(username, multipartFile, FileName);
        awsSqsService.sendCreateMessage(username, uploadFileName);
        return uploadFileName;
    }


    public String uploadImage(String username, MultipartFile multipartFile) {
        String uploadFileName = awsS3Service.uploadFile(username, multipartFile);
        awsSqsService.sendCreateMessage(username, uploadFileName);
        return uploadFileName;
    }


    public List<String> getImageNames(String username) {
        List<String> fileNames = new ArrayList<>();
        awsS3Service.listFiles(username)
                    .forEach(s3ObjectSummary -> fileNames.add(removeFolderName(s3ObjectSummary.getKey())));

        return fileNames;
    }


    public List<String> getOutputImageNames(String username, String originalFileName) {
        List<String> fileNames = new ArrayList<>();
        awsS3Service.listOutputFiles(username, removeExt(originalFileName))
                    .forEach(s3ObjectSummary -> fileNames.add(removeFolderName(s3ObjectSummary.getKey())));

        return fileNames;
    }


    public ResponseEntity<Object> downloadImage(String username, String downloadFileName) {
        byte[] bytes = null;

        try {
            bytes = awsS3Service.downloadFile(username, downloadFileName);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.MULTIPART_FORM_DATA);
        return new ResponseEntity<>(bytes, httpHeaders, HttpStatus.OK);
    }


    public ResponseEntity<Object> downloadImages(String username, List<String> downloadFileNames) {
        MultiValueMap<String, Object> bytesMap = new LinkedMultiValueMap<>();

        try {
            for (String downloadFileName : downloadFileNames) {
                bytesMap.add(downloadFileName, awsS3Service.downloadFile(username, downloadFileName));
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.MULTIPART_FORM_DATA);
        return new ResponseEntity<>(bytesMap, httpHeaders, HttpStatus.OK);
    }


    public String deleteImage(String username, String deleteFileName){
        return awsS3Service.deleteFile(username, deleteFileName);
    }


    private String removeFolderName(String fileName) {
        int index = fileName.indexOf("/");
        return fileName.substring(index + 1);
    }


    private String removeExt(String fileName) {
        int index = fileName.lastIndexOf(".");
        return fileName.substring(0, index);
    }
}
