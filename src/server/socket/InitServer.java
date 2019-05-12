package server.socket;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.charset.Charset;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

import constant.InetConfig;
import pojo.UserPojo;

public class InitServer implements Runnable{
	
	private ServerSocketChannel serverSocketChannel;
	
	private Selector selector;
	
	private Charset charset = Charset.forName("utf-8");
	
	private BlockingDeque<UserPojo> userlist = new LinkedBlockingDeque<>();
	
	public InitServer() throws IOException {
		
		selector = Selector.open();
		serverSocketChannel = ServerSocketChannel.open();
		
		// 配置服务器为非阻塞模式
		serverSocketChannel.configureBlocking(false);
		
		// 设置为true时，当 ServerSocket 关闭时, 如果网络上还有发送到这个 ServerSocket 的数据, 
		// 这个ServerSocket 不会立即释放本地端口, 而是会等待一段时间, 确保接收到了网络上发送过来的延迟数据, 然后再释放端口.
		serverSocketChannel.socket().setReuseAddress(true);
		serverSocketChannel.socket().bind(new InetSocketAddress(InetConfig.SERVER_PORT));
		
		
	}
	
	@Override
	public void run() {
		
		
		
	}
	
}
