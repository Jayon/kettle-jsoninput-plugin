package com.renren.games.data.tech.kettle.plugin.jsoninput;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONValue;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.i18n.BaseMessages;

import com.alibaba.fastjson.JSON;

public class JsonReader {
	private static Class<?> PKG = RRJsonInputMeta.class; // for i18n purposes,
	// needed by
	// Translator2!!
	// $NON-NLS-1$
	private boolean ignoreMissingPath;
	private Object jsonValue = null;
	private final String tagPathSeparator = ":";// set json field separator

	public JsonReader() throws KettleException {
		init();
		this.ignoreMissingPath = false;
	}

	public void SetIgnoreMissingPath(boolean value) {
		this.ignoreMissingPath = value;
	}

	private void init() throws KettleException {

	}

	public void readFile(String filename) throws KettleException {
		FileReader fr = null;
		try {
			fr = new FileReader(filename);
			Object o = JSONValue.parse(fr);
			if (o == null) {
				throw new KettleException(BaseMessages.getString(PKG,
						"JsonReader.Error.ParsingFile", filename));
			}
			eval(o);
		} catch (Exception e) {
			throw new KettleException(e);
		} finally {
			try {
				if (fr != null)
					fr.close();
			} catch (Exception e) {
			}
		}
	}

	public void readString(String value) throws KettleException {
		try {
			// Object o = JSONValue.parse(value);
			Object o = JSON.parse(value);
			if (o == null) {
				throw new KettleException(BaseMessages.getString(PKG,
						"JsonReader.Error.ParsingString", value));
			}
			eval(o);
		} catch (Exception e) {
			throw new KettleException(e);
		}
	}

	public void readUrl(String value) throws KettleException {
		InputStreamReader is = null;
		BufferedReader br = null;
		try {
			URL url = new URL(value);
			is = new InputStreamReader(url.openConnection().getInputStream());
			Object o = JSONValue.parse(is);
			if (o == null) {
				throw new KettleException(BaseMessages.getString(PKG,
						"JsonReader.Error.ParsingString", value));
			}
			eval(o);
		} catch (Exception e) {
			throw new KettleException(e);
		} finally {
			try {
				if (br != null)
					br.close();
				if (is != null)
					is.close();
			} catch (Exception e) {
			}
		}
	}

	private void eval(Object o) throws Exception {
		this.jsonValue = o;
	}

	public List<String> getPath(String tagPath) throws KettleException {
		List<String> value = new ArrayList<String>();
		if (this.jsonValue == null) {
			return value;
		}
		String[] path = tagPath.split(this.tagPathSeparator);
		getJsonValue(this.jsonValue, path, 0, value);
		return value;
	}

	public static boolean isEmpty(Object obj) {
		boolean result = true;
		if (obj == null) {
			return true;
		}
		if (obj instanceof String) {
			result = (obj.toString().trim().length() == 0)
					|| obj.toString().trim().equals("null");
		} else if (obj instanceof Collection) {
			result = ((Collection<?>) obj).size() == 0;
		} else {
			result = ((obj == null) || (obj.toString().trim().length() < 1)) ? true
					: false;
		}
		return result;
	}

	private static void getJsonValue(Object node, String[] path, int offset,
			List<String> results) {
		if (isEmpty(node)) {
			return;
		}
		Object curNode = node;
		for (int i = offset; i < path.length; i++) {
			String p = path[i];
			if (curNode instanceof Map) {
				// parse map object
				@SuppressWarnings("unchecked")
				Map<String, String> jsonObject = (Map<String, String>) curNode;
				Object value = jsonObject.get(p);
				if (value == null) {
					return;
				}
				if (i == path.length - 1) {
					results.add(value.toString());
					return;
				} else {
					curNode = value;
				}
			} else if (curNode instanceof List) {
				parseArray(path, results, (List<?>) curNode, i);
				return;
			} else {
				throw new RuntimeException("Not a valid json object:" + curNode);
			}
		}
	}

	/**
	 * @param path
	 * @param results
	 * @param root
	 * @param offset
	 */
	private static void parseArray(String[] path, List<String> results,
			List<?> root, int offset) {
		for (int j = 0; j < root.size(); j++) {
			Object o = root.get(j);
			getJsonValue(o, path, offset, results);
		}
	}

	public boolean isIgnoreMissingPath() {
		return this.ignoreMissingPath;
	}

}