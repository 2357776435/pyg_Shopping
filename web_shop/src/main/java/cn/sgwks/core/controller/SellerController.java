package cn.sgwks.core.controller;

import cn.sgwks.core.pojo.entity.Result;
import cn.sgwks.core.pojo.seller.Seller;
import cn.sgwks.core.service.SellerService;
import com.alibaba.dubbo.config.annotation.Reference;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/seller")
public class SellerController {
    @Reference
    private SellerService sellerService;

    /**
     * 商家注册
     * @param seller 商家实体对象
     * @return
     */
    @RequestMapping("/add")
    public Result add(@RequestBody Seller seller){
        //密码加密,需在配置文件解除2个注释文件，但在发布的时候才开启以便调试
        /**
         * 1.<password-encoder ref="passwordEncoder"></password-encoder>
         * 2.<beans:bean id="passwordEncoder" class="org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder"/>
         */
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String password = passwordEncoder.encode(seller.getPassword());
        seller.setPassword(password);
        try {
            sellerService.add(seller);
            return new Result(true,"添加成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"添加失败");
        }
    }
}
