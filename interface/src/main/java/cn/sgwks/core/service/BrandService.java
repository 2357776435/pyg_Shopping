package cn.sgwks.core.service;

import cn.sgwks.core.pojo.entity.PageResult;
import cn.sgwks.core.pojo.good.Brand;
import com.github.pagehelper.PageInfo;

import java.util.List;
import java.util.Map;

public interface BrandService {
    List<Brand> findAll();
    //使用自己定义的实体类对象
    PageResult findByPage(Integer page, Integer rows);
    //使用默认的PageInfo对象
    PageInfo<Brand> findByPage2(Integer page, Integer rows);
    //添加品牌数据
    void save(Brand brand);
    //查找指定id的品牌数据，得到修改的回显数据
    Brand findById(Long id);
    //修改品牌数据
    void update(Brand brand);
    //删除多个品牌数据，批量删除
    void delete(Long[] ids);
    //查询+分页
    PageResult search(Integer page, Integer rows,Brand brand);
    //获取模板下拉数据
    List<Map> selectOptionList();
}
