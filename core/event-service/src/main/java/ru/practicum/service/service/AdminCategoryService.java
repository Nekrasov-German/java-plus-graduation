package ru.practicum.service.service;

import ru.practicum.interaction.dto.CategoryDto;
import ru.practicum.interaction.dto.NewCategoryDto;

public interface AdminCategoryService {
    CategoryDto createCategory(NewCategoryDto dto);

    void deleteCategory(Long id);

    CategoryDto updateCategory(Long id, CategoryDto dto);
}
