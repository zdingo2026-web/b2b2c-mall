package com.mall.service.config;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@ConfigurationProperties(prefix = "minio")
@Getter
public class MinioConfig {

    private String endpoint;
    private String accessKey;
    private String secretKey;
    private String bucketName;
    private String urlPrefix;

    public void setEndpoint(String endpoint) { this.endpoint = endpoint; }
    public void setAccessKey(String accessKey) { this.accessKey = accessKey; }
    public void setSecretKey(String secretKey) { this.secretKey = secretKey; }
    public void setBucketName(String bucketName) { this.bucketName = bucketName; }
    public void setUrlPrefix(String urlPrefix) { this.urlPrefix = urlPrefix; }

    @Bean
    public MinioClient minioClient() {
        // MinIO SDK reads SSL_CERT_FILE / NSSL_CERT_FILE which may point to a non-existent path (e.g. Anaconda)
        String sslCertFile = System.getenv("SSL_CERT_FILE");
        if (sslCertFile != null && !new java.io.File(sslCertFile).exists()) {
            System.clearProperty("javax.net.ssl.trustStore");
        }
        // Use OkHttpClient that ignores the broken SSL_CERT_FILE env var
        okhttp3.OkHttpClient httpClient = new okhttp3.OkHttpClient.Builder()
                .connectTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
                .writeTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
                .build();
        return MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .httpClient(httpClient)
                .build();
    }

    @Bean
    public ApplicationRunner minioBucketInitializer(MinioClient client) {
        return args -> {
            try {
                boolean exists = client.bucketExists(BucketExistsArgs.builder()
                        .bucket(bucketName)
                        .build());
                if (!exists) {
                    client.makeBucket(MakeBucketArgs.builder()
                            .bucket(bucketName)
                            .build());
                    log.info("[MinioConfig] Created bucket: {}", bucketName);
                }
            } catch (Exception e) {
                log.warn("[MinioConfig] Failed to init bucket '{}': {}", bucketName, e.getMessage());
            }
        };
    }
}
