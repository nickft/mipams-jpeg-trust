package org.mipams.jpegtrust.entities.assertions.ingredients;

public class IngredientDeltaValidationResultMap {
    private String ingredientAssertionURI;
    private StatusCodesMap validationDeltas;

    public String getIngredientAssertionURI() {
        return ingredientAssertionURI;
    }

    public void setIngredientAssertionURI(String ingredientAssertionURI) {
        this.ingredientAssertionURI = ingredientAssertionURI;
    }

    public StatusCodesMap getValidationDeltas() {
        return validationDeltas;
    }

    public void setValidationDeltas(StatusCodesMap validationDeltas) {
        this.validationDeltas = validationDeltas;
    }
}
