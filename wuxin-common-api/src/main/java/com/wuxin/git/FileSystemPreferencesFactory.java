package com.wuxin.git;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.naming.NamingException;
import javax.naming.Reference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.wuxin.preference.Preferences;
import com.wuxin.preference.PreferencesFactory;
import com.wuxin.preference.PreferencesFactoryLoader;
import com.wuxin.utils.PlatformClientUtils;

public class FileSystemPreferencesFactory extends PreferencesFactory {
	private static final Logger logger = LoggerFactory.getLogger(FileSystemPreferencesFactory.class);

	private final String filePath;

	private Map<String, Item> items = new LinkedHashMap<>();

	public FileSystemPreferencesFactory(String filePath) {
		this.filePath = filePath;
		try {
			load();
			logger.info("load file filePath:{} fileList:{}",filePath,this.items.keySet());
		} catch (IOException e) {
			logger.error("load file error,filePath:{},fail:{}",filePath,e);
			throw new RuntimeException(e);
		}

		PreferencesFactoryLoader.compareAndSetPreferencesFactory(null, this);
	}

	public Reference getReference() throws NamingException {
		return null;
	}

	private void load() throws IOException {
		if (this.filePath.startsWith("http://")) {
			if ((this.filePath.endsWith("zip")) || (this.filePath.endsWith("jar"))) {
				URL url = new URL(this.filePath);
				HttpURLConnection conn = (HttpURLConnection) url.openConnection();
				conn.connect();
				InputStream in = null;
				try {
					in = (InputStream) conn.getContent();
					loadZip(in);
					conn.disconnect();
				} finally {
					PlatformClientUtils.close(in);
				}
				return;
			}
		}

		File file = new File(this.filePath);

		if (!file.exists()) {
			throw new IllegalStateException("装载配置出错, 文件 '" + this.filePath + "' 不存在");
		}

		if (file.isDirectory()) {
			for (File itemFile : file.listFiles()) {
				if (itemFile.isDirectory()) {
					loadItem(itemFile);
				}
			}
			return;
		}

		if ((file.isFile()) && ((file.getName().endsWith(".zip")) || (file.getName().endsWith(".jar")))) {
			InputStream in = null;
			try {
				in = new FileInputStream(file);
				loadZip(in);
			} finally {
				PlatformClientUtils.close(in);
			}
			return;
		}
	}

	private void loadZip(InputStream in) throws FileNotFoundException, IOException {
		ZipInputStream zipIn = null;
		try {
			zipIn = new ZipInputStream(in);
			for (;;) {
				ZipEntry zipEntry = zipIn.getNextEntry();
				if (zipEntry == null)
					break;
				String entryName = zipEntry.getName();

				int level = 0;
				for (int i = 0; i < entryName.length(); i++) {
					char ch = entryName.charAt(i);
					if (ch == '/') {
						level++;
					}
				}

				if (level <= 1) {

					String itemName = entryName.substring(0, entryName.indexOf('/'));

					if (zipEntry.isDirectory()) {
						Item item = new Item();
						item.name = itemName;
						this.items.put(item.name, item);
					} else {
						String fileName = entryName.substring(entryName.indexOf('/') + 1);

						Item item = (Item) this.items.get(itemName);

						int size = (int) zipEntry.getSize();
						byte[] bytes = new byte[size];
						int rest = size;
						int len = 0;
						while (rest > 0) {
							len = zipIn.read(bytes, size - rest, rest);
							if (len == -1) {
								break;
							}
							rest -= len;
						}

						ByteArrayInputStream entryIn = null;
						try {
							entryIn = new ByteArrayInputStream(bytes);

							if ("include".equals(fileName)) {
								List<String> lines = PlatformClientUtils.readLines(entryIn, "UTF-8");
								for (String line : lines) {
									if ((line != null) && (line.length() > 0)) {
										item.includes.add(line.trim());
									}
								}
							} else if (fileName.endsWith(".properties")) {
								item.properties.load(entryIn);
							}
						} finally {
						}
					}
				}
			}
		} finally {
			PlatformClientUtils.close(zipIn);
		}
	}

	private void loadItem(File itemDir) throws IOException {
		Item item = new Item();
		for (File file : itemDir.listFiles()) {
			String fileName = file.getName();

			if ("include".equals(fileName)) {
				List<String> lines = PlatformClientUtils.readLines(file, "UTF-8");
				for (String line : lines) {
					if ((line != null) && (line.length() > 0)) {
						item.includes.add(line.trim());
					}
				}
			}

			if ((file.isFile()) && (fileName.endsWith(".properties"))) {
				FileInputStream in = null;
				try {
					in = new FileInputStream(file);
					item.properties.load(in);
				} finally {
					PlatformClientUtils.close(in);
				}
			}
		}

		this.items.put(itemDir.getName(), item);
	}

	private static class Item {
		Properties properties = new Properties();
		String name;
		String description;
		List<String> includes = new ArrayList<>();
	}

	private void loadProperties(String name, Map<String, Object> map) {
		Item item = (Item) this.items.get(name);
		if (item == null) {
			return;
		}

		for (Map.Entry<Object, Object> entry : item.properties.entrySet()) {
			map.put((String) entry.getKey(), entry.getValue());
		}

		for (String include : item.includes) {
			int index = include.indexOf('!');

			if (index == -1) {
				loadProperties(include, map);
			} else {
				String fileName = include.substring(0, index);
				String extIncludeName = include.substring(index + 1);

				FileSystemPreferencesFactory extFactory = new FileSystemPreferencesFactory(fileName);
				extFactory.loadProperties(extIncludeName, map);
			}
		}
	}

	public Preferences getPreferences(String name) {
		Item item = (Item) this.items.get(name);
		if (item == null) {
			return null;
		}

		Preferences pref = new Preferences();

		pref.setName(item.name);
		pref.setDescription(item.description);

		Map<String, Object> map = new HashMap<>();
		loadProperties(name, map);

		for (Map.Entry<String,Object> entry : map.entrySet()) {
			pref.put((String) entry.getKey(), entry.getValue());
		}

		return pref;
	}
}
