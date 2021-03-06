package com.diwayou.utils.db.dao.impl;

import com.diwayou.utils.db.dao.UserDao;
import com.diwayou.utils.db.domain.User;
import com.diwayou.utils.db.support.AbstractShardDaoSupport;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by cn40387 on 15/9/9.
 */
@Component("userDao")
public class UserDaoImpl extends AbstractShardDaoSupport implements UserDao {

    @Override
    public String getTableName() {
        return "user";
    }

    @Override
    public void insert(User user) {
        Map<String, Object> parameter = new HashMap<String, Object>();
        parameter.put("user", user);

        insert("com.diwayou.utils.db.dao.UserDao.insert", user.getId(), parameter);
    }

    @Override
    public User get(Long id) {
        Map<String, Object> parameter = new HashMap<String, Object>();
        parameter.put("id", id);

        return selectOne("com.diwayou.utils.db.dao.UserDao.get", id, parameter);
    }

    @Override
    public int delete(Long id) {
        Map<String, Object> parameter = new HashMap<String, Object>();
        parameter.put("id", id);

        return delete("com.diwayou.utils.db.dao.UserDao.delete", id, parameter);
    }

    @Override
    public int update(User user) {
        Map<String, Object> parameter = new HashMap<String, Object>();
        parameter.put("user", user);

        return update("com.diwayou.utils.db.dao.UserDao.update", user.getId(), parameter);
    }
}
