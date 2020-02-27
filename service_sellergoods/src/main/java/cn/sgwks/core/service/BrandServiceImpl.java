package cn.sgwks.core.service;

import cn.sgwks.core.dao.good.BrandDao;
import cn.sgwks.core.pojo.entity.PageResult;
import cn.sgwks.core.pojo.good.Brand;
import cn.sgwks.core.pojo.good.BrandQuery;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

@Service
public class BrandServiceImpl implements BrandService {
    @Autowired
    private BrandDao brandDao;

    @Override
    public List<Brand> findAll() {
        return brandDao.selectByExample(null);
    }

    @Override
    public PageResult findByPage(Integer page, Integer rows) {
        //利用分页助手实现分页，第一个参数:当前页，第二个参数：每页展示的数据条数
        PageHelper.startPage(page, rows);
        Page<Brand> brandList = (Page<Brand>) brandDao.selectByExample(null);
        //getTotal()总条数,.getResult()数据集合
        return new PageResult(brandList.getTotal(), brandList.getResult());
    }

    @Override
    public PageInfo<Brand> findByPage2(Integer page, Integer rows) {
        //为分页助手初始化参数 -->作用是把2个参数绑定到线程上执行
        PageHelper.startPage(page, rows);
        //查询全部
        List<Brand> brandList = brandDao.selectByExample(null);
        //创建PageInfo对象 -- 相当于自定义PageBean :需要通过构造传入查询的集合对象 , 页面最多显示3个页码
        PageInfo<Brand> pageInfo = new PageInfo<>(brandList);
        return pageInfo;
    }

    /**
     * insertSelective 插入数据，插入的时候不会判断传入对象中的属性是否为空，如果为空，不参与拼接sql语句，sql语句变短，执行效率会提高
     * insert 插入数据，插入的时候不会判断传入对象中的属性是否为空，所有字段都参与拼接sql语句执行数据
     *
     * @param brand
     */
    @Override
    public void save(Brand brand) {
        brandDao.insertSelective(brand);
    }

    /**
     * 获取实体，数据回显
     *
     * @param id
     * @return
     */
    @Override
    public Brand findById(Long id) {
        return brandDao.selectByPrimaryKey(id);
    }

    /**
     * 修改品牌数据
     * updateByPrimaryKey():根据主键作为条件修改，不带Selective说明传入对象属性为null也会拼接sql语句，修改完如果有属性为null则数据库中的值也会被修改为null
     * updateByPrimaryKeySelective():根据主键作为条件修改，这个方法带Selective说明传入的对象会进行判断，如果存入的对象属性为null则不会拼接到sql语句中
     * updateByExample():根据非主键条件修改,第一个参数传入需要修改的对象，第二个参数传入修改的条件对象，不带Selective说明传入的对象属性不管是否为null都会参与修改
     * updateByExampleSelective():根据非主键条件修改,第一个参数传入需要修改的对象，第二个参数传入修改的条件对象，带Selective说明传入的对象属性为null不会参与修改
     *
     * @param brand
     */
    @Override
    public void update(Brand brand) {
        brandDao.updateByPrimaryKeySelective(brand);
    }

    /**
     * 删除多个品牌数据，批量删除
     *
     * @param ids
     */
    @Override
    public void delete(Long[] ids) {
        for (Long id : ids) {
            brandDao.deleteByPrimaryKey(id);
        }
    }

    /**
     * 查询+分页
     *
     * @param brand
     * @param page
     * @param rows
     * @return
     */
    @Override
    public PageResult search(Integer page, Integer rows,Brand brand) {
        //利用分页助手实现分页，第一个参数:当前页，第二个参数：每页展示的数据条数
        PageHelper.startPage(page, rows);
        BrandQuery brandQuery = new BrandQuery();
        BrandQuery.Criteria criteria = brandQuery.createCriteria();
        if (brand != null) {
            if (brand.getName() != null && !"".equals(brand.getName())) {
                criteria.andNameLike("%" + brand.getName() + "%");
            }
            if (brand.getFirstChar() != null && !"".equals(brand.getFirstChar())) {
                criteria.andFirstCharLike("%" + brand.getFirstChar() + "%");
            }
        }
        Page<Brand> brandList = (Page<Brand>) brandDao.selectByExample(brandQuery);
        return new PageResult(brandList.getTotal(),brandList.getResult());
    }

    /**
     * 获取模板下拉数据
     * @return
     */
    @Override
    public List<Map> selectOptionList() {
        return brandDao.selectOptionList();
    }
}
