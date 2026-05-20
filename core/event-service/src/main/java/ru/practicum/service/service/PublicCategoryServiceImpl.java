package ru.practicum.service.service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.interaction.dto.CategoryDto;
import ru.practicum.service.dal.CategoryRepository;
import ru.practicum.service.error.NotFoundException;
import ru.practicum.service.mapper.CategoryMapper;
import ru.practicum.service.model.Category;

import java.util.List;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
@Slf4j
public class PublicCategoryServiceImpl implements PublicCategoryService {
    final CategoryRepository categoryRepository;

    @Override
    public List<CategoryDto> getCategories(Integer from, Integer size, HttpServletRequest request) {
        log.info("PublicCategoryService: выгрузка категорий по заданным параметрам:");
        Pageable pageable = PageRequest.of(from / size, size);
        List<Category> categoryList = categoryRepository.findAll(pageable).getContent();
        log.info("{}", categoryList);

        return categoryList.stream().map(CategoryMapper::toCategoryDto).toList();
    }

    @Override
    public CategoryDto getById(Long catId, HttpServletRequest request) {
        log.info("PublicCategoryService: поиск категории с переданным id:");
        Category category = categoryRepository.findById(catId)
                .orElseThrow(() -> new NotFoundException(String.format("Категория с id: %d не найдена", catId)));
        log.info("{}", category);

        return CategoryMapper.toCategoryDto(category);
    }
}
