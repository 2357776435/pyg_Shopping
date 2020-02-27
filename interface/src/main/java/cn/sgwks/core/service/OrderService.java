package cn.sgwks.core.service;

import cn.sgwks.core.pojo.log.PayLog;
import cn.sgwks.core.pojo.order.Order;

public interface OrderService {
    //提交订单
    void add(Order order);
    //根据用户名获取支付日志对象
    PayLog getPayLogByUserName(String userName);
    //如果支付成功, 支付日志表和订单表的支付状态改为已支付, redis的支付日志对象删除
    void updatePayStatus(String userName);
}
