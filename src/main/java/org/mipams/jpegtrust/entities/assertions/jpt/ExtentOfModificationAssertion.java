package org.mipams.jpegtrust.entities.assertions.jpt;

import org.mipams.jpegtrust.entities.assertions.CborAssertion;
import org.mipams.jumbf.util.MipamsException;

public class ExtentOfModificationAssertion extends CborAssertion {

    private Boolean compliance;

    private String metric;

    private Float score;

    public Boolean getCompliance() {
        return compliance;
    }

    public void setCompliance(Boolean compliance) {
        this.compliance = compliance;
    }

    public String getMetric() {
        return metric;
    }

    public void setMetric(String metric) {
        this.metric = metric;
    }

    public Float getScore() {
        return score;
    }

    public void setScore(Float score) {
        this.score = score;
    }

    @Override
    public String getLabel() {
        return "jpt.mod-extent";
    }

    @Override
    public boolean isReductable() throws MipamsException {
        return true;
    }

}
