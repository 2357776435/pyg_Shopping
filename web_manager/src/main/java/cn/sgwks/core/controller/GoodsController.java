package cn.sgwks.core.controller;

import cn.sgwks.core.pojo.entity.GoodsEntity;
import cn.sgwks.core.pojo.entity.PageResult;
import cn.sgwks.core.pojo.entity.Result;
import cn.sgwks.core.pojo.good.Goods;
import cn.sgwks.core.service.GoodsService;
import com.alibaba.dubbo.config.annotation.Reference;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/goods")
public class GoodsController {
    @Reference
    private GoodsService goodsService;//商品管理服务

    /**
     * 搜索
     *
     * @param page
     * @param rows
     * @param goods
     * @return
     */
    @RequestMapping("/search")
    public PageResult search(Integer page, Integer rows, @RequestBody Goods goods) {
        //获取当前登录用户的用户名
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        goods.setSellerId(username);
        return goodsService.search(page, rows, goods);
    }

    /**
     * 根据指定的id回显数据
     */
    @RequestMapping("/findOne")
    public GoodsEntity findOne(Long id) {
        return goodsService.findOne(id);
    }

    /**
     * 批量删除
     *
     * @param ids
     * @return
     */
    @RequestMapping("/delete")
    public Result delete(Long[] ids) {
        try {
            if (ids != null) {
                for (Long id : ids) {
                    //1. 根据商品id到数据库中删除
                    goodsService.delete(id);
                }
            }
            return new Result(true, "删除成功!");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "删除失败!");
        }
    }

    /**
     * 修改商品状态
     *
     * @param ids    需要修改的商品id数组
     * @param status 状态码, 由页面传入
     * @return
     */
    @RequestMapping("/updateStatus")
    public Result updateStatus(Long[] ids, String status) {
        try {
            if (ids != null) {
                for (Long id : ids) {
                    //1. 到数据库中根据商品id改变商品的上架状态
                    goodsService.updateStatus(id, status);
                }
            }
            return new Result(true, "状态修改成功!");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "状态修改失败!");
        }
    }
}
