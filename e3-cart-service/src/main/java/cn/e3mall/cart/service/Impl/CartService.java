package cn.e3mall.cart.service.Impl;



import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import cn.e3mall.common.jedis.JedisClient;
import cn.e3mall.common.utils.E3Result;
import cn.e3mall.common.utils.JsonUtils;
import cn.e3mall.mapper.TbItemMapper;
import cn.e3mall.pojo.TbItem;


/**  

* <p>Title: CartService</p>  

* <p>Description: </p>  

* @author 赵天宇

* @date 2019年1月28日  

*/
@Service
public class CartService implements cn.e3mall.cart.service.CartService{

	@Autowired
	private JedisClient jedisClient;
	
	@Value("${REDIS_CART_PRE}")
	private String REDIS_CART_PRE;
	
	@Autowired
	private TbItemMapper itemMapper;
	
	/**
	 * 向Redis中加入购物车
	 * 数据类型是hash key:用户id  field:商品id  value:商品信息
	 */
	@Override
	public E3Result addcart(long userId, long itemId, int num) {
		//判断商品是否存在(itemId+""  是因为要改成字符串的形式)
		Boolean hexists = jedisClient.hexists(REDIS_CART_PRE+":"+userId, itemId+"");
		//如果存在则数量相加
		if(hexists){
			String json = jedisClient.hget(REDIS_CART_PRE+":"+userId, itemId+"");
			TbItem item = JsonUtils.jsonToPojo(json, TbItem.class);
			item.setNum(item.getNum()+num);
			//在写入Redis
			jedisClient.hset(REDIS_CART_PRE+":"+userId, itemId+"", JsonUtils.objectToJson(item));
			return E3Result.ok();
		}
		//如果不存在，根据商品id取商品信息
		TbItem item = itemMapper.selectByPrimaryKey(itemId);
		item.setNum(num);
		//取一张图片
		String image = item.getImage();
		if(StringUtils.isNotBlank(image)){
			String[] images = image.split(",");
			item.setImage(images[0]);
		}
		//添加到购物车列表
		jedisClient.hset(REDIS_CART_PRE+":"+userId, itemId+"", JsonUtils.objectToJson(item));
		//返回结果
		return E3Result.ok();
	}

	
	
	/**
	 * 合并购物车
	 */
	@Override
	public E3Result mergecart(long userId, List<TbItem> itemList) {
		// 遍历商品列表
		// 把列表放入到购物车
		// 判断购物车是否有此商品
		// 如果有，数量相加
		// 如果没有，添加新商品
	
		for (TbItem tbItem : itemList) {
			addcart(userId, tbItem.getId(), tbItem.getNum());
		}
		
		// 返回成功
		return E3Result.ok();
	}



	/**
	 * 取商品列表
	 */
	@Override
	public List<TbItem> getCart(long userId) {
		//根据id获取购物车列表
		List<String> jsonList = jedisClient.hvals(REDIS_CART_PRE+":"+userId);
		List<TbItem> ItemList = new ArrayList<>();
		for (String string : jsonList) {
			//创建一个TbItem对象
			TbItem item = JsonUtils.jsonToPojo(string, TbItem.class);
			//添加到ItemList中
			ItemList.add(item);
		}
		return ItemList;
	}



	
	@Override
	public E3Result updatecartNum(long userId, long itemId, int num) {
		//根据userId, itemId从Redis取出商品
		String json = jedisClient.hget(REDIS_CART_PRE+":"+userId,itemId+"" );
		TbItem item = JsonUtils.jsonToPojo(json, TbItem.class);
		item.setNum(num);
		//写入Redis
		jedisClient.hset(REDIS_CART_PRE+":"+userId,itemId+"",JsonUtils.objectToJson(item));
		return E3Result.ok();
	}



	
	@Override
	public E3Result delcart(long userId, long itemId) {
		//根据userId, itemId从Redis删除商品
		jedisClient.hdel(REDIS_CART_PRE+":"+userId,itemId+"" );
		return E3Result.ok();
	}


	//清空购物车
	@Override
	public E3Result clearcart(Long userId) {
		jedisClient.del(REDIS_CART_PRE+":"+userId);
		return E3Result.ok();
	}

}
