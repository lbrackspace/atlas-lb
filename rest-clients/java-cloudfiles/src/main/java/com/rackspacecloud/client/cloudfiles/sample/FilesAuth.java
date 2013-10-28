/*
 * See COPYING for license information.
 */ 

package com.rackspacecloud.client.cloudfiles.sample;

import com.rackspacecloud.client.cloudfiles.*;

public class FilesAuth
{
	public static void main(String[] args)
	{
		try
		{
			FilesClient client = new FilesClient();
			boolean success = client.login();
			if (success)
			{
				System.out.println("username: "+client.getUserName());
				System.out.println("url: "+client.getStorageURL());
				System.out.println("token: "+client.getAuthToken());
			}
			else
			{
				System.out.println("login failed.");
			}
		}
		catch (Exception e)
		{
			System.err.println(e.toString());
		}
	}
}
