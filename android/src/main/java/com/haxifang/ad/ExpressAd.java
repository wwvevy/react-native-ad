package com.haxifang.ad;

import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bytedance.sdk.openadsdk.AdSlot;
import com.bytedance.sdk.openadsdk.FilterWord;
import com.bytedance.sdk.openadsdk.TTAdConstant;
import com.bytedance.sdk.openadsdk.TTAdDislike;
import com.bytedance.sdk.openadsdk.TTAdNative;
import com.bytedance.sdk.openadsdk.TTAppDownloadListener;
import com.bytedance.sdk.openadsdk.TTNativeExpressAd;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.haxifang.ad.utils.DislikeDialog;
import com.haxifang.ad.utils.TToast;

import java.util.List;

public class ExpressAd extends ReactContextBaseJavaModule {

    final private static String TAG = "ExpressAd";
    private static ReactApplicationContext mContext;
    private TTAdDislike mTTAdDislike;
    private TTNativeExpressAd mTTAd;
    private long startTime = 0;
    private boolean mHasShowDownloadActive = false;

    public ExpressAd(ReactApplicationContext context) {
        super(context);
        mContext = context;
    }

    @NonNull
    @Override
    public String getName() {
        return TAG;
    }

    @ReactMethod
    public void startAd(ReadableMap options, final Promise promise) {

        //拿到参数
        String appId = options.getString("appid"); //可空
        String codeId = options.getString("codeid");
        Log.d(TAG, "startAd:  appId: " + appId + ", codeId: " + codeId);

        // 启动插屏广告
        startTT(codeId);
    }


