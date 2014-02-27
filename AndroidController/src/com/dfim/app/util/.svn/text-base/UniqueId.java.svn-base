package com.dfim.app.util;

import java.util.Calendar;
import java.util.Random;

/**
 * @ClassName:UniqueId
 * @Description:产生一个唯一ID
 * @author mark
 * @date 2011-4-17 下午03:58:58
 */
public class UniqueId {
	private static long thisId = 0;

	/**
	 * @Title: genId
	 * @Description: 根据时间戳产生一个唯一ID,具有防止重复机制
	 * @return
	 * @throws Exception
	 */
	public synchronized static long genId() throws Exception {
		long id = 0;
		do {
			Calendar c = Calendar.getInstance();
			id = c.getTimeInMillis();
		} while (id == thisId);
		thisId = id;
		return id;
	}

    /**
     * 生成一个指定位数的唯一ID
     * @param digit ID位数
     * @return 由数字和大写字母组成的ID号
     */
    public static String genId(int digit)
    {
        char[] digits =
            {
            '1', '2', '3', '4', '5', '6', '7', '8', '9', '0',
            'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J',
            'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T',
            'U', 'V', 'W', 'X', 'Y', 'Z'};
        int temp;
        String id = "";
        for (int i = 0; i < digit; i++)
        {
            temp = (new Double(Math.random() * 997)).intValue() % 36;
            id += String.valueOf(digits[temp]);
        }
        return id;
    }
    
	public static Integer getNum() { 
	    Random random = new Random();
	    Integer temp;
	    do {
	    	 temp=random.nextInt();
		} while (temp<0);
		return temp;
	     
	} 
	
	public static void main(String[] args) { 
        System.out.println(getNum().toString()); 
        String str = " hell o ";
        String str2 = str.replaceAll(" ", "");
        String str3 = str.trim();
        System.out.println(str2+"==="+str3);
    } 
}
