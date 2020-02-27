package cn.sgwks.core.service;

import cn.sgwks.core.pojo.item.Item;
import cn.sgwks.core.util.Constants;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.*;
import org.springframework.data.solr.core.query.result.*;

import java.util.*;

@Service
public class ItemSearchServiceImpl implements ItemSearchService {
    @Autowired
    private SolrTemplate solrTemplate;
    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 返回的数据有查询到的集合, 当前页, 每页展示多少条数据, 总记录数, 总页数
     *
     * @param paramMap
     * @return
     */
    @Override
    public Map<String, Object> search(Map paramMap) {
        System.out.println(paramMap);
        //1. 根据查询参数, 到solr中分页, 高亮, 过滤, 排序查询
        Map<String, Object> resultMap = highlightSearch(paramMap);
        //2. 根据查询参数, 到solr中获取对应的分类结果集, 由于分类重复, 所以需要分组去重
        List<String> groupCatagoryList = findGroupCatagoryList(paramMap);
        resultMap.put("categoryList", groupCatagoryList);
        //3. 判断paramMap传入参数中是否有分类名称
        String categoryName = String.valueOf(paramMap.get("category"));
        if (categoryName != null && !"".equals(categoryName)) {
            //4. 如果没有默认就根据第一个分类查询对应的品牌集合和规格集合
            Map specListAndBrandList = findSpecListAndBrandList(categoryName);
            resultMap.putAll(specListAndBrandList);
        } else {
            //5. 如果有分类参数, 则根据分类查询对应的品牌集合和规格集合
            Map specListAndBrandList = findSpecListAndBrandList(groupCatagoryList.get(0));
            resultMap.putAll(specListAndBrandList);
        }
        return resultMap;
    }

