package com.diwayou.utils.db.dao.impl;

import com.diwayou.utils.db.dao.ItemDao;
import com.diwayou.utils.db.domain.Item;
import com.diwayou.utils.db.support.AbstractShardDaoSupport;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by cn40387 on 15/9/11.
 */
@Component("itemDao")
public class ItemDaoImpl extends AbstractShardDaoSupport implements ItemDao {

    @Override
    public String getTableName() {
        return "item";
    }

    @Override
    public void insert(Item item) {
        Map<String, Object> parameter = new HashMap<String, Object>();
        parameter.put("item", item);

        insert("com.diwayou.utils.db.dao.ItemDao.insert", item.getId(), parameter);
    }

    @Override
    public Item get(Long id) {
        Map<String, Object> parameter = new HashMap<String, Object>();
        parameter.put("id", id);

        return selectOne("com.diwayou.utils.db.dao.ItemDao.get", id, parameter);
    }

    @Override
    public int delete(Long id) {
        Map<String, Object> parameter = new HashMap<String, Object>();
        parameter.put("id", id);

        return delete("com.diwayou.utils.db.dao.ItemDao.delete", id, parameter);
    }

    @Override
    public int update(Item item) {
        Map<String, Object> parameter = new HashMap<String, Object>();
        parameter.put("item", item);

        return update("com.diwayou.utils.db.dao.ItemDao.update", item.getId(), parameter);
    }
}
