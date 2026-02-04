package com.nbhang.services;

import com.nbhang.entities.Invoice;
import com.nbhang.entities.User;
import com.nbhang.repositories.IInvoiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class InvoiceService {
    private final IInvoiceRepository invoiceRepository;

    public List<Invoice> getAllInvoices() {
        return invoiceRepository.findAll();
    }

    public List<Invoice> getInvoicesByUser(User user) {
        return invoiceRepository.findByUser(user);
    }

    public Optional<Invoice> getInvoiceById(Long id) {
        return invoiceRepository.findById(id);
    }

    @Transactional
    public void updateStatus(Long id, String status) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));
        invoice.setStatus(status);
        invoiceRepository.save(invoice);
    }

    @Transactional
    public void updatePaymentProof(Long id, String proof) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));
        invoice.setPaymentProof(proof);
        invoiceRepository.save(invoice);
    }
}