    /**
     * 启动穿山甲插屏广告
     *
     * @param codeId
     */
    public void startTT(String codeId) {
        Activity context = mContext.getCurrentActivity();
        try {
            loadExpressAd(codeId, 300,450);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "start express ad error: ", e);
        }
    }

    // 发送事件到RN
    public static void sendEvent(String eventName, @Nullable WritableMap params) {
        mContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                .emit(TAG + "-" + eventName, params);
    }

    public void loadExpressAd(String codeId, int expressViewWidth, int expressViewHeight) {
        //step4:创建广告请求参数AdSlot,具体参数含义参考文档
        AdSlot adSlot = new AdSlot.Builder()
                .setCodeId(codeId) //广告位id
                .setAdCount(1) //请求广告数量为1到3条
                .setExpressViewAcceptedSize(expressViewWidth, expressViewHeight) //期望模板广告view的size,单位dp
                .build();
        //step5:请求广告，对请求回调的广告作渲染处理
        AdBoss.TTAdSdk.loadInteractionExpressAd(adSlot, new TTAdNative.NativeExpressAdListener() {
            @Override
            public void onError(int code, String message) {
                TToast.show(mContext, "load error : " + code + ", " + message);
            }

            @Override
            public void onNativeExpressAdLoad(List<TTNativeExpressAd> ads) {
                if (ads == null || ads.size() == 0) {
                    return;
                }
                mTTAd = ads.get(0);
                bindAdListener(mTTAd);
                startTime = System.currentTimeMillis();
                showAd();
            }
        });
    }

    private void showAd() {
        if (mTTAd != null) {
            mTTAd.render();
        }else {
            TToast.show(mContext,"请先加载广告");
        }
    }


    private void bindAdListener(TTNativeExpressAd ad) {
        ad.setExpressInteractionListener(new TTNativeExpressAd.AdInteractionListener() {
            @Override
            public void onAdDismiss() {
                Log.d(TAG, "广告关闭");
                WritableMap params = Arguments.createMap();
                params.putBoolean("onAdClose", true);
                sendEvent("onAdClose", params);
            }

            @Override
            public void onAdClicked(View view, int type) {
                Log.d(TAG, "广告被点击");
                WritableMap params = Arguments.createMap();
                params.putBoolean("onAdClick", true);
                sendEvent("onAdClick", params);
            }

            @Override
            public void onAdShow(View view, int type) {
                Log.d(TAG, "广告展示");
                WritableMap params = Arguments.createMap();
                params.putBoolean("onAdShow", true);
                sendEvent("onAdShow", params);
            }

            @Override
            public void onRenderFail(View view, String msg, int code) {
                Log.e("ExpressView", "render fail:" + (System.currentTimeMillis() - startTime));
                TToast.show(mContext, msg + " code:" + code);
                WritableMap params = Arguments.createMap();
                params.putBoolean("onAdError", true);
                sendEvent("onAdError", params);
            }

            @Override
            public void onRenderSuccess(View view, float width, float height) {
                Log.e("ExpressView", "渲染成功,render suc:" + (System.currentTimeMillis() - startTime));
                //返回view的宽高 单位 dp
                mTTAd.showInteractionExpressAd(mContext.getCurrentActivity());
                WritableMap params = Arguments.createMap();
                params.putBoolean("onAdLoaded", true);
                sendEvent("onAdLoaded", params);
            }
        });
        bindDislike(ad, false);
        if (ad.getInteractionType() != TTAdConstant.INTERACTION_TYPE_DOWNLOAD) {
            return;
        }
        ad.setDownloadListener(new TTAppDownloadListener() {
            @Override
            public void onIdle() {
                //  TToast.show(mContext, "点击开始下载", Toast.LENGTH_LONG);
            }

            @Override
            public void onDownloadActive(long totalBytes, long currBytes, String fileName, String appName) {
                if (!mHasShowDownloadActive) {
                    mHasShowDownloadActive = true;
                    TToast.show(mContext, "下载中，点击暂停", Toast.LENGTH_LONG);
                }
            }

            @Override
            public void onDownloadPaused(long totalBytes, long currBytes, String fileName, String appName) {
                TToast.show(mContext, "下载暂停，点击继续", Toast.LENGTH_LONG);
            }

            @Override
            public void onDownloadFailed(long totalBytes, long currBytes, String fileName, String appName) {
                TToast.show(mContext, "下载失败，点击重新下载", Toast.LENGTH_LONG);
            }

            @Override
            public void onInstalled(String fileName, String appName) {
                TToast.show(mContext, "安装完成，点击图片打开", Toast.LENGTH_LONG);
            }

            @Override
            public void onDownloadFinished(long totalBytes, String fileName, String appName) {
                TToast.show(mContext, "点击安装", Toast.LENGTH_LONG);
            }
        });
    }

    private void bindDislike(TTNativeExpressAd ad, boolean customStyle) {
        if (customStyle) {
            //使用自定义样式
            List<FilterWord> words = ad.getFilterWords();
            if (words == null || words.isEmpty()) {
                return;
            }

            final DislikeDialog dislikeDialog = new DislikeDialog(mContext, words);
            dislikeDialog.setOnDislikeItemClick(new DislikeDialog.OnDislikeItemClick() {
                @Override
                public void onItemClick(FilterWord filterWord) {
                    //屏蔽广告
                    TToast.show(mContext, "点击 " + filterWord.getName());
                }
            });
            ad.setDislikeDialog(dislikeDialog);
            return;
        }
        //使用默认模板中默认dislike弹出样式
        ad.setDislikeCallback(mContext.getCurrentActivity(), new TTAdDislike.DislikeInteractionCallback() {
            @Override
            public void onSelected(int position, String value) {
                //TToast.show(mContext, "反馈了 " + value);
                TToast.show(mContext, "\t\t\t\t\t\t\t感谢您的反馈!\t\t\t\t\t\t\n我们将为您带来更优质的广告体验", 3);
            }

            @Override
            public void onCancel() {
                TToast.show(mContext, "点击取消 ");
            }

            @Override
            public void onRefuse() {
                TToast.show(mContext, "您已成功提交反馈，请勿重复提交哦！", 3);
            }

        });
    }
}
