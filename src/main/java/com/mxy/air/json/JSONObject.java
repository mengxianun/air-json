package com.mxy.air.json;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * JSON对象包装器, 内部用一个的map集合存储.
 * 
 * @author mengxiangyun
 *
 */
public class JSONObject extends JSON implements Map<String, Object> {

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
	 * 通过一个key-value对构建JSONObject对象
	 * @param key
	 * @param value
	 */
	public JSONObject(String key, Object value) {
		this();
		map.put(key, value);
	}

	/**
	 * 获取JSON对象的第一个元素
	 * @param key
	 * @return
	 */
	public Entry<String, Object> getFirst() {
		if (size() == 0) return null;
		return this.map.entrySet().iterator().next();
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

	/*
	 * 添加对枚举类型的基本方法, key为枚举名称对应的小写字符串
	 */

	public boolean containsKey(Enum<?> e) {
		return containsKey(e.toString().toLowerCase());
	}

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
	 * 删除指定枚举类型key元素
	 * 
	 * @param key
	 * @return
	 */
	public Object remove(Enum<?> e) {
		return this.map.remove(e.toString().toLowerCase());
	}

	/**
	 * 将另一个JSONObject的属性值覆盖到当前JSONObject对象中, 只覆盖第一级属性
	 * 
	 * @param another
	 * @return
	 */
	public JSONObject merge(JSONObject another) {
		if (another == null) {
			return this;
		}
		for (Entry<String, Object> entry : another.entrySet()) {
			put(entry.getKey(), entry.getValue());
		}
		return this;
	}

	/**
	 * 将另一个JSONObject的属性值覆盖到当前JSONObject对象中
	 * 在有多级嵌套属性的情况下
	 *   如果属性值是JSONObject类型, 则会递归merge
	 *   如果属性值是JSONArray类型, 怎会进行数组合并, 这里只进行基本类型的合并
	 *   其他类型属性值会直接进行覆盖
	 * 
	 * @param another
	 * @return
	 */
	public JSONObject deepMerge(JSONObject another) {
		if (another == null) {
			return this;
		}
		for (Entry<String, Object> entry : another.entrySet()) {
			String key = entry.getKey();
			Object value = entry.getValue();
			if (containsKey(key)) {
				Object originValue = get(key);
				if (originValue instanceof JSONObject && value instanceof JSONObject) {
					JSONObject newValue = ((JSONObject) originValue).merge((JSONObject) value);
					put(key, newValue);
				} else if (originValue instanceof JSONArray && value instanceof JSONArray) {
					JSONArray originValueArray = ((JSONArray) originValue);
					for (Object valueEle : ((JSONArray) value).list()) {
						if (!originValueArray.contains(valueEle)) {
							originValueArray.add(valueEle);
						}
					}
				} else {
					put(key, value);
				}
			} else {
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
	 * 将JSONObject转换成指定类型, 通过setter方法将JSON对象的属性设置到实例对象中的属性中
	 * @param object
	 * @param clazz
	 * @return
	 */
	public <T> T toBean(Class<T> clazz) {
		T t = null;
		try {
			t = clazz.newInstance();
			
			Map<String, Method> methods = new HashMap<>();
			for (Method method : clazz.getMethods()) {
				methods.put(method.getName(), method);
			}

			for (Entry<String, Object> entry : entrySet()) {
				String name = entry.getKey();
				Object value = entry.getValue();
				String methodName = "set" + name.substring(0, 1).toUpperCase() + name.substring(1);
				Method method = methods.get(methodName);
				Method beanMethod = method.getDeclaringClass().getMethod(methodName, method.getParameterTypes());
				beanMethod.setAccessible(true);
				beanMethod.invoke(t, value);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return t;
	}

	@Override
	public int size() {
		return map.size();
	}

	@Override
	public boolean isEmpty() {
		return map.isEmpty();
	}

	@Override
	public boolean containsKey(Object key) {
		return map.containsKey(key);
	}

	@Override
	public boolean containsValue(Object value) {
		return map.containsValue(value);
	}

	@Override
	public Object get(Object key) {
		return map.get(key);
	}

	@Override
	public JSONObject put(String key, Object value) {
		map.put(key, value);
		return this;
	}

	@Override
	public Object remove(Object key) {
		return map.remove(key);
	}

	@Override
	public void putAll(Map<? extends String, ? extends Object> m) {
		map.putAll(m);
	}

	@Override
	public void clear() {
		map.clear();
	}

	@Override
	public Set<String> keySet() {
		return map.keySet();
	}

	@Override
	public Collection<Object> values() {
		return map.values();
	}

	@Override
	public Set<Entry<String, Object>> entrySet() {
		return map.entrySet();
	}

}
