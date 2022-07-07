import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class S3Lambda {

    public void main() throws IOException {
        AmazonS3 s3client = AmazonS3ClientBuilder
                .standard()
                .build();

        final AmazonSQS sqs = AmazonSQSClientBuilder.defaultClient();

        // bucket name, key file
        S3Object object = s3client.getObject(new GetObjectRequest("test-arquivos", "file.txt"));
        InputStream objectData = object.getObjectContent();

        // read line by line
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(objectData))) {
            String line;
            int position = 0;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);

                SendMessageRequest send_msg_request = new SendMessageRequest()
                        .withQueueUrl("url queue")
                        .withMessageBody(line)
                        .withMessageDeduplicationId("test" + position)
                        .withMessageGroupId("test" + position);
                sqs.sendMessage(send_msg_request);

                position++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        objectData.close();
    }

}
