package mate.academy.intro.service;

import mate.academy.intro.dto.user.UserRegistrationRequestDto;
import mate.academy.intro.dto.user.UserResponseDto;
import mate.academy.intro.exception.RegistrationException;
import mate.academy.intro.mapper.UserMapper;
import mate.academy.intro.model.Role;
import mate.academy.intro.model.ShoppingCart;
import mate.academy.intro.model.User;
import mate.academy.intro.repository.role.RoleRepository;
import mate.academy.intro.repository.shoppingcart.ShoppingCartRepository;
import mate.academy.intro.repository.user.UserRepository;
import mate.academy.intro.service.impl.UserServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {
    @InjectMocks
    private UserServiceImpl userService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private UserMapper userMapper;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private ShoppingCartRepository shoppingCartRepository;
    private static final Long USER_ID = 1L;
    private static final String EMAIL = "user@mail.com";
    private static final String PASSWORD = "password";
    private static final String SAVED_PASS = "EnCoDeDpAsSwOrD";
    private static final String FIRST_NAME = "Bob";
    private static final String LAST_NAME = "Marley";

    @Test
    @DisplayName("Register new user")
    public void register_RegistrationRequestDto_ReturnUserResponseDto() throws RegistrationException {
        UserRegistrationRequestDto requestDto = new UserRegistrationRequestDto()
                .setEmail(EMAIL)
                .setPassword(PASSWORD)
                .setPasswordRepeat(PASSWORD)
                .setFirstName("name")
                .setLastName("last");
        User user = new User()
                .setEmail(EMAIL)
                .setFirstName(FIRST_NAME)
                .setLastName(LAST_NAME);
        Role role = new Role().setId(1L).setName(Role.RoleName.ROLE_USER);
        UserResponseDto expected = new UserResponseDto()
                .setId(USER_ID)
                .setEmail(EMAIL)
                .setFirstName(FIRST_NAME)
                .setLastName(LAST_NAME);
        ShoppingCart cart = new ShoppingCart()
                .setUser(user);

        when(userRepository.findByEmail(requestDto.getEmail())).thenReturn(Optional.empty());
        when(userMapper.toModel(requestDto)).thenReturn(user);
        when(passwordEncoder.encode(requestDto.getPassword())).thenReturn(SAVED_PASS);
        when(roleRepository.findRoleByName(Role.RoleName.ROLE_USER)).thenReturn(Optional.of(role));
        when(userRepository.save(user)).thenReturn(user.setId(USER_ID));
        when(shoppingCartRepository.save(cart)).thenReturn(new ShoppingCart());
        when(userMapper.toDto(user)).thenReturn(expected);

        UserResponseDto actual = userService.register(requestDto);
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Register new user, password and passwordRepeat is not equals")
    public void register_DifferentPassAndPassRepeat_ThrowException() {
        UserRegistrationRequestDto requestDto = new UserRegistrationRequestDto()
                .setEmail(EMAIL)
                .setPassword(PASSWORD)
                .setPasswordRepeat(SAVED_PASS);

        when(userRepository.findByEmail(requestDto.getEmail())).thenReturn(Optional.empty());

        Exception exception = assertThrows(RegistrationException.class,
                () -> userService.register(requestDto));
        String expected = "Unable to complete registration";
        String actual = exception.getMessage();
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Register new user, when it's already exist")
    public void register_RegisterExistedUser_ThrowException() {
        UserRegistrationRequestDto requestDto = new UserRegistrationRequestDto()
                .setEmail("existedUser@mail.com");

        when(userRepository.findByEmail(requestDto.getEmail())).thenReturn(Optional.of(new User()));

        Exception exception = assertThrows(RegistrationException.class,
                () -> userService.register(requestDto));
        String expected = "Unable to complete registration";
        String actual = exception.getMessage();
        assertEquals(expected, actual);
    }
}
