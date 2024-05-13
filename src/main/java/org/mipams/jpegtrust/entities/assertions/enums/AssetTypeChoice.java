package org.mipams.jpegtrust.entities.assertions.enums;

import java.util.regex.Pattern;

public enum AssetTypeChoice {
    C2PA_TYPES_CLASSIFIER("c2pa.types.classifier"),
    C2PA_TYPES_CLUSTER("c2pa.types.cluster"),
    C2PA_TYPES_DATASET("c2pa.types.dataset"),
    C2PA_TYPES_DATASET_JAX("c2pa.types.dataset.jax"),
    C2PA_TYPES_DATASET_KERAS("c2pa.types.dataset.keras"),
    C2PA_TYPES_DATASET_ML_NET("c2pa.types.dataset.ml_net"),
    C2PA_TYPES_DATASET_MXNET("c2pa.types.dataset.mxnet"),
    C2PA_TYPES_DATASET_ONNX("c2pa.types.dataset.onnx"),
    C2PA_TYPES_DATASET_OPENVINO("c2pa.types.dataset.openvino"),
    C2PA_TYPES_DATASET_PYTORCH("c2pa.types.dataset.pytorch"),
    C2PA_TYPES_DATASET_TENSORFLOW("c2pa.types.dataset.tensorflow"),
    C2PA_TYPES_FORMAT_NUMPY("c2pa.types.format.numpy"),
    C2PA_TYPES_FORMAT_PROTOBUF("c2pa.types.format.protobuf"),
    C2PA_TYPES_FORMAT_PICKLE("c2pa.types.format.pickle"),
    C2PA_TYPES_GENERATOR("c2pa.types.generator"),
    C2PA_TYPES_GENERATOR_PROMPT("c2pa.types.generator.prompt"),
    C2PA_TYPES_GENERATOR_SEED("c2pa.types.generator.seed"),
    C2PA_TYPES_MODEL("c2pa.types.model"),
    C2PA_TYPES_MODEL_JAX("c2pa.types.model.jax"),
    C2PA_TYPES_MODEL_KERAS("c2pa.types.model.keras"),
    C2PA_TYPES_MODEL_ML_NET("c2pa.types.model.ml_net"),
    C2PA_TYPES_MODEL_MXNET("c2pa.types.model.mxnet"),
    C2PA_TYPES_MODEL_ONNX("c2pa.types.model.onnx"),
    C2PA_TYPES_MODEL_OPENVINO("c2pa.types.model.openvino"),
    C2PA_TYPES_MODEL_OPENVINO_PARAMETER("c2pa.types.model.openvino.parameter"),
    C2PA_TYPES_MODEL_OPENVINO_TOPOLOGY("c2pa.types.model.openvino.topology"),
    C2PA_TYPES_MODEL_PYTORCH("c2pa.types.model.pytorch"),
    C2PA_TYPES_MODEL_TENSORFLOW("c2pa.types.model.tensorflow"),
    C2PA_TYPES_REGRESSOR("c2pa.types.regressor"),
    C2PA_TYPES_TENSORFLOW_HUBMODULE("c2pa.types.tensorflow.hubmodule"),
    C2PA_TYPES_TENSORFLOW_SAVEDMODEL("c2pa.types.tensorflow.savedmodel");

    private static final Pattern REGEX_PATTERN = Pattern.compile("([\\da-zA-Z_-]+\\.)+[\\da-zA-Z_-]+");

    private final String value;

    AssetTypeChoice(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    } 


    public static boolean isCompliant(String input) {
        return REGEX_PATTERN.matcher(input).matches();
    }
}
