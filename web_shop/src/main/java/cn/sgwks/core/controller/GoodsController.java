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

@RestController
@RequestMapping("/goods")
public class GoodsController {
    @Reference
    private GoodsService goodsService;

    @RequestMapping("/add")
    public Result add(@RequestBody GoodsEntity goodsEntity) {
        try {
            //获取登录用户名
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            //设置这个商品添加的用户名, 也就是卖家id
            goodsEntity.getGoods().setSellerId(username);
            goodsService.add(goodsEntity);
            return new Result(true, "添加成功!");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "添加失败!");
        }
    }

    /**
     * 搜索
     * @param page
     * @param rows
     * @param goods
     * @return
     */
    @RequestMapping("/search")
    public PageResult search(Integer page, Integer rows, @RequestBody Goods goods){
        //获取当前登录用户的用户名
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        goods.setSellerId(username);
        return goodsService.search(page,rows,goods);
    }
    /**
     * 根据指定的id回显数据
     */
    @RequestMapping("/findOne")
    public GoodsEntity findOne(Long id){
        return goodsService.findOne(id);
    }
    /**
     * 修改数据
     * @RequestBody 是吧json数据转换成pojo对象
     */
    @RequestMapping("/update")
    public Result update(@RequestBody GoodsEntity goodsEntity){
        try {
            //获取当前登录用户的用户名
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            //获取这个商品的所有者
            String sellerId = goodsEntity.getGoods().getSellerId();
            //判断该会话的用户是否是该商品的用户
            if(username.equals(sellerId)){
                goodsService.update(goodsEntity);
                return new Result(true,"修改成功!");
            }else {
                return new Result(false,"您没有权限修改此商品!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"修改失败!");
        }
    }

    /**
     * 提交审核,根据用户修改商品后需要重新提交审核，重新修改solr数据
     * 先删除改用户商品id的solr数据，到消息中间件判断重新添加
     * @param ids
     * @return
     */
    @RequestMapping("/insertUpdateStatus")
    public Result insertUpdateStatus(Long[] ids){
        try {
            if(ids!=null){
                for (Long id : ids) {
                    //1. 根据商品id删除数据库中商品数据
                    goodsService.insertUpdateStatus(id);
                }
                return new Result(true,"提交审核成功!");
            }
            return new Result(false,"提交审核失败!");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"审核失败!");
        }
    }
    /**
     * 批量删除+下架
     * @param ids
     * @return
     */
    @RequestMapping("/delete")
    public Result delete(Long[] ids){
        try {
            if(ids!=null){
                for (Long id : ids) {
                    //1. 根据商品id删除数据库中商品数据
                    goodsService.delete(id);
                }
            }
            return new Result(true,"删除成功!");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"删除失败!");
        }
    }
}
