package com.mxy.air.json;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * JSON对象包装器, 内部用一个的map集合存储.
 * 
 * @author mengxiangyun
 *
 */
public class JSONObject {

	private final Map<String, Object> map;

	/**
	 * map初始化为LinkedHashMap类型, 保证元素的顺序
	 */
	public JSONObject() {
		this.map = new LinkedHashMap<>();
	}

	/**
	 * 通过Map构建一个JSONObject对象
	 * 
	 * @param copyFrom
	 */
	public JSONObject(Map<?, ?> copyFrom) {
		this();
		Map<?, ?> typed = (Map<?, ?>) copyFrom;
		for (Entry<?, ?> entry : typed.entrySet()) {
			String key = (String) entry.getKey();
			if (key == null) {
				throw new JSONException("key is null");
			}
			put(key, wrap(entry.getValue()));
		}
	}
	
	/**
	 * 通过Bean getters方法从Java Bean中构建一个JSONObject对象
	 * 方法必须是公共的, 不能是静态的, 必须以get或is开头
	 * @param bean
	 */
	public JSONObject(Object bean) {
		this();
		Method[] methods = bean.getClass().getDeclaredMethods();
		for (Method method : methods) {
			int modifiers = method.getModifiers();
			if (Modifier.isPublic(modifiers) 
					&& !Modifier.isStatic(modifiers) 
					&& method.getParameterTypes().length == 0 
					&& method.getReturnType() != Void.TYPE) {
				String name = method.getName();
				String field;
				if (name.startsWith("get")) {
					field = name.substring(3);
				} else if (name.startsWith("is")) {
					field = name.substring(2);
				} else {
					continue;
				}
				if (field.length() > 0) {
					field = field.substring(0, 1).toLowerCase() + field.substring(1);
					try {
						Object result = method.invoke(bean);
						put(field, wrap(result));
					} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
						// ignore
					}
				}
				
			}
		}
	}

	/**
	 * 通过JSON字符串构建JSONObject对象
	 * 
	 * @param source
	 *            JSON字符串
	 * @throws IOException
	 */
	public JSONObject(String source) {
		this(new JSONTokener(source));

	}

	public JSONObject(JSONTokener tokener) {
		this.map = tokener.readObject().map;
	}

	/**
	 * 返回JSON对象元素集合
	 * 
	 * @return
	 */
	public Set<Entry<String, Object>> entrySet() {
		return this.map.entrySet();
	}

	/**
	 * 返回JSON对象顶级Key的集合
	 * 
	 * @return
	 */
	public Set<String> keySet() {
		return this.map.keySet();
	}

	/**
	 * JSON对象长度
	 * 
	 * @return
	 */
	public int size() {
		return this.map.size();
	}

	/**
	 * 返回指定key的value
	 * 
	 * @param key
	 * @return
	 */
	public Object get(String key) {
		return this.map.get(key);
	}

	public String getString(String key) {
		Object object = get(key);
		return object == null ? null : object.toString();
	}

	public boolean getBoolean(String key) {
		Object object = get(key);
		if (object instanceof Boolean) {
			return (Boolean) object;
		} else {
			String value = object.toString();
			if (value.equalsIgnoreCase("true")) {
				return true;
			} else if (value.equalsIgnoreCase("false")) {
				return false;
			} else {
				throw new JSONException("com.mxy.air.json.JSONObject [\"" + key + "\"] 不是布尔类型");
			}
		}
	}

	public long getLong(String key) {
		Object object = get(key);
		return object instanceof Number ? ((Number) object).longValue() : Long.parseLong((String) object);
	}

	public int getInt(String key) {
		Object object = get(key);
		return object instanceof Number ? ((Number) object).intValue() : Integer.parseInt((String) object);
	}

	public double getDouble(String key) {
		Object object = get(key);
		return object instanceof Number ? ((Number) object).doubleValue() : Double.parseDouble((String) object);
	}

	/**
	 * 返回JSONObject对象
	 * 
	 * @param key
	 * @return
	 */
	public JSONObject getObject(String key) {
		Object object = get(key);
		try {
			return (JSONObject) object;
		} catch (Exception e) {
			throw new JSONException("com.mxy.air.json.JSONObject [\"" + key + "\"] 不是JSONObject类型");
		}
	}

	/**
	 * 返回JSONArray对象
	 * 
	 * @param key
	 * @return
	 */
	public JSONArray getArray(String key) {
		Object object = get(key);
		try {
			return (JSONArray) object;
		} catch (Exception e) {
			throw new JSONException("com.mxy.air.json.JSONObject [\"" + key + "\"] 不是JSONArray类型");
		}
	}

	/**
	 * 判断key是否存在
	 * 
	 * @param key
	 * @return
	 */
	public boolean containsKey(String key) {
		return this.map.containsKey(key);
	}

	/**
	 * 添加对枚举类型的基本方法, key为枚举名称对应的小写字符串
	 * 
	 * @param e
	 * @return
	 */
	public Object get(Enum<?> e) {
		return get(e.toString().toLowerCase());
	}

	public String getString(Enum<?> e) {
		return getString(e.toString().toLowerCase());
	}
	
	public boolean getBoolean(Enum<?> e) {
		return getBoolean(e.toString().toLowerCase());
	}
	
	public long getLong(Enum<?> e) {
		return getLong(e.toString().toLowerCase());
	}
	
	public int getInt(Enum<?> e) {
		return getInt(e.toString().toLowerCase());
	}
	
	public double getDouble(Enum<?> e) {
		return getDouble(e.toString().toLowerCase());
	}

	public JSONObject getObject(Enum<?> e) {
		return getObject(e.toString().toLowerCase());
	}

	public JSONArray getArray(Enum<?> e) {
		return getArray(e.toString().toLowerCase());
	}

	public boolean containsKey(Enum<?> e) {
		return containsKey(e.toString().toLowerCase());
	}

	/**
	 * 添加key-value对
	 * 
	 * @param key
	 * @param value
	 */
	public JSONObject put(String key, Object value) {
		this.map.put(key, value);
		return this;
	}
	
	/**
	 * 添加枚举类型key的key-value对
	 * 
	 * @param key
	 * @param value
	 */
	public JSONObject put(Enum<?> e, Object value) {
		this.map.put(e.toString().toLowerCase(), value);
		return this;
	}

	/**
	 * 删除指定key元素
	 * 
	 * @param key
	 * @return
	 */
	public Object remove(String key) {
		return this.map.remove(key);
	}
	
	/**
	 * 删除指定枚举类型key元素
	 * 
	 * @param key
	 * @return
	 */
	public Object remove(Enum<?> e) {
		return this.map.remove(e.toString().toLowerCase());
	}

	/**
	 * 将另一个JSONObject的同名属性值覆盖到当前JSONObject对象中
	 * 
	 * @param another
	 * @return
	 */
	public JSONObject merge(JSONObject another) {
		if (another == null) {
			return this;
		}
		for (Entry<String, Object> entry : another.entrySet()) {
			String key = entry.getKey();
			Object value = entry.getValue();
			if (containsKey(key)) {
				put(key, value);
			}
		}
		return this;
	}

	/**
	 * 输出json字符串, 格式紧凑, 只有一行
	 */
	@Override
	public String toString() {
		return toString(0);
	}

	/**
	 * 输出格式化的json字符串
	 * 
	 * @param indentSpaces
	 *            缩进大小
	 * @return
	 */
	public String toString(int indentSpaces) {
		return new JSONStringer(indentSpaces).object(this);
	}

	/**
	 * 包装一个对象 如果对象是一个Map, 则用JSONObject包装它 如果对象是一个数组或集合, 则用JSONArray包装它
	 * 如果对象来自java包或javax包, 则转换为字符串 其他情况返回对象本身 对象为null返回null
	 * 对象为JSONObject或JSONArray类型不需要包装, 直接返回 对象为原始包装器类型, 直接返回
	 * 
	 * @param object
	 * @return
	 */
	public static Object wrap(Object object) {
		if (object == null) {
			return null;
		}
		if (object instanceof Map) {
			Map<?, ?> map = (Map<?, ?>) object;
			new JSONObject(map);
		} else if (object instanceof Collection) {
			Collection<?> collection = (Collection<?>) object;
			return new JSONArray(collection);
		} else if (object.getClass().isArray()) {
			return new JSONArray(object);
		}
		return object;
	}

	/**
	 * 返回JSONObject内部Map对象
	 * 
	 * @return
	 */
	public Map<String, Object> map() {
		return this.map;
	}

	/**
	 * 将JSONObject转换成原始类型的Map对象
	 * 
	 * @return
	 */
	public Map<String, Object> toMap() {
		Map<String, Object> result = new HashMap<>();
		for (Entry<String, Object> entry : this.map.entrySet()) {
			Object value = entry.getValue();
			if (value instanceof JSONObject) {
				value = ((JSONObject) value).toMap();
			} else if (value instanceof JSONArray) {
				value = ((JSONArray) value).toList();
			}
			result.put(entry.getKey(), value);
		}
		return result;
	}
	
	/**
	 * 将JSONObject转换成指定类型, 通过setter方法将json对象的属性设置到实例对象中的属性中
	 * @param object
	 * @param clazz
	 * @return
	 */
	public <T> T toBean(Class<T> clazz) {
		T t = null;
		try {
			t = clazz.newInstance();
			
			for (Entry<String, Object> entry : entrySet()) {
				String name = entry.getKey();
				Object value = entry.getValue();
				String methodName = "set" + name.substring(0, 1).toUpperCase() + name.substring(1);
				Method method = clazz.getMethod(methodName, value.getClass());
				method.setAccessible(true);
				method.invoke(t, value);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return t;
	}

}
