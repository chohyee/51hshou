package cn.wells.web.servlet;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.beanutils.BeanUtils;

import com.google.gson.Gson;

import cn.wells.domain.Cart;
import cn.wells.domain.CartItem;
import cn.wells.domain.Category;
import cn.wells.domain.Order;
import cn.wells.domain.OrderItem;
import cn.wells.domain.PageBean;
import cn.wells.domain.Product;
import cn.wells.domain.User;
import cn.wells.service.ProductService;
import cn.wells.utils.CommonsUtils;
import cn.wells.utils.JedisPoolUtils;
import cn.wells.utils.PaymentUtil;
import redis.clients.jedis.Jedis;

public class ProductServlet extends BaseServlet {

	/**
	 * 订单详情
	 * @param request
	 * @param response
	 * @throws ServletException
	 * @throws IOException
	 */
	public void myOrders(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
		HttpSession session = request.getSession();
		User user = (User) session.getAttribute("user");
		if(user==null){
			//没有登录
			response.sendRedirect(request.getContextPath()+"/login.jsp");
			return;
		}
		ProductService service = new ProductService();
		//查询该用户的所有的订单信息(单表查询orders表)
		//集合中的每一个Order对象的数据是不完整的 缺少List<OrderItem> orderItems数据
		List<Order> orderList = service.findAllOrders(user.getUid());
		//循环所有的订单 为每个订单填充订单项集合信息
		if(orderList!=null){
			for(Order order:orderList){
				//获得每一个订单的oid
				String oid = order.getOid();
				//根据oid查询该订单的所有的订单项---mapList封装的是多个订单项和该订单项中的商品的信息
				List<Map<String,Object>> mapList=service.findAllOrderItemByOid(oid);
				//将mapList转换成List<OrderItem> orderItems 
				for(Map<String,Object> map : mapList){
					
					try {
						//从map中取出count subtotal 封装到OrderItem中
						OrderItem item = new OrderItem();
						//item.setCount(Integer.parseInt(map.get("count").toString()));
						BeanUtils.populate(item, map);
						//从map中取出pimage pname shop_price 封装到Product中
						Product product = new Product();
						BeanUtils.populate(product, map);
						//将product封装到OrderItem
						item.setProduct(product);
						//将orderitem封装到order中的orderItemList中
						order.getOrderItems().add(item);
					} catch (IllegalAccessException | InvocationTargetException e) {
						e.printStackTrace();
					}
				}
			}
		}
		//orderList封装完整了
		request.setAttribute("orderList", orderList);	
		request.getRequestDispatcher("/order_list.jsp").forward(request, response);
	}
	/**
	 * 确认订单（添加地址，电话，收货人,选择支付银行等）
	 * @param request
	 * @param response
	 * @throws ServletException
	 * @throws IOException
	 */
	public void confirmOrder(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
		//1、更新收货人信息
		Map<String, String[]> properties = request.getParameterMap();//4个信息，oid，name，address，telephone
		Order order = new Order();
		try {
			BeanUtils.populate(order, properties);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}

		ProductService service = new ProductService();
		service.updateOrderAdrr(order);//更新收货人信息
		
		//2、在线支付
		/*if(pd_FrpId.equals("ABC-NET-B2C")){
			//介入农行的接口
		}else if(pd_FrpId.equals("ICBC-NET-B2C")){
			//接入工行的接口
		}*/
		//.......

		//只接入一个接口，这个接口已经集成所有的银行接口了  ，这个接口是第三方支付平台提供的
		//接入的是易宝支付
		// 获得 支付必须基本数据
		String orderid = request.getParameter("oid");
		//String money = order.getTotal()+"";//支付金额
		String money = "0.01";//支付金额

		String pd_FrpId = request.getParameter("pd_FrpId");// 银行

		// 发给支付公司需要哪些数据
		String p0_Cmd = "Buy";
		String p1_MerId = ResourceBundle.getBundle("merchantInfo").getString("p1_MerId");
		String p2_Order = orderid;
		String p3_Amt = money;
		String p4_Cur = "CNY";
		String p5_Pid = "";
		String p6_Pcat = "";
		String p7_Pdesc = "";
		// 支付成功回调地址 ---- 第三方支付公司会访问、用户访问
		// 第三方支付可以访问网址
		String p8_Url = ResourceBundle.getBundle("merchantInfo").getString("callback");
		String p9_SAF = "";
		String pa_MP = "";
		String pr_NeedResponse = "1";
		// 加密hmac 需要密钥
		String keyValue = ResourceBundle.getBundle("merchantInfo").getString(
				"keyValue");//秘钥
		String hmac = PaymentUtil.buildHmac(p0_Cmd, p1_MerId, p2_Order, p3_Amt,
				p4_Cur, p5_Pid, p6_Pcat, p7_Pdesc, p8_Url, p9_SAF, pa_MP,
				pd_FrpId, pr_NeedResponse, keyValue);


		//拼接url字符串
		String url = "https://www.yeepay.com/app-merchant-proxy/node?pd_FrpId="+pd_FrpId+
				"&p0_Cmd="+p0_Cmd+
				"&p1_MerId="+p1_MerId+
				"&p2_Order="+p2_Order+
				"&p3_Amt="+p3_Amt+
				"&p4_Cur="+p4_Cur+
				"&p5_Pid="+p5_Pid+
				"&p6_Pcat="+p6_Pcat+
				"&p7_Pdesc="+p7_Pdesc+
				"&p8_Url="+p8_Url+
				"&p9_SAF="+p9_SAF+
				"&pa_MP="+pa_MP+
				"&pr_NeedResponse="+pr_NeedResponse+
				"&hmac="+hmac;

		//重定向到第三方支付平台
		response.sendRedirect(url);

	}
	/**
	 *提交订单 
	 * @param request
	 * @param response
	 * @throws ServletException
	 * @throws IOException
	 */
	public void submitOrder(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
		HttpSession session = request.getSession();
		//判断用户是否已经登录 未登录下面代码不执行
		User user = (User) session.getAttribute("user");
		if(user==null){
			//没有登录
			response.sendRedirect(request.getContextPath()+"/login.jsp");
			return;
		}
		//目的：封装好一个Order对象 传递给service层
		Order order = new Order();

		//1、private String oid;//该订单的订单号
		String oid = CommonsUtils.getUUID();
		order.setOid(oid);

		//2、private Date ordertime;//下单时间
		order.setOrdertime(new Date());

		//3、private double total;//该订单的总金额
		//获得session中的购物车
		Cart cart = (Cart) session.getAttribute("cart");
		double total = cart.getTotal();
		order.setTotal(total);

		//4、private int state;//订单支付状态 1代表已付款 0代表未付款
		order.setState(0);

		//5、private String address;//收货地址
		order.setAddress(null);

		//6、private String name;//收货人
		order.setName(null);

		//7、private String telephone;//收货人电话
		order.setTelephone(null);

		//8、private User user;//该订单属于哪个用户
		order.setUser(user);

		//9、该订单中有多少订单项List<OrderItem> orderItems = new ArrayList<OrderItem>();
		//获得购物车中的购物项的集合map
		Map<String, CartItem> cartItems = cart.getCartItems();
		for(Map.Entry<String, CartItem> entry : cartItems.entrySet()){
			//取出每一个购物项
			CartItem cartItem = entry.getValue();
			//创建新的订单项(将购物车项的数据腾到订单项中)
			OrderItem orderItem = new OrderItem();
			//1)private String itemid;//订单项的id
			orderItem.setItemid(CommonsUtils.getUUID());
			//2)private int count;//订单项内商品的购买数量
			orderItem.setCount(cartItem.getBuyNum());
			//3)private double subtotal;//订单项小计
			orderItem.setSubtotal(cartItem.getSubtotal());
			//4)private Product product;//订单项内部的商品
			orderItem.setProduct(cartItem.getProduct());
			//5)private Order order;//该订单项属于哪个订单
			orderItem.setOrder(order);

			//将该订单项添加到订单的订单项集合中
			order.getOrderItems().add(orderItem);
		}
		//order对象封装完毕
		//传递数据到service层
		ProductService service = new ProductService();
		service.submitOrder(order);
		//暂存于session中，再放到order_info.jsp中显示，修改好订单后
		session.setAttribute("order", order);
		response.sendRedirect(request.getContextPath()+"/order_info.jsp");
		
	}
	/**
	 * 获取商品分类功能
	 * @param request
	 * @param response
	 * @throws ServletException
	 * @throws IOException
	 */
	public void categoryList(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		ProductService service = new ProductService();
		
		//先从缓存中查询categoryList 如果有直接使用 没有在从数据库中查询 存到缓存中
		//1、获得jedis对象 连接redis数据库
		Jedis jedis = JedisPoolUtils.getJedis();

		String categoryListJson = jedis.get("categoryListJson");
		
		
		if(categoryListJson == null){
			//没有就从数据库中用ajax获取
			List<Category> categoryList = service.findCategoryList();
			Gson gson = new Gson();
			categoryListJson = gson.toJson(categoryList);//json的String串
			jedis.set(categoryListJson, categoryListJson);//没有就从数据库获取
		}
		//准备商品分类数据
		
		//Gson 插件，转化为json数据格式
		response.setContentType("text/html;charset=UTF-8");
		response.getWriter().write(categoryListJson);
	}
	
	
	
