package ru.practicum.service.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.interaction.dto.CategoryDto;
import ru.practicum.interaction.dto.NewCategoryDto;
import ru.practicum.service.model.Category;

@UtilityClass
public class CategoryMapper {
    public CategoryDto toCategoryDto(Category category) {
        return CategoryDto.builder()
                .id(category.getId())
                .name(category.getName())
                .build();
    }

    public Category toCategoryEntity(NewCategoryDto dto) {
        return Category.builder()
                .name(dto.getName())
                .build();
    }
}
