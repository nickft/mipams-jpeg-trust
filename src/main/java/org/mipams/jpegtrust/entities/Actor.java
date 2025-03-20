package org.mipams.jpegtrust.entities;

import java.util.List;

public class Actor {
    String identifier;
    List<HashedUriReference> credentials;

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public List<HashedUriReference> getCredentials() {
        return credentials;
    }

    public void setCredentials(List<HashedUriReference> credentials) {
        this.credentials = credentials;
    }
}
