package cn.sgwks.core.listener;

import cn.sgwks.core.service.CmsService;
import cn.sgwks.core.service.SolrManagerService;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.springframework.beans.factory.annotation.Autowired;

import javax.jms.Message;
import javax.jms.MessageListener;
import java.util.Map;

/**
 * 发布订阅模式，接收方2
 */
public class PageListener implements MessageListener{

    @Autowired
    private CmsService cmsService;

    @Autowired
    private SolrManagerService solrManagerService;//solr搜索管理器

    @Override
    public void onMessage(Message message) {
        ActiveMQTextMessage amt = (ActiveMQTextMessage) message;
        try {
            String goodsIdStr = amt.getText();
            Long goodsId=Long.parseLong(goodsIdStr);
            Map<String, Object> goodsData = cmsService.findGoodsData(goodsId);
            boolean findGoodsId = solrManagerService.findGoodsId(goodsId);
            if (findGoodsId){//刚审核，但solr中没有数据,如果返回true，说明solr中有数据,说明生成下架页面
                cmsService.createStaticDelPage(goodsId,goodsData);
            }else {//如果solr中没有数据，说明需要上架生成上架页面
                cmsService.createStaticPage(goodsId,goodsData);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
