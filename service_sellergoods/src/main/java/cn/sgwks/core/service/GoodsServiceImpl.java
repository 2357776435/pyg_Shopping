package cn.sgwks.core.service;

import cn.sgwks.core.dao.good.BrandDao;
import cn.sgwks.core.dao.good.GoodsDao;
import cn.sgwks.core.dao.good.GoodsDescDao;
import cn.sgwks.core.dao.item.ItemCatDao;
import cn.sgwks.core.dao.item.ItemDao;
import cn.sgwks.core.dao.seller.SellerDao;
import cn.sgwks.core.pojo.entity.GoodsEntity;
import cn.sgwks.core.pojo.entity.PageResult;
import cn.sgwks.core.pojo.good.Brand;
import cn.sgwks.core.pojo.good.Goods;
import cn.sgwks.core.pojo.good.GoodsDesc;
import cn.sgwks.core.pojo.good.GoodsQuery;
import cn.sgwks.core.pojo.item.Item;
import cn.sgwks.core.pojo.item.ItemCat;
import cn.sgwks.core.pojo.item.ItemQuery;
import cn.sgwks.core.pojo.seller.Seller;
import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.apache.activemq.command.ActiveMQQueue;
import org.apache.activemq.command.ActiveMQTopic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.transaction.annotation.Transactional;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import javax.jms.TextMessage;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class GoodsServiceImpl implements GoodsService {
    @Autowired
    private GoodsDao goodsDao;//商家表
    @Autowired
    private GoodsDescDao goodsDescDao;//商家详情表
    @Autowired
    private ItemDao itemDao;//库存数据
    @Autowired
    private ItemCatDao itemCatDao;//分类表
    @Autowired
    private BrandDao brandDao;//品牌表
    @Autowired
    private SellerDao sellerDao;//卖家表
    @Autowired
    private JmsTemplate jmsTemplate;//创建模板引擎对象
    //为商品上架使用
    @Autowired
    private ActiveMQTopic topicPageAndSolrDestination;
    //为商品下架使用
    @Autowired
    private ActiveMQQueue queueSolrDeleteDestination;
    @Autowired
    private SolrManagerService solrManagerService;

    @Override
    public void add(GoodsEntity goodsEntity) {
        /**
         * 1. 保存商品对象
         */
        //刚添加的商品状态默认为0 未审核
        goodsEntity.getGoods().setAuditStatus("0");
        goodsDao.insertSelective(goodsEntity.getGoods());
        /**
         * 2. 保存商品详情对象
         */
        //商品主键作为商品详情的主键
        goodsEntity.getGoodsDesc().setGoodsId(goodsEntity.getGoods().getId());
        goodsDescDao.insertSelective(goodsEntity.getGoodsDesc());
        /**
         * 3. 保存库存集合对象
         */
        insertItem(goodsEntity);
    }

    @Override
    public PageResult search(Integer page, Integer rows, Goods goods) {
        PageHelper.startPage(page, rows);
        GoodsQuery goodsQuery = new GoodsQuery();
        GoodsQuery.Criteria criteria = goodsQuery.createCriteria();
        //判断goods是否为空，并且IsDelete不能为1
        if (goods != null) {
            if (goods.getIsDelete() == null) {
                //select * from tb_goods where seller_id='sgw' and is_delete IS NULL;
                //显示没被删除的数据，idDelete=0显示
                criteria.andIsDeleteIsNull();
            }
            //判断输入名称是否为空或空串,并加入where条件查询
            if (goods.getGoodsName() != null && !"".equals(goods.getGoodsName())) {
                criteria.andGoodsNameLike("%" + goods.getGoodsName() + "%");
            }
            //判断输入状态是否为空或空串，并加入where条件模糊查询
            if (goods.getAuditStatus() != null && !"".equals(goods.getAuditStatus())) {
                criteria.andAuditStatusEqualTo(goods.getAuditStatus());
            }
            //判断是否是该用户访问该信息,并且管理员也不能查看,只查询是该用户的信息，其他用户不予查看
            if (goods.getSellerId() != null && !"".equals(goods.getSellerId())
                    && !"admin".equals(goods.getSellerId()) && !"wc".equals(goods.getSellerId())) {
                criteria.andSellerIdEqualTo(goods.getSellerId());
            }
        }
        Page<Goods> goodsList = (Page<Goods>) goodsDao.selectByExample(goodsQuery);
        return new PageResult(goodsList.getTotal(), goodsList.getResult());
    }

    /**
     * 修改数据
     *
     * @param goodsEntity
     */
    @Override
    public void update(GoodsEntity goodsEntity) {
        goodsEntity.getGoods().setAuditStatus("0");
        //1. 修改商品对象
        goodsDao.updateByPrimaryKeySelective(goodsEntity.getGoods());
        //2. 修改商品详情对象
        goodsDescDao.updateByPrimaryKeySelective(goodsEntity.getGoodsDesc());
        //3. 根据商品id删除对应的库存集合数据,先删除原有的数据，再次插入数据即可
        ItemQuery itemQuery = new ItemQuery();
        ItemQuery.Criteria criteria = itemQuery.createCriteria();
        //商品id,根据id获取到分类对象，删除原有的分类对象后再次添加新的分类对象
        criteria.andGoodsIdEqualTo(goodsEntity.getGoods().getId());
        itemDao.deleteByExample(itemQuery);
        //4. 添加库存集合数据
        insertItem(goodsEntity);
    }

    /**
     * 根据指定的id回显数据
     *
     * @return
     */
    @Override
    public GoodsEntity findOne(Long id) {
        //1. 根据商品id查询商品对象
        Goods goods = goodsDao.selectByPrimaryKey(id);
        //2. 根据商品id查询商品详情对象
        GoodsDesc goodsDesc = goodsDescDao.selectByPrimaryKey(id);
        //3. 根据商品id查询库存集合对象,因为商品id是库存表的外键所有需要where条件
        ItemQuery itemQuery = new ItemQuery();
        ItemQuery.Criteria criteria = itemQuery.createCriteria();
        criteria.andGoodsIdEqualTo(id);
        List<Item> itemList = itemDao.selectByExample(itemQuery);

        //4. 将以上查询到的对象封装到GoodsEntity中返回
        GoodsEntity goodsEntity = new GoodsEntity();
        goodsEntity.setGoods(goods);
        goodsEntity.setGoodsDesc(goodsDesc);
        goodsEntity.setItemList(itemList);
        return goodsEntity;
    }

    /**
     * 提交审核主要是是修改商品状态，默认是0修改为
     * @param id
     */
    @Override
    public void insertUpdateStatus(Long id) {
        //只要提交审核不管solr里面是否存在改商品id的数据，先删除，以便审核判断使用
        solrManagerService.deleteItemFromSolr(id);
        Goods goods = new Goods();
        goods.setAuditStatus("4");
        GoodsQuery goodsQuery = new GoodsQuery();
        GoodsQuery.Criteria criteria = goodsQuery.createCriteria();
        criteria.andIdEqualTo(id);
        goodsDao.updateByExampleSelective(goods,goodsQuery);
    }
    /**
     * 批量删除+solr
     *
     * @param id 商品id
     */
    @Override
    public void delete(final Long id) {
        /**
         * 1. 到数据库中对商品进行逻辑删除
         */
        Goods goods = new Goods();
        goods.setId(id);
        //设置该属性，默认是NULL,当删除时修改了该数据，商家看不到，但数据中还存在
        goods.setIsDelete("1");
        //修改属性，但数据库中还有该数据
        goodsDao.updateByPrimaryKeySelective(goods);
        /**
         * 2 将商品id作为消息发送给消息服务器
         */
//        jmsTemplate.send(queueSolrDeleteDestination, new MessageCreator() {
//            @Override
//            public Message createMessage(Session session) throws JMSException {
//                TextMessage textMessage = session.createTextMessage(String.valueOf(id));
//                return textMessage;
//            }
//        });
        jmsTemplate.send(topicPageAndSolrDestination, new MessageCreator() {
            @Override
            public Message createMessage(Session session) throws JMSException {
                TextMessage textMessage = session.createTextMessage(String.valueOf(id));
                return textMessage;
            }
        });
    }

    /**
     * 修改商品状态
     *
     * @param id     需要修改的商品id并加入了solr
     * @param status 状态码, 由页面传入
     */
    @Override
    public void updateStatus(final Long id, String status) {
        /**
         * 根据商品id到数据库中将商品的上架状态改变
         */

        //1. 根据商品id修改商品对象状态码
        Goods goods = new Goods();
        goods.setId(id);
        goods.setAuditStatus(status);
        goodsDao.updateByPrimaryKeySelective(goods);
        //2. 根据商品id修改库存集合对象状态码
        Item item = new Item();
        item.setStatus(status);
        ItemQuery itemQuery = new ItemQuery();
        ItemQuery.Criteria criteria = itemQuery.createCriteria();
        criteria.andGoodsIdEqualTo(id);
        itemDao.updateByExampleSelective(item, itemQuery);
        /**
         * 将商品id作为消息发送给消息服务器,判断商品状态是否审核通过,审核通过就创建模板引擎静态文件
         */
        if ("1".equals(status)) {
            jmsTemplate.send(topicPageAndSolrDestination, new MessageCreator() {
                @Override
                public Message createMessage(Session session) throws JMSException {
                    TextMessage textMessage = session.createTextMessage(String.valueOf(id));
                    return textMessage;
                }
            });
        }
    }

    /**
     * 使用goodsEntity实体类中的数据初始化, item库存对象中的属性值
     *
     * @param goodsEntity
     * @param item
     * @return
     */
    private Item setItemValue(GoodsEntity goodsEntity, Item item) {
        //商品id
        item.setGoodsId(goodsEntity.getGoods().getId());

        //商家ID
        item.setSellerId(goodsEntity.getGoods().getSellerId());

        //创建时间
        item.setCreateTime(new Date());
        //更新时间
        item.setUpdateTime(new Date());
        //商品状态, 默认为0-未审核,1-正常，2-下架，3-删除
        item.setStatus("0");
        //分类id,所属类目，叶子类目. 库存使用商品的第三级分类最为库存分类
        Long category3Id = goodsEntity.getGoods().getCategory3Id();
        item.setCategoryid(category3Id);
        //分类名称
        ItemCat itemCat = itemCatDao.selectByPrimaryKey(category3Id);
        item.setCategory(itemCat.getName());
        //品牌名称
        Brand brand = brandDao.selectByPrimaryKey(goodsEntity.getGoods().getBrandId());
        item.setBrand(brand.getName());
        //卖家名称
        Seller seller = sellerDao.selectByPrimaryKey(goodsEntity.getGoods().getSellerId());
        item.setSeller(seller.getName());
        //示例图片
        String itemImages = goodsEntity.getGoodsDesc().getItemImages();
        //因为itemImages是,所有需要将json格式字符串解析成Java中的List集合对象
        List<Map> maps = JSON.parseArray(itemImages, Map.class);
        if (maps != null && maps.size() > 0) {
            String url = String.valueOf(maps.get(0).get("url"));
            item.setImage(url);
        }
        return item;
    }

    /**
     * 保存库存数据
     *
     * @param goodsEntity
     */
    private void insertItem(GoodsEntity goodsEntity) {
        //isEnableSpec:是否启用规格,ng-true-value="1"就是启动, ng-false-value="0"就是不启动
        if ("1".equals(goodsEntity.getGoods().getIsEnableSpec())) {
            //勾选规格复选框, 有库存数据
            if (goodsEntity.getItemList() != null) {
                for (Item item : goodsEntity.getItemList()) {
                    //商品名称
                    String title = goodsEntity.getGoods().getGoodsName();
                    //从库存对象中获取前端传入的json格式规格字符串, 例如: {"机身内存":"16G","网络":"联通3G"}
                    String specJsonStr = item.getSpec();
                    Map specMap = JSON.parseObject(specJsonStr, Map.class);
                    //规格名称集合，获取map中的value集合,{"机身内存":"16G","网络":"联通3G"}中的 16G,联通3G
                    Collection<String> values = specMap.values();
                    for (String value : values) {
                        title += " " + value;
                    }
                    //库存标题, 由商品名 + 规格组成具体的库存标题, 供消费者搜索使用, 可以搜索的更精确
                    item.setTitle(title);
                    //设置库存对象的属性值
                    setItemValue(goodsEntity, item);
                    itemDao.insertSelective(item);
                }
            }
        } else {
            //没有勾选复选框, 没有库存数据, 但是我们需要初始化一条, 不然前端有可能报错
            Item item = new Item();
            item.setSellerId(goodsEntity.getGoods().getSellerId());
            //价格,防止价格太低造成商家亏本
            item.setPrice(new BigDecimal("99999999999"));
            //库存量,没有库存
            item.setNum(0);
            //初始化规格
            item.setSpec("{}");
            //标题
            item.setTitle(goodsEntity.getGoods().getGoodsName());
            //设置库存对象的属性值
            setItemValue(goodsEntity, item);
            itemDao.insertSelective(item);
        }
    }
}
