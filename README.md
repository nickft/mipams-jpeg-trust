# MIPAMS JPEG Trust (ISO/IEC 21617 series)

This repository provides a Java reference SDK for **JPEG Trust - Part 1: Core Foundation (ISO/IEC 21617-1)**. 

It provides the necessary interfaces and services to securely annotate provenance data, generate JPEG Trust Records, and validate that a Trust Record embedded within a media asset is well-formed and authentic.

---

## Requirements & Prerequisites

This is a standalone repository, but it fundamentally relies on the core JUMBF data structures and security extensions provided by the [MIPAMS JPEG Systems repository](https://github.com/nickft/mipams-jpeg-systems).

**CRITICAL:** Before you can build this JPEG Trust SDK, you must first build and install the following specific dependencies into your local Maven repository:

* **JUMBF** (`jumbf-2.2`)
* **Privacy & Security** (`privsec-1.2`)

**How to install the prerequisites:**
1. Clone the `mipams-jpeg-systems` repository.
2. Run `mvn clean install` from the root of that project.

---

## Installation & Build

Once the core JUMBF and PrivSec dependencies are available in your local environment, you can build and install the JPEG Trust extension from the root of this repository:

```bash
mvn clean install
```

### Importing as a Dependency

End-user applications can include the JPEG Trust implementation by adding the following to their `pom.xml`:

```xml
<dependency>
    <groupId>org.mipams</groupId>
    <artifactId>jpeg-trust</artifactId>
    <version>1.2</version>
</dependency>
```

### Loading all services from library

After adding the dependency, all services provided by the SDK can be registered within the host application's Spring context. An example of how the registration can take place through configuration files, is shown below (This example shows how to use the @Import annotation to the main class of an end user application):

```java
import org.mipams.jpegtrust.config.JpegTrustConfig;
import org.mipams.jumbf.config.JumbfConfig;
import org.mipams.privsec.config.PrivSecConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import({
    JpegTrustConfig.class, 
    JumbfConfig.class, 
    PrivSec.class
}) 
public class EndUserApplicationMainClass {

    public static void main(String[] args) {
        SpringApplication.run(EndUserApplicationMainClass.class, args);
    }
}
```

Once configured, the services of the SDK are fully managed Spring Beans. Therefore, they can be injected into the end user application using @Autowired or constructor injection. Examples of using the SDK are presented in the Usage Scenarios below.

---

## Usage Scenarios

### 1. Creating a JPEG Trust Record

One of the key functionalities of this SDK is the creation of JPEG Trust data. The `ManifestBuilder` allows you to instruct the creation of specific Trust Manifests. In this example, a Standard Manifest Content type is selected.

```java
import org.mipams.jumbf.entities.JumbfBox;
import org.mipams.jpegtrust.entities.JpegTrustUtils;
import org.mipams.jpegtrust.jpeg_systems.content_types.StandardManifestContentType;
import org.mipams.jpegtrust.builders.ManifestBuilder;

// ... Application logic to instantiate ActionAssertion and ContentBinding ...

ManifestBuilder builder = new ManifestBuilder(new StandardManifestContentType());

builder.addCreatedAssertion(actionAssertion, actionAssertionDigest);
builder.addCreatedAssertion(contentBindingAssertion, contentBindingAssertionDigest);

builder.setTitle("MIPAMS test image");
builder.setInstanceID("uuid:7b57930e-2f23-47fc-affe-0400d70b738d");
builder.setGeneratorInfoName("MIPAMS GENERATOR 0.1");
builder.setAlgorithm("sha256");

// Include certificates associated with the private key used for signing
builder.setClaimSignatureCertificates(certificates);

byte[] encodedClaim = builder.encodeClaimToBeSigned();

// ... Application-specific logic to digitally sign the encodedClaim ...
// The signature is stored as raw signature bytes (not ASN.1/DER-encoded).

builder.setClaimSignature(claimSignature);

// Generate the final Trust Record JUMBF Box
JumbfBox trustRecord = JpegTrustUtils.buildTrustRecord(builder.build());

// You can now embed the trustRecord into a JPEG or store it as a .jumbf file
```

### 2. Verifying a Trust Record and Extracting Indicators

The SDK exposes `ManifestConsumerService`, a Spring-managed Java Bean, to evaluate that a provided JUMBF box is a well-formed Trust Record compliant with ISO/IEC 21617-1. It performs integrity and authenticity checks and extracts the **Trust Indicators Set**.

```java
import org.mipams.jumbf.util.MipamsException;
import org.mipams.jpegtrust.service.ManifestConsumerService;
import org.mipams.jpegtrust.entities.validation.trustindicators.TrustIndicatorSet;
import org.springframework.beans.factory.annotation.Autowired;

@Autowired
ManifestStoreConsumerService manifestStoreConsumerService;

public TrustIndicatorSet validateTrustRecordAndExtractTrustIndicatorSet(JumbfBox trustRecord, String targetFileUrl) throws MipamsException {

    // Validate the extracted trustRecord associated with a media asset located at targetFileUrl
    TrustIndicatorSet trustIndicatorSet = manifestStoreConsumerService.validate(trustRecord, targetFileUrl);
    
    return trustIndicatorSet;
}
```
*Note: The resulting `TrustIndicatorSet` object can later be serialized as a JSON structure to be evaluated against a specific Trust Profile.*

---

## Current Assumptions & Limitations

| Feature/Scope | Description |
| :--- | :--- |
| **Cryptographic Operations** | Supports the verification of all types of allowed digital signatures. Supports `sha256` hashing for integrity checks on JUMBF URI references. |
| **Serialization of Certificates** | Handled natively by the SDK. |
| **Unknown Assertions** | Unknown assertions are currently not rejected. |
| **External References** | The library does not handle external JUMBF URI references. |
| **Ingredient Validation** | Does not consider metadata concerning the validation of ingredient assertions; it validates them from scratch. |
| **Assertion-Specific Validation** | Constraints pertaining to specific assertions (e.g., Actions, Metadata, Ingredients) are not implemented yet. |
| **Date Formats** | No constraints pertaining to date formats are taken into consideration during validation. |
| **TSA Countersignature** | TSA countersignatures in the Claim structure are not processed or validated. |

---

## License

This project is currently licensed under the **BSD 3-Clause License**. 

Please note the following version-specific license transitions:
* **Main Project:** Version 1.2 and onward are licensed under the BSD 3-Clause License. (Versions prior to 1.2 were licensed under the Apache License, Version 2.0).

You may obtain a copy of the current license at:
[https://opensource.org/licenses/BSD-3-Clause](https://opensource.org/licenses/BSD-3-Clause)

See the `LICENSE` file in the root directory of this repository for the full text, including specific permissions, conditions, and disclaimers.