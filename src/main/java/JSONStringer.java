import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

/**
 * 格式化json对象或数组到字符串, 可以指定缩进的大小
 * 
 * @author mengxiangyun
 *
 */
public class JSONStringer {

	/* 输出数据 */
	private final StringBuilder out = new StringBuilder();

	/* 缩进字符串 */
	private final String indent;

	/* 对象嵌入层级 */
	private int level = 0;

	public JSONStringer() {
		this(0);
	}

	/**
	 * 构造函数指定字符串格式化缩进的数量
	 * 
	 * @param indentSpaces
	 */
	public JSONStringer(int indentSpaces) {
		char[] indentChars = new char[indentSpaces];
		Arrays.fill(indentChars, ' ');
		this.indent = new String(indentChars);
	}

	/**
	 * 格式化JSONArray
	 * 
	 * @param array
	 * @return
	 */
	public String array(JSONArray array) {
		out.append('[');
		level++;
		newLine();

		boolean comma = false;
		Iterator<Object> iterator = array.iterator();
		while (iterator.hasNext()) {
			Object object = iterator.next();
			if (comma) {
				out.append(',');
				newLine();
			}
			value(object);
			comma = true;
		}
		level--;
		newLine();
		out.append(']');
		return toString();
	}

	/**
	 * 格式化JSONObject
	 * 
	 * @param object
	 * @return
	 */
	public String object(JSONObject object) {
		out.append('{');
		level++;
		newLine();

		boolean comma = false;
		for (Entry<String, Object> entry : object.entrySet()) {
			if (comma) {
				out.append(',');
				newLine();
			}
			string(entry.getKey());
			out.append(':');
			// 如果指定了缩进格式, key-value之间的分隔符后面添加一个空格, 美化输出
			if (indent.length() > 0) {
				out.append(' ');
			}
			value(entry.getValue());
			comma = true;
		}
		level--;
		newLine();
		out.append('}');
		return toString();
	}

	/**
	 * 格式化Object
	 * 
	 * @param value
	 * @return
	 */
	public String value(Object value) {
		if (value == null) {
			out.append("null");
		} else if (value instanceof Number) {
			String valueStr = value.toString();
			// 通过BigDecimal的构造器验证数值格式
			new BigDecimal(valueStr);
			out.append(valueStr);
		} else if (value instanceof Boolean) {
			out.append(value.toString());
		} else if (value instanceof Map) {
			Map<?, ?> map = (Map<?, ?>) value;
			object(new JSONObject(map));
		} else if (value instanceof Collection) {
			Collection<?> collection = (Collection<?>) value;
			array(new JSONArray(collection));
		} else if (value.getClass().isArray()) {
			array(new JSONArray(value));
		} else if (value instanceof JSONObject) {
			object((JSONObject) value);
		} else if (value instanceof JSONArray) {
			array((JSONArray) value);
		} else {
			string(String.valueOf(value));
		}
		return toString();
	}

	/**
	 * 处理字符串类型, 用引号包裹
	 * 
	 * @param value
	 */
	private void string(String value) {
		out.append('"');
		for (int i = 0; i < value.length(); i++) {
			char c = value.charAt(i);
			switch (c) {
			case '"':
			case '\\':
				out.append('\\').append(c);
				break;

			case '\t':
				out.append("\\t");
				break;

			case '\b':
				out.append("\\b");
				break;

			case '\n':
				out.append("\\n");
				break;

			case '\r':
				out.append("\\r");
				break;

			case '\f':
				out.append("\\f");
				break;

			default:
				if (c <= 0x1F) {
					out.append(String.format("\\u%04x", (int) c));
				} else {
					out.append(c);
				}
				break;
			}
		}
		out.append('"');
	}

	/**
	 * 输出下一行并添加缩进字符
	 */
	private void newLine() {
		if (indent.length() > 0) {
			out.append('\n');
			for (int i = 0; i < level; i++) {
				out.append(indent);
			}
		}

	}

	/**
	 * 输出字符串
	 */
	public String toString() {
		return out.toString();
	}

}
