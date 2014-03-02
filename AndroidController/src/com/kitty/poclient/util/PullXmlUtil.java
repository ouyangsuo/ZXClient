package com.kitty.poclient.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.content.res.XmlResourceParser;
import android.util.Xml;

public class PullXmlUtil {

	
	public static String getData(InputStream inputstream)  {
		         String str="";
		          XmlPullParser parser = Xml.newPullParser(); 
		         
		       try {
		    	   parser.setInput(inputstream, "UTF-8");
		            // 直到文档的结尾处
		             while (parser.getEventType() != XmlResourceParser.END_DOCUMENT) {
		                 // 如果遇到了开始标签
		           
	                     String tagName = parser.getName();// 获取标签的名字
	                     if(tagName!=null){
		                   if (tagName.equals("Hassyn")) {
		                	 /*  <Event xmlns="urn:schemas-upnp-org:metadata-1-0/AVT/"><InstanceID val="0"><Hassyn val="cloudchange:1380191845606,type:1,5,10,ids:11233-754554,oper:1,5"/></InstanceID></Event>

		                	   说明：type :1专辑   5单曲  15主题
		                	         oper:1删除  5同步本地      */
		                    str = parser.getAttributeValue(null, "val");// 通过属性名来获取属性值
		                
		                     break;
		          
		                    }
	                     }
		                parser.next();// 获取解析下一个事件
		            }
		        } catch (XmlPullParserException e) {
		            // TODO Auto-generated catch block
		            e.printStackTrace();
		        } catch (IOException e) {
		            // TODO Auto-generated catch block
		            e.printStackTrace();
		        }
		 
		        return str;
		    }
}
