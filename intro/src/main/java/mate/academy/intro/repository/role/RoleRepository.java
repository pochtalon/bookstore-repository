package mate.academy.intro.repository.role;

import java.util.Optional;
import mate.academy.intro.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findRoleByName(Role.RoleName roleName);
}
