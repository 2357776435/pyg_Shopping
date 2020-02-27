package cn.sgwks.core.service;

import cn.sgwks.core.pojo.entity.GoodsEntity;
import cn.sgwks.core.pojo.entity.PageResult;
import cn.sgwks.core.pojo.good.Goods;

public interface GoodsService {
    //添加
    void add(GoodsEntity goodsEntity);
    //搜索
    PageResult search(Integer page, Integer rows, Goods goods);
    //修改数据
    void update(GoodsEntity goodsEntity);
    //根据指定的id回显数据
    GoodsEntity findOne(Long id);
    //批量删除+solr
    void delete(Long id);
    //修改商品状态+solr
    void updateStatus(Long id, String status);
    //提交审核
    void insertUpdateStatus(Long id);
}
