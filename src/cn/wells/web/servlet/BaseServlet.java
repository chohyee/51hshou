package cn.wells.web.servlet;

import java.io.IOException;
import java.lang.reflect.Method;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
@SuppressWarnings("all")
public class BaseServlet extends HttpServlet {

	@Override
	protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		req.setCharacterEncoding("utf-8");//其他的就不用写了
		try {
			
			// 1、获得请求的method的名称
			String methodName = req.getParameter("method");
			/**
			 * 2、获得当前被访问的对象的字节码对象(this指访问service方法的对象，
			 * 在这里，ProductServlet和UserServlet均继承BaseServlet 因此谁访问该方法，this就代表谁)
			 */
			Class<? extends BaseServlet> clazz = this.getClass();// ProductServlet.class ----UserServlet.class
			// 3获得当前字节码对象的中的指定方法
			Method method = clazz.getMethod(methodName, HttpServletRequest.class,HttpServletResponse.class);
			//4、执行相应功能方法
			method.invoke(this, req,resp);
			
		} catch (Exception e) {
			System.out.println(e);
		}
	}
}