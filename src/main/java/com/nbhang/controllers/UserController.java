package com.nbhang.controllers;

import com.nbhang.entities.Role;
import com.nbhang.entities.User;
import com.nbhang.repositories.IRoleRepository;
import com.nbhang.services.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/")
public class UserController {
    @Autowired
    private UserService userService;
    @Autowired
    private IRoleRepository roleRepository;

    @GetMapping("/login")
    public String login() {
        return "user/login";
    }

    @GetMapping("/register")
    public String register(@NotNull Model model) {
        model.addAttribute("user", new User());
        return "user/register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("user") User user,
            @NotNull BindingResult bindingResult,
            Model model) {
        if (bindingResult.hasErrors()) {
            var errors = bindingResult.getAllErrors()
                    .stream()
                    .map(DefaultMessageSourceResolvable::getDefaultMessage)
                    .toArray(String[]::new);
            model.addAttribute("errors", errors);
            return "user/register";
        }
        userService.save(user);
        userService.setDefaultRole(user.getUsername());
        return "redirect:/login";
    }

    @GetMapping("/profile")
    @PreAuthorize("hasAnyAuthority('USER', 'ADMIN')")
    public String profile(Authentication authentication, @NotNull Model model) {
        String username = authentication.getName();
        User user = userService.findByUsername(username).orElseThrow(() -> new RuntimeException("User not found"));
        model.addAttribute("user", user);
        return "user/profile";
    }

    @PostMapping("/profile")
    @PreAuthorize("hasAnyAuthority('USER', 'ADMIN')")
    public String updateProfile(@RequestParam("email") String email,
            @RequestParam(value = "phone", required = false) String phone,
            Authentication authentication,
            Model model,
            RedirectAttributes redirectAttributes) {
        String username = authentication.getName();
        User existingUser = userService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Update only the editable fields
        existingUser.setEmail(email);
        existingUser.setPhone(phone);

        try {
            userService.updateUser(existingUser);
            redirectAttributes.addFlashAttribute("success", "Profile updated successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to update profile: " + e.getMessage());
        }
        return "redirect:/profile";
    }

    @PostMapping("/profile/change-password")
    @PreAuthorize("hasAnyAuthority('USER', 'ADMIN')")
    public String changePassword(@RequestParam("currentPassword") String currentPassword,
            @RequestParam("newPassword") String newPassword,
            @RequestParam("confirmPassword") String confirmPassword,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
        String username = authentication.getName();
        if (!newPassword.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("error", "New passwords do not match");
            return "redirect:/profile";
        }
        try {
            userService.changePassword(username, currentPassword, newPassword);
            redirectAttributes.addFlashAttribute("success", "Password changed successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/profile";
    }

    @GetMapping("/admin/users")
    @PreAuthorize("hasAuthority('ADMIN')")
    public String listUsers(@NotNull Model model) {
        List<User> users = userService.findAllUsers();
        model.addAttribute("users", users);
        return "user/admin/list";
    }

    @GetMapping("/admin/users/add")
    @PreAuthorize("hasAuthority('ADMIN')")
    public String addUser(@NotNull Model model) {
        model.addAttribute("user", new User());
        return "user/admin/add";
    }

    @PostMapping("/admin/users/add")
    @PreAuthorize("hasAuthority('ADMIN')")
    public String addUser(@Valid @ModelAttribute("user") User user,
            @NotNull BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            var errors = bindingResult.getAllErrors()
                    .stream()
                    .map(DefaultMessageSourceResolvable::getDefaultMessage)
                    .toArray(String[]::new);
            model.addAttribute("errors", errors);
            return "user/admin/add";
        }
        userService.save(user);
        userService.setDefaultRole(user.getUsername());
        redirectAttributes.addFlashAttribute("success", "User added successfully");
        return "redirect:/admin/users";
    }

    @GetMapping("/admin/users/edit/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public String editUser(@PathVariable Long id, @NotNull Model model) {
        User user = userService.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
        model.addAttribute("user", user);
        return "user/admin/edit";
    }

    @PostMapping("/admin/users/edit/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public String updateUser(@PathVariable Long id,
            @RequestParam("email") String email,
            @RequestParam(value = "phone", required = false) String phone,
            @RequestParam(value = "roles", required = false) List<String> selectedRoles,
            Model model,
            RedirectAttributes redirectAttributes) {
        User user = userService.findById(id).orElseThrow(() -> new RuntimeException("User not found"));

        // Update editable fields
        user.setEmail(email);
        user.setPhone(phone);

        // Handle roles
        user.getRoles().clear();
        if (selectedRoles != null) {
            for (String roleName : selectedRoles) {
                Role role = roleRepository.findByName(roleName);
                if (role != null) {
                    user.getRoles().add(role);
                }
            }
        }

        try {
            userService.updateUser(user);
            redirectAttributes.addFlashAttribute("success", "User updated successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to update user: " + e.getMessage());
            return "redirect:/admin/users/edit/" + id;
        }
        return "redirect:/admin/users";
    }

    @PostMapping("/admin/users/delete/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public String deleteUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        userService.deleteUser(id);
        redirectAttributes.addFlashAttribute("success", "User deleted successfully");
        return "redirect:/admin/users";
    }
}
