package cn.sgwks.core.controller;

import cn.sgwks.core.pojo.template.TypeTemplate;
import cn.sgwks.core.service.TemplateService;
import com.alibaba.dubbo.config.annotation.Reference;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/typeTemplate")
public class TemplateController {
    @Reference
    private TemplateService templateService;

    @RequestMapping("/findOne")
    public TypeTemplate findOne(Long id) {
        return templateService.findOne(id);
    }
    /**
     * 根据模板id, 查询规格集合和对应的规格选项集合数据
     * 数据例如: [{"id":27,"text":"网络","options":
     *                              [{"id":1, option_name:3G},{"id":2, option_name:4G}]},
     *          {"id":32,"text":"机身内存","options":
     *                              [{"id":3, option_name:128G},{"id":4, option_name:256G}]}
     *          ]
     * @param id
     */
    @RequestMapping("/findBySpecList")
    public List<Map> findBySpecList(Long id) {
       return templateService.findBySpecList(id);
    }
}
