package cn.sgwks.core.service;

import cn.sgwks.core.dao.specification.SpecificationDao;
import cn.sgwks.core.dao.specification.SpecificationOptionDao;
import cn.sgwks.core.pojo.entity.PageResult;
import cn.sgwks.core.pojo.entity.SpecEntity;
import cn.sgwks.core.pojo.specification.Specification;
import cn.sgwks.core.pojo.specification.SpecificationOption;
import cn.sgwks.core.pojo.specification.SpecificationOptionQuery;
import cn.sgwks.core.pojo.specification.SpecificationQuery;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@Transactional
public class SpecificationServiceImpl implements SpecificationService {
    @Autowired
    private SpecificationDao specificationDao;
    @Autowired
    private SpecificationOptionDao specificationOptionDao;

    /**
     * 规格高级分页查询
     *
     * @param page
     * @param rows
     * @param spec
     * @return
     */
    @Override
    public PageResult search(Integer page, Integer rows, Specification spec) {
        //利用分页助手实现分页，第一个参数:当前页，第二个参数：每页展示的数据条数
        PageHelper.startPage(page, rows);
        SpecificationQuery specificationQuery = new SpecificationQuery();
        SpecificationQuery.Criteria criteria = specificationQuery.createCriteria();
        if (spec != null) {
            if (spec.getSpecName() != null && !"".equals(spec.getSpecName())) {
                criteria.andSpecNameLike("%" + spec.getSpecName() + "%");
            }
        }
        Page<Specification> specList = (Page<Specification>) specificationDao.selectByExample(specificationQuery);
        return new PageResult(specList.getTotal(), specList.getResult());
    }

    /**
     * 规格添加
     *
     * @param specEntity
     */
    @Override
    public void add(SpecEntity specEntity) {
        //1.添加规格对象
        specificationDao.insertSelective(specEntity.getSpecification());
        //2.添加规格选项集合对象
        if (specEntity.getSpecificationOptionList() != null) {
            for (SpecificationOption option : specEntity.getSpecificationOptionList()) {
                //获取规格的主键id，作为规格选项表的外键
                Long id = specEntity.getSpecification().getId();
                //设置规格选项外键
                option.setSpecId(id);
                specificationOptionDao.insertSelective(option);
            }
        }
    }

    /**
     * 规格数据回显
     *
     * @param id
     * @return
     */
    @Override
    public SpecEntity findOne(Long id) {
        //1.根据规格id查询规格对象
        Specification specification = specificationDao.selectByPrimaryKey(id);
        //2.根据规格id查询规格选项集合对象
        SpecificationOptionQuery specificationOptionQuery = new SpecificationOptionQuery();
        SpecificationOptionQuery.Criteria criteria = specificationOptionQuery.createCriteria();
        criteria.andSpecIdEqualTo(id);
        List<SpecificationOption> specificationOptions = specificationOptionDao.selectByExample(specificationOptionQuery);
        //3.将规格对象和规格选项集合对象封装到返回的实体对象中
        SpecEntity specEntity = new SpecEntity();
        specEntity.setSpecification(specification);
        specEntity.setSpecificationOptionList(specificationOptions);
        return specEntity;
    }

    @Override
    public void update(SpecEntity specEntity) {
        //1.根据规格对象进行更新
        specificationDao.updateByPrimaryKeySelective(specEntity.getSpecification());
        //创建条件查询
        SpecificationOptionQuery specificationOptionQuery = new SpecificationOptionQuery();
        SpecificationOptionQuery.Criteria criteria = specificationOptionQuery.createCriteria();
        //根据规格选项id删除规格选项集合数据，规格id在这里是外键
        criteria.andSpecIdEqualTo(specEntity.getSpecification().getId());
        //2.根据规格id删除对应的规格选项集合数据
        specificationOptionDao.deleteByExample(specificationOptionQuery);
        //3.将新的规格选项集合对象插入到规格选项表中
        if (specEntity.getSpecificationOptionList() != null) {
            for (SpecificationOption option : specEntity.getSpecificationOptionList()) {
                //设置选项对象外键
                option.setSpecId(specEntity.getSpecification().getId());
                specificationOptionDao.insertSelective(option);
            }
        }
    }

    /**
     * 批量删除
     * @param ids
     */
    @Override
    public void delete(Long[] ids) {
        if(ids!=null){
            for (Long id : ids) {
                //1.根据规格id删除规格对象
                specificationDao.deleteByPrimaryKey(id);
                //2.根据规格id删除规格选项集合对象
                SpecificationOptionQuery specificationOptionQuery = new SpecificationOptionQuery();
                SpecificationOptionQuery.Criteria criteria = specificationOptionQuery.createCriteria();
                criteria.andSpecIdEqualTo(id);
                specificationOptionDao.deleteByExample(specificationOptionQuery);
            }
        }
    }

    /**
     * 获取模板(规格)下拉数据
     * @return
     */
    @Override
    public List<Map> selectOptionList() {
        return specificationDao.selectOptionList();
    }
}
