package com.kexin.framework.web.service;

import com.kexin.common.constant.CacheConstants;
import com.kexin.common.core.domain.entity.SysUser;
import com.kexin.common.core.domain.model.LoginUser;
import com.kexin.common.core.redis.RedisCache;
import com.kexin.common.exception.ServiceException;
import com.kexin.common.exception.user.CaptchaException;
import com.kexin.common.exception.user.CaptchaExpireException;
import com.kexin.common.exception.user.UserPasswordNotMatchException;
import com.kexin.common.utils.DateUtils;
import com.kexin.common.utils.ServletUtils;
import com.kexin.common.utils.StringUtils;
import com.kexin.common.utils.ip.IpUtils;
import com.kexin.system.service.ISysConfigService;
import com.kexin.system.service.ISysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * Login verification class
 */
@Component
public class SysLoginService {
    @Autowired
    private TokenService tokenService;

    @Resource
    private AuthenticationManager authenticationManager;

    @Autowired
    private RedisCache redisCache;

    @Autowired
    private ISysUserService userService;

    @Autowired
    private ISysConfigService configService;

    /**
     * login verification
     *
     * @param username username
     * @param password password
     * @param code     captcha
     * @param uuid     unique identification
     * @return user token
     */
    public String login(String username, String password, String code, String uuid) {
        boolean captchaOnOff = configService.selectCaptchaOnOff();
        // verification code switch
        if (captchaOnOff) {
            validateCaptcha(username, code, uuid);
        }
        // user Authentication
        Authentication authentication;
        try {
            // call the UserDetailsServiceImpl.loadUserByUsername method complete the authentication
            authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
        } catch (Exception e) {
            if (e instanceof BadCredentialsException) {
                throw new UserPasswordNotMatchException();
            } else {
                throw new ServiceException(e.getMessage());
            }
        }
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        recordLoginInfo(loginUser.getUserId());
        // generator token
        return tokenService.createToken(loginUser);
    }

    /**
     * verification code
     *
     * @param username username
     * @param code     captcha
     * @param uuid     unique identification
     */
    public void validateCaptcha(String username, String code, String uuid) {
        String verifyKey = CacheConstants.CAPTCHA_CODE_KEY + StringUtils.nvl(uuid, "");
        String captcha = redisCache.getCacheObject(verifyKey);
        redisCache.deleteObject(verifyKey);
        if (captcha == null) {
            throw new CaptchaExpireException();
        }
        if (!code.equalsIgnoreCase(captcha)) {
            throw new CaptchaException();
        }
    }

    /**
     * record user login information
     */
    public void recordLoginInfo(Long userId) {
        SysUser sysUser = new SysUser();
        sysUser.setUserId(userId);
        sysUser.setLoginIp(IpUtils.getIpAddr(ServletUtils.getRequest()));
        sysUser.setLoginDate(DateUtils.getNowDate());
        userService.updateUserProfile(sysUser);
    }
}
