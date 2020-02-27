package cn.sgwks.core.service;

import cn.sgwks.core.dao.good.GoodsDao;
import cn.sgwks.core.dao.good.GoodsDescDao;
import cn.sgwks.core.dao.item.ItemCatDao;
import cn.sgwks.core.dao.item.ItemDao;
import cn.sgwks.core.pojo.good.Goods;
import cn.sgwks.core.pojo.good.GoodsDesc;
import cn.sgwks.core.pojo.item.Item;
import cn.sgwks.core.pojo.item.ItemCat;
import cn.sgwks.core.pojo.item.ItemQuery;
import com.alibaba.dubbo.config.annotation.Service;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.ServletContextAware;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import javax.servlet.ServletContext;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CmsServiceImpl implements CmsService, ServletContextAware {
    @Autowired
    private GoodsDao goodsDao;//商品数据
    @Autowired
    private GoodsDescDao goodsDescDao;//商品详情数据
    @Autowired
    private ItemDao itemDao;//库存数据
    @Autowired
    private ItemCatDao itemCatDao;//库存分类数据
    //获取绝对路径
    private ServletContext servletContext;
    @Autowired
    private FreeMarkerConfigurer freemarkerConfig;//创建模板引擎

    @Override
    public void createStaticPage(Long goodsId, Map<String, Object> rootMap) throws Exception {
        //1. 获取模板的初始化对象
        Configuration configuration = freemarkerConfig.getConfiguration();
        Template template = configuration.getTemplate("item.ftl");
        //3. 创建输出流, 指定生成静态页面的位置和名称
        String path = goodsId + ".html";
        String realPath = getRealPath(path);
        Writer out = new OutputStreamWriter(new FileOutputStream(new File(realPath)), "utf-8");
        //4. 生成
        template.process(rootMap, out);
        //5.关闭流
        out.close();
    }

    /**
     * 删除时发生
     * @param goodsId
     * @param rootMap
     * @throws Exception
     */
    @Override
    public void createStaticDelPage(Long goodsId, Map<String, Object> rootMap) throws Exception {
        //1. 获取模板的初始化对象
        Configuration configuration = freemarkerConfig.getConfiguration();
        Template template = configuration.getTemplate("itemDel.ftl");
        //3. 创建输出流, 指定生成静态页面的位置和名称
        String path = goodsId + ".html";
        String realPath = getRealPath(path);
        Writer out = new OutputStreamWriter(new FileOutputStream(new File(realPath)), "utf-8");
        //4. 生成
        template.process(rootMap, out);
        //5.关闭流
        out.close();
    }

    /**
     * 根据商品id获取商品集合数据
     *
     * @param goodsId
     * @return
     */
    @Override
    public Map<String, Object> findGoodsData(Long goodsId) {
        HashMap<String, Object> resultMap = new HashMap<>();
        //1. 获取商品数据
        Goods goods = goodsDao.selectByPrimaryKey(goodsId);
        //2. 获取商品详情数据
        GoodsDesc goodsDesc = goodsDescDao.selectByPrimaryKey(goodsId);
        //3. 获取库存集合数据,因为商品id是库存集合的外键
        ItemQuery itemQuery = new ItemQuery();
        ItemQuery.Criteria criteria = itemQuery.createCriteria();
        criteria.andGoodsIdEqualTo(goodsId);
        List<Item> itemList = itemDao.selectByExample(itemQuery);
        //4. 获取商品对应的分类数据
        if (goods != null) {
            ItemCat itemCat1 = itemCatDao.selectByPrimaryKey(goods.getCategory1Id());
            ItemCat itemCat2 = itemCatDao.selectByPrimaryKey(goods.getCategory2Id());
            ItemCat itemCat3 = itemCatDao.selectByPrimaryKey(goods.getCategory3Id());
            resultMap.put("itemCat1", itemCat1.getName());
            resultMap.put("itemCat2", itemCat2.getName());
            resultMap.put("itemCat3", itemCat3.getName());
        }
        //5. 将商品所有数据封装成Map返回
        resultMap.put("goods", goods);
        resultMap.put("goodsDesc", goodsDesc);
        resultMap.put("itemList", itemList);
        return resultMap;
    }

    /**
     * 将相对路径转换成绝对路径
     *
     * @param path 相对路径
     * @return
     */
    private String getRealPath(String path) {
        String realPath = servletContext.getRealPath(path);
        return realPath;
    }

    /**
     * 由于当前项目是service项目, 没有配置springMvc所以没有初始化servletContext对象,
     * 但是我们这个项目配置了spring, spring中有servletContextAware接口, 这个接口中用servletContext对象
     * 这个是spring初始化好的, 所以我们实现servletContextAware接口, 目的是使用里面的servletContext对象给
     * 我们当前类上的servletContext对象赋值
     *
     * @param servletContext
     */
    @Override
    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }
}
