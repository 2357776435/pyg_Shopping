package cn.sgwks.core.service;

import cn.sgwks.core.dao.ad.ContentDao;
import cn.sgwks.core.pojo.ad.Content;
import cn.sgwks.core.pojo.ad.ContentQuery;
import cn.sgwks.core.pojo.entity.PageResult;
import cn.sgwks.core.util.Constants;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.List;

/**
 * 使用redis分布式缓存原则:
 * 一般关系型数据库作为我们的主数据库存储数据, 如果涉及到使用分布式缓存redis, 要保证redis中的数据
 * 和数据库中的数据要一致.
 */
@Service
public class ContentServiceImpl implements ContentService {
    @Autowired
    private ContentDao contentDao;
    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public List<Content> findAll() {
        return contentDao.selectByExample(null);
    }

    @Override
    public void add(Content content) {
        //1. 将新广告添加到数据库中
        contentDao.insertSelective(content);
        //2. 根据分类id, 到redis中删除对应分类的广告集合数据
        redisTemplate.boundHashOps(Constants.CONTENT_LIST_REDIS).delete(content.getCategoryId());
    }

    @Override
    public Content findOne(Long id) {
        return contentDao.selectByPrimaryKey(id);
    }

    /**
     * 关系型数据库，修改数据时不能先删除数据，因为删除了数据就无法从原先redis中获取到缓存数据
     * @param content
     */
    @Override
    public void update(Content content) {
        //1. 根据广告id, 到数据库中查询原来的广告对象
        Content oldContent = contentDao.selectByPrimaryKey(content.getId());
        //2. 根据原来的广告对象中的分类id, 到redis中删除对应的广告集合数据
        redisTemplate.boundHashOps(Constants.CONTENT_LIST_REDIS).delete(oldContent.getCategoryId());
        //3. 根据传入的最新的广告对象中的分类id, 删除redis中对应的广告集合数据
        redisTemplate.boundHashOps(Constants.CONTENT_LIST_REDIS).delete(content.getCategoryId());
        //4. 将新的广告对象更新到数据库中
        contentDao.updateByPrimaryKeySelective(content);
    }

    @Override
    public void delete(Long[] ids) {
        if (ids != null) {
            for (Long id : ids) {
                //1. 根据广告id, 到数据库中查询广告对象
                Content content = contentDao.selectByPrimaryKey(id);
                //2. 根据广告对象中的分类id, 删除redis中对应的广告集合数据
                redisTemplate.boundHashOps(Constants.CONTENT_LIST_REDIS).delete(content.getCategoryId());
                //3. 根据广告id删除数据库中的广告数据
                contentDao.deleteByPrimaryKey(id);
            }
        }
    }

    @Override
    public PageResult search(Integer page, Integer rows, Content content) {
        PageHelper.startPage(page, rows);
        ContentQuery contentQuery = new ContentQuery();
        ContentQuery.Criteria criteria = contentQuery.createCriteria();
        if (content != null) {
            if (content.getTitle() != null && !"".equals(content.getTitle())) {
                criteria.andTitleLike("%" + content.getTitle() + "%");
            }
        }
        Page<Content> contentsList = (Page<Content>) contentDao.selectByExample(contentQuery);
        return new PageResult(contentsList.getTotal(), contentsList.getResult());
    }

    /**
     * @param categoryId
     * @return
     */
    @Override
    public List<Content> findByCategoryId(Long categoryId) {
        ContentQuery contentQuery = new ContentQuery();
        ContentQuery.Criteria criteria = contentQuery.createCriteria();
        criteria.andCategoryIdEqualTo(categoryId);
        return contentDao.selectByExample(contentQuery);
    }
    /**
     * 整个redis相当于一个大的hashMap, 在这个map中key是不可以重复的, 所以key是稀缺资源
     * key      value(value使用hash类型因为可以尽量少的占用key)
     *          field           value
     *          分类id            对应的这个分类的广告集合数据List<Content>
     *            001               List<Content>
     *            002               List<Content>
     *
     *
     */
    @Override
    public List<Content> findByCategoryIdFromRedis(Long categoryId) {
        //1. 首先根据分类id到redis中获取数据
        List<Content> contentList = (List<Content>) redisTemplate.boundHashOps(Constants.CONTENT_LIST_REDIS).get(categoryId);
        //2. 如果redis中没有数据则到数据库中获取数据
        if (contentList == null) {
            //3. 如果数据库中获取到数据, 则放入redis中一份
            contentList = findByCategoryId(categoryId);
            redisTemplate.boundHashOps(Constants.CONTENT_LIST_REDIS).put(categoryId, contentList);
        }
        return contentList;
    }
}
