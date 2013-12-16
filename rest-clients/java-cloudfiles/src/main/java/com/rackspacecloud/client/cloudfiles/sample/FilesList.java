/*
 * See COPYING for license information.
 */ 

package com.rackspacecloud.client.cloudfiles.sample;

import java.io.IOException;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang.StringUtils;

// import org.apache.log4j.Logger;
import org.apache.http.HttpException;

import com.rackspacecloud.client.cloudfiles.*;

import java.util.*;

/*
 * TODO: Add List Metadata for all objects as a switch on the all
 * TODO: Add list Metadata for one object as a switch on a specific object of a container
 * TODO: Add list specific Objects of a container
 */

public class FilesList
{
	// private static Logger logger = Logger.getLogger(List.class);

	public static void main (String args[])
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

			if (line.hasOption("containersOnly"))
			{
				if (line.hasOption("H"))
					printContainers (true);
				else
					printContainers (false);
			}
			else if (line.hasOption("all"))
			{
				if (line.hasOption("H"))
					printContainersAll (true);
				else
					printContainersAll (false);
			}//if (line.hasOption("all"))
			else if (line.hasOption("container"))
			{
				String containerName = line.getOptionValue("container");
				if (StringUtils.isNotBlank(containerName))
				{
					if (line.hasOption("H"))
						printContainer (containerName, true);
					else
						printContainer (containerName, false);
				}
			}//if (line.hasOption("container"))
			else if (line.hasOption("H"))
			{
				System.out.println ("This option needs to be used in conjunction with another option that lists objects or container.");    
			}
		}
		catch( ParseException err )
		{
			System.err.println( "Please see the logs for more details. Error Message: "+err.getMessage() );
			err.printStackTrace(System.err);
		}//catch( ParseException err )
		catch( HttpException err )
		{
			System.err.println( "Please see the logs for more details. Error Message: "+err.getMessage() );
			err.printStackTrace(System.err);
		}//catch( ParseException err )

		catch ( IOException err)
		{
			System.err.println( "Please see the logs for more details. Error Message: "+err.getMessage() );        
		}//catch ( IOException err)
	}

	private static void printContainer (String containerName, boolean humanReadable) throws IOException, HttpException, FilesException
	{
		boolean notFound = true;
		FilesClient client = new FilesClient();
		if ( client.login() )
		{
			List<FilesContainer> containers = client.listContainers();
			System.out.println (client.getAccount() + " containers: ");
			for (FilesContainer value: containers )
			{
				if (value.getName().equalsIgnoreCase(containerName))
				{
					notFound = false;

					FilesContainerInfo info = value.getInfo();
					System.out.println ("\t"+value.getName ()+" - "+info.getObjectCount()+" objects:");

					List<FilesObject> objects = value.getObjects();
					for (FilesObject obj: objects)
					{
						if (humanReadable)
							System.out.println ("\t\t"+StringUtils.rightPad(obj.getName (), 35) + obj.getSizeString());
						else
							System.out.println ("\t\t"+StringUtils.rightPad(obj.getName (), 35) + obj.getSize()+" Bytes");
					}

					if (humanReadable)
					{
						System.out.println ("\tTotal Size: "+info.getTotalSize()/1024+"KB\n");
					}
					else
						System.out.println ("\tTotal Size: "+info.getTotalSize()+"Bytes\n");
				}//if (value.getName().equalsIgnoreCase(containerName))
				else
					notFound = true;
			}//end for

			if (notFound)
				System.out.println ("Container: "+containerName +" was not found !");
		}
	}//end private static void printContainersAndContent () throws IOException, HttpException, FilesAuthorizationException

	private static void printContainersAll (boolean humanReadable) throws IOException, HttpException, FilesException
	{
		FilesClient client = new FilesClient();
		if ( client.login() )
		{
			List<FilesContainer> containers = client.listContainers();
			System.out.println (client.getAccount() + " containers: ");
			for (FilesContainer value: containers )
			{
				FilesContainerInfo info = value.getInfo();                
				System.out.println ("\t"+value.getName ()+" - "+info.getObjectCount()+" objects:");

				List<FilesObject> objects = value.getObjects();
				for (FilesObject obj: objects)
				{
					if (humanReadable)
						System.out.println ("\t\t"+StringUtils.rightPad(obj.getName (), 35) + obj.getSizeString());
					else
						System.out.println ("\t\t"+StringUtils.rightPad(obj.getName (), 35) + obj.getSize()+"Bytes");                
				}

				if (humanReadable)
				{
					System.out.println ("\tTotal Size: "+info.getTotalSize()/1024+"KB\n");
				}
				else
					System.out.println ("\tTotal Size: "+info.getTotalSize()+"Bytes\n");
			}
		}
	}//end private static void printContainersAndContent () throws IOException, HttpException, FilesAuthorizationException

	private static void printContainers (boolean humanReadable) throws IOException, HttpException, FilesException
	{
		FilesClient client = new FilesClient();
		if ( client.login() )
		{
			List<FilesContainer> containers = client.listContainers();
			System.out.println (client.getAccount() + " containers: ");
			for (FilesContainer value: containers )
			{
				FilesContainerInfo info = value.getInfo();
				System.out.println ("\t"+value.getName ()+" - "+info.getObjectCount()+" objects:");            

				if (humanReadable)
				{
					System.out.println ("\tTotal Size: "+ getSizeString (info.getTotalSize())+"\n");
				}
				else
					System.out.println ("\tTotal Size: "+info.getTotalSize()+"Bytes\n");                
			}
		}
	}//end private static void printContainers ()

	private static void printHelp (Options options)
	{
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp( "FilesList", options );
	}//private static void printHelp ()

	@SuppressWarnings("static-access")
	private static Options addCommandLineOptions ()
	{
		Option help = new Option( "help", "print this message" );

		Option containersOnly = OptionBuilder.hasArg (false)
		.withDescription ("Show ONLY Containers in account.  No Object information is provided.")
		.create ("containersOnly");

		Option containers = OptionBuilder.hasArg (false)
		.withDescription ("Show Containers and their objects in this account")
		.create ("all");

		Option container = OptionBuilder.withArgName("container")
		.hasArg (true)
		.withDescription ("Show Containers and their objects in this account")
		.create ("container");

		Option kb = new Option ("H","humanReadable", false, "Show size of objects in human readable form.  On its own this has no meaning and needs to be used in conjuction to a command that provides size of objects and containers");

		Options options = new Options();

		options.addOption(containersOnly);
		options.addOption(containers);
		options.addOption(container);
		options.addOption(kb);    
		options.addOption(help);

		return options;
	}

	public static String getSizeString (long size)
	{
		long kb = 1024;
		long mb = 1024*1024;
		long gb = 1024*1024*1024;

		//KB
		if (size > gb)
			return (size/gb) + "GB";
		else if (size > mb)
			return (size/mb)+"MB";
		else if (size > kb)
			return (size/kb) +"KB";
		else
			return size+"Bytes";
	}
}
