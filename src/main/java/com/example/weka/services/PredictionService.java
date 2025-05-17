package com.example.weka.services;

import com.example.weka.entities.PatientPrediction;
import com.example.weka.entities.PredictionRequest;
import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import weka.classifiers.Classifier;
import weka.core.DenseInstance;
import weka.core.Instances;
import weka.core.Attribute;

import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

@Service
public class PredictionService {

  private static final Logger logger = LoggerFactory.getLogger(PredictionService.class);

  private Classifier model;
  private Instances datasetStructure;
  private List<PatientPrediction> predictionResults = new ArrayList<>();
  private int currentId = 1;

  private String apiKey = "Bearer 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";

  public PredictionService() {
    try {
      loadModel();
      loadDatasetStructure();
    } catch (Exception e) {
      logger.error("Error al cargar el modelo o el dataset", e);
      throw new RuntimeException("Error al cargar el modelo o el dataset: " + e.getMessage(), e);
    }
  }

  private void loadModel() throws Exception {
    try (InputStream modelStream = getClass().getResourceAsStream("/models/modelocancerwinsonsim.model");
         ObjectInputStream ois = new ObjectInputStream(modelStream)) {
      model = (Classifier) ois.readObject();
      logger.info("Modelo cargado exitosamente");
    }
  }

  private void loadDatasetStructure() throws Exception {
    try (InputStream datasetStream = getClass().getResourceAsStream("/DataSet/breast-cancer-wisconsin.arff");
         InputStreamReader reader = new InputStreamReader(datasetStream)) {
      datasetStructure = new Instances(reader);
      datasetStructure.setClassIndex(datasetStructure.numAttributes() - 1);
      logger.info("Estructura del dataset cargada exitosamente");
      printAttributesInfo();
    }
  }

  private void printAttributesInfo() {
    logger.info("Atributos del dataset:");
    for (int i = 0; i < datasetStructure.numAttributes(); i++) {
      Attribute attr = datasetStructure.attribute(i);
      logger.info("Atributo {}: {}", i, attr.name());
      if (attr.isNominal()) {
        logger.info("  Tipo: Nominal");
        logger.info("  Valores: {}", attr.numValues());
        for (int j = 0; j < attr.numValues(); j++) {
          logger.info("    - {}", attr.value(j));
        }
      } else {
        logger.info("  Tipo: Numérico");
      }
    }
  }

  public String predict(PredictionRequest request) {
    try {
      DenseInstance instance = new DenseInstance(datasetStructure.numAttributes());
      instance.setDataset(datasetStructure);

      // Mapear los campos de entrada a los atributos del dataset
      instance.setValue(datasetStructure.attribute("V1"), request.getClumpThickness());
      instance.setValue(datasetStructure.attribute("V2"), request.getUniformityOfCellSize());
      instance.setValue(datasetStructure.attribute("V3"), request.getUniformityOfCellShape());
      instance.setValue(datasetStructure.attribute("V4"), request.getMarginalAdhesion());
      instance.setValue(datasetStructure.attribute("V5"), request.getSingleEpithelialCellSize());
      instance.setValue(datasetStructure.attribute("V6"), request.getBareNuclei());
      instance.setValue(datasetStructure.attribute("V7"), request.getBlandChromatin());
      instance.setValue(datasetStructure.attribute("V8"), request.getNormalNucleoli());
      instance.setValue(datasetStructure.attribute("V9"), request.getMitoses());

      double result = model.classifyInstance(instance);
      String predictedClass = datasetStructure.classAttribute().value((int) result);

      if (predictedClass == null || predictedClass.isEmpty()) {
        throw new IllegalArgumentException("La clase predicha es nula o vacía");
      }

      String prediction = "Resultado: " + translateClass(predictedClass);
      String advice = getAdviceFromAI(predictedClass, request);

      PatientPrediction predictionResult = new PatientPrediction(
        currentId++,
        prediction,
        advice,
        request
      );
      predictionResults.add(predictionResult);

      logger.info("Predicción realizada para paciente con ID {}: {}", currentId, prediction);
      logger.info("Consejo de IA: {}", advice != null ? advice : "No obtenido");

      return prediction + (advice != null ? "\n\nRecomendaciones de la IA:\n" + advice : "");

    } catch (Exception e) {
      logger.error("Error en la predicción", e);
      throw new RuntimeException("Error en la predicción: " + e.getMessage(), e);
    }
  }

