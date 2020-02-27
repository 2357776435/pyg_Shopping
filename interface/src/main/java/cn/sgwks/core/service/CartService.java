package cn.sgwks.core.service;

import cn.sgwks.core.pojo.entity.BuyerCart;

import java.util.List;

public interface CartService {
    //添加商品到购物车
    List<BuyerCart> addItemToCartList(List<BuyerCart> cartList, Long itemId, Integer num);
    //如果已登录, 则将购物车列表存入redis中
    void setCartListToRedis(String userName, List<BuyerCart> cartList);
    //已登录, 从redis中获取购物车列表对象
    List<BuyerCart> getCartListFromRedis(String userName);
    //如果cookie中存在购物车列表则和redis中的购物车列表合并成一个对象
    List<BuyerCart> mergeCookieCartListToRedisCartList(List<BuyerCart> cookieCartList, List<BuyerCart> redisCartList);
}
