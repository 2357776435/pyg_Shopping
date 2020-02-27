package cn.sgwks.core.service;

import cn.sgwks.core.dao.item.ItemDao;
import cn.sgwks.core.pojo.item.Item;
import cn.sgwks.core.pojo.item.ItemQuery;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.Criteria;
import org.springframework.data.solr.core.query.Query;
import org.springframework.data.solr.core.query.SimpleQuery;

import java.util.List;
import java.util.Map;


@Service
public class SolrManagerServiceImpl implements SolrManagerService {
    @Autowired
    private ItemDao itemDao;
    @Autowired
    private SolrTemplate solrTemplate;

    /**
     * 根据商品id到solr索引库中删除对应的数据
     *
     * @param id
     */
    @Override
    public void deleteItemFromSolr(Long id) {
        //创建查询对象
        Query query = new SimpleQuery();
        //创建条件对象,is是将查询关键字使用对应这个域的分词器进行切分词, 然后将切分出来的每个词, 进行查询.
        Criteria criteria = new Criteria("item_goodsid").is(id);
        query.addCriteria(criteria);
        //删除
        solrTemplate.delete(query);
        //提交
        solrTemplate.commit();
    }

    /**
     * 将根据商品id获取库存数据, 放入solr索引库
     *
     * @param id
     */
    @Override
    public void saveItemToSolr(Long id) {
        ItemQuery query = new ItemQuery();
        ItemQuery.Criteria criteria = query.createCriteria();
        //查询指定商品的库存数据
        criteria.andGoodsIdEqualTo(id);
        List<Item> items = itemDao.selectByExample(query);
        if (items != null) {
            for (Item item : items) {
                //获取规格json格式字符串
                String specJsonStr = item.getSpec();
                Map map = JSON.parseObject(specJsonStr, Map.class);
                item.setSpecMap(map);
            }
            //保存
            solrTemplate.saveBeans(items);
            //提交
            solrTemplate.commit();
        }
    }

    @Override
    public boolean findGoodsId(Long id) {
        Query query = new SimpleQuery();
        //创建条件对象,is是将查询关键字使用对应这个域的分词器进行切分词, 然后将切分出来的每个词, 进行查询.
        Criteria criteria = new Criteria("item_goodsid").is(id);
        query.addCriteria(criteria);
        Item item = solrTemplate.queryForObject(query, Item.class);
        if (item == null) {//如果改对象为空，说明该商品需要上架
            saveItemToSolr(id);//将根据商品id获取库存数据, 放入solr索引库
            return false;
        } else {//否则不为空，商品需要下架
            deleteItemFromSolr(id);//根据商品id到solr索引库中删除对应的数据
            return true;
        }
    }
}
