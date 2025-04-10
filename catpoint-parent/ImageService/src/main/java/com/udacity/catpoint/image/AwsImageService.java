package com.udacity.catpoint.image;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.rekognition.RekognitionClient;
import software.amazon.awssdk.services.rekognition.model.DetectLabelsRequest;
import software.amazon.awssdk.services.rekognition.model.DetectLabelsResponse;
import software.amazon.awssdk.services.rekognition.model.Image;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.stream.Collectors;

public class AwsImageService implements ImageService {

    private final Logger log = LoggerFactory.getLogger(AwsImageService.class);

    //aws recommendation is to maintain only a single instance of client objects
    private static RekognitionClient rekognitionClient;

    public AwsImageService() {
        Properties props = new Properties();
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("config.properties")) {
            props.load(is);
        } catch (IOException ioe ) {
            log.error("Unable to initialize AWS Rekognition, no properties file found", ioe);
            return;
        }

        String awsId = props.getProperty("aws.id");
        String awsSecret = props.getProperty("aws.secret");
        String awsRegion = props.getProperty("aws.region");

        AwsCredentials awsCredentials = AwsBasicCredentials.create(awsId, awsSecret);
        rekognitionClient = RekognitionClient.builder()
                .credentialsProvider(StaticCredentialsProvider.create(awsCredentials))
                .region(Region.of(awsRegion))
                .build();
    }

    /**
     * Returns true if the provided image contains a cat.
     * @param image Image to scan
     * @param confidenceThreshhold Minimum threshhold to consider for cat. For example, 90.0f would require 90% confidence minimum
     */
    @Override
    public boolean imageContainsCat(BufferedImage image, float confidenceThreshhold) {
        Image awsImage = null;
        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            ImageIO.write(image, "jpg", os);
            awsImage = Image.builder().bytes(SdkBytes.fromByteArray(os.toByteArray())).build();
        } catch (IOException ioe) {
            log.error("Error building image byte array", ioe);
            return false;
        }
        DetectLabelsRequest detectLabelsRequest = DetectLabelsRequest.builder().image(awsImage).minConfidence(confidenceThreshhold).build();
        DetectLabelsResponse response = rekognitionClient.detectLabels(detectLabelsRequest);
        logLabelsForFun(response);
        return response.labels().stream().anyMatch(l -> l.name().toLowerCase().contains("cat"));
    }

    private void logLabelsForFun(DetectLabelsResponse response) {
        log.info(response.labels().stream()
                .map(label -> String.format("%s(%.1f%%)", label.name(), label.confidence()))
                .collect(Collectors.joining(", ")));
    }
}
