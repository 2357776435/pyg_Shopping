package cn.sgwks.core.service;

public interface SolrManagerService {
    //根据商品id到solr索引库中删除对应的数据
    void deleteItemFromSolr(Long id);
    //将根据商品id获取库存数据, 放入solr索引库
    void saveItemToSolr(Long id);
    //判断传递的商品id是否存在
    boolean findGoodsId(Long id);
}
