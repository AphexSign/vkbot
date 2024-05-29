package ru.yarm.vkbot.Model;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Data
@Builder
@Setter
public class ResponseDto {
    private String username;
    private String message;
}
