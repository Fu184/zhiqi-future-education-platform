package com.zhiqi.platform.user;

import static com.zhiqi.platform.common.ApiException.check;

import com.zhiqi.platform.auth.Requests;
import com.zhiqi.platform.common.Checks;
import com.zhiqi.platform.config.PlatformProperties;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class UserService {
    private final UserRepository users;
    private final PasswordEncoder passwords;

    public UserService(UserRepository users, PasswordEncoder passwords) {
        this.users = users;
        this.passwords = passwords;
    }

    public boolean enabled(PlatformUser user) {
        if (user == null || !user.active()) return false;
        Long parent = user.parentId();
        while (parent != null) {
            var ancestor = users.find(parent);
            if (ancestor == null || !ancestor.active()) return false;
            parent = ancestor.parentId();
        }
        return true;
    }

    public PlatformUser current(long id) {
        var user = users.find(id);
        check(enabled(user), 401, "请先登录");
        return user;
    }

    private PlatformUser manager(long id) {
        var actor = current(id);
        check(actor.role().canManage(), 403, "无用户管理权限");
        return actor;
    }

    private PlatformUser targetFor(PlatformUser actor, long id) {
        var target = users.find(id);
        check(target != null, 404, "用户不存在");
        boolean allowed =
                actor.role() == Role.ADMIN
                        || (target.parentId() != null && target.parentId() == actor.id());
        if (!allowed
                && actor.role() == Role.PRINCIPAL
                && target.role().isStudent()
                && target.parentId() != null) {
            var teacher = users.find(target.parentId());
            allowed =
                    teacher != null
                            && teacher.role() == Role.TEACHER
                            && teacher.parentId() != null
                            && teacher.parentId() == actor.id();
        }
        check(allowed, 403, "无权管理该用户");
        return target;
    }

    @Transactional(isolation = Isolation.READ_COMMITTED, readOnly = true)
    public UsersPage list(long actorId, int page, int pageSize, String search) {
        check(page >= 1 && page <= 1000000 && pageSize >= 1 && pageSize <= 100, 400, "分页参数不合法");
        check(search != null && search.length() <= 100, 400, "搜索内容不超过 100 个字符");
        var actor = manager(actorId);
        String keyword = search.trim();
        return new UsersPage(
                users.page(actor, keyword, page, pageSize),
                users.publicUser(actor),
                users.scopedCount(actor, keyword),
                page,
                pageSize);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED, readOnly = true)
    public List<ParentOption> parents(long actorId, String role, String search) {
        check(manager(actorId).role() == Role.ADMIN, 403, "只有管理员可以选择账号归属");
        Role parentRole = Role.parse(role);
        check(parentRole == Role.PRINCIPAL || parentRole == Role.TEACHER, 400, "只可选择校长或老师");
        check(search != null && search.length() <= 100, 400, "搜索内容不超过 100 个字符");
        return users.parentOptions(parentRole, search.trim());
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public UserView create(long actorId, Requests.CreateUser input) {
        String passwordHash = passwords.encode(Checks.password(input.password()));
        users.lockHierarchies(actorId, input.parentId());
        var actor = manager(actorId);
        Role role = Role.parse(input.role());
        Long parent = input.parentId();
        int teacherQuota = 0, studentQuota = 0;
        if (actor.role() == Role.PRINCIPAL) {
            check(role == Role.TEACHER, 403, "校长只能注册内部老师账号");
            check(parent == null || parent == actor.id(), 403, "不能指定其他校长");
            parent = actor.id();
            studentQuota = Checks.quota(input.studentQuota() == null ? 0 : input.studentQuota());
            check(users.teachersUsed(actor.id()) < actor.teacherQuota(), 409, "老师账号名额已用完");
            check(
                    users.studentsAllocated(actor.id(), 0) + studentQuota <= actor.studentQuota(),
                    409,
                    "学校学生名额不足");
        } else if (actor.role() == Role.TEACHER) {
            check(role == Role.LEARNER, 403, "老师只能注册可学习学生账号");
            check(parent == null || parent == actor.id(), 403, "不能指定其他老师");
            parent = actor.id();
            check(users.studentsUsed(actor.id()) < actor.studentQuota(), 409, "学生账号名额已用完");
        } else {
            teacherQuota =
                    role == Role.PRINCIPAL
                            ? Checks.quota(input.teacherQuota() == null ? 0 : input.teacherQuota())
                            : 0;
            studentQuota =
                    role == Role.PRINCIPAL || role == Role.TEACHER
                            ? Checks.quota(input.studentQuota() == null ? 0 : input.studentQuota())
                            : 0;
            if (role == Role.TEACHER) {
                var principal = parent == null ? null : users.find(parent);
                check(
                        enabled(principal) && principal.role() == Role.PRINCIPAL,
                        400,
                        "老师账号必须归属有效校长");
                check(users.teachersUsed(parent) < principal.teacherQuota(), 409, "校长老师名额已用完");
                check(
                        users.studentsAllocated(parent, 0) + studentQuota
                                <= principal.studentQuota(),
                        409,
                        "学校学生名额不足");
            } else if (parent != null) {
                check(role.isStudent(), 400, "该角色不能设置上级");
                var teacher = users.find(parent);
                check(enabled(teacher) && teacher.role() == Role.TEACHER, 400, "学生的上级必须是有效老师");
                check(users.studentsUsed(parent) < teacher.studentQuota(), 409, "老师学生名额已用完");
            }
        }
        check(users.byPhone(input.phone()) == null, 409, "手机号已注册");
        long id =
                users.insert(
                        Checks.phone(input.phone()),
                        Checks.name(input.name()),
                        passwordHash,
                        role,
                        parent,
                        teacherQuota,
                        studentQuota);
        users.audit(actor.id(), "create-user", id);
        return users.publicUser(users.find(id));
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public UserView update(long actorId, long targetId, UserUpdate input) {
        check(input != null, 400, "请求不能为空");
        String replacementHash =
                input.has("password") ? passwords.encode(Checks.password(input.password())) : null;
        users.lockHierarchies(actorId, targetId);
        var actor = manager(actorId);
        var target = targetFor(actor, targetId);
        var allowed =
                actor.role() == Role.ADMIN
                        ? Set.of(
                                "name",
                                "phone",
                                "active",
                                "password",
                                "teacherQuota",
                                "studentQuota",
                                "role")
                        : Set.of("name", "active", "password", "studentQuota");
        check(!input.fields().isEmpty() && allowed.containsAll(input.fields()), 400, "包含不允许修改的字段");
        String name = target.name(), phone = target.phone(), passwordHash = target.passwordHash();
        Role role = target.role();
        boolean active = target.active();
        int teachers = target.teacherQuota(), students = target.studentQuota();
        if (input.has("name")) name = Checks.name(input.name());
        if (input.has("phone")) {
            phone = Checks.phone(input.phone());
            var samePhone = users.byPhone(phone);
            check(samePhone == null || samePhone.id() == target.id(), 409, "手机号已注册");
        }
        if (input.has("role")) {
            check(input.role() != null, 400, "无效的角色");
            role = Role.parse(input.role());
            check(
                    target.role().isStudent() && role.isStudent(),
                    400,
                    "仅支持普通与可学习用户互转；机构、管理与政府角色请创建专用账号");
        }
        if (input.has("active")) {
            check(input.active() != null, 400, "active 必须为布尔值");
            active = input.active();
            check(actor.id() != target.id() || active, 400, "不能停用自己的账号");
            if (target.role() == Role.ADMIN && !active) {
                check(users.hasOtherAdmin(target.id()), 409, "不能停用最后一个管理员");
            }
        }
        if (input.has("password")) passwordHash = replacementHash;
        if (input.has("teacherQuota")) {
            check(target.role() == Role.PRINCIPAL, 400, "仅校长拥有老师名额");
            teachers = Checks.quota(input.teacherQuota());
            check(teachers >= users.teachersUsed(target.id()), 409, "名额不能小于已创建老师数");
        }
        if (input.has("studentQuota")) {
            check(
                    target.role() == Role.PRINCIPAL || target.role() == Role.TEACHER,
                    400,
                    "仅校长或老师拥有学生名额");
            check(
                    actor.role() == Role.ADMIN
                            || (actor.role() == Role.PRINCIPAL && target.role() == Role.TEACHER),
                    403,
                    "无权调整学生名额");
            students = Checks.quota(input.studentQuota());
            if (target.role() == Role.PRINCIPAL) {
                check(students >= users.studentsAllocated(target.id(), 0), 409, "不能低于已分配给老师的学生名额");
            } else {
                check(students >= users.studentsUsed(target.id()), 409, "不能低于已创建学生数");
                var principal = users.find(target.parentId());
                check(
                        users.studentsAllocated(principal.id(), target.id()) + students
                                <= principal.studentQuota(),
                        409,
                        "学校学生名额不足");
            }
        }
        users.update(
                new PlatformUser(
                        target.id(),
                        phone,
                        name,
                        passwordHash,
                        role,
                        target.parentId(),
                        teachers,
                        students,
                        active,
                        target.createdAt()));
        if (!active
                || !phone.equals(target.phone())
                || role != target.role()
                || input.has("password")) {
            users.revokeSessions(target.id());
        }
        users.audit(actor.id(), "update-user", target.id());
        return users.publicUser(users.find(target.id()));
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public UserView confirmPurchase(long actorId, long targetId, String reference) {
        users.lockHierarchies(actorId, targetId);
        var actor = manager(actorId);
        check(actor.role() == Role.ADMIN, 403, "只有管理员可确认购买");
        var target = targetFor(actor, targetId);
        check(target.role().isStudent(), 400, "只可为普通或可学习用户确认购买");
        check(
                reference != null && reference.trim().length() >= 3 && reference.length() <= 100,
                400,
                "请输入实际购买凭证编号");
        check(!users.purchaseExists(reference.trim()), 409, "购买凭证已使用");
        users.confirmPurchase(target.id(), reference.trim(), actor.id());
        users.audit(actor.id(), "confirm-purchase", target.id());
        return users.publicUser(users.find(target.id()));
    }

    @Transactional(isolation = Isolation.READ_COMMITTED, readOnly = true)
    public Map<String, Object> statistics(long actorId) {
        var actor = current(actorId);
        check(actor.role() == Role.ADMIN || actor.role() == Role.GOVERNMENT, 403, "仅政府账号和管理员可查看统计");
        List<Map<String, Object>> roles =
                Arrays.stream(Role.values())
                        .map(
                                role ->
                                        Map.<String, Object>of(
                                                "role",
                                                role.code(),
                                                "label",
                                                role.label(),
                                                "count",
                                                users.roleCount(role)))
                        .toList();
        return Map.of(
                "totalUsers",
                users.totalUsers(),
                "activeUsers",
                users.activeUsers(),
                "roles",
                roles,
                "confirmedPurchases",
                users.purchaseCount(),
                "generatedAt",
                java.time.Instant.now().toString());
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void bootstrap(PlatformProperties properties) {
        var config = properties.bootstrap();
        if (!config.enabled() || !Checks.hasText(config.phone())) return;
        Checks.phone(config.phone());
        Checks.password(config.password());
        check(
                !properties.production()
                        || (!"DevAdmin123!".equals(config.password())
                                && !"ChangeThisPassword123!".equals(config.password())),
                500,
                "生产环境不能使用开发管理员密码");
        String hash = passwords.encode(config.password());
        users.lockPhone(config.phone());
        if (users.byPhone(config.phone()) == null) {
            long id =
                    users.insert(
                            config.phone(),
                            Checks.name(config.name()),
                            hash,
                            Role.ADMIN,
                            null,
                            0,
                            0);
            users.audit(id, "bootstrap-admin", id);
        }
    }
}
