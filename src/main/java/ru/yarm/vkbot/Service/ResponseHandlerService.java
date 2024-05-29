package ru.yarm.vkbot.Service;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ru.yarm.vkbot.Model.LongPollServerDto;
import ru.yarm.vkbot.Model.ResponseDto;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

@Service
public class ResponseHandlerService {

    private final RestTemplate restTemplate;

    @Value("${access_token}")
    private String token;

    @Value("${group_id}")
    private String group_id;

    @Value("${ver_api}")
    private String ver_api;

    public ResponseHandlerService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public LongPollServerDto getLongPollSettings() {
        String LongPollServerUrl = "https://api.vk.com/method/messages.getLongPollServer?access_token="
                + token + "&group_id=" + group_id + "&v=" + ver_api;
        // Посылка и получение ответа:
        String LongPollServerResponse = restTemplate.getForObject(LongPollServerUrl, String.class);
        // Парсим ответ и задаем настройки:
        Integer ts = getTs(LongPollServerResponse);
        String sessionKey = getSessionKey(LongPollServerResponse);
        String serverUrl = getServerUrl(LongPollServerResponse);
        return LongPollServerDto.builder()
                .ts(ts)
                .serverUrl(serverUrl)
                .sessionKey(sessionKey)
                .build();
    }

    public Integer requestLongPollCheck(LongPollServerDto longPollServerDto) {
        String longPollServerRequest = "https://" + longPollServerDto.getServerUrl() + "?act=a_check&key="
                + longPollServerDto.getSessionKey()
                + "&ts=" + longPollServerDto.getTs() + "&wait=25&mode=2&version=3";
        String longPollServerResponse = restTemplate.getForObject(longPollServerRequest, String.class);
        // Получаем новый номер события
        return getTsFromResponse(longPollServerResponse);
    }

    // Процедура для попытки ответа, если того требует ситуация
    public void tryToAnswer(ResponseDto responseDto) {
        Random random = new Random();
        int random_id = random.nextInt(10000);
        if (responseDto.getUsername() != null) {
            String myAnswer = "https://api.vk.com/method/messages.send?access_token=" + token + "&user_id="
                    + responseDto.getUsername() + "&peer_id=" + group_id + "&random_id=" + random_id + "&message="
                    + responseDto.getMessage() + "&v=" + ver_api;
            restTemplate.getForObject(myAnswer, String.class);
        }
    }

    public String requestContinueLongPollCheck(String url, String key, String ts) {
        String longPollServerRequest = "https://" + url + "?act=a_check&key=" + key
                + "&ts=" + ts + "&wait=25&mode=2&version=3";
        return restTemplate.getForObject(longPollServerRequest, String.class);
    }


    // Парсим JSON-ответ, получая новый номер события
    public Integer getTs(String response) {
        JSONObject jsonObject = new JSONObject(response);
        return jsonObject.getJSONObject("response").getInt("ts");
    }

    // Парсим JSON-ответ, получая новый номер события
    public Integer getTsFromResponse(String response) {
        JSONObject jsonObject = new JSONObject(response);
        return jsonObject.getInt("ts");
    }

    // Парсим JSON-ответ, получая секретный ключ сессии
    public String getSessionKey(String response) {
        JSONObject jsonObject = new JSONObject(response);
        return jsonObject.getJSONObject("response").getString("key");
    }

    // Парсим JSON-ответ, получая url-сервера VK
    public String getServerUrl(String response) {
        JSONObject jsonObject = new JSONObject(response);
        return jsonObject.getJSONObject("response").getString("server");
    }

    // Парсим JSON-ответ, получая тело самого ответа, если оно вообще имеет полезность
    public ResponseDto getUpdates(String response) {
        JSONObject jsonObject = new JSONObject(response);
        ResponseDto responseDto = ResponseDto.builder().build();
        List<String> body;
        // Обходим массив в теле JSON, забираем только тот элемент, где есть сообщение
        // И нет присутствия бота. "Вы писали" - написано с латинскими символами.
        // Как временная мера - исключения бота.
        for (Object o : jsonObject.getJSONArray("updates")) {
            if (o.toString().contains("title") && !o.toString().contains("Вы пиcaли")) {
                body = Arrays.stream(o.toString().split(",")).toList();
                responseDto.setUsername(body.get(3));
                responseDto.setMessage("Вы пиcaли: " + body.get(5));
            }
        }
        return responseDto;
    }
}
