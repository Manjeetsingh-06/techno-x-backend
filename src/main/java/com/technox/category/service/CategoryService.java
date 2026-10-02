package com.technox.category.service;

import com.technox.category.dto.CategoryDto;
import com.technox.category.entity.Category;
import com.technox.category.repository.CategoryRepository;
import com.technox.common.exception.BusinessException;
import com.technox.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    @Transactional(readOnly = true)
    public List<CategoryDto> getAllCategories() {
        return categoryRepository.findAll().stream().map(this::mapToDto).toList();
    }

    @Transactional(readOnly = true)
    public CategoryDto getCategoryById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Category not found with ID: " + id));
        return mapToDto(category);
    }

    @Transactional
    public CategoryDto createCategory(CategoryDto dto) {
        if (categoryRepository.existsByName(dto.getName().trim())) {
            throw new BusinessException(ErrorCode.CONFLICT, "Category already exists: " + dto.getName());
        }

        Category category = Category.builder()
                .name(dto.getName().trim())
                .description(dto.getDescription())
                .colorCode(dto.getColorCode())
                .build();

        category = categoryRepository.save(category);
        return mapToDto(category);
    }

    public CategoryDto mapToDto(Category category) {
        return CategoryDto.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .colorCode(category.getColorCode())
                .build();
    }
}
