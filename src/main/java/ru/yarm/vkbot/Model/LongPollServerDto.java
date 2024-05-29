package ru.yarm.vkbot.Model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LongPollServerDto {

    private Integer ts;
    private String sessionKey;
    private String serverUrl;


}
