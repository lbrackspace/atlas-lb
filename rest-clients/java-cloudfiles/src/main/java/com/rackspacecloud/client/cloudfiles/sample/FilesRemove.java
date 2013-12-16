/*
 * See COPYING for license information.
 */ 

package com.rackspacecloud.client.cloudfiles.sample;

import org.apache.log4j.Logger;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.cli.*;
import org.apache.http.HttpException;

import java.io.*;
import java.security.NoSuchAlgorithmException;
import java.util.*;

import com.rackspacecloud.client.cloudfiles.*;

public class FilesRemove
{
	private static final Logger logger = Logger.getLogger(FilesRemove.class);

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

			if (line.hasOption("help")) {
				printHelp (options);
				System.exit(0);
			}

			if (line.hasOption("container"))
			{
				String containerName = null;
				containerName = line.getOptionValue("container");
				removeContainer (containerName, line.hasOption('r'));    
			}//if (line.hasOption("container"))

			if (line.hasOption("object"))
			{
				String ObjectNameWithPath = null;
				ObjectNameWithPath = line.getOptionValue("object");
				removeObject (ObjectNameWithPath);
			}//if (line.hasOption("container"))


		}//end try
		catch( ParseException err )
		{
			logger.fatal("Parsing exception on the command line: "+ err);
			System.err.println( "Please see the logs for more details. Error Message: "+err.getMessage() );
			err.printStackTrace(System.err);
		}//catch( ParseException err )
		catch ( Exception err)
		{
			logger.fatal("Exception : "+ err);
			System.err.println( "Please see the logs for more details. Error Message: "+err.getMessage() );
		}//catch ( IOException err)
	}//end main

	private static void removeObject (String objectNameWithPath) throws HttpException, IOException, FilesException
	{
		if (!StringUtils.isNotBlank(objectNameWithPath))
		{
			System.err.println ("You must provide a valid value for the  Object name and path !");
			System.exit (0);
		}//if (!StringUtils.isNotBlank(ObjectNameWithPath))

		int firstSlashLocation = objectNameWithPath.indexOf('/');
		if (firstSlashLocation >  -1)
		{
			String container = objectNameWithPath.substring(0, firstSlashLocation - 1);
			String object = objectNameWithPath.substring(firstSlashLocation + 1);
			FilesClient client = new FilesClient();
			if ( client.login() ) {
				client.deleteObject(container, object);
			}
			else {
				System.err.println("Failed to log in to Cloud FS");
				System.exit(-1);
			}
		}
		else
		{
			System.err.println("Please specify the object path in the form containerName/objectName");
			System.exit(-1);
		}
	}

	private static void removeContainer (String containerName, boolean recurse) throws HttpException, IOException, FilesAuthorizationException, FilesException
	{
		if (!StringUtils.isNotBlank(containerName))
		{
			System.out.println ("You must provide a valid value for the  Container name !");
			logger.fatal("You must provide a valid value for the  Container name !");
			System.exit (0);
		}//if (!StringUtils.isNotBlank(containerName))

		//Check to see if a Container with this name already exists

		FilesClient client = new FilesClient();
		if ( client.login() )
		{
			if(recurse) {
				List<FilesObject> objects = client.listObjects(containerName);
				for (FilesObject obj : objects) {
					client.deleteObject(containerName, obj.getName());
				}
			}
			
			try {
				if (client.deleteContainer(containerName)) {
					System.out.println(containerName+" deleted");
					System.exit (0);					
				}
				else{
					System.out.println(containerName+" was not deleted");
					System.exit (-1);					
				}
			}
			catch (FilesNotFoundException fnfe) {
				System.out.println(containerName+" not found !");
				System.exit (0);				
			}
			catch (FilesContainerNotEmptyException fcnee) {
				System.out.println(containerName+" is not empty use -r !");
				System.exit (0);
			}
		}
		else
			System.out.println ("Failed to login to  !");

		System.exit(0);

	}

	private static void printHelp (Options options)
	{
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp( "Remove [-container] ContainerName [-rf]", options );
	}//private static void printHelp ()


	@SuppressWarnings("static-access")
	private static Options addCommandLineOptions ()
	{
		Option help = new Option( "help", "print this message" );
		Option recurse = new Option( "r", "Recursively go through the folders and files" );    

		Option container = OptionBuilder.withArgName("container")
		.hasArg (true)
		.withDescription ("Name of  container to remove.")
		.create ("container");

		Option object = OptionBuilder.withArgName("object")
		.hasArg (true)
		.withDescription ("Name and path of  object to remove.")
		.create ("object");
		
		Options options = new Options();

		options.addOption(help);
		options.addOption(recurse);

		options.addOption(container);
		options.addOption(object);

		return options;
	}

}
