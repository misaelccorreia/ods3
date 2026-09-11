package com.projeto.ods3.controller;

import com.projeto.ods3.dto.auth.LoginRequestDTO;
import com.projeto.ods3.dto.auth.LoginResponseDTO;
import com.projeto.ods3.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public LoginResponseDTO login(@RequestBody @Valid LoginRequestDTO dados) {
        return authService.login(dados);
    }
}
