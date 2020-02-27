package cn.sgwks.core.service;

import cn.sgwks.core.dao.seller.SellerDao;
import cn.sgwks.core.pojo.entity.PageResult;
import cn.sgwks.core.pojo.seller.Seller;
import cn.sgwks.core.pojo.seller.SellerQuery;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Service
@Transactional
public class SellerServiceImpl implements SellerService{
    @Autowired
    private SellerDao sellerDao;
    @Override
    public void add(Seller seller) {
        seller.setCreateTime(new Date());
        //审核状态，注册的时候默认为0，未审核
        seller.setStatus("0");
        sellerDao.insertSelective(seller);
    }

    /**
     * 查询审核商家未通过
     * @param page
     * @param rows
     * @param seller
     * @return
     */
    @Override
    public PageResult search(Integer page, Integer rows, Seller seller) {
        PageHelper.startPage(page,rows);
        SellerQuery sellerQuery = new SellerQuery();
        SellerQuery.Criteria criteria = sellerQuery.createCriteria();
        if(seller!=null){
            if(seller.getStatus()!=null && !"".equals(seller.getStatus())){
                criteria.andStatusEqualTo(seller.getStatus());
            }
            if(seller.getName()!=null && !"".equals(seller.getName())){
                criteria.andNameLike("%"+seller.getName()+"%");
            }
            if(seller.getNickName()!=null && !"".equals(seller.getNickName())){
                criteria.andNickNameLike("%"+seller.getNickName()+"%");
            }
        }
        Page<Seller> sellerList = (Page<Seller>)sellerDao.selectByExample(sellerQuery);
        return new PageResult(sellerList.getTotal(),sellerList.getResult());
    }

    /**
     * 审核数据回显
     * @param id
     * @return
     */
    @Override
    public Seller findOne(String id) {
        return sellerDao.selectByPrimaryKey(id);
    }

    /**
     * 改变商家审核状态
     * @param sellerId 卖家id
     * @param status 状态码
     */
    @Override
    public void updateStatus(String sellerId, String status) {
        Seller seller = new Seller();
        seller.setSellerId(sellerId);
        seller.setStatus(status);
        sellerDao.updateByPrimaryKeySelective(seller);
    }
}
