package org.mipams.jpegtrust.config;

import org.mipams.jpegtrust.jpeg_systems.SaltHashBoxService;
import org.mipams.jpegtrust.jpeg_systems.content_types.AssertionStoreContentType;
import org.mipams.jpegtrust.jpeg_systems.content_types.ClaimContentType;
import org.mipams.jpegtrust.jpeg_systems.content_types.ClaimSignatureContentType;
import org.mipams.jpegtrust.jpeg_systems.content_types.CompressedManifestContentType;
import org.mipams.jpegtrust.jpeg_systems.content_types.CredentialStoreContentType;
import org.mipams.jpegtrust.jpeg_systems.content_types.StandardManifestContentType;
import org.mipams.jpegtrust.jpeg_systems.content_types.TrustDeclarationContentType;
import org.mipams.jpegtrust.jpeg_systems.content_types.TrustRecordContentType;
import org.mipams.jpegtrust.jpeg_systems.content_types.UpdateManifestContentType;
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
    public CredentialStoreContentType credentialStoreContentType() {
        return new CredentialStoreContentType();
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
}
