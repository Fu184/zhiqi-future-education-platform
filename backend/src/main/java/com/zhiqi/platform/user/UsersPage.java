package com.zhiqi.platform.user;

import java.util.List;

public record UsersPage(List<UserView> users, UserView me, int total, int page, int pageSize) {}
