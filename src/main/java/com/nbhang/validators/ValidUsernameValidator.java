package com.nbhang.validators;

import com.nbhang.services.UserService;
import com.nbhang.validators.annotations.ValidUsername;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ValidUsernameValidator implements ConstraintValidator<ValidUsername, String> {

    @Autowired
    private UserService userService;

    @Override
    public boolean isValid(String username, ConstraintValidatorContext context) {
        if (userService == null) return true; // Tránh lỗi NullPointerException khi khởi tạo
        return userService.findByUsername(username).isEmpty();
    }
}