	/**
	 * 检验验证码是否正确功能
	 * @param request
	 * @param response
	 * @throws ServletException
	 * @throws IOException
	 */
	public void checkIdentify(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String checkCode_client = request.getParameter("checkCode");//乱码的参数
		//get方式乱码解决
		checkCode_client = new String(checkCode_client.getBytes("iso8859-1"),"utf-8");
		//检测验证码是否正确，在IdentifyingCodeServlet中已经设置了session属性checkcode_session，将之对比
  		//获取用户输入的验证码,与session域中的验证码对比，要是错误，就不用连接数据库。跳转到注册页面
		String checkCode_session = (String) request.getSession().getAttribute("checkcode_session");
		boolean equals = checkCode_session.equals(checkCode_client);
		String json ="{\"isSame\":"+equals+"}";
		//System.out.println(json);
		response.getWriter().write(json);	
	}
	
	

	/**
	 * 首页热门商品及最新商品的准备功能
	 * @param request
	 * @param response
	 * @throws ServletException
	 * @throws IOException
	 */
	public void index(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		ProductService service = new ProductService();
		
		//首先准备热门商品--List<Product>
		List<Product> hotProductList = service.findHotProductList();
		//其次准备最新商品--List<Product>
		List<Product> newProductList = service.findNewProductList();
		//准备商品分类数据
		
		
		request.setAttribute("hotProductList", hotProductList);
		request.setAttribute("newProductList", newProductList);
		//System.out.println(hotProductList.get(1).getPname());
		//请求转发
		request.getRequestDispatcher("/index.jsp").forward(request, response);
	}
	
	
	/**
	 * 查询当前商品的具体信息功能
	 * @param request
	 * @param response
	 * @throws ServletException
	 * @throws IOException
	 */
		public void productInfo(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
			//获取数据
			String pid = request.getParameter("pid");
			String cid = request.getParameter("cid");
			String currentPage = request.getParameter("currentPage");
			
			ProductService service = new ProductService();
			Product product = service.findProductByPid(pid);
			
			//传到/product_info.jsp的数据
			request.setAttribute("product", product);
			request.setAttribute("cid", cid);
			request.setAttribute("currentPage", currentPage);
			
			//获得客户端的cookie，将名字为pids的cookie拿出来
			Cookie[] cookies = request.getCookies();
			//设历史记录的pid被设置名为pids的cookie
			String pids = pid;//用来保存从名为pids的cookie
			//下面代码在cookies为空时直接跳到“Cookie cookie_pids = new Cookie("pids",pids);”处
			//下面代码要是执行，则pids覆盖以cookie中的pids
			if(cookies!=null){
				for(Cookie cookie:cookies){
					//System.out.println(cookie.getValue());
					if("pids".equals(cookie.getName())){
						pids = cookie.getValue();//获得从客户端cookie得到的名为pids的数据,格式自定义为1-2-3
						//1-3-2若 本次访问商品pid是8----->8-1-3-2
						//1-3-2 若本次访问商品pid是3----->3-1-2
						//1-3-2 若本次访问商品pid是2----->2-1-3
						String[] split = pids.split("-");
						List<String> asList = Arrays.asList(split);//将数组直接装进list集合
						//用asList构造一个存取有序的LinkedList
						LinkedList<String> list = new LinkedList<String>(asList);
						//判断集合中是否存在当前pid
						if(list.contains(pid)){
							list.remove(pid);
							list.addFirst(pid);
						}else{
							list.addFirst(pid);
						}
						//将list形式转换为String形式
						StringBuffer sb = new StringBuffer();
						for(int i=0;i<list.size();i++){
							sb.append("-");
							sb.append(list.get(i));//-1-2-3
						}
						pids = sb.substring(1);//将-1-2-3前面的-号去掉
					}
				}
			}	
			Cookie cookie_pids = new Cookie("pids",pids);
			response.addCookie(cookie_pids);//追加cookie
			//请求转发
			request.getRequestDispatcher("/product_info.jsp").forward(request, response);
		}

