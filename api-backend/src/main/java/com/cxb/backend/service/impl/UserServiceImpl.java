package com.cxb.backend.service.impl;

import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.LineCaptcha;
import cn.hutool.captcha.generator.MathGenerator;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.crypto.digest.DigestUtil;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cxb.apicommon.common.JwtUtils;
import com.cxb.backend.mapper.UserMapper;
import com.cxb.backend.model.vo.UserKeyVo;
import com.cxb.backend.model.vo.UserVO;
import com.cxb.backend.service.UserService;
import com.cxb.apicommon.exception.BusinessException;
import com.cxb.apicommon.model.dto.message.SmsMessage;
import com.cxb.apicommon.model.entity.User;
import com.cxb.apicommon.common.ErrorCode;
import lombok.extern.slf4j.Slf4j;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import static com.cxb.backend.constant.UserConstant.ADMIN_ROLE;
import static com.cxb.backend.constant.UserConstant.USER_LOGIN_STATE;
import static com.cxb.apicommon.constant.RabbitmqConstant.SMS_CAPTCHA_EXCHANGE;
import static com.cxb.apicommon.constant.RabbitmqConstant.SMS_CAPTCHA_KEY;


/**
 * 用户服务实现类
 *
 * @author yupi
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
        implements UserService {

    @Resource
    private UserMapper userMapper;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    private static final String API_CAPTCHA_PRE = "api:captcha:";

    @Resource
    private RabbitTemplate rabbitTemplate;

    /**
     * 盐值，混淆密码
     */
    private static final String SALT = "cxb";

    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword, String captchaCode, HttpServletRequest request) {
        // 1. 校验
        if (CharSequenceUtil.hasBlank(userAccount, userPassword, checkPassword, captchaCode)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        //校验验证码
        String sessionId = request.getSession().getId();
        String cacheCaptchaCode = stringRedisTemplate.opsForValue().get(API_CAPTCHA_PRE + sessionId);
        if (cacheCaptchaCode == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "验证码已过期");
        }
        //四则运算验证码校验
        boolean verify = new MathGenerator().verify(cacheCaptchaCode, captchaCode);
        if (!verify) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "验证码错误");
        }
        if (userAccount.length() < 4 || userAccount.length() > 17) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号格式错误");
        }
        if (userPassword.length() < 8 || userPassword.length() > 17) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户密码格式错误");
        }
        // 密码和校验密码相同
        if (!userPassword.equals(checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次输入的密码不一致");
        }
        synchronized (userAccount.intern()) {
            // 账户不能重复
            QueryWrapper<User> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("userAccount", userAccount);
            long count = userMapper.selectCount(queryWrapper);
            if (count > 0) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号重复");
            }
            // 2. 加密
            String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
            //3.分配ak、sk
            String accessKey = DigestUtil.md5Hex(SALT + userAccount + RandomUtil.randomNumbers(5));
            String secretKey = DigestUtil.md5Hex(SALT + userAccount + RandomUtil.randomNumbers(8));
            // 4. 插入数据
            User user = new User();
            user.setUserAccount(userAccount);
            user.setUserPassword(encryptPassword);
            user.setAccessKey(accessKey);
            user.setSecretKey(secretKey);
            boolean saveResult = this.save(user);
            if (!saveResult) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "注册失败，数据库错误");
            }
            return user.getId();
        }
    }

    @Override
    public UserVO userLogin(String userAccount, String userPassword, HttpServletRequest request, HttpServletResponse response) {
        // 1. 校验
        if (CharSequenceUtil.hasBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (userAccount.length() < 3) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号错误");
        }
        if (userPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码错误");
        }
        // 2. 加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
        // 查询用户是否存在
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        queryWrapper.eq("userPassword", encryptPassword);
        User user = userMapper.selectOne(queryWrapper);
        // 用户不存在
        if (user == null) {
            log.info("user login failed, userAccount cannot match userPassword");
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在或密码错误");
        }
        // 3. 记录用户到cookie
        UserVO userVO = BeanUtil.copyProperties(user, UserVO.class);
        setJwtTokenCookie(response, userVO.getId());
        return userVO;
    }

    /**
     * 手机号登录
     *
     * @param phoneNumber
     * @param captcha
     * @param request
     * @return
     */
    @Override
    public UserVO userLoginByPhone(String phoneNumber, String captcha, HttpServletRequest request, HttpServletResponse response) {
        //1.校验
        if (!Pattern.matches("^(13[0-9]|14[01456879]|15[0-35-9]|16[2567]|17[0-8]|18[0-9]|19[0-35-9])\\d{8}$", phoneNumber)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "不合法的手机号！");
        }
        //比对验证码
        String smsCaptcha = stringRedisTemplate.opsForValue().get("api:sms:login:" + phoneNumber);
        if (smsCaptcha == null) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "验证码已过期！");
        }
        if (!captcha.equals(smsCaptcha)) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "验证码错误！");
        }
        //2.查询手机号
        User user = lambdaQuery().eq(User::getPhone, phoneNumber).one();
        if (user == null) {
            //3.手机号不存在，创建用户并分配ak、sk
            User insertUser = new User();
            String accessKey = DigestUtil.md5Hex(SALT + phoneNumber + RandomUtil.randomNumbers(5));
            String secretKey = DigestUtil.md5Hex(SALT + phoneNumber + RandomUtil.randomNumbers(8));
            insertUser.setAccessKey(accessKey);
            insertUser.setSecretKey(secretKey);
            insertUser.setPhone(phoneNumber);
            boolean b = this.save(insertUser);
            if (!b) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR);
            }
            // 4. 生成token,存放到cookie
            UserVO userVO = BeanUtil.copyProperties(insertUser, UserVO.class);
            setJwtTokenCookie(response, userVO.getId());
            return userVO;
        } else {
            UserVO userVO = BeanUtil.copyProperties(user, UserVO.class);
            setJwtTokenCookie(response, userVO.getId());
            return userVO;
        }
    }

    /**
     * 添加cookie,存放jwt token
     *
     * @param response
     * @param userId
     */
    private void setJwtTokenCookie(HttpServletResponse response, Long userId) {
        String token = JwtUtils.getJwtToken(userId);
        Cookie cookie = new Cookie("token", token);
        //有效路径
        cookie.setPath("/");
        //HttpOnly Cookies是一个cookie安全行的解决方案,JavaScript脚本将无法读取到Cookie信息
        cookie.setHttpOnly(true);
        response.addCookie(cookie);
    }

    /**
     * 更新ak、sk
     *
     * @param userId
     * @return
     */
    @Override
    public UserKeyVo updateUserKey(Long userId) {
        User user = getById(userId);
        String accessKey = null;
        String secretKey = null;
        if (user.getUserAccount() != null) {
            accessKey = DigestUtil.md5Hex(SALT + user.getUserAccount() + RandomUtil.randomNumbers(5));
            secretKey = DigestUtil.md5Hex(SALT + user.getUserAccount() + RandomUtil.randomNumbers(8));
        } else {
            accessKey = DigestUtil.md5Hex(SALT + user.getPhone() + RandomUtil.randomNumbers(5));
            secretKey = DigestUtil.md5Hex(SALT + user.getPhone() + RandomUtil.randomNumbers(8));
        }
        user.setAccessKey(accessKey);
        user.setSecretKey(secretKey);
        boolean b = updateById(user);
        if (!b) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
        UserKeyVo userKeyVo = new UserKeyVo();
        userKeyVo.setAccessKey(accessKey);
        userKeyVo.setSecretKey(secretKey);
        return userKeyVo;
    }

    /**
     * 更新用户信息
     *
     * @param userPassword    密码
     * @param newUserPassword 密码
     * @param checkPassword   确认密码
     * @param user
     * @param request
     * @return
     */
    @Override
    public boolean updateUserInfo(String userPassword, String newUserPassword, String checkPassword, User user, HttpServletRequest request) {
        //1.获取当前用户
        Long userId = user.getId();
        if (userId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //2.设置账号密码
        if (userPassword == null && newUserPassword != null && checkPassword != null) {
            if (!newUserPassword.equals(checkPassword)) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次密码一致");
            }
            if (newUserPassword.length() < 8 || newUserPassword.length() > 17) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户密码格式错误");
            }
            // 加密
            String encryptPassword = DigestUtils.md5DigestAsHex((SALT + newUserPassword).getBytes());
            user.setUserPassword(encryptPassword);
        }
        //3.修改密码
        if (userPassword != null && newUserPassword != null && checkPassword != null) {
            //校验密码
            String intoEncryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
            String sqlEncryptPassword = getById(userId).getUserPassword();
            if (!intoEncryptPassword.equals(sqlEncryptPassword)) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码错误！");
            }
            if (userPassword.equals(newUserPassword)) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "新密码与旧密码一致");
            }
            if (!newUserPassword.equals(checkPassword)) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次密码不一致");
            }
            if (newUserPassword.length() < 8 || newUserPassword.length() > 17) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户密码格式错误");
            }
            // 加密
            String encryptPassword = DigestUtils.md5DigestAsHex((SALT + newUserPassword).getBytes());
            user.setUserPassword(encryptPassword);
        }
        return updateById(user);
    }


    /**
     * 获取当前登录用户
     *
     * @param request
     * @return
     */
    @Override
    public User getLoginUser(HttpServletRequest request) {
        // 获取用户id
        Long userId = JwtUtils.getUserIdByToken(request);
        if (userId == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        // 从数据库查询（追求性能的话可以注释，直接走缓存）
        User user = this.getById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "用户不存在");
        }
        return user;
    }

    /**
     * 是否为管理员
     *
     * @param request
     * @return
     */
    @Override
    public boolean isAdmin(HttpServletRequest request) {
        // 仅管理员可查询
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User user = (User) userObj;
        return user != null && ADMIN_ROLE.equals(user.getUserRole());
    }

    /**
     * 用户注销
     *
     * @param request
     * @param response
     */
    @Override
    public boolean userLogout(HttpServletRequest request, HttpServletResponse response) {
        if (request == null || response == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //删除cookie其实就是给指定name的cookie写入空值.然后最大存活时间设为0
        Cookie[] cookies = request.getCookies();
        for (Cookie cookie : cookies) {
            if(cookie.getName().equals("token")){
                Cookie timeoutCookie = new Cookie(cookie.getName(), cookie.getValue());
                timeoutCookie.setMaxAge(0);
                //有效路径
                timeoutCookie.setPath("/");
                //HttpOnly Cookies是一个cookie安全行的解决方案,JavaScript脚本将无法读取到Cookie信息
                timeoutCookie.setHttpOnly(true);
                response.addCookie(timeoutCookie);
                return true;
            }
        }
        return false;
    }

    /**
     * 获取图片验证码
     *
     * @param request
     * @param response
     */
    @Override
    public void getCaptCha(HttpServletRequest request, HttpServletResponse response) {
        //获取sessionId,作为redis唯一标识
        String sessionId = request.getSession().getId();
        // 定义图形验证码的长、宽、验证码字符数、干扰元素个数
        LineCaptcha captcha = CaptchaUtil.createLineCaptcha(100, 30, 4, 10);
        // 自定义验证码内容为四则运算方式
        captcha.setGenerator(new MathGenerator(1));
        //设置响应头
        response.setContentType("image/jpeg");
        response.setHeader("Pragma", "No-cache");//禁用缓存
        //try-with-resources（自动资源管理) 自动关闭资源
        try (ServletOutputStream outputStream = response.getOutputStream()) {
            //输出到页面
            captcha.write(outputStream);
            //打印日志
            log.info("生成的验证码： {}", captcha.getCode());
            //验证码存放到redis,2分钟过期
            stringRedisTemplate.opsForValue().set(API_CAPTCHA_PRE + sessionId,
                    captcha.getCode(), 2, TimeUnit.MINUTES);
        } catch (IOException e) {
            e.printStackTrace();
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }


    }

    /**
     * 发送短信验证码
     *
     * @param phoneNumber 手机号
     * @param captchaType 验证码类型
     * @return
     */
    @Override
    public boolean sendSmsCaptcha(String phoneNumber, String captchaType) {

        //1.校验手机号

        if (!Pattern.matches("^(13[0-9]|14[01456879]|15[0-35-9]|16[2567]|17[0-8]|18[0-9]|19[0-35-9])\\d{8}$", phoneNumber)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "手机号格式不正确");
        }
        //2.生成验证码和消息
        String captcha = RandomUtil.randomNumbers(5);
        SmsMessage smsMessage = new SmsMessage(captcha, captchaType, phoneNumber);

        //3.redis  一个时间一天只能发送5次短信
        String data = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        //得到明天零点时间
        LocalDateTime midnight = LocalDateTime.now().plusDays(1).withHour(0).withMinute(0).withSecond(0);
        //得到今天剩余时间
        long leftTime = ChronoUnit.SECONDS.between(LocalDateTime.now(), midnight);
        Long count = stringRedisTemplate.opsForValue().increment("api:sms:" + phoneNumber + ":" + data);
        if (count == 1) {
            stringRedisTemplate.expire("api:sms:" + phoneNumber + ":" + data, leftTime, TimeUnit.SECONDS);
        }
        if (count >= 6) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "今天短信发送次数已用完！");
        }

        //4.消息队列异步发送短信，响应更快，提高吞吐量
        rabbitTemplate.convertAndSend(SMS_CAPTCHA_EXCHANGE, SMS_CAPTCHA_KEY, smsMessage);
        log.info("短信消息：" + smsMessage);

        return true;
    }


    /**
     * 用户更新手机号
     *
     * @param phoneNumber 手机号
     * @param captcha     验证码
     * @param request     request
     * @return
     */
    @Override
    public boolean userUpdatePhone(String phoneNumber, String captcha, HttpServletRequest request) {
        //1.校验
        if (!Pattern.matches("^(13[0-9]|14[01456879]|15[0-35-9]|16[2567]|17[0-8]|18[0-9]|19[0-35-9])\\d{8}$", phoneNumber)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "不合法的手机号！");
        }
        //比对验证码
        String smsCaptcha = stringRedisTemplate.opsForValue().get("api:sms:update:" + phoneNumber);
        if (smsCaptcha == null) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "验证码已过期！");
        }
        if (!captcha.equals(smsCaptcha)) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "验证码错误！");
        }
        //2.判断类型
        User user = getLoginUser(request);
        if (user.getPhone() == null) {
            //手机号为空，绑定手机号
            user.setPhone(phoneNumber);
        }
        if (user.getPhone() != null) {
            //手机号不为空,修改手机号
            if (Objects.equals(user.getPhone(), phoneNumber)) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "新手机号与旧手机号一致");
            }
            user.setPhone(phoneNumber);
        }
        return updateById(user);
    }

    /**
     * 用户更新手机号校验
     *
     * @param phoneNumber
     * @param captcha
     * @param request
     * @return
     */
    @Override
    public boolean userUpdatePhoneCheck(String phoneNumber, String captcha, HttpServletRequest request) {
        //1.校验
        if (!Pattern.matches("^(13[0-9]|14[01456879]|15[0-35-9]|16[2567]|17[0-8]|18[0-9]|19[0-35-9])\\d{8}$", phoneNumber)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "不合法的手机号！");
        }
        //比对验证码
        String smsCaptcha = stringRedisTemplate.opsForValue().get("api:sms:check:" + phoneNumber);
        if (smsCaptcha == null) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "验证码已过期！");
        }
        if (!captcha.equals(smsCaptcha)) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "验证码错误！");
        }
        //2.判断类型
        User user = getLoginUser(request);
        if (user.getPhone() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        if (!user.getPhone().equals(phoneNumber)) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "手机号错误");
        }

        return true;
    }


}




