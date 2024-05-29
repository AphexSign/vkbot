package ru.yarm.vkbot.Configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class ApplicationConf {

    @Bean
    public RestTemplate restTemplate(){
        return new RestTemplate();
    }


}
