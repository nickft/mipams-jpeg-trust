package org.mipams.jpegtrust.entities.assertions.ingredients;

import java.util.List;

public class ValidationResultsMap {
    private StatusCodesMap activeManifest;
    private List<IngredientDeltaValidationResultMap> ingredientDeltas;

    public StatusCodesMap getActiveManifest() {
        return activeManifest;
    }

    public void setActiveManifest(StatusCodesMap activeManifest) {
        this.activeManifest = activeManifest;
    }

    public List<IngredientDeltaValidationResultMap> getIngredientDeltas() {
        return ingredientDeltas;
    }

    public void setIngredientDeltas(List<IngredientDeltaValidationResultMap> ingredientDeltas) {
        this.ingredientDeltas = ingredientDeltas;
    }
}
