#include<sys/types.h>
#include<sys/stat.h>
#include<unistd.h>
#include<string.h>
#include<stdio.h>
#include<pcre.h>
#include<pwd.h>

#define STRSIZE 1024

int isValidRegex(char *pattern_str,char **error){
    int isValid;
    pcre *test_re = NULL;
    int offset;
    test_re = pcre_compile(pattern_str,0,(const char **)error,&offset,NULL);
    if (test_re == NULL){
        isValid = 0;
    }else{
        isValid = 1;
        free(test_re);
    }
    return isValid;
}

int sudo2Nobody(){
    struct passwd *pw;
    pw = getpwnam("nobody");
    setgid(pw->pw_gid);
    setuid(pw->pw_uid);
    return 0;
}

int main(int argc,char **argv){
    char regex_pattern[STRSIZE+1];
    char *error_str;
    sudo2Nobody();

    fgets(regex_pattern,STRSIZE,stdin);
    if(!isValidRegex(regex_pattern,&error_str)){
        printf("BAD|%s",error_str);
        return -1;
    }
    printf("GOOD|");
    return 0;
}


