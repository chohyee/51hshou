package cn.wells.web.servlet;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.Converter;

import cn.wells.domain.User;
import cn.wells.service.UserService;
import cn.wells.utils.CommonsUtils;
import cn.wells.utils.MailUtils;

public class UserServlet extends BaseServlet {
	/**
	 * 注销用户功能模块
	 * @param request
	 * @param response
	 * @throws ServletException
	 * @throws IOException
	 */
	public void logout(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		HttpSession session = request.getSession();
		//将session中的user删除
		session.removeAttribute("user");
		
		//将存储在客户端的cookie删除掉,生存时间为0
		Cookie cookie_username = new Cookie("cookie_username","");
		cookie_username.setMaxAge(0);
		//密码
		Cookie cookie_password = new Cookie("cookie_password","");
		cookie_password.setMaxAge(0);
		//一定要加这个区覆盖
		response.addCookie(cookie_username);
		response.addCookie(cookie_password);
		
		response.sendRedirect(request.getContextPath()+"/login.jsp");
	}
	/**
	 * 用户注册功能
	 * @param request
	 * @param response
	 * @throws ServletException
	 * @throws IOException
	 */
		public void register(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
			//设置编码方式
			request.setCharacterEncoding("UTF-8");
			
			//获取表单数据
			Map<String, String[]> properties = request.getParameterMap();
			User user = new User();
			
			try {
			//表单中的birthday为字符串传过来,而user中birthday为Date类型，需要转换
			//注册一个转换器，BeanUtils.populate发现封装不了该字符串形式的birthday数据，
			//就采用该转换器将String形式的birthday转换为Date形式的birthday
			ConvertUtils.register(new Converter() {
				@Override
				public Object convert(@SuppressWarnings("rawtypes") Class clazz, Object value) {//value为String值
					//将String转成Date
					Date parse = null;
					SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");//指定日期格式
					try {
						parse = format.parse(value.toString());
					} catch (ParseException e) {
						e.printStackTrace();
					}
					return parse;
				}
			}, Date.class);
			
			//封装的
			
				BeanUtils.populate(user, properties);
			} catch (IllegalAccessException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (InvocationTargetException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}//用BeanUtils工具类用bean对象封装request传过来的数据
			//user中还有缺一些数据
		/*	private String uid;
			private String username;
			private String password;
			private String name;
			private String email;
			private String telephone;
			private Date birthday;
			private String sex;
			private int state;//是否激活
			private String code;//激活码
	*/		
			//uid
			user.setUid(CommonsUtils.getUUID());
			//state
			user.setState(0);
			//激活码
			String activeCode = CommonsUtils.getUUID();
			user.setCode(activeCode);
			
			//将user转给下一层
			UserService service = new UserService();
			Boolean isRegistSuccess = service.regist(user);
			//判断注册是否成功
			
			
			if(isRegistSuccess){
				//注意邮件里面带了生成的激活码，该激活码可以用来识别用户
				String emailMsg = "恭喜您注册成功，请点击下面的连接进行激活账户"
						+ "<a href='http://5407dc8c.nat123.cc"+request.getContextPath()+"/user?method=active&activeCode="+activeCode+"'>http://5407dc8c.nat123.cc"
								+request.getContextPath()+"/user?method=active&activeCode="+activeCode+"</a>";
				try {
					MailUtils.sendMail(user.getEmail(), emailMsg);//邮件发送目的地和内容
				} catch (AddressException e) {
					e.printStackTrace();
				} catch (MessagingException e) {
					System.out.println(e);
				}
				//跳转到注册成功页面,重定向
				response.sendRedirect(request.getContextPath()+"/registerSuccess.jsp");
			}else{
				//跳转到注册失败页面
				response.sendRedirect(request.getContextPath()+"/registerFail.jsp");
			}
			
			
		}

		/**
		 * 检验用户名是否存在功能
		 * @param request
		 * @param response
		 * @throws ServletException
		 * @throws IOException
		 */
		public void checkUsername(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
			//获取用户名
			request.setCharacterEncoding("utf-8");
			String username = request.getParameter("username");
			
			UserService service = new UserService();
			boolean isExit = service.checkUsername(username);
			String json = "{\"isExit\":"+isExit+"}";
			//向异步Ajax处写数据，data接收
			response.getWriter().write(json);
			
		}
		
		/**
		 * 激活用户功能
		 * @param request
		 * @param response
		 * @throws ServletException
		 * @throws IOException
		 */
		public void active(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
			//获得激活码
			String activeCode = request.getParameter("activeCode");
			UserService service = new UserService();
			service.active(activeCode);
			//跳转,重定向
			response.sendRedirect(request.getContextPath()+"/login.jsp");
		}
		/**
		 * 用户登录功能
		 * @param request
		 * @param response
		 * @throws ServletException
		 * @throws IOException
		 */
		public void login(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
			//获取session
			HttpSession session = request.getSession();
			String username = request.getParameter("username");
			String password = request.getParameter("password");
			//暂不考虑用户是否激活
			UserService service = new UserService();
			User user = null;
		
			try {
				user = service.login(username,password);
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			//用户登陆成功才能选择自动登录
			if(user!=null){
				//登录成功才能选择自动登录,因此才要判断是否勾选自动登录
				//***************判断用户是否勾选了自动登录*****************
				String autoLogin = request.getParameter("autoLogin");
				if("true".equals(autoLogin)){
					//要自动登录
					//创建存储用户名的cookie
					Cookie cookie_username = new Cookie("cookie_username",user.getUsername());
					cookie_username.setMaxAge(10*60*1000);
					//创建存储密码的cookie
					Cookie cookie_password = new Cookie("cookie_password",user.getPassword());
					cookie_password.setMaxAge(10*60*1000);

					response.addCookie(cookie_username);
					response.addCookie(cookie_password);
				}

				//***************************************************
				//将user对象存到session中,供其他页面调用
				session.setAttribute("user", user);

				//重定向到首页
				response.sendRedirect(request.getContextPath());
			}else{
				request.setAttribute("loginError", "用户名或密码错误");
				request.getRequestDispatcher("/login.jsp").forward(request, response);
			}
		}
}