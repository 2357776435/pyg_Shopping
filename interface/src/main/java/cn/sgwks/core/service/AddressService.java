package cn.sgwks.core.service;

import cn.sgwks.core.pojo.address.Address;

import java.util.List;

public interface AddressService {
    //用户地址
    List<Address> findListByLoginUser(String userName);
}
