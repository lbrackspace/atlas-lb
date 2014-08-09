package org.openstack.atlas.service.domain.entities;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.KeyPair;
import java.util.ArrayList;
import java.util.List;
import org.json.simple.parser.ParseException;
import org.openstack.atlas.service.domain.util.conf.SslTerminationConfig;
import org.openstack.atlas.util.b64aes.Aes;
import org.openstack.atlas.util.ca.zeus.ErrorEntry;
import org.openstack.atlas.util.debug.Debug;
import org.openstack.atlas.util.itest.hibernate.HibernateDbConf;
import org.openstack.atlas.util.itest.hibernate.HuApp;
import org.openstack.atlas.util.staticutils.StaticFileUtils;
import org.openstack.atlas.util.staticutils.StaticStringUtils;
import org.openstack.atlas.util.ca.zeus.ZeusUtils;

public class SslKeyEncryptCli {

    private static final String SSL_QUERY;
    private static List<SslTermination> sslRows;
    private static HuApp hu;
    private static HibernateDbConf huConf;
    private static BufferedReader stdin;
    private static final String prompt;
    private static final int BUFFSIZE;

    static {
        SSL_QUERY = "SELECT s FROM SslTermination s";
        sslRows = new ArrayList<SslTermination>();
        prompt = "sslKeyEncrypter>";
        BUFFSIZE = 1024 * 32;
    }

