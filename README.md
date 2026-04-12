# MIPAMS JPEG Trust (ISO/IEC 21617 series)

This repository provides a Java reference SDK for **JPEG Trust - Part 1: Core Foundation (ISO/IEC 21617-1)**. 

It provides the necessary interfaces and services to securely annotate provenance data, generate JPEG Trust Records, and validate that a Trust Record embedded within a media asset is well-formed and authentic.

---

## Requirements & Prerequisites

This is a standalone repository, but it fundamentally relies on the core JUMBF data structures and security extensions provided by the [MIPAMS JPEG Systems repository](https://github.com/nickft/mipams-jpeg-systems).

**CRITICAL:** Before you can build this JPEG Trust SDK, you must first build and install the following specific dependencies into your local Maven repository:

* **JUMBF** (`jumbf-2.1`)
* **Privacy & Security** (`privsec-1.1`)

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
    <version>1.1</version>
</dependency>
```

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
| **Cryptographic Operations** | Verifies ECDSA and RSA digital signatures. Supports `sha256` hashing for integrity checks on JUMBF URI references. |
| **Serialization of Certificates** | Handled natively by the SDK. |
| **Unknown Assertions** | Unknown assertions are currently not rejected. |
| **External References** | The library does not handle external JUMBF URI references. |
| **Ingredient Validation** | Does not consider metadata concerning the validation of ingredient assertions; it validates them from scratch. |
| **Assertion-Specific Validation** | Constraints pertaining to specific assertions (e.g., Actions, Metadata, Ingredients) are not implemented yet. |
| **Date Formats** | No constraints pertaining to date formats are taken into consideration during validation. |
| **TSA Countersignature** | TSA countersignatures in the Claim structure are not processed or validated. |

---

## License

This project is licensed under the Apache License, Version 2.0. 

```
Copyright 2022-Present Nikolaos Fotos, Universitat Politècnica de Catalunya (UPC)

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```