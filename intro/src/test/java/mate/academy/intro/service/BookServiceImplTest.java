package mate.academy.intro.service;

import mate.academy.intro.dto.book.BookDto;
import mate.academy.intro.dto.book.BookDtoWithoutCategoriesIds;
import mate.academy.intro.dto.book.BookSearchParameters;
import mate.academy.intro.dto.book.CreateBookRequestDto;
import mate.academy.intro.exception.EntityNotFoundException;
import mate.academy.intro.mapper.BookMapper;
import mate.academy.intro.model.Book;
import mate.academy.intro.model.Category;
import mate.academy.intro.repository.book.BookRepository;
import mate.academy.intro.repository.book.BookSpecificationBuilder;
import mate.academy.intro.repository.category.CategoryRepository;
import mate.academy.intro.service.impl.BookServiceImpl;
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
import org.springframework.data.jpa.domain.Specification;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookServiceImplTest {
    @InjectMocks
    BookServiceImpl bookService;
    @Mock
    private BookRepository bookRepository;
    @Mock
    private BookMapper bookMapper;
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private BookSpecificationBuilder builder;
    private static final String TITLE = "Leviathan";
    private static final String AUTHOR = "Thomas Hobbes";
    private static final String ISBN = "978-1439297254";
    private static final BigDecimal PRICE = BigDecimal.valueOf(148.8);
    private static final String DESCRIPTION = "Early modern political philosophy";
    private static final String COVER_IMAGE = "Some printmaking";

    @Test
    @DisplayName("Save book to db without categories")
    public void save_WithoutCategories_ReturnValidDto() {
        CreateBookRequestDto requestDto = new CreateBookRequestDto()
                .setTitle(TITLE)
                .setAuthor(AUTHOR)
                .setIsbn(ISBN)
                .setPrice(PRICE)
                .setDescription(DESCRIPTION)
                .setCoverImage(COVER_IMAGE);
        Book book = new Book()
                .setTitle(TITLE)
                .setAuthor(AUTHOR)
                .setIsbn(ISBN)
                .setPrice(PRICE)
                .setDescription(DESCRIPTION)
                .setCoverImage(COVER_IMAGE);
        BookDto expected = new BookDto()
                .setTitle(TITLE)
                .setAuthor(AUTHOR)
                .setIsbn(ISBN)
                .setPrice(PRICE)
                .setDescription(DESCRIPTION)
                .setCoverImage(COVER_IMAGE);

        when(bookMapper.toModel(requestDto)).thenReturn(book);
        when(bookRepository.save(book)).thenReturn(book);
        when(bookMapper.toDto(book)).thenReturn(expected);

        BookDto actual = bookService.save(requestDto);

        assertEquals(expected, actual);
        verify(bookRepository, times(1)).save(book);
        verifyNoMoreInteractions(bookRepository, bookMapper);
    }

    @Test
    @DisplayName("Save book to db with categories")
    public void save_WithCategories_ReturnValidDto() {
        CreateBookRequestDto requestDto = new CreateBookRequestDto()
                .setTitle(TITLE)
                .setAuthor(AUTHOR)
                .setIsbn(ISBN)
                .setPrice(PRICE)
                .setDescription(DESCRIPTION)
                .setCoverImage(COVER_IMAGE)
                .setCategories(Set.of(1L));
        Category category = new Category()
                .setId(1L)
                .setName("Philosophy")
                .setDescription("Systematic study of general and fundamental questions");
        Book book = new Book()
                .setTitle(TITLE)
                .setAuthor(AUTHOR)
                .setIsbn(ISBN)
                .setPrice(PRICE)
                .setDescription(DESCRIPTION)
                .setCoverImage(COVER_IMAGE)
                .setCategories(new HashSet<>());
        BookDto expected = new BookDto()
                .setTitle(TITLE)
                .setAuthor(AUTHOR)
                .setIsbn(ISBN)
                .setPrice(PRICE)
                .setDescription(DESCRIPTION)
                .setCoverImage(COVER_IMAGE)
                .setCategoriesId(Set.of(1L));

        when(bookMapper.toModel(requestDto)).thenReturn(book);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(bookRepository.save(book)).thenReturn(book);
        when(bookMapper.toDto(book)).thenReturn(expected);

        BookDto actual = bookService.save(requestDto);
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Save book to db with wrong categories, expect exception")
    public void save_WithWrongCategories_ThrowException() {
        CreateBookRequestDto requestDto = new CreateBookRequestDto()
                .setTitle(TITLE)
                .setAuthor(AUTHOR)
                .setIsbn(ISBN)
                .setPrice(PRICE)
                .setDescription(DESCRIPTION)
                .setCoverImage(COVER_IMAGE)
                .setCategories(Set.of(1L));
        Book book = new Book()
                .setTitle(TITLE)
                .setAuthor(AUTHOR)
                .setIsbn(ISBN)
                .setPrice(PRICE)
                .setDescription(DESCRIPTION)
                .setCoverImage(COVER_IMAGE)
                .setCategories(new HashSet<>());

        when(bookMapper.toModel(requestDto)).thenReturn(book);
        when(categoryRepository.findById(1L)).thenReturn(Optional.empty());

        Exception exception = assertThrows(EntityNotFoundException.class,
                () -> bookService.save(requestDto));
        String expected = "Can't find category with id 1";
        String actual = exception.getMessage();
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Find book by valid id")
    public void findById_ValidBookId_ReturnValidDto() {
        Long id = 1L;
        Book book = new Book()
                .setId(id)
                .setTitle(TITLE)
                .setAuthor(AUTHOR)
                .setIsbn(ISBN)
                .setPrice(PRICE)
                .setDescription(DESCRIPTION)
                .setCoverImage(COVER_IMAGE);
        BookDto expected = new BookDto()
                .setId(id)
                .setTitle(TITLE)
                .setAuthor(AUTHOR)
                .setIsbn(ISBN)
                .setPrice(PRICE)
                .setDescription(DESCRIPTION)
                .setCoverImage(COVER_IMAGE);

        when(bookRepository.findById(id)).thenReturn(Optional.of(book));
        when(bookMapper.toDto(book)).thenReturn(expected);

        BookDto actual = bookService.findById(id);
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Find book by invalid id")
    public void findById_InvalidBookId_ThrowException() {
        Long id = 1L;
        when(bookRepository.findById(id)).thenReturn(Optional.empty());
        Exception exception = assertThrows(EntityNotFoundException.class,
                () -> bookService.findById(id));
        String expected = "Can't get book with id: " + id;
        String actual = exception.getMessage();
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Find all book, db is not empty")
    public void findAll_ValidPageable_ReturnAllBooks() {
        Long id = 1L;
        Book book = new Book()
                .setId(id)
                .setTitle(TITLE)
                .setAuthor(AUTHOR)
                .setIsbn(ISBN)
                .setPrice(PRICE)
                .setDescription(DESCRIPTION)
                .setCoverImage(COVER_IMAGE);
        BookDto bookDto = new BookDto()
                .setId(id)
                .setTitle(TITLE)
                .setAuthor(AUTHOR)
                .setIsbn(ISBN)
                .setPrice(PRICE)
                .setDescription(DESCRIPTION)
                .setCoverImage(COVER_IMAGE);
        Pageable pageable = PageRequest.of(0, 10);
        List<Book> books = List.of(book);
        Page<Book> bookPage = new PageImpl<>(books, pageable, books.size());

        when(bookRepository.findAll(pageable)).thenReturn(bookPage);
        when(bookMapper.toDto(book)).thenReturn(bookDto);

        List<BookDto> dtoList = bookService.findAll(pageable);
        assertThat(dtoList).hasSize(1);
        assertEquals(dtoList.get(0), bookDto);
    }

    @Test
    @DisplayName("Find all book, db is empty")
    public void findAll_ValidPageable_ReturnEmptyList() {
        Pageable pageable = PageRequest.of(0, 10);
        List<Book> books = Collections.emptyList();
        Page<Book> bookPage = new PageImpl<>(books, pageable, books.size());

        when(bookRepository.findAll(pageable)).thenReturn(bookPage);

        List<BookDto> dtoList = bookService.findAll(pageable);
        assertThat(dtoList).hasSize(0);
    }

    @Test
    @DisplayName("Search books with valid parameters")
    public void search_ValidParams_ReturnListDto() {
        Long id = 1L;
        Book book = new Book()
                .setId(id)
                .setTitle(TITLE)
                .setAuthor(AUTHOR)
                .setIsbn(ISBN)
                .setPrice(PRICE)
                .setDescription(DESCRIPTION)
                .setCoverImage(COVER_IMAGE);
        BookDto bookDto = new BookDto()
                .setId(id)
                .setTitle(TITLE)
                .setAuthor(AUTHOR)
                .setIsbn(ISBN)
                .setPrice(PRICE)
                .setDescription(DESCRIPTION)
                .setCoverImage(COVER_IMAGE);
        BookSearchParameters searchParameters = new BookSearchParameters()
                .setAuthors(new String[]{AUTHOR})
                .setTitles(new String[]{TITLE});
        Specification<Book> specification = (root, query, criteriaBuilder) -> null;

        when(builder.build(searchParameters)).thenReturn(specification);
        when(bookRepository.findAll(specification)).thenReturn(List.of(book));
        when(bookMapper.toDto(book)).thenReturn(bookDto);

        List<BookDto> bookDtos = bookService.search(searchParameters);
        assertThat(bookDtos).hasSize(1);
        assertEquals(bookDto, bookDtos.get(0));
    }

    @Test
    @DisplayName("Search books with invalid parameters")
    public void search_InvalidParams_ReturnEmptyListDto() {
        BookSearchParameters searchParameters = new BookSearchParameters()
                .setAuthors(new String[]{"invalid"})
                .setTitles(new String[]{"invalid"});
        Specification<Book> specification = (root, query, criteriaBuilder) -> null;

        when(builder.build(searchParameters)).thenReturn(specification);
        when(bookRepository.findAll(specification)).thenReturn(Collections.emptyList());

        List<BookDto> bookDtos = bookService.search(searchParameters);
        assertThat(bookDtos).hasSize(0);
    }

    @Test
    @DisplayName("Update book with valid id")
    public void update_ValidId_ReturnValidDto() {
        Long id = 1L;
        CreateBookRequestDto requestDto = new CreateBookRequestDto()
                .setTitle(TITLE)
                .setAuthor(AUTHOR)
                .setIsbn(ISBN)
                .setPrice(PRICE)
                .setDescription(DESCRIPTION)
                .setCoverImage(COVER_IMAGE);
        Book book = new Book()
                .setId(id)
                .setTitle(TITLE)
                .setAuthor(AUTHOR)
                .setIsbn(ISBN)
                .setPrice(PRICE)
                .setDescription(DESCRIPTION)
                .setCoverImage(COVER_IMAGE);
        BookDto bookDto = new BookDto()
                .setId(id)
                .setTitle(TITLE)
                .setAuthor(AUTHOR)
                .setIsbn(ISBN)
                .setPrice(PRICE)
                .setDescription(DESCRIPTION)
                .setCoverImage(COVER_IMAGE);

        when(bookRepository.findById(id)).thenReturn(Optional.of(new Book()));
        when(bookMapper.toModel(requestDto)).thenReturn(book);
        when(bookRepository.save(book)).thenReturn(book);
        when(bookMapper.toDto(book)).thenReturn(bookDto);

        BookDto actual = bookService.update(id, requestDto);
        assertEquals(bookDto, actual);
    }

    @Test
    @DisplayName("Update book with invalid id")
    public void update_InvalidId_ThrowException() {
        Long id = 1L;
        CreateBookRequestDto requestDto = new CreateBookRequestDto();

        when(bookRepository.findById(id)).thenReturn(Optional.empty());

        Exception exception = assertThrows(EntityNotFoundException.class,
                () -> bookService.update(id, requestDto));
        String expected = "The book with id " + id + " was not found";
        String actual = exception.getMessage();
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Find all books by valid category id")
    public void findAllByCategoryId_ValidId_ReturnListDto() {
        Long firstId = 1L;
        Long secondId = 2L;
        Book firstBook = new Book()
                .setId(firstId)
                .setTitle(TITLE)
                .setAuthor(AUTHOR)
                .setIsbn(ISBN)
                .setPrice(PRICE)
                .setDescription(DESCRIPTION)
                .setCoverImage(COVER_IMAGE);
        Book secondBook = new Book()
                .setId(secondId)
                .setTitle("Beyond Good and Evil")
                .setAuthor("Friedrich Nietzsche")
                .setIsbn("0-486-29868-X")
                .setPrice(PRICE)
                .setDescription(DESCRIPTION)
                .setCoverImage(COVER_IMAGE);
        BookDtoWithoutCategoriesIds firstDto = new BookDtoWithoutCategoriesIds()
                .setId(firstId)
                .setTitle(TITLE)
                .setAuthor(AUTHOR)
                .setIsbn(ISBN)
                .setPrice(PRICE)
                .setDescription(DESCRIPTION)
                .setCoverImage(COVER_IMAGE);
        BookDtoWithoutCategoriesIds secondDto = new BookDtoWithoutCategoriesIds()
                .setId(firstId)
                .setTitle(TITLE)
                .setAuthor(AUTHOR)
                .setIsbn(ISBN)
                .setPrice(PRICE)
                .setDescription(DESCRIPTION)
                .setCoverImage(COVER_IMAGE);

        when(bookRepository.getAllByCategoriesId(1L)).thenReturn(List.of(firstBook, secondBook));
        when(bookMapper.toDtoWithoutCategories(firstBook)).thenReturn(firstDto);
        when(bookMapper.toDtoWithoutCategories(secondBook)).thenReturn(secondDto);

        List<BookDtoWithoutCategoriesIds> byCategoryId = bookService.findAllByCategoryId(1L);
        assertThat(byCategoryId).hasSize(2);
        assertEquals(firstDto, byCategoryId.get(0));
        assertEquals(secondDto, byCategoryId.get(1));
    }

    @Test
    @DisplayName("Find all books by invalid category id")
    public void findAllByCategoryId_InvalidId_ReturnEmptyList() {
        when(bookRepository.getAllByCategoriesId(1L)).thenReturn(Collections.emptyList());

        List<BookDtoWithoutCategoriesIds> byCategoryId = bookService.findAllByCategoryId(1L);
        assertThat(byCategoryId).hasSize(0);
    }
}