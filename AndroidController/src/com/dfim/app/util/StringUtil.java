package com.dfim.app.util;


import java.util.Collection;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * @ClassName:StringUtil
 * @Description:�ַ�����
 * @author Mark
 * @date 2011-12-2 ����11:12:14
 */
public class StringUtil {

	/**
	 * �Ƿ�Ϊ��
	 * 
	 * @param str
	 * @return
	 */
	public static boolean isEmpty(Object obj) {
		return obj == null || isEmpty(obj.toString());
	}
	/**
	 * @Title: isNull
	 * @Description: �ж��ַ��Ƿ�Ϊ��
	 * @param @param s
	 * @param @return
	 * @return boolean
	 * @throws
	 */
	public static boolean isEmpty(String s) {
		if (s == null) {
			return true;
		}
		s = s.trim();
		return (s.equals(StringPool.BLANK) || s.equals(StringPool.NULL));
	}

	/**
	 * @Title: isNotEmpty
	 * @Description: �ж��ַ��Ƿ�Ϊ��
	 * @param @param s
	 * @param @return
	 * @return boolean
	 * @throws
	 */
	public static boolean isNotEmpty(String s) {
		return !isEmpty(s);
	}

	/**
	 * @Title: first2Upper
	 * @Description: �ַ�����ĸת��д
	 * @param @param s
	 * @return void
	 * @throws
	 */
	public static String first2Upper(String s) {
		String temp = getFirst(s);
		return (!ValidateUtil.isChar(temp)) ? null : temp.toUpperCase();
	}

	/**
	 * @Title: first2Upper
	 * @Description: ��ݱ��ֶ���ƻ�ȡ��񻯵��������:ȥ���»��ߣ����׵��ʵ����ַ��д
	 * @param @param s
	 * @param @param underLine
	 * @param @return
	 * @return String
	 * @throws
	 */
	public static String first2Upper(String s, String underLine) {
		StringBuffer bs = new StringBuffer();
		String[] array = split(s.toLowerCase(), underLine);
		bs.append(array[0]);// ��ȡһ���ַ�
		for (int i = 0; i < array.length; i++) {
			bs.append(array[i].substring(0, 1).toUpperCase())// ���ִ�д
					.append(array[i].substring(1, array[i].length()));// �����ַ��
		}
		return bs.toString();
	}

	/**
	 * @Title: getFirst
	 * @Description:ȡ�ַ����ַ�
	 * @param @param s
	 * @param @return
	 * @return String
	 * @throws
	 */
	public static String getFirst(String s) {
		return isEmpty(s) ? null : s.substring(0, 1);
	}

	/**
	 * @Title: removeLastChar
	 * @Description: ɾ�����һ���ַ�
	 * @param @param s
	 * @param @return
	 * @return String
	 * @throws
	 */
	public static String removeLastChar(String s) {
		return isEmpty(s) ? null : s.substring(0, s.length() - 1);
	}

	/**
	 * @Title: shorten
	 * @Description: ��ȡ����Ϊ20,���Ϻ�׺...
	 * @param @param s
	 * @param @return
	 * @return String
	 * @throws
	 */
	public static String shorten(String s) {
		return shorten(s, 20);
	}

	/**
	 * @Title: shorten
	 * @Description: ��ȡ����Ϊ20,���Ϻ�׺...
	 * @param @param s
	 * @param @param length
	 * @param @return
	 * @return String
	 * @throws
	 */
	public static String shorten(String s, int length) {
		return shorten(s, 20, "...");
	}

	/**
	 * @Title: shorten
	 * @Description: ��ȡָ�����ȵ��ַ����Ϻ�׺
	 * @param @param s
	 * @param @param length
	 * @param @param suffix
	 * @param @return
	 * @return String
	 * @throws
	 */
	public static String shorten(String s, int length, String suffix) {
		if (isEmpty(s) || isEmpty(suffix)) {
			return null;
		}
		if (s.length() > length) {// ����ַ��ȴ���ָ����ȡ�ĳ���
			s = s.substring(0, length) + suffix;
		}
		return s;
	}

