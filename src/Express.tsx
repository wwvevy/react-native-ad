import React from 'react';
import { NativeModules, NativeEventEmitter } from 'react-native';

const listenerCache = {};

interface EVENT_TYPE {
    onAdError: string; // 广告加载失败监听
    onAdLoaded: string; // 广告加载成功监听
    onAdClick: string; // 广告被点击监听
    onAdClose: string; // 广告关闭监听
}

type expressInfo = {
    appid: string;
    codeid: string;
};

export default function (info: expressInfo) {
    const { ExpressAd } = NativeModules;
    const eventEmitter = new NativeEventEmitter(ExpressAd);
    const result = ExpressAd.startAd(info);

    return {
        result,
        subscribe: (type: keyof EVENT_TYPE, callback: (event: any) => void) => {
            if (listenerCache[type]) {
                listenerCache[type].remove();
            }
            return (listenerCache[type] = eventEmitter.addListener('ExpressAd-' + type, (event: any) => {
                callback(event);
            }));
        },
    };
}
