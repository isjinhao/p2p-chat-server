package constant;

import java.util.ResourceBundle;

public class InetConfig {
	
	// 指定客户端接收服务器和其他客户端消息的端口
	public static Integer CLIENT_CHAT_PORT;
	
	// 指定服务器接收客户端消息的端口
	public static Integer SERVER_PORT;
	
	// 指定服务器的ip地址
	public static String SERVER_IP;
	
	static {
		
		ResourceBundle bundle = ResourceBundle.getBundle("base");
		SERVER_PORT = Integer.valueOf(bundle.getString("server_port"));
		SERVER_IP = bundle.getString("server_ip");
		CLIENT_CHAT_PORT = Integer.valueOf(bundle.getString("client_chat_port"));
		
	}
	
}