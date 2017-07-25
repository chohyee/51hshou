package cn.wells.service;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import cn.wells.dao.ProductDao;
import cn.wells.domain.Category;
import cn.wells.domain.Order;
import cn.wells.domain.PageBean;
import cn.wells.domain.Product;
import cn.wells.utils.DataSourceUtils;

public class ProductService {

	public void updateOrderAdrr(Order order) {
		ProductDao dao = new ProductDao();
		try {
			dao.updateOrderAdrr(order);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	//获得热门商品
	public List<Product> findHotProductList() {
		ProductDao dao = new ProductDao();
		List<Product> hotProductList = null;
		try {
			hotProductList = dao.findHotProductList();
		} catch (SQLException e) {
			System.out.println(e);
		}
		return hotProductList;
	}
	//获得最新商品
	public List<Product> findNewProductList() {
		ProductDao dao = new ProductDao();
		List<Product> newProductList = null;
		try {
			newProductList = dao.findNewProductList();
		} catch (SQLException e) {
			System.out.println(e);
		}
		return newProductList;
	}
	//准备商品分类数据
	public List<Category> findCategoryList() {
		ProductDao dao = new ProductDao();
		List<Category> categoryList = null;
		try {
			categoryList = dao.findCategoryList();
		} catch (SQLException e) {
			System.out.println(e);
		}
		return categoryList;
	}
	
	//根据商品cid分页显示对应商品
	public PageBean<Product> findProductByCid(String cid,int currentPage,int currentCount) {
		//目的是封装一个pagebean对象返回给web层
		//这里的数据有来自前端，也有来自后台的。参数传过来的为前端数据
		PageBean<Product> pageBean = new PageBean<Product>();
		//1.当前页
		pageBean.setCurrentPage(currentPage);
		//2.每页数
		pageBean.setCurrentCount(currentCount);
		//3.总条数(查数据库)
		int totalCount = 0;
		ProductDao dao = new ProductDao();
		try {
			totalCount = dao.getCounnt(cid);
		} catch (SQLException e) {
			System.out.println(e);
		}
		pageBean.setTotalCount(totalCount);
		//4.总页数
		int totalPage = (int)Math.ceil(1.0*totalCount/currentCount);
		pageBean.setTotalPage(totalPage);
		//5.pageBean包含的数据
		// select * from product where cid=? limit index,currentCount
		int index = (currentPage-1)*currentCount;
		List<Product> list = null;
		try {
			list = dao.findProductByPage(cid,index,currentCount);
		} catch (SQLException e) {
			System.out.println(e);
		}
		pageBean.setList(list);
		return pageBean;
	}
	/**
	 * 按pid查询某个商品的详细信息
	 * @param pid
	 * @return
	 */
	public Product findProductByPid(String pid) {
		ProductDao dao = new ProductDao();
		Product product = null;
		try {
			product = dao.findProductByPid(pid);
		} catch (SQLException e) {
			System.out.println(e);
		}
		return product;
	}
	/**
	 * 提交订单 (事务控制)
	 * 将订单的数据和订单项的数据存储到数据库中
	 * @param order
	 */
	public void submitOrder(Order order) {
		ProductDao dao = new ProductDao();
		try {
			//开启事务
			DataSourceUtils.startTransaction();
			dao.addOrders(order);
			dao.addOrderItem(order);
		} catch (SQLException e) {
			try {
				//捕获到异常，事务回滚
				DataSourceUtils.rollback();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
			e.printStackTrace();
		}finally{
			try {
				//关闭事务
				DataSourceUtils.commitAndRelease();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
	/**
	 * 更新订单支付状态
	 * @param r6_Order
	 */
	public void updateOrderState(String r6_Order) {
		ProductDao dao = new ProductDao();
		try {
			dao.updateOrderState(r6_Order);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	public List<Order> findAllOrders(String uid) {
		ProductDao dao = new ProductDao();
		List<Order> orderList = null;
		try {
			orderList = dao.findAllOrders(uid);
		} catch (SQLException e) {
			System.out.println(e);;
		}
		return orderList;
	}
	public List<Map<String, Object>> findAllOrderItemByOid(String oid) {
		ProductDao dao = new ProductDao();
		List<Map<String, Object>> mapList = null;
		try {
			mapList = dao.findAllOrderItemByOid(oid);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return mapList;
	}
	
	
	//
	
}