	/**
	 * 产品分页用功能
	 * @param request
	 * @param response
	 * @throws ServletException
	 * @throws IOException
	 */
		public void productListByCid(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
			//获取cid
			String cid = request.getParameter("cid");
			//System.out.println(cid);
			ProductService service = new ProductService();
			//当前页
			String currentPageStr = request.getParameter("currentPage");
			if(currentPageStr==null)currentPageStr = "1";
			int currentPage = Integer.parseInt(currentPageStr);
			//每页显示的条数
			int currentCount = 12;
			PageBean<Product> pageBean = service.findProductByCid(cid,currentPage,currentCount);
			request.setAttribute("pageBean", pageBean);
			request.setAttribute("cid", cid);
			
			
			
			//添加历史浏览数据到product_list.jsp页面用于展示
			//并且应该是从cookie中读取
			List<Product> historyProductList = new ArrayList<Product>();
			Cookie[] cookies = request.getCookies();
			if(cookies!=null){
				for(Cookie cookie:cookies){
					if("pids".equals(cookie.getName())){
						String pids = cookie.getValue();//获得cookie中名为pids的记录,如"2-3-1"
						String[] split = pids.split("-");//将"2-3-1"拆成数组
						for(int j=0;j<split.length;j++){
							Product pro = service.findProductByPid(split[j]);
							historyProductList.add(pro);
							if(j>=6)break;//控制7条历史记录
						}
					}
				}
			}
			
			//将查询到的历史数据集合追加到域中
			request.setAttribute("historyProductList", historyProductList);
			request.getRequestDispatcher("/product_list.jsp").forward(request, response);
		}
		/**
		 * 将商品添加到购物车
		 * @param request
		 * @param response
		 * @throws IOException 
		 */
		public void addProductToCart(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
			//目的是封装一个Cart对象到session域中，在其他页面也可以获取,Cart对象包含
			HttpSession session = request.getSession();//获取客户session
			ProductService service = new ProductService();
			
			//获得该商品的购买数量
			int buyNum = 1;//默认一件
			if(Integer.parseInt(request.getParameter("buyNum"))>0){//用户输入不为负数或者0件的时候
				buyNum = Integer.parseInt(request.getParameter("buyNum"));
			}
			//获得要放到购物车的商品的pid
			String pid = request.getParameter("pid");
			Product product = service.findProductByPid(pid);
			//计算一个商品的总金额
			double subtotal = buyNum*product.getShop_price();
			//将上述数据封装成CartItem对象
			CartItem item = new CartItem();//这是一条新的
			item.setBuyNum(buyNum);
			item.setProduct(product);
			item.setSubtotal(subtotal);
			
			//将item累加到用户对应的session购物车对象中
			//获得购物车---判断是否在session中已经存在购物车
			Cart cart = (Cart)session.getAttribute("cart");
			
			if(cart==null){
				cart = new Cart();
			}
			
			//更新session域中的cart对象值
			
			//将购物项放到车中---key是pid
			//先判断购物车中是否已将包含此购物项了 ----- 判断key是否已经存在
			//如果购物车中已经存在该商品----将现在买的数量与原有的数量进行相加操作
			
			Map<String,CartItem> cartItems = cart.getCartItems();//键为pid，值为对应的CartItem对象的map
			double oldTotal = cart.getTotal();//购物车商品总价;
			
			CartItem oldCartItem = null;//旧的某一条商品记录项(如果cart中存在，后面会给他赋值)
			
			if(cartItems.containsKey(pid)){
				//购物车包含该pid的商品,需要先接收一下商品信息
				oldCartItem = cartItems.get(pid);//旧的某一条商品记录项
				oldCartItem.setBuyNum(buyNum+oldCartItem.getBuyNum());//设置更新后的数量
				oldCartItem.setSubtotal(subtotal+oldCartItem.getSubtotal());//设置更新后的价格
				//上一些步骤已经将cartItems（map）中键为pid的cartitem项更新了，因此下面直接将该cartItems（map）设置到cart对象即可
				//更新cart对象
				cart.setCartItems(cartItems);
				cart.setTotal(cart.getTotal()+subtotal);
			}else{//若果购物车没有该商品的信息
				
				//更新cart对象
				cart.getCartItems().put(pid, item);
				cart.setTotal(cart.getTotal()+subtotal);
			}
			
			//将车再次放回session
			session.setAttribute("cart", cart);
			
			response.sendRedirect(request.getContextPath()+"/cart.jsp");	
		}

