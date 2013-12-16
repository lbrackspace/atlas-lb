/*
 * See COPYING for license information.
 */ 

package com.rackspacecloud.client.cloudfiles.sample;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.rackspacecloud.client.cloudfiles.FilesAccountInfo;
import com.rackspacecloud.client.cloudfiles.FilesClient;
import com.rackspacecloud.client.cloudfiles.FilesConstants;
import com.rackspacecloud.client.cloudfiles.FilesContainer;
import com.rackspacecloud.client.cloudfiles.FilesContainerExistsException;
import com.rackspacecloud.client.cloudfiles.FilesContainerInfo;
import com.rackspacecloud.client.cloudfiles.FilesContainerNotEmptyException;
import com.rackspacecloud.client.cloudfiles.FilesInvalidNameException;
import com.rackspacecloud.client.cloudfiles.FilesNotFoundException;
import com.rackspacecloud.client.cloudfiles.FilesObject;
import com.rackspacecloud.client.cloudfiles.FilesObjectMetaData;

/**
 * @author lvaughn
 *
 */
public class FilesCli {
	FilesClient client = null;
	BufferedReader console = new BufferedReader(new InputStreamReader(System.in));
	
	private boolean doLogin() {
		
		try {
			System.out.print("Username: ");
			String username = console.readLine().trim();
			
			System.out.print("Password: ");
			String password = console.readLine().trim();
			
			final boolean result = doLogin(username, password);			
			
			return result;
		} catch (Exception e) {
			System.out.println("Error logging in!");
			e.printStackTrace();
			return false;
		}
		

	}
	
	private boolean doLogin(final String username, final String password) throws Exception {		
		client = new FilesClient(username, password);
		return client.login();		
	}
	
	private static final String HELP_STRING = 
		"Commands:\n" +
		"   get                               List the containers for this account\n" +
		"   get container                     List the contents of the given container\n" +
		"   get container/object destination  Download the given object and store it at the destination\n" +
		"   head                              Get information about this account\n" +
		"   head container                    Get the container's information\n" +
		"   head container/object             Get the objects's information and metadata\n" +
		"   put container                     Create the given container\n" +
		"   put container localfile           Upload the local file to the container\n" +
		"   delete container                  Delete the container\n" +
		"   delete container/object           Delete the given object\n" +
		"   regions                           List the available regions\n" +
		"   setregion region                  Set the current region\n" +
		"   help                              Print this help message\n" +
     	"   exit                              Exit the program\n";
	
	private boolean evaluateCommand(String cmd) {
		cmd = cmd.trim();
		
		String components[] = cmd.split("\\s+");
		if (cmd.length() == 0 || "help".equals(components[0].toLowerCase())) {
			System.out.println(HELP_STRING);
			return true;
		}
		return evaluateCommand(components);
	}
	
