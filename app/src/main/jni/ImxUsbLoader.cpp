// This file is heavily based on https://github.com/boundarydevices/imx_usb_loader/blob/master/imx_usb.c

#include <jni.h>
#include "JLogProxy.h"

#include <linux/usbdevice_fs.h>
#include <linux/usb/ch9.h>
#include <linux/hid.h>
#include <linux/hiddev.h>
#include <sys/ioctl.h>
#include <cstdlib>
#include <cstring>
#include <portable.h>
#include <errno.h>

extern "C" {
#include <imx_log_helper.h>
#include <imx_loader_config.h>
#include <imx_sdp.h>
}

static void handleWork(JLogProxy& log, sdp_dev* dev) {
    if (do_status(dev) < 0) {
        log.e("Status error");
        return;
    }

    sdp_work* curr = dev->work;
    while (curr) {
        if (curr->mem) {
            perform_mem_work(dev, curr->mem);
        }
        if (curr->filename[0]) {
            int err = DoIRomDownload(dev, curr, 0);
            if (err) {
                log.e("DoIRomDownload failed; err = %i", err);
                break;
            }
        }
        if (!curr->next) {
            if (curr->plug)
                curr->plug = 0;
            else
                break;
        } else {
            curr = curr->next;
        }

        if (do_status(dev) < 0) {
            log.e("Device disconnected");
            break;
        }
    }
}

static int funcHidTransfer(sdp_dev* dev, int report, unsigned char* p, unsigned int cnt,
                           unsigned int, int* last_trans) {
    int err;
    int fd = *((int*) dev->priv);
    if (cnt > dev->max_transfer)
        cnt = dev->max_transfer;
    unsigned char buf[1028];
    buf[0] = (unsigned char) report;
    if (report < 3) {
        memcpy(&buf[1], p, cnt);
        if (report == 2)
            cnt = dev->max_transfer;

        usbdevfs_ctrltransfer transfer;
        memset(&transfer, 0, sizeof(transfer));
        transfer.bRequestType = USB_TYPE_CLASS | USB_RECIP_INTERFACE | USB_DIR_OUT;
        transfer.bRequest = HID_REQ_SET_REPORT;
        transfer.wValue = (__u16) ((HID_REPORT_TYPE_OUTPUT << 8) | report);
        transfer.wIndex = 0;
        transfer.wLength = (__u16) (cnt + 1);
        transfer.data = buf;
        transfer.timeout = 1000;
        err = ioctl(fd, USBDEVFS_CONTROL, &transfer);
        *last_trans = (err > 0) ? err - 1 : 0;
        if (err > 0)
            err = 0;
        else
            err = errno;
    } else {
        *last_trans = 0;
        memset(&buf[1], 0, cnt);

        usbdevfs_urb urb;
        memset(&urb, 0, sizeof(usbdevfs_urb));
        urb.type = USBDEVFS_URB_TYPE_INTERRUPT;
        urb.endpoint = 1 + USB_DIR_IN;
        urb.buffer = buf;
        urb.buffer_length = cnt + 1;
        urb.usercontext = (void*) 0xf0f;
        ioctl(fd, USBDEVFS_SUBMITURB, &urb);

        usbdevfs_urb* purb = nullptr;
        do {
            if (ioctl(fd, USBDEVFS_REAPURB, &purb)) {
                imx_logger_log("ioctl: error");
                return -1;
            }
            if (purb != &urb)
                imx_logger_log("Reaped wrong URB");
        } while (purb != &urb);
        err = urb.status;
        if (err >= 0) {
            *last_trans = urb.actual_length;
            if (buf[0] == (unsigned char)report) {
                if (*last_trans > 1) {
                    *last_trans -= 1;
                    memcpy(p, &buf[1], (size_t) *last_trans);
                }
            } else {
                imx_logger_log("Unexpected report %i err=%i, cnt=%i, last_trans=%i, %02x %02x %02x %02x", buf[0], err, cnt, *last_trans, buf[0], buf[1], buf[2], buf[3]);
                err = 0;
            }
        }
        ioctl(fd, USBDEVFS_DISCARDURB, &urb);
    }
    return err;
}

static int funcBulkTransfer(sdp_dev* dev, int report, unsigned char* p, unsigned int cnt,
                            unsigned int, int* last_trans) {
    int fd = *((int*) dev->priv);
    if (cnt > dev->max_transfer)
        cnt = dev->max_transfer;

    usbdevfs_bulktransfer transfer;
    memset(&transfer, 0, sizeof(transfer));
    transfer.ep = (unsigned int) ((report < 3) ? 1 : 2 + USB_DIR_IN);
    transfer.len = cnt;
    transfer.data = p;
    transfer.timeout = 1000;
    int ret = ioctl(fd, USBDEVFS_BULK, &transfer);
    if (ret < 0) { // error
        *last_trans = 0;
        return ret;
    }
    *last_trans = ret;
    return 0;
}

extern "C" JNIEXPORT void JNICALL
Java_io_mrarm_switchlinuxlauncher_ImxUsbLoader_nativeLoad(
        JNIEnv* env, jclass, jobject jlog, jint fd, jboolean hidMode, jstring confPath) {
    JLogProxyEnv logEnv(env);
    JLogProxy log(logEnv, jlog);
    imx_logger_set(&log);

    const char* str = env->GetStringUTFChars(confPath, nullptr);
    sdp_dev* dev = parse_conf(str);
    env->ReleaseStringUTFChars(confPath, str);

    if (dev == nullptr) {
        imx_logger_log(nullptr);
        return;
    }

    dev->priv = &fd;
    dev->transfer = hidMode ? funcHidTransfer : funcBulkTransfer;

    handleWork(log, dev);

    free(dev);

    imx_logger_set(nullptr);
}