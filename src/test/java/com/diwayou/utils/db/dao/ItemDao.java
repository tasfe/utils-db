package com.diwayou.utils.db.dao;

import com.diwayou.utils.db.domain.Item;

/**
 * Created by cn40387 on 15/9/9.
 */
public interface ItemDao {

    void insert(Item Item);

    Item get(Long id);

    int delete(Long id);

    int update(Item Item);
}
