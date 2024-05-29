package ru.yarm.vkbot.Contollers;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.ModelAndView;
import ru.yarm.vkbot.Model.LongPollServerDto;
import ru.yarm.vkbot.Model.ResponseDto;
import ru.yarm.vkbot.Service.ResponseHandlerService;

import java.util.Random;

@RestController
public class SimpleController {

    private final ResponseHandlerService responseHandlerService;

    public SimpleController(ResponseHandlerService responseHandlerService) {
        this.responseHandlerService = responseHandlerService;
    }


    // Первый запрос - как того требует VK-API, для получения начальных параметров
    @GetMapping("/start")
    public ModelAndView listenVk() {
        LongPollServerDto longPollServerDto = responseHandlerService.getLongPollSettings();
        Integer tsNew = responseHandlerService.requestLongPollCheck(longPollServerDto);
        return new ModelAndView("redirect:/continue?url=" + longPollServerDto.getServerUrl()
                + "&key=" + longPollServerDto.getSessionKey()
                + "&ts=" + tsNew);
    }

    // Циклически получаем новую Ts - и работаем с ней
    @GetMapping("/continue")
    public ModelAndView continueVk(@RequestParam String url,
                                   @RequestParam String key,
                                   @RequestParam String ts) {

        String longPollResponse = responseHandlerService.requestContinueLongPollCheck(url, key, ts);
        Integer tsNew = responseHandlerService.getTsFromResponse(longPollResponse);
        ResponseDto responseDto = responseHandlerService.getUpdates(longPollResponse);
        responseHandlerService.tryToAnswer(responseDto);
        return new ModelAndView("redirect:/continue?url=" + url + "&key=" + key + "&ts=" + tsNew);
    }

    @GetMapping("/exit")
    public void exit() {
        System.exit(0);
    }


}
