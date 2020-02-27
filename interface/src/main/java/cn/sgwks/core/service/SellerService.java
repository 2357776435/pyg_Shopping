package cn.sgwks.core.service;

import cn.sgwks.core.pojo.entity.PageResult;
import cn.sgwks.core.pojo.seller.Seller;

public interface SellerService {
    //商家注册
    void add(Seller seller);
    //查询审核商家未通过
    PageResult search(Integer page, Integer rows, Seller seller);
    //审核数据回显
    Seller findOne(String id);
    // * 改变商家审核状态，@param sellerId 卖家id，@param status 状态码
    void updateStatus(String sellerId, String status);
}