	private boolean evaluateCommand(String[] components) {
		
		String command = components[0].toLowerCase();
		
		if ("help".equals(command)) {
			System.out.println(HELP_STRING);
		}
		// Exit
		if("exit".equals(command) || "quit".equals(command)) {
			System.out.println("Exiting");
			return false; 
		}
		
		//-- "get" - lists containers
		//-- "get <container name>" - list contents of specified container
		//	--"get <object name>" - downloads object 
		if("get".equals(command)) {
			if(components.length == 1) {
				try {
					// List containers
					List<FilesContainer> containers = client.listContainers();
					int nContainers = containers.size();
					System.out.println("The account has " + nContainers + ((nContainers == 1) ? " container" : " containers")
							+ " in region " + client.getCurrentRegion());
					for(FilesContainer container : containers) {
						System.out.println("   " + container.getName());
					}
					return true;
				}
				catch (Exception ex) {
					System.out.println("Problem listing containers");
					ex.printStackTrace();
					return true;
				}
			}
			else {
				String name = components[1];
				int slashLocation = name.indexOf('/');
				if (slashLocation == -1) {
					// It's a container
					try {
						List<FilesObject> objects = client.listObjects(name);
						if (objects.size() == 0) { 
							System.out.println("Container " + name + " was empty");
							return true;
						}
						
						System.out.println("Contents of " + name + ":");
						for(FilesObject obj : objects) { 
							System.out.println("  " + obj.getName() + " " + obj.getSizeString());
						}
						System.out.println();
					} catch (Exception e) {
						System.out.println("Error trying to list container contents");
						e.printStackTrace();
						return true;
					} 
				}
				else {
					// It's an object
					if (components.length != 3) { 
						System.out.println("usage:  get container/filename.ext destination.ext");
						return true;
					}
					
					String container = name.substring(0, slashLocation);
					String object = name.substring(slashLocation + 1);
					String destination = components[2];
					
					try {
						InputStream is = client.getObjectAsStream(container, object);
						FileOutputStream fos = new FileOutputStream(destination);
						byte buffer[] = new byte[4096];
						int read = -1;
						while ((read = is.read(buffer)) > 0) {
							fos.write(buffer, 0, read);
						}
						fos.close();
						is.close();
						System.out.println(name + " downlaoded to " + destination);
					}
					catch (Exception ex) {
						System.out.println("Problem getting " + name);
						ex.printStackTrace();
						return true;
					}
				}
			}
			return true;
		}

		// -- "head <container name>" - show container info  
		// -- "head <object name>" - shown object info, incl meta data 
		if ("head".equals(command)) {
			if (components.length == 1) {
				try {
					FilesAccountInfo info = client.getAccountInfo();
					System.out.println("Account information:");
					System.out.println("  Number of Containers: " + info.getContainerCount());
					System.out.println("    Total Account Size: " + info.getBytesUsed());
					System.out.println();
				}
				catch (Exception e) {
					System.err.println("Error getting container info");
					e.printStackTrace();
					return true;
				} 	
			}
			DecimalFormat format = new DecimalFormat();
			for(int i=1; i < components.length; i++) {
				String name = components[i];
				int slashLocation = name.indexOf('/');
				if (slashLocation == -1) { 
					// assume it's a container
					try {
						FilesContainerInfo containerInfo = client.getContainerInfo(name);
						System.out.println("Information for " + name);
						System.out.println("  Object Count: " + containerInfo.getObjectCount());
						System.out.println("    Total Size: " + format.format(containerInfo.getTotalSize()) + " bytes");
						System.out.println();
					} catch (Exception e) {
						System.err.println("Error getting container info");
						e.printStackTrace();
						return true;
					} 
				}
				else {
					String container = name.substring(0, slashLocation);
					String object = name.substring(slashLocation + 1);
					try {
						FilesObjectMetaData metadata = client.getObjectMetaData(container, object);
						if (metadata == null) { 
							System.out.println("Could not get metadata for " + name);
						}
						else {
							System.out.println("LGV: " + container + ":" + object + ":" + metadata);
							System.out.println("Information for " + name);
							System.out.println("  Total Size: " + metadata.getContentLength() + " bytes");
							System.out.println("  MIME type: " + metadata.getMimeType());
							Map<String, String> md = metadata.getMetaData();
							if (md.size() == 0) { 
								System.out.println("  Contains no metadata");
							}
							else {
								System.out.println("  Metadata:");
								for(String key : md.keySet()) { 
									System.out.println("    " + key + " => " + md.get(key));
								}
							}
							System.out.println();		
						}
					}
					catch (Exception e) { 
						System.err.println("Error getting object info");
						e.printStackTrace();
						return true;
					}
				}
			}
			return true;
		}

		// --"put <container name>" - create new container 
		// --"put <local file>" - upload object 
		if("put".equals(command)) {
			if (components.length == 2) {
				String newContainerName = components[1];
				if(newContainerName.indexOf('/') != -1) {
					System.out.println("Container names may not contain slashes");
					return true;
				}
				try {
					client.createContainer(newContainerName);
				}
				catch (FilesContainerExistsException fcee) {
					System.out.println(newContainerName + " already existed");
				} catch (Exception e) {
					System.out.println("Error creating container");
					e.printStackTrace();
					return true;
				} 
				return true;
			}
			else if(components.length == 3) { 
				String containerName = components[1];	
				String filename = components[2];
				
				File file = new File(filename);
				if (!file.exists()) { 
					System.out.println("Could not find file " + file.getAbsolutePath());
					return true;
				}
				String name = file.getName();
				String extention = "";
				int dotLocation = name.lastIndexOf('.');
				if (dotLocation > 0) {
					extention = name.substring(dotLocation + 1);
				}
				String mimeType = FilesConstants.getMimetype(extention);
				
				try {
					if (!client.containerExists(containerName)) {
						System.out.println("Container " + containerName + " does not exist");
						return true;
					}
					if (client.storeObject(containerName, file, mimeType) != null) { 
						System.out.println("Object " + file.getName() + " was created");
					}
					return true;
				}
				catch (Exception e) { 
					System.out.println("Problem uploading file");
					e.printStackTrace();
					return true;
				}
			}
			else {
				System.out.println("Usage:\n  put container\n  put container file");
			}
			return true;
		}

		//--"delete <object name>" - delete the specified object  
		// --"delete <container name>" - delete the specified container 
		if("delete".equals(command)) {
			for(int i=1; i < components.length; i++) {
				String name = components[i];
				int slashLocation = name.indexOf('/');
				if (slashLocation == -1) { 
					// assume it's a container
					boolean returnCode = false;
					try {
						returnCode = client.deleteContainer(name);
					} catch (FilesInvalidNameException fine) {
						System.out.println(name + " is not a valid container name");
					} catch (FilesNotFoundException fine) {
						System.out.println(name + " could not be found");
					} catch (FilesContainerNotEmptyException fine) {
						System.out.println(name + " was not empty.  Please delete the contents and try again");
					}
					catch (Exception e) {
						System.out.println("Error deleting container");
						e.printStackTrace();
						return true;
					} 
					if (returnCode) { 
						System.out.println ("Container \"" + name + "\" deleted");
					}
					else {
						System.out.println("Unexpected result deleting container ");
					}
				}
				else {
					// object
					String container = name.substring(0, slashLocation);
					String object = name.substring(slashLocation + 1);
					try {
						client.deleteObject(container, object);
						System.out.println ("Object \"" + name + "\" deleted");
					} 
					catch (FilesNotFoundException fnfe) {
						System.out.println(name + " could not be found");
					}
					catch (Exception e) {
						System.out.println("Error deleting object");
						e.printStackTrace();
						return true;
					} 
				}
			}
			return true;	
		}
		
		// -- "regions" - show available regions  
		if ("regions".equals(command)) {
			if (components.length == 1) {
				try {
					String[] regions = client.getRegions();
					if (regions.length > 0) {
						System.out.println("Available regions:");
						for (int i = 0; i < regions.length; i++) {
							System.out.println("  " + regions[i]);
						}
					} else {
						System.out.println("No regions found");
						System.out.println();
					}
				}
				catch (Exception e) {
					System.err.println("Error getting container info");
					e.printStackTrace();
					return true;
				} 	
			} else {
				System.out.println("Usage:\n  regions");
			}
			return true;
		}
		
		// -- "setregion <region>" - set the current region
		if ("setregion".equals(command)) {
			if (components.length == 2) {
				try {
					client.setCurrentRegion(components[1]);
					System.out.println("Current region set to " + client.getCurrentRegion());
					System.out.println();
				}
				catch (Exception e) {
					System.err.println("Error setting region");
					return true;
				} 	
			} else {
				System.out.println("Usage:\n  setregion region");
			}
			return true;
		}

		// We should never get here
		System.out.println("Unrecognized command " + command);
		System.out.println(HELP_STRING);
		return true;
	}
	

