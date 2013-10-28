/*
 * See COPYING for license information.
 */ 

package com.rackspacecloud.client.cloudfiles.sample;

import org.apache.commons.cli.*;
import org.apache.commons.lang.SystemUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.http.HttpException;

import org.apache.log4j.Logger;

import com.rackspacecloud.client.cloudfiles.FilesAuthorizationException;
import com.rackspacecloud.client.cloudfiles.FilesClient;
import com.rackspacecloud.client.cloudfiles.FilesConstants;
import com.rackspacecloud.client.cloudfiles.FilesException;
import com.rackspacecloud.client.cloudfiles.FilesObject;

import java.io.*;
import java.util.zip.*;
import java.util.*;
import java.security.NoSuchAlgorithmException;

public class FilesCopy
{
	private static final Logger logger = Logger.getLogger(FilesCopy.class);

	private static final String ZIPEXTENSION = ".zip";

	private static File SYSTEM_TMP = SystemUtils.getJavaIoTmpDir();

	public static void main (String args[]) throws NoSuchAlgorithmException, FilesException
	{
		//Build the command line options
		Options options = addCommandLineOptions ();

		if (args.length <= 0)
			printHelp (options);

		CommandLineParser parser = new GnuParser();
		try
		{
			// parse the command line arguments
			CommandLine line = parser.parse( options, args );

			if (line.hasOption("help"))
				printHelp (options);

			if (line.hasOption("file") && line.hasOption("folder"))
			{
				System.err.println ("Can not use both -file and -folder on the command line at the same time.");
				System.exit(-1);
			}//if (line.hasOption("file") && line.hasOption("folder"))

			if (line.hasOption("download"))
			{
				if (line.hasOption("folder") )
				{
					String localFolder = FilenameUtils.normalize( line.getOptionValue("folder") );
					String containerName = null;    
					if (StringUtils.isNotBlank(localFolder))
					{
						File localFolderObj = new File (localFolder);
						if (localFolderObj.exists() && localFolderObj.isDirectory())
						{
							if (line.hasOption("container"))
							{
								containerName = line.getOptionValue("container");
								if (!StringUtils.isNotBlank(containerName))
								{
									System.err.println ("You must provide a valid value for the  Container to upload to !");
									System.exit (-1);
								}//if (!StringUtils.isNotBlank(ontainerName))                            
							}
							else
							{
								System.err.println ("You must provide the -container for a copy operation to work as expected.");
								System.exit (-1);
							}

							System.out.println ("Downloading all objects from: "+containerName+ " to local folder: "+ localFolder);

							getContainerObjects (localFolderObj, containerName);

						}
						else
						{
							if ( !localFolderObj.exists() )
							{
								System.err.println ("The local folder: "+ localFolder + " does not exist.  Create it first and then run this command.");
							}

							if ( !localFolderObj.isDirectory() )
							{
								System.err.println ("The local folder name supplied : "+ localFolder + " is not a folder !");
							}

							System.exit (-1);
						}
					}
				}
				System.exit (0);
			}//if (line.hasOption("download"))


			if (line.hasOption("folder"))
			{
				String containerName = null;
				String folderPath = null;

				if (line.hasOption("container"))
				{
					containerName = line.getOptionValue("container");
					if (!StringUtils.isNotBlank(containerName))
					{
						System.err.println ("You must provide a valid value for the  Container to upload to !");
						System.exit (-1);
					}//if (!StringUtils.isNotBlank(containerName))

				}
				else
				{
					System.err.println ("You must provide the -container for a copy operation to work as expected.");
					System.exit (-1);
				}

				folderPath = line.getOptionValue("folder");
				if (StringUtils.isNotBlank(folderPath))
				{
					File folder = new File ( FilenameUtils.normalize (folderPath) );
					if (folder.isDirectory())
					{
						if (line.hasOption("z"))
						{
							System.out.println ("Zipping: "+folderPath);
							System.out.println ("Nested folders are ignored !");

							File zipedFolder = zipFolder(folder);
							String mimeType = FilesConstants.getMimetype(ZIPEXTENSION);    
							copyToCreateContainerIfNeeded (zipedFolder, mimeType, containerName);
						}
						else
						{
							File [] files = folder.listFiles();
							for (File f: files)
							{
								String mimeType = FilesConstants.getMimetype(FilenameUtils.getExtension(f.getName()));
								System.out.println ("Uploading :"+f.getName()+" to "+folder.getName());
								copyToCreateContainerIfNeeded (f, mimeType, containerName);
								System.out.println ("Upload :"+f.getName()+" to "+folder.getName()+" completed.");
							}
						}
					}
					else
					{
						System.err.println ("You must provide a valid folder value for the -folder option !");
						System.err.println ("The value provided is: "+FilenameUtils.normalize (folderPath));
						System.exit (-1);
					}

				}
			}//if (line.hasOption("folder"))

			if (line.hasOption("file"))
			{
				String containerName = null;
				String fileNamePath = null;

				if (line.hasOption("container"))
				{
					containerName = line.getOptionValue("container");
					if (!StringUtils.isNotBlank(containerName) || containerName.indexOf('/') != -1)
					{
						System.err.println ("You must provide a valid value for the  Container to upload to !");
						System.exit (-1);
					}//if (!StringUtils.isNotBlank(containerName))
				}
				else
				{
					System.err.println ("You must provide the -container for a copy operation to work as expected.");
					System.exit (-1);
				}

				fileNamePath = line.getOptionValue("file");
				if (StringUtils.isNotBlank(fileNamePath))
				{
					String fileName = FilenameUtils.normalize (fileNamePath);
					String fileExt = FilenameUtils.getExtension(fileNamePath);
					String mimeType = FilesConstants.getMimetype(fileExt);
					File file = new File (fileName);    

					if (line.hasOption("z"))
					{
						logger.info("Zipping "+fileName);
						if (!file.isDirectory())
						{
							File zippedFile = zipFile(file);
							mimeType = FilesConstants.getMimetype(ZIPEXTENSION);
							copyTo (zippedFile, mimeType, containerName);
							zippedFile.delete();    
						}

					}//if (line.hasOption("z"))
					else
					{

						logger.info("Uploading "+fileName+ ".");
						if (!file.isDirectory())
							copyTo (file, mimeType, containerName);                        
						else
						{
							System.err.println ("The path you provided is a folder.  For uploading folders use the -folder option.");
							System.exit (-1);
						}
					}
				}//if (StringUtils.isNotBlank(file))
				else
				{
					System.err.println ("You must provide a valid value for the file to upload !");
					System.exit (-1);
				}
			}//if (line.hasOption("file"))
		}//end try        
		catch( ParseException err )
		{
			System.err.println( "Please see the logs for more details. Error Message: "+err.getMessage() );
			err.printStackTrace(System.err);
		}//catch( ParseException err )
		catch (FilesAuthorizationException err)
		{
			logger.fatal("FilesAuthorizationException : Failed to login to your  account !"+ err);
			System.err.println( "Please see the logs for more details. Error Message: "+err.getMessage() );
		}//catch (FilesAuthorizationException err)

		catch ( Exception err)
		{
			logger.fatal("IOException : "+ err);
			System.err.println( "Please see the logs for more details. Error Message: "+err.getMessage() );
		}//catch ( IOException err)
	}

