package com.nbhang.services;

import com.nbhang.daos.Cart;
import com.nbhang.daos.Item;
import com.nbhang.entities.Invoice;
import com.nbhang.entities.ItemInvoice;
import com.nbhang.repositories.IBookRepository;
import com.nbhang.repositories.IInvoiceRepository;
import com.nbhang.repositories.IItemInvoiceRepository;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import java.util.Date;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(isolation = Isolation.SERIALIZABLE, rollbackFor = { Exception.class, Throwable.class })
public class CartService {
    private static final String CART_SESSION_KEY = "cart";
    private final IInvoiceRepository invoiceRepository;
    private final IItemInvoiceRepository itemInvoiceRepository;
    private final IBookRepository bookRepository;

    public Cart getCart(@NotNull HttpSession session) {
        return Optional.ofNullable((Cart) session.getAttribute(CART_SESSION_KEY))
                .orElseGet(() -> {
                    Cart cart = new Cart();
                    session.setAttribute(CART_SESSION_KEY, cart);
                    return cart;
                });
    }

    public void updateCart(@NotNull HttpSession session, Cart cart) {
        session.setAttribute(CART_SESSION_KEY, cart);
    }

    public String updateCartItem(@NotNull HttpSession session, Long bookId, int quantity) {
        var cart = getCart(session);
        var book = bookRepository.findById(bookId).orElse(null);
        if (book == null) {
            return "Sách không tồn tại";
        }
        if (quantity > book.getQuantity()) {
            return "Số lượng tồn kho không đủ. Chỉ còn " + book.getQuantity() + " sản phẩm.";
        }
        cart.updateItems(bookId, quantity);
        return null;
    }

    public void removeCart(@NotNull HttpSession session) {
        session.removeAttribute(CART_SESSION_KEY);
    }

    public int getSumQuantity(@NotNull HttpSession session) {
        return getCart(session).getCartItems().stream()
                .mapToInt(Item::getQuantity)
                .sum();
    }

    public double getSumPrice(@NotNull HttpSession session) {
        return getCart(session).getCartItems().stream()
                .mapToDouble(item -> item.getPrice() *
                        item.getQuantity())
                .sum();
    }

    public Invoice saveCart(@NotNull HttpSession session, com.nbhang.entities.User user, String shippingAddress,
            String paymentMethod) {
        var cart = getCart(session);
        if (cart.getCartItems().isEmpty())
            return null;
        var invoice = new Invoice();
        invoice.setInvoiceDate(new Date());
        invoice.setPrice(getSumPrice(session));
        invoice.setUser(user);
        invoice.setShippingAddress(shippingAddress);
        invoice.setStatus("Mới đặt");
        invoice.setPaymentMethod(paymentMethod);
        invoiceRepository.save(invoice);
        cart.getCartItems().forEach(item -> {
            var items = new ItemInvoice();
            items.setInvoice(invoice);
            items.setQuantity(item.getQuantity());
            var book = bookRepository.findById(item.getBookId())
                    .orElseThrow();
            items.setBook(book);
            itemInvoiceRepository.save(items);
            // Update book quantity
            book.setQuantity(book.getQuantity() - item.getQuantity());
            bookRepository.save(book);
        });
        removeCart(session);
        return invoice;
    }
}