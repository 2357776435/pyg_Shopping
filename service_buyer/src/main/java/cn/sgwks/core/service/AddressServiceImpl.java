package cn.sgwks.core.service;

import cn.sgwks.core.dao.address.AddressDao;
import cn.sgwks.core.pojo.address.Address;
import cn.sgwks.core.pojo.address.AddressQuery;
import com.alibaba.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Service
public class AddressServiceImpl implements AddressService{
    @Autowired
    private AddressDao addressDao;
    /**
     * 用户地址
     * @param userName
     * @return
     */
    @Override
    public List<Address> findListByLoginUser(String userName) {
        AddressQuery query = new AddressQuery();
        AddressQuery.Criteria criteria = query.createCriteria();
        criteria.andUserIdEqualTo(userName);
        List<Address> addressList = addressDao.selectByExample(query);
        return addressList;
    }
}
