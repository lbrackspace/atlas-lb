package org.openstack.atlas.util.debug;

public class DebugMain {
    private static final int PAGESIZE = 4096;
    public static void main(String[] args){
        String prog = Debug.getProgName(DebugMain.class);
        String usage = getUsage(prog);
        if(args.length <1){
            System.out.printf("%s",getUsage(prog));
            return;
        }
        String className = args[0];
        String classPath;
        try {
            classPath = Debug.findClassPath(className);
        } catch (ClassNotFoundException ex) {
            System.out.printf("Error loading class %s:%s\n",className,Debug.getEST(ex));
            return;
        }
        System.out.printf("%s\n",classPath);
    }

    private static String getUsage(String prog){
        StringBuilderWriter sbw = new StringBuilderWriter(PAGESIZE);
        sbw.printf("Usage is %s <className>\n",prog);
        sbw.printf("\n");
        sbw.printf("Prints out the jar path for the given file\n");
        return sbw.toString();
    }
}
