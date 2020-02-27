package cn.sgwks.core.service;

import cn.sgwks.core.pojo.entity.PageResult;
import cn.sgwks.core.pojo.template.TypeTemplate;

import java.util.List;
import java.util.Map;

public interface TemplateService {
    //模板高级分页查询
    PageResult search(Integer page, Integer rows, TypeTemplate typeTemplate);
    //修改模板数据
    void update(TypeTemplate typeTemplate);
    //添加模板数据
    void add(TypeTemplate typeTemplate);
    //根据id获取实体数据作为修改数据的回显
    TypeTemplate findOne(Long id);
    //批量删除
    void delete(Long[] ids);
    //根据模板id, 查询规格集合和对应的规格选项集合数据
    List<Map> findBySpecList(Long id);
}
