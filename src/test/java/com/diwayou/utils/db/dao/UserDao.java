package com.diwayou.utils.db.dao;

import com.diwayou.utils.db.domain.User;

/**
 * Created by cn40387 on 15/9/9.
 */
public interface UserDao {

    void insert(User user);

    User get(Long id);

    int delete(Long id);

    int update(User user);
}
