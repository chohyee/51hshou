package cn.wells.utils;

import java.util.UUID;

public class CommonsUtils {
	//生成uid的方法
	public static String getUUID(){
		return UUID.randomUUID().toString();
	}
}
