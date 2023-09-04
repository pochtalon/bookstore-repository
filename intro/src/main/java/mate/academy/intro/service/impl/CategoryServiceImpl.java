package mate.academy.intro.service.impl;

import java.util.List;
import lombok.RequiredArgsConstructor;
import mate.academy.intro.dto.category.CategoryDto;
import mate.academy.intro.service.CategoryService;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class CategoryServiceImpl implements CategoryService {
    @Override
    public List findAll() {
        return null;
    }

    @Override
    public CategoryDto getById(Long id) {
        return null;
    }

    @Override
    public CategoryDto save(CategoryDto categoryDto) {
        return null;
    }

    @Override
    public CategoryDto update(Long id, CategoryDto categoryDto) {
        return null;
    }

    @Override
    public void deleteById(Long id) {

    }
}
