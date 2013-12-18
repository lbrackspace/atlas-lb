package com.rackspacecloud.client.cloudfiles;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.http.HttpException;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.SystemUtils;
import org.apache.log4j.Logger;

import com.rackspacecloud.client.cloudfiles.FilesAccountInfo;
import com.rackspacecloud.client.cloudfiles.FilesCDNContainer;
import com.rackspacecloud.client.cloudfiles.FilesClient;
import com.rackspacecloud.client.cloudfiles.FilesConstants;
import com.rackspacecloud.client.cloudfiles.FilesContainer;
import com.rackspacecloud.client.cloudfiles.FilesContainerExistsException;
import com.rackspacecloud.client.cloudfiles.FilesContainerInfo;
import com.rackspacecloud.client.cloudfiles.FilesInvalidNameException;
import com.rackspacecloud.client.cloudfiles.FilesNotFoundException;
import com.rackspacecloud.client.cloudfiles.FilesObject;
import com.rackspacecloud.client.cloudfiles.FilesObjectMetaData;
import com.rackspacecloud.client.cloudfiles.FilesUtil;
import com.rackspacecloud.client.cloudfiles.IFilesTransferCallback;


import junit.framework.TestCase;

public class FilesClientTestCase extends TestCase {
    private static Logger logger = Logger.getLogger(FilesClientTestCase.class);
	private static File SYSTEM_TMP = SystemUtils.getJavaIoTmpDir();
	private static int NUMBER_RANDOM_BYTES = 513;
	
	public void testConstructor() {
		FilesClient client = new FilesClient("foo", "bar");
		
		assertNotNull(client);
		assertEquals("foo", client.getUserName());
		assertEquals("bar", client.getPassword());
				
	}

	public void testNoArgConstructor() {
		FilesClient client = new FilesClient();
		
		assertNotNull(client);
		assertEquals(FilesUtil.getProperty("username"), client.getUserName());
		assertEquals(FilesUtil.getProperty("password"), client.getPassword());
		assertEquals(FilesUtil.getProperty("account"), client.getAccount());
	}

	public void testLogin() {
		FilesClient client = new FilesClient();
		
		try {
			assertTrue(client.login());
		} catch (Exception e) {
			fail(e.getMessage());
		} 
		
		// Now try a failed login
		// Note: This causes a warning from DefaultHttpClient along these lines:
		//       13 Dec 2010 15:46:26 WARN  impl.client.DefaultHttpClient - Authentication error: Unable to respond to any of these challenges: {}
		//       It's just trying a little too hard to be helpful.  
		client = new FilesClient(FilesUtil.getProperty("username"), 
				 	   		  FilesUtil.getProperty("password") + " this is a bogus password", 
				 	   		  FilesUtil.getProperty("account"));
		try {
			assertFalse(client.login());
		} catch (Exception e) {
			fail(e.getMessage());
		} 
	}
	
	public void testAccountInfo() {
		String containerName = createTempContainerName("acct-info");
		String filename = makeFileName("accountinfo");
		FilesClient client = new FilesClient();
		try {
			assertTrue(client.login());
			
			// Make sure it's not there
			assertFalse(client.containerExists(containerName));
			
			// Add it
			//logger.error(client.getStorageURL());
			client.createContainer(containerName);
			
			// See that it's there
			assertTrue(client.containerExists(containerName));
			assertNotNull(client.getContainerInfo(containerName));
			
			// Add some data
			byte randomData[] = makeRandomBytes();
			assertTrue(client.storeObject(containerName, randomData, "application/octet-stream", filename, new HashMap<String,String>()));
			
			// Do the test if we have an account
			if (FilesUtil.getProperty("account") != null) { 
				FilesAccountInfo info = client.getAccountInfo();
				assertTrue(info.getContainerCount() > 0);
				assertTrue(info.getBytesUsed() >= randomData.length);
			}
			
			// Clean up 
			client.deleteObject(containerName, filename);
			assertTrue(client.deleteContainer(containerName));
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		} 		
	}
	
	public void testMultipleFilesNotThere() {
	    // Tests to make sure we're releasing connections with 404's
		FilesClient client = new FilesClient();
		String filename = makeFileName("random");
		String fullPath = FilenameUtils.concat(SYSTEM_TMP.getAbsolutePath(), filename);
		try {
			byte[] content = makeRandomFile(fullPath);
			assertTrue(client.login());
			String containerName = createTempContainerName("file-not-there");
			client.createContainer(containerName);
			
			String[] names =  new String[10];
			for(int i=0; i < 10; ++i) names[i] = "File" + (i + 1) + ".txt";
			for(int i=0; i < 5; ++i) 			
				assertNotNull(client.storeObjectAs(containerName, new File(fullPath), "application/octet-stream", names[i]));

            for (int i = 0; i < 10; i++) {
                String fileName = names[i];

                byte[] retrievedContent = null;
                try {
                    retrievedContent = client.getObject(containerName, fileName);
                    assertArrayEquals(content, retrievedContent);
                } catch(FilesNotFoundException ex) {
                    assertTrue(i >= 5);
                }
            }
 			// Cleanup
			for(int i=0; i < 5; ++i) 			
				client.deleteObject(containerName, names[i]);
			client.deleteContainer(containerName);

		} catch (Exception e) {
			fail(e.getMessage());
		} 
		finally {
			File f = new File(fullPath);
			f.delete();
		}
	}

	public void testContainerCreation() {
		FilesClient client = new FilesClient();
		try {
			assertTrue(client.login());
			String containerName = createTempContainerName("container");
			
			// Make sure it's not there
			assertFalse(client.containerExists(containerName));
			
			// Add it
			//logger.error("Creating the container");
			client.createContainer(containerName);
			//logger.error("URL:\n" + client.getStorageURL() + "/" + containerName + "\n");
			//Thread.sleep(10000);
			
			// See that it's there
			assertTrue(client.containerExists(containerName));
			assertNotNull(client.getContainerInfo(containerName));
			
			// Try Adding it again
			try {
				client.createContainer(containerName);
				fail("Allowed duplicate container creation");
			}
			catch (FilesContainerExistsException fcee) {
				// Hooray!
			}
			
			// See that it's still there
			assertTrue(client.containerExists(containerName));
			
			// Delete it
			assertTrue(client.deleteContainer(containerName));
			
			// Make sure it's gone
			assertFalse(client.containerExists(containerName));
			

		} catch (Exception e) {
			fail(e.getMessage());
		} 
	}
	
	public void testAlternateLoginMethod() {
		FilesClient client = new FilesClient();
		try {
			assertTrue(client.login());
			String containerName = createTempContainerName("container");
			
			// Make sure it's not there
			assertFalse(client.containerExists(containerName));
			
			// Add it
			//logger.error("Creating the container");
			client.createContainer(containerName);
			//logger.error("URL:\n" + client.getStorageURL() + "/" + containerName + "\n");
			Thread.sleep(1000);
			
			// See that it's there
			assertTrue(client.containerExists(containerName));
			assertNotNull(client.getContainerInfo(containerName));
			
			// Create a new Client
			FilesClient newClient = new FilesClient();
			newClient.login(client.getAuthToken(), client.getStorageURL(), client.getCdnManagementURL());
			
			// See that it's still there
			assertTrue(newClient.containerExists(containerName));
			assertNotNull(newClient.getContainerInfo(containerName));
			
			// Try Adding it again
			try {
				newClient.createContainer(containerName);
				fail("Allowed duplicate container creation");
			}
			catch (FilesContainerExistsException fcee) {
				// Hooray!
			}
			
			// See that it's still there
			assertTrue(newClient.containerExists(containerName));
			
			// Delete it
			assertTrue(newClient.deleteContainer(containerName));
			
			// Make sure it's gone
			assertFalse(newClient.containerExists(containerName));
			

		} catch (Exception e) {
			fail(e.getMessage());
		} 
	}
	
