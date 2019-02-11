package cn.e3mall.cart.service;

import java.util.List;

import cn.e3mall.common.utils.E3Result;
import cn.e3mall.pojo.TbItem;

/**  

* <p>Title: CartService</p>  

* <p>Description:购物车的接口 </p>  

* @author 赵天宇

* @date 2019年1月28日  

*/
public interface CartService {
	
	//添加购物车
	public E3Result addcart(long userId,long itemId,int num);
	//合并购物车
	public E3Result mergecart(long userId,List<TbItem> itemList);
	//取商品列表
	List<TbItem> getCart(long userId);
	//更新购物车的商品数量
	E3Result updatecartNum(long userId,long itemId,int num);
	
	//删除商品
	E3Result delcart(long userId,long itemId);
	
	//请客购物车
	E3Result clearcart(Long userId);
}
