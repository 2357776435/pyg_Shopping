package cn.sgwks.core.service;

import cn.sgwks.core.pojo.entity.PageResult;
import cn.sgwks.core.pojo.entity.SpecEntity;
import cn.sgwks.core.pojo.specification.Specification;

import java.util.List;
import java.util.Map;

public interface SpecificationService {
    //规格高级分页查询
    PageResult search(Integer page, Integer rows, Specification spec);
    //规格添加
    void add(SpecEntity specEntity);
    //规格数据回显
    SpecEntity findOne(Long id);
    //更新规格数据
    void update(SpecEntity specEntity);
    //批量删除
    void delete(Long[] ids);
    //获取模板(规格)下拉数据
    List<Map> selectOptionList();
}
