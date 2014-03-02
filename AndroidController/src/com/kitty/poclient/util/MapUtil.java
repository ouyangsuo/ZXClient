package com.kitty.poclient.util;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @ClassName:MapUtil
 * @Description:
 * @author Mark
 * @date 2012-7-26 ����01:36:42
 */
public class MapUtil {

	/**
	 * @Title: isNull
	 * @Description: �ж�map�Ƿ�Ϊ��
	 * @param @param map
	 * @param @return
	 * @return boolean
	 * @throws
	 */
	public static boolean isNull(Map<?, ?> map) {
		return map == null || map.size() <= 0;
	}

	/**
	 * ��ȥ�����еĿ�ֵ��ǩ�����
	 * 
	 * @param map
	 *            ǩ�������
	 * @return ȥ����ֵ��ǩ���������ǩ�������
	 */
	public static Map<String, Object> filter(Map<String, Object> map,
			String filterKey) {
//		logger.debug("���ڽ����˲���map ��" + map + "�� �Ŀ�ֵ��ǩ�����...");
		Map<String, Object> result = new HashMap<String, Object>();
		if (isNull(map)) {
			return result;
		}
		Object value = "";
		for (String key : map.keySet()) {
			value = map.get(key);
			if (value == null || value.equals("")
					|| key.equalsIgnoreCase(filterKey)) {
//				logger.debug("���˿�ֵ||ǩ�����: key = " + key + ", value = " + value);
				continue;
			}
			result.put(key, value);
		}
//		logger.debug("�Ѿ��˲���map�Ŀ�ֵ��ǩ�����, ���˺��map: ��" + result + "��");
//		logger.debug("----------------------------------------------------");
		return result;
	}

	/**
	 * @throws UnsupportedEncodingException 
	 * @Title: getUrlFromMap
	 * @Description: ��map����ת��Ϊurl��������
	 * @param @param params
	 * @param @return
	 * @return String
	 * @throws
	 */
	public static String getUrlFromMap(Map<String, Object> params) throws UnsupportedEncodingException {
//		logger.debug("���ڽ�����map ��" + params + "�� ת��������Url...");
		List<String> keys = new ArrayList<String>(params.keySet());// ��keyת��Ϊlist
		Collections.sort(keys);// ��key��������
//		logger.debug("�ѽ�����map��key��������,���key��list ��" + keys + "��");
		StringBuffer sb = new StringBuffer();
		for (String key : keys) {
			sb.append(key).append("=").append(params.get(key)).append("&");
//				try {
//					sb.append(key).append("=").append(URLEncoder.encode((params.get(key).toString()),"UTF-8")).append("&");
//				} catch (UnsupportedEncodingException e) {
//					e.printStackTrace();
//				}
	
		}
		String url = sb.deleteCharAt(sb.lastIndexOf("&")).toString();
//		logger.debug("�ѽ�����mapת��������Url,�������Url:\n��" + url + "��");
//		logger.debug("----------------------------------------------------");
		return url;
	}

}