	public void testContainerNotThereDeletion() {
		FilesClient client = new FilesClient();
		try {
			assertTrue(client.login());
			String containerName = createTempContainerName("I'mNotHere!");
			
			// Make sure it's not there
			assertFalse(client.containerExists(containerName));
			
			boolean exceptionThrown = false;
			try {
				client.deleteContainer(containerName);
				fail("Exception not thrown");
			}
			catch (FilesNotFoundException fnfe) {
				exceptionThrown = true;
			}
			assertTrue (exceptionThrown);
			
			// Make still not there
			assertFalse(client.containerExists(containerName));
			

		} catch (Exception e) {
			fail(e.getMessage());
		} 
	}
	
	public void testContainerCreationWithSpaces() {
		FilesClient client = new FilesClient();
		try {
			assertTrue(client.login());
			String containerName = createTempContainerName("with space+and+plus");
			
			// Make sure it's not there
			assertFalse(client.containerExists(containerName));
			
			// Add it
			client.createContainer(containerName);
			
			// See that it's there
			assertTrue(client.containerExists(containerName));
			assertNotNull(client.getContainerInfo(containerName));
			
			// Try Adding it again
			try {
				client.createContainer(containerName);
				fail("Allowed duplicate container creation");
			}
			catch (FilesContainerExistsException fcee) {
				// Pass this case
			}
			
			// See that it's still there
			assertTrue(client.containerExists(containerName));
			
			boolean found = false;
			List<FilesContainer> containers = client.listContainers();
			for (FilesContainer cont : containers) {
				// logger.warn(cont.getName());
				if(containerName.equals(cont.getName())) found = true;
			}
			assertTrue(found);
			
			
			// Delete it
			assertTrue(client.deleteContainer(containerName));
			
			// Make sure it's gone
			assertFalse(client.containerExists(containerName));
			

		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		} 
	}
	
	public void testContainerInfoListing() {
		FilesClient client = new FilesClient();
		try {
			assertTrue(client.login());
			String containerName = createTempContainerName("<container with\u1422 spaces>");
			
			// Make sure it's not there
			assertFalse(client.containerExists(containerName));
			
			// Add it
			client.createContainer(containerName);
			
			// See that it's there
			assertTrue(client.containerExists(containerName));
			assertNotNull(client.getContainerInfo(containerName));
			
			// Try Adding it again
			try {
				client.createContainer(containerName);
				fail("Allowed duplicate container creation");
			}
			catch (FilesContainerExistsException fcee) {
				// Hooray!
			}
			
			// See that it's still there
			assertTrue(client.containerExists(containerName));
			
			boolean found = false;
			List<FilesContainerInfo> containers = client.listContainersInfo();
			for (FilesContainerInfo info : containers) {
				if(containerName.equals(info.getName())) {
					found = true;
					assertEquals(0, info.getTotalSize());
					assertEquals(0, info.getObjectCount());
				}
			}
			assertTrue(found);
			
			
			// Delete it
			assertTrue(client.deleteContainer(containerName));
			
			// Make sure it's gone
			assertFalse(client.containerExists(containerName));
			

		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		} 
	}
	
	public void testUserAgent() {
		FilesClient client = new FilesClient();
		assertEquals(FilesConstants.USER_AGENT, client.getUserAgent());
		client.setUserAgent("Java-Test-User-Agent");
		assertEquals("Java-Test-User-Agent", client.getUserAgent());
		try {
			assertTrue(client.login());
			String containerName = createTempContainerName("user-agent");
			
			// Make sure it's not there
			assertFalse(client.containerExists(containerName));
			
			// Add it
			client.createContainer(containerName);
			
			// See that it's there
			assertTrue(client.containerExists(containerName));
			assertNotNull(client.getContainerInfo(containerName));
			
			// Delete it
			assertTrue(client.deleteContainer(containerName));
			
			// Make sure it's gone
			assertFalse(client.containerExists(containerName));
			

		} catch (Exception e) {
			fail(e.getMessage());
		} 
	}
	
	public void testContainerNameNoSlashes() {
		FilesClient client = new FilesClient();
		try {
			assertTrue(client.login());
			String containerName = createTempContainerName("/");
			
			// Make sure they're not there
			assertFalse(client.containerExists(containerName));
			
			// Try to add it
			boolean exceptionThrown = false;
			try {
				client.createContainer(containerName);
				fail("Should not have been able to create container: " + containerName);
			}
			catch (FilesInvalidNameException fine) {
				exceptionThrown = true;
			}
			assertTrue(exceptionThrown);
		} catch (Exception e) {
			fail(e.getMessage());
		} 
	}
	public void testCustomHttpClient() {
		String containerName = createTempContainerName("customHttp");
		String filename = makeFileName("customHttpFile");
		String fullPath = FilenameUtils.concat(SYSTEM_TMP.getAbsolutePath(), filename);
		logger.debug("Test File Location: " + fullPath);
		try {
			byte randomData[] = makeRandomFile(fullPath);
			
			DefaultHttpClient httpClient = new DefaultHttpClient();
			
			FilesClient client = new FilesClient(httpClient,
					FilesUtil.getProperty("username"), 
					FilesUtil.getProperty("password"),
					null,
					FilesUtil.getProperty("account"), 
					FilesUtil.getIntProperty("connection_timeout"));
			assertTrue(client.login());
			
			// Set up
			client.createContainer(containerName);
			
			// Store it
			logger.info("About to save: " + filename);
			assertNotNull(client.storeObjectAs(containerName, new File(fullPath), "application/octet-stream", filename));
			
			// Make sure it's there
			List<FilesObject> objects = client.listObjects(containerName);
			assertEquals(1, objects.size());
			FilesObject obj = objects.get(0);
			assertEquals(filename, obj.getName());
			assertEquals("application/octet-stream", obj.getMimeType());
			
			// Make sure the data is correct
			assertArrayEquals(randomData, client.getObject(containerName, filename));
			
			// Make sure the data is correct as a stream
			InputStream is = client.getObjectAsStream(containerName, filename);
			byte otherData[] = new byte[NUMBER_RANDOM_BYTES];
			is.read(otherData);
			assertArrayEquals(randomData, otherData);
			assertEquals(-1, is.read()); // Could hang if there's a bug on the other end
			
			// Clean up 
			client.deleteObject(containerName, filename);
			assertTrue(client.deleteContainer(containerName));
			
		}
		catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		finally {
			File f = new File(fullPath);
			f.delete();
		}
		
	}
	
