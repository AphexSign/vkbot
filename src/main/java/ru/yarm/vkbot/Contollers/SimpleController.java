package ru.yarm.vkbot.Contollers;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.ModelAndView;
import ru.yarm.vkbot.Model.ResponseDto;
import ru.yarm.vkbot.Service.ResponseHandlerService;

import java.util.Random;

@RestController
public class SimpleController {

    private final RestTemplate restTemplate;
    private final ResponseHandlerService responseHandlerService;

    public SimpleController(RestTemplate restTemplate, ResponseHandlerService responseHandlerService) {
        this.restTemplate = restTemplate;
        this.responseHandlerService = responseHandlerService;
    }

    @Value("${access_token}")
    private String token;

    @Value("${group_id}")
    private String group_id;

    @Value("${ver_api}")
    private String ver_api;

    // Первый запрос - как того требует VK-API, для получения начальных параметров
    @GetMapping("/start")
    public ModelAndView listingVk() {
        // Формируем наш запрос, подразумевая, что у нас уже есть токен, номер группы и версия API
        String LongPollServerUrl = "https://api.vk.com/method/messages.getLongPollServer?access_token="
                + token + "&group_id=" + group_id + "&v=" + ver_api;
        // Посылка и получение ответа:
        String LongPollServerResponse = restTemplate.getForObject(LongPollServerUrl, String.class);
        // Парсим ответ и задаем настройки:
        Integer ts = responseHandlerService.getTs(LongPollServerResponse);
        String sessionKey = responseHandlerService.getSessionKey(LongPollServerResponse);
        String serverUrl = responseHandlerService.getServerUrl(LongPollServerResponse);
        // Обращаемся с запросом к Long Poll Api - сервер начнет нам присылать наши параметры:
        String longPollServerRequest = "https://" + serverUrl + "?act=a_check&key=" + sessionKey
                + "&ts=" + ts + "&wait=25&mode=2&version=3";
        String longPollServerResponse = restTemplate.getForObject(longPollServerRequest, String.class);
        // Получаем новый номер события
        Integer tsNew = responseHandlerService.getTsFromResponse(longPollServerResponse);
        // Делаем редирект, замыкая нашего "бота" на получение нового события
        return new ModelAndView("redirect:/continue?url=" + serverUrl + "&key=" + sessionKey + "&ts=" + tsNew);
    }

    // Циклически получаем новую Ts - и работаем с ней
    @GetMapping("/continue")
    public ModelAndView continueVk(@RequestParam String url,
                                   @RequestParam String key,
                                   @RequestParam String ts) {
        // Повторно отправляем новые параметры в запрос
        String longPollServerRequest = "https://" + url + "?act=a_check&key=" + key
                + "&ts=" + ts + "&wait=25&mode=2&version=3";
        String longPollServerResponse = restTemplate.getForObject(longPollServerRequest, String.class);
        Integer tsNew = responseHandlerService.getTsFromResponse(longPollServerResponse);
        // Парсим наш ответ
        ResponseDto responseDto = responseHandlerService.getUpdates(longPollServerResponse);
        // Генерируем случайное число, на случай если придется отправлять ответ User'у
        Random random = new Random();
        int random_id = random.nextInt(10000);
        // Отправляем ответ, если есть кому отправлять
        if (responseDto.getUsername() != null) {
            String myAnswer = "https://api.vk.com/method/messages.send?access_token=" + token + "&user_id="
                    + responseDto.getUsername() + "&peer_id=" + group_id + "&random_id=" + random_id + "&message="
                    + responseDto.getMessage() + "&v=" + ver_api;
            restTemplate.getForObject(myAnswer, String.class);
        }
        // Вызываем сами себя
        return new ModelAndView("redirect:/continue?url=" + url + "&key=" + key + "&ts=" + tsNew);
    }

    @GetMapping("/exit")
    public void exit() {
        System.exit(0);
    }


}
