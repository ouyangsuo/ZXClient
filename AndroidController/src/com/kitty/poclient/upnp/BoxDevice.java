package com.kitty.poclient.upnp;

import org.fourthline.cling.model.meta.Device;
import org.fourthline.cling.model.meta.Service;

import com.kitty.poclient.R;
import com.kitty.poclient.common.UpnpApp;

public class BoxDevice {

	Device device;

	public BoxDevice(Device device) {
		this.device = device;
	}

	public Device getDevice() {
		return device;
	}

	public String getDetailsMessage() {
		StringBuilder sb = new StringBuilder();
		if (getDevice().isFullyHydrated()) {
			sb.append(getDevice().getDisplayString());
			sb.append("\n\n");
			for (Service service : getDevice().getServices()) {
				sb.append(service.getServiceType()).append("\n");
			}
		} else {
			sb.append(UpnpApp.getContext().getString(R.string.deviceDetailsNotYetAvailable));
		}
		return sb.toString();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		BoxDevice that = (BoxDevice) o;
		return device.equals(that.device);
	}

	@Override
	public int hashCode() {
		return device.hashCode();
	}

	@Override
	public String toString() {
		String name = getDevice().getDetails() != null && getDevice().getDetails().getFriendlyName() != null ? getDevice().getDetails().getFriendlyName() : getDevice().getDisplayString();
		return device.isFullyHydrated() ? name : name + " *";
	}
}