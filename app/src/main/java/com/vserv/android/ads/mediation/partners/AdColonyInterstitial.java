package com.vserv.android.ads.mediation.partners;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.jirbo.adcolony.AdColony;
import com.jirbo.adcolony.AdColonyAd;
import com.jirbo.adcolony.AdColonyAdAvailabilityListener;
import com.jirbo.adcolony.AdColonyAdListener;
import com.jirbo.adcolony.AdColonyV4VCAd;
import com.jirbo.adcolony.AdColonyV4VCListener;
import com.jirbo.adcolony.AdColonyV4VCReward;
import com.jirbo.adcolony.AdColonyVideoAd;
import com.vserv.android.ads.api.VservAdView;
import com.vserv.android.ads.reward.RewardVideo;
import com.vserv.android.ads.reward.RewardVideoDelegate;

import java.util.*;
import java.util.concurrent.*;

/*
 * Tested with AdColony SDK 2.2.2.
 */
public class AdColonyInterstitial extends VservCustomAd implements
        AdColonyAdListener, AdColonyV4VCListener, AdColonyAdAvailabilityListener {
    /*
     *
	 * Please see AdColony's documentation for more information:
	 * https://github.com
	 * /AdColony/AdColony-Android-SDK/wiki/API-Details#configure
	 * -activity-activity-string-client_options-string-app_id-string-zone_ids-
	 */

    /*
     * These keys are intended for vserv internal use. Do not modify.
     */
    private static final String DEFAULT_CLIENT_OPTIONS = "google";
    private static final String CLIENT_OPTIONS_KEY = "store";
    private static final String APP_ID_KEY = "appid";
    private static final String ZONE_ID_KEY = "zoneid";
    private static final String ALL_ZONE_IDS_KEY = "allzoneids";
    private static final String AD_TYPE = "adtype";
    private static final String DEFAULT_AD_TYPE = "v4vc";


    // public static boolean isAdColonyConfigured = false;

    private RewardVideoDelegate rewardedVideoDelegate;
    private RewardVideo rewardedVideoAd;
    private VservCustomAdListener mInterstitialListener;
    private Handler mHandler;
    private AdColonyVideoAd mAdColonyVideoAd;
    private AdColonyV4VCAd v4vc_ad;
    private ScheduledThreadPoolExecutor mScheduledThreadPoolExecutor;
    private boolean mIsLoading;

    public boolean LOGS_ENABLED = true;
    private static final String TAG = "vserv";
    Context context;
    private String zoneid = null;
    private int attempt = 0;
    private int timeout = 20;
    private String adtype;
    private long reward = 0;

    public AdColonyInterstitial() {
        mScheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(10);
        mHandler = new Handler();
    }

    @Override
    public void loadAd(Context context,
                       VservCustomAdListener customEventListener,
                       Map<String, Object> localExtras, Map<String, Object> serverExtras) {
        try {
            this.context = context;
            if (LOGS_ENABLED) {
                Log.i("vserv", "load adcolonyInterstitial:: ");
            }
            mInterstitialListener = customEventListener;

            String clientOptions = null;
            String appId = null;
            String zoneId = null;
            String final_clientOptions = null;
            String[] allZoneIds = null;
            if (extrasAreValid(serverExtras)) {

                appId = serverExtras.get(APP_ID_KEY).toString();
                zoneId = serverExtras.get(ZONE_ID_KEY).toString();
                this.zoneid = zoneId;
                if (serverExtras.containsKey(ALL_ZONE_IDS_KEY)) {
                    allZoneIds = extractAllZoneIds(serverExtras);
                } else {
                    allZoneIds = new String[]{zoneid};
                }
                adtype = serverExtras.containsKey(AD_TYPE) ? serverExtras
                        .get(AD_TYPE).toString()
                        : DEFAULT_AD_TYPE;
                clientOptions = serverExtras.containsKey(CLIENT_OPTIONS_KEY) ? serverExtras
                        .get(CLIENT_OPTIONS_KEY).toString()
                        : DEFAULT_CLIENT_OPTIONS;
            } else {
                mInterstitialListener.onAdFailed(0);
                return;
            }

            if (localExtras != null) {
                if (localExtras.containsKey("appversion")) {
                    final_clientOptions = "version="
                            + localExtras.get("appversion").toString()
                            + ",store:" + clientOptions;
                    if (LOGS_ENABLED) {
                        Log.i("vserv", "final_clientOptions:: "
                                + final_clientOptions);
                    }
                }
                if (localExtras.containsKey("timeOut")) {
                    timeout = (Integer) localExtras.get("timeOut");
                    if (LOGS_ENABLED) {
                        Log.i("vserv", "Adcolony timeOut:: " + " " + timeout);
                    }
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
            // if (LOGS_ENABLED) {
            // Log.i("vserv", "isAdColonyConfigured:: " + isAdColonyConfigured);
            // }
            // if (!isAdColonyConfigured) {
            // isAdColonyConfigured = true;
            Log.i("vserv", "configure :: ");
            AdColony.configure((Activity) context, final_clientOptions, appId,
                    allZoneIds);
            AdColony.resume((Activity) context);
            // }
            // AdColony.addAdAvailabilityListener(this);
            if (adtype.equalsIgnoreCase("v4vc")) {

                // Notify this object about confirmed virtual currency.
                AdColony.addV4VCListener(this);

                // Notify this object about ad availability changes.
                AdColony.addAdAvailabilityListener(this);
                v4vc_ad = new AdColonyV4VCAd(zoneid).withListener(AdColonyInterstitial.this);
                Log.i("vserv", "init v4vc :: ");
            } else {
                Log.i("vserv", "init interstitial video :: ");

                mAdColonyVideoAd = new AdColonyVideoAd(zoneid);

                mAdColonyVideoAd.withListener(this);
            }
            scheduleOnInterstitialLoaded();
        } catch (Exception e) {
            e.printStackTrace();
            if (LOGS_ENABLED) {
                Log.i("vserv", "Exception:: " + e.getMessage());
            }
            mInterstitialListener.onAdFailed(0);
            return;
        }
    }

    private String[] extractAllZoneIds(Map<String, Object> serverExtras) {
        String result = serverExtras.get(ALL_ZONE_IDS_KEY).toString();
        if (LOGS_ENABLED) {
            Log.i("vserv", "result:: " + result);
        }
        String t_allzones[] = null;

        // AdColony requires at least one valid String in the allZoneIds array.
        if (result.length() == 0) {
            t_allzones = new String[]{zoneid};
        } else {
            t_allzones = result.split(",");
        }
        if (LOGS_ENABLED) {
            Log.i("vserv", "t_allzones:: " + t_allzones.length);
        }
        return t_allzones;
    }

    @Override
    public void showAd() {
        try {
            if (LOGS_ENABLED) {
                Log.i("vserv", "show adcolonyInterstitial:: ");
            }
            if (adtype.equalsIgnoreCase("v4vc")) {
                if (v4vc_ad != null) {
                    v4vc_ad.show();
                }
            } else {
                if (mAdColonyVideoAd != null) {
                    mAdColonyVideoAd.show();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            mInterstitialListener.onAdFailed(0);
        }
    }

    @Override
    public void onInvalidate() {
        try {
            Log.d("vserv", "AdColony onInvalidate.");
            if (mAdColonyVideoAd != null) {
                mAdColonyVideoAd.withListener(null);
            }
            if (v4vc_ad != null) {
                v4vc_ad.withListener(null);
            }
            // mHandler = null;
            // mScheduledThreadPoolExecutor = null;

            mAdColonyVideoAd = null;
            v4vc_ad = null;
            AdColony.onBackPressed();
            mInterstitialListener = null;

            mScheduledThreadPoolExecutor.shutdownNow();
            mIsLoading = false;
        } catch (Exception e) {
            e.printStackTrace();
            mInterstitialListener.onAdFailed(0);
        }
    }

    private boolean extrasAreValid(Map<String, Object> extras) {
        return extras.containsKey(APP_ID_KEY)
                && extras.containsKey(ZONE_ID_KEY);
    }

    private void scheduleOnInterstitialLoaded() {
        try {
            if (LOGS_ENABLED) {
                Log.i("vserv", " scheduleOnInterstitialLoaded:: ");
            }
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    if (adtype.equalsIgnoreCase("v4vc")) {
                        if (v4vc_ad != null && v4vc_ad.isReady()) {
                            Log.d("vserv",
                                    "AdColony interstitial ad successfully loaded.");
                            mIsLoading = false;
                            mScheduledThreadPoolExecutor.shutdownNow();
                            mHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    if (mInterstitialListener != null) {
                                        mInterstitialListener.onAdLoaded();
                                    }
                                }
                            });
                        } else {
                            Log.d("vserv", "AdColony interstitial ad not loaded.");
                            attempt++;
                            if (attempt > timeout) {
                                Log.d("vserv",
                                        "AdColony interstitial ad fetchiong timeout failed.");
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
                    } else {
                        if (mAdColonyVideoAd != null && mAdColonyVideoAd.isReady()) {
                            Log.d("vserv",
                                    "AdColony interstitial ad successfully loaded.");
                            mIsLoading = false;
                            mScheduledThreadPoolExecutor.shutdownNow();
                            mHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    if (mInterstitialListener != null) {
                                        mInterstitialListener.onAdLoaded();
                                    }
                                }
                            });
                        } else {
                            Log.d("vserv", "AdColony interstitial ad not loaded.");
                            attempt++;
                            if (attempt > timeout) {
                                Log.d("vserv",
                                        "AdColony interstitial ad fetchiong timeout failed.");
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
                }
            };

            if (!mIsLoading) {
                mScheduledThreadPoolExecutor.scheduleAtFixedRate(runnable, 1,
                        1, TimeUnit.SECONDS);
                mIsLoading = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (LOGS_ENABLED) {
                Log.i("vserv", " scheduleOnInterstitialLoaded Exception:: " + e);
            }
        }
    }

	/*
     * AdColonyAdListener implementation
	 */

    @Override
    public void onAdColonyAdStarted(AdColonyAd adColonyAd) {
        Log.d("vserv", "onAdColonyAdStarted.");

        mHandler.post(new Runnable() {
            @Override
            public void run() {

                if (mInterstitialListener != null) {
                    mInterstitialListener.onAdShown();
                }
            }
        });
    }

    @Override
    public void onAdColonyAdAttemptFinished(final AdColonyAd adColonyAd) {
        Log.d("vserv", "onAdColonyAdAttemptFinished.");
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (adColonyAd.skipped()) {
                    Log.d("vserv", "onAdColonyAdAttemptFinished skipped.");
                    if (mInterstitialListener != null) {
                        mInterstitialListener.onAdDismissed();
                    }
                } else if (adColonyAd.noFill()) {
                    Log.d("vserv", "onAdColonyAdAttemptFinished noFill.");
                    if (mInterstitialListener != null) {
                        mInterstitialListener.onAdFailed(0);
                    }
                } else if (adColonyAd.canceled()) {
                    Log.d("vserv", "onAdColonyAdAttemptFinished canceled.");

                    if (mInterstitialListener != null) {
                        mInterstitialListener.onAdDismissed();
                    }
                } else if (adColonyAd.notShown()) {
                    Log.d("vserv", "onAdColonyAdAttemptFinished notShown.");

                    if (mInterstitialListener != null) {
                        mInterstitialListener.onAdFailed(0);
                    }
                } else if (adColonyAd.shown()) {
                    Log.d("vserv", "onAdColonyAdAttemptFinished shown.");

                    if (mInterstitialListener != null) {
                        mInterstitialListener.onAdDismissed();
                    }
                }

            }
        });
    }

    public void onPause() {
        Log.d("vserv", "AdColony onPause.");
        AdColony.pause();

    }

    public void onResume() {
        Log.d("vserv", "AdColony onResume.");
        AdColony.resume((Activity) context);
    }

    @Override
    public void onAdColonyAdAvailabilityChange(boolean b, String s) {

    }

    @Override
    public void onAdColonyV4VCReward(AdColonyV4VCReward adColonyV4VCReward) {
        Log.d("vserv", "onAdColonyV4VCReward: ");
        VservAdView.isVideoComplete=true;
        if (rewardedVideoAd != null) {
            Log.d("vserv", "onAdColonyV4VCReward award: " + reward);
            rewardedVideoAd.getWalletElement().awardVirtualCurrency(reward);
        }
        if (rewardedVideoDelegate != null) {
            Log.d("vserv", "onAdColonyV4VCReward delegate: " );
            rewardedVideoDelegate.onRewardVideoCompleted(reward);
        }


    }

//
}