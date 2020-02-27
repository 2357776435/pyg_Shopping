package cn.sgwks.core.service;

import cn.sgwks.core.dao.specification.SpecificationOptionDao;
import cn.sgwks.core.dao.template.TypeTemplateDao;
import cn.sgwks.core.pojo.entity.PageResult;
import cn.sgwks.core.pojo.specification.SpecificationOption;
import cn.sgwks.core.pojo.specification.SpecificationOptionQuery;
import cn.sgwks.core.pojo.template.TypeTemplate;
import cn.sgwks.core.pojo.template.TypeTemplateQuery;
import cn.sgwks.core.util.Constants;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@Transactional
public class TemplateServiceImpl implements TemplateService {
    @Autowired
    private TypeTemplateDao templateDao;//模块
    @Autowired
    private SpecificationOptionDao optionDao;//规格选项
    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 模板高级分页查询
     *
     * @param page
     * @param rows
     * @param typeTemplate
     * @return
     */
    @Override
    public PageResult search(Integer page, Integer rows, TypeTemplate typeTemplate) {
        /**
         * redis中缓存模板所有数据
         */
        //模板id作为key, 品牌集合作为value缓存入redis中
        List<TypeTemplate> templateAll = templateDao.selectByExample(null);
        for (TypeTemplate template : templateAll) {
            //模板id作为key, 品牌集合作为value缓存入redis中,[{"id":1,"text":"联想"}]
            String brandIdsJsonStr = template.getBrandIds();
            //将json转换成集合
            List<Map> brandList = JSON.parseArray(brandIdsJsonStr, Map.class);
            redisTemplate.boundHashOps(Constants.BRAND_LIST_REDIS).put(template.getId(),brandList);
            //模板id作为key, 规格集合作为value缓存入redis中
            List<Map> specList = findBySpecList(template.getId());
            redisTemplate.boundHashOps(Constants.SPEC_LIST_REDIS).put(template.getId(),specList);
        }
        /**
         * 模板分页查询
         */
        PageHelper.startPage(page, rows);
        TypeTemplateQuery typeTemplateQuery = new TypeTemplateQuery();
        TypeTemplateQuery.Criteria criteria = typeTemplateQuery.createCriteria();
        if (typeTemplate != null) {
            if (typeTemplate.getName() != null && !"".equals(typeTemplate.getName())) {
                criteria.andNameLike("%" + typeTemplate.getName() + "%");
            }
        }
        Page<TypeTemplate> templateList = (Page<TypeTemplate>) templateDao.selectByExample(typeTemplateQuery);
        return new PageResult(templateList.getTotal(), templateList.getResult());
    }

    /**
     * 修改模板数据
     *
     * @param typeTemplate
     */
    @Override
    public void update(TypeTemplate typeTemplate) {
        templateDao.updateByPrimaryKeySelective(typeTemplate);
    }

    /**
     * 添加模板数据
     *
     * @param typeTemplate
     */
    @Override
    public void add(TypeTemplate typeTemplate) {
        templateDao.insertSelective(typeTemplate);
    }

    /**
     * 据id获取实体数据作为修改数据的回显
     *
     * @param id
     * @return
     */
    @Override
    public TypeTemplate findOne(Long id) {
        return templateDao.selectByPrimaryKey(id);
    }

    /**
     * 批量删除
     *
     * @param ids
     */
    @Override
    public void delete(Long[] ids) {
        if (ids != null) {
            for (Long id : ids) {
                templateDao.deleteByPrimaryKey(id);
            }
        }
    }

    /**
     * 根据模板id, 查询规格集合和对应的规格选项集合数据
     * options中的sql语句是:
     *  select * from (select * from tb_specification where id='map遍历后id的值') a,tb_specification_option b where b.spec_id=a.id
     *  select * from (select * from tb_specification where id='map遍历后id的值') a,tb_specification_option b where b.spec_id=a.id
     * @param id
     * @return
     */
    @Override
    public List<Map> findBySpecList(Long id) {
        //1. 根据模板id查询模板对象
        TypeTemplate typeTemplate = templateDao.selectByPrimaryKey(id);
        //2. 从模板对象中获取规格集合数据, 获取到的是json格式字符串
        //数据格式例如: [{"id":27,"text":"网络"},{"id":32,"text":"机身内存"}]
        String specIds = typeTemplate.getSpecIds();//例如得到的字符串是：{"id":27,"text":"网络"}
        //3. 将json格式字符串解析成Java中的List集合对象
        List<Map> maps = JSON.parseArray(specIds, Map.class);
        //4. 遍历集合对象
        if (maps != null) {
            for (Map map : maps) {
                //5. 遍历过程中根据规格id, 查询对应的规格选项集合数据
                Long specId =Long.parseLong(String.valueOf(map.get("id")));
                //6. 将规格选项再封装到规格数据中一起返回
                SpecificationOptionQuery query = new SpecificationOptionQuery();
                SpecificationOptionQuery.Criteria criteria = query.createCriteria();
                criteria.andSpecIdEqualTo(specId);
                //根据规格id获取规格选项集合数据
                List<SpecificationOption> optionList =  optionDao.selectByExample(query);
                //将规格选项集合数据封装到原来的map中
                map.put("options", optionList);
            }
        }
        return maps;
    }
}