    public static String usage(String prog) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("usage is <conf.json>\n"));
        sb.append(String.format("Externally test the reuploader code for CloudFiles\n"));
        sb.append(String.format("the json conf file will be of the form:\n%s\n", HibernateDbConf.exampleJson));
        sb.append(String.format("\n"));
        return sb.toString();
    }

    public static void main(String[] argv) {
        stdin = StaticFileUtils.inputStreamToBufferedReader(System.in, BUFFSIZE);
        String cmdLine;
        String prog = Debug.getClassName(SslKeyEncryptCli.class);
        if (argv.length < 1) {
            System.out.printf("%s", usage(prog));
            return;
        }
        String confFile = StaticFileUtils.expandUser(argv[0]);
        System.out.printf("Using conf file \"%s\"\n", confFile);
        System.out.printf("Press enter to continue\n");
        try {
            cmdLine = stdin.readLine();
        } catch (Exception ex) {
            return;
        }

        try {
            hu = new HuApp();
            huConf = HibernateDbConf.newHibernateConf(confFile);
            hu.setDbMap(huConf);
        } catch (Exception ex) {
            System.out.printf("Error connecting to database.\n%s\n Bailing out. :(\n", Debug.getExtendedStackTrace(ex));
            return;
        }
        System.out.printf("Connected to db\n");
        while (true) {
            try {
                System.out.printf("%s ", prompt);
                System.out.flush();
                cmdLine = stdin.readLine();
                if (cmdLine == null) {
                    break; // EOF
                }
                String[] args = StaticStringUtils.stripBlankArgs(cmdLine);
                if (args.length < 1) {
                    System.out.printf("usage is help\n");
                    continue;
                }
                String cmd = args[0];
                if (cmd.equals("help")) {
                    System.out.printf("gc                         #Run garbage collector\n");
                    System.out.printf("mem                        #Display memory usage\n");
                    System.out.printf("exit                       #exit program\n");
                    System.out.printf("count                      #Count all entries in memory\n");
                    System.out.printf("clear                      #Clear the lb_ssl entries from memory\n");
                    System.out.printf("read                       #Read all lb_ssl entries from database into memory\n");
                    System.out.printf("write                      #Save all entries in memory to database\n");
                    System.out.printf("encrypt                    #Encrypt all entries\n");
                    System.out.printf("decrypt                    #decrypt all entires\n");
                    System.out.printf("showkey                    #Display key used for encrypt/decrypt\n");
                    System.out.printf("reloadkey                  #Force a reload of the key from the publicapiconf\n");
                    System.out.printf("begin                      #begin transaction\n");
                    System.out.printf("rollback                   #rollback transaction from database\n");
                    System.out.printf("commit                     #Commit transaction to database\n");
                    System.out.printf("display <start> <stop>     #Display ssl entries for the range\n");
                    System.out.printf("displayraw <start> <stop>  #Display raw ssl entries for the range\n");
                    System.out.printf("validatekeys                  #Scan keys to make sure there actually valid\n");
                    System.out.printf("validaterawkeys               #scan rawkeys to make sure there valid\n");
                    System.out.printf("\n");
                    continue;
                } else if (cmd.equals("gc")) {
                    Debug.gc();
                    System.out.printf("Garbage collector invoked\n");
                    continue;
                } else if (cmd.equals("mem")) {
                    String memInfo = Debug.showMem();
                    System.out.printf("%s\n", memInfo);
                    continue;
                } else if (cmd.equals("clear")) {
                    System.out.printf("Clearing %d entries from memory\n", sslRows.size());
                    sslRows = new ArrayList<SslTermination>();
                    System.out.printf("Cleared\n");
                    continue;
                } else if (cmd.equals("count")) {
                    System.out.printf("%d entries loaded from database\n", sslRows.size());
                    continue;
                } else if (cmd.equals("begin")) {
                    System.out.printf("Starting transaction\n");
                    hu.begin();
                    System.out.printf("Transaction started\n");
                    continue;
                } else if (cmd.equals("rollback")) {
                    System.out.printf("Aborting transaction\n");
                    hu.rollback();
                    System.out.printf("Rolled back\n");
                    continue;
                } else if (cmd.equals("commit")) {
                    System.out.printf("commiting transaction\n");
                    hu.commit();
                    System.out.printf("Transaction commited\n");
                    continue;
                } else if (cmd.equals("read")) {
                    System.out.printf("Reading lb_ssl from database\n");
                    sslRows = hu.getList(SSL_QUERY);
                    System.out.printf("rows Read\n");
                    continue;
                } else if (cmd.equals("display") && args.length >= 3) {
                    int start = Integer.valueOf(args[1]);
                    int stop = Integer.valueOf(args[2]);
                    int i;
                    System.out.printf("display rows %d to %d\n==========\n", start, stop);
                    for (i = start; i <= stop; i++) {
                        SslTermination sslTerm = sslRows.get(i);
                        System.out.printf("ssl[%5d]\n%s\n==========\n", i, sslTerm.toString());
                    }
                    continue;
                } else if (cmd.equals("displayraw") && args.length >= 3) {
                    int start = Integer.valueOf(args[1]);
                    int stop = Integer.valueOf(args[2]);
                    int i;
                    System.out.printf("display rows %d to %d\n", start, stop);
                    for (i = start; i <= stop; i++) {
                        SslTermination sslTerm = sslRows.get(i);
                        String rawKey = sslTerm.getPrivatekeyRaw();
                        System.out.printf("ssl[%5d]\n%s\nrawKey: %s\n==========\n", i, sslTerm.toString(), rawKey);
                    }
                    continue;
                } else if (cmd.equals("showkey")) {
                    String key = SslTerminationConfig.getEncryptKey();
                    System.out.printf("Encrypting key is \"%s\"\n", key);
                    continue;
                } else if (cmd.equals("reloadkey")) {
                    System.out.printf("Reloading key from public-api.conf\n");
                    SslTerminationConfig.resetConfigs(null);
                    System.out.printf("Key was reloaded %d times\n", SslTerminationConfig.getResetCount());
                    continue;
                } else if (cmd.equals("encrypt")) {
                    int i;
                    int nRows = sslRows.size();
                    System.out.printf("Encrypting %d keys\n", nRows);
                    for (i = 0; i < nRows; i++) {
                        String key = SslTerminationConfig.getEncryptKey();
                        SslTermination sslTerm = sslRows.get(i);
                        String ptext = sslTerm.getPrivatekeyRaw();
                        if (ptext == null) {
                            System.out.printf("not encrypted null key %d of %d\n", i, nRows);
                            continue;
                        }
                        String ctext = Aes.b64encrypt_str(ptext, key);
                        sslTerm.setPrivatekeyRaw(ctext);
                        System.out.printf("encrypted entrie %d of %d keys\n", i, nRows);
                    }
                    continue;
                } else if (cmd.equals("decrypt")) {
                    int i;
                    int nRows = sslRows.size();
                    System.out.printf("Decrypting %d keys\n", nRows);
                    for (i = 0; i < nRows; i++) {
                        String key = SslTerminationConfig.getEncryptKey();
                        SslTermination sslTerm = sslRows.get(i);
                        String ctext = sslTerm.getPrivatekeyRaw();
                        if (ctext == null) {
                            System.out.printf("Not decrypting null key %d of %d\n", i, nRows);
                            continue;
                        }
                        String ptext = Aes.b64decrypt_str(ctext, key);
                        sslTerm.setPrivatekeyRaw(ptext);
                        System.out.printf("encrypted %d of %d keys\n", i, nRows);
                    }
                } else if (cmd.equals("write")) {
                    int i;
                    int nRows = sslRows.size();
                    System.out.printf("Writing %d keys to database\n", nRows);
                    for (i = 0; i < nRows; i++) {
                        String key = SslTerminationConfig.getEncryptKey();
                        SslTermination sslTerm = sslRows.get(i);
                        hu.saveOrUpdate(sslTerm);
                        System.out.printf("wrote %d of %d rows\n", i, nRows);
                    }
                    System.out.printf("If your sure about this run the commit command otherwise type rollback\n");
                } else if (cmd.equals("validatekeys")) {
                    int i;
                    int nRows = sslRows.size();
                    KeyPair kp;
                    List<ErrorEntry> errors = new ArrayList<ErrorEntry>();
                    System.out.printf("Scanning keys\n");
                    for (i = 0; i < nRows; i++) {
                        errors.clear();
                        SslTermination sslTerm = sslRows.get(i);
                        String key = sslTerm.getPrivatekey();
                        if (key == null) {
                            System.out.printf("key %d of %d: NULL\n", i, nRows);
                            continue;
                        }
                        kp = ZeusUtils.validateKey(key, errors);
                        if (errors.size() > 0) {
                            System.out.printf("key %d of %d: INVALID\n", i, nRows);
                            continue;
                        } else {
                            System.out.printf("key %d of %d: VALID\n", i, nRows);
                            continue;
                        }
                    }
                } else if (cmd.equals("validaterawkeys")) {
                    int i;
                    int nRows = sslRows.size();
                    KeyPair kp;
                    List<ErrorEntry> errors = new ArrayList<ErrorEntry>();
                    System.out.printf("Scanning keys\n");
                    for (i = 0; i < nRows; i++) {
                        errors.clear();
                        SslTermination sslTerm = sslRows.get(i);
                        String key = sslTerm.getPrivatekeyRaw();
                        if (key == null) {
                            System.out.printf("key %d of %d: NULL\n", i, nRows);
                            continue;
                        }
                        kp = ZeusUtils.validateKey(key, errors);
                        if (errors.size() > 0) {
                            System.out.printf("key %d of %d: INVALID\n", i, nRows);
                            continue;
                        } else {
                            System.out.printf("key %d of %d: VALID\n", i, nRows);
                            continue;
                        }
                    }
                }
            } catch (Exception ex) {
                System.out.printf("Exception: %s\n", Debug.getExtendedStackTrace(ex));
            }
        }
    }
}
