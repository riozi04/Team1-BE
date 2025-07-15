package hackathon.chatbot;

import com.google.cloud.vertexai.VertexAI;
import com.google.cloud.vertexai.api.GenerateContentResponse;
import com.google.cloud.vertexai.api.Tool;
import com.google.cloud.vertexai.api.FunctionDeclaration;
import com.google.cloud.vertexai.api.Schema;
import com.google.cloud.vertexai.api.Type;
import com.google.cloud.vertexai.api.Part;
import com.google.cloud.vertexai.api.Content;
import com.google.cloud.vertexai.api.FunctionResponse;
import com.google.cloud.vertexai.generativeai.GenerativeModel;
import com.google.cloud.vertexai.generativeai.ResponseHandler;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.google.protobuf.Struct;
import com.google.protobuf.ListValue;

@Component
public class GeminiClient {

    @Value("${gemini.api-key}")
    private final String geminiApiKey;

    @Value("${gemini.model-name}")
    private final String geminiModelName;

    @Value("${GOOGLE_CLOUD_PROJECT_ID}")
    private final String projectId;

    private VertexAI vertexAI;
    private GenerativeModel model;
    private final ObjectMapper objectMapper;

    public GeminiClient(
            ObjectMapper objectMapper,
            @Value("${gemini.api-key}") String geminiApiKey,
            @Value("${gemini.model-name}") String geminiModelName,
            @Value("${GOOGLE_CLOUD_PROJECT_ID}") String projectId
    ) {
        this.objectMapper = objectMapper;
        this.geminiApiKey = geminiApiKey;
        this.geminiModelName = geminiModelName;
        this.projectId = projectId;
    }

    @PostConstruct
    public void init() throws IOException {
        String location = "us-central1";

        try {
            this.vertexAI = new VertexAI(projectId, location);

            FunctionDeclaration searchPlacesFunction = FunctionDeclaration.newBuilder()
                    .setName("search_places")
                    .setDescription("주변의 장소(식당, 카페 등)를 검색합니다. 특정 장소 이름과 검색 쿼리(음식 종류, 카테고리 등)를 입력받습니다.")
                    .setParameters(Schema.newBuilder()
                            .setType(Type.OBJECT)
                            .putProperties("query", Schema.newBuilder()
                                    .setType(Type.STRING)
                                    .setDescription("검색할 장소의 이름과 종류 (예: '강남역 중식당', '홍대 카페', '판교역 맛집')")
                                    .build())
                            .addRequired("query")
                            .build())
                    .build();

            this.model = new GenerativeModel(geminiModelName, vertexAI)
                    .withTools(Arrays.asList(Tool.newBuilder().addFunctionDeclarations(searchPlacesFunction).build()));

        } catch (NoClassDefFoundError | NoSuchMethodError e) {
            System.err.println("VertexAI 생성자를 찾을 수 없습니다. SDK 버전을 확인하거나 종속성을 확인하세요.");
            throw new IOException("VertexAI 초기화 오류: " + e.getMessage(), e);
        }
    }

    public String getGeminiResponse(String userPrompt, KakaoMapClient kakaoMapClient) {
        try {
            GenerateContentResponse response = model.generateContent(userPrompt);
            Content modelContent = response.getCandidates(0).getContent();

            if (modelContent.getPartsCount() > 0 && modelContent.getParts(0).hasFunctionCall()) {
                String functionName = modelContent.getParts(0).getFunctionCall().getName();

                if ("search_places".equals(functionName)) {
                    Struct functionArgs = modelContent.getParts(0).getFunctionCall().getArgs();
                    String query = null;
                    if (functionArgs.getFieldsMap().containsKey("query")) {
                        com.google.protobuf.Value queryValue = functionArgs.getFieldsMap().get("query");
                        if (queryValue.hasStringValue()) {
                            query = queryValue.getStringValue();
                        }
                    }

                    if (query == null || query.isEmpty()) {
                        return "Gemini가 검색 쿼리를 제대로 생성하지 못했습니다. 질문을 명확히 해주세요.";
                    }

                    System.out.println("Gemini가 카카오맵 검색을 제안했습니다. 검색 쿼리: " + query);

                    List<Map<String, String>> places = kakaoMapClient.searchPlace(query);

                    Struct.Builder responseStructBuilder = Struct.newBuilder();

                    if (places.isEmpty()) {
                        responseStructBuilder.putFields("status", com.google.protobuf.Value.newBuilder().setStringValue("No results").build());
                        responseStructBuilder.putFields("message", com.google.protobuf.Value.newBuilder().setStringValue("No places found for query: " + query).build());
                    } else {
                        List<com.google.protobuf.Value> placeValueList = new java.util.ArrayList<>();
                        for (Map<String, String> place : places) {
                            Struct.Builder placeStructBuilder = Struct.newBuilder();
                            place.forEach((key, val) -> placeStructBuilder.putFields(key, com.google.protobuf.Value.newBuilder().setStringValue(val).build()));
                            placeValueList.add(com.google.protobuf.Value.newBuilder().setStructValue(placeStructBuilder.build()).build());
                        }
                        responseStructBuilder.putFields("places", com.google.protobuf.Value.newBuilder().setListValue(ListValue.newBuilder().addAllValues(placeValueList).build()).build());
                        responseStructBuilder.putFields("status", com.google.protobuf.Value.newBuilder().setStringValue("success").build());
                    }
                    Struct toolOutputStruct = responseStructBuilder.build();

                    GenerateContentResponse finalResponse = model.generateContent(Arrays.asList(
                            Content.newBuilder()
                                    .setRole("user")
                                    .addParts(Part.newBuilder().setText(userPrompt).build())
                                    .build(),
                            modelContent,
                            Content.newBuilder()
                                    .addParts(Part.newBuilder()
                                            .setFunctionResponse(
                                                    FunctionResponse.newBuilder()
                                                            .setName(functionName)
                                                            .setResponse(toolOutputStruct)
                                                            .build())
                                            .build())
                                    .build()
                    ));
                    return ResponseHandler.getText(finalResponse);

                } else {
                    return "Gemini가 알 수 없는 함수를 호출하려 했습니다: " + functionName;
                }
            } else {
                return ResponseHandler.getText(response);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return "Gemini API 호출 중 오류가 발생했습니다: " + e.getMessage();
        }
    }

    @PreDestroy
    public void cleanup() throws IOException {
        if (vertexAI != null) {
            vertexAI.close();
        }
    }
}