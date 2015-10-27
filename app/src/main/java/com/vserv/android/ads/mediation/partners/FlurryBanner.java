package com.vserv.android.ads.mediation.partners;

import android.content.Context;
import android.util.Log;
import android.view.ViewGroup;

import com.flurry.android.FlurryAdType;
import com.flurry.android.FlurryAds;
import com.flurry.android.FlurryAdSize;
import com.flurry.android.FlurryAdListener;
import com.flurry.android.FlurryAgent;

import java.util.Map;


/*
 * Tested with flurry SDK 5.6.0
 */


public class FlurryBanner extends VservCustomAd implements FlurryAdListener {
    /*
     * These keys are intended for vserv internal use. Do not modify.
     */
    private static final String APP_ID = "appid";
    private static final String ADSPACE_NAME = "adspace";
    private static final String DEFAULT_ADSPACE_NAME = "Banner";

    private VservCustomAdListener mBannerListener;
    public boolean LOGS_ENABLED = true;
    private Context mcontext;
    private String adSpace;
    private ViewGroup adLayout;
    private String appid;
    private boolean isCacheAd = false;

    @Override
    public void loadAd(final Context context,
                       final VservCustomAdListener customEventListener,
                       final Map<String, Object> localExtras,
                       final Map<String, Object> serverExtras) {
        try {
//			if (LOGS_ENABLED) {
//				Log.d("vserv", "Inside flurry loadBanner ");
//			}
            mcontext = context;
            mBannerListener = customEventListener;
            if (localExtras != null) {
                if (localExtras.containsKey("adview")) {
                    adLayout = (ViewGroup) localExtras.get("adview");
                }
                if (localExtras.containsKey("cacheAd")) {
                    isCacheAd = (Boolean) localExtras.get("cacheAd");
                }
            }

            if (extrasAreValid(serverExtras)) {
                appid = serverExtras.get(APP_ID).toString();
//				if (LOGS_ENABLED) {
//					Log.d("vserv", "Inside loadBanner adUnitId " + appid);
//				}
                adSpace = serverExtras.containsKey(ADSPACE_NAME) ? serverExtras
                        .get(ADSPACE_NAME).toString() : DEFAULT_ADSPACE_NAME;

            } else {
                mBannerListener.onAdFailed(0);
                return;
            }
            // configure Flurry
            FlurryAgent.setLogEnabled(false);
            // init Flurry
            FlurryAgent.init(mcontext, appid);
            FlurryAgent.onStartSession(mcontext, appid);
            FlurryAds.setAdListener(this);
//			if (LOGS_ENABLED) {
//				Log.i("vserv", "set listener appid:: " + appid);
//			}
            // FlurryAgent.setLogEnabled(true);
            // FlurryAgent.setLogLevel(2);
            //
            // FlurryAds.enableTestAds(true);
            if (!FlurryAds.isAdReady(adSpace)) {
//				if (LOGS_ENABLED) {
//					Log.i("vserv", "loadInterstitial fetch:: " + adSpace);
//				}
                FlurryAds.fetchAd(mcontext, adSpace, adLayout,
                        FlurryAdSize.BANNER_BOTTOM);
            }

        } catch (Exception e) {
            if (mBannerListener != null) {
                mBannerListener.onAdFailed(0);
            }
            e.printStackTrace();
            return;
        }
    }

    @Override
    public void showAd() {
//		if (LOGS_ENABLED) {
//			Log.i("vserv", "showAd:: ");
//		}
        FlurryAds.displayAd(mcontext, adSpace, adLayout);
    }

    @Override
    public void onInvalidate() {
        // Views.removeFromParent(mGoogleAdView);
        try {

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean extrasAreValid(Map<String, Object> serverExtras) {
        return serverExtras.containsKey(APP_ID);
    }

    // private class AdViewListener implements FlurryAdListener {

    @Override
    public void onAdClicked(String arg0) {
        if (LOGS_ENABLED) {
            Log.d("vserv", "Flurry banner ad clicked." + arg0);
        }
        if (mBannerListener != null) {
            mBannerListener.onAdClicked();
        }

    }

    @Override
    public void onAdClosed(String arg0) {
        if (LOGS_ENABLED) {
            Log.d("vserv", "Flurry banner onAdClosed." + arg0);
        }
        // if (mBannerListener != null) {
        // mBannerListener.onAdDismissed();
        // }
    }

    @Override
    public void onAdOpened(String arg0) {
        if (LOGS_ENABLED) {
            Log.d("vserv", "Flurry banner onAdOpened." + arg0);
        }

    }

    @Override
    public void onApplicationExit(String arg0) {
        if (LOGS_ENABLED) {
            Log.d("vserv", "Flurry banner onApplicationExit." + arg0);
        }
        if (mBannerListener != null) {
            mBannerListener.onLeaveApplication();
        }

    }

    @Override
    public void onRenderFailed(String arg0) {
        if (LOGS_ENABLED) {
            Log.d("vserv", "Flurry banner onRenderFailed." + arg0);
        }
        if (mBannerListener != null) {
            mBannerListener.onAdFailed(0);
        }

    }

    @Override
    public void onRendered(String arg0) {
        if (LOGS_ENABLED) {
            Log.d("vserv", "Flurry banner onRendered." + arg0);
        }
        if (mBannerListener != null) {
            mBannerListener.onAdShown();
        }

    }

    @Override
    public void onVideoCompleted(String arg0) {
        if (LOGS_ENABLED) {
            Log.d("vserv", "Flurry banner onVideoCompleted." + arg0);
        }

    }

    @Override
    public boolean shouldDisplayAd(String arg0, FlurryAdType arg1) {
        if (LOGS_ENABLED) {
            Log.d("vserv", "Flurry banner shouldDisplayAd." + arg0);
        }
        return true;
    }

    @Override
    public void spaceDidFailToReceiveAd(String arg0) {
        // if (LOGS_ENABLED) {
        Log.d("vserv", "Flurry banner spaceDidFailToReceiveAd." + arg0);
        // }
        if (mBannerListener != null) {
            mBannerListener.onAdFailed(0);
        }

    }

    @Override
    public void spaceDidReceiveAd(String arg0) {
        if (LOGS_ENABLED) {
            Log.d("vserv", "Flurry banner ad spaceDidReceiveAd." + arg0);
        }
        if (!isCacheAd) {
            showAd();
        }
        if (mBannerListener != null) {
            mBannerListener.onAdLoaded(null);
        }

    }
    /*
	 * Flurry AdListener implementation
	 */

    // }

    public void onPause() {

    }

    public void onResume() {

    }

}