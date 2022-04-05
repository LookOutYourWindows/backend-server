package lookoutyourwindows.server.integration;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.ReceiveMessageResult;
import lookoutyourwindows.server.repository.AccountRepository;
import lookoutyourwindows.server.service.ImageService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
public class ImageServiceIntegrationTest {

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    @Value("${cloud.aws.sqs.queue}")
    private String queueUrl;

    @Autowired private ImageService imageService;
    @Autowired private AccountRepository accountRepository;
    @Autowired private AmazonS3 amazonS3;
    @Autowired private AmazonSQS amazonSQS;

    @Test
    void uploadImage() {
        // given
        MultipartFile testFile = createTestMultipartFile();
        clearTestQueue();

        // when
        String uploadFileName = imageService.uploadImage("testuser1", testFile);

        // then
        ReceiveMessageRequest request = new ReceiveMessageRequest().withQueueUrl(queueUrl)
                .withMaxNumberOfMessages(1)
                .withWaitTimeSeconds(10);

        List<Message> messages = amazonSQS.receiveMessage(request).getMessages();
        assertThat(amazonS3.doesObjectExist(bucket, "testuser1/" + uploadFileName)).isTrue();
        assertThat(messages.size()).isEqualTo(1);
        for (Message message : messages) {
            assertThat(message.getBody()).isEqualTo("{\"username\":\"testuser1\",\"imageName\":\"" + uploadFileName + "\"}");
        }

        // clean up
        clearTestQueue();
        imageService.deleteImage("testuser1", uploadFileName);
    }

    @Test
    void downloadImage() {
        // given
        MultipartFile testFile = createTestMultipartFile();

        String uploadFileName = imageService.uploadImageWithName("testuser1", testFile, "test1.jpg");

        // when
        ResponseEntity result = imageService.downloadImage("testuser1", uploadFileName);

        // then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);

        //
        imageService.deleteImage("testuser1", uploadFileName);
    }

    @Test
    void downloadImages() {
        // given
        MultipartFile testFile = createTestMultipartFile();

        List<String> uploadFileNames = new ArrayList<>();
        uploadFileNames.add(imageService.uploadImageWithName("testuser1", testFile, "test1.jpg"));
        uploadFileNames.add(imageService.uploadImageWithName("testuser1", testFile, "test2.jpg"));

        // when
        ResponseEntity result = imageService.downloadImages("testuser1", uploadFileNames);

        // then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);

        // clean up
        uploadFileNames.stream()
                       .forEach(uploadFileName -> imageService.deleteImage("testuser1", uploadFileName));
    }


    @Test
    void deleteImage() {
        // given
        MultipartFile testFile = createTestMultipartFile();

        String uploadFileName = imageService.uploadImage("testuser1", testFile);

        // when
        String result = imageService.deleteImage("testuser1", uploadFileName);

        // then
        assertThat(result).isEqualTo("Delete \"" + uploadFileName +"\" successfully.");
    }

    @Test
    void listImages() {
        // given
        String testUsername = UUID.randomUUID().toString();
        MultipartFile testFile = createTestMultipartFile();

        String uploadFileName = imageService.uploadImage(testUsername, testFile);

        // when
        List<String> fileNames = imageService.getImageNames(testUsername);

        // then
        assertThat(fileNames.get(0)).isEqualTo(uploadFileName);

        // clean up
        String result = imageService.deleteImage(testUsername, uploadFileName);
    }
    
    @Test
    void listImagesOnEmptyDir() {
        // given
        String testUsername = UUID.randomUUID().toString();
        
        // when
        List<String> fileNames = imageService.getImageNames(testUsername);

        // then
        assertThat(fileNames.isEmpty()).isTrue();
    }

    private MultipartFile createTestMultipartFile() {
        // Create a test MultipartFile
        BufferedImage testImage = new BufferedImage(100, 100, BufferedImage.TYPE_3BYTE_BGR);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            ImageIO.write(testImage, "jpg", baos);
        } catch (IOException ex) {
            throw new IllegalStateException("Test Failed.");
        }
        MultipartFile testFile = new MockMultipartFile("test.jpg","test.jpg",
                "image/jpg", baos.toByteArray());

        return testFile;
    }


    private void clearTestQueue() {
        List<Message> existMessages = null;
        ReceiveMessageRequest request = new ReceiveMessageRequest().withQueueUrl(queueUrl)
                .withMaxNumberOfMessages(100)
                .withWaitTimeSeconds(3);

        existMessages = amazonSQS.receiveMessage(request).getMessages();
        while (!existMessages.isEmpty()) {
            existMessages.forEach(message -> amazonSQS.deleteMessage(queueUrl, message.getReceiptHandle()));
            existMessages = amazonSQS.receiveMessage(request).getMessages();
        }
    }
}
