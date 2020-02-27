package cn.sgwks.core.util;

/**
 * 常量接口
 */
public interface Constants {
    public final static String CONTENT_LIST_REDIS = "contentList";
    //分类集合
    public final static String CATEGORY_LIST_REDIS = "categoryList";
    //产品集合
    public final static String BRAND_LIST_REDIS = "brandList";
    public final static String SPEC_LIST_REDIS = "specList";

    //购物车集合存储Cookie
    public final static String CART_LIST_COOKIE = "pyg_cartList";
    //购物车集合存储Redis
    public final static String CART_LIST_REDIS = "pyg_cartList";
    //使用当前登录用户的用户名作为key, 支付日志对象作为value存入redis中供支付使用
    public final static String PAY_LIST_LOG = "pyg_pay_log";
}
