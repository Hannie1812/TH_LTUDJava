package com.nbhang.controllers;

import com.nbhang.services.CartService;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {
    private final CartService cartService;
    private final com.nbhang.services.UserService userService;
    private final com.nbhang.services.InvoiceService invoiceService;

    @GetMapping
    public String showCart(HttpSession session,
            @NotNull Model model) {
        model.addAttribute("cart", cartService.getCart(session));
        model.addAttribute("totalPrice",
                cartService.getSumPrice(session));
        model.addAttribute("totalQuantity",
                cartService.getSumQuantity(session));
        return "book/cart";
    }

    @GetMapping("/removeFromCart/{id}")
    public String removeFromCart(HttpSession session,
            @PathVariable Long id) {
        var cart = cartService.getCart(session);
        cart.removeItems(id);
        return "redirect:/cart";
    }

    @GetMapping("/updateCart/{id}/{quantity}")
    public String updateCart(HttpSession session,
            @PathVariable Long id,
            @PathVariable int quantity,
            RedirectAttributes redirectAttributes) {
        String errorMessage = cartService.updateCartItem(session, id, quantity);
        if (errorMessage != null) {
            redirectAttributes.addFlashAttribute("error", errorMessage);
        }
        return "redirect:/cart";
    }

    @GetMapping("/clearCart")
    public String clearCart(HttpSession session) {
        cartService.removeCart(session);
        return "redirect:/cart ";
    }

    @GetMapping("/checkout")
    public String checkout(HttpSession session, org.springframework.security.core.Authentication authentication,
            Model model) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }
        String username = authentication.getName();
        com.nbhang.entities.User user = userService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        model.addAttribute("cart", cartService.getCart(session));
        model.addAttribute("totalPrice", cartService.getSumPrice(session));
        model.addAttribute("user", user);
        return "book/checkout";
    }

    @PostMapping("/checkout")
    public String placeOrder(HttpSession session,
            org.springframework.security.core.Authentication authentication,
            @RequestParam("shippingAddress") String shippingAddress,
            @RequestParam("paymentMethod") String paymentMethod,
            RedirectAttributes redirectAttributes) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }
        String username = authentication.getName();
        com.nbhang.entities.User user = userService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        try {
            com.nbhang.entities.Invoice invoice = cartService.saveCart(session, user, shippingAddress, paymentMethod);
            if ("QR".equals(paymentMethod)) {
                return "redirect:/cart/payment-success?orderId=" + invoice.getId();
            }
            redirectAttributes.addFlashAttribute("successMessage", "Đặt hàng thành công!");
            return "redirect:/books";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Đặt hàng thất bại: " + e.getMessage());
            return "redirect:/cart";
        }
    }

    @GetMapping("/payment-success")
    public String paymentSuccess(@RequestParam("orderId") Long orderId, Model model) {
        com.nbhang.entities.Invoice invoice = invoiceService.getInvoiceById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        model.addAttribute("invoice", invoice);
        model.addAttribute("orderId", orderId);
        return "book/payment-success";
    }
}
