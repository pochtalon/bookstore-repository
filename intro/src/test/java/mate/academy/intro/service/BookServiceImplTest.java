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

        List<BookDto> bookDtos = bookService.search(List.of(TITLE), List.of(AUTHOR));
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

        List<BookDto> bookDtos = bookService.search(List.of("invalid"), List.of("invalid"));
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
        Long horrorCategoryId = 1L;
        Long detectiveCategoryId = 2L;
        Category horror = new Category()
                .setId(horrorCategoryId)
                .setName("Horror")
                .setDescription("Something scary");
        Category detective = new Category()
                .setId(detectiveCategoryId)
                .setName("Detective")
                .setDescription("Something enigmatic");
        Book cthulhu = new Book()
                .setId(1L)
                .setTitle("Call of Cthulhu")
                .setAuthor("Howard Lovecraft")
                .setIsbn("978-966-2355-82-6")
                .setPrice(BigDecimal.valueOf(192.8))
                .setDescription("Book about Cthulhu")
                .setCoverImage("Cthulhu_cover")
                .setCategories(Set.of(horror));
        Book blackCat = new Book()
                .setId(2L)
                .setTitle("The Black Cat")
                .setAuthor("Edgar Poe")
                .setIsbn("978-0-8154-1038-6")
                .setPrice(BigDecimal.valueOf(184.3))
                .setDescription("Scary black cat")
                .setCoverImage("Cat_cover")
                .setCategories(Set.of(horror, detective));
        BookDtoWithoutCategoriesIds cthulhuDto = new BookDtoWithoutCategoriesIds()
                .setId(1L)
                .setTitle("Call of Cthulhu")
                .setAuthor("Howard Lovecraft")
                .setIsbn("978-966-2355-82-6")
                .setPrice(BigDecimal.valueOf(192.8))
                .setDescription("Book about Cthulhu")
                .setCoverImage("Cthulhu_cover");
        BookDtoWithoutCategoriesIds blackCatDto = new BookDtoWithoutCategoriesIds()
                .setId(2L)
                .setTitle("The Black Cat")
                .setAuthor("Edgar Poe")
                .setIsbn("978-0-8154-1038-6")
                .setPrice(BigDecimal.valueOf(184.3))
                .setDescription("Scary black cat")
                .setCoverImage("Cat_cover");

        when(bookRepository.getAllByCategoriesId(horrorCategoryId)).thenReturn(List.of(cthulhu, blackCat));
        when(bookMapper.toDtoWithoutCategories(cthulhu)).thenReturn(cthulhuDto);
        when(bookMapper.toDtoWithoutCategories(blackCat)).thenReturn(blackCatDto);

        List<BookDtoWithoutCategoriesIds> byHorrorId = bookService.findAllByCategoryId(horrorCategoryId);
        assertThat(byHorrorId).hasSize(2);
        assertEquals(cthulhuDto, byHorrorId.get(0));
        assertEquals(blackCatDto, byHorrorId.get(1));

        when(bookRepository.getAllByCategoriesId(detectiveCategoryId)).thenReturn(List.of(blackCat));
        when(bookMapper.toDtoWithoutCategories(blackCat)).thenReturn(blackCatDto);

        List<BookDtoWithoutCategoriesIds> byDetectiveId = bookService.findAllByCategoryId(detectiveCategoryId);
        assertThat(byDetectiveId).hasSize(1);
        assertEquals(blackCatDto, byDetectiveId.get(0));
    }

    @Test
    @DisplayName("Find all books by invalid category id")
    public void findAllByCategoryId_InvalidId_ReturnEmptyList() {
        when(bookRepository.getAllByCategoriesId(100L)).thenReturn(Collections.emptyList());

        List<BookDtoWithoutCategoriesIds> byCategoryId = bookService.findAllByCategoryId(100L);
        assertThat(byCategoryId).hasSize(0);
    }
}