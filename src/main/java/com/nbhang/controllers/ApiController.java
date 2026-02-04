package com.nbhang.controllers;

import com.nbhang.entities.Book;
import com.nbhang.entities.Category;
import com.nbhang.services.BookService;
import com.nbhang.services.CartService;
import com.nbhang.services.CategoryService;
import com.nbhang.viewmodels.BookGetVm;
import com.nbhang.viewmodels.BookPostVm;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class ApiController {
        private final BookService bookService;
        private final CategoryService categoryService;
        private final CartService cartService;

        @GetMapping("/books")
        public ResponseEntity<List<BookGetVm>> getAllBooks(Integer pageNo,
                        Integer pageSize, String sortBy) {
                return ResponseEntity.ok(bookService.getAllBooks(
                                pageNo == null ? 0 : pageNo,
                                pageSize == null ? 20 : pageSize,
                                sortBy == null ? "id" : sortBy)
                                .stream()
                                .map(BookGetVm::from)
                                .toList());
        }

        @GetMapping("/books/id/{id}")
        public ResponseEntity<BookGetVm> getBookById(@PathVariable Long id) {
                return ResponseEntity.ok(bookService.getBookById(id)
                                .map(BookGetVm::from)
                                .orElse(null));
        }

        @DeleteMapping("/books/{id}")
        public ResponseEntity<String> deleteBookById(@PathVariable Long id) {
                if (!bookService.getBookById(id).isPresent()) {
                        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Book not found");
                }
                bookService.deleteBookById(id);
                return ResponseEntity.ok("Book deleted successfully");
        }

        @GetMapping("/books/search")
        public ResponseEntity<List<BookGetVm>> searchBooks(String keyword) {
                return ResponseEntity.ok(bookService.searchBook(keyword)
                                .stream()
                                .map(BookGetVm::from)
                                .toList());
        }

        @PostMapping("/books")
        public ResponseEntity<BookGetVm> createBook(@Valid @RequestBody BookPostVm bookPostVm) {
                Category category = categoryService.getCategoryById(bookPostVm.categoryId())
                                .orElseThrow(() -> new RuntimeException(
                                                "Category not found with id: " + bookPostVm.categoryId()));

                Book book = Book.builder()
                                .title(bookPostVm.title())
                                .author(bookPostVm.author())
                                .price(bookPostVm.price())
                                .category(category)
                                .build();

                Book savedBook = bookService.addBook(book);
                return new ResponseEntity<>(BookGetVm.from(savedBook), HttpStatus.CREATED);
        }

        @PutMapping("/books/{id}")
        public ResponseEntity<BookGetVm> updateBook(@PathVariable Long id, @Valid @RequestBody BookPostVm bookPostVm) {
                Category category = categoryService.getCategoryById(bookPostVm.categoryId())
                                .orElseThrow(() -> new RuntimeException(
                                                "Category not found with id: " + bookPostVm.categoryId()));

                Book book = Book.builder()
                                .id(id)
                                .title(bookPostVm.title())
                                .author(bookPostVm.author())
                                .price(bookPostVm.price())
                                .category(category)
                                .build();

                bookService.updateBook(book);
                Book updatedBook = bookService.getBookById(id)
                                .orElseThrow(() -> new RuntimeException("Book not found after update"));

                return ResponseEntity.ok(BookGetVm.from(updatedBook));
        }
}