	public static void getContainerObjects (File localFolder, String containerName) throws IOException, HttpException, FilesAuthorizationException, NoSuchAlgorithmException, FilesException
	{
		FilesClient client = new FilesClient();
		if ( client.login() )
		{
			if (client.containerExists(containerName))
			{
				List<FilesObject> objects = client.listObjects(containerName);
				for (FilesObject obj: objects)
				{
					System.out.println ("\t"+StringUtils.rightPad(obj.getName (), 35)+ obj.getSizeString());
					File localFile = new File ( FilenameUtils.concat(localFolder.getAbsolutePath(), obj.getName()) );
					obj.writeObjectToFile (localFile);
				}//for (Object obj: objects)
			}
			else
			{
				logger.fatal("The  container: "+containerName+" does not exist.");
				System.out.println ("The  container: "+containerName+" does not exist!");
				System.exit (0);
			}
		}//if ( client.login() )
	}

	/**
	 *
	 * @param file
	 * @param mimeType
	 * @param containerName
	 * @throws IOException
	 * @throws HttpException
	 * @throws NoSuchAlgorithmException
	 * @throws FilesException 
	 */
	private static void copyToCreateContainerIfNeeded (File file, String mimeType,String containerName) throws IOException, HttpException, NoSuchAlgorithmException, FilesException
	{
		FilesClient client = new FilesClient();
		if ( client.login() )
		{
			if (client.containerExists(containerName))
			{
				logger.info ("Copying files to "+containerName);
				copyTo (file, mimeType, containerName);
			}
			else
			{
				logger.warn("The  container: "+containerName+" does not exist.  Creating it first before placing objects into it.");
				System.out.println ("The  container: "+containerName+" does not exist.  Creating it first before placing objects into it.");
				client.createContainer(containerName);
				copyTo (file, mimeType, containerName);    
			}

		}
	}