	public static class CommandLineOptions {
		public final String userName;
		public final String password;
		public final String[] command;
		
		public CommandLineOptions(String[] args) {
			String userName = null;
			String password = null;
			List<String> command = new ArrayList<String>();
			userName = System.getenv("CLOUDFILES_USERNAME");
			password = System.getenv("CLOUDFILES_PASSWORD");
			for (int i = 0; i < args.length; i ++) {		
				if ("username".equals(args[i])) {
					if (i >= args.length - 1) {
						throw new RuntimeException("No argument following option 'username'.");
					}
					userName = args[i + 1];
					i ++;
				} else if ("password".equals(args[i])) {
					if (i >= args.length - 1) {
						throw new RuntimeException("No argument following option 'password'.");
					}
					password = args[i + 1];
					i ++;
				} else {
					command.add(args[i]);
				}
			}
			if (userName == null) {
				throw new RuntimeException("No username specified (use option 'username' or set CLOUDFILES_USERNAME environment variable).");
			}
			if (password == null) {
				throw new RuntimeException("No password specified (use option 'password' or set CLOUDFILES_PASSWORD environment variable).");
			}
			this.userName = userName;
			this.password = password;
			this.command = new String[command.size()];
			command.toArray(this.command);
		}
		
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {

		if (args.length < 1) {
			interactiveMode();			
		} else {
			parseArgs(args);
		}
	}
	
	public static void parseArgs(String[] args) {
		try {
			final CommandLineOptions options = new CommandLineOptions(args);
			final FilesCli cli = new FilesCli();
			if (!cli.doLogin(options.userName, options.password)) {
				throw new RuntimeException("Failed to login.");
			}
			if (options.command.length == 0) {
				System.out.println("Login was successful, but no other commands were specified.");
				System.out.println(HELP_STRING);				
			} else {
				cli.evaluateCommand(options.command);
			}
		} catch(Exception e) {
			System.err.println("Error:" + e.getMessage());
			e.printStackTrace();
		}
	}
	
	public static void interactiveMode() {
		FilesCli commandLine = new FilesCli();
		
		if (commandLine.doLogin()) {
			System.out.println("Type 'help' for assistance");
			String cmd = "";
			do {
				String account = commandLine.client.getAccount();
				account = (account == null) ? commandLine.client.getUserName() : account;
				System.out.print(account + ": ");
				try {
					cmd = commandLine.console.readLine();
				}
				catch (IOException e) { 
					cmd = "";
				}
				
			} while(commandLine.evaluateCommand(cmd)); 
			
			System.exit(0);
		}
		else {
			System.err.println("Login failed");
			System.exit(-1);
		}
	}
}
