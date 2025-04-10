module ImageService {
    requires java.desktop;
    requires software.amazon.awssdk.auth;
    requires org.slf4j;
    requires software.amazon.awssdk.services.rekognition;
    requires software.amazon.awssdk.regions;
    requires software.amazon.awssdk.core;
    exports com.udacity.catpoint.image;
}