		/**
		 * 删除购物车项功能
		 * @param request
		 * @param response
		 * @throws ServletException
		 * @throws IOException
		 */
		public void delProFromCart(HttpServletRequest request, HttpServletResponse response)throws ServletException, IOException {
			//获得要删除的item的pid
			String pid = request.getParameter("pid");
			//删除session中的购物车中的购物项集合中的item
			HttpSession session = request.getSession();
			Cart cart = (Cart) session.getAttribute("cart");
			//修改总价
			cart.setTotal(cart.getTotal()-cart.getCartItems().get(pid).getSubtotal());
			if(cart!=null){
				cart.getCartItems().remove(pid);//引用类型，不用再将值set进cart对应的cartItem了
			}
			//更新session
			session.setAttribute("cart", cart);
			//重定向回cart.jsp
			response.sendRedirect(request.getContextPath()+"/cart.jsp");
		}
		/**
		 * 清空购物车功能
		 * @param request
		 * @param response
		 * @throws ServletException
		 * @throws IOException
		 */
		public void clearCart(HttpServletRequest request, HttpServletResponse response)throws ServletException, IOException{
			HttpSession session = request.getSession();
			session.removeAttribute("cart");
			//跳转回cart.jsp
			response.sendRedirect(request.getContextPath()+"/cart.jsp");
		}
		
		/**
		 * 模板
		 * @param request
		 * @param response
		 */
		/*public void addProductToCart(HttpServletRequest request, HttpServletResponse response)throws ServletException, IOException{
			
		}*/
	
}