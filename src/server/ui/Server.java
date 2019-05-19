package server.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextPane;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import constant.InetConfig;
import constant.MessageType;
import pojo.UserPojo;
import pojo.message.LoginMessage;
import pojo.message.OfflineMessage;
import pojo.message.RegisterMessage;
import server.dao.DBHander;
import utils.ui.JTextPaneUtils;

public class Server extends JFrame {

	private static final long serialVersionUID = 1L;

	// 显示 登录用户的 用户名、ip、端口、登录时间
	final DefaultTableModel onlineUsersDtm = new DefaultTableModel();
	private final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
	private final JPanel contentPane;
	private final JTable tableOnlineUsers;
	private final JTextPane textPaneMsgRecord;
	
	private ServerSocketChannel serverSocketChannel;
	private Selector selector;
	private Charset charset = Charset.forName("utf-8");
	private BlockingDeque<UserPojo> userlist = new LinkedBlockingDeque<>();

	private DBHander dbHander = DBHander.getInstance();
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				try {
					Server frame = new Server();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	public Server() {
		//服务器界面
		setTitle("\u670D\u52A1\u5668");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 561, 403);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);

		JSplitPane splitPaneNorth = new JSplitPane();
		splitPaneNorth.setResizeWeight(0.5);
		contentPane.add(splitPaneNorth, BorderLayout.CENTER);

		JScrollPane scrollPaneMsgRecord = new JScrollPane();
		scrollPaneMsgRecord.setPreferredSize(new Dimension(100, 300));
		scrollPaneMsgRecord.setViewportBorder(
				new TitledBorder(null, "\u6D88\u606F\u8BB0\u5F55", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		splitPaneNorth.setLeftComponent(scrollPaneMsgRecord);

		textPaneMsgRecord = new JTextPane();
		textPaneMsgRecord.setPreferredSize(new Dimension(100, 100));
		scrollPaneMsgRecord.setViewportView(textPaneMsgRecord);
		JScrollPane scrollPaneOnlineUsers = new JScrollPane();
		scrollPaneOnlineUsers.setPreferredSize(new Dimension(100, 300));
		splitPaneNorth.setRightComponent(scrollPaneOnlineUsers);

		onlineUsersDtm.addColumn("用户名");
		onlineUsersDtm.addColumn("IP");
		onlineUsersDtm.addColumn("端口");
		onlineUsersDtm.addColumn("登录时间");
		tableOnlineUsers = new JTable(onlineUsersDtm);
		tableOnlineUsers.setPreferredSize(new Dimension(100, 270));
		tableOnlineUsers.setFillsViewportHeight(true); // 让JTable充满它的容器
		scrollPaneOnlineUsers.setViewportView(tableOnlineUsers);

		JPanel panelSouth = new JPanel();
		contentPane.add(panelSouth, BorderLayout.SOUTH);
		panelSouth.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));

