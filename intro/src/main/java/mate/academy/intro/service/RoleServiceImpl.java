package mate.academy.intro.service;

import lombok.RequiredArgsConstructor;
import mate.academy.intro.model.Role;
import mate.academy.intro.repository.role.RoleRepository;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class RoleServiceImpl implements RoleService {
    private final RoleRepository roleRepository;

    @Override
    public Role getRoleByName(Role.RoleName roleName) {
        return roleRepository.getRoleByName(roleName);
    }
}
