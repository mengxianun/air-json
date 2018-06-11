package com.mxy.air.json;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.stream.Stream;

public class JSON {

	/**
	 * 读取JSON文件, 返回文件内容字符串
	 * @param jsonFile
	 * @return
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	public static String read(String jsonFile) throws IOException, URISyntaxException {
		Path path = getPath(jsonFile);
		if (path == null) return null;
		return new String(Files.readAllBytes(path), Charset.defaultCharset());
	}

	/**
	 * 从JSON文件中读取JSON对象
	 * @param jsonFile
	 * @return
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	public static JSONObject readObject(String jsonFile) throws IOException, URISyntaxException {
		String json = read(jsonFile);
		return json == null ? null : new JSONObject(json);
	}

	public static JSONArray readArray(String jsonFile) throws IOException, URISyntaxException {
		String json = read(jsonFile);
		return json == null ? null : new JSONArray(json);
	}

	/**
	 * 从JSON文件中读取JSON数组, 如果是文件夹, 会递归读取文件夹里面的文件
	 * @param jsonFile
	 * @return
	 * @throws IOException 
	 * @throws URISyntaxException 
	 */
	public static JSONArray readDirectory(String jsonFile) throws IOException, URISyntaxException {
		Path path = getPath(jsonFile);
		if (path == null) return null;
		JSONArray jsonArray = new JSONArray();
		// 遍历文件夹的文件
		try (Stream<Path> stream = Files.list(path)) {
			stream.filter(Files::isRegularFile).forEach(subFile -> {
				try {
					String subFileString = new String(Files.readAllBytes(subFile), Charset.defaultCharset());
					if (subFileString.isEmpty()) return;
					jsonArray.add(toJSON(subFileString));
				} catch (IOException e) {
					throw new JSONException(e);
				}
			});
		}
		return jsonArray;
	}

	/**
	 * 获取文件路径
	 * @param jsonFile 文件路径字符串
	 * @return
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	public static Path getPath(String jsonFile) throws IOException, URISyntaxException {
		if (jsonFile == null) {
			return null;
		}
		File file = new File(jsonFile);
		Path path = null;
		if (file.isAbsolute()) { // 绝对路径
			path = Paths.get(jsonFile);
		} else {
			URL url = JSON.class.getClassLoader().getResource(jsonFile);
			if (url == null) {
				return null;
			}
			URI uri = url.toURI();
			if (uri.toString().startsWith("jar:file:/") && uri.toString().indexOf("jar!") != -1) { // jar
				String[] pathArray = url.toString().split("!", 2);
				try (FileSystem fileSystem = FileSystems.newFileSystem(URI.create(pathArray[0]), new HashMap<>())) {
					path = fileSystem.getPath(pathArray[1].replaceAll("!", ""));
				}
			} else {
				path = Paths.get(uri);
			}
		}
		return path;
	}

	/**
	 * JSON字符串转JSON对象或JSON数组
	 * @param jsonString
	 * @return
	 */
	public static Object toJSON(String jsonString) {
		if (jsonString == null) return null;
		if (jsonString.startsWith("{") && jsonString.endsWith("}")) {
			return new JSONObject(jsonString);
		} else if (jsonString.startsWith("[") && jsonString.endsWith("]")) {
			return new JSONArray(jsonString);
		}
		return jsonString;
	}

	public static boolean isEmpty(String json) {
		if (json == null) {
			return true;
		}
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
