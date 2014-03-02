package com.kitty.poclient.upnp;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;

import org.fourthline.cling.UpnpServiceConfiguration;
import org.fourthline.cling.android.AndroidRouter;
import org.fourthline.cling.android.AndroidUpnpServiceConfiguration;
import org.fourthline.cling.android.AndroidUpnpServiceImpl;
import org.fourthline.cling.model.types.ServiceType;
import org.fourthline.cling.model.types.UDAServiceType;
import org.fourthline.cling.protocol.ProtocolFactory;
import org.fourthline.cling.transport.impl.jetty.StreamClientConfigurationImpl;
import org.fourthline.cling.transport.spi.StreamClient;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.kitty.poclient.common.Constant;
import com.kitty.poclient.events.SimpleEvent;

public class MyUpnpServiceImpl extends AndroidUpnpServiceImpl{
	
	private static final String TAG = MyUpnpServiceImpl.class.getSimpleName();
	public static final String NOWIFIAVAILABLE = "NoWifiAvailable";
	
	public static class NoWifiAvailableEvent extends SimpleEvent{

		public NoWifiAvailableEvent(String type) {
			super(type);
		}
		
	}

	@Override
	protected UpnpServiceConfiguration createConfiguration() {
		return new AndroidUpnpServiceConfiguration(){

			@Override
			public int getRegistryMaintenanceIntervalMillis() {
				return 3000;
			}

			@Override
			public ServiceType[] getExclusiveServiceTypes() {
				return new ServiceType[]{						
						new UDAServiceType("ConnectionManager"),
						new UDAServiceType("AVTransport"),
						new UDAServiceType("RenderingControl"),
						new UDAServiceType("BoxControl"),
						new UDAServiceType("ContentDirectory"),
						new UDAServiceType("CacheControl")
				};
			}
			
			//该方法的源码中定义了线程池容�?
			@Override
			protected ExecutorService createDefaultExecutorService() {
				// TODO Auto-generated method stub
				return super.createDefaultExecutorService();
			}

			@Override
			public StreamClient createStreamClient() {
				StreamClientConfigurationImpl configuration=new StreamClientConfigurationImpl(getDefaultExecutorService(), Constant.UPNP_TIMEOUT_MILLIS);
				StreamClient client=new MyStreamClientImpl(configuration);
//				StreamClient client=new StreamClientImpl(configuration);
				return client;
			}

			@Override
			public int getAliveIntervalMillis() {
				// TODO Auto-generated method stub
				return super.getAliveIntervalMillis();
			}

			@Override
			public Executor getRegistryMaintainerExecutor() {
				// TODO Auto-generated method stub
				return super.getRegistryMaintainerExecutor();
			}	
		};
	}

	@Override
	protected AndroidRouter createRouter(
			UpnpServiceConfiguration configuration,
			ProtocolFactory protocolFactory, Context context) {
		AndroidRouter router = new AndroidRouter(configuration, protocolFactory, context);
		if(router.getNetworkInfo() == null){
			Log.e(TAG, "no wifi available!!");
			context.sendBroadcast(new Intent(NOWIFIAVAILABLE));
		}
		return router;
	}

}
