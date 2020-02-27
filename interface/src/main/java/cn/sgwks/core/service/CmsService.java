package cn.sgwks.core.service;

import java.util.Map;

public interface CmsService {
    void createStaticPage(Long goodsId, Map<String, Object> rootMap) throws Exception;
    void createStaticDelPage(Long goodsId, Map<String, Object> rootMap) throws Exception;
    Map<String, Object> findGoodsData(Long goodsId);
}
