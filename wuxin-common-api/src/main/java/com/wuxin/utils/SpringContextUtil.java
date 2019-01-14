package com.wuxin.utils;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class SpringContextUtil implements ApplicationContextAware{
	private static ApplicationContext applicationContext;
	
	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		SpringContextUtil.applicationContext = applicationContext;
	}
	
	 /**
	  * 返回spring容器
	  * @return ApplicationContext
	  */
	  public static ApplicationContext getApplicationContext() {
	    return applicationContext;
	  }
	 
	/**
	 * 通过bean的名字获取bean
	 * @param name
	 * @return
	 * @throws BeansException
	 */
	public static Object getBean(String name) throws BeansException {
	    return applicationContext.getBean(name);
	  }
	 
	/**
	 * 通过类名找对应的bean
	 * @param name
	 * @param requiredType
	 * @return
	 * @throws BeansException
	 */
	public static Object getBean(String name, Class<?> requiredType) throws BeansException {
	    return applicationContext.getBean(name, requiredType);
	  }
}
