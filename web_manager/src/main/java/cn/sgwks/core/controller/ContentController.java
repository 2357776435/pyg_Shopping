package cn.sgwks.core.controller;

import cn.sgwks.core.pojo.ad.Content;
import cn.sgwks.core.pojo.entity.PageResult;
import cn.sgwks.core.pojo.entity.Result;
import cn.sgwks.core.service.ContentService;
import com.alibaba.dubbo.config.annotation.Reference;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/content")
public class ContentController {
    @Reference
    private ContentService contentService;

    /**
     * 查询广告管理数据
     * @return
     */
    @RequestMapping("/findAll")
    public List<Content> findAll(){
        return contentService.findAll();
    }

    /**
     * 添加广告管理数据
     * @param content
     * @return
     */
    @RequestMapping("/add")
    public Result add(@RequestBody Content content){
        try {
            contentService.add(content);
            return new Result(true,"保存成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"保存失败");
        }
    }

    /**
     * 广告管理数据回显
     * @param id
     * @return
     */
    @RequestMapping("/findOne")
    public Content findOne(Long id){
        return contentService.findOne(id);
    }

    /**
     * 广告管理数据修改
     * @param content
     * @return
     */
    @RequestMapping("/update")
    public Result update(@RequestBody Content content){
        try {
            contentService.update(content);
            return new Result(true,"修改成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"修改失败");
        }
    }

    /**
     * 广告管理数据删除
     * @param ids
     * @return
     */
    @RequestMapping("/delete")
    public Result delete(Long[] ids){
        try {
            contentService.delete(ids);
            return new Result(true,"删除成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"删除失败");
        }
    }
    /**
     *
     */
    @RequestMapping("/search")
    public PageResult search(Integer page, Integer rows, @RequestBody Content content){
        return contentService.search(page,rows,content);
    }
}
