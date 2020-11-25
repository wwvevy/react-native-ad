import React from "react";
import { NativeModules, NativeEventEmitter } from "react-native";


interface EVENT_TYPE {
	onAdError: string; // 广告加载失败监听
	onAdClick: string; // 广告被点击监听
	onAdClose: string; // 广告时间结束关闭
	onAdSkip: string; // 用户点击跳过广告监听
	onAdShow: string; // 开屏广告开始展示
}

const listenerCache = {};

export default ({ appid, codeid }) => {
	const { SplashAd } = NativeModules;
	const eventEmitter = new NativeEventEmitter(SplashAd);
	let result = SplashAd.loadSplashAd({ appid, codeid });

	return {
		result,
		subscribe: (type: keyof EVENT_TYPE, callback: (event: any) => void) => {
			if (listenerCache[type]) {
				listenerCache[type].remove();
			}
			return listenerCache[type] = eventEmitter.addListener("SplashAd-" + type, (event: any) => {
				callback(event);
			});
		}
	};
};
