package com.kitty.poclient.upnp.lastchange;

import java.util.Set;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import org.fourthline.cling.model.ModelUtil;
import org.fourthline.cling.support.lastchange.EventedValue;
import org.fourthline.cling.support.lastchange.LastChangeParser;

public class BoxControlLastChangeParser extends LastChangeParser
{
  public static final String NAMESPACE_URI = "urn:schemas-upnp-org:metadata-1-0/AVT/";
  public static final String SCHEMA_RESOURCE = "org/teleal/cling/support/avtransport/metadata-1.0-avt.xsd";

  protected String getNamespace()
  {
    return "urn:schemas-upnp-org:metadata-1-0/AVT/";
  }

  protected Source[] getSchemaSources()
  {
    if (!ModelUtil.ANDROID_RUNTIME) {
      return new Source[] { new StreamSource(Thread.currentThread().getContextClassLoader().getResourceAsStream("org/teleal/cling/support/avtransport/metadata-1.0-avt.xsd")) };
    }

    return null;
  }

  protected Set<Class<? extends EventedValue>> getEventedVariables()
  {
    return BoxControlVariable.ALL;
  }
}