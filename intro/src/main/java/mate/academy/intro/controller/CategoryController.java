package mate.academy.intro.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import mate.academy.intro.dto.category.CategoryDto;
import mate.academy.intro.service.CategoryService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/categories")
public class CategoryController {
    private final CategoryService categoryService;

    public CategoryDto createCategory(CategoryDto categoryDto);
    public List getAll();
    public CategoryDto getCategoryById(Long id);
    public CategoryDto updateCategory(Long id, CategoryDto categoryDto);
    public void deleteCategory(Long id);
    public List getBooksByCategoryId(Long id) //(endpoint: "/{id}/books");
}
