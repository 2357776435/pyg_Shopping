package cn.sgwks.core.controller;

import cn.sgwks.core.pojo.entity.PageResult;
import cn.sgwks.core.pojo.entity.Result;
import cn.sgwks.core.pojo.good.Brand;
import cn.sgwks.core.service.BrandService;
import com.alibaba.dubbo.config.annotation.Reference;
import com.github.pagehelper.PageInfo;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 品牌管理
 *  @RestController 二合一 :@Controller 和 @ResponseBody的整合
 */
@RestController
@RequestMapping("/brand")
public class BrandController {
    @Reference
    private BrandService brandService;

    /**
     * 查询所有数据
     * @return
     */
    @RequestMapping("/findAll")
    public List<Brand> findAll(){
        return brandService.findAll();
    }

    /**
     * 分页查询品牌
     * @param page 当前页
     * @param rows 每页展示数据条数
     * @return
     */
    @RequestMapping("/findByPage")
    public PageResult findByPage(Integer page,Integer rows){
        return brandService.findByPage(page, rows);
    }
    /**
     * 分页查询品牌
     * @param page 当前页
     * @param rows 每页展示数据条数
     * @return
     */
    @RequestMapping("/findByPage2")
        public PageInfo<Brand> findByPage2(Integer page, Integer rows){
        PageInfo<Brand> pageInfo = brandService.findByPage2(page, rows);
        return pageInfo;
    }

    /**
     * 添加品牌数据
     * RequestBody 把pojo对象转换成json对象
     * @return
     */
    @RequestMapping("/save")
    public Result save(@RequestBody Brand brand){
        try {
            brandService.save(brand);
            return new Result(true,"添加成功!");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(true,"添加失败!");
        }
    }

    /**
     * 根据ID获取实体，主键查询对象
     * @param id
     * @return
     */
    @RequestMapping("/findById")
    public Brand findById(Long id){
        return brandService.findById(id);
    }

    /**
     * 修改品牌数据
     * @param brand
     * @return
     */
    @RequestMapping("/update")
    public Result update(@RequestBody Brand brand){
        try {
            brandService.update(brand);
            return new Result(true, "修改成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "修改失败");
        }
    }

    /**
     * 删除勾选复选框的品牌数据，批量删除
     * @param ids 属猪id，多个主键
     * @return
     */
    @RequestMapping("/delete")
    public Result delete(Long[] ids){
        try {
            brandService.delete(ids);
            return new Result(true, "删除成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "删除失败");
        }
    }

    /**
     * 查询+分页
     * @param brand
     * @param page
     * @param rows
     * @return
     */
    @RequestMapping("/search")
    public PageResult search(Integer page,Integer rows,@RequestBody Brand brand){
        return brandService.search(page,rows,brand);
    }

    /**
     * 获取模板(品牌)下拉数据
     * @return
     */
    @RequestMapping("/selectOptionList")
    public List<Map> selectOptionList(){
        return brandService.selectOptionList();
    }
}
