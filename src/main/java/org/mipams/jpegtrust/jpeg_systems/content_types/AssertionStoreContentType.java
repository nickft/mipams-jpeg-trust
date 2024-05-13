package org.mipams.jpegtrust.jpeg_systems.content_types;

import java.io.OutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import org.mipams.jumbf.entities.BmffBox;
import org.mipams.jumbf.entities.JumbfBox;
import org.mipams.jumbf.entities.ParseMetadata;
import org.mipams.jumbf.services.boxes.JumbfBoxService;
import org.mipams.jumbf.util.CoreUtils;
import org.mipams.jumbf.util.MipamsException;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;

@Service
public class AssertionStoreContentType implements ProvenanceContentType {

    @Autowired
    JumbfBoxService jumbfBoxService;

    @Override
    public String getContentTypeUuid() {
        return "63326173-0011-0010-8000-00AA00389B71";
    }

    @Override
    public String getLabel() {
        return "c2pa.assertions";
    }

    @Override
    public List<BmffBox> parseContentBoxesFromJumbfFile(InputStream input, ParseMetadata parseMetadata)
            throws MipamsException {

        List<BmffBox> contentBoxList = new ArrayList<>();

        long remainingBytes = parseMetadata.getAvailableBytesForBox();

        String assertionStoreDir = CoreUtils.createSubdirectory(parseMetadata.getParentDirectory(), getLabel());
        ParseMetadata assertionParseMetadata;

        while (remainingBytes > 0) {

            assertionParseMetadata = new ParseMetadata();
            assertionParseMetadata.setAvailableBytesForBox(remainingBytes);
            assertionParseMetadata.setParentDirectory(assertionStoreDir);

            JumbfBox assertionBox = jumbfBoxService.parseFromJumbfFile(input, assertionParseMetadata);
            contentBoxList.add(assertionBox);

            remainingBytes -= assertionBox.getBoxSize();
        }

        return contentBoxList;
    }

    @Override
    public void writeContentBoxesToJumbfFile(List<BmffBox> contentBoxList, OutputStream outputStream)
            throws MipamsException {

        for (BmffBox bmffBox : contentBoxList) {
            JumbfBox assertionBox = (JumbfBox) bmffBox;

            jumbfBoxService.writeToJumbfFile(assertionBox, outputStream);
        }
    }
}
