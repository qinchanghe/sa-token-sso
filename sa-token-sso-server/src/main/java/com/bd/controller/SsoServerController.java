package com.bd.controller;

/**
 * @ClassName SsoServerController
 * @Author qch
 * @Description //TODO
 * @Date 2023/7/11 15:01
 * @Version 1.0
 **/

import cn.dev33.satoken.config.SaSsoConfig;
import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.sso.SaSsoProcessor;
import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.util.SaResult;
import com.alibaba.fastjson.JSON;
import com.bd.domain.SysUser;
import com.bd.mapper.SysUserMapper;
import com.dtflys.forest.Forest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;

/**
 * Sa-Token-SSO Server端 Controller
 */
@RestController
public class SsoServerController {

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private SysUserMapper sysUserMapper;

    /*
     * SSO-Server端：处理所有SSO相关请求 (下面的章节我们会详细列出开放的接口)
     */
    @RequestMapping("/sso/*")
    public Object ssoRequest() {
        StringBuffer requestURL = request.getRequestURL();
        if (requestURL.indexOf("doLogin") != -1) {
            String kaptcha_code = (String) request.getSession().getAttribute("kaptcha_code");
            String code = request.getParameter("code");
            //验证是否对，不管大小写
            if (StringUtils.isEmpty(code)) {
                return SaResult.error("验证码不能为空");
            } else if (!kaptcha_code.equalsIgnoreCase(code)) {
                return SaResult.error("验证码错误");
            }
        }
        return SaSsoProcessor.instance.serverDister();
    }

    /**
     * 配置SSO相关参数
     */
    @Autowired
    private void configSso(SaSsoConfig sso) {
        // 配置：未登录时返回的View
        sso.setNotLoginView(() -> {
            return new ModelAndView("sa-login.html");
        });

        // 配置：登录处理函数
        sso.setDoLoginHandle((name, pwd) -> {
            SysUser user = sysUserMapper.selectUser(name);
            if (ObjectUtils.isEmpty(user)) {
                return SaResult.error("用户不存在！");
            } else if (!pwd.equals(user.getPw())) {
                return SaResult.error("密码错误！");
            } else {
                StpUtil.login("userId-"+user.getUserId());
                SaSession session = StpUtil.getTokenSession();
                session.set("user", JSON.toJSON(user));
                return SaResult.ok("登录成功！").setData(StpUtil.getTokenValue());
            }
        });

        // 配置 Http 请求处理器 （在模式三的单点注销功能下用到，如不需要可以注释掉）
        sso.setSendHttp(url -> {
            try {
                // 发起 http 请求
                System.out.println("------ 发起请求：" + url);
                return Forest.get(url).executeAsString();
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        });
    }

}
