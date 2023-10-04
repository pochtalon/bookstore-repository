package mate.academy.intro.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import lombok.SneakyThrows;
import mate.academy.intro.dto.book.BookDto;
import mate.academy.intro.dto.book.CreateBookRequestDto;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.shaded.org.apache.commons.lang3.builder.EqualsBuilder;
import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class BookControllerTest {
    protected static MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    private static final String TITLE = "Leviathan";
    private static final String AUTHOR = "Thomas Hobbes";
    private static final String ISBN = "978-1439297254";
    private static final BigDecimal PRICE = BigDecimal.valueOf(148.8);
    private static final String DESCRIPTION = "Early modern political philosophy";
    private static final String COVER_IMAGE = "Some printmaking";
    private static final List<BookDto> booksCatalog = new ArrayList<>();

    @BeforeAll
    static void beforeAll(
            @Autowired DataSource dataSource,
            @Autowired WebApplicationContext applicationContext
    ) throws SQLException {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(applicationContext)
                .apply(springSecurity())
                .build();
        teardown(dataSource);
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(true);
            ScriptUtils.executeSqlScript(
                    connection,
                    new ClassPathResource("database/books/add-three-books-to-db.sql")
            );
        }
        booksCatalogInit();
    }

    private static void booksCatalogInit() {
        booksCatalog.add(new BookDto()
                .setId(1L)
                .setTitle("Call of Cthulhu")
                .setAuthor("Howard Lovecraft")
                .setIsbn("978-966-2355-82-6")
                .setPrice(BigDecimal.valueOf(192.8))
                .setDescription("Book about Cthulhu")
                .setCoverImage("Cthulhu_cover")
                .setCategoriesId(new HashSet<>()));
        booksCatalog.add(new BookDto()
                .setId(2L)
                .setTitle("The Raven")
                .setAuthor("Edgar Poe")
                .setIsbn("0-7858-1453-1")
                .setPrice(BigDecimal.valueOf(184.5))
                .setDescription("Feel the NeverMore")
                .setCoverImage("Raven_cover")
                .setCategoriesId(new HashSet<>()));
        booksCatalog.add(new BookDto()
                .setId(3L)
                .setTitle("The Name of the Rose")
                .setAuthor("Umberto Eco")
                .setIsbn("978-0-15-144647-6")
                .setPrice(BigDecimal.valueOf(198.0))
                .setDescription("Murderer is Jorge")
                .setCoverImage("Monastery_cover")
                .setCategoriesId(new HashSet<>()));
    }

    @AfterAll
    static void afterAll(@Autowired DataSource dataSource) {
        teardown(dataSource);
    }

    @SneakyThrows
    static void teardown(DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(true);
            ScriptUtils.executeSqlScript(
                    connection,
                    new ClassPathResource("database/books/remove-all-books.sql")
            );
        }
    }


    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @Test
    @Sql(
            scripts = "classpath:database/books/delete-leviathan-book.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD
    )
    @DisplayName("Create a new book")
    public void createBook_ValidRequestDto_Success() throws Exception {
        CreateBookRequestDto requestDto = getBookRequestDto();
        BookDto expected = getBookDto();
        String jsonRequest = objectMapper.writeValueAsString(requestDto);

        MvcResult result = mockMvc.perform(post("/books")
                .content(jsonRequest)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        BookDto actual = objectMapper.readValue(result.getResponse().getContentAsString(), BookDto.class);
        Assertions.assertNotNull(actual);
        Assertions.assertNotNull(actual.getId());
        EqualsBuilder.reflectionEquals(expected, actual, "id");
    }

    @WithMockUser
    @Test
    @DisplayName("Get all books from db")
    public void getAll_GivenBooksInCatalog_ReturnAllBooks() throws Exception {
        MvcResult result = mockMvc.perform(get("/books")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        BookDto[] actual = objectMapper.readValue(result.getResponse().getContentAsByteArray(), BookDto[].class);
        Assertions.assertEquals(3, actual.length);
        Assertions.assertEquals(booksCatalog, Arrays.stream(actual).toList());
    }

    @WithMockUser
    @Test
    @DisplayName("Get book from db by id")
    public void getAll_GivenBooksInCatalog_ReturnBookById() throws Exception {
        MvcResult resultFirstId = mockMvc.perform(get("/books/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        BookDto actual1 = objectMapper.readValue(resultFirstId.getResponse().getContentAsByteArray(), BookDto.class);
        Assertions.assertEquals(booksCatalog.get(0), actual1);

        MvcResult resultSecondId = mockMvc.perform(get("/books/2")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        BookDto actual2 = objectMapper.readValue(resultSecondId.getResponse().getContentAsByteArray(), BookDto.class);
        Assertions.assertEquals(booksCatalog.get(1), actual2);

        MvcResult resultThirdId = mockMvc.perform(get("/books/3")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        BookDto actual3 = objectMapper.readValue(resultThirdId.getResponse().getContentAsByteArray(), BookDto.class);
        Assertions.assertEquals(booksCatalog.get(2), actual3);
    }

    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @Test
    @Sql(
            scripts = "classpath:database/books/add-leviathan-book.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
    )
    @Sql(
            scripts = "classpath:database/books/delete-leviathan-book.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD
    )
    @DisplayName("Delete book by valid id")
    public void delete_ValidBookId_ChangedBookCount() throws Exception {
        MvcResult beforeDeleting = mockMvc.perform(get("/books")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        BookDto[] booksBefore = objectMapper
                .readValue(beforeDeleting.getResponse().getContentAsByteArray(), BookDto[].class);

        MvcResult resultFirstId = mockMvc.perform(delete("/books/4")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent())
                .andReturn();

        MvcResult afterDeleting = mockMvc.perform(get("/books")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        BookDto[] booksAfter = objectMapper
                .readValue(afterDeleting.getResponse().getContentAsByteArray(), BookDto[].class);

        Assertions.assertEquals(booksBefore.length - 1, booksAfter.length);
    }

    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @Test
    @DisplayName("Delete book by invalid id")
    public void delete_InvalidBookId_NonChangedBookCount() throws Exception {
        MvcResult beforeDeleting = mockMvc.perform(get("/books")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        BookDto[] booksBefore = objectMapper
                .readValue(beforeDeleting.getResponse().getContentAsByteArray(), BookDto[].class);

        MvcResult result = mockMvc.perform(delete("/books/100")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent())
                .andReturn();

        MvcResult afterDeleting = mockMvc.perform(get("/books")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        BookDto[] booksAfter = objectMapper
                .readValue(afterDeleting.getResponse().getContentAsByteArray(), BookDto[].class);

        Assertions.assertEquals(booksBefore.length, booksAfter.length);
    }

    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @Test
    @Sql(
            scripts = "classpath:database/books/add-book-for-update-test.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
    )
    @Sql(
            scripts = "classpath:database/books/delete-book-after-updating.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD
    )
    @DisplayName("Update book by valid id")
    public void update_IdAndRequestDto_UpdatedBook() throws Exception {
        Long id = 4L;
        CreateBookRequestDto requestDto = getBookRequestDto();
        String jsonRequest = objectMapper.writeValueAsString(requestDto);

        MvcResult result = mockMvc.perform(put("/books/" + id)
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        BookDto expected = getBookDto();
        expected.setId(id);
        BookDto actual = objectMapper.readValue(result.getResponse().getContentAsByteArray(), BookDto.class);

        Assertions.assertEquals(expected, actual);
    }

    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @Test
    @DisplayName("Update book by invalid id")
    public void update_InvalidIdAndRequestDto_ThrowException() throws Exception {
        Long id = 100L;
        CreateBookRequestDto requestDto = getBookRequestDto();
        String jsonRequest = objectMapper.writeValueAsString(requestDto);

        Exception exception = assertThrows(ServletException.class,
                () -> mockMvc.perform(put("/books/" + id)
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andReturn());

        String expected = "Request processing failed: mate.academy.intro.exception."
            + "EntityNotFoundException: The book with id " + id + " was not found";
        String actual = exception.getMessage();
        assertEquals(expected, actual);
    }

    @WithMockUser
    @Test
    @DisplayName("Search book by valid parameters")
    public void search_ValidParameters_ReturnDto() throws Exception {
        MvcResult resultTitles = mockMvc.perform(get("/books/search")
                        .param("title", "The Raven")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        BookDto[] booksTitle = objectMapper
                .readValue(resultTitles.getResponse().getContentAsByteArray(), BookDto[].class);
        Assertions.assertEquals(1, booksTitle.length);
        Assertions.assertEquals(booksCatalog.get(1), booksTitle[0]);

        MvcResult resultAuthors = mockMvc.perform(get("/books/search")
                        .param("author", "Umberto Eco", "Howard Lovecraft")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        BookDto[] booksAuthor = objectMapper
                .readValue(resultAuthors.getResponse().getContentAsByteArray(), BookDto[].class);
        Assertions.assertEquals(2, booksAuthor.length);
        Assertions.assertEquals(booksCatalog.get(0), booksAuthor[0]);
        Assertions.assertEquals(booksCatalog.get(2), booksAuthor[1]);

        MvcResult resultAuthorsAndTitle = mockMvc.perform(get("/books/search")
                        .param("author", "Umberto Eco", "Howard Lovecraft")
                        .param("title", "Call of Cthulhu")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        BookDto[] booksAuthorAndTitle = objectMapper
                .readValue(resultAuthorsAndTitle.getResponse().getContentAsByteArray(), BookDto[].class);
        Assertions.assertEquals(1, booksAuthorAndTitle.length);
        Assertions.assertEquals(booksCatalog.get(0), booksAuthorAndTitle[0]);
    }

    @WithMockUser
    @Test
    @DisplayName("Search book by invalid parameters")
    public void search_InvalidParameters_ReturnEmptyList() throws Exception {
        MvcResult resultTitle = mockMvc.perform(get("/books/search")
                        .param("title", "invalid")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        BookDto[] booksTitle = objectMapper
                .readValue(resultTitle.getResponse().getContentAsByteArray(), BookDto[].class);
        Assertions.assertEquals(0, booksTitle.length);

        MvcResult resultAuthor = mockMvc.perform(get("/books/search")
                        .param("title", "invalid")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        BookDto[] booksAuthor = objectMapper
                .readValue(resultAuthor.getResponse().getContentAsByteArray(), BookDto[].class);
        Assertions.assertEquals(0, booksAuthor.length);
    }

    private BookDto getBookDto() {
        return new BookDto()
                .setTitle(TITLE)
                .setAuthor(AUTHOR)
                .setIsbn(ISBN)
                .setPrice(PRICE)
                .setDescription(DESCRIPTION)
                .setCoverImage(COVER_IMAGE)
                .setCategoriesId(new HashSet<>());
    }

    private CreateBookRequestDto getBookRequestDto() {
        return new CreateBookRequestDto()
                .setTitle(TITLE)
                .setAuthor(AUTHOR)
                .setIsbn(ISBN)
                .setPrice(PRICE)
                .setDescription(DESCRIPTION)
                .setCoverImage(COVER_IMAGE)
                .setCategories(new HashSet<>());
    }
}