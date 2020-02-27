package cn.sgwks.core.util;

import cn.sgwks.core.dao.item.ItemDao;
import cn.sgwks.core.pojo.item.Item;
import cn.sgwks.core.pojo.item.ItemQuery;
import com.alibaba.fastjson.JSON;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class DataImportToSolr {
    @Autowired
    private SolrTemplate solrTemplate;
    @Autowired
    private ItemDao itemDao;
    public void importItemDataToSolr() {
        //创建spring的条件查询对象
        ItemQuery itemQuery = new ItemQuery();
        ItemQuery.Criteria criteria = itemQuery.createCriteria();
        //1:查询库存商品状态可售1的数据
        criteria.andStatusEqualTo("1");
        List<Item> itemList = itemDao.selectByExample(itemQuery);
        if (itemList!=null){
            for (Item item : itemList) {
                //获取规格json格式字符串
                String specJsonStr = item.getSpec();
                Map map = JSON.parseObject(specJsonStr, Map.class);
                item.setSpecMap(map);
            }
            //保存
            solrTemplate.saveBeans(itemList);
            //提交
            solrTemplate.commit();
        }
    }
    public static void main(String[] args) {
        ApplicationContext context = new ClassPathXmlApplicationContext("classpath*:spring/applicationContext*.xml");
        //获取唯一的标识符，改类默认的就是类名首字母小写
        DataImportToSolr bean = (DataImportToSolr)context.getBean("dataImportToSolr");
        bean.importItemDataToSolr();
    }
}