  private String translateClass(String predictedClass) {
    switch (predictedClass) {
      case "1": return "Tumor benigno (bajo riesgo)";
      case "2": return "Tumor maligno (alto riesgo)";
      default: return "Diagnóstico: " + predictedClass;
    }
  }

  public List<PatientPrediction> getAllPredictions() {
    return new ArrayList<>(predictionResults);
  }

  private String getAdviceFromAI(String predictedClass, PredictionRequest predictionRequest) {
    try {
      OkHttpClient client = new OkHttpClient();

      String translatedClass = translateClass(predictedClass);
      String prompt = "Eres un oncólogo. Un paciente tiene las siguientes características de tumor mamario:\n" +
        "Grosor del tumor: " + predictionRequest.getClumpThickness() + " (escala 1-10)\n" +
        "Uniformidad del tamaño celular: " + predictionRequest.getUniformityOfCellSize() + " (escala 1-10)\n" +
        "Uniformidad de la forma celular: " + predictionRequest.getUniformityOfCellShape() + " (escala 1-10)\n" +
        "Adhesión marginal: " + predictionRequest.getMarginalAdhesion() + " (escala 1-10)\n" +
        "Tamaño de células epiteliales: " + predictionRequest.getSingleEpithelialCellSize() + " (escala 1-10)\n" +
        "Núcleos desnudos: " + predictionRequest.getBareNuclei() + " (escala 1-10)\n" +
        "Cromatina blanda: " + predictionRequest.getBlandChromatin() + " (escala 1-10)\n" +
        "Nucléolos normales: " + predictionRequest.getNormalNucleoli() + " (escala 1-10)\n" +
        "Mitosis: " + predictionRequest.getMitoses() + " (escala 1-10)\n\n" +
        "Diagnóstico: " + translatedClass + "\n\n" +
        "Proporciona recomendaciones médicas específicas basadas en estas características. " +
        "Incluye próximos pasos, tratamientos potenciales y consejos de estilo de vida. " +
        "Usa un lenguaje sencillo que el paciente pueda entender.";

      JSONObject json = new JSONObject();
      json.put("model", "gpt-3.5-turbo");

      JSONArray messages = new JSONArray();
      messages.put(new JSONObject()
        .put("role", "user")
        .put("content", prompt)
      );
      json.put("messages", messages);

      RequestBody body = RequestBody.create(MediaType.parse("application/json"), json.toString());

      Request httpRequest = new Request.Builder()
        .url("https://openrouter.ai/api/v1/chat/completions")
        .post(body)
        .addHeader("Authorization", apiKey)
        .addHeader("Content-Type", "application/json")
        .build();

      Response response = client.newCall(httpRequest).execute();

      if (!response.isSuccessful()) {
        throw new RuntimeException("Error al contactar OpenRouter: " + response);
      }

      ResponseBody responseBody = response.body();
      if (responseBody == null) {
        throw new RuntimeException("Respuesta vacía de OpenRouter");
      }

      String responseString = responseBody.string();
      JSONObject jsonObject = new JSONObject(responseString);
      JSONArray choicesArray = jsonObject.optJSONArray("choices");

      if (choicesArray == null || choicesArray.isEmpty()) {
        throw new RuntimeException("No se encontraron opciones de respuesta de la IA");
      }

      JSONObject firstChoice = choicesArray.getJSONObject(0);
      JSONObject messageObject = firstChoice.optJSONObject("message");

      return messageObject != null ? messageObject.optString("content", "Sin contenido") : "Respuesta sin mensaje";

    } catch (Exception e) {
      logger.error("Error al obtener consejo de IA para la clase {}", predictedClass, e);
      return "No se pudo obtener una recomendación de la IA en este momento.";
    }
  }
}
