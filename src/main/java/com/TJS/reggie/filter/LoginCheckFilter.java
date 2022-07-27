package com.TJS.reggie.filter;

import com.TJS.reggie.common.BaseContext;
import com.TJS.reggie.common.R;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.AntPathMatcher;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;

@Slf4j
@WebFilter(filterName = "loginCheckFilter",urlPatterns = "/*")
public class LoginCheckFilter implements Filter {
    //路径匹配器，支持通配符
    public static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        //获取请求的URI
        String requestURI = request.getRequestURI();
        //判断请求是否应该过滤
        String[] urls = new String[]{//这些都是需要放行的
                "/employee/login",
                "/employee/logout",
                "/backend/**",
                "/front/**",
                "/common/**",
                "/user/login",
                "/user/sendMsg"
        };
        //如果 请求不需要处理就放行
        if(check(urls,requestURI)){
            filterChain.doFilter(request,response);
            return;
        }
        Object empId = request.getSession().getAttribute("employee");
        //如果已经后台用户登录就放行
        if(empId !=null){
            BaseContext.set((long)empId);
            filterChain.doFilter(request,response);
            return;
        }
        Object userId = request.getSession().getAttribute("user");
        //如果已经用户登录就放行
        if(userId !=null){
            BaseContext.set((long)userId);
            filterChain.doFilter(request,response);
            return;
        }
        //如果未登录
        response.getWriter().write(JSON.toJSONString(R.error("NOTLOGIN")));
    }

    /**
     *路径检查，检查是否需要放行
     * @param urls
     * @param URI
     * @return
     */
    public boolean check(String[] urls,String URI){
        for (String url : urls) {
            boolean match = PATH_MATCHER.match(url, URI);
            if(match)return true;
        }
        return false;
    }
}
