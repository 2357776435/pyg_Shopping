package cn.sgwks.core.service;

import cn.sgwks.core.pojo.ad.Content;
import cn.sgwks.core.pojo.entity.PageResult;

import java.util.List;

public interface ContentService {
    List<Content> findAll();

    void add(Content content);

    Content findOne(Long id);

    void update(Content content);

    void delete(Long[] ids);

    PageResult search(Integer page, Integer rows, Content content);

    List<Content> findByCategoryId(Long categoryId);

    List<Content> findByCategoryIdFromRedis(Long categoryId);
}
