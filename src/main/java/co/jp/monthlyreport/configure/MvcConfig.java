package co.jp.monthlyreport.configure;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import co.jp.monthlyreport.controller.AppHandlerInterceptor;

@Configuration
public class MvcConfig implements WebMvcConfigurer {
    @Autowired
    private AppHandlerInterceptor appHandlerInterceptor;

    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/").setViewName("login/login");
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(appHandlerInterceptor)
        .addPathPatterns("/*")
        .excludePathPatterns("/error");
    }
    
}
