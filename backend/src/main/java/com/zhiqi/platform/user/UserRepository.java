package com.zhiqi.platform.user;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public class UserRepository {
    private final JdbcTemplate jdbc;
    private static final RowMapper<PlatformUser> MAPPER =
            (row, index) ->
                    new PlatformUser(
                            row.getLong("id"),
                            row.getString("phone"),
                            row.getString("name"),
                            row.getString("password_hash"),
                            Role.parse(row.getString("role")),
                            row.getObject("parent_id", Long.class),
                            row.getInt("teacher_quota"),
                            row.getInt("student_quota"),
                            row.getBoolean("active"),
                            row.getTimestamp("created_at").toLocalDateTime());

    private final com.zhiqi.platform.common.OperationLocks locks;

    public UserRepository(JdbcTemplate jdbc, com.zhiqi.platform.common.OperationLocks locks) {
        this.jdbc = jdbc;
        this.locks = locks;
    }

    public void lockPhone(String phone) {
        locks.acquire("phone:" + phone);
    }

    public void lockHierarchies(Long... ids) {
        var keys = new java.util.TreeSet<String>();
        for (Long id : ids) {
            if (id == null) continue;
            var user = find(id);
            if (user == null) continue;
            if (user.role() == Role.ADMIN) keys.add("administrators");
            while (user.parentId() != null) {
                var parent = find(user.parentId());
                if (parent == null) break;
                user = parent;
            }
            keys.add("hierarchy:" + user.id());
        }
        keys.forEach(locks::acquire);
    }

    public PlatformUser find(long id) {
        return jdbc.query("SELECT * FROM users WHERE id = ?", MAPPER, id).stream()
                .findFirst()
                .orElse(null);
    }

    public PlatformUser byPhone(String phone) {
        return jdbc.query("SELECT * FROM users WHERE phone = ?", MAPPER, phone).stream()
                .findFirst()
                .orElse(null);
    }

    public long insert(
            String phone,
            String name,
            String passwordHash,
            Role role,
            Long parent,
            int teachers,
            int students) {
        var keys = new GeneratedKeyHolder();
        jdbc.update(
                connection -> {
                    var statement =
                            connection.prepareStatement(
                                    "INSERT INTO"
                                        + " users(phone,name,password_hash,role,parent_id,teacher_quota,student_quota)"
                                        + " VALUES(?,?,?,?,?,?,?)",
                                    new String[] {"id"});
                    statement.setString(1, phone);
                    statement.setString(2, name);
                    statement.setString(3, passwordHash);
                    statement.setString(4, role.code());
                    statement.setObject(5, parent);
                    statement.setInt(6, teachers);
                    statement.setInt(7, students);
                    return statement;
                },
                keys);
        return keys.getKey().longValue();
    }

    public void update(PlatformUser user) {
        jdbc.update(
                "UPDATE users SET phone=?, name=?, password_hash=?, role=?, teacher_quota=?,"
                        + " student_quota=?, active=? WHERE id=?",
                user.phone(),
                user.name(),
                user.passwordHash(),
                user.role().code(),
                user.teacherQuota(),
                user.studentQuota(),
                user.active(),
                user.id());
    }

    private String scopeCondition(PlatformUser actor, List<Object> args) {
        return switch (actor.role()) {
            case ADMIN -> "TRUE";
            case PRINCIPAL -> {
                args.add(actor.id());
                args.add(actor.id());
                yield "(u.parent_id=? OR u.parent_id IN (SELECT id FROM users WHERE parent_id=? AND"
                        + " role='teacher'))";
            }
            case TEACHER -> {
                args.add(actor.id());
                yield "u.parent_id=?";
            }
            default -> "FALSE";
        };
    }

    private String searchCondition(String search, List<Object> args) {
        if (search.isBlank()) return "";
        args.add(search);
        args.add(search);
        args.add(search);
        var roles =
                java.util.Arrays.stream(Role.values())
                        .filter(role -> role.label().contains(search))
                        .map(Role::code)
                        .toList();
        String extra =
                roles.isEmpty()
                        ? ""
                        : " OR u.role IN ("
                                + String.join(",", java.util.Collections.nCopies(roles.size(), "?"))
                                + ")";
        args.addAll(roles);
        return " AND (LOCATE(?,u.name)>0 OR LOCATE(?,u.phone)>0 OR LOCATE(?,u.role)>0"
                + extra
                + ")";
    }

    public int scopedCount(PlatformUser actor, String search) {
        var args = new java.util.ArrayList<Object>();
        String condition = scopeCondition(actor, args) + searchCondition(search, args);
        return count("SELECT COUNT(*) FROM users u WHERE " + condition, args.toArray());
    }

    public List<UserView> page(PlatformUser actor, String search, int page, int pageSize) {
        var args = new java.util.ArrayList<Object>();
        String condition = scopeCondition(actor, args) + searchCondition(search, args);
        args.add(pageSize);
        args.add((page - 1) * pageSize);
        var parentNames = new java.util.HashMap<Long, String>();
        var rows =
                jdbc.query(
                        "SELECT u.*, p.name AS parent_name FROM users u LEFT JOIN users p ON"
                                + " p.id=u.parent_id WHERE "
                                + condition
                                + " ORDER BY u.id DESC LIMIT ? OFFSET ?",
                        (row, index) -> {
                            parentNames.put(row.getLong("id"), row.getString("parent_name"));
                            return MAPPER.mapRow(row, index);
                        },
                        args.toArray());
        var counts = usage(rows.stream().map(PlatformUser::id).toList());
        return rows.stream()
                .map(
                        user -> {
                            int[] used = counts.getOrDefault(user.id(), new int[] {0, 0});
                            return UserView.of(user, used[0], used[1], parentNames.get(user.id()));
                        })
                .toList();
    }

    private Map<Long, int[]> usage(List<Long> ids) {
        var result = new java.util.HashMap<Long, int[]>();
        if (ids.isEmpty()) return result;
        String slots = String.join(",", java.util.Collections.nCopies(ids.size(), "?"));
        jdbc.query(
                "SELECT parent_id, SUM(CASE WHEN role='teacher' THEN 1 ELSE 0 END) AS teachers,"
                    + " SUM(CASE WHEN role IN ('ordinary','learner') THEN 1 ELSE 0 END) AS students"
                    + " FROM users WHERE parent_id IN ("
                        + slots
                        + ") GROUP BY parent_id",
                (org.springframework.jdbc.core.RowCallbackHandler)
                        row -> {
                            result.put(
                                    row.getLong("parent_id"),
                                    new int[] {row.getInt("teachers"), row.getInt("students")});
                        },
                ids.toArray());
        return result;
    }

    public List<ParentOption> parentOptions(Role role, String search) {
        return jdbc.query(
                "SELECT u.id,u.name,u.phone,u.role FROM users u LEFT JOIN users p ON"
                        + " p.id=u.parent_id WHERE u.role=? AND u.active=TRUE AND (p.id IS NULL OR"
                        + " p.active=TRUE) AND (LOCATE(?,u.name)>0 OR LOCATE(?,u.phone)>0) ORDER BY"
                        + " u.id DESC LIMIT 50",
                (row, index) ->
                        new ParentOption(
                                row.getLong("id"),
                                row.getString("name"),
                                row.getString("phone"),
                                row.getString("role")),
                role.code(),
                search,
                search);
    }

    public int teachersUsed(long id) {
        return count("SELECT COUNT(*) FROM users WHERE parent_id=? AND role='teacher'", id);
    }

    public int studentsUsed(long id) {
        return count(
                "SELECT COUNT(*) FROM users WHERE parent_id=? AND role IN ('ordinary','learner')",
                id);
    }

    public int studentsAllocated(long id, long excludeId) {
        return count(
                "SELECT COALESCE(SUM(student_quota),0) FROM users WHERE parent_id=? AND"
                        + " role='teacher' AND id<>?",
                id,
                excludeId);
    }

    public int count(String sql, Object... args) {
        return jdbc.queryForObject(sql, Integer.class, args);
    }

    public void audit(long actor, String action, long target) {
        jdbc.update(
                "INSERT INTO audit_logs(actor_id,action,target_id) VALUES(?,?,?)",
                actor,
                action,
                target);
    }

    public UserView publicUser(PlatformUser user) {
        var counts = usage(List.of(user.id())).getOrDefault(user.id(), new int[] {0, 0});
        var parent = user.parentId() == null ? null : find(user.parentId());
        return UserView.of(user, counts[0], counts[1], parent == null ? null : parent.name());
    }

    public void revokeSessions(long id) {
        jdbc.update("DELETE FROM sessions WHERE user_id=?", id);
    }

    public void changePassword(long id, String hash) {
        jdbc.update("UPDATE users SET password_hash=? WHERE id=?", hash, id);
    }

    public boolean hasOtherAdmin(long id) {
        return count("SELECT COUNT(*) FROM users WHERE role='admin' AND active=TRUE AND id<>?", id)
                > 0;
    }

    public boolean purchaseExists(String reference) {
        return count("SELECT COUNT(*) FROM purchases WHERE purchase_reference=?", reference) > 0;
    }

    public void confirmPurchase(long user, String reference, long actor) {
        jdbc.update(
                "INSERT INTO purchases(user_id,purchase_reference,confirmed_by) VALUES(?,?,?)",
                user,
                reference,
                actor);
        jdbc.update("UPDATE users SET role='learner' WHERE id=?", user);
    }

    public int totalUsers() {
        return count("SELECT COUNT(*) FROM users");
    }

    public int activeUsers() {
        return count("SELECT COUNT(*) FROM users WHERE active=TRUE");
    }

    public int roleCount(Role role) {
        return count("SELECT COUNT(*) FROM users WHERE role=?", role.code());
    }

    public int purchaseCount() {
        return count("SELECT COUNT(*) FROM purchases");
    }
}
