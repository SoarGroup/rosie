#include <jni.h>
#include <stdio.h>
#include "sphinxJNI.h"
#include <pocketsphinx.h>
#define MODELDIR "/home/bolt/SST/pocketsphinx-0.8/model"
// Implementation of native method sayHello() of HelloJNI class
JNIEXPORT jstring JNICALL Java_edu_umich_insoar_sphinxJNI_decodeAudio(JNIEnv *env, jobject thisObj) {
   ps_decoder_t *ps;
    cmd_ln_t *config;
    FILE *fh;
    char const *hyp, *uttid;
        int16 buf[512];
    int rv;
    
    int32 score;
   
    config = cmd_ln_init(NULL, ps_args(), FALSE,
			 "-logfn", "/home/bolt/SST/logfile",
			 "-hmm", MODELDIR "/hmm/en_US/hub4wsj_sc_8k",
			 "-lm", "/home/bolt/SST/sample.lm",
			 "-dict", "/home/bolt/SST/sample.dic",
			 NULL);
    if (config == NULL)
      return (*env)->NewStringUTF(env,"no config");// 1;
    ps = ps_init(config);
    if (ps == NULL)
      return (*env)->NewStringUTF(env,"no ps");
    
    fh = fopen("/home/bolt/insoar/forward.raw", "rb");
    if (fh == NULL) {
      perror("Failed to open goforward.raw");
      return (*env)->NewStringUTF(env,"failed to open file");
    }
    
    rv = ps_decode_raw(ps, fh, "forward", -1);
    if (rv < 0)
      return (*env)->NewStringUTF(env,"rv less 0");
    hyp = ps_get_hyp(ps, &score, &uttid);
    
    if (hyp == NULL)
      return (*env)->NewStringUTF(env,"no hypoth");
    // printf("%s\n", hyp);
    
    jstring result = (*env)->NewStringUTF(env,hyp);
    fclose(fh);
    ps_free(ps);
    
    return result;
}

