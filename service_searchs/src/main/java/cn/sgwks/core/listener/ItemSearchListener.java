package cn.sgwks.core.listener;

import cn.sgwks.core.service.SolrManagerService;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.springframework.beans.factory.annotation.Autowired;

import javax.jms.Message;
import javax.jms.MessageListener;

/**
 * 发布订阅模式，接收方1
 */
public class ItemSearchListener implements MessageListener {
    @Autowired
    private SolrManagerService solrManagerService;//solr搜索管理器

    @Override
    public void onMessage(Message message) {
        //为了方便获取文本消息, 将原生的消息对象转换成activeMq的文本消息对象
        ActiveMQTextMessage atm = (ActiveMQTextMessage) message;
        try {
            //传递过来的id是字符串类型
            String goodsId = atm.getText();
            solrManagerService.saveItemToSolr(Long.parseLong(goodsId));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
