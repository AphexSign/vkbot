package ru.yarm.vkbot.Service;
import org.json.JSONObject;
import org.springframework.stereotype.Service;
import ru.yarm.vkbot.Model.ResponseDto;
import java.util.Arrays;
import java.util.List;

@Service
public class ResponseHandlerService {

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
        ResponseDto responseDto=ResponseDto.builder().build();
        List<String> body;
        // Обходим массив в теле JSON, забираем только тот элемент, где есть сообщение
        // И нет присутствия бота. "Вы писали" - написано с латинскими символами.
        // Как временная мера - исключения бота.
        for (Object o : jsonObject.getJSONArray("updates")) {
            if(o.toString().contains("title")&&!o.toString().contains("Вы пиcaли")){
                body= Arrays.stream(o.toString().split(",")).toList();
                responseDto.setUsername(body.get(3));
                responseDto.setMessage("Вы пиcaли: "+body.get(5));
            };
        }
        return responseDto;
    }
}
