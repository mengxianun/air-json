public class JSON {

	public static boolean isEmpty(String json) {
		json = json.trim();
		if (json.startsWith("{") && json.endsWith("}")) {
			json = json.substring(json.indexOf("{"), json.lastIndexOf("}"));
		} else if (json.startsWith("[") && json.endsWith("]")) {
			json = json.substring(json.indexOf("["), json.lastIndexOf("]"));
		}
		if (json.trim().length() > 0) {
			return false;
		} else {
			return true;
		}
	}

	public static boolean isEmpty(JSONObject json) {
		return json == null || json.size() == 0;
	}

	public static boolean isEmpty(JSONArray json) {
		return json == null || json.length() == 0;
	}

}
