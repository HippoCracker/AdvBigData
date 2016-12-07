package com.main;



import com.google.gson.JsonElement;

import java.util.Arrays;
import java.util.Map;

import static com.main.common.Utils.SCHEMA;

import static com.main.common.Utils.SEPARATOR;
import static com.main.common.Utils.isNumeric;
import static com.main.common.Utils.join;

public class JsonValidator {

    public static ValidateResult validate(Json schema, Json data) {
        if (schema == null || schema.flatEntrySet().size() == 0) {
            return new ValidateResult(false, "Schema does not exists, create schema before insert data.");
        }
        if (data == null || data.flat().flatEntrySet().size() == 0) {
            return new ValidateResult(false, "Empty data, validation rejected.");
        }

        for (Map.Entry<String, JsonElement> entry : data.flat().flatEntrySet()) {
            String key = entry.getKey();
            JsonElement value = entry.getValue();
            if (isNumeric(String.valueOf(key.charAt(key.length() - 1)))) {
                continue;
            }
            String[] tokens = key.split("\\.");
            tokens[2] = SCHEMA;
            key = join(SEPARATOR, tokens);
            if (!schema.hasFlat(key)) {
                return new ValidateResult(false, "Invalid attribute: " + key);
            }
        }
        return new ValidateResult(true, "Validation succeed");
    }
}
