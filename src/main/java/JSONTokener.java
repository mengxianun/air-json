/**
 * 将json字符串解析为相应对象
 * 遵循标准的json语法. 除此之外, 还支持以下语法
 * 		-行注释"//"或'#'
 * 		-注释/* * /
 * 		-没有引号或单引号的字符串
 * 		-数组元素分隔符';'
 * 		-key-value之间的分隔符'=', '=>'
 * 		-key-value对的分隔符';'
 * 
 * @author mengxiangyun
 *
 */
public class JSONTokener {

	/* json字符串 */
	private final String json;

	/* 当前读取的字符的位置 */
	private int pos;

	/* 当前读取的字符 */
	private char c;

	/* 字符串的长度 */
	private int length;

	public JSONTokener(String json) {
		this.json = json;
		this.pos = 0;
		this.length = json.length();
	}

	/**
	 * 读取一个字符, 读取后索引移到下一个字符位置
	 * 
	 * @return
	 */
	private char read() {
		return json.charAt(pos++);
	}

	/**
	 * 读取一段连续字符
	 * 
	 * @param length
	 * @return
	 */
	private String next(int length) {
		if (pos + length > this.length) {
			throw new JSONException("超出长度范围");
		}
		String str = json.substring(pos, pos + length);
		pos += length;
		return str;
	}

	/**
	 * 读取下一个非空字符
	 * 
	 * @return
	 */
	private char nextCleanInternal() {
		while (pos < length) {
			switch (c = read()) {
			case ' ':
			case '\t':
			case '\n':
			case '\r':
				continue;
			case '/':
				switch (read()) {
				case '*':
					// 跳过注释/* */
					int commentEnd = json.indexOf("*/", pos);
					pos = commentEnd + 2;
					continue;
				case '/':
					// 跳过行注释"//"
					skipToNextLine();
					continue;

				default:
					pos--;
					return c;
				}
			case '#':
				// 跳过行注释'#'
				skipToNextLine();
				continue;

			default:
				return c;
			}
		}
		throw new JSONException("json已读取结束");
	}

	/**
	 * 读取下一个值. 读取后pos在value值的下一个索引位置 值可能是对象, 数组, 字符串, 或者其他类型.
	 * 
	 * @return
	 */
	public Object nextValue() {
		switch (c = nextCleanInternal()) {
		case '{':
			pos--;
			return readObject();
		case '[':
			pos--;
			return readArray();
		case '\'':
		case '"':
			return nextString(c);
		default:
			pos--;
			Object literal = readLiteral();
			/**
			 * readLiteral()方法执行后, pos在分隔符的下一个字符位置. 由于该值没有被特定字符包裹, 所以调用next*()方法时将跳过分隔符,
			 * 导致解析不到分隔符而失败 为了避免这种情况, 这里将pos往前移动一个位置
			 */
			pos--;
			return literal;
		}
	}

	/**
	 * 读取下一个字符串
	 * 
	 * @param quote
	 *            包裹字符串的引号, 单引号或双引号
	 * @return
	 */
	private String nextString(char quote) {
		StringBuffer sb = new StringBuffer();
		over: while (pos < length) {
			switch (c = read()) {
			case '\\':
				switch (c = read()) {
				case 'u':
					if (pos + 4 > length) {
						throw new JSONException("未终止的转义字符序列");
					}
					sb.append((char) Integer.parseInt(next(4), 16));

				case 't':
					sb.append('\t');
					break;
				case 'b':
					sb.append('\b');
					break;
				case 'n':
					sb.append('\n');
					break;
				case 'r':
					sb.append('\r');
					break;
				case 'f':
					sb.append('\f');
					break;

				case '\'':
				case '"':
				case '\\':
				default:
					sb.append(c);
					break;
				}

			default:
				if (c == quote) {
					break over;
				}
				sb.append(c);
			}
		}
		return sb.toString();
	}

	/**
	 * 跳到下一行
	 */
	private void skipToNextLine() {
		while ((c = read()) != '\r' && c != '\n') {
		}
	}

	/**
	 * 读取下一个json对象, 以'{'字符开始, 以'}'字符结束.
	 * 
	 * @return
	 */
	public JSONObject readObject() {
		JSONObject object = new JSONObject();

		read(); // 读取开头'{'

		while (pos < length) {
			switch (nextCleanInternal()) {
			case '}':
				return object;
			case ',':
			case ';':
				continue;

			default:
				pos--;
			}

			Object key = nextValue();
			if (key == null)
				throw new JSONException("key 不能为空");

			String keyStr = String.valueOf(key);

			// key-value分隔符, 可以是':', '=', '=>'
			char separator = nextCleanInternal();
			if (separator != ':' && separator != '=') {
				throw new JSONException("key-value 分隔符必须是':'或'='");
			}
			if (pos < length && json.charAt(pos) == '>') {
				pos++;
			}
			object.put(keyStr, nextValue());

			switch (nextCleanInternal()) {
			case '}':
				return object;
			case ',':
			case ';':
				continue;

			default:
				throw new JSONException("未终止的对象");
			}

		}
		return object;

	}

	/**
	 * 读取下一个json数组, 以'['字符开始, 以']'字符结束.
	 * 
	 * @return
	 */
	public JSONArray readArray() {
		JSONArray array = new JSONArray();

		read(); // 读取开头'['

		while (pos < length) {
			switch (nextCleanInternal()) {
			case ']':
				return array;
			case ',':
			case ';':
				continue;

			default:
				pos--;
			}

			array.add(nextValue());

			switch (nextCleanInternal()) {
			case ']':
				return array;
			case ',':
			case ';':
				continue;

			default:
				throw new JSONException("未终止的数组");
			}

		}
		return array;
	}

	/**
	 * 读取下一个值, 可能是null, boolean, numeric, 未被引号包裹的字符串.
	 * 
	 * @return
	 */
	private Object readLiteral() {
		StringBuilder sb = new StringBuilder();

		// 读取值, 值可以包含空格
		while ((c = read()) >= ' ' && "{}[]/\\:,=;#".indexOf(c) < 0) {
			sb.append(c);
		}

		String literal = sb.toString().trim();
		if (literal.length() == 0) {
			throw new JSONException("在位置[" + (pos - 1) + "]缺失值");
		}

		if (literal.equalsIgnoreCase("null")) {
			return null;
		}
		if (literal.equalsIgnoreCase("true")) {
			return Boolean.TRUE;
		}
		if (literal.equalsIgnoreCase("false")) {
			return Boolean.FALSE;
		}

		// 数值类型
		char initial = literal.charAt(0);
		if ((initial >= '0' && initial <= '9') || initial == '-' || initial == '+') {
			if (isDecimal(literal)) { // 小数
				return Double.valueOf(literal);
			} else {
				long l = Long.valueOf(literal);
				if (l >= Integer.MIN_VALUE && l <= Integer.MAX_VALUE) {
					return (int) l;
				}
				return l;
			}
		}
		// 如果以上类型都不匹配, 则返回字符串
		return literal;

	}

	/**
	 * 是否是小数
	 * 
	 * @param str
	 * @return
	 */
	private boolean isDecimal(String str) {
		return str.contains(".") || str.contains("e") || str.contains("E") || str.contains("-0");
	}

}
