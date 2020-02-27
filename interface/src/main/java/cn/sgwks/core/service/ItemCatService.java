package cn.sgwks.core.service;

import cn.sgwks.core.pojo.item.ItemCat;

import java.util.List;

public interface ItemCatService {
    //根据上级ID查询列表
    List<ItemCat> findByParentId(Long parentId);
    //查询模块ID
    ItemCat findOne(Long id);
    //查询商品管理全部数据
    List<ItemCat> findAll();
}
