package org.mipams.jpegtrust.config;

import org.mipams.jpegtrust.jpeg_systems.SaltHashBoxService;
import org.mipams.jpegtrust.jpeg_systems.content_types.AssertionStoreContentType;
import org.mipams.jpegtrust.jpeg_systems.content_types.ClaimContentType;
import org.mipams.jpegtrust.jpeg_systems.content_types.ClaimSignatureContentType;
import org.mipams.jpegtrust.jpeg_systems.content_types.CompressedManifestContentType;
import org.mipams.jpegtrust.jpeg_systems.content_types.DataBoxesStoreContentType;
import org.mipams.jpegtrust.jpeg_systems.content_types.StandardManifestContentType;
import org.mipams.jpegtrust.jpeg_systems.content_types.TrustDeclarationContentType;
import org.mipams.jpegtrust.jpeg_systems.content_types.TrustRecordContentType;
import org.mipams.jpegtrust.jpeg_systems.content_types.UpdateManifestContentType;
import org.mipams.jpegtrust.services.validation.consumer.AssertionConsumer;
import org.mipams.jpegtrust.services.validation.consumer.ClaimConsumer;
import org.mipams.jpegtrust.services.validation.consumer.ClaimSignatureConsumer;
import org.mipams.jpegtrust.services.validation.consumer.ManifestConsumer;
import org.mipams.jpegtrust.services.validation.consumer.ManifestStoreConsumer;
import org.mipams.jpegtrust.services.validation.consumer.UriReferenceService;
import org.mipams.jpegtrust.services.validation.discovery.AssertionDiscovery;
import org.mipams.jpegtrust.services.validation.discovery.ManifestDiscovery;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JpegTrustConfig {

    @Bean
    public AssertionStoreContentType assertionStoreContentType() {
        return new AssertionStoreContentType();
    }

    @Bean
    public ClaimContentType claimContentType() {
        return new ClaimContentType();
    }

    @Bean
    public ClaimSignatureContentType claimSignatureContentType() {
        return new ClaimSignatureContentType();
    }

    @Bean
    public DataBoxesStoreContentType dataBoxesStoreContentType() {
        return new DataBoxesStoreContentType();
    }

    @Bean
    public TrustRecordContentType trustRecordContentType() {
        return new TrustRecordContentType();
    }

    @Bean
    public StandardManifestContentType standardManifestContentType() {
        return new StandardManifestContentType();
    }

    @Bean
    public TrustDeclarationContentType trustDeclarationContentType() {
        return new TrustDeclarationContentType();
    }

    @Bean
    public CompressedManifestContentType compressedManifestContentType() {
        return new CompressedManifestContentType();
    }

    @Bean
    public UpdateManifestContentType updateManifestContentType() {
        return new UpdateManifestContentType();
    }

    @Bean 
    public SaltHashBoxService saltHashBoxService() {
        return new SaltHashBoxService();
    }

    @Bean 
    public AssertionConsumer assertionConsumer() {
        return new AssertionConsumer();
    }

    @Bean 
    public AssertionDiscovery assertionDiscovery() {
        return new AssertionDiscovery();
    }

    @Bean 
    public ClaimConsumer claimConsumer() {
        return new ClaimConsumer();
    }

    @Bean 
    public ClaimSignatureConsumer claimSignatureConsumer() {
        return new ClaimSignatureConsumer();
    }

    @Bean 
    public ManifestConsumer manifestConsumer() {
        return new ManifestConsumer();
    }

    @Bean 
    public ManifestDiscovery manifestDiscovery() {
        return new ManifestDiscovery();
    }

    @Bean 
    public ManifestStoreConsumer manifestStoreConsumer() {
        return new ManifestStoreConsumer();
    }

    @Bean 
    public UriReferenceService uriReferenceService() {
        return new UriReferenceService();
    }
}