    /**
     * 根据关键字, 分页, 高亮, 过滤, 排序查询, 并且将查询结果返回
     * @param paramMap 从页面传入进来的查询参数
     * @return
     */
    private Map<String, Object> highlightSearch(Map paramMap) {
        /**
         * 1:获取查询条件
         */
        //获取查询关键字
        String keywords = String.valueOf(paramMap.get("keywords"));
        if (keywords != null) {
            keywords = keywords.replaceAll(" ", "");
        }
        //当前页
        Integer pageNo = Integer.parseInt(String.valueOf(paramMap.get("pageNo")));
        //每页查询多少条数据
        Integer pageSize = Integer.parseInt(String.valueOf(paramMap.get("pageSize")));
        //获取页面点击的分类过滤条件
        String category = String.valueOf(paramMap.get("category"));
        //获取页面点击的品牌过滤条件
        String brand = String.valueOf(paramMap.get("brand"));
        //获取页面点击的规格过滤条件
        String spec = String.valueOf(paramMap.get("spec"));
        //获取页面点击的价格区间过滤条件
        String price = String.valueOf(paramMap.get("price"));
        //获取页面传入的排序的域
        String sortField = String.valueOf(paramMap.get("sortField"));
        //获取页面传入的排序方式
        String sortType = String.valueOf(paramMap.get("sort"));
        /**
         * 2:封装查询对象
         */
        //创建查询对象
        HighlightQuery highlightQuery = new SimpleHighlightQuery();
        //创建查询条件对象,item_keywords是solr配置文件中的动态标识符，里面包含了name，category,sepc等
        Criteria criteria = new Criteria("item_keywords").is(keywords);
        //将查询条件放入查询对象中
        highlightQuery.addCriteria(criteria);
        //计算从第几条开始查询,如果当前页为空,设置默认值为1
        if (pageNo == null || pageNo <= 0) {
            pageNo = 1;
        }
        //设置从第几条开始查询
        highlightQuery.setOffset((pageNo - 1) * pageSize);
        //设置每页查询多少条数据
        highlightQuery.setRows(pageSize);
        /**
         * 2.1:创建高亮选项对象
         */
        HighlightOptions highlightOptions = new HighlightOptions();
        //设置哪个域需要高亮显示
        highlightOptions.addField("item_title");
        //设置高亮前缀
        highlightOptions.setSimplePrefix("<em style='color:red'>");
        //设置高亮后缀
        highlightOptions.setSimplePostfix("</em>");
        //将高亮选项加入到查询对象中
        highlightQuery.setHighlightOptions(highlightOptions);
        /**
         * 2.2:过滤查询
         */
        //1.根据分类过滤查询
        if (category!=null && !"".equals(category)){
            //创建过滤查询对象
            FilterQuery filterQuery = new SimpleFilterQuery();
            Criteria filterCriteria = new Criteria("item_category").is(category);
            //将条件对象放入过滤对象中
            filterQuery.addCriteria(filterCriteria);
            //过滤对象放入查询对象中
            highlightQuery.addFilterQuery(filterQuery);
        }
        //2.根据品牌顾虑查询
        if (brand != null && !"".equals(brand)) {
            //创建过滤查询对象
            FilterQuery filterQuery = new SimpleFilterQuery();
            //创建条件对象
            Criteria filterCriteria = new Criteria("item_brand").is(brand);
            //将条件对象放入过滤对象中
            filterQuery.addCriteria(filterCriteria);
            //过滤对象放入查询对象中
            highlightQuery.addFilterQuery(filterQuery);
        }
        //3.根据规格过滤查询 spec中的数据格式{网络:移动4G, 内存小大: 16G}
        if (spec != null && !"".equals(spec)) {
            Map<String, String> speMap = JSON.parseObject(spec, Map.class);
            if (speMap != null && speMap.size() > 0) {
                Set<Map.Entry<String, String>> entries = speMap.entrySet();
                for (Map.Entry<String, String> entry : entries) {
                    //创建过滤查询对象
                    FilterQuery filterQuery = new SimpleFilterQuery();
                    //创建条件对象
                    Criteria filterCriteria = new Criteria("item_spec_" + entry.getKey()).is(entry.getValue());
                    //将条件对象放入过滤对象中
                    filterQuery.addCriteria(filterCriteria);
                    //过滤对象放入查询对象中
                    highlightQuery.addFilterQuery(filterQuery);
                }
            }
        }
        //4.根据价格过滤    0-500   500-1000    1000-1500   .....    3000 - *
        if (price != null && !"".equals(price)) {
            //切分价格, 这个素组中有最小值和最大值
            String[] split = price.split("-");
            if (split != null && split.length == 2) {
                //说明大于等于最小值, 如果第一个最小值为0, 进入不到这里
                if (!"0".equals(split[0])) {
                    //创建过滤查询对象
                    FilterQuery filterQuery = new SimpleFilterQuery();
                    //创建条件对象
                    Criteria filterCriteria = new Criteria("item_price").greaterThanEqual(split[0]);
                    //将条件对象放入过滤对象中
                    filterQuery.addCriteria(filterCriteria);
                    //过滤对象放入查询对象中
                    highlightQuery.addFilterQuery(filterQuery);
                }
                //说明小于等于最大值, 如果最后的元素也就是最大值为*, 进入不到这里
                if (!"*".equals(split[1])) {
                    //创建过滤查询对象
                    FilterQuery filterQuery = new SimpleFilterQuery();
                    //创建条件对象
                    Criteria filterCriteria = new Criteria("item_price").lessThanEqual(split[1]);
                    //将条件对象放入过滤对象中
                    filterQuery.addCriteria(filterCriteria);
                    //过滤对象放入查询对象中
                    highlightQuery.addFilterQuery(filterQuery);
                }
            }
        }
        //5.添加排序条件
        if (sortField != null && sortType != null && !"".equals(sortField) && !"".equals(sortType)){
            //升序排序
            if ("ASC".equals(sortType)) {
                //创建排序对象
                Sort sort = new Sort(Sort.Direction.ASC, "item_" + sortField);
                //将排序对象放入查询对象中
                highlightQuery.addSort(sort);
            }
            //降序排序
            if ("DESC".equals(sortType)) {
                Sort sort = new Sort(Sort.Direction.DESC , "item_" + sortField);
                //将排序对象放入查询对象中
                highlightQuery.addSort(sort);
            }
        }
        /**
         * 3:查询并返回结果
         */
        HighlightPage<Item> items = solrTemplate.queryForHighlightPage(highlightQuery, Item.class);
        //获取带高亮的集合
        List<HighlightEntry<Item>> highlighted = items.getHighlighted();
        //创建集合接收高亮的数据
        ArrayList<Item> itemList = new ArrayList<>();
        //遍历高亮集合
        for (HighlightEntry<Item> itemHighlightEntry : highlighted) {
            //获取到不带高亮的实体对象
            Item item = itemHighlightEntry.getEntity();
            List<HighlightEntry.Highlight> highlights = itemHighlightEntry.getHighlights();
            //如果高亮的field和snipplets数据是否为空并且集合数量是否大于0,否则使用默认的title数据
            if (highlights != null && highlights.size() > 0) {
                //获取到高亮标题集合,因为每个产品只有一个,再次获取snipplets,title样式数据在里面
                List<String> highlightTitle = highlights.get(0).getSnipplets();
                //如果高亮的集合数据是否为空并且集合数量是否大于0,否则使用默认的title数据
                if (highlightTitle != null && highlightTitle.size() > 0) {
                    //终于获取到高亮的标题,获取value,0就是该集合的下标，value就是title值
                    String title = highlightTitle.get(0);
                    item.setTitle(title);
                }
            }
            itemList.add(item);
        }
        HashMap<String, Object> resultMap = new HashMap<>();
        //查询到的结果集
        resultMap.put("rows", itemList);
        //查询到的总页数
        resultMap.put("totalPages", items.getTotalPages());
        //查询到的总条数
        resultMap.put("total", items.getTotalElements());
        return resultMap;
    }

