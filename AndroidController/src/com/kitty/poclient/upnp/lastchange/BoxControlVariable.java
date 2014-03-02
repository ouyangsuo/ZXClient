package com.kitty.poclient.upnp.lastchange;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.fourthline.cling.support.lastchange.EventedValue;
import org.fourthline.cling.support.lastchange.EventedValueString;

public class BoxControlVariable {
	 public static Set<Class<? extends EventedValue>> ALL = new HashSet() { } ;
	  public static class Userid extends EventedValueString
	  {
	    public Userid(String value)
	    {
	      super(value);
	    }

	    public Userid(Map.Entry<String, String>[] attributes) {
	      super(attributes);
	    }
	  }

      public static class Password extends EventedValueString
	  {
	    public Password(String value)
	    {
	      super(value);
	    }

	    public Password(Map.Entry<String, String>[] attributes) {
	      super(attributes);
	    }
	  }
	  
	  public static class Host extends EventedValueString
	  {
	    public Host(String value)
	    {
	      super(value);
	    }

	    public Host(Map.Entry<String, String>[] attributes) {
	      super(attributes);
	    }
	  }
	  
	  public static class ControlState extends EventedValueString
	  {
	    public ControlState(String value)
	    {
	      super(value);
	    }

	    public ControlState(Map.Entry<String, String>[] attributes) {
	      super(attributes);
	    }
	  }
	  
	  public static class Controlkey extends EventedValueString
	  {
	    public Controlkey(String value)
	    {
	      super(value);
	    }

	    public Controlkey(Map.Entry<String, String>[] attributes) {
	      super(attributes);
	    }
	  }
	  
	  public static class SynchoContent extends EventedValueString
	  {
	    public SynchoContent(String value)
	    {
	      super(value);
	    }

	    public SynchoContent(Map.Entry<String, String>[] attributes) {
	      super(attributes);
	    }
	  }
	  
	  public static class Hassyn extends EventedValueString
	  {
	    public Hassyn(String value)
	    {
	      super(value);
	    }

	    public Hassyn(Map.Entry<String, String>[] attributes) {
	      super(attributes);
	    }
	  }
}
