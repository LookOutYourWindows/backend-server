package lookoutyourwindows.server.service;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AwsSqsService {

    private final AmazonSQS amazonSQS;
    private final String queueUrl;
    private final ObjectMapper objectMapper;

    @Autowired
    public AwsSqsService(AmazonSQS amazonSQS,
                         @Value("${cloud.aws.sqs.queue}") String queueUrl) {
        this.amazonSQS = amazonSQS;
        this.queueUrl = queueUrl;
        this.objectMapper = new ObjectMapper();
    }

    public void sendCreateMessage(String username, String imageName) {
        String messageBody = null;
        ObjectNode objectNode = objectMapper.createObjectNode();
        objectNode.put("username", username);
        objectNode.put("imageName", imageName);
        try {
            messageBody = objectMapper.writeValueAsString(objectNode);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        SendMessageRequest request = new SendMessageRequest().withMessageGroupId(username)
                .withQueueUrl(queueUrl)
                .withMessageDeduplicationId(UUID.randomUUID().toString())
                .withMessageBody(messageBody);

        amazonSQS.sendMessage(request);
    }
}
