package com.vserv.android.ads.mediation.partners;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.unity3d.ads.android.IUnityAdsListener;
import com.unity3d.ads.android.UnityAds;
import com.vserv.android.ads.api.VservAdView;
import com.vserv.android.ads.reward.RewardVideo;
import com.vserv.android.ads.reward.RewardVideoDelegate;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Narendra on 10/08/2015.
 */
public class UnityAdsRewardedVideo extends VservCustomAd implements IUnityAdsListener {
    private Context context;
    private VservCustomAdListener customListener;
    private Map<String, Object> localExtras;
    private Map<String, Object> serverExtras;
    private static final String GAME_ID_KEY = "gameid";
    private final String TAG = "Unity";
    private final boolean LOGS_ENABLED = true;

    private String gameId;
    private UnityAdsRewardedVideo _self = null;

    private RewardVideoDelegate rewardedVideoDelegate;
    private RewardVideo rewardedVideoAd;
    private long reward = 0;

    @Override
    public void loadAd(Context context, final VservCustomAdListener customListener, Map<String, Object> localExtras, Map<String, Object> serverExtras) {
        try {
            this.context = context;
            this.customListener = customListener;
            this.localExtras = localExtras;
            this.serverExtras = serverExtras;
            _self = UnityAdsRewardedVideo.this;

            if (extrasAreValid(serverExtras)) {
                if (LOGS_ENABLED) {
                    Log.i(TAG, "extrasAreValid");
                }
                gameId = serverExtras.get(GAME_ID_KEY).toString();

                if (localExtras != null) {


                    if (localExtras.containsKey("rewardVideoAd")) {
                        rewardedVideoAd = (RewardVideo) localExtras.get("rewardVideoAd");

                    }
                    if (rewardedVideoAd != null) {
                        rewardedVideoDelegate = rewardedVideoAd.getDelegate();

                    }
                    if (localExtras.containsKey("rewardAmount")) {
                        reward = (Long) localExtras.get("rewardAmount");

                    }
                }

                UnityAds.init((Activity) context, gameId, _self);
                UnityAds.setListener(_self);

                if (UnityAds.canShow()) {
                    if (LOGS_ENABLED) {
                        Log.i(TAG, "UnityAds.canShow is true");
                    }

                    onFetchCompleted();
                } else {
                    if (LOGS_ENABLED) {
                        Log.i(TAG, "UnityAds.canShow is false");
                    }

                    //onFetchFailed();
                }

            } else {
                if ((context != null) && (context instanceof Activity)) {
                    ((Activity) context).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (customListener != null) {
                                if (LOGS_ENABLED) {
                                    Log.i(TAG, "inside else extrasAreValid onAdFailed");
                                }
                                customListener.onAdFailed(0);
                            }
                        }
                    });
                }
                return;
            }
        } catch (Exception e) {
            if ((context != null) && (context instanceof Activity)) {
                ((Activity) context).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (customListener != null) {
                            if (LOGS_ENABLED) {
                                Log.i(TAG, "inside else extrasAreValid onAdFailed");
                            }
                            customListener.onAdFailed(0);
                        }
                    }
                });
            }
        }
    }

    @Override
    public void showAd() {
        if (UnityAds.canShow()) {
            if (LOGS_ENABLED) {
                Log.i("vserv", "showAd canShow");
            }
            Map<String, Object> optionsMap = new HashMap<String, Object>();
//            optionsMap.put(UnityAds.UNITY_ADS_OPTION_NOOFFERSCREEN_KEY, true);
//            optionsMap.put(UnityAds.UNITY_ADS_OPTION_OPENANIMATED_KEY, false);
//            optionsMap.put(UnityAds.UNITY_ADS_OPTION_GAMERSID_KEY, "gom");
            optionsMap.put(UnityAds.UNITY_ADS_OPTION_MUTE_VIDEO_SOUNDS, false);
//            optionsMap.put(UnityAds.UNITY_ADS_OPTION_VIDEO_USES_DEVICE_ORIENTATION, false);
            UnityAds.show(optionsMap);
        } else {
            if (LOGS_ENABLED) {
                Log.i(TAG, "showAd canShow false");
            }
            if ((context != null) && (context instanceof Activity)) {
                ((Activity) context).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (customListener != null) {
                            if (LOGS_ENABLED) {
                                Log.i(TAG, "inside else showAd onAdFailed");
                            }

                            customListener.onAdFailed(0);
                        }
                    }
                });
            }
        }

    }

    @Override
    public void onInvalidate() {
        UnityAds.setListener(null);
    }

    @Override
    public void onHide() {
        if ((context != null) && (context instanceof Activity)) {
            ((Activity) context).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (customListener != null) {
                        if (LOGS_ENABLED) {
                            Log.i(TAG, "onVideoCompleted onAdDismissed on skip");
                        }
                        customListener.onAdDismissed();
                    }
                }
            });
        }

    }

    @Override
    public void onShow() {
//        Map<String, Object> optionsMap = new HashMap<String, Object>();
//        optionsMap.put(UnityAds.UNITY_ADS_OPTION_NOOFFERSCREEN_KEY, true);
//        optionsMap.put(UnityAds.UNITY_ADS_OPTION_OPENANIMATED_KEY, false);
//        optionsMap.put(UnityAds.UNITY_ADS_OPTION_GAMERSID_KEY, "gom");
//        optionsMap.put(UnityAds.UNITY_ADS_OPTION_MUTE_VIDEO_SOUNDS, true);
//        optionsMap.put(UnityAds.UNITY_ADS_OPTION_VIDEO_USES_DEVICE_ORIENTATION, false);
//
//        UnityAds.show(optionsMap);
    }

    @Override
    public void onVideoStarted() {
        if ((context != null) && (context instanceof Activity)) {
            ((Activity) context).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (customListener != null) {
                        if (LOGS_ENABLED) {
                            Log.i(TAG, "onVideoStarted onAdShown");
                        }
                        customListener.onAdShown();
                    }
                }
            });
        }
    }

    @Override
    public void onVideoCompleted(String rewardItemKey, boolean skipped) {
        if (LOGS_ENABLED) {
            Log.i(TAG, "Video Completed rewardItemKey : " + rewardItemKey);
            Log.i(TAG, "Video Completed skipped : " + skipped);
        }
        VservAdView.isVideoComplete=skipped;
        if (skipped) {
            VservAdView.isVideoComplete=false;
        } else {

            VservAdView.isVideoComplete=true;
            if (rewardedVideoAd != null) {
                Log.d("vserv", "vungle onVideoViewComplete reward: " + reward);
                rewardedVideoAd.getWalletElement().awardVirtualCurrency(reward);
            }
            if (rewardedVideoDelegate != null) {
                Log.d("vserv", "vungle onVideoViewComplete delegate: " );
                rewardedVideoDelegate.onRewardVideoCompleted(reward);
            }
        }

    }

    @Override
    public void onFetchCompleted() {
        if (UnityAds.canShow()) {
            if ((context != null) && (context instanceof Activity)) {
                ((Activity) context).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (customListener != null) {
                            if (LOGS_ENABLED) {
                                Log.i(TAG, "onFetchCompleted onAdLoaded");
                            }
                            customListener.onAdLoaded();
                        }
                    }
                });
            }
        } else {
            if ((context != null) && (context instanceof Activity)) {
                ((Activity) context).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (customListener != null) {
                            if (LOGS_ENABLED) {
                                Log.i(TAG, "onFetchCompleted onAdFailed");
                            }
                            customListener.onAdFailed(0);
                        }
                    }
                });
            }
        }
    }

    @Override
    public void onFetchFailed() {
        if ((context != null) && (context instanceof Activity)) {
            ((Activity) context).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (customListener != null) {
                        if (LOGS_ENABLED) {
                            Log.i(TAG, "onFetchFailed onAdFailed");
                        }
                        customListener.onAdFailed(0);
                    }
                }
            });
        }
    }

    private boolean extrasAreValid(Map<String, Object> extras) {
        return extras.containsKey(GAME_ID_KEY);
    }

    public void onPause() {

		/*
         * Call pause for UnityAds
		 */

    }

    public void onResume() {

		/*
         * Call resume for UnityAds
		 */
    }
}
