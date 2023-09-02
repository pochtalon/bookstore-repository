package mate.academy.intro.service;

import mate.academy.intro.model.Role;

public interface RoleService {
    Role getRoleByName(Role.RoleName roleName);
}
