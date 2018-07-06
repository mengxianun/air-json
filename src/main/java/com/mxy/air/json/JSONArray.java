package com.mxy.air.json;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

/**
 * json数组包装器, 内部用一个list集合存储
 * 
 * @author mengxiangyun
 *
 */
public class JSONArray extends JSON implements List<Object> {

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
				result.add(((JSONObject) object).toMap());
			} else if (object instanceof JSONArray) {
				result.add(((JSONArray) object).toList());
			} else {
				result.add(object);
			}
		}
		return result;
	}

	/**
	 * 将JSONArray转换为Map List对象
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<Map<String, Object>> toMapList() {
		List<Map<String, Object>> result = new ArrayList<>();
		for (Object object : list) {
			if (object instanceof JSONObject) {
				result.add(((JSONObject) object).toMap());
			} else if (object instanceof Map) {
				result.add((Map<String, Object>) object);
			}
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
		if (size() == 0) {
			return new String[0];
		}
		String[] result = new String[size()];
		for (int i = 0; i < size(); i++) {
			result[i] = list.get(i).toString();
		}
		return result;
	}

	@Override
	public int size() {
		return list.size();
	}

	@Override
	public boolean isEmpty() {
		return list.isEmpty();
	}

	@Override
	public boolean contains(Object o) {
		return list.contains(o);
	}

	@Override
	public Iterator<Object> iterator() {
		return list.iterator();
	}

	@Override
	public <T> T[] toArray(T[] a) {
		return null;
	}

	@Override
	public boolean add(Object e) {
		return list.add(e);
	}

	@Override
	public boolean remove(Object o) {
		return list.remove(o);
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		return list.containsAll(c);
	}

	@Override
	public boolean addAll(Collection<? extends Object> c) {
		return list.addAll(c);
	}

	@Override
	public boolean addAll(int index, Collection<? extends Object> c) {
		return list.addAll(index, c);
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		return list.removeAll(c);
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		return list.retainAll(c);
	}

	@Override
	public void clear() {
		list.clear();
	}

	@Override
	public Object get(int index) {
		return list.get(index);
	}

	@Override
	public Object set(int index, Object element) {
		return list.set(index, element);
	}

	@Override
	public void add(int index, Object element) {
		list.add(index, element);
	}

	@Override
	public Object remove(int index) {
		return list.remove(index);
	}

	@Override
	public int indexOf(Object o) {
		return list.indexOf(o);
	}

	@Override
	public int lastIndexOf(Object o) {
		return list.lastIndexOf(o);
	}

	@Override
	public ListIterator<Object> listIterator() {
		return list.listIterator();
	}

	@Override
	public ListIterator<Object> listIterator(int index) {
		return list.listIterator(index);
	}

	@Override
	public List<Object> subList(int fromIndex, int toIndex) {
		return list.subList(fromIndex, toIndex);
	}

}
