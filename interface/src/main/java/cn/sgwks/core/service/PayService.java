package cn.sgwks.core.service;

import java.util.Map;

public interface PayService {
    //获取当前登录用户名, 根据用户名获取redis中的支付日志对象, 根据支付日志对象中的支付单号和总金额,调用微信统一下单接口, 生成支付链接返回
    Map createNative(String outTradeNo, String totalFee);
    //调用查询订单接口, 查询是否支付成功
    Map queryPayStatus(String out_trade_no);
}
