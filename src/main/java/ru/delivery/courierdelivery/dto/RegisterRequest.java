package ru.delivery.courierdelivery.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "Имя обязательно") String name,
        @Email(message = "Некорректный email") @NotBlank(message = "Email обязателен") String email,
        @NotBlank(message = "Пароль обязателен")
        @Size(min = 8, message = "Пароль не короче 8 символов")
        String password
) {
}