	/**
	 *
	 * @param file
	 * @param mimeType
	 * @param containerName
	 * @throws IOException
	 * @throws HttpException
	 * @throws NoSuchAlgorithmException
	 * @throws FilesException 
	 */
	private static void copyTo (File file, String mimeType,String containerName) throws IOException, HttpException, NoSuchAlgorithmException, FilesException
	{
		FilesClient client = new FilesClient();
		if ( client.login() )
		{
			if (client.containerExists(containerName))
				client.storeObject(containerName, file, mimeType);                
			else
			{
				logger.info("The  container: "+containerName+" does not exist.  Create it first before placing objects into it.");
				System.out.println ("The  container: "+containerName+" does not exist.  Create it first before placing objects into it.");
				System.exit (0);
			}

		}//if ( client.login() )
	}

	/**
	 *
	 * @param folder
	 * @return null if the input is not a folder otherwise a zip file containing all the files in the folder with nested folders skipped.
	 * @throws IOException
	 */
	public static File zipFolder (File folder) throws IOException
	{
		byte[] buf = new byte[1024];
		int len;

		// Create the ZIP file
		String filenameWithZipExt = folder.getName()+ZIPEXTENSION;
		File zippedFile = new File (FilenameUtils.concat( SYSTEM_TMP.getAbsolutePath(), filenameWithZipExt ));

		ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zippedFile));

		if (folder.isDirectory())
		{
			File [] files = folder.listFiles();

			for (File f: files)
			{
				if (!f.isDirectory())
				{
					FileInputStream in = new FileInputStream(f);

					// Add ZIP entry to output stream.
					out.putNextEntry(new ZipEntry(f.getName()));

					// Transfer bytes from the file to the ZIP file
					while ((len = in.read(buf)) > 0)
					{
						out.write(buf, 0, len);
					}

					// Complete the entry
					out.closeEntry();
					in.close();
				}
				else
					logger.warn ("Skipping nested folder: "+f.getAbsoluteFile());
			}

			out.flush();
			out.close();
		}
		else
		{
			logger.warn ("The folder name supplied is not a folder!");
			System.err.println("The folder name supplied is not a folder!");
			return null;
		}

		return zippedFile;
	}

	public static File zipFile (File f) throws IOException
	{
		byte[] buf = new byte[1024];
		int len;

		// Create the ZIP file
		String filenameWithZipExt = f.getName()+ZIPEXTENSION;
		File zippedFile = new File (FilenameUtils.concat( SYSTEM_TMP.getAbsolutePath(), filenameWithZipExt ));

		ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zippedFile));
		FileInputStream in = new FileInputStream(f);

		// Add ZIP entry to output stream.
		out.putNextEntry(new ZipEntry(f.getName()));

		// Transfer bytes from the file to the ZIP file
		while ((len = in.read(buf)) > 0)
		{
			out.write(buf, 0, len);
		}

		// Complete the entry
		out.closeEntry();
		out.flush();

		in.close();
		out.close();

		return zippedFile;
	}

	private static void printHelp (Options options)
	{
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp( "List -file filename -container ContainerName [-z]", options );
	}//private static void printHelp ()    

	@SuppressWarnings("static-access")
	private static Options addCommandLineOptions ()
	{
		Option help = new Option( "help", "print this message" );

		Option folder = OptionBuilder.withArgName("folder")
		.hasArg (true)
		.withDescription ("Name of folder to upload.  Only top level objects that are not folders will be uploaded.")
		.create ("folder");

		Option file = OptionBuilder.withArgName("file")
		.hasArg (true)
		.withDescription ("Name of file to upload to .")
		.create ("file");

		Option container = OptionBuilder.withArgName("container")
		.hasArg (true)
		.withDescription ("Name of container to place objects into.")
		.create ("container");

		Option zip = new Option ("z","zip", false, "Compress the object being placed into . This option can be used with other options e.g. -tar");
//		Option tar = new Option ("t","tar", false, "Create a tar of the folder. Using this option without a -folder has no effect !");
		Option download = new Option ("d","download", false, "Copy files from  to the local system.  Must be used in conjunction to -folder -file -container");        

		Options options = new Options();

		options.addOption(folder);
		options.addOption(file);
		options.addOption(zip);
		options.addOption(help);
		options.addOption(container);
		//options.addOption(tar);
		options.addOption(download);    

		return options;
	}
}