	/**
	 * @Title: split
	 * @Description: ��ݶ��ŷָ��ַ�
	 * @param @param s
	 * @param @return
	 * @return String[]
	 * @throws
	 */
	public static String[] split(String s) {
		return split(s, ",");
	}

	/**
	 * @Title: split
	 * @Description: ���ָ���ַ�ָ��ַ�
	 * @param @param s
	 * @param @param delimiter
	 * @param @return
	 * @return String[]
	 * @throws
	 */
	public static String[] split(String s, String delimiter) {
		return s.split(delimiter);
	}

	/**
	 * @Title: split
	 * @Description: �ָ�ָ���ַ�Ϊ��������,��Ϊ���ε��ַ�Ĭ��ֵx
	 * @param @param s
	 * @param @param delimiter
	 * @param @param x
	 * @param @return
	 * @return int[]
	 * @throws
	 */
	public static int[] split(String s, String delimiter, int x) {
		String[] array = s.split(delimiter);
		int[] newArray = new int[array.length];
		for (int i : newArray) {
			int value = x;// ��Ĭ��ֵ
			try {
				value = Integer.parseInt(array[i]);// �������ַ�ת��Ϊ����
			} catch (NumberFormatException e) {
				e.printStackTrace();
			}
			newArray[i] = value;
		}
		return newArray;
	}

	/**
	 * @Title: split
	 * @Description: ���ַ�ת��Ϊboolean������
	 * @param @param s
	 * @param @param delimiter
	 * @param @param x
	 * @param @return
	 * @return boolean[]
	 * @throws
	 */
	public static boolean[] split(String s, String delimiter, boolean x) {
		String[] array = split(s, delimiter);
		boolean[] newArray = new boolean[array.length];
		for (int i = 0; i < array.length; i++) {
			boolean value = x;
			try {
				value = Boolean.valueOf(array[i]).booleanValue();
			} catch (Exception e) {
				e.printStackTrace();
			}
			newArray[i] = value;
		}
		return newArray;
	}

	/**
	 * @Title: split
	 * @Description: TODO
	 * @param @param s
	 * @param @param delimiter
	 * @param @param x
	 * @param @return
	 * @return double[]
	 * @throws
	 */
	public static double[] split(String s, String delimiter, double x) {
		String[] array = split(s, delimiter);
		double[] newArray = new double[array.length];
		for (int i = 0; i < array.length; i++) {
			double value = x;
			try {
				value = Double.parseDouble(array[i]);
			} catch (Exception e) {
				e.printStackTrace();
			}
			newArray[i] = value;
		}
		return newArray;
	}

	/**
	 * @Title: split
	 * @Description: ���ַ�ת��Ϊfloat������
	 * @param @param s
	 * @param @param delimiter
	 * @param @param x
	 * @param @return
	 * @return float[]
	 * @throws
	 */
	public static float[] split(String s, String delimiter, float x) {
		String[] array = split(s, delimiter);
		float[] newArray = new float[array.length];
		for (int i = 0; i < array.length; i++) {
			float value = x;
			try {
				value = Float.parseFloat(array[i]);
			} catch (Exception e) {
				e.printStackTrace();
			}
			newArray[i] = value;
		}
		return newArray;
	}

	/**
	 * @Title: split
	 * @Description: ���ַ�ת��short������
	 * @param @param s
	 * @param @param delimiter
	 * @param @param x
	 * @param @return
	 * @return short[]
	 * @throws
	 */
	public static short[] split(String s, String delimiter, short x) {
		String[] array = split(s, delimiter);
		short[] newArray = new short[array.length];
		for (int i = 0; i < array.length; i++) {
			short value = x;
			try {
				value = Short.parseShort(array[i]);
			} catch (Exception e) {
				e.printStackTrace();
			}
			newArray[i] = value;
		}

		return newArray;
	}