		final JButton btnStart = new JButton("\u542F\u52A8");
		// "启动"按钮
		btnStart.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				
				String msgRecord = dateFormat.format(new Date()) + " 服务器正在启动！" + "\r\n";
				JTextPaneUtils.printTextLog(textPaneMsgRecord, msgRecord, Color.red);
				new Thread() {

					@Override
					public void run() {
						
						try {
							selector = Selector.open();
							serverSocketChannel = ServerSocketChannel.open();
							// 配置服务器为非阻塞模式
							// 设置为true时，当 ServerSocket 关闭时, 如果网络上还有发送到这个 ServerSocket 的数据, 
							// 这个ServerSocket 不会立即释放本地端口, 而是会等待一段时间, 确保接收到了网络上发送过来的延迟数据, 然后再释放端口.
							//serverSocketChannel.socket().setReuseAddress(true);
							serverSocketChannel.socket().bind(new InetSocketAddress(InetConfig.SERVER_PORT));
							serverSocketChannel.configureBlocking(false);
							serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
						} catch (IOException e2) {
							e2.printStackTrace();
						}
						
						String msgRecord = dateFormat.format(new Date()) + " 服务器启动成功！" + "\r\n";
						JTextPaneUtils.printTextLog(textPaneMsgRecord, msgRecord, Color.red);
						
						while (true) {
							//没有连接就会阻塞
							int n = 0;
							try {
								n = selector.select();
							} catch (IOException e1) {
								e1.printStackTrace();
							}
							if (n == 0)
								continue;
							Set<SelectionKey> readkeys = selector.selectedKeys();
							Iterator<SelectionKey> iterator = readkeys.iterator();
							while (iterator.hasNext()) {
								SelectionKey key = iterator.next();
								System.out.println("interestOps :   " + key.interestOps());
								if(!key.isValid()) {
									key.cancel();
									iterator.remove();
									continue;
								}
								if (key.isAcceptable()) {
									accept(key);
								}
								if(key.isReadable()) {
									try {
										read(key);
									} catch (IOException e) {
										e.printStackTrace();
									}
								}
								iterator.remove();
							}  //end iterator
						} // end endless-loop
					}
				}.start();
				btnStart.setEnabled(false);
			}
		});
		panelSouth.add(btnStart);
	}

	private void read(SelectionKey key) throws IOException{
        ByteBuffer recvBuff=ByteBuffer.allocate(10240);//接收缓冲区
        ByteBuffer sendBuff=ByteBuffer.allocate(10240);//发送缓冲区 
        SocketChannel clientChannel=(SocketChannel)key.channel(); //会话通道
        
		clientChannel.read(recvBuff);
        recvBuff.flip(); //缓冲区指针回到数据起点
        String recvStr=charset.decode(recvBuff).toString(); //解码成字符串
        
        // 接受到的字符串解码成JSONObject对象
        JSONObject recvObj = JSONObject.parseObject(recvStr);
        String type = null;
        
        // 无法解码成JSONObject对象 表示接受到的不是 JSON 字符串
        if(recvObj != null)
        	type = (String)recvObj.get("type");
        // 能正确解码的字符串才能 进入其他过程
        if(type != null) {
        	
        	// 接收到的是注册消息
            if(MessageType.REGISTER.equals(type)) {
            	RegisterMessage parseObject = JSON.parseObject(recvStr, RegisterMessage.class);
            	System.out.println(parseObject);
            	boolean isInserted = dbHander.insertUser(parseObject.getId(), parseObject.getPwd());
            	sendBuff.clear(); //发送缓冲区清空
            	if(isInserted) {
            		sendBuff=ByteBuffer.wrap("true".getBytes(charset)); //发送字符串放入缓冲区
            	} else {
            		sendBuff=ByteBuffer.wrap("false".getBytes(charset)); //发送字符串放入缓冲区
            	}
			// 接收到的是登录消息
            } else if (MessageType.LOGIN.equals(type)) {
            	LoginMessage lm = JSON.parseObject(recvStr, LoginMessage.class);
            	Boolean exists = dbHander.isUserExists(lm.getId(), lm.getPwd());
            	
            	// 当 账户密码不正确时，返回 登录失败 
            	if(!exists) {
            		sendBuff=ByteBuffer.wrap("false".getBytes(charset)); //发送字符串放入缓冲区
					clientChannel.write(sendBuff);
					
				// 账户密码正确时
            	} else {
            		
            		// 创建一个表示客户端的对象，暂时只存入 id
            		UserPojo up = new UserPojo();
            		up.setId(lm.getId());
            		
            		// 账号已登录，返回 重复登录
            		if(userlist.contains(up)) {
            			sendBuff=ByteBuffer.wrap("reLogin".getBytes(charset)); //发送字符串放入缓冲区
            			
            		// 账号密码正确 且是 第一次登录
            		} else {
            			
            			// 面板上打印控制信息
            			String msgRecord = dateFormat.format(new Date()) + ":  " + lm.getId() + " 登录！" + "\r\n";
            			JTextPaneUtils.printTextLog(textPaneMsgRecord, msgRecord, Color.red);
            			
            			// 将 客户端的ip封装在 客户端对象中
        				InetSocketAddress remoteAddress = (InetSocketAddress)clientChannel.getRemoteAddress();
        				up.setIp(remoteAddress.getHostString());
        				
        				// 返回当前系统中其他 已登录的用户
        				String string = JSONObject.toJSONString(userlist);
        				sendBuff=ByteBuffer.wrap(string.getBytes(charset)); //发送字符串放入缓冲区
        				
        				// 将当前用户 加入 已登录用户名单
        				try {
							userlist.put(up);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
        				System.out.println(up.getId() + "	登录后，所有用户的信息：" + userlist);
        				
        				// 用户信息添加在面板上
        				onlineUsersDtm.addRow(new Object[] { 
        						up.getId(), 
        						up.getIp(),
        						remoteAddress.getPort(),
    							dateFormat.format(new Date()) });
        				
            		}
            	}
            } else if (MessageType.OFFLINE.equals(type)) {
            	OfflineMessage offlineMessage = JSON.parseObject(recvStr, OfflineMessage.class);
            	UserPojo up = new UserPojo();
            	up.setId(offlineMessage.getId());
            	userlist.remove(up);
            	System.out.println(up.getId() + "	退出后，所有用户的信息：" + userlist);
            	
            	// 从面板上删除用户的信息
            	for (int i = 0; i < onlineUsersDtm.getRowCount(); i++) {
					if (onlineUsersDtm.getValueAt(i, 0).equals(up.getId())) {
						onlineUsersDtm.removeRow(i);
					}
				}
            	
            	String msgRecord = dateFormat.format(new Date()) + ":  " + offlineMessage.getId() + " 退出！" + "\r\n";
    			JTextPaneUtils.printTextLog(textPaneMsgRecord, msgRecord, Color.red);
            	
            }
            clientChannel.write(sendBuff);
        } // end 能正确解码的字符串才能 进入其他过程
        
		key.cancel();	// 此次通信结束之后，关闭此次通话（如果不关闭，喜提异常一堆 ~.~
		try {
			clientChannel.socket().close();
			clientChannel.close();
			sendBuff.clear();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

	// 处理接收连接就绪事件
	private void accept(SelectionKey key){
		// 返回一个对象
		SocketChannel socketChannel;
		try {
			socketChannel = serverSocketChannel.accept();
			socketChannel.configureBlocking(false);
			//注册到selector
			socketChannel.register(selector, SelectionKey.OP_READ, socketChannel);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}