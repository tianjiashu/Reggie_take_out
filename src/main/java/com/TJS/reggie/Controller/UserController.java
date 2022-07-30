package com.TJS.reggie.Controller;

import com.TJS.reggie.Service.UserService;
import com.TJS.reggie.common.BaseContext;
import com.TJS.reggie.common.R;
import com.TJS.reggie.domain.User;
import com.TJS.reggie.utils.SMSUtils;
import com.TJS.reggie.utils.ValidateCodeUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private RedisTemplate<String,String> redisTemplate;
//    /user/sendMsg
    @PostMapping("/sendMsg")
    public R<String> sendMsg(@RequestBody User user, HttpSession session){
        //获取用户电话号码
        String phone = user.getPhone();
        if(phone!=null){
            //随机生成验证码
            String code = ValidateCodeUtils.generateValidateCode4String(4);
            log.info("code:{}",code);
            //发送短信
//            SMSUtils.sendMessage("瑞吉外卖","",phone,code);


            //保存到缓存,保存五分钟
            redisTemplate.opsForValue().set(phone,code,5, TimeUnit.MINUTES);
            //session.setAttribute(phone,code);保存到session

            return R.success("验证码发送成功！");
        }
        return R.error("短语验证码发送失败");
    }

    @PostMapping("/login")
    /*
    {phone: "13412345678", code: "42a2"}
     */
    public R<User> login(@RequestBody Map<String,String> map,HttpSession session){
        //获取手机号 和 验证码
        String phone = map.get("phone");
        String code = map.get("code");
//        Object Oricode = session.getAttribute(phone);  获取原始的验证码

        //从缓存中获取验证码
        String Oricode = redisTemplate.opsForValue().get(phone);

        //验证码比较
        if(code!=null&&code.equals(Oricode)){
            //验证码正确

            //验证码正确就清楚缓存
            redisTemplate.delete(phone);

            //查询用户是否是全新的用户
            LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(User::getPhone,phone);
            User user = userService.getOne(queryWrapper);
            if(user==null){
                //说明用户是全新用户
                user = new User();
                user.setPhone(phone);
                user.setStatus(1);
                userService.save(user);
            }
            session.setAttribute("user",user.getId());
            return R.success(user);
        }
        return R.error("验证码错误");
    }

    @PostMapping("/loginout")
    public R<String> loginout(HttpSession session){
        session.removeAttribute("user");
        return R.success("OK！");
    }
}