	/**
	 * @Title: split
	 * @Description: ���ַ�ת��Long������
	 * @param @param s
	 * @param @param delimiter
	 * @param @param defaultValue
	 * @param @return
	 * @return Long[]
	 * @throws
	 */
	public static Long[] split(String s, String delimiter, Long defaultValue) {
		String[] array = split(s, delimiter);
		Long[] newArray = new Long[array.length];
		for (int i = 0; i < array.length; i++) {
			Long value = defaultValue;
			try {
				value = new Long(array[i]);
			} catch (Exception e) {
				e.printStackTrace();
			}
			newArray[i] = value;
		}
		return newArray;
	}

	/**
	 * @Title: add
	 * @Description: ����Ŀ���ַ�
	 * @param @param s ԭʼ�ַ�
	 * @param @param add Ŀ���ַ�
	 * @param @return
	 * @return String
	 * @throws
	 */
	public static String add(String s, String add) {
		return add(s, add, StringPool.COMMA);
	}

	/**
	 * @Title: add
	 * @Description: ����Ŀ���ַ�
	 * @param @param s ԭʼ�ַ�
	 * @param @param add Ŀ���ַ�
	 * @param @param delimiter �ָ���
	 * @param @return
	 * @return String Ĭ�������ظ�
	 * @throws
	 */
	public static String add(String s, String add, String delimiter) {
		return add(s, add, delimiter, false);
	}

	/**
	 * @Title: add
	 * @Description: ����Ŀ���ַ�
	 * @param @param s ԭʼ�ַ�
	 * @param @param add �����ӵ��ַ�
	 * @param @param delimiter �ָ��
	 * @param @param allowDuplicates �Ƿ������ظ�
	 * @param @return
	 * @return String
	 * @throws
	 */
	public static String add(String s, String add, String delimiter,
			boolean allowDuplicates) {
		if (add == null || delimiter == null) {// ��������ӵ��ַ���߷ָ��Ϊnull,��ֱ�ӷ���null
			return null;
		}
		if (s == null) {// ���ԭʼ�ַ�Ϊnull,�򸳿��ַ�
			s = StringPool.BLANK;
		}
		if (allowDuplicates || !contains(s, add, delimiter)) {
			if (isEmpty(s) || s.endsWith(delimiter)) {
				s += add + delimiter;
			} else {
				s += delimiter + add + delimiter;
			}
		}
		return s;
	}

	/**
	 * @Title: contains
	 * @Description: �ж��ַ��Ƿ����һ�ַ�,Ĭ�ϴ��ж��ŷָ���
	 * @param @param s
	 * @param @param text
	 * @param @return
	 * @return boolean
	 * @throws
	 */
	public static boolean contains(String s, String text) {
		return contains(s, text, StringPool.COMMA);
	}

	/**
	 * @Title: contains
	 * @Description: �ַ��Ƿ����һ�ַ�
	 * @param @param s ԭʼ�ַ�
	 * @param @param text ������ַ�
	 * @param @param delimiter �ָ��,�ɶ�����طָ���
	 * @param @return
	 * @return boolean
	 * @throws
	 */
	public static boolean contains(String s, String text, String delimiter) {
		if (s == null || text == null || delimiter == null) {
			return false;
		}
		if (!s.endsWith(delimiter)) {// �ж��ַ��Ƿ��Էָ�����
			s += delimiter;// ���û�зָ������Ϸָ��
		}
		int pos = s.indexOf(delimiter + text + delimiter);
		if (pos == -1) {
			return s.startsWith(text + delimiter);// �ַ��Ƿ��ָ������
		}
		return true;
	}

	/**
	 * @Title: count
	 * @Description: ����Ŀ���ַ���ԭʼ�ַ���ֵĴ���
	 * @param @param s Դ�ַ�
	 * @param @param text Ŀ���ַ�
	 * @param @return
	 * @return int
	 * @throws
	 */
	public static int count(String s, String text) {
		if (isEmpty(s) || isEmpty(text)) {
			return 0;
		}
		int count = 0;
		int pos = s.indexOf(text);
		while (pos != -1) {
			pos = s.indexOf(text, pos + text.length());
			count++;
		}
		return count;
	}

