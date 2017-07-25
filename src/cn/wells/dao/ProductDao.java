package cn.wells.dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.dbutils.handlers.MapListHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;

import cn.wells.domain.Category;
import cn.wells.domain.Order;
import cn.wells.domain.OrderItem;
import cn.wells.domain.Product;
import cn.wells.utils.DataSourceUtils;

public class ProductDao {

	/**
	 * 获得热门商品
	 * @return
	 * @throws SQLException
	 */
	public List<Product> findHotProductList() throws SQLException {
		QueryRunner runner = new QueryRunner(DataSourceUtils.getDataSource());
		String sql = "select * from product where is_hot=? limit?,?";
		List<Product> query = runner.query(sql, new BeanListHandler<Product>(Product.class), 1,0,9);//只要9条记录
		return query;
	}
	/**
	 * 获得最新商品
	 * @return
	 * @throws SQLException
	 */
	public List<Product> findNewProductList() throws SQLException {
		QueryRunner runner = new QueryRunner(DataSourceUtils.getDataSource());
		String sql = "select * from product order by pdate desc limit?,?";
		List<Product> query = runner.query(sql, new BeanListHandler<Product>(Product.class), 0,9);//只要9条记录
		return query;
	}
	/**
	 * 准备商品分类数据
	 * @return
	 * @throws SQLException
	 */
	public List<Category> findCategoryList() throws SQLException {
		QueryRunner runner = new QueryRunner(DataSourceUtils.getDataSource());
		String sql = "select * from category";
		List<Category> query = runner.query(sql, new BeanListHandler<Category>(Category.class));
		return query;
	}
	/**
	 * 根据商品分类cid获取对应商品的总条数
	 * @param cid
	 * @return
	 * @throws SQLException
	 */
	public int getCounnt(String cid) throws SQLException {
		QueryRunner runner = new QueryRunner(DataSourceUtils.getDataSource());
		String sql = "select count(*) from product where cid=?";
		Long query = (Long)runner.query(sql, new ScalarHandler(),cid);
		return query.intValue();
	}
	/**
	 * 根据商品分类cid获取对应商品的分页数据
	 * @param cid
	 * @param index
	 * @param currentCount
	 * @return 
	 * @throws SQLException 
	 */
	public List<Product> findProductByPage(String cid, int index, int currentCount) throws SQLException {
		QueryRunner runner = new QueryRunner(DataSourceUtils.getDataSource());
		String sql = "select * from product where cid=? limit ?,?";
		List<Product> list = runner.query(sql, new BeanListHandler<Product>(Product.class), cid,index,currentCount);
		//System.out.println(list);
		return list;
	}
	/**
	 * 按pid查询某个商品的详细信息
	 * @param pid
	 * @return
	 * @throws SQLException 
	 */
	public Product findProductByPid(String pid) throws SQLException {
		QueryRunner runner = new QueryRunner(DataSourceUtils.getDataSource());
		String sql = "select * from product where pid=?";
		return runner.query(sql, new BeanHandler<Product>(Product.class), pid);
	}
	/**
	 * 向orders表插入数据
	 * @param order
	 * @throws SQLException 
	 */
	public void addOrders(Order order) throws SQLException {
		QueryRunner runner = new QueryRunner();
		Connection conn = DataSourceUtils.getConnection();
		String sql = "insert into orders values(?,?,?,?,?,?,?,?)";
		runner.update(conn, sql, order.getOid(),order.getOrdertime(),order.getTotal(),
					order.getState(),order.getAddress(),order.getName(),order.getTelephone(),
					order.getUser().getUid()
				);
	}
	
	/**
	 * 向orderitem表插入数据
	 * @param order
	 * @throws SQLException 
	 */
	public void addOrderItem(Order order) throws SQLException {
		QueryRunner runner = new QueryRunner();
		Connection conn = DataSourceUtils.getConnection();
		String sql = "insert into orderitem values(?,?,?,?,?)";
		List<OrderItem> orderItems = order.getOrderItems();
		for(OrderItem item:orderItems){
			runner.update(conn,sql,item.getItemid(),item.getCount(),item.getSubtotal(),item.getProduct().getPid(),item.getOrder().getOid());
		}
	}
	/**
	 * 更新收件人信息
	 * @param order
	 * @throws SQLException
	 */
	public void updateOrderAdrr(Order order) throws SQLException {
		QueryRunner runner = new QueryRunner(DataSourceUtils.getDataSource());
		String sql = "update orders set address=?,name=?,telephone=? where oid=?";
		runner.update(sql, order.getAddress(),order.getName(),order.getTelephone(),order.getOid());
	}
	/**
	 * 更新订单支付状态
	 * @param r6_Order
	 * @throws SQLException
	 */
	public void updateOrderState(String r6_Order) throws SQLException {
		QueryRunner runner = new QueryRunner(DataSourceUtils.getDataSource());
		String sql = "update orders set state=? where oid=?";
		runner.update(sql, 1,r6_Order);
	}
	/**
	 * 
	 * @param uid
	 * @return
	 */
	public List<Order> findAllOrders(String uid) throws SQLException {
		QueryRunner runner = new QueryRunner(DataSourceUtils.getDataSource());
		String sql = "select * from orders where uid=?";
		return runner.query(sql, new BeanListHandler<Order>(Order.class), uid);
	}
	public List<Map<String, Object>> findAllOrderItemByOid(String oid) throws SQLException {
		QueryRunner runner = new QueryRunner(DataSourceUtils.getDataSource());
		String sql = "select i.count,i.subtotal,p.pimage,p.pname,p.shop_price from orderitem i,product p where  i.pid=p.pid and i.oid=?";
		List<Map<String, Object>> mapList = runner.query(sql, new MapListHandler(), oid);
		return mapList;
	}

}
