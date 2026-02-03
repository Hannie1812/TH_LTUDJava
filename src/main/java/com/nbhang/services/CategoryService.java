package com.nbhang.services;

import com.nbhang.entities.Category;
import com.nbhang.repositories.ICategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class CategoryService {
    private final ICategoryRepository categoryRepository;

    
    // Xóa danh mục theo ID
    public void findById(Long id) {
        categoryRepository.findById(id);
    }

    // Lấy danh sách tất cả các danh mục
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    // Lấy danh mục theo ID
    public Optional<Category> getCategoryById(Long id) {
        return categoryRepository.findById(id);
    }

    // Thêm danh mục mới
    public Category addCategory(Category category) {
        return categoryRepository.save(category);
    }

    // Cập nhật danh mục
    public Category updateCategory(Category category) {
        return categoryRepository.save(category);
    }

    // Xóa danh mục theo ID
    public void deleteCategoryById(Long id) {
        categoryRepository.deleteById(id);
    }

    // Kiểm tra danh mục có tồn tại không
    public boolean existsById(Long id) {
        return categoryRepository.existsById(id);
    }

}
