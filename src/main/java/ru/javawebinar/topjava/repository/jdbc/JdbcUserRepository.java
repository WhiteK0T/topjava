package ru.javawebinar.topjava.repository.jdbc;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import ru.javawebinar.topjava.model.Role;
import ru.javawebinar.topjava.model.User;
import ru.javawebinar.topjava.repository.UserRepository;

import java.util.*;

@Repository
@Transactional(readOnly = true)
public class JdbcUserRepository implements UserRepository {

    private static final BeanPropertyRowMapper<User> ROW_MAPPER = BeanPropertyRowMapper.newInstance(User.class);

    private final JdbcTemplate jdbcTemplate;

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    private final SimpleJdbcInsert insertUser;

    @Autowired
    public JdbcUserRepository(JdbcTemplate jdbcTemplate, NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.insertUser = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("users")
                .usingGeneratedKeyColumns("id");

        this.jdbcTemplate = jdbcTemplate;
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }

    @Override
    @Transactional
    public User save(User user) {
        BeanPropertySqlParameterSource parameterSource = new BeanPropertySqlParameterSource(user);
        Set<Role> roles = user.getRoles();

        if (user.isNew()) {
            Number newKey = insertUser.executeAndReturnKey(parameterSource);
            user.setId(newKey.intValue());
            if (!CollectionUtils.isEmpty(roles)) {
                jdbcTemplate.batchUpdate("INSERT INTO user_role (user_id, role) VALUES (?, ?)", roles, roles.size(),
                        (ps, role) -> {
                            ps.setInt(1, user.id());
                            ps.setString(2, role.name());
                        });
            }
        } else {
            if (namedParameterJdbcTemplate.update("""
                       UPDATE users SET name=:name, email=:email, password=:password, 
                       registered=:registered, enabled=:enabled, calories_per_day=:caloriesPerDay WHERE id=:id
                    """, parameterSource) == 0) {
                return null;
            }
            List<Role> rolesInDB = getRoles(user);
            if (!CollectionUtils.isEmpty(roles)) {
                roles.forEach(role -> {
                    if (rolesInDB.contains(role)) {
                        jdbcTemplate.update("UPDATE user_role SET user_id=?, role=? WHERE user_id=?", user.id(), role.name(), user.id());
                    } else {
                        jdbcTemplate.update("INSERT INTO user_role (user_id, role) VALUES (?, ?)", user.id(), role.name());
                    }
                });
                rolesInDB.removeAll(roles);
                rolesInDB.forEach(role -> jdbcTemplate.update("DELETE FROM user_role WHERE user_id=? AND role=?", user.getId(), role.name()));
            }
        }
        return user;
    }

    @Override
    @Transactional
    public boolean delete(int id) {
        return jdbcTemplate.update("DELETE FROM users WHERE id=?", id) != 0;
    }

    @Override
    public User get(int id) {
        List<User> users = jdbcTemplate.query("SELECT * FROM users WHERE id=?", ROW_MAPPER, id);
        return getRolesAndSetRolesUser(DataAccessUtils.singleResult(users));
    }

    @Override
    public User getByEmail(String email) {
//        return jdbcTemplate.queryForObject("SELECT * FROM users WHERE email=?", ROW_MAPPER, email);
        List<User> users = jdbcTemplate.query("SELECT * FROM users WHERE email=?", ROW_MAPPER, email);
        return getRolesAndSetRolesUser(DataAccessUtils.singleResult(users));
    }

    @Override
    public List<User> getAll() {
        List<User> users = jdbcTemplate.query("SELECT * FROM users ORDER BY name, email", ROW_MAPPER);
        Map<Integer, Set<Role>> map = new HashMap<>();
        jdbcTemplate.query("SELECT * FROM user_role", rs -> {
            map.computeIfAbsent(rs.getInt("user_id"), userId -> EnumSet.noneOf(Role.class))
                    .add(Role.valueOf(rs.getString("role")));
        });
        users.forEach(u -> u.setRoles(map.get(u.getId())));
        return users;
    }

    private User getRolesAndSetRolesUser(User user) {
        if (user != null) {
            user.setRoles(getRoles(user));
        }
        return user;
    }

    private List<Role> getRoles(User user) {
        return jdbcTemplate.queryForList("SELECT role FROM user_role  WHERE user_id=?", Role.class, user.getId());
    }
}
