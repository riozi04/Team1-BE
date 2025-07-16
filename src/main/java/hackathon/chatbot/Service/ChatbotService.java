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
        String initialPrompt = String.format("'%s' 주변에 대해 '%s'라고 질문했습니다. 이 질문에 답하기 위해 적절한 장소를 검색해야 합니다." +
                        "\n\n장소 검색 결과가 있다면, 다음 양식에 맞춰 장소를 추천해주세요:" +
                        "\n\n[번호]. [장소 이름] ([장소 카테고리])" +
                        "\n[장소 주소]" +
                        "\n[장소 전화번호]" +
                        "\n[장소에 대한 모델의 간략한 설명 (특징, 분위기, 추천 대상 등)]" +
                        "\n\n각 추천 장소는 서로 다른 줄로 구분해주세요." +
                        "\n만약 검색 결과가 없다면, '죄송합니다. 요청하신 조건에 맞는 장소를 찾을 수 없습니다. 다른 검색 조건을 알려주시겠어요?'와 같이 응답해주세요.",
                placeName, userQuestion);

        String geminiResponse = geminiClient.getGeminiResponse(initialPrompt, kakaoMapClient);

        return geminiResponse;
    }
}