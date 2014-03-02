package com.kitty.poclient.util;
import java.util.Collection;

/**
 * @ClassName:CollectionUtil
 * @Description:collection����
 * @author Mark
 * @date 2011-12-3 ����04:21:32
 */
public class CollectionUtil {

	/**
	 * @Title: isNull
	 * @Description: �жϼ����Ƿ�Ϊ��
	 * @param @param coll
	 * @param @return
	 * @return boolean
	 * @throws
	 */
	public static boolean isNull(Collection<?> coll) {
		if (coll == null) {
			return true;
		}
		if (coll.size() == 0) {
			return true;
		}
		return false;
	}

	public static boolean isNotNull(Collection<?> coll) {
		return !isNull(coll);
	}

}
