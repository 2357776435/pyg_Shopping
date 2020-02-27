package cn.sgwks.core.controller;

import cn.sgwks.core.pojo.ad.ContentCategory;
import cn.sgwks.core.pojo.entity.PageResult;
import cn.sgwks.core.pojo.entity.Result;
import cn.sgwks.core.service.ContentCategoryService;
import com.alibaba.dubbo.config.annotation.Reference;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/contentCategory")
public class ContentCategoryController {
    @Reference
    private ContentCategoryService contentCategoryService;

    /**
     * 查询广告分类管理所有数据
     * @return
     */
    @RequestMapping("/findAll")
    public List<ContentCategory> findAll(){
        return contentCategoryService.findAll();
    }

    /**
     * 添加广告分类管理数据
     * @param contentCategory
     * @return
     */
    @RequestMapping("/add")
    public Result add(@RequestBody ContentCategory contentCategory){
        try {
            contentCategoryService.add(contentCategory);
            return new Result(true,"保存成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"保存失败");
        }
    }

    /**
     * 广告分类管理数据回显
     * @param id
     * @return
     */
    @RequestMapping("/findOne")
    public ContentCategory findOne(Long id){
        return contentCategoryService.findOne(id);
    }

    /**
     * 修改广告分类管理数据
     * @param contentCategory
     * @return
     */
    @RequestMapping("/update")
    public Result update(@RequestBody ContentCategory contentCategory){
        try {
            contentCategoryService.update(contentCategory);
            return new Result(true,"修改成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"修改失败");
        }
    }

    /**
     * 广告分类管理批量删除
     * @param ids
     * @return
     */
    @RequestMapping("/delete")
    public Result delete(Long[] ids){
        try {
            contentCategoryService.delete(ids);
            return new Result(true,"删除成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"删除失败");
        }
    }

    /**
     * 广告分类管理分页查询搜索
     * @param page
     * @param rows
     * @param contentCategory
     * @return
     */
    @RequestMapping("/search")
    public PageResult search(Integer page,Integer rows,@RequestBody ContentCategory contentCategory){
        return contentCategoryService.search(page,rows,contentCategory);
    }
}
