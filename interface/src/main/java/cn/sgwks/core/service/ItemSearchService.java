package cn.sgwks.core.service;

import java.util.Map;

public interface ItemSearchService {
    //返回的数据有查询到的集合, 当前页, 每页展示多少条数据, 总记录数, 总页数
    Map<String, Object> search(Map paramMap);
}
