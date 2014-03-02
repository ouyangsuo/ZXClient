package com.kitty.poclient.util;


public class ValidateUtil {

	/**
	 * @Title: isChar
	 * @Description: �ж��ַ��Ƿ�ΪӢ����ĸ���
	 * @param @param s
	 * @param @return
	 * @return boolean
	 * @throws
	 */
	public static boolean isChar(String s) {
		if (StringUtil.isEmpty(s)) {// ����ַ�Ϊ��
			return false;
		}

		char[] _c = s.toCharArray();// ת���ַ�����
		for (char c : _c) {// �����ַ����
			if (!isChar(c)) {// �������һ����Ϊ�ַ�,����ַ���ȫ��Ӣ����ĸ���
				return false;
			}
		}
		return true;
	}

	/**
	 * @Title: isChar
	 * @Description: �ж��ַ��Ƿ���Ӣ����ĸ
	 * @param @param c
	 * @param @return
	 * @return boolean
	 * @throws
	 */
	public static boolean isChar(char c) {
		return Character.isLetter(c);
	}

	/**
	 * @Title: isDigit
	 * @Description: �ж��ַ��Ƿ��ǰ���������
	 * @param @param c
	 * @param @return
	 * @return boolean
	 * @throws
	 */
	public static boolean isDigit(char c) {
		int x = (int) c;
		return (x >= 48) && (x <= 57);
	}

	/**
	 * @Title: isNotDigit
	 * @Description: �ж��ַ��Ƿ��ǰ���������
	 * @param @param c
	 * @param @return
	 * @return boolean
	 * @throws
	 */
	public static boolean isNotDigit(char c) {
		return !isDigit(c);
	}

	/**
	 * @Title: isDigit
	 * @Description: �Ƿ���������
	 * @param @param s
	 * @param @return
	 * @return boolean
	 * @throws
	 */
	public static boolean isDigit(String s) {
		if (StringUtil.isEmpty(s)) {
			return false;
		}
		char[] _c = s.toCharArray();
		for (char c : _c) {
			if (!isDigit(c)) {
				return false;
			}
		}

		return true;
	}

	/**
	 * @Title: isNotDigit
	 * @Description: �Ƿ���������
	 * @param @param s
	 * @param @return
	 * @return boolean
	 * @throws
	 */
	public static boolean isNotDigit(String s) {
		return !isNotDigit(s);
	}

	public static void main(String[] args) {
		System.out.println(ValidateUtil.isDigit('a'));
	}
}

