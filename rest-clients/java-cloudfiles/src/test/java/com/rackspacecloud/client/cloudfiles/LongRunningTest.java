/**
 * 
 */
package com.rackspacecloud.client.cloudfiles;

import com.rackspacecloud.client.cloudfiles.FilesClient;

/**
 * @author lvaughn
 *
 */
public class LongRunningTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			FilesClient client = new FilesClient();
	
			client.login();
			
			for(int i=0; i < 120; ++i) {
				byte data[] = client.getObject("test_html", "test.html");
				assert (data != null);
				System.out.println("Got object " + i);
				try {
					Thread.sleep(1000 * 60);
				}
				catch (InterruptedException ie) {
					// No Op
				}
			}
		
		
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
	}

}
