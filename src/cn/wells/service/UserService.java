package cn.wells.service;

import java.sql.SQLException;

import cn.wells.dao.UserDao;
import cn.wells.domain.User;

public class UserService {

	/**
	 * 用户注册
	 * @param user
	 * @return
	 */
	public boolean regist(User user) {
		UserDao dao = new UserDao();
		int raw = 0;
		try {
			raw = dao.regist(user);
		} catch (SQLException e) {
			System.out.println(e);
		}
		
		return raw>0?true:false;
	}

	public void active(String activeCode) {
		UserDao dao = new UserDao();
		try {
			dao.active(activeCode);
		} catch (SQLException e) {	
			System.out.println(e);
		}
	}
	//校验用户名是否存在
	public boolean checkUsername(String username) {
		UserDao dao = new UserDao();
		Long isExit=0L;
		try {
			isExit = dao.checkUsername(username);
		} catch (SQLException e) {
			
			e.printStackTrace();
		}
		return isExit>0?true:false;
	}
	/**
	 * 用户登录
	 * @param username
	 * @param password
	 * @return
	 */
	public User login(String username, String password) throws SQLException {
		UserDao dao = new UserDao();
		return dao.login(username,password);
	}

	
}
