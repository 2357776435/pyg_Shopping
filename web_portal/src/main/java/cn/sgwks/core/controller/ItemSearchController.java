package cn.sgwks.core.controller;

import cn.sgwks.core.service.ItemSearchService;
import com.alibaba.dubbo.config.annotation.Reference;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/itemSearch")
public class ItemSearchController {
    @Reference
    private ItemSearchService itemSearchService;
    /**
     * 返回的数据有查询到的集合, 当前页, 每页展示多少条数据, 总记录数, 总页数
     */
    @RequestMapping("/search")
    public Map<String,Object> search(@RequestBody Map paramMap){
        return itemSearchService.search(paramMap);
    }
}