    /**
     * 根据关键字到solr中查询结果集中对应的分类集合, 要分组去重
     *
     * @param paramMap 页面传入的查询参数
     * @return
     */
    private List<String> findGroupCatagoryList(Map paramMap) {
        ArrayList<String> resultList = new ArrayList<>();
        //获取查询关键字
        String keywords = String.valueOf(paramMap.get("keywords"));
        if (keywords != null) {
            //把空格空串剔除
            keywords = keywords.replaceAll(" ", "");
        }
        //创建查询对象
        Query simpleQuery = new SimpleQuery();
        //创建查询条件对象
        Criteria criteria = new Criteria("item_keywords").is(keywords);
        //将查询条件放入查询对象中
        simpleQuery.addCriteria(criteria);
        /**
         * 1.创建分组对象
         */
        GroupOptions groupOptions = new GroupOptions();
        //设置根据分类域进行分组
        groupOptions.addGroupByField("item_category");
        //将分组对象放入查询对象中
        simpleQuery.setGroupOptions(groupOptions);
        //分组查询分类集合
        GroupPage<Item> items = solrTemplate.queryForGroupPage(simpleQuery, Item.class);
        //获取结果集中分类域的集合
        GroupResult<Item> item_category = items.getGroupResult("item_category");
        //获取分类域的实体集合
        Page<GroupEntry<Item>> groupEntries = item_category.getGroupEntries();
        //遍历实体集合得到实体对象
        for (GroupEntry<Item> groupEntry : groupEntries) {
            //集合中获取手机数据
            String groupValue = groupEntry.getGroupValue();
            resultList.add(groupValue);
        }
        return resultList;
    }
    /**
     * 根据分类名称查询对应的品牌集合和规格集合
     * @param categoryName  分类名称
     * @return
     */
    private Map findSpecListAndBrandList(String categoryName) {
        //1. 根据分类名称到redis中查询对应的模板id
        Long templateId = (Long) redisTemplate.boundHashOps(Constants.CATEGORY_LIST_REDIS).get(categoryName);
        //2. 根据模板id到redis中查询对应的品牌集合
        List<Map> brandList = (List<Map>)redisTemplate.boundHashOps(Constants.BRAND_LIST_REDIS).get(templateId);
        //3. 根据模板id到reids中查询对应的规格集合
        List<Map> specList = (List<Map>)redisTemplate.boundHashOps(Constants.SPEC_LIST_REDIS).get(templateId);
        //4. 将品牌集合和规格集合数据封装到Map中返回
        Map resultMap = new HashMap<>();
        resultMap.put("brandList",brandList);
        resultMap.put("specList",specList);
        return resultMap;
    }
}
