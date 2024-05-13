package org.mipams.jpegtrust.jpeg_systems;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.mipams.jumbf.entities.BmffBox;
import org.mipams.jumbf.entities.JumbfBox;
import org.mipams.jumbf.util.CoreUtils;
import org.mipams.jumbf.util.MipamsException;

public class JumbfUtils {

    private static final String JUMBF_URI_REFERENCE_REGEX = "self#jumbf=[\\w\\d\\/][\\w\\d\\.\\/:\\-]+[\\w\\d]";

    public static Optional<JumbfBox> searchJumbfBox(JumbfBox superBox, String uriReference) throws MipamsException{
        if(!isJumbfUriReferenceValid(uriReference)) {
            throw new MipamsException(String.format("Invalid JUMBF URI reference acccording to ISO/IEC 19566-5 specification: [%s]", uriReference));
        }

        final String uriPath = uriReference.substring("self#jumbf=".length());
        final boolean isAbsolute = uriPath.startsWith("/");

        List<String> literals = new ArrayList<>(Arrays.asList(uriPath.split("/")));

        if(isAbsolute) {
            literals.removeFirst();
            String superBoxDescriptionBoxLabel = literals.removeFirst();

            if(!superBoxDescriptionBoxLabel.equals(superBox.getDescriptionBox().getLabel())) {
                throw new MipamsException(String.format("Invalid superbox description box label. Searching for [%s] but found %s", uriReference, superBox.getDescriptionBox().getLabel()));
            }

            if(literals.isEmpty()) {
                return Optional.of(superBox);
            }
        }

        Iterator<BmffBox> jumbfBoxIterator = superBox.getContentBoxList().stream().filter(contentBox -> contentBox.getClass().equals(JumbfBox.class)).iterator();
        Iterator<String> literalIterator = literals.iterator();
        while(literalIterator.hasNext()) {
            String literal = literalIterator.next();
            while(jumbfBoxIterator.hasNext()) {
                JumbfBox box = (JumbfBox) jumbfBoxIterator.next();

                if(literal.equals(box.getDescriptionBox().getLabel())) {
                    if(literalIterator.hasNext()) {
                        jumbfBoxIterator = box.getContentBoxList().stream().filter(contentBox -> contentBox.getClass().equals(JumbfBox.class)).iterator();
                    } else {
                        return Optional.of(box);
                    }
                    break;
                }
            }
        }
        
        return (jumbfBoxIterator.hasNext()) ? Optional.of(new JumbfBox()) : Optional.empty();
    }

    private static boolean isJumbfUriReferenceValid(String uriReference) {
        if(uriReference == null) {
            return false;
        }

        Pattern pattern = Pattern.compile(JUMBF_URI_REFERENCE_REGEX);
        Matcher matcher = pattern.matcher(uriReference);

        return matcher.matches();
    }

    public static long getSizeOfJumbfInApp11SegmentsInBytes(JumbfBox jumbfBox) throws MipamsException{
        long jumbfBoxSize = (jumbfBox.getBoxSizeFromBmffHeaders() > 0) ? jumbfBox.getBoxSizeFromBmffHeaders() : jumbfBox.getBoxSize();

        if (jumbfBoxSize == 0) {
            throw new MipamsException("Cannot know the size of JUMBF Box in advance. LBox equals to 0.");
        }

        long jumbfBoxHeaderSize = CoreUtils.INT_BYTE_SIZE * 2;
        jumbfBoxHeaderSize += jumbfBox.isXBoxEnabled() ? CoreUtils.LONG_BYTE_SIZE : 0;

        final int totalSegments = (int) Math.ceil((double) jumbfBoxSize / CoreUtils.MAX_APP_SEGMENT_SIZE);

        final int app11MarkerSize = CoreUtils.WORD_BYTE_SIZE;
        final int segmentLengthSize = CoreUtils.WORD_BYTE_SIZE;
        final int commonIdentifierSize = CoreUtils.WORD_BYTE_SIZE;
        final int boxInstanceNumberSize = CoreUtils.WORD_BYTE_SIZE;
        final int packetSequenceSize = CoreUtils.INT_BYTE_SIZE;

        final long jpegXtHeaderSize = app11MarkerSize + segmentLengthSize + commonIdentifierSize + boxInstanceNumberSize + packetSequenceSize + jumbfBoxHeaderSize;

        long totalApp11SegmentsSize = jpegXtHeaderSize - jumbfBoxHeaderSize + jpegXtHeaderSize * (totalSegments - 1);
        return totalApp11SegmentsSize + jumbfBoxSize;
    }
}
