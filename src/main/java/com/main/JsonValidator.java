package com.main;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.github.fge.jackson.JsonLoader;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.core.report.ProcessingMessage;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.github.fge.jsonschema.main.JsonSchema;
import com.github.fge.jsonschema.main.JsonSchemaFactory;

import java.io.IOException;

public class JsonValidator {

  private static JsonSchemaFactory factory = JsonSchemaFactory.byDefault();

  public static ValidateResult validate(String jsonSchema, String jsonData) throws IOException, ProcessingException {
    if (jsonSchema == null || jsonSchema.length() == 0) {
      return new ValidateResult(true, "Schema does not exists, all data accepted.");
    }
    if (jsonData == null || jsonData.length() == 0) {
      return new ValidateResult(false, "Empty data, validation rejected.");
    }
    ProcessingReport report = null;
    JsonNode schemaNode = JsonLoader.fromString(jsonSchema);
    JsonNode dataNode = JsonLoader.fromString(jsonData);
    JsonSchema schema = factory.getJsonSchema(schemaNode);
    report = schema.validate(dataNode);

    return createValidateResult(report);
  }

  private static ValidateResult createValidateResult(ProcessingReport report) {
    if (report == null) {
      return new ValidateResult(false, "Error in validation: ProcessingReport is NULL.");
    }
    if (report.isSuccess()) {
      return new ValidateResult(true, "Validation success.");
    } else {
      StringBuilder msgBuilder = new StringBuilder();
      msgBuilder.append(report.toString());
      return new ValidateResult(false, msgBuilder.toString());
    }
  }


}
