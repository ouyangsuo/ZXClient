package com.kitty.poclient.util;

/**
 * 
 * @ClassName DateTimeFormatUtil
 * @Description 将歌曲毫秒值转为时间字符串
 * @author 王梓光
 * @date 2013-1-8上午11:07:42
 *
 */
public class DateTimeFormatUtil {
	
	/**
	 * 转为"02:49"或"01:02:49"的格式
	 * @param millisecond
	 * @return
	 */
	public static String parseInt2Time(int millisecond){
		int hour = (int) ((long)millisecond/(60*60*1000));
		int munit = (int) ((long)(millisecond%(60*60*1000))/(60*1000));
		int second = (int) ((long)(millisecond%(60*1000))/1000);
		return (hour==0?"":hour>10?hour+":":"0"+hour+":")+
				(munit==0?"00:":munit<10?"0"+munit+":":munit+":")+
				(second==0?"00":second<10?"0"+second:second);
	}
	/**
	 * 将"00:03:21"格式的字符串转为"03:21"
	 * @param timeString
	 * @return
	 */
	public static String clearZeroHour(String timeString){
		if(timeString!=null&&timeString.matches("^00:\\d{2}:\\d{2}$")){
			return timeString.substring(3);
		}
		return timeString;
	}
	
	/**
	 * 将"00:03:42"格式的字符转为毫秒值
	 * @param timestr
	 * @return
	 */
	public static long time2LongMillis(String timestr){  
        String[] times = timestr.split(":");
        if(times.length==1){
       	 return Long.parseLong(times[0])*1000;
        }else if(times.length==2){
       	 return (Long.parseLong(times[0])*60+Long.parseLong(times[1]))*1000;
        }else if(times.length==3){
       	 return (Long.parseLong(times[0])*3600+Long.parseLong(times[1])*60+Long.parseLong(times[2]))*100;
        }else{
       	 return 0;
        }
	}

	/**
	 * 将"00:03:42"格式的字符转为毫秒值
	 * @param timestr
	 * @return
	 */
	public static int time2IntMillis(String timestr){
        String[] times = timestr.split(":");
        if(times.length==1){
       	 return Integer.parseInt(times[0])*1000;
        }else if(times.length==2){
       	 return (Integer.parseInt(times[0])*60+Integer.parseInt(times[1]))*1000;
        }else if(times.length==3){
//       	return (Integer.parseInt(times[0])*3600+Integer.parseInt(times[1])*60+Integer.parseInt(times[2]))*100;//NumberFormatException: Invalid int: ""
        	return 0;
        }else{
       	 return 0;
        }
	}

	/*以毫秒为单位计算播放进度*/
	public static int getPlayingProgress(long elapsedMills, long totalMillis) {
		// TODO Auto-generated method stub
		return (int) (Double.parseDouble(elapsedMills+"")/Double.parseDouble(totalMillis+"")*100);
	}
	
	/*转换毫秒值为时长字符串00：00*/
	public static String parseMills2Time(long elapsedMills) {
		return parseInt2Time(Integer.parseInt(elapsedMills+""));		
	}
	
}
