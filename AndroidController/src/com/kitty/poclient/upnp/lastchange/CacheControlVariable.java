package com.kitty.poclient.upnp.lastchange;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.fourthline.cling.support.lastchange.EventedValue;
import org.fourthline.cling.support.lastchange.EventedValueString;

public class CacheControlVariable {
	 public static Set<Class<? extends EventedValue>> ALL = new HashSet() { } ;
	  public static class CacheInfoUpdate extends EventedValueString
	  {
	    public CacheInfoUpdate(String value)
	    {
	      super(value);
	    }

	    public CacheInfoUpdate(Map.Entry<String, String>[] attributes) {
	      super(attributes);
	    }
	  }

      public static class CurrentCacheURI extends EventedValueString
	  {
	    public CurrentCacheURI(String value)
	    {
	      super(value);
	    }

	    public CurrentCacheURI(Map.Entry<String, String>[] attributes) {
	      super(attributes);
	    }
	  }
	  
	  public static class A_ARG_TYPE_CacheURI extends EventedValueString
	  {
	    public A_ARG_TYPE_CacheURI(String value)
	    {
	      super(value);
	    }

	    public A_ARG_TYPE_CacheURI(Map.Entry<String, String>[] attributes) {
	      super(attributes);
	    }
	  }
	  
	  public static class A_ARG_TYPE_CacheInfo extends EventedValueString
	  {
	    public A_ARG_TYPE_CacheInfo(String value)
	    {
	      super(value);
	    }

	    public A_ARG_TYPE_CacheInfo(Map.Entry<String, String>[] attributes) {
	      super(attributes);
	    }
	  } 
	  
	    public static class A_ARG_TYPE_CacheProgress extends EventedValueString
		  {
		    public A_ARG_TYPE_CacheProgress(String value)
		    {
		      super(value);
		    }

		    public A_ARG_TYPE_CacheProgress(Map.Entry<String, String>[] attributes) {
		      super(attributes);
		    }
	  }
	    
	    public static class CurrentCacheChange extends EventedValueString
			  {
			    public CurrentCacheChange(String value)
			    {
			      super(value);
			    }

			    public CurrentCacheChange(Map.Entry<String, String>[] attributes) {
			      super(attributes);
			    }
		  }   
	    
	    public static class StatusCode extends EventedValueString
		  {
		    public StatusCode(String value)
		    {
		      super(value);
		    }

		    public StatusCode(Map.Entry<String, String>[] attributes) {
		      super(attributes);
		    }
	  }
}
