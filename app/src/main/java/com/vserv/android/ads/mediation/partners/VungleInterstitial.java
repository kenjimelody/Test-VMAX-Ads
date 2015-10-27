package com.vserv.android.ads.mediation.partners;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.vserv.android.ads.api.VservAdView;
import com.vserv.android.ads.reward.RewardVideo;
import com.vserv.android.ads.reward.RewardVideoDelegate;
import com.vungle.publisher.EventListener;
import com.vungle.publisher.VunglePub;

import java.util.Map;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/*
 * Tested with Vungle SDK 3.3.1
 */
public class VungleInterstitial extends VservCustomAd {

    /*
     * APP_ID_KEY is intended for vserv internal use. Do not modify.
     */
    private static final String APP_ID_KEY = "appid";

    private VunglePub mVunglePub;
    private Handler mHandler;
    private ScheduledThreadPoolExecutor mScheduledThreadPoolExecutor;
    private boolean mIsLoading;

    private VservCustomAdListener mInterstitialListener;
    private Context context;
    public boolean LOGS_ENABLED = false;
    private int attempt = 0;
    private int timeout = 20;

    private RewardVideoDelegate rewardedVideoDelegate;
    private RewardVideo rewardedVideoAd;
    private long reward = 0;

    @Override
    public void loadAd(Context context,
                       VservCustomAdListener customEventInterstitialListener,
                       Map<String, Object> localExtras, Map<String, Object> serverExtras) {
        try {
            mInterstitialListener = customEventInterstitialListener;
            this.context = context;
            mHandler = new Handler();
            mScheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(1);
            Activity activity = (Activity) context;
            if (LOGS_ENABLED) {
                Log.i("vserv", "load vungleInterstitial:: ");
            }

            final String appId;
            if (extrasAreValid(serverExtras)) {
                appId = serverExtras.get(APP_ID_KEY).toString();
            } else {
                mInterstitialListener.onAdFailed(0);
                return;
            }
            if (localExtras != null) {

                if (localExtras.containsKey("timeOut")) {
                    timeout = (Integer) localExtras.get("timeOut");
                }

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
            if (LOGS_ENABLED) {
                Log.i("vserv", "vungleInterstitial appId:: " + appId);
            }
            mVunglePub = VunglePub.getInstance();

            mVunglePub.init(activity, appId);
            mVunglePub.setEventListeners(vungleListener);

            scheduleOnInterstitialLoaded();
        } catch (Exception e) {
            if (LOGS_ENABLED) {
                Log.i("vserv", "init vungleInterstitial Exception:: " + e);
            }
            e.printStackTrace();
            mInterstitialListener.onAdFailed(0);
        }
    }

    @Override
    public void showAd() {
        try {
            if (LOGS_ENABLED) {
                Log.i("vserv", "showAd vungleInterstitial:: ");
            }
            if (mVunglePub.isAdPlayable()) {
                // if (LOGS_ENABLED) {
                // Log.i("vserv", "showAd vungleInterstitial 1:: ");
                // }
                // final AdConfig overrideConfig = new AdConfig();
                //
                // // set any configuration options you like.
                // // For a full description of available options, see the
                // // 'Configuration Options' section.
                // overrideConfig.setIncentivized(true);
                mVunglePub.playAd();
            } else {
                Log.d("vserv",
                        "Tried to show a Vungle interstitial ad before it finished loading. Please try again.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            mInterstitialListener.onAdFailed(0);
        }
    }

    @Override
    public void onInvalidate() {
        try {
            Log.d("vserv", "vungle onInvalidate.");
            if (LOGS_ENABLED) {
                Log.i("vserv", "onInvalidate vungleInterstitial:: ");
            }
            if (mVunglePub != null) {
                mVunglePub.setEventListeners(null);
            }

            mInterstitialListener = null;
            mScheduledThreadPoolExecutor.shutdownNow();
            mIsLoading = false;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean extrasAreValid(Map<String, Object> serverExtras) {
        return serverExtras.containsKey(APP_ID_KEY);
    }

    private void scheduleOnInterstitialLoaded() {
        try {
//			Log.d("vserv", "Vungle interstitial scheduleOnInterstitialLoaded.");
            Runnable runnable = new Runnable() {
                @Override
                public void run() {

                    if (mVunglePub.isAdPlayable()) {
                        Log.d("vserv",
                                "Vungle interstitial ad successfully loaded.");
                        mScheduledThreadPoolExecutor.shutdownNow();
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {

                                if (mInterstitialListener != null) {
                                    mInterstitialListener.onAdLoaded();
                                }
                            }
                        });
                        mIsLoading = false;
                    } else {
                        Log.d("vserv", "Vungle interstitial ad not loaded.");
                        attempt++;
                        if (attempt > timeout) {
                            mScheduledThreadPoolExecutor.shutdownNow();
                            mHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    if (mInterstitialListener != null) {
                                        mInterstitialListener.onAdFailed(0);
                                    }
                                }
                            });
                        }
                    }
                }
            };

            if (!mIsLoading) {
                Log.d("vserv",
                        "Vungle interstitial scheduleOnInterstitialLoaded mIsLoading.");
                mScheduledThreadPoolExecutor.scheduleAtFixedRate(runnable, 1,
                        1, TimeUnit.SECONDS);
                mIsLoading = true;
            }
        } catch (Exception e) {
            Log.d("vserv",
                    "Vungle interstitial scheduleOnInterstitialLoaded Exception."
                            + e);
            e.printStackTrace();
        }
    }

