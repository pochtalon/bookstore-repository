package mate.academy.intro.service;

import mate.academy.intro.dto.category.CategoryDto;
import mate.academy.intro.exception.EntityNotFoundException;
import mate.academy.intro.mapper.CategoryMapper;
import mate.academy.intro.model.Category;
import mate.academy.intro.repository.category.CategoryRepository;
import mate.academy.intro.service.impl.CategoryServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoryServiceImplTest {
    @InjectMocks
    private CategoryServiceImpl categoryService;
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private CategoryMapper categoryMapper;
    private static final Long ID = 1L;
    private static final String NAME = "Philosophy";
    private static final String DESCRIPTION = "Systematic study of fundamental questions";

    @Test
    @DisplayName("Find all categories, db is not empty")
    public void findAll_ValidPageable_ReturnListCategories() {
        Category category = new Category()
                .setId(ID)
                .setName(NAME)
                .setDescription(DESCRIPTION);
        CategoryDto categoryDto = new CategoryDto()
                .setId(ID)
                .setName(NAME)
                .setDescription(DESCRIPTION);
        Pageable pageable = PageRequest.of(0, 10);
        List<Category> categories = List.of(category);
        Page<Category> categoryPage = new PageImpl<>(categories, pageable, categories.size());

        when(categoryRepository.findAll(pageable)).thenReturn(categoryPage);
        when(categoryMapper.toDto(category)).thenReturn(categoryDto);

        List<CategoryDto> dtoList = categoryService.findAll(pageable);
        assertThat(dtoList).hasSize(1);
        assertEquals(dtoList.get(0), categoryDto);
    }

    @Test
    @DisplayName("Find all categories, db is empty")
    public void findAll_ValidPageable_ReturnEmptyList() {
        Pageable pageable = PageRequest.of(0, 10);
        List<Category> categories = Collections.emptyList();
        Page<Category> categoryPage = new PageImpl<>(categories, pageable, categories.size());

        when(categoryRepository.findAll(pageable)).thenReturn(categoryPage);

        List<CategoryDto> dtoList = categoryService.findAll(pageable);
        assertThat(dtoList).hasSize(0);
    }

    @Test
    @DisplayName("Get category by valid id")
    public void getById_ValidId_ReturnDto() {
        Category category = new Category()
                .setId(ID)
                .setName(NAME)
                .setDescription(DESCRIPTION);
        CategoryDto expected = new CategoryDto()
                .setId(ID)
                .setName(NAME)
                .setDescription(DESCRIPTION);

        when(categoryRepository.findById(ID)).thenReturn(Optional.of(category));
        when(categoryMapper.toDto(category)).thenReturn(expected);

        CategoryDto actual = categoryService.getById(ID);
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Get category by invalid id")
    public void getById_InvalidId_ThrowException() {
        when(categoryRepository.findById(ID)).thenReturn(Optional.empty());
        Exception exception = assertThrows(EntityNotFoundException.class,
                () -> categoryService.getById(ID));
        String expected = "Can't find category with id " + ID;
        String actual = exception.getMessage();
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Save category to db")
    public void save_ValidDto_ReturnDtoWithId() {
        CategoryDto request = new CategoryDto()
                .setName(NAME)
                .setDescription(DESCRIPTION);
        Category category = new Category()
                .setName(NAME)
                .setDescription(DESCRIPTION);
        Category savedCategory = new Category()
                .setId(ID)
                .setName(NAME)
                .setDescription(DESCRIPTION);
        CategoryDto expected = new CategoryDto()
                .setId(ID)
                .setName(NAME)
                .setDescription(DESCRIPTION);

        when(categoryMapper.toEntity(request)).thenReturn(category);
        when(categoryRepository.save(category)).thenReturn(savedCategory);
        when(categoryMapper.toDto(savedCategory)).thenReturn(expected);

        CategoryDto actual = categoryService.save(request);
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Update category with valid id")
    public void update_ValidId_ReturnDto(){
        CategoryDto request = new CategoryDto()
                .setName(NAME)
                .setDescription(DESCRIPTION);
        Category category = new Category()
                .setName(NAME)
                .setDescription(DESCRIPTION);
        CategoryDto expected = new CategoryDto()
                .setId(ID)
                .setName(NAME)
                .setDescription(DESCRIPTION);

        when(categoryRepository.findById(ID)).thenReturn(Optional.of(category));
        when(categoryMapper.toEntity(request)).thenReturn(category);
        when(categoryRepository.save(category)).thenReturn(category.setId(ID));
        when(categoryMapper.toDto(category)).thenReturn(request.setId(ID));

        CategoryDto actual = categoryService.update(ID, request);
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Update category with invalid id")
    public void update_InvalidId_ThrowException(){
        when(categoryRepository.findById(ID)).thenReturn(Optional.empty());

        Exception exception = assertThrows(EntityNotFoundException.class,
                () -> categoryService.update(ID, new CategoryDto()));
        String expected = "Category with id " + ID + " wasn't found";
        String actual = exception.getMessage();
        assertEquals(expected, actual);
    }
}