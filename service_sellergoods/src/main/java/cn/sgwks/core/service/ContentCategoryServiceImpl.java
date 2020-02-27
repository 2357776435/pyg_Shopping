package cn.sgwks.core.service;

import cn.sgwks.core.dao.ad.ContentCategoryDao;
import cn.sgwks.core.pojo.ad.ContentCategory;
import cn.sgwks.core.pojo.ad.ContentCategoryQuery;
import cn.sgwks.core.pojo.entity.PageResult;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ContentCategoryServiceImpl implements ContentCategoryService{
    @Autowired
    private ContentCategoryDao contentCategoryDao;

    @Override
    public List<ContentCategory> findAll() {
        return contentCategoryDao.selectByExample(null);
    }

    @Override
    public void add(ContentCategory contentCategory) {
        contentCategoryDao.insertSelective(contentCategory);
    }

    @Override
    public ContentCategory findOne(Long id) {
        return contentCategoryDao.selectByPrimaryKey(id);
    }

    @Override
    public void update(ContentCategory contentCategory) {
        contentCategoryDao.updateByPrimaryKeySelective(contentCategory);
    }

    @Override
    public void delete(Long[] ids) {
        if(ids!=null){
            for (Long id : ids) {
                contentCategoryDao.deleteByPrimaryKey(id);
            }
        }
    }

    @Override
    public PageResult search(Integer page, Integer rows, ContentCategory contentCategory) {
        PageHelper.startPage(page,rows);
        ContentCategoryQuery contentCategoryQuery = new ContentCategoryQuery();
        ContentCategoryQuery.Criteria criteria = contentCategoryQuery.createCriteria();
        if(contentCategory!=null){
            if (contentCategory.getName() != null && !"".equals(contentCategory.getName())) {
                criteria.andNameLike("%"+contentCategory.getName()+"%");
            }
        }
        Page<ContentCategory> contentCategoriesList = (Page<ContentCategory>)contentCategoryDao.selectByExample(contentCategoryQuery);
        return new PageResult(contentCategoriesList.getTotal(),contentCategoriesList.getResult());
    }
}
