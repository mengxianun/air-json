package com.mxy.air.json;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * json数组包装器, 内部用一个list集合存储
 * 
 * @author mengxiangyun
 *
 */
public class JSONArray {

	private final List<Object> list;

	public JSONArray() {
		this.list = new ArrayList<Object>();
	}

	/**
	 * 通过一个集合数组构建JSONArray
	 * 
	 * @param collection
	 */
	public JSONArray(Collection<?> collection) {
		this();
		for (Object object : collection) {
			if (object != null) {
				list.add(object);
			}
		}
	}

	/**
	 * 通过一个json字符串构建JSONArray
	 * 
	 * @param source
	 */
	public JSONArray(String source) {
		this(new JSONTokener(source));
	}

	public JSONArray(JSONTokener tokener) {
		this.list = tokener.readArray().list;
	}

	/**
	 * 通过一个数组构建JSONArray
	 * 
	 * @param array
	 */
	public JSONArray(Object array) {
		if (!array.getClass().isArray()) {
			throw new JSONException(array.getClass() + "不是一个数组");
		}
		int length = Array.getLength(array);
		this.list = new ArrayList<>(length);
		for (int i = 0; i < length; i++) {
			this.list.add(JSONObject.wrap(Array.get(array, i)));
		}

	}

	/**
	 * 返回元素的迭代器
	 * 
	 * @return
	 */
	public Iterator<Object> iterator() {
		return this.list.iterator();
	}

	/**
	 * 返回指定位置的元素
	 * 
	 * @param index
	 * @return
	 */
	public Object get(int index) {
		return this.list.get(index);
	}

	/**
	 * 添加对象
	 * 
	 * @param value
	 * @return
	 */
	public JSONArray add(Object value) {
		this.list.add(value);
		return this;
	}

	/**
	 * 添加对象
	 * 
	 * @param value
	 * @return
	 */
	public JSONArray addAll(Collection<?> c) {
		this.list.addAll(c);
		return this;
	}

	/**
	 * JSON数组长度
	 * 
	 * @return
	 */
	public int length() {
		return this.list.size();
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
		return new JSONStringer(indentSpaces).array(this);
	}

	/**
	 * 返回JSONArray内部的List对象
	 * 
	 * @return
	 */
	public List<Object> list() {
		return this.list;
	}

	/**
	 * 将JSONArray转换为原始类型的List对象
	 * 
	 * @return
	 */
	public List<Object> toList() {
		List<Object> result = new ArrayList<>();
		for (Object object : list) {
			if (object instanceof JSONObject) {
				object = ((JSONObject) object).toMap();
			} else if (object instanceof JSONArray) {
				object = ((JSONArray) object).toList();
			}
			result.add(object);
		}
		return result;
	}

	/**
	 * 将JSONArray转换为Object[]
	 * 
	 * @return
	 */
	public Object[] array() {
		return list.toArray();
	}

	/**
	 * 将JSONArray转换为原始类型的数组
	 * 
	 * @return
	 */
	public Object[] toArray() {
		return toList().toArray();
	}

	/**
	 * 将JSONArray转换为String[]
	 * 
	 * @return
	 */
	public String[] toStringArray() {
		if (length() == 0) {
			return new String[0];
		}
		String[] result = new String[length()];
		for (int i = 0; i < length(); i++) {
			result[i] = list.get(i).toString();
		}
		return result;
	}

}
