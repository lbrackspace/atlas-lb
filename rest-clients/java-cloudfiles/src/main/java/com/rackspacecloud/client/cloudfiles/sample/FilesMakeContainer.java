/*
 * See COPYING for license information.
 */ 

package com.rackspacecloud.client.cloudfiles.sample;

//import org.apache.log4j.Logger;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.cli.*;
import org.apache.http.HttpException;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import com.rackspacecloud.client.cloudfiles.*;

public class FilesMakeContainer
{
	//private static final Logger logger = Logger.getLogger(FilesMakeContainer.class);

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

			String containerName = null;
			if (line.hasOption("container"))
			{
				containerName = line.getOptionValue("container");
				createContaier (containerName);    
			}//end if (line.hasOption("container"))
			else if (args.length > 0)
			{
				//If we got this far there are command line arguments but none of what we expected treat the first one as the Container name
				containerName = args[0];
				createContaier (containerName);
			}
			else
			{
				System.err.println ("You must provide the -container with a valid value for this to work !");
				System.exit (-1);
			}

		}//end try
		catch( ParseException err )
		{
			System.err.println( "Please see the logs for more details. Error Message: "+err.getMessage() );
			err.printStackTrace(System.err);
		}//catch( ParseException err )

		catch ( Exception err)
		{
			System.err.println( "Please see the logs for more details. Error Message: "+err.getMessage() );
		}//catch ( IOException err)

	}//end main

	private static void createContaier (String containerName) throws HttpException, IOException, FilesException
	{
		if (!StringUtils.isNotBlank(containerName) || containerName.indexOf('/') != -1)
		{
			System.err.println ("You must provide a valid value for the  Container name !");
			System.exit (-1);
		}//if (!StringUtils.isNotBlank(containerName))

		//Check to see if a Container with this name already exists

		FilesClient client = new FilesClient();
		if ( client.login() )
		{
			client.createContainer(containerName);
		}
		else
			System.out.println ("Failed to login to Cloud Files!");

		System.exit(0);

	}

	private static void printHelp (Options options)
	{
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp( "MkDir [-container] ContainerName", options );
	}//private static void printHelp ()

	@SuppressWarnings("static-access")
	private static Options addCommandLineOptions ()
	{
		Option help = new Option( "help", "print this message" );

		Option container = OptionBuilder.withArgName("container")
		.hasArg (true)
		.withDescription ("Name of container to create.")
		.create ("container");

		Options options = new Options();

		options.addOption(help);
		options.addOption(container);

		return options;
	}

}
