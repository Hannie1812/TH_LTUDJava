package com.nbhang.controllers;

import com.nbhang.entities.Book;
import com.nbhang.services.BookService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/")
@RequiredArgsConstructor
public class HomeController {
    private final BookService bookService;

    @GetMapping
    public String home(Model model, Authentication authentication) {
        if (authentication != null && authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ADMIN"))) {
            List<Book> lowStockBooks = bookService.getAllBooks(0, Integer.MAX_VALUE, "id")
                    .stream()
                    .filter(book -> book.getQuantity() != null && book.getQuantity() <= 1)
                    .toList();
            if (!lowStockBooks.isEmpty()) {
                model.addAttribute("lowStockAlerts", lowStockBooks);
            }
        }
        return "home/index";
    }
}
