package cn.sgwks.core.controller;

import cn.sgwks.core.pojo.entity.PageResult;
import cn.sgwks.core.pojo.entity.Result;
import cn.sgwks.core.pojo.entity.SpecEntity;
import cn.sgwks.core.pojo.specification.Specification;
import cn.sgwks.core.service.SpecificationService;
import com.alibaba.dubbo.config.annotation.Reference;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 规格管理
 */
@RestController
@RequestMapping("/specification")
public class SpecController {
    @Reference
    private SpecificationService specificationService;

    /**
     * 规格高级分页查询
     * @param page
     * @param rows
     * @param spec
     * @return
     */
    @RequestMapping("/search")
    public PageResult search(Integer page, Integer rows, @RequestBody Specification spec){
        return specificationService.search(page,rows,spec);
    }

    /**
     * 规格添加
     * @param specEntity
     * @return
     */
    @RequestMapping("/add")
    public Result add(@RequestBody SpecEntity specEntity){
        try {
            specificationService.add(specEntity);
            return new Result(true,"添加成功!");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"添加失败!");
        }
    }

    /**
     * 规格数据回显
     * @param id 规格id
     * @return
     */
    @RequestMapping("/findOne")
    public SpecEntity findOne(Long id){
        SpecEntity specEntity = specificationService.findOne(id);
        return specEntity;
    }

    /**
     * 更新规格数据
     * @param specEntity
     * @return
     */
    @RequestMapping("/update")
    public Result update(@RequestBody SpecEntity specEntity){
        try {
            specificationService.update(specEntity);
            return new Result(true,"修改成功!");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"修改失败!");
        }
    }

    /**
     * 批量删除
     * @param ids
     * @return
     */
    @RequestMapping("/delete")
    public Result delete(Long[] ids){
        try {
            specificationService.delete(ids);
            return new Result(true,"删除成功!");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"删除失败!");
        }
    }

    /**
     * 获取模板(规格)下拉数据
     * @return
     */
    @RequestMapping("/selectOptionList")
    public List<Map> selectOptionList(){
       return specificationService.selectOptionList();
    }
}