	public void testFileSaving() {
		String containerName = createTempContainerName("file-test");
		String filename = makeFileName("random");
		String fullPath = FilenameUtils.concat(SYSTEM_TMP.getAbsolutePath(), filename);
		logger.info("Test File Location: " + fullPath);
		try {
			byte randomData[] = makeRandomFile(fullPath);
			FilesClient client = new FilesClient();
			assertTrue(client.login());
			
			// Set up
			client.createContainer(containerName);
			
			// Store it
			logger.info("About to save: " + filename);
			assertNotNull(client.storeObjectAs(containerName, new File(fullPath), "application/octet-stream", filename));
			
			// Make sure it's there
			List<FilesObject> objects = client.listObjects(containerName);
			assertEquals(1, objects.size());
			FilesObject obj = objects.get(0);
			assertEquals(filename, obj.getName());
			assertEquals("application/octet-stream", obj.getMimeType());
			
			// Make sure the data is correct
			assertArrayEquals(randomData, client.getObject(containerName, filename));
			
			// Make sure the data is correct as a stream
			InputStream is = client.getObjectAsStream(containerName, filename);
			byte otherData[] = new byte[NUMBER_RANDOM_BYTES];
			is.read(otherData);
			assertArrayEquals(randomData, otherData);
			assertEquals(-1, is.read()); // Could hang if there's a bug on the other end
			
			// Clean up 
			client.deleteObject(containerName, filename);
			assertTrue(client.deleteContainer(containerName));
			
		}
		catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		finally {
			File f = new File(fullPath);
			f.delete();
		}
		
	}
	public void testSlashInName() {
		String containerName = createTempContainerName("slashTest");
		String filename = makeFileName("slash/backslash\\slash");
		try {
			byte randomData[] = makeRandomBytes();
			FilesClient client = new FilesClient();
			// client.setUseETag(false);
			assertTrue(client.login());
			
			// Set up
			client.createContainer(containerName);
			
			// Store it
			assertTrue(client.storeObject(containerName, randomData, "application/octet-stream", filename, new HashMap<String,String>()));
			
			// Make sure it's there
			List<FilesObject> objects = client.listObjects(containerName);
			assertEquals(1, objects.size());
			FilesObject obj = objects.get(0);
			assertEquals(filename, obj.getName());
			assertEquals("application/octet-stream", obj.getMimeType());
			
			// Make sure the data is correct
			assertArrayEquals(randomData, client.getObject(containerName, filename));
			
			// Make sure the data is correct as a stream
			InputStream is = client.getObjectAsStream(containerName, filename);
			byte otherData[] = new byte[NUMBER_RANDOM_BYTES];
			is.read(otherData);
			assertArrayEquals(randomData, otherData);
			assertEquals(-1, is.read()); // Could hang if there's a bug on the other end
			
			// Clean up 
			client.deleteObject(containerName, filename);
			assertTrue(client.deleteContainer(containerName));
			
		}
		catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}		
	}
	public void testSaveAs() {
		String containerName = createTempContainerName("file-test");
		String filename = makeFileName("random");
		String fullPath = FilenameUtils.concat(SYSTEM_TMP.getAbsolutePath(), filename);
		String otherFileName = "Bob";
		logger.info("Test File Location: " + fullPath);
		try {
			byte randomData[] = makeRandomFile(fullPath);
			FilesClient client = new FilesClient();
			assertTrue(client.login());
			
			// Set up
			client.createContainer(containerName);
			
			// Store it
			logger.info("About to save: " + filename);
			assertNotNull(client.storeObjectAs(containerName, new File(fullPath), "application/octet-stream", otherFileName));
			
			// Make sure it's there
			List<FilesObject> objects = client.listObjects(containerName);
			assertEquals(1, objects.size());
			FilesObject obj = objects.get(0);
			assertEquals("Bob", obj.getName());
			assertEquals("application/octet-stream", obj.getMimeType());
			
			// Make sure the data is correct
			assertArrayEquals(randomData, client.getObject(containerName, otherFileName));
			
			// Make sure the data is correct as a stream
			InputStream is = client.getObjectAsStream(containerName, otherFileName);
			byte otherData[] = new byte[NUMBER_RANDOM_BYTES];
			is.read(otherData);
			assertArrayEquals(randomData, otherData);
			assertEquals(-1, is.read()); // Could hang if there's a bug on the other end
			
			// Clean up 
			client.deleteObject(containerName, otherFileName);
			assertTrue(client.deleteContainer(containerName));
			
		}
		catch (Exception e) {
			fail(e.getMessage());
		}
		finally {
			File f = new File(fullPath);
			f.delete();
		}
		
	}
	public void testFileSavingWithCallback() {
		String containerName = createTempContainerName("file-test");
		String filename = makeFileName("random");
		String fullPath = FilenameUtils.concat(SYSTEM_TMP.getAbsolutePath(), filename);
		logger.info("Test File Location: " + fullPath);
		try {
			byte randomData[] = makeRandomFile(fullPath);
			FilesClient client = new FilesClient();
			assertTrue(client.login());
			
			// Set up
			client.createContainer(containerName);
			
			// Store it
			logger.info("About to save: " + filename);
			TesterCallback callback = new TesterCallback();
			assertNotNull(client.storeObjectAs(containerName, new File(fullPath), "application/octet-stream", filename, callback));
			
			// Make sure the callback was called 
			assertEquals(randomData.length, callback.bytesSent);
			assertEquals(1, callback.nCalls);
			
			// Make sure it's there
			List<FilesObject> objects = client.listObjects(containerName);
			assertEquals(1, objects.size());
			FilesObject obj = objects.get(0);
			assertEquals(filename, obj.getName());
			assertEquals("application/octet-stream", obj.getMimeType());
			
			// Make sure the data is correct
			assertArrayEquals(randomData, client.getObject(containerName, filename));
			
			// Make sure the data is correct as a stream
			InputStream is = client.getObjectAsStream(containerName, filename);
			byte otherData[] = new byte[NUMBER_RANDOM_BYTES];
			is.read(otherData);
			assertArrayEquals(randomData, otherData);
			assertEquals(-1, is.read()); // Could hang if there's a bug on the other end
			
			// Clean up 
			client.deleteObject(containerName, filename);
			assertTrue(client.deleteContainer(containerName));
			
		}
		catch (Exception e) {
			e.printStackTrace();
			if (e.getCause() != null) e.getCause().printStackTrace();
			fail(e.getMessage());
		}
		finally {
			File f = new File(fullPath);
			f.delete();
		}		
	}
	public void testFileSavingWithMetadata() {
		String containerName = createTempContainerName("meta-data-test");
		String filename = makeFileName("random-with-meta");
		String fullPath = FilenameUtils.concat(SYSTEM_TMP.getAbsolutePath(), filename);
		logger.info("Test File Location: " + fullPath);
		try {
			byte randomData[] = makeRandomFile(fullPath);
			FilesClient client = new FilesClient();
			assertTrue(client.login());
			
			// Set up
			client.createContainer(containerName);
			
			// Store it
			HashMap<String,String> meta = new HashMap<String,String>();
			meta.put("Foo", "bar");
			meta.put("Uni", "\u0169\u00f1\u00efcode-test");
			meta.put("Width", "336");
			meta.put("Height", "183");
			assertNotNull(client.storeObjectAs(containerName, new File(fullPath), "application/octet-stream", filename, meta));
			
			// Make sure it's there
			List<FilesObject> objects = client.listObjects(containerName);
			assertEquals(1, objects.size());
			FilesObject obj = objects.get(0);
			assertEquals(filename, obj.getName());
			assertEquals("application/octet-stream", obj.getMimeType());
			
			// Make sure the data is correct
			assertArrayEquals(randomData, client.getObject(containerName, filename));
			
			// Make sure the data is correct as a stream
			InputStream is = client.getObjectAsStream(containerName, filename);
			byte otherData[] = new byte[NUMBER_RANDOM_BYTES];
			is.read(otherData);
			assertArrayEquals(randomData, otherData);
			assertEquals(-1, is.read()); // Could hang if there's a bug on the other end
			
			// Make sure the metadata is correct
			FilesObjectMetaData metadata = client.getObjectMetaData(containerName, filename);
			assertNotNull(metadata);
			Map<String,String> serverMetadata = metadata.getMetaData();
			assertEquals(meta.size(), serverMetadata.size());
			for(String key : meta.keySet()) {
				assertTrue(serverMetadata.containsKey(key));
				assertEquals(meta.get(key), serverMetadata.get(key));
			}
			
			// Clean up 
			client.deleteObject(containerName, filename);
			assertTrue(client.deleteContainer(containerName));
			
		}
		catch (FilesException e) {
			e.printStackTrace();
			fail(e.getHttpStatusMessage() + ":" + e.getMessage());
		}
		catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		finally {
			File f = new File(fullPath);
			f.delete();
		}
		
	}

	public void testMetadataUpdate() {
		String containerName = createTempContainerName("metadata-update");
		String filename = makeFileName("random-with-meta");
		String fullPath = FilenameUtils.concat(SYSTEM_TMP.getAbsolutePath(), filename);
		logger.info("Test File Location: " + fullPath);
		try {
			makeRandomFile(fullPath);
			
			// DefaultHttpClient is single threaded, which will catch a big we've seen with not
			// releasing the connection
			DefaultHttpClient httpClient = new DefaultHttpClient();
			FilesClient client = new FilesClient(httpClient,
					FilesUtil.getProperty("username"), 
					FilesUtil.getProperty("password"),
					null,
					FilesUtil.getProperty("account"), 
					FilesUtil.getIntProperty("connection_timeout"));
			assertTrue(client.login());

			// Set up
			client.createContainer(containerName);

			// Store it
			HashMap<String,String> meta = new HashMap<String,String>();
			meta.put("Foo", "bar");
			meta.put("Uni", "\u0169\u00f1\u00efcode-test");
			meta.put("Width", "336");
			meta.put("Height", "183");
			assertNotNull(client.storeObjectAs(containerName, new File(fullPath), "application/octet-stream", filename, meta));

			// Make sure it's there
			List<FilesObject> objects = client.listObjects(containerName);
			assertEquals(1, objects.size());
			FilesObject obj = objects.get(0);
			assertEquals(filename, obj.getName());
			assertEquals("application/octet-stream", obj.getMimeType());

			// Make sure the metadata is correct
			FilesObjectMetaData metadata = client.getObjectMetaData(containerName, filename);
			assertNotNull(metadata);
			Map<String,String> serverMetadata = metadata.getMetaData();
			assertEquals(meta.size(), serverMetadata.size());
			for(String key : meta.keySet()) {
				assertTrue(serverMetadata.containsKey(key));
				assertEquals(meta.get(key), serverMetadata.get(key));
			}

			// Make sure we can update
			meta.put("Foo", "one");
			meta.put("Uni", "\u0169\u00f1\u00eftwo");
			meta.put("Width", "three");
			meta.put("Height", "four");
			assertTrue(client.updateObjectMetadata(containerName, filename, meta));

			// Re-fect and make sure things are the same
			objects = client.listObjects(containerName);
			assertEquals(1, objects.size());
			obj = objects.get(0);
			assertEquals(filename, obj.getName());
			assertEquals("application/octet-stream", obj.getMimeType());
			metadata = client.getObjectMetaData(containerName, filename);
			assertNotNull(metadata);
			serverMetadata = metadata.getMetaData();
			assertEquals(meta.size(), serverMetadata.size());
			for(String key : meta.keySet()) {
				assertTrue(serverMetadata.containsKey(key));
				assertEquals(meta.get(key), serverMetadata.get(key));
			}
			// Clean up 
			client.deleteObject(containerName, filename);
			assertTrue(client.deleteContainer(containerName));

		}
		catch (FilesException e) {
			e.printStackTrace();
			fail(e.getHttpStatusMessage() + ":" + e.getMessage());
		}
		catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		finally {
			File f = new File(fullPath);
			f.delete();
		}
	}
	
	public void testFileSavingNoETag() {
		String containerName = createTempContainerName("etagless");
		String filename = makeFileName("etagless");
		String fullPath = FilenameUtils.concat(SYSTEM_TMP.getAbsolutePath(), filename);
		logger.info("Test File Location: " + fullPath);
		try {
			byte randomData[] = makeRandomFile(fullPath);
			FilesClient client = new FilesClient();
			assertTrue(client.getUseETag());
			client.setUseETag(false);
			assertFalse(client.getUseETag());

			assertTrue(client.login());
			
			// Set up
			client.createContainer(containerName);
			
			// Store it
			assertNotNull(client.storeObjectAs(containerName, new File(fullPath), "application/octet-stream", filename));
			
			// Make sure it's there
			List<FilesObject> objects = client.listObjects(containerName);
			assertEquals(1, objects.size());
			FilesObject obj = objects.get(0);
			assertEquals(filename, obj.getName());
			assertEquals("application/octet-stream", obj.getMimeType());
			
			// Make sure the data is correct
			assertArrayEquals(randomData, client.getObject(containerName, filename));
			
			// Make sure the data is correct as a stream
			InputStream is = client.getObjectAsStream(containerName, filename);
			byte otherData[] = new byte[NUMBER_RANDOM_BYTES];
			is.read(otherData);
			assertArrayEquals(randomData, otherData);
			assertEquals(-1, is.read()); // Could hang if there's a bug on the other end
			
			// Clean up 
			client.deleteObject(containerName, filename);
			assertTrue(client.deleteContainer(containerName));
			
		}
		catch (Exception e) {
			fail(e.getMessage());
		}
		finally {
			File f = new File(fullPath);
			f.delete();
		}
		
	}

    public void testCopyObject() throws Exception {
        String containerSrc = createTempContainerName("copy-source");
        String containerDest = createTempContainerName("copy-dest");
        doTestCopyObject(containerSrc, containerDest);
    }

    public void testCopyObjectSameContainer() throws Exception {
        String containerSrc = createTempContainerName("copy-source1");
        doTestCopyObject(containerSrc, containerSrc);
    }

    public void testCopyObjectNoDestContainer() throws Exception {
        String containerSrc = createTempContainerName("copy-source2");
        String containerDest = null;

        try {
            System.err.println("-- expected 404 exception below --");
            doTestCopyObject(containerSrc, containerDest);
            fail("exception expected");

        } catch (FilesException fe) {
            assertEquals(404, fe.getHttpStatusCode());
        }
    }

    public void doTestCopyObject(String containerSrc, String containerDest)
        throws Exception {
        String filename = makeFileName("copy");
        String fullPath = FilenameUtils.concat(SYSTEM_TMP.getAbsolutePath(),
                                               filename);
        logger.info("Test Copy File Location: " + fullPath);
        try {
            byte randomData[] = makeRandomFile(fullPath);
            FilesClient client = new FilesClient();
            assertTrue(client.login());

            // Set up
            client.createContainer(containerSrc);
            if (null != containerDest && !containerSrc.equals(containerDest)) {
                client.createContainer(containerDest);
            }

            // Store it
            logger.info("About to save: " + filename);
            String mime = "application/octet-stream";
            assertNotNull(client.storeObjectAs(containerSrc,
                                               new File(fullPath),
                                               mime,
                                               filename));

            verifyStoredObject(containerSrc,
                               filename,
                               mime,
                               randomData,
                               client);

            // Do the copy
            logger.info("About to copy: " + filename);
            String dest = null == containerDest ? "not-exist" : containerDest;
            String etag = client.copyObject(containerSrc,
                                            filename,
                                            dest,
                                            filename);

            String md5 = FilesClient.md5Sum(randomData);
            assertEquals(md5, etag);

            verifyStoredObject(containerDest,
                               filename,
                               mime,
                               randomData,
                               client);

            // Clean up
            client.deleteObject(containerSrc, filename);
            assertTrue(client.deleteContainer(containerSrc));
            if (null != containerDest && !containerSrc.equals(containerDest)) {
                client.deleteObject(containerDest, filename);
                assertTrue(client.deleteContainer(containerDest));
            }

        } catch (FilesException fe) {
            System.err.println(fe.getHttpHeadersAsString());
            System.err.println(fe.getHttpStatusMessage());
            System.err.println(fe.getHttpStatusCode());
            System.err.println(fe.getMessage());
            fe.printStackTrace();
            throw fe;

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println(e.getMessage());
            throw e;

        } finally {
            File f = new File(fullPath);
            f.delete();
        }
    }

    private void verifyStoredObject(String container,
                                    String objName,
                                    String mime,
                                    byte[] data,
                                    FilesClient client)
        throws IOException, HttpException {

        // Make sure it's there
        List<FilesObject> objects = client.listObjects(container);
        assertEquals(1, objects.size());
        FilesObject obj = objects.get(0);
        assertEquals(objName, obj.getName());
        assertEquals(mime, obj.getMimeType());

        // Make sure the data is correct
        assertArrayEquals(data, client.getObject(container, objName));

        // Make sure the data is correct as a stream
        InputStream is = client.getObjectAsStream(container, objName);
        byte otherData[] = new byte[NUMBER_RANDOM_BYTES];
        is.read(otherData);
        assertArrayEquals(data, otherData);

        // Could hang if there's a bug on the other end
        assertEquals(-1, is.read());
    }

    public void testContainerListing() {
		String containerName = createTempContainerName("<container>");
		String filename = makeFileName("<object>");
		String fullPath = FilenameUtils.concat(SYSTEM_TMP.getAbsolutePath(), filename);
		logger.info("Test File Location: " + fullPath);
		try {
			byte randomData[] = makeRandomFile(fullPath);
			FilesClient client = new FilesClient();
			assertTrue(client.login());
			
			// Set up
			client.createContainer(containerName);
			
			// Store it
			logger.info("About to save: " + filename);
			assertNotNull(client.storeObjectAs(containerName, new File(fullPath), "application/octet-stream", filename));
			
			// Make sure it's there
			List<FilesObject> objects = client.listObjects(containerName);
			assertEquals(1, objects.size());
			FilesObject obj = objects.get(0);
			assertEquals(filename, obj.getName());
			assertEquals("application/octet-stream", obj.getMimeType());
			assertEquals(NUMBER_RANDOM_BYTES, obj.getSize());
			assertEquals(md5Sum(randomData), obj.getMd5sum());
			
			// Make sure the data is correct
			assertArrayEquals(randomData, client.getObject(containerName, filename));
			
			// Make sure the data is correct as a stream
			InputStream is = client.getObjectAsStream(containerName, filename);
			byte otherData[] = new byte[NUMBER_RANDOM_BYTES];
			is.read(otherData);
			assertArrayEquals(randomData, otherData);
			assertEquals(-1, is.read()); // Could hang if there's a bug on the other end
			
			// Clean up 
			client.deleteObject(containerName, filename);
			assertTrue(client.deleteContainer(containerName));
			
		}
		catch (Exception e) {
			fail(e.getMessage());
		}
		finally {
			File f = new File(fullPath);
			f.delete();
		}
		
	}

	public void testContainerListingWithXML() {
		String containerName = createTempContainerName("<container>");
		String filename = makeFileName("</name></object>");
		try {
			byte randomData[] = makeRandomBytes();
			FilesClient client = new FilesClient();
			assertTrue(client.login());
			
			// Set up
			client.createContainer(containerName);
			
			// Store it
			logger.info("About to save: " + filename);
			assertTrue(client.storeObject(containerName, randomData, "application/octet-stream", filename, new HashMap<String,String>()));
				
			// Make sure it's there
			List<FilesObject> objects = client.listObjects(containerName);
			assertEquals(1, objects.size());
			FilesObject obj = objects.get(0);
			assertEquals(filename, obj.getName());
			assertEquals("application/octet-stream", obj.getMimeType());
			assertEquals(NUMBER_RANDOM_BYTES, obj.getSize());
			assertEquals(md5Sum(randomData), obj.getMd5sum());
			
			// Make sure the data is correct
			assertArrayEquals(randomData, client.getObject(containerName, filename));
			
			// Make sure the data is correct as a stream
			InputStream is = client.getObjectAsStream(containerName, filename);
			byte otherData[] = new byte[NUMBER_RANDOM_BYTES];
			is.read(otherData);
			assertArrayEquals(randomData, otherData);
			assertEquals(-1, is.read()); // Could hang if there's a bug on the other end
			
			// Clean up 
			client.deleteObject(containerName, filename);
			assertTrue(client.deleteContainer(containerName));
			
		}
		catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		
	}

	public void testByteArraySaving() {
		String containerName = createTempContainerName("byte-array");
		String filename = makeFileName("bytearray");
		try {
			byte randomData[] = makeRandomBytes();
			FilesClient client = new FilesClient();
			// client.setUseETag(false);
			assertTrue(client.login());
			
			// Set up
			client.createContainer(containerName);
			
			// Store it
			assertTrue(client.storeObject(containerName, randomData, "application/octet-stream", filename, new HashMap<String,String>()));
			
			// Make sure it's there
			List<FilesObject> objects = client.listObjects(containerName);
			assertEquals(1, objects.size());
			FilesObject obj = objects.get(0);
			assertEquals(filename, obj.getName());
			assertEquals("application/octet-stream", obj.getMimeType());
			
			// Make sure the data is correct
			assertArrayEquals(randomData, client.getObject(containerName, filename));
			
			// Make sure the data is correct as a stream
			InputStream is = client.getObjectAsStream(containerName, filename);
			byte otherData[] = new byte[NUMBER_RANDOM_BYTES];
			is.read(otherData);
			assertArrayEquals(randomData, otherData);
			assertEquals(-1, is.read()); // Could hang if there's a bug on the other end
			
			// Clean up 
			client.deleteObject(containerName, filename);
			assertTrue(client.deleteContainer(containerName));
			
		}
		catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}		
	}

	public void testLineNoiseSaving() {
		String containerName = createTempContainerName("[]<>{}!@#$%^&*()_-+=|,.?");
		String filename = makeFileName("/[]<>{}!@#$%^&*()_-+=|,.?/");
		try {
			byte randomData[] = makeRandomBytes();
			FilesClient client = new FilesClient();
			// client.setUseETag(false);
			assertTrue(client.login());

			// Set up
			client.createContainer(containerName);
			
			// Store it 
			assertTrue(client.storeObject(containerName, randomData, "application/octet-stream", filename, new HashMap<String,String>()));
			
			// Make sure it's there
			List<FilesObject> objects = client.listObjects(containerName);
			assertEquals(1, objects.size());
			FilesObject obj = objects.get(0);
			assertEquals(filename, obj.getName());
			assertEquals("application/octet-stream", obj.getMimeType());
			
			// Make sure the data is correct
			assertArrayEquals(randomData, client.getObject(containerName, filename));
			
			// Make sure the data is correct as a stream
			InputStream is = client.getObjectAsStream(containerName, filename);
			byte otherData[] = new byte[NUMBER_RANDOM_BYTES];
			is.read(otherData);
			assertArrayEquals(randomData, otherData);
			assertEquals(-1, is.read()); // Could hang if there's a bug on the other end
			
			// Clean up 
			client.deleteObject(containerName, filename);
			assertTrue(client.deleteContainer(containerName));
			
		}
		catch (Exception e) {
			logger.error("LGV: LINE NOISE", e);
			e.printStackTrace();
			fail(e.getMessage());
		}		
	}

	public void testRequestEntitySaving() {
		String containerName = createTempContainerName("requst-entity");
		String filename = makeFileName("req-entity");
		try {
			byte randomData[] = makeRandomBytes();
			FilesClient client = new FilesClient();
			assertTrue(client.login());
			
			// Set up
			client.createContainer(containerName);
			
			// Store it
			ByteArrayEntity entity = new ByteArrayEntity(randomData);
			entity.setContentType("test/content_type");

			assertNotNull(client.storeObjectAs(containerName, filename, entity, new HashMap<String,String>(), FilesClient.md5Sum(randomData)));
			
			// Make sure it's there
			List<FilesObject> objects = client.listObjects(containerName);
			assertEquals(1, objects.size());
			FilesObject obj = objects.get(0);
			assertEquals(filename, obj.getName());
			assertEquals("test/content_type", obj.getMimeType());
			
			// Make sure the data is correct
			assertArrayEquals(randomData, client.getObject(containerName, filename));
			
			// Make sure the data is correct as a stream
			InputStream is = client.getObjectAsStream(containerName, filename);
			byte otherData[] = new byte[NUMBER_RANDOM_BYTES];
			is.read(otherData);
			assertArrayEquals(randomData, otherData);
			assertEquals(-1, is.read()); // Could hang if there's a bug on the other end
			
			// Clean up 
			client.deleteObject(containerName, filename);
			assertTrue(client.deleteContainer(containerName));
			
		}
		catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}		
	}

	public void testObjectListing() {
		String containerName = createTempContainerName("object-listing-marker");
		try {
			byte randomData[] = makeRandomBytes();
			FilesClient client = new FilesClient();
			// client.setUseETag(false);
			assertTrue(client.login());
			
			// Set up
			client.createContainer(containerName);
			
			// Store it
			for (int i=0; i < 10; i++) {
				assertTrue(client.storeObject(containerName, randomData, "application/octet-stream", "testfile" + i + ".bogus", new HashMap<String,String>()));
			}
			
			// Make sure it's there
			List<FilesObject> objects = client.listObjects(containerName);
			assertEquals(10, objects.size());
			for (int i=0; i < 10; i++) {
				FilesObject obj = objects.get(i);
				assertEquals("testfile" + i + ".bogus", obj.getName());
				assertEquals("application/octet-stream", obj.getMimeType());
			}

			// Now do a limit
			objects = client.listObjects(containerName, 3);
			assertEquals(3, objects.size());
			for (int i=0; i < 3; i++) {
				FilesObject obj = objects.get(i);
				assertEquals("testfile" + i + ".bogus", obj.getName());
				assertEquals("application/octet-stream", obj.getMimeType());
			}
			
			// Now check out a marker
			objects = client.listObjects(containerName, 4, "testfile3.bogus");
			assertEquals(4, objects.size());
			for (int i=0; i < 4; i++) {
				FilesObject obj = objects.get(i);
				assertEquals("testfile" + (i + 4) + ".bogus", obj.getName());
				assertEquals("application/octet-stream", obj.getMimeType());
			}
			
			// Clean up 
			for (int i=0; i < 10; i++) {
				client.deleteObject(containerName, "testfile" + i + ".bogus");
			}
			assertTrue(client.deleteContainer(containerName));
			
		}
		catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}		
	}

	public void testObjectListingWithDelimiter() {
		String containerName = createTempContainerName("delimiter");
		try {
			byte randomData[] = makeRandomBytes();
			FilesClient client = new FilesClient();
			assertTrue(client.login());
			
			// Set up
			client.createContainer(containerName);
			
			// Store it
			for (int i=0; i < 10; i++) {
				assertTrue(client.storeObject(containerName, randomData, "application/octet-stream", "foo/testfile" + i + ".bogus", new HashMap<String,String>()));
				assertTrue(client.storeObject(containerName, randomData, "application/octet-stream", "bar/testfile" + i + ".bogus", new HashMap<String,String>()));
			}
			
			// Make sure it's there
			List<FilesObject> objects = client.listObjects(containerName);
			assertEquals(20, objects.size());

			// Now check with delimiter
			objects = client.listObjects(containerName, new Character('/'));
			assertEquals(2, objects.size());
			for(FilesObject obj : objects) {
				assertTrue(obj.isDirectory());
			}
			
			objects = client.listObjects(containerName, "foo", new Character('/'));
			assertEquals(10, objects.size());
			for (FilesObject obj : objects) {
				assertTrue(obj.getName().startsWith("foo/"));
			}
			
			// Clean up 
			for (int i=0; i < 10; i++) {
				client.deleteObject(containerName, "foo/testfile" + i + ".bogus");
				client.deleteObject(containerName, "bar/testfile" + i + ".bogus");
			}
			assertTrue(client.deleteContainer(containerName));
			
		}
		catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}		
	}

	public void testContainerListingWithLimitMarker() {
		try {
			FilesClient client = new FilesClient();
			assertTrue(client.login());
			
			// Populate our  account
			for (int i=0; i < 20; i++) {
				client.createContainer("test_container_" + i);
			}
			
			// Make sure it's there
			List<FilesContainer> originalContainers = client.listContainers();
			assertTrue(20 <=originalContainers.size());

			// Now do a limit
			List<FilesContainer> containers = client.listContainers(5);
			assertEquals(5, containers.size());
			for (int i=0; i < 5; i++) {
				FilesContainer container = containers.get(i);
				assertEquals(originalContainers.get(i).getName(), container.getName());
			}
			
			// Now check out a marker
			containers = client.listContainers(10, originalContainers.get(originalContainers.size() - 5).getName());
			assertEquals(4, containers.size());
			for (int i=0; i < 2; i++) {
				FilesContainer container = containers.get(i);
				assertEquals(originalContainers.get(originalContainers.size() - 4 + i).getName(), container.getName());
			}
			
			// Clean up 
			for (int i=0; i < 20; i++) {
				assertTrue(client.deleteContainer("test_container_" + i));
			}	
		}
		catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}		
	}

	public void testByteArraySavingWithCallback() {
		String containerName = createTempContainerName("byte-array");
		String filename = makeFileName("bytearray");
		try {
			byte randomData[] = makeRandomBytes(1024 * 100); // 100 K to make sure we do more with the callback
			FilesClient client = new FilesClient();
			// client.setUseETag(false);
			assertTrue(client.login());
			
			// Set up
			client.createContainer(containerName);
			
			// Store it
			TesterCallback callback = new TesterCallback();
			assertTrue(client.storeObject(containerName, randomData, "application/octet-stream", filename, new HashMap<String,String>(), callback));
			
			// Make sure it all got written
			assertEquals(randomData.length, callback.bytesSent);
			assertEquals(randomData.length/8192 + 1, callback.nCalls);
			
			// Make sure it's there
			List<FilesObject> objects = client.listObjects(containerName);
			assertEquals(1, objects.size());
			FilesObject obj = objects.get(0);
			assertEquals(filename, obj.getName());
			assertEquals("application/octet-stream", obj.getMimeType());
			
			// Make sure the data is correct
			assertArrayEquals(randomData, client.getObject(containerName, filename));
			
			// Make sure the data is correct as a stream
			InputStream is = client.getObjectAsStream(containerName, filename);
			byte otherData[] = new byte[randomData.length];
			int loc = 0;
			int bytesRead = 0;
			while ((bytesRead = is.read(otherData, loc, otherData.length - loc)) > 0) {
				loc += bytesRead;
			}
			assertArrayEquals(randomData, otherData);
			assertEquals(-1, is.read()); // Could hang if there's a bug on the other end
			
			// Clean up 
			client.deleteObject(containerName, filename);
			assertTrue(client.deleteContainer(containerName));
			
		}
		catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}		
	}

	public void testStreamedSaving() {
		String containerName = createTempContainerName("streamed");
		String filename = makeFileName("streamed");
		try {
			byte randomData[] = makeRandomBytes(1024 * 100); // 100 K to make sure it's interesting
			FilesClient client = new FilesClient();
			assertTrue(client.login());
			
			// Set up
			client.createContainer(containerName);
			
			// Store it
			assertNotNull(client.storeStreamedObject(containerName, new ByteArrayInputStream(randomData), "application/octet-stream", filename, new HashMap<String,String>()));
			
			// Make sure it's there
			List<FilesObject> objects = client.listObjects(containerName);
			assertEquals(1, objects.size());
			FilesObject obj = objects.get(0);
			assertEquals(filename, obj.getName());
			assertEquals("application/octet-stream", obj.getMimeType());
			
			// Make sure the data is correct
			assertArrayEquals(randomData, client.getObject(containerName, filename));
			
			// Make sure the data is correct as a stream
			InputStream is = client.getObjectAsStream(containerName, filename);
			byte otherData[] = new byte[randomData.length];
			int loc = 0;
			int bytesRead = 0;
			while ((bytesRead = is.read(otherData, loc, otherData.length - loc)) > 0) {
				loc += bytesRead;
			}
			assertArrayEquals(randomData, otherData);
			assertEquals(-1, is.read()); // Could hang if there's a bug on the other end
			
			// Clean up 
			client.deleteObject(containerName, filename);
			assertTrue(client.deleteContainer(containerName));
			
		}
		catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}		
	}

	public void testMD5IssueSaving() {
		String containerName = createTempContainerName("byte-array");
		String filename = makeFileName("bytearray");
		try {
			byte randomData[] = makeRandomBytes();
			
			while(zeroStripMd5Sum(randomData).length() ==32) {
				randomData = makeRandomBytes();
			}
			FilesClient client = new FilesClient();
			// client.setUseETag(false);
			assertTrue(client.login());
			
			// Set up
			client.createContainer(containerName);
			
			// Store it
			assertTrue(client.storeObject(containerName, randomData, "application/octet-stream", filename, new HashMap<String,String>()));
			
			// Make sure it's there
			List<FilesObject> objects = client.listObjects(containerName);
			assertEquals(1, objects.size());
			FilesObject obj = objects.get(0);
			assertEquals(filename, obj.getName());
			assertEquals("application/octet-stream", obj.getMimeType());
			
			// Make sure the data is correct
			assertArrayEquals(randomData, client.getObject(containerName, filename));
			
			// Make sure the data is correct as a stream
			InputStream is = client.getObjectAsStream(containerName, filename);
			byte otherData[] = new byte[NUMBER_RANDOM_BYTES];
			is.read(otherData);
			assertArrayEquals(randomData, otherData);
			assertEquals(-1, is.read()); // Could hang if there's a bug on the other end
			
			// Clean up 
			client.deleteObject(containerName, filename);
			assertTrue(client.deleteContainer(containerName));
			
		}
		catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}		
	}

    private static String zeroStripMd5Sum (byte[] data) throws IOException, NoSuchAlgorithmException
    {
    	MessageDigest digest = MessageDigest.getInstance("MD5");

    	byte[] md5sum = digest.digest(data);
    	BigInteger bigInt = new BigInteger(1, md5sum);

    	return bigInt.toString(16);
    }
    
    private static String md5Sum (byte[] data) throws IOException, NoSuchAlgorithmException
    {
    	MessageDigest digest = MessageDigest.getInstance("MD5");

    	byte[] md5sum = digest.digest(data);
    	BigInteger bigInt = new BigInteger(1, md5sum);
    	
    	String result = bigInt.toString(16);
    	
    	while(result.length() < 32) {
    		result = "0" + result;
    	}

    	return result;
    }
    	
	public void testUnicodeContainer() {
		String containerName = createTempContainerName("\u0169\u00f1\u00efcode-test-\u03de");
		try {
			FilesClient client = new FilesClient(FilesUtil.getProperty("username"), FilesUtil.getProperty("password"), FilesUtil.getProperty("account"));
			assertTrue(client.login());
			
			// Set up
			client.createContainer(containerName);
			
			
			// Make sure it's there
			assertTrue(client.containerExists(containerName));

			// Make sure we can get the container info
			assertNotNull(client.getContainerInfo(containerName));
			
			// Clean up 
			assertTrue(client.deleteContainer(containerName));
			
		}
		catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}		
	}

	public void testUnicode() {
		String containerName = createTempContainerName("\u0169\u00f1\u00efcode-test-\u03de");
		String filename = makeFileName("unicode_\u03DA_\u2042");
		filename = makeFileName("\u00f1\u00efcode-test-\u03de");
		String fullPath = FilenameUtils.concat(SYSTEM_TMP.getAbsolutePath(), filename);
		logger.info("Test File Location: " + fullPath);
		try {
			byte randomData[] = makeRandomFile(fullPath);
			FilesClient client = new FilesClient(FilesUtil.getProperty("username"), FilesUtil.getProperty("password"), FilesUtil.getProperty("account"));
			assertTrue(client.login());
			
			// Set up
			client.createContainer(containerName);
			
			// Store it
			assertNotNull(client.storeObjectAs(containerName, new File(fullPath), "application/octet-stream", filename));
			
			// Make sure it's there
			List<FilesObject> objects = client.listObjects(containerName);
			assertEquals(1, objects.size());
			FilesObject obj = objects.get(0);
			assertEquals(filename, obj.getName());
			assertEquals("application/octet-stream", obj.getMimeType());
			
			assertNotNull(obj.getMetaData());
			
			// Make sure the data is correct
			assertArrayEquals(randomData, client.getObject(containerName, filename));
			
			// Make sure the data is correct as a stream
			InputStream is = client.getObjectAsStream(containerName, filename);
			byte otherData[] = new byte[NUMBER_RANDOM_BYTES];
			is.read(otherData);
			assertArrayEquals(randomData, otherData);
			assertEquals(-1, is.read()); // Could hang if there's a bug on the other end
			
			// Make sure we can get the container info
			assertNotNull(client.getContainerInfo(containerName));
			
			// Clean up 
			client.deleteObject(containerName, filename);
			assertTrue(client.deleteContainer(containerName));
			
		}
		catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		finally {
			File f = new File(fullPath);
			f.delete();
		}
		
	}
	
	public void testCDNContainerList() {
		FilesClient client = new FilesClient();
		try {
			assertTrue(client.login());
			
			List<String> containers = client.listCdnContainers();
			assertTrue(containers.size() > 0);
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		} 
	}
	
	public void testCDNContainerListLimitMarker() {
		FilesClient client = new FilesClient();
		try {
			assertTrue(client.login());
						
			List<String> originalContainers = client.listCdnContainers();
			assertTrue(originalContainers.size() > 0);
			
			// Now do a limit
			List<String> containers = client.listCdnContainers(5);
			assertEquals(5, containers.size());
			for (int i=0; i < 5; i++) {
				assertEquals(originalContainers.get(i), containers.get(i));
			}
			
			// Now check out a marker
			containers = client.listCdnContainers(10, originalContainers.get(originalContainers.size() - 5));
			assertEquals(4, containers.size());
			for (int i=0; i < 2; i++) {
				assertEquals(originalContainers.get(originalContainers.size() - 4 + i), containers.get(i));
			}
		} catch (Exception e) {
			fail(e.getMessage());
		} 
	}
	public void testCDNContainerFullListing() {
		FilesClient client = new FilesClient();
		try {
			assertTrue(client.login());
			
			List<String> originalContainers = client.listCdnContainers();
			assertTrue(originalContainers.size() > 0);
			
			// Now do a limit
			List<FilesCDNContainer> containers = client.listCdnContainerInfo(5);
			assertEquals(5, containers.size());
			for (int i=0; i < 5; i++) {
				assertEquals(originalContainers.get(i), containers.get(i).getName());
				assertNotNull(containers.get(i).getSSLURL());
				assertNotNull(containers.get(i).getStreamingURL());
			}
			
			// Now check out a marker
			containers = client.listCdnContainerInfo(10, originalContainers.get(originalContainers.size() - 5));
			assertEquals(4, containers.size());
			for (int i=0; i < 2; i++) {
				assertEquals(originalContainers.get(originalContainers.size() - 4 + i), containers.get(i).getName());
			}
		} catch (Exception e) {
			fail(e.getMessage());
		} 
	}
	
	public void testCDNUrlOnObject() {
		String containerName = createTempContainerName("cdnURLtest");
		String filename = makeFileName("cdnURLtest");
		try {
			byte randomData[] = makeRandomBytes();
			FilesClient client = new FilesClient();
			// client.setUseETag(false);
			assertTrue(client.login());
			
			// Set up
			client.createContainer(containerName);
			String cdnUrl = client.cdnEnableContainer(containerName); 
			
			// Store it
			assertTrue(client.storeObject(containerName, randomData, "application/octet-stream", filename, new HashMap<String,String>()));
			
			// Make sure it's there
			List<FilesObject> objects = client.listObjects(containerName);
			
			// See that the CDN URL works
			FilesObject obj = objects.get(0);
			assertEquals(cdnUrl + "/" + filename, obj.getCDNURL());
			
			// Clean up 
			client.deleteObject(containerName, filename);
			assertTrue(client.deleteContainer(containerName));
			
		} catch (Exception e) {
			fail(e.getMessage());
		} 
		
	}
	
	public void testCDNPurge() {
		String containerName = createTempContainerName("cdnPurgeTest");
		try {
			FilesClient client = new FilesClient();
			// client.setUseETag(false);
			assertTrue(client.login());
			
			// Set up
			client.createContainer(containerName);
			String cdnUrl = client.cdnEnableContainer(containerName); 
			assertNotNull(cdnUrl);
			
			//client.purgeCDNContainer(containerName, "lowell.vaughn@rackspace.com");
			//client.purgeCDNContainer(containerName, null);
			//client.purgeCDNObject(containerName, "object.txt", "lowell.vaughn@rackspace.com");
			client.purgeCDNObject(containerName, "object.txt", null);
			client.purgeCDNObject(containerName, "path/object.txt", null);
	
			assertTrue(client.deleteContainer(containerName));
			
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		} 
		
	}
	
	public void testCDNContainerFullListingAll() {
		FilesClient client = new FilesClient();
		try {
			assertTrue(client.login());
			String container = createTempContainerName("aaa_\u1422_aaa");
			client.cdnEnableContainer(container);
			// Now do a limit
			client.listCdnContainerInfo();
		} catch (Exception e) {
			fail(e.getMessage());
		} 
	}
	
	
	public void testCDNApi() {
		String containerName = createTempContainerName("java api Test\u03DA_\u2042\u03de#<>\u2043\u2042\u2044\u2045");
		//containerName = createTempContainerName("java Api Test no uniocde");
		//logger.warn("Container:" + containerName.length() + ":" + containerName);
		FilesClient client = new FilesClient();
		try {
			assertTrue(client.login());
			
			List<String> containers = client.listCdnContainers();
			int originalContainerListSize = containers.size();
			
			assertFalse(client.isCDNEnabled(containerName));
	
			String url = client.cdnEnableContainer(containerName);
			assertNotNull(url);
			assertTrue(client.isCDNEnabled(containerName));
			containers = client.listCdnContainers();
			assertEquals(originalContainerListSize + 1, containers.size());
			
			boolean found = false;
			for(String container : containers) {
				// logger.warn(container);
				if (containerName.equals(container)) found = true;
			}
			assertTrue(found);
			
			FilesCDNContainer info = client.getCDNContainerInfo(containerName);
			assertTrue(info.isEnabled());
//			assertEquals("", info.getUserAgentACL());
//			assertEquals("", info.getReferrerACL());
			String cdnUrl = info.getCdnURL();
			assertNotNull(cdnUrl);
			assertNotNull(info.getSSLURL());
			assertNotNull(info.getStreamingURL());
			
			client.cdnUpdateContainer(containerName, 31415, false, true);
			assertFalse(client.isCDNEnabled(containerName));
			info = client.getCDNContainerInfo(containerName);
			assertFalse(info.isEnabled());
			assertTrue(info.getRetainLogs());
			assertEquals(31415, info.getTtl());
			assertEquals(cdnUrl, info.getCdnURL());
			
			//client.cdnUpdateContainer(containerName, 54321, true, "Referrer Test", "User Agent Acl Test");
			client.cdnUpdateContainer(containerName, 54321, true, false);
			assertTrue(client.isCDNEnabled(containerName));
			info = client.getCDNContainerInfo(containerName);
			assertTrue(info.isEnabled());
			assertFalse(info.getRetainLogs());
			assertEquals(54321, info.getTtl());
			assertEquals(cdnUrl, info.getCdnURL());
//			assertEquals("Referrer Test", info.getReferrerACL());
//			assertEquals("User Agent Acl Test", info.getUserAgentACL());
			

		} catch (Exception e) {
			fail(e.getMessage());
		} 
	}
	
	// Test container name limits
	public void testContainerNameLimits()  {
		FilesClient fc = new FilesClient();
		try {
			assertTrue(fc.login());
			StringBuilder nameBuilder = new StringBuilder(createTempContainerName("long"));
			while(nameBuilder.length() <= FilesConstants.CONTAINER_NAME_LENGTH) {
				nameBuilder.append("a");
			}
			try {
				fc.createContainer(nameBuilder.toString());
				// Note, we shouldn't get here, but want to clean up if we do
				fc.deleteContainer(nameBuilder.toString());
				fail("No exception thrown");
			}
			catch (FilesInvalidNameException fine) {
				// Hooray!
			}
		}
		catch (Exception ex) {
			fail(ex.getMessage());
		}
	}
	
	public void testPathCreationAndListing() {
		FilesClient client = new FilesClient();
		try {
			assertTrue(client.login());
			String containerName = createTempContainerName("pathTest");
			
			// Make sure it's not there
			assertFalse(client.containerExists(containerName));
			
			// Add it
			client.createContainer(containerName);
			
			// See that it's there
			assertTrue(client.containerExists(containerName));
			
			// Add a path and two files
			byte randomData[] = makeRandomBytes();
			client.createPath(containerName, "foo");
			client.storeObject(containerName, randomData, "application/octet-stream", "notInContainer.dat", new HashMap<String, String>());
			client.storeObject(containerName, randomData, "application/octet-stream", "foo/inContainer.dat", new HashMap<String, String>());
			
			List<FilesObject> allObjects = client.listObjects(containerName);
			List<FilesObject> pathObjects = client.listObjects(containerName, "foo");
			
			assertEquals(3, allObjects.size());
			assertEquals(1, pathObjects.size());
			assertEquals("foo/inContainer.dat", pathObjects.get(0).getName());
			
			// Delete it
			client.deleteObject(containerName, "notInContainer.dat");
			client.deleteObject(containerName, "foo/inContainer.dat");
			client.deleteObject(containerName, "foo");
			assertTrue(client.deleteContainer(containerName));
			
			// Make sure it's gone
			assertFalse(client.containerExists(containerName));
			
		} catch (Exception e) {
			fail(e.getMessage());
		} 
	}
	
	public void testPathCreation() {
		FilesClient client = new FilesClient();
		try {
			assertTrue(client.login());
			String containerName = createTempContainerName("pathTest");
			
			// Make sure it's not there
			assertFalse(client.containerExists(containerName));
			
			// Add it
			client.createContainer(containerName);
			
			// See that it's there
			assertTrue(client.containerExists(containerName));
			
			// Add a path and two files
			client.createFullPath(containerName, "foo/bar/baz");
			
			List<FilesObject> allObjects = client.listObjects(containerName);
			
			assertEquals(3, allObjects.size());
			
			// If we don't throw an exception, we should be OK
			client.getObject(containerName, "foo");
			client.getObject(containerName, "foo/bar");
			client.getObject(containerName, "foo/bar/baz");
			
			// Delete it
			client.deleteObject(containerName, "foo/bar/baz");
			client.deleteObject(containerName, "foo/bar");
			client.deleteObject(containerName, "foo");
			assertTrue(client.deleteContainer(containerName));
			
			// Make sure it's gone
			assertFalse(client.containerExists(containerName));
			
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		} 
	}
	
	public void testFilesObjectPath() {
		FilesClient client = new FilesClient();
		try {
			assertTrue(client.login());
			String containerName = createTempContainerName("FOpathTest");
			
			// Make sure it's not there
			assertFalse(client.containerExists(containerName));
			
			// Add it
			client.createContainer(containerName);
			
			// See that it's there
			assertTrue(client.containerExists(containerName));
			
			// Add a path and two files
			client.createPath(containerName, "test");
			
			List<FilesObject> allObjects = client.listObjects(containerName);
			
			assertEquals(1, allObjects.size());
			
			FilesObject obj = allObjects.get(0);
			assertEquals(0, obj.getSize());
			assertEquals("application/directory", obj.getMimeType());
			
			// If we don't throw an exception, we should be OK
			client.getObject(containerName, "test");
			
			// Delete it
			client.deleteObject(containerName, "test");
			assertTrue(client.deleteContainer(containerName));
			
			// Make sure it's gone
			assertFalse(client.containerExists(containerName));
			
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		} 
	}
	
	public void testURLs() {
		// Test to make sure these are getting set and are visible to the outside world (needed for Cyberduck's SSL).
		FilesClient client = new FilesClient();
		try {
			assertTrue(client.login());
			assertNotNull(client.getCdnManagementURL());
			assertNotNull(client.getStorageURL());

		} catch (Exception e) {
			fail(e.getMessage());
		} 
	}
	
	// Fun utilities
	private String createTempContainerName(String addition) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss_SSSS");
		return "test-container-" + addition + "-" + sdf.format(new Date(System.currentTimeMillis()));
	}
	
	private String makeFileName(String addition) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss_SSSS");
		return "test-file-" + addition + "-" + sdf.format(new Date(System.currentTimeMillis()));
	}
	
	private byte[] makeRandomFile(String name) throws IOException {

		File file = new File(name);
		FileOutputStream fos = new FileOutputStream(file);
		byte randomData[] = makeRandomBytes();
		fos.write(randomData);
		fos.close();
		
		return randomData;
	}
	
	private byte[] makeRandomBytes() {
		return makeRandomBytes(NUMBER_RANDOM_BYTES);
	}
	private byte[] makeRandomBytes(int nBytes) {
		byte results[] = new byte[nBytes];
		Random gen = new Random();
		gen.nextBytes(results);
		
		// Uncomment to get some not so random data
		// for(int i=0; i < results.length; ++i) results[i] = (byte) (i % Byte.MAX_VALUE);
		
		return results;
	}
	
	private void assertArrayEquals(byte a[], byte b[]) {
		assertEquals(a.length, b.length);
		for(int i=0; i < a.length; ++i) assertEquals(a[i], b[i]);
	}
	
	private class TesterCallback implements IFilesTransferCallback {
		public long bytesSent = 0;
		public int nCalls = 0;
		public void progress(long n) {
			bytesSent = n;
			++nCalls;
		}
	}
}