	/*
     * EventListener implementation
	 */

    private final EventListener vungleListener = new EventListener() {

        @Override
        public void onVideoView(final boolean isCompletedView,
                                final int watchedMillis, final int videoDurationMillis) {
            final double watchedPercent = (double) watchedMillis
                    / videoDurationMillis * 100;
            Log.d("vserv", String.format("%.1f%% of Vungle video watched.",
                    watchedPercent));
            VservAdView.isVideoComplete=isCompletedView;
            if (isCompletedView) {

                Log.d("vserv", "vungle onVideoViewComplete : ");


                if (rewardedVideoAd != null) {
                    Log.d("vserv", "vungle onVideoViewComplete reward: " + reward);
                    rewardedVideoAd.getWalletElement().awardVirtualCurrency(reward);
                }
                if (rewardedVideoDelegate != null) {
                    Log.d("vserv", "vungle onVideoViewComplete delegate: " );
                    rewardedVideoDelegate.onRewardVideoCompleted(reward);
                }

            }
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    Log.d("vserv", "Vungle interstitial ad dismissed.");
                    if (mInterstitialListener != null) {
                        mInterstitialListener.onAdDismissed();
                    }
                }
            });


        }

        @Override
        public void onAdStart() {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    Log.d("vserv", "Showing Vungle interstitial onAdStart.");
                    if (mInterstitialListener != null) {
                        mInterstitialListener.onAdShown();
                    }
                }
            });
        }

        @Override
        public void onAdUnavailable(final String s) {
            Log.d("vserv", "Vungle interstitial onAdUnavailable.");
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (mInterstitialListener != null) {
                        mInterstitialListener.onAdFailed(0);
                    }
                }
            });
        }


        @Override
        public void onAdEnd(boolean arg0) {
            Log.d("vserv", "Vungle interstitial onAdEnd.");
//            mHandler.post(new Runnable() {
//                @Override
//                public void run() {
//                    Log.d("vserv", "Vungle interstitial ad dismissed.");
//                    if (mInterstitialListener != null) {
//                        mInterstitialListener.onAdDismissed();
//                    }
//                }
//            });

        }

        @Override
        public void onAdPlayableChanged(boolean arg0) {
            Log.d("vserv", "Vungle interstitial onAdPlayableChanged.");

        }

    };

    public void onPause() {

		/*
         * Call pause for Vungle
		 */
        if (mVunglePub != null) {
            if (LOGS_ENABLED) {
                Log.d("vserv", "vungle onPause");
            }
            mVunglePub.onPause();
        }
    }

    public void onResume() {

		/*
         * Call resume for Vungle
		 */
        if (mVunglePub != null) {
            if (LOGS_ENABLED) {
                Log.d("vserv", "vungle onResume");
            }
            mVunglePub.onResume();
        }
    }

}
