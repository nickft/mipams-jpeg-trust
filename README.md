# MIPAMS JPEG Trust

This is an SDK library in Java that provides the interfaces to securely annotate provenance data as specified in ISO/IEC 21617-1 (JPEG Trust - Part 1: Core Foundation). It provides the interfaces to produce JPEG Trust data, but also to validate that a JPEG Trust Record embedded in a media asset is valid and well-formed.

## Building from source code
Once all dependencies are available in the local maven repository, it is possible to build this reference implementation from source code. This allows to install the JPEG Trust reference implementation as an available package in the local maven repository as well.

Building from scratch the org.mipams.jpeg-trust library as a package available in the local maven repository. This assumes that the org.mipams.privsec is already installed in the local maven repository. To install MIPAMS JPEG Trust library run the following command within the home directory of the repository:

```bash
mvn clean install
```

## Importing as a dependency
Once the org.mipams.jpeg-trust library has been successfully installed in the local environment, an end-user application can install it as part of its own dependencies by adding the dependency snippet in the pom.xml file of the application as shown in Figure A.2. Including the MIPAMS JPEG Trust implementation by including the following dependency in the pom.xml file:

```xml
<dependency>
    <groupId>org.mipams</groupId>
    <artifactId>jpeg-trust</artifactId>
    <version>1.0</version>
</dependency>
```

## Examples of using the MIPAMS JPEG Trust SDK
### Creating a JPEG Trust Record
One of the key functionalities of this SDK is the creation of JPEG Trust data. An example is shown below where one may use the ManifestBuilder object to instruct the creation of a particular type of Trust Manifest. In this example, a Standard Manifest Content type is selected. 

```java
import org.mipams.jumbf.entities.JumbfBox;

import org.mipams.jpegtrust.entities.JpegTrustUtils;
import org.mipams.jpegtrust.jpeg_systems.content_types.StandardManifestContentType;
import org.mipams.jpegtrust.builders.ManifestBuilder;

// Code omitted for presentation purposes. 
// At some point there is the actual application logic
// ...

ManifestBuilder builder = new ManifestBuilder(new StandardManifestContentType());

// The application logic has instantiated an ActionAssertion and a ContentBinding object
// in the variables actionAssertion and contentBindingAssertion respectively.
// ...

builder.addCreatedAssertion(actionAssertion);
builder.addCreatedAssertion(contentBindingAssertion);

builder.setTitle("MIPAMS test image");
builder.setInstanceID("uuid:7b57930e-2f23-47fc-affe-0400d70b738d");
builder.setGeneratorInfoName("MIPAMS GENERATOR 0.1");

// Include the associated list of certificates 
// associated with the private key used for signing the claim
builder.setClaimSignatureCertificates(certificates);

byte[] encodedClaim = builder.encodeClaimToBeSigned();

// Application-specific logic to digitally sign the encodedClaim object and produce 
// the resulted signature as an array of bytes in the variable claimSignature.
// ...

builder.setClaimSignature(claimSignature);

JumbfBox trustManifest = builder.build();

JumbfBox trustRecord = JpegTrustUtils.buildTrustRecord(builder.build());

// Either embed the Trust Record Content type JUMBF box 
// or store the result as a standalone .jumbf file
// ...
```


### Verifying that a JPEG Trust Record is valid and well-formed

One of the core parts of the overall JPEG Trust framework is the extraction of Trust Indicators coming from the media asset. This reference implementation exposes a service, namely  the ManifestConsumerServie. It is developed as a Java Bean class, allowing applications to load it and use it to validate the provided JUMBF box that corresponds to a JPEG Trust Record Content type. 

The figure below demonstrates how this service can be invoked in order to extract the Trust Indicators Set in the form of a Java object that could be later serialized as a JSON structure to be evaluated against a Trust Profile. This example shows how a third application would use the service offered by this referencec implementation to extract the Trust Indicator Set. Assuming that it has already located and extracted the JPEG Trust Record JUMBF box (instantiated as a JumbfBox Java object in the variable trustRecord) within the media asset, it is able to invoke the “validate()” method to extract all the Trust Indicators from the JPEG Trust metadata of the media asset. The main functionalities of this service is to evaluate that the provided JPEG Trust Record constitutes a well-formed JUMBF Box. This is equivalent to the service validating all internal JUMBF Boxes within a JPEG Trust Record and ensuring that their contents have been encoded in conformance to ISO/IEC 21617-1. In addition, this service performs all the integrity and authenticity checks ensuring that the JPEG Trust Record can be considered valid.

```java
import org.mipams.jumbf.util.MipamsException;

import org.mipams.jpegtrust.service.ManifestConsumerService;
import org.mipams.jpegtrust.entities.validation.trustindicators.TrustIndicatorSet;

ManifestStoreConsumerService manifestStoreConsumerService;

public TrustIndicatorSet validateTrustRecordAndExtractTrustIndicatorSet(JumbfBox trustRecord, String targetFileUrl) throws MipamsException {

// Validate the extracted trustRecord associated with a media asset 
// located at targetFileUrl

    TrustIndicatorSet trustIndicatorSet = manifestStoreConsumerService.validate(trustRecord, targetFileUrl);
    return trustIndicatorSet;
}
```

## Current assumptions on the implementation

|Title| Description|
|--|--|
|Cryptographic Operations| Verification of ECDSA and RSA digital signatures. Support of "SHA256" hashing algorithm for the proccessing of integrity checks on the hashed (JUMBF) URI referenecs.|
|Serialization of Certificates| |
|Unknown assertions are not rejected| |
|External references | The library does not handle external JUMBF URI references. |
|Ingredient validation results | The library does not take into consideration metadata concerning the validation of ingredient assertions. It goes and validates them from scratch. |
|Assertion-specific validation (e.g., Actions, Metadata, Ingredients) | Constraints pertaining to the assertion-specific requirements are not implemented yet.|
|Not checking date formats | No constraint pertaining to the date formats is taken into consideration.|