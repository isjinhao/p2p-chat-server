package server.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.mchange.v2.c3p0.ComboPooledDataSource;


/**
 * 
 * 对数据库进行操作的类，包含插入、按id查找、按id+pwd查找，此类是一个单例类
 * @author ISJINHAO
 *
 */
public class DBHander {

	private static ComboPooledDataSource cpds;
	
	private static DBHander bdHander;
	
	private DBHander() {}
	
	public static DBHander getInstance() {
		return bdHander;
	}
	
	static {
		cpds = new ComboPooledDataSource();
		bdHander = new DBHander();
	}

	public Connection getCon() {
		Connection connection = null;
		try {
			connection = cpds.getConnection();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return connection;
	}

	/**
	 * 
	 * 通过 id 判断用户存不存在
	 * @param id
	 * @return
	 */
	public Boolean isUserExists(String id) {
		Connection con = getCon();
		PreparedStatement ps = null;
		String sql = "select * from users where id = ?";
		try {
			ps = con.prepareStatement(sql);
			ps.setString(1, id);
			ResultSet query = ps.executeQuery();
			while (query.next()) {
				// 查询出来有结果，表示存在用户，返回真
				return true;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		// 没有进入query.next()的循环表示无结果，返回假
		return false;
	}
	
	
	/**
	 * 
	 * 通过 id 和 pwd 判断用户是否存在
	 * @param id
	 * @param pwd
	 * @return
	 */
	public Boolean isUserExists(String id, String pwd) {
		Connection con = getCon();
		PreparedStatement ps = null;
		String sql = "select * from users where id = ? and pwd = ?";
		try {
			ps = con.prepareStatement(sql);
			ps.setString(1, id);
			ps.setString(2, pwd);
			ResultSet query = ps.executeQuery();
			while (query.next()) {
				// 查询出来有结果，表示存在用户，返回真
				return true;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		// 没有进入query.next()的循环表示无结果，返回假
		return false;
	}
	
	
	/**
	 * 
	 * 向 users表 插入一个用户
	 * @param id
	 * @param pwd
	 * @return
	 */
	public boolean insertUser(String id, String pwd) {
		// id 已存在 返回假
		if(isUserExists(id))
			return false;
		Connection con = getCon();
		PreparedStatement ps = null;
		String sql = "insert into users(id, pwd) values (?, ?)";
		try {
			ps = con.prepareStatement(sql);
			ps.setString(1, id);
			ps.setString(2, pwd);
			ps.executeUpdate();
			// 成功执行插入，返回真
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		// 执行失败 返回假
		return false;
	}
}