package com.catination.server.util;
import java.util.Map;
import java.util.Set;

public class JSONUtil {
	
	static String number2Json(Number number) {
		return number.toString();
	}

	static String boolean2Json(Boolean bool) {
		return bool.toString();
	}

	static String string2Json(String string) {
		return "\""+string+"\"";
	}
	
	static String array2Json(Object[] array) {
		if (array.length == 0)
		{
			return "[]";
		}
		StringBuilder sb = new StringBuilder(array.length << 4);
		sb.append('[');
		for(Object o:array)
		{
			sb.append(toJson(o));
			sb.append(',');
		}
		//将最后添加','，变为']'
		sb.setCharAt(sb.length() - 1,']');
		return sb.toString();
	}
	
	static String map2Json(Map<String, Object> map) {
		if (map.isEmpty())
			return "{}";
		StringBuilder sb  = new StringBuilder(map.size() << 4);
		sb.append('{');
		Set<String> keys = map.keySet();
		for(String key : keys)
		{
			Object value = map.get(key);
			sb.append('\"');
			sb.append(key);
			sb.append('\"');
			sb.append(':');
			sb.append(toJson(value));
			sb.append(',');
		}
		//将最后的','变为'}'
		sb.setCharAt(sb.length()-1 , '}');
		return sb.toString();
	}
	
	public static String toJson(Object o) {
		if (o == null)
			return "null";
		if (o instanceof String)
			return string2Json((String)o);
		if (o instanceof Boolean)
			return boolean2Json((Boolean)o);
		if (o instanceof Number)
			return number2Json((Number)o);
		if (o instanceof Map)
			return map2Json((Map<String,Object>)o);
		if (o instanceof Object[])
			return array2Json((Object[])o);
		throw new RuntimeException("Unsupported type:"+o.getClass().getName());
	}
}
