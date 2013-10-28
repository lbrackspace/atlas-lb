package com.rackspacecloud.client.cloudfiles;

public class FilesRegion {

	private static final String SNET_PREFIX = "https://snet-";

	private final String regionId;
	private final String storageURL;
	private final String cdnManagementURL;
	private final boolean isDefault;

	FilesRegion(String regionId, String storageURL, String CDNManagementURL, boolean isDefault) {
		this.regionId = regionId;
		this.storageURL = storageURL;
		this.cdnManagementURL = CDNManagementURL;
		this.isDefault = isDefault;
	}
	
	public String getRegionId() {
		return regionId;
	}

	public String getStorageUrl(boolean usingSnet) {
		if (usingSnet) {
			return SNET_PREFIX + storageURL.substring(8);
		} else {
			return storageURL;
		}
	}

	public String getCDNManagementURL() {
		return cdnManagementURL;
	}
	
	public boolean isDefault() {
		return isDefault;
	}
}
