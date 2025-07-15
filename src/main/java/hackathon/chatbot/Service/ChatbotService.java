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
        String placeInfo = kakaoMapClient.searchPlace(placeName);

        String prompt = String.format("사용자가 '%s' 장소에 대해 '%s'라고 질문했습니다. 다음 장소 정보를 참고하여 답변해주세요: %s"
                ,placeName, userQuestion, placeInfo);

        String geminiResponse = geminiClient.generateContent(prompt);

        return geminiResponse;
    }

}
