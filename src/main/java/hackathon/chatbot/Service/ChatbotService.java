package hackathon.chatbot.Service;

import hackathon.chatbot.GeminiClient;
import hackathon.chatbot.KakaoMapClient;
import org.springframework.stereotype.Service;

@Service
public class ChatbotService {

    private final KakaoMapClient kakaoMapClient;
    private final GeminiClient geminiClient;

    public ChatbotService(KakaoMapClient kakaoMapClient, GeminiClient geminiClient) {
        this.kakaoMapClient = kakaoMapClient;
        this.geminiClient = geminiClient;
    }

    public String getChatbotResponse(String placeName, String userQuestion) {
        String initialPrompt = String.format("'%s' 주변에 대해 '%s'라고 질문했습니다. 이 질문에 답하기 위해 적절한 장소를 검색해야 합니다.",
                placeName, userQuestion);

        String geminiResponse = geminiClient.getGeminiResponse(initialPrompt, kakaoMapClient);

        return geminiResponse;
    }
}