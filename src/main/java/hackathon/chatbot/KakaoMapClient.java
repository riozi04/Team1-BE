package hackathon.chatbot;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpHeaders;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class KakaoMapClient {

    private final ObjectMapper objectMapper;
    @Value("${kakao.api-key}")
    private String kakaoApiKey;

    private final WebClient webClient;

    public KakaoMapClient(WebClient.Builder webClientBuilder, ObjectMapper objectMapper, @Value("${kakao.api-key}") String kakaoApiKey) {
        this.kakaoApiKey = kakaoApiKey; // 주입받은 값으로 초기화
        this.webClient = webClientBuilder.baseUrl("https://dapi.kakao.com/v2/local")
                .defaultHeader(HttpHeaders.AUTHORIZATION, "KakaoAK " + this.kakaoApiKey) // 초기화된 키 사용
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .build();
        this.objectMapper = objectMapper;
    }

    public String searchPlace(String query) {
        String jsonResponse = webClient.get()
                .uri(uriBuilder -> uriBuilder.path("/search/keyword.json")
                        .queryParam("query", query)
                        .build())
                .retrieve()
                .bodyToMono(String.class)
                .block();

        if (jsonResponse != null) {
            try {
                JsonNode root = objectMapper.readTree(jsonResponse);
                JsonNode documents = root.path("documents");
                if (documents.isArray() && documents.size() > 0) {
                    JsonNode firstPlace = documents.get(0);
                    String placeName = firstPlace.path("place_name").asText();
                    String addressName = firstPlace.path("address_name").asText();
                    String categoryName = firstPlace.path("category_name").asText();

                    return String.format("%s (주소: %s, 카테고리: %s)", placeName, addressName, categoryName);
                }
            } catch (Exception e) {
                e.printStackTrace();
                return "장소 검색 결과 파싱 중 오류: " + e.getMessage();
            }
        }
        return "장소 검색 결과: 정보 없음";
    }
}
