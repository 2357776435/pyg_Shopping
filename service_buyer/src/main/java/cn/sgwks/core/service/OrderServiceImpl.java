package cn.sgwks.core.service;

import cn.sgwks.core.dao.log.PayLogDao;
import cn.sgwks.core.dao.order.OrderDao;
import cn.sgwks.core.dao.order.OrderItemDao;
import cn.sgwks.core.pojo.entity.BuyerCart;
import cn.sgwks.core.pojo.log.PayLog;
import cn.sgwks.core.pojo.order.Order;
import cn.sgwks.core.pojo.order.OrderItem;
import cn.sgwks.core.util.Constants;
import cn.sgwks.core.util.IdWorker;
import com.alibaba.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@Transactional
public class OrderServiceImpl implements OrderService {
    @Autowired
    private PayLogDao payLogDao;
    @Autowired
    private OrderDao orderDao;//订单表
    @Autowired
    private OrderItemDao orderItemDao;//订单详情表
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private IdWorker idWorker;

    /**
     * 提交订单
     *
     * @param order 订单pojo对象
     */
    @Override
    public void add(Order order) {
        //1. 从订单对象中获取当前登录用户用户名
        String userId = order.getUserId();
        //2. 根据用户名获取购物车集合
        List<BuyerCart> cartList = (List<BuyerCart>) redisTemplate.boundHashOps(Constants.CART_LIST_REDIS).get(userId);
        ArrayList<String> orderIdList = new ArrayList<>();//订单ID列表
        double total_money = 0;//总金额 （元）
        //3. 遍历购物车集合
        if (cartList != null) {
            for (BuyerCart cart : cartList) {
                //TODO 4. 根据购物车对象保存订单数据
                long orderId = idWorker.nextId();
                System.out.println("sellerId:" + cart.getSellerId());
                Order tborder = new Order();//新创建订单对象
                tborder.setOrderId(orderId);//订单ID
                tborder.setUserId(order.getUserId());//用户名
                tborder.setPaymentType(order.getPaymentType());//支付类型
                tborder.setStatus("1");//状态：未付款
                tborder.setCreateTime(new Date());//订单创建日期
                tborder.setUpdateTime(new Date());//订单更新日期
                tborder.setReceiverAreaName(order.getReceiverAreaName());//地址
                tborder.setReceiverMobile(order.getReceiverMobile());//手机号
                tborder.setReceiver(order.getReceiver());//收货人
                tborder.setSourceType(order.getSourceType());//订单来源
                tborder.setSellerId(cart.getSellerId());//商家ID
                //循环购物车明细
                double money = 0;
                //5. 从购物车中获取购物项集合
                List<OrderItem> orderItemList = cart.getOrderItemList();
                //6. 遍历购物项集合
                if (orderItemList != null) {
                    for (OrderItem orderItem : orderItemList) {
                        //TODO 7. 根据购物项对象保存订单详情数据
                        orderItem.setId(idWorker.nextId());
                        orderItem.setOrderId(orderId);//订单ID
                        orderItem.setSellerId(cart.getSellerId());
                        money += orderItem.getTotalFee().doubleValue();//金额累加
                        orderItemDao.insertSelective(orderItem);
                    }
                }
                tborder.setPayment(new BigDecimal(money));
                orderDao.insertSelective(tborder);
                orderIdList.add(orderId + "");//添加到订单列表
                total_money += money;//累加到总金额
            }
        }
        //TODO 8. 计算总价钱保存支付日志数据
        if ("1".equals(order.getPaymentType())) {//如果是微信支付
            PayLog payLog = new PayLog();
            String outTradeNo = idWorker.nextId() + "";//支付订单号
            payLog.setOutTradeNo(outTradeNo);//支付订单号
            payLog.setCreateTime(new Date());//创建时间
            //订单号列表，逗号分隔
            String ids = orderIdList.toString().replace("[", "").replace("]", "").replace(" ", "");
            payLog.setOrderList(ids);//订单号列表，逗号分隔
            payLog.setPayType("1");//支付类型
            payLog.setTotalFee((long) (total_money * 100));//总金额(分)
            payLog.setTradeState("0");//支付状态
            payLog.setUserId(order.getUserId());//用户ID
            payLogDao.insertSelective(payLog);//插入到支付日志表
            //TODO 9. 使用当前登录用户的用户名作为key, 支付日志对象作为value存入redis中供支付使用
            redisTemplate.boundHashOps(Constants.PAY_LIST_LOG).put(order.getUserId(), payLog);//放入缓存
        }
        //TODO 10. 根据当前登录用户的用户名删除购物车
        redisTemplate.boundHashOps(Constants.CART_LIST_REDIS).delete(order.getUserId());
    }

    /**
     * 根据用户名获取支付日志对象
     *
     * @param userName
     * @return
     */
    @Override
    public PayLog getPayLogByUserName(String userName) {
        PayLog payLog = (PayLog) redisTemplate.boundHashOps(Constants.PAY_LIST_LOG).get(userName);
        return payLog;
    }

    /**
     * 如果支付成功, 支付日志表和订单表的支付状态改为已支付, redis的支付日志对象删除
     *
     * @param userName
     */
    @Override
    public void updatePayStatus(String userName) {
        //1. 根据登录用户的用户名, 获取redis中的支付日志对象
        PayLog payLog = (PayLog) redisTemplate.boundHashOps(Constants.PAY_LIST_LOG).get(userName);
        //2. 根据支付日志对象修改数据库中的支付状态
        payLog.setTradeState("1");//交易状态,交易未成功为0,交易成功为1
        payLog.setPayTime(new Date());//支付完成时间
        payLogDao.updateByPrimaryKeySelective(payLog);
        //3. 根据订单id修改订单表的支付状态,例如:37,38 多个购物车对象
        String orderListStr = payLog.getOrderList();
        //得到订单id数组
        String[] split = orderListStr.split(",");
        if (split != null) {
            //变量订单id数组得到单个指定的订单id用来修改订单状态
            for (String orderId : split) {
                Order order = new Order();
                order.setOrderId(Long.parseLong(orderId));
                order.setStatus("2");//状态：1、未付款，2、已付款，3、未发货，4、已发货，5、交易成功，6、交易关闭,7、待评价
                orderDao.updateByPrimaryKeySelective(order);
            }
        }
        //4. 删除redis中这个用户的支付日志对象
        redisTemplate.boundHashOps(Constants.PAY_LIST_LOG).delete(userName);
    }
}
