package hackathon.chatbot;

import com.google.cloud.vertexai.VertexAI;
import com.google.cloud.vertexai.api.GenerateContentResponse;
import com.google.cloud.vertexai.generativeai.GenerativeModel;
import com.google.cloud.vertexai.generativeai.ResponseHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;

@Component
public class GeminiClient {

    @Value("${gemini.api-key}")
    private String geminiApiKey;

    @Value("${gemini.model-name}")
    private String geminiModelName;

    private VertexAI vertexAI;
    private GenerativeModel model;

    @PostConstruct
    public void init() throws IOException {
        String projectId = "gen-lang-client-0820148005";
        String location = "us-central1";

        String envVar = System.getenv("GOOGLE_APPLICATION_CREDENTIALS");
        System.out.println("GOOGLE_APPLICATION_CREDENTIALS environment variable: " + envVar);

        try {
            this.vertexAI = new VertexAI(projectId, location);
        } catch (NoClassDefFoundError | NoSuchMethodError e) {
            System.err.println("VertexAI 생성자를 찾을 수 없습니다. SDK 버전을 확인하거나 종속성을 확인하세요.");
            throw new IOException("VertexAI 초기화 오류: " + e.getMessage(), e);
        }

        this.model = new GenerativeModel(geminiModelName, vertexAI);
    }

    public String generateContent(String prompt) {
        try {
            GenerateContentResponse response = model.generateContent(prompt);
            return ResponseHandler.getText(response);
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