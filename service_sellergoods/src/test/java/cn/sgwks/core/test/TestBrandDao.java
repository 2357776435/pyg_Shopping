package cn.sgwks.core.test;

import cn.sgwks.core.dao.good.BrandDao;
import cn.sgwks.core.dao.good.GoodsDao;
import cn.sgwks.core.dao.user.UserDao;
import cn.sgwks.core.pojo.good.Brand;
import cn.sgwks.core.pojo.good.BrandQuery;
import cn.sgwks.core.pojo.good.Goods;
import cn.sgwks.core.pojo.good.GoodsQuery;
import cn.sgwks.core.pojo.user.User;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

// @RunWith 配置spring测试环境
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath*:spring/applicationContext*.xml")
public class TestBrandDao {
    @Autowired
    private UserDao userDao;
    @Autowired
    private BrandDao brandDao;
    @Autowired
    private GoodsDao goodsDao;

    @Test
    public void testMycatUser(){
        List<User> userList = userDao.selectByExample(null);
        System.out.println(userList);
    }

    @Test
    public void testIsDelete(){
        Goods goods = new Goods();
        //yijia
        // select is_delete from tb_goods where seller_id='sgw';
        goods.setSellerId("sgw");
        GoodsQuery goodsQuery2 = new GoodsQuery();
//        goodsQuery2.setFields("is_delete");
        GoodsQuery.Criteria criteria2 = goodsQuery2.createCriteria();
        criteria2.andSellerIdEqualTo(goods.getSellerId());
        if (goods.getIsDelete()==null) {
            System.out.println("222");
            //select * from tb_goods where seller_id='sgw' and is_delete IS NULL;
            criteria2.andIsDeleteIsNull();
            System.out.println(goodsDao.selectByExample(goodsQuery2));
        }
        if (goods.getIsDelete()!=null) {
            //select * from tb_goods where seller_id='sgw' and is_delete IS NULL;
            criteria2.andIsDeleteIsNotNull();
            System.out.println(goodsDao.selectByExample(goodsQuery2));
        }
//        criteria2.andIsDeleteEqualTo("1");
//        criteria2.andSellerIdEqualTo(goods.getSellerId());
//        List<Goods> goodsList2 = goodsDao.selectByExample(goodsQuery2);
//        //System.out.println(goodsList2);
//        List<Goods> gd1 = new ArrayList<>();//被删除的
//        List<Goods> gd2 = new ArrayList<>();//未被删除的
//        for (Goods goods2 : goodsList2) {
//            if(goods2.getIsDelete()!=null){
//                gd1.add(goods2);
//            }
//            if(goods2.getIsDelete()==null){
//                gd2.add(goods2);
//            }
//        }
//        for (Goods goods1 : gd1) {
//            System.out.println(goods1.getIsDelete() != null && !"".equals(goods1.getIsDelete()));
//        }
//        for (Goods goods2 : gd2) {
//            System.out.println(goods2.getIsDelete() != null && !"".equals(goods2.getIsDelete()));
//        }
//        //解决商家删除了该id,就不显示
//        GoodsQuery goodsQuery2 = new GoodsQuery();
//        goodsQuery2.setFields("is_delete");
//        GoodsQuery.Criteria criteria2 = goodsQuery2.createCriteria();
//        criteria2.andIsDeleteEqualTo("1");
//        criteria2.andSellerIdEqualTo(goods.getSellerId());
//        List<Goods> goodsList2 = goodsDao.selectByExample(goodsQuery2);
//        for (Goods goods2 : goodsList2) {
//            goods.setIsDelete(goods2.getIsDelete());
//        }
//        if (goods.getIsDelete() != null && !"".equals(goods.getIsDelete())) {
//            criteria.andIsDeleteEqualTo("");
//        }
        //goods.getIsDelete() != null && !"".equals(goods.getIsDelete())
    }
    /**
     * 查询指定id数据
     */
    @Test
    public void testFindBrandById(){
        Brand brand = brandDao.selectByPrimaryKey(9L);
        System.out.println("======"+brand);
    }

    /**
     * 查询所有数据，因为selectByExample是where条件，如果条件为空就是查询所有
     */
    @Test
    public void testFindBrandAll(){
        List<Brand> brandList = brandDao.selectByExample(null);
        System.out.println("======"+brandList);
    }
    /**
     * 根据复杂的条件查询
     *  要得到的sql语句是
     *      select distinct id,name from tb_brand where id=4 and name like '%米%'
     *      and first_char like '%X%' order by id desc;
     */
    @Test
    public void testFindBrandByWhere(){
        //创健查询对象
        BrandQuery brandQuery = new BrandQuery();
        //设置查询的字段名,如果不写默认是*,查询所有,只会指定列属性段有数据
        brandQuery.setFields("id,name");
        //不设置默认是false，不去重
        brandQuery.setDistinct(true);
        //设置排序,不设置默认是升序 asc,desc 是降序,设置根据id降序排序
        brandQuery.setOrderByClause("id desc");

        //创健where查询条件对象
        BrandQuery.Criteria criteria = brandQuery.createCriteria();
        //查询id等于4的
        criteria.andIdEqualTo(4L);
        //根据名称模糊查询
        criteria.andNameLike("%米%");
        //在根据首字母字段模糊查询
        criteria.andFirstCharLike("%X%");
        List<Brand> brandList = brandDao.selectByExample(brandQuery);
        System.out.println("======"+brandList);
    }
}
