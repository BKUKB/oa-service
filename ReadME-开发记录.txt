ERROR：当在C层使用@PreAuthorize注解private方法时，该方法无法获得注入的service bean。
可能原因：与代理类的构造、使用方式和方法权限有关
解释资料：https://blog.csdn.net/qq_25046827/article/details/129272316

TIP：使用ThreadLocal实现线程安全的数据操作

TIP：不同域名、ip、端口之间访问，存在跨域问题，可以使用@CrossOrigin注解进行解决