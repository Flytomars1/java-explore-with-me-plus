package ru.practicum.main.service.category;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.main.dto.category.CategoryDto;
import ru.practicum.main.dto.category.NewCategoryDto;
import ru.practicum.main.exception.AlreadyExistsException;
import ru.practicum.main.exception.NotFoundException;
import ru.practicum.main.mapper.CategoryMapper;
import ru.practicum.main.model.Category;
import ru.practicum.main.repository.CategoryRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    @Override
    @Transactional
    public CategoryDto createCategory(NewCategoryDto newCategoryDto) {
        log.info("Creating category with name: {}", newCategoryDto.getName());

        if (categoryRepository.existsByName(newCategoryDto.getName())) {
            throw new AlreadyExistsException("Category name must be unique");
        }

        Category category = CategoryMapper.toEntity(newCategoryDto);
        Category savedCategory = categoryRepository.save(category);

        log.info("Category created with id: {}", savedCategory.getId());
        return CategoryMapper.toDto(savedCategory);
    }

    @Override
    @Transactional
    public CategoryDto updateCategory(Long categoryId, CategoryDto categoryDto) {
        log.info("Updating category with id: {}", categoryId);

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException("Category with id=" + categoryId + " was not found"));

        if (!category.getName().equals(categoryDto.getName()) &&
                categoryRepository.existsByName(categoryDto.getName())) {
            throw new AlreadyExistsException("Category name must be unique");
        }

        category.setName(categoryDto.getName());
        Category updatedCategory = categoryRepository.save(category);

        log.info("Category with id: {} updated", categoryId);
        return CategoryMapper.toDto(updatedCategory);
    }

    @Override
    @Transactional
    public void deleteCategory(Long categoryId) {
        log.info("Deleting category with id: {}", categoryId);

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException("Category with id=" + categoryId + " was not found"));

        categoryRepository.delete(category);
        log.info("Category with id: {} deleted", categoryId);
    }

    @Override
    public List<CategoryDto> getAllCategories(int from, int size) {
        log.info("Getting categories from: {}, size: {}", from, size);

        validatePaginationParams(from, size);

        Pageable pageable = PageRequest.of(from / size, size);
        return categoryRepository.findAll(pageable).stream()
                .map(CategoryMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public CategoryDto getCategoryById(Long categoryId) {
        log.info("Getting category by id: {}", categoryId);

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException("Category with id=" + categoryId + " was not found"));

        return CategoryMapper.toDto(category);
    }

    private void validatePaginationParams(int from, int size) {
        if (from < 0) {
            throw new IllegalArgumentException("Parameter 'from' must be >= 0");
        }
        if (size <= 0) {
            throw new IllegalArgumentException("Parameter 'size' must be > 0");
        }
    }
}