package mate.academy.intro.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import mate.academy.intro.dto.book.BookDto;
import mate.academy.intro.dto.category.CategoryDto;
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
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CategoryControllerTest {
    protected static MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    private static List<CategoryDto> categoriesCatalog = new ArrayList<>();
    private static List<BookDto> booksCatalog = new ArrayList<>();

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
                    new ClassPathResource("database/categories/add-three-categories-and-four-books-to-db.sql")
            );
        }
        categoriesCatalogInit();
        booksCatalogInit();
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
                    new ClassPathResource("database/categories/clear-books-and-categories-tables.sql")
            );
        }
    }

    private static void categoriesCatalogInit() {
        categoriesCatalog.add(new CategoryDto()
                .setId(1L)
                .setName("Horror")
                .setDescription("Something scary"));
        categoriesCatalog.add(new CategoryDto()
                .setId(2L)
                .setName("Detective")
                .setDescription("Something enigmatic"));
        categoriesCatalog.add(new CategoryDto()
                .setId(3L)
                .setName("Fantasy")
                .setDescription("Something faibled"));
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
                .setCategoriesId(Set.of(1L, 3L)));
        booksCatalog.add(new BookDto()
                .setId(2L)
                .setTitle("The Black Cat")
                .setAuthor("Edgar Poe")
                .setIsbn("978-0-8154-1038-6")
                .setPrice(BigDecimal.valueOf(184.3))
                .setDescription("Scary black cat")
                .setCoverImage("Cat_cover")
                .setCategoriesId(Set.of(1L, 2L)));
        booksCatalog.add(new BookDto()
                .setId(3L)
                .setTitle("The Name of the Rose")
                .setAuthor("Umberto Eco")
                .setIsbn("978-0-15-144647-6")
                .setPrice(BigDecimal.valueOf(198.0))
                .setDescription("Murderer is Jorge")
                .setCoverImage("Monastery_cover")
                .setCategoriesId(Set.of(2L)));
        booksCatalog.add(new BookDto()
                .setId(4L)
                .setTitle("Guards! Guards!")
                .setAuthor("Terry Pratchett")
                .setIsbn("0-575-04606-6")
                .setPrice(BigDecimal.valueOf(198.9))
                .setDescription("Night life in Ankh-Morpork")
                .setCoverImage("Samuel_Vimes_cover")
                .setCategoriesId(Set.of(2L, 3L)));
    }

    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @Test
    @Sql(
            scripts = "classpath:database/categories/delete-pulp-fiction-category.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD
    )
    @DisplayName("Create new category")
    public void create_ValidRequestDto_Success() throws Exception {
        CategoryDto requestDto = new CategoryDto()
                .setName("Pulp Fiction")
                .setDescription("Tarantino approves");
        String jsonRequest = objectMapper.writeValueAsString(requestDto);

        MvcResult result = mockMvc.perform(post("/categories")
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        CategoryDto actual = objectMapper.readValue(result.getResponse().getContentAsString(), CategoryDto.class);
        Assertions.assertNotNull(actual);
        Assertions.assertNotNull(actual.getId());
        EqualsBuilder.reflectionEquals(requestDto, actual, "id");
    }

    @WithMockUser
    @Test
    @DisplayName("Get all categories from db")
    public void getAll_GivenCategoriesInCatalog_ReturnAllCategories() throws Exception {
        MvcResult result = mockMvc.perform(get("/categories")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        CategoryDto[] actual = objectMapper.readValue(result.getResponse().getContentAsByteArray(), CategoryDto[].class);
        Assertions.assertEquals(3, actual.length);
        Assertions.assertEquals(categoriesCatalog, Arrays.stream(actual).toList());
    }

    @WithMockUser
    @Test
    @DisplayName("Get category from db by id")
    public void getAll_GivenCategoriesInCatalog_ReturnCategoryById() throws Exception {
        MvcResult resultFirstId = mockMvc.perform(get("/categories/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        CategoryDto actual1 = objectMapper.readValue(resultFirstId.getResponse().getContentAsByteArray(), CategoryDto.class);
        Assertions.assertEquals(categoriesCatalog.get(0), actual1);

        MvcResult resultSecondId = mockMvc.perform(get("/categories/2")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        CategoryDto actual2 = objectMapper.readValue(resultSecondId.getResponse().getContentAsByteArray(), CategoryDto.class);
        Assertions.assertEquals(categoriesCatalog.get(1), actual2);

        MvcResult resultThirdId = mockMvc.perform(get("/categories/3")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        CategoryDto actual3 = objectMapper.readValue(resultThirdId.getResponse().getContentAsByteArray(), CategoryDto.class);
        Assertions.assertEquals(categoriesCatalog.get(2), actual3);
    }

    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @Test
    @Sql(
            scripts = "classpath:database/categories/add-pulp-fiction-category.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
    )
    @Sql(
            scripts = "classpath:database/categories/delete-pulp-fiction-category.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD
    )
    @DisplayName("Delete category by valid id")
    public void delete_ValidBCategoryId_ChangedCategoryCount() throws Exception {
        MvcResult beforeDeleting = mockMvc.perform(get("/categories")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        CategoryDto[] categoriesBefore = objectMapper
                .readValue(beforeDeleting.getResponse().getContentAsByteArray(), CategoryDto[].class);

        MvcResult resultFirstId = mockMvc.perform(delete("/categories/4")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        MvcResult afterDeleting = mockMvc.perform(get("/categories")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        CategoryDto[] categoriesAfter = objectMapper
                .readValue(afterDeleting.getResponse().getContentAsByteArray(), CategoryDto[].class);

        Assertions.assertEquals(categoriesBefore.length - 1, categoriesAfter.length);
    }

    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @Test
    @DisplayName("Delete category by invalid id")
    public void delete_InvalidCategoryId_NonChangedCategoryCount() throws Exception {
        MvcResult beforeDeleting = mockMvc.perform(get("/categories")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        CategoryDto[] categoriesBefore = objectMapper
                .readValue(beforeDeleting.getResponse().getContentAsByteArray(), CategoryDto[].class);

        MvcResult result= mockMvc.perform(delete("/categories/100")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        MvcResult afterDeleting = mockMvc.perform(get("/categories")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        CategoryDto[] categoriesAfter = objectMapper
                .readValue(afterDeleting.getResponse().getContentAsByteArray(), CategoryDto[].class);

        Assertions.assertEquals(categoriesBefore.length, categoriesAfter.length);
    }

    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @Test
    @Sql(
            scripts = "classpath:database/categories/add-pulp-fiction-category.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
    )
    @Sql(
            scripts = "classpath:database/categories/delete-category-after-updating.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD
    )
    @DisplayName("Update category by valid id")
    public void update_IdAndRequestDto_UpdatedCategory() throws Exception {
        Long id = 4L;
        CategoryDto requestDto = new CategoryDto()
                .setName("Non Fiction")
                .setDescription("Science books");
        String jsonRequest = objectMapper.writeValueAsString(requestDto);

        MvcResult result = mockMvc.perform(put("/categories/" + id)
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        CategoryDto expected = new CategoryDto()
                .setId(id)
                .setName("Non Fiction")
                .setDescription("Science books");
        CategoryDto actual = objectMapper.readValue(result.getResponse().getContentAsByteArray(), CategoryDto.class);

        Assertions.assertEquals(expected, actual);
    }

    @WithMockUser
    @Test
    @DisplayName("Get book by category id")
    public void getBookByCategoryId_ValidId_ListOfBookDto() throws Exception {
        MvcResult horror = mockMvc.perform(get("/categories/1/books")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        BookDto[] horrorActual = objectMapper
                .readValue(horror.getResponse().getContentAsByteArray(), BookDto[].class);
        int expectSizeHorror = 2;
        Assertions.assertNotNull(horrorActual);
        Assertions.assertEquals(expectSizeHorror, horrorActual.length);

        MvcResult detective = mockMvc.perform(get("/categories/2/books")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        BookDto[] detectiveActual = objectMapper
                .readValue(detective.getResponse().getContentAsByteArray(), BookDto[].class);
        int expectSizeDetective = 3;
        Assertions.assertNotNull(detectiveActual);
        Assertions.assertEquals(expectSizeDetective, detectiveActual.length);

        MvcResult fantasy = mockMvc.perform(get("/categories/3/books")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        BookDto[] fantasyActual = objectMapper
                .readValue(fantasy.getResponse().getContentAsByteArray(), BookDto[].class);
        int expectSizeFantasy = 2;
        Assertions.assertNotNull(fantasyActual);
        Assertions.assertEquals(expectSizeFantasy, fantasyActual.length);
    }

    @WithMockUser
    @Test
    @DisplayName("Get book by invalid category id")
    public void getBookByCategoryId_InvalidId_EmptyList() throws Exception {
        MvcResult horror = mockMvc.perform(get("/categories/100/books")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        List<BookDto> expected = Collections.emptyList();
        BookDto[] actual = objectMapper
                .readValue(horror.getResponse().getContentAsByteArray(), BookDto[].class);
        Assertions.assertEquals(0, actual.length);
        Assertions.assertEquals(expected, Arrays.stream(actual).toList());
    }
}
