#include <jsvc.h>

int jsvc_help(jsvc_config *config) {
    printf("Usage: %s [@file] [-options] class [args...]\n",config->binary);
    printf("\n");
    printf("Where options include:\n");
    printf("\n");
    printf("    @[file]\n");
    printf("        read command line options from the specified file\n");
    printf("        if [file] wasn't specified, or there were no other\n");
    printf("        arguments specified on the command line, the default\n");
    printf("        default arguments are read from the default file:\n");
    printf("        %s\n",config->parfile);
    printf("\n");
    printf("    -h, -? or -help\n");
    printf("        print this help message and exit\n");
    printf("\n");
    printf("    -vm:[server|client|<jvm>]\n");
    printf("        select the server, client or another virtual machine,\n");
    printf("        where <jvm> is the name of the virtual machine to be\n");
    printf("        used\n");
    printf("\n");
    printf("    -cp <classpath> or -classpath <classpath>\n");
    printf("        set search path for application classes and resources\n");
    printf("        this is equal to -Djava.class.path=<classpath> where\n");
    printf("        <classpath> is a list of directories and/or jar/zip\n");
    printf("        files separated by \":\"\n");
    printf("        (example: -classpath /usr/java/lib:/home/user/lib.jar)\n");
    printf("\n");
    printf("    -verbose:[class|gc|jni]\n");
    printf("        enable verbose options\n");
    printf("        multiple options can be separated with \",\"\n");
    printf("        (example: -verbose:class,gc)\n");
    printf("\n");
    printf("    -version\n");
    printf("        print product version and exit\n");
    printf("\n");
    printf("    -showversion\n");
    printf("        print product version and continue\n");
    printf("\n");
#ifndef WIN32
    printf("    -user:<username>\n");
    printf("        change the user of the virtual machine process after\n");
    printf("        startup\n");
    printf("\n");
    printf("    -group:<username>\n");
    printf("        change the group of the virtual machine process after\n");
    printf("        startup\n");
    printf("\n");
    printf("    -pidfile:<file>\n");
    printf("        specify the file name where the pid will be written to\n");
    printf("        this option is ignored in case -nodetach is specified\n");
    printf("        (default: %s)\n",config->pidfile);
    printf("\n");
    printf("    -nodetach\n");
    printf("        don't detach from the console and fork in background\n");
    printf("        after initialization\n");
    printf("\n");
#endif
    printf("    -D<name>=<value>\n");
    printf("        set a system property\n");
    printf("\n");
    printf("    -X<...>\n");
    printf("        set a non standard java virtual machine option\n");
    printf("        to check what non standard options your java virtual\n");
    printf("        supports try to invoke \"java -X\"\n");
    printf("\n");
}
