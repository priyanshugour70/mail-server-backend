package com.lssgoo.mail.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class APIResponse<T> {

    private boolean success;
    private String message;
    private List<Error> errors;
    private T data;
    private LocalDateTime timestamp;

}
