#include <cstring>
#include <thread>
#include <chrono>
#include <jni.h>
#include <linux/usb/ch9.h>
#include <linux/usbdevice_fs.h>
#include <sys/ioctl.h>
#include "JLogProxy.h"
#include "HexString.h"

extern "C" JNIEXPORT void JNICALL
Java_io_mrarm_switchlinuxlauncher_ShofEL2_nativeControlReadUnbounded(
        JNIEnv* env, jclass, jobject jlog, jint fd, jint size) {
    JLogProxyEnv logEnv(env);
    JLogProxy log(logEnv, jlog);

    log.v("Size: 0x%x", size);

    char* buffer = new char[sizeof(usb_ctrlrequest) + size];
    usb_ctrlrequest* header = (usb_ctrlrequest*) buffer;
    header->bRequestType = USB_TYPE_STANDARD | USB_RECIP_ENDPOINT | USB_DIR_IN;
    header->bRequest = 0;
    header->wValue = 0;
    header->wIndex = 0;
    header->wLength = (__le16) size;
    memset(&buffer[sizeof(usb_ctrlrequest)], 0, (size_t) size);

    log.v("%s", HexString::encode(buffer, sizeof(usb_ctrlrequest)).c_str());

    usbdevfs_urb* urb = (usbdevfs_urb*) new char[sizeof(usbdevfs_urb) + 1024];
    memset(urb, 0, sizeof(usbdevfs_urb) + 1024);
    urb->type = USBDEVFS_URB_TYPE_CONTROL;
    urb->endpoint = 0;
    urb->buffer = buffer;
    urb->buffer_length = sizeof(usb_ctrlrequest) + size;
    urb->actual_length = 0;
    urb->usercontext = (void*) 0xf0f;

    log.v("%s", HexString::encode((char*) urb, sizeof(usbdevfs_urb)).c_str());

    ioctl(fd, USBDEVFS_SUBMITURB, urb);
    std::this_thread::sleep_for(std::chrono::milliseconds(100));
    ioctl(fd, USBDEVFS_DISCARDURB, urb);
    usbdevfs_urb* purb;
    do {
        ioctl(fd, USBDEVFS_REAPURB, &purb);
        if (purb != urb)
            log.e("Reaped the wrong URB! addr 0x%lx != 0x%lx", (unsigned long) purb,
                  (unsigned long) &urb);
    } while (purb != urb);

    log.v("URB status: %d", urb->status);
    if (urb->usercontext != (void*) 0xf0f)
        log.e("Reaped the wrong URB! ctx=0x%lx", (unsigned long) urb->usercontext);

    delete[] urb;
    delete[] buffer;
}