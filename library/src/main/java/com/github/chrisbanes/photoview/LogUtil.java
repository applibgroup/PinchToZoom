package com.github.chrisbanes.photoview;

import com.github.chrisbanes.library.BuildConfig;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;

public class LogUtil {

    private static final boolean IS_DEBUG = BuildConfig.DEBUG;
    private static final HiLogLabel LABEL = new HiLogLabel(HiLog.LOG_APP, 0x03709, "PhotoView_TAG");

    public static void d(String msg){
        if (IS_DEBUG){
            HiLog.debug(LABEL,msg);
        }
    }

    public static void d(String format,Object... arg){
        if (IS_DEBUG){
            HiLog.debug(LABEL,format,arg);
        }
    }

    public static void d(String msg,Throwable throwable){
        if (IS_DEBUG){
            HiLog.debug(LABEL,"msg:%{public}d ; error:%{public}d ",msg,throwable.getMessage());
        }
    }

    public static void i(String msg){
        if (IS_DEBUG){
            HiLog.info(LABEL,msg);
        }
    }

    public static void i(String format,Object... arg){
        if (IS_DEBUG){
            HiLog.info(LABEL,format,arg);
        }
    }

    public static void i(String msg,Throwable throwable){
        if (IS_DEBUG){
            HiLog.info(LABEL,"msg:%{public}d ; error:%{public}d ",msg,throwable.getMessage());
        }
    }
}