	/**
	 * @Title: merge
	 * @Description: �ϲ���������ַ�Ԫ��,Ĭ�϶��Ÿ���
	 * @param @param array
	 * @param @return
	 * @return String
	 * @throws
	 */
	public static String merge(String[] array) {
		return merge(array, StringPool.COMMA);
	}

	/**
	 * @Title: merge
	 * @Description: �ϲ���������ַ�Ԫ��
	 * @param @param array
	 * @param @param delimiter
	 * @param @return
	 * @return String
	 * @throws
	 */
	public static String merge(String[] array, String delimiter) {
		if (array == null) {
			return null;
		}
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < array.length; i++) {// ��������
			sb.append(array[i].trim());
			if ((i + 1) != array.length) {// ����±겻�������鳤��,����ʾ���һ��Ԫ��
				sb.append(delimiter);
			}
		}
		return sb.toString();
	}

	/**
	 * @Title: merge
	 * @Description: ��������ϲ�,Ĭ�϶��ŷָ�
	 * @param @param coll
	 * @param @return
	 * @return String
	 * @throws
	 */
	public static String merge(Collection<?> coll) {
		return merge(coll, StringPool.COMMA);
	}

	/**
	 * @Title: merge �����Ϻϲ����ַ�
	 * @Description: �ϲ��ַ�
	 * @param @param coll ������
	 * @param @param delimiter �ָ���
	 * @param @return
	 * @return String
	 * @throws
	 */
	public static String merge(Collection<?> coll, String delimiter) {
		StringBuffer sb = new StringBuffer();
		if (CollectionUtil.isNotNull(coll)) {
			Iterator<?> it = coll.iterator();
			while (it.hasNext()) {
				sb.append(it.next()).append(delimiter);
			}
			return sb.toString().substring(0, sb.length() - 1);
		} else {
			return null;
		}
	}
	
	
	public static String StringFilter(String str) throws PatternSyntaxException {
		String regEx = "[`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~��@#��%����&*��������+|{}������������������������\\s*|\t|\r|\n]";
		Pattern p = Pattern.compile(regEx);
		Matcher m = p.matcher(str);
		return m.replaceAll("").trim();
	}	

/**
	 * ʱ���ʽת������
	 * 
	 * @param time
	 * @return
	 */
	public static String toTime(int time) {
		time /= 1000;
		int minute = time / 60;
		int hour = minute / 60;
		int second = time % 60;
		minute %= 60;
		return String.format("%02d:%02d", minute, second);
	}
	
	public static int toSec(String time){   
    	String[] times =time.split(":");
    	int s = 0;
    	try{
             if(times.length==1){
            	 s = Integer.parseInt(times[0]);
             }else if(times.length==2){
            	 s = Integer.parseInt(times[0])*60+Integer.parseInt(times[1]);
             }else if(times.length==3){
            	 s = Integer.parseInt(times[0])*3600+Integer.parseInt(times[1])*60+Integer.parseInt(times[2]);
             }
    	}catch(Exception e){
    	}
          return s;
	}
	
	/**
	 * @Title: ToDBC
	 * @Description:�ַ�ȫ�ǻ�
	 * @param @param input
	 * @param @return
	 * @return String    
	 * @throws
	 */
	public static String ToDBC(String input) {
		   if(input==null){
			   return "";
		   }
		   char[] c = input.toCharArray();
		   for (int i = 0; i< c.length; i++) {
		       if (c[i] == 12288) {
		         c[i] = (char) 32;
		         continue;
		       }if (c[i]> 65280&& c[i]< 65375)
		          c[i] = (char) (c[i] - 65248);
		       }
		   return new String(c);
		}
}
