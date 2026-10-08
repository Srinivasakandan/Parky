package com.smartparking.dto;

import jakarta.validation.constraints.NotBlank;

public class ANPRRequestDto {
    @NotBlank(message = "Vehicle number input cannot be blank")
    private String input;

    public ANPRRequestDto() {}

    public ANPRRequestDto(String input) {
        this.input = input;
    }

    public String getInput() { return input; }
    public void setInput(String input) { this.input = input; }
}
