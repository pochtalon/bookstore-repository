package mate.academy.intro.service;

import java.util.List;
import mate.academy.intro.dto.category.CategoryDto;

public interface CategoryService {
    List findAll();
    CategoryDto getById(Long id);
    CategoryDto save(CategoryDto categoryDto);
    CategoryDto update(Long id, CategoryDto categoryDto);
    void deleteById(Long id);
}
