package cn.sgwks.core.service;

import cn.sgwks.core.pojo.ad.ContentCategory;
import cn.sgwks.core.pojo.entity.PageResult;

import java.util.List;

public interface ContentCategoryService {
    List<ContentCategory> findAll();

    void add(ContentCategory contentCategory);

    ContentCategory findOne(Long id);

    void update(ContentCategory contentCategory);

    void delete(Long[] ids);

    PageResult search(Integer page, Integer rows, ContentCategory contentCategory);
}
