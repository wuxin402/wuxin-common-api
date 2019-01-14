该项目用来读取git上的配置文件
   /**
 	* 读取配置文件的位置,通过命令来设置
	 */
private static String filePath;

public static void main(String[] args) {
	if (args.length > 0) {
		filePath = args[0];
	} else {
		filePath = "http://wuxin.config.com/git-config-1.0.jar";
	}
	//springboot 默认包扫描路径是当前启动类包
    new SpringApplication(App.class).run(args);
}

1、配置preferencesFactory对象
@Bean(value = "preferencesFactory")
public FileSystemPreferencesFactory createPreferencesFactory () {
	return new FileSystemPreferencesFactory(filePath);
}

2、初始化spring的读取配置对象
<bean id="placeholderConfig" class="com.wuxin.git.PreferencesPlaceholderConfigurer">
	<constructor-arg ref="preferencesFactory"></constructor-arg>
	<constructor-arg value="wuxin-spring-cloud"></constructor-arg>
</bean>

3、通过${属性名}获取对应的属性值