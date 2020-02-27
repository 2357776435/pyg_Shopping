package cn.sgwks.core.dao.good;

import cn.sgwks.core.pojo.good.Brand;
import cn.sgwks.core.pojo.good.BrandQuery;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

public interface BrandDao {
    // 按照条件计算记录数
    int countByExample(BrandQuery example);
    // 按照条件删除
    int deleteByExample(BrandQuery example);
    // 按照主键删除
    int deleteByPrimaryKey(Long id);

    int insert(Brand record);

    int insertSelective(Brand record);
    // 根据查询条件查询
    List<Brand> selectByExample(BrandQuery example);
    // 根据主键查询
    Brand selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") Brand record, @Param("example") BrandQuery example);

    int updateByExample(@Param("record") Brand record, @Param("example") BrandQuery example);

    int updateByPrimaryKeySelective(Brand record);

    int updateByPrimaryKey(Brand record);
    //获取模板下拉数据
    List<Map> selectOptionList();
}