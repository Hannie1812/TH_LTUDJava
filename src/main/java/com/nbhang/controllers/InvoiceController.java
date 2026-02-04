package com.nbhang.controllers;

import com.nbhang.entities.Invoice;
import com.nbhang.entities.User;
import com.nbhang.services.InvoiceService;
import com.nbhang.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Controller
@RequiredArgsConstructor
@Slf4j
public class InvoiceController {
    private final com.nbhang.services.InvoiceService invoiceService;
    private final com.nbhang.services.FileStorageService fileStorageService;
    private final UserService userService;

    // User viewing their own orders
    @GetMapping("/orders")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'USER')")
    public String userOrders(Authentication authentication, Model model) {
        String username = authentication.getName();
        User user = userService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Invoice> orders = invoiceService.getInvoicesByUser(user);
        model.addAttribute("orders", orders);
        return "invoice/user/list";
    }

    // Admin viewing all orders
    @GetMapping("/admin/orders")
    @PreAuthorize("hasAuthority('ADMIN')")
    public String adminOrders(Model model) {
        List<Invoice> orders = invoiceService.getAllInvoices();
        model.addAttribute("orders", orders);
        return "invoice/admin/list";
    }

    // Admin updating order status
    @PostMapping("/admin/orders/update-status/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public String updateStatus(@PathVariable Long id, @RequestParam("status") String status,
            RedirectAttributes redirectAttributes) {
        log.info("Attempting to update status for invoice ID: {} to {}", id, status);
        try {
            invoiceService.updateStatus(id, status);
            log.info("Successfully updated status for invoice ID: {}", id);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật trạng thái thành công!");
        } catch (Exception e) {
            log.error("Failed to update status for invoice ID: {}", id, e);
            redirectAttributes.addFlashAttribute("errorMessage", "Cập nhật trạng thái thất bại: " + e.getMessage());
        }
        return "redirect:/admin/orders";
    }

    // User viewing order details
    @GetMapping("/orders/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'USER')")
    public String viewOrder(@PathVariable Long id, Authentication authentication, Model model,
            RedirectAttributes redirectAttributes) {
        log.info("User {} attempting to view order detail for ID: {}", authentication.getName(), id);
        try {
            Invoice invoice = invoiceService.getInvoiceById(id)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));

            // Check if the user is the owner or an admin
            boolean isAdmin = authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ADMIN"));

            String currentUsername = authentication.getName();
            // Check if user is linked to invoice
            boolean isOwner = invoice.getUser() != null &&
                    invoice.getUser().getUsername() != null &&
                    invoice.getUser().getUsername().equalsIgnoreCase(currentUsername);

            if (!isAdmin && !isOwner) {
                log.warn("Access denied. Current User: '{}', Invoice Owner: '{}' (Invoice ID: {})",
                        currentUsername,
                        (invoice.getUser() != null ? invoice.getUser().getUsername() : "null"),
                        id);
                redirectAttributes.addFlashAttribute("errorMessage", "Bạn không có quyền xem đơn hàng này.");
                return "redirect:/orders";
            }

            model.addAttribute("invoice", invoice);
            return "invoice/detail";
        } catch (Exception e) {
            log.error("Error viewing order ID: {}", id, e);
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/orders";
        }
    }

    // Admin viewing order details
    @GetMapping("/admin/orders/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public String adminViewOrder(@PathVariable Long id, Model model) {
        Invoice invoice = invoiceService.getInvoiceById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));
        model.addAttribute("invoice", invoice);
        return "invoice/detail";
    }

    @PostMapping("/invoices/{id}/upload-proof")
    public String uploadProof(@PathVariable("id") Long id,
            @RequestParam("file") org.springframework.web.multipart.MultipartFile file,
            RedirectAttributes redirectAttributes) {
        try {
            String fileName = fileStorageService.storeInvoiceProof(file, id);
            invoiceService.updatePaymentProof(id, fileName);
            redirectAttributes.addFlashAttribute("successMessage", "Tải lên ảnh minh chứng thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Tải lên thất bại: " + e.getMessage());
        }
        return "redirect:/orders/" + id;
    }
}
