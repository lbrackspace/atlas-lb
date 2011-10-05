Running migrations:
     For the old Rackspace atlas project.(All paths in this project will 
     be relative to your main workspace directory.
     For example in my case if the path documented starting at atlas-lb

    1. During development write your migration schema with a friendly name 
       such as /atlas-lb/migration/schema/loadbalancing/somefeature_up.sql 
       and ./atlas-lb/migration/schema/loadbalancing/somefeature_down.sql
       This is because at development time we don't know what the meta_value 
       would be during development time.

    2. When its time to prepare the a release that will include this 
       migration. Add the correct SQL to update the meta_value and rename the
       file to its proper number to number migration. For example if the last
       migration used was 49-50.sql and 50-49.sql you would rename the 
       somefeature_up.sql file to 50-51.sql and rename the 
       somefeature_down.sql to 51-50.sql. Don't forget about adding the 
       meta_values at the bottom of the SQL files. Also to keep the other 
       database migrations in sync you will also need create a 50-51.sql 
       and 51-50.sql file in the
       ./atlas-lb/migration/schema/loadbalancing_usage directory as well 
       since all the dbs must be on the same migration number.

    3. Change into your ./atlas-lb/migration directory

    4. Execute the file  ./prep_migration
       This will modify the the txt files in the schema directory which the 
       jdeb script will use to build the migration package.

    5. Push the changes to the txt files so the jdep plugin will actually have
       access to the files when it runs on jenkins. (Doing this locally
       won't work as Maven releases are done on jenkins)


