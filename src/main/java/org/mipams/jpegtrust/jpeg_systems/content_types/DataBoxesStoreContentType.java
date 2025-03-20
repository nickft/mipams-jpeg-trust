package org.mipams.jpegtrust.jpeg_systems.content_types;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.mipams.jumbf.entities.BmffBox;
import org.mipams.jumbf.entities.JumbfBox;
import org.mipams.jumbf.entities.ParseMetadata;
import org.mipams.jumbf.services.boxes.JumbfBoxService;
import org.mipams.jumbf.services.content_types.CborContentType;
import org.mipams.jumbf.util.MipamsException;
import org.springframework.beans.factory.annotation.Autowired;

public class DataBoxesStoreContentType implements ProvenanceContentType {
    @Autowired
    JumbfBoxService jumbfBoxService;

    @Override
    public String getContentTypeUuid() {
        return "63326462-0011-0010-8000-00AA00389B71";
    }

    @Override
    public String getLabel() {
        return "c2pa.databoxes";
    }

    @Override
    public List<BmffBox> parseContentBoxesFromJumbfFile(InputStream input,
            ParseMetadata parseMetadata) throws MipamsException {

        List<BmffBox> dataBoxesStoreContents = new ArrayList<>();
        long remainingBytes = parseMetadata.getAvailableBytesForBox();

        CborContentType cborContentType = new CborContentType();

        while (remainingBytes > 0) {

            ParseMetadata manifestParseMetadata = new ParseMetadata();
            manifestParseMetadata.setAvailableBytesForBox(remainingBytes);
            manifestParseMetadata.setParentDirectory(parseMetadata.getParentDirectory());

            JumbfBox dataBox = jumbfBoxService.parseFromJumbfFile(input, manifestParseMetadata);
            if (!cborContentType.getContentTypeUuid()
                    .equals(dataBox.getDescriptionBox().getUuid())) {
                throw new MipamsException(
                        "Data boxes store shall have one or more Cbor Content type boxes");
            }
            dataBoxesStoreContents.add(dataBox);

            remainingBytes -= dataBox.getBoxSize();
        }

        return dataBoxesStoreContents;
    }

    @Override
    public void writeContentBoxesToJumbfFile(List<BmffBox> contentBoxList,
            OutputStream outputStream) throws MipamsException {

        CborContentType cborContentType = new CborContentType();

        for (BmffBox bmffBox : contentBoxList) {
            JumbfBox jumbfBox = (JumbfBox) bmffBox;

            if (!cborContentType.getContentTypeUuid()
                    .equals(jumbfBox.getDescriptionBox().getUuid())) {
                throw new MipamsException(
                        "Data boxes store shall have one or more Cbor Content type boxes");
            }
            jumbfBoxService.writeToJumbfFile(jumbfBox, outputStream);
        }
    }
}
