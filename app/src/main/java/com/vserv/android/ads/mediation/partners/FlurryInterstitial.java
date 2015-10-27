package com.vserv.android.ads.mediation.partners;

import android.content.Context;
import android.util.Log;
import android.widget.FrameLayout;

import com.flurry.android.FlurryAdListener;
import com.flurry.android.FlurryAdSize;
import com.flurry.android.FlurryAdType;
import com.flurry.android.FlurryAds;
import com.flurry.android.FlurryAgent;

import java.util.Map;

/*
 * Tested with flurry SDK 5.6.0.
 */

public class FlurryInterstitial extends VservCustomAd implements
        FlurryAdListener {
    /*
     * These keys are intended for Vserv internal use. Do not modify.
     */
    private static final String APP_ID = "appid";
    private static final String ADSPACE_NAME = "adspace";
    private static final String DEFAULT_ADSPACE_NAME = "Interstitial";

    private VservCustomAdListener mInterstitialListener;
    private Context mcontext;
    public boolean LOGS_ENABLED = true;
    private String appid;
    private String adSpace;
    private FrameLayout adLayout;

    @Override
    public void loadAd(final Context context,
                       final VservCustomAdListener customEventListener,
                       final Map<String, Object> localExtras,
                       final Map<String, Object> serverExtras) {
        try {
            mcontext = context;
//			if (LOGS_ENABLED) {
//				Log.i("vserv", "Flurry loadInterstitial:: ");
//			}
            mInterstitialListener = customEventListener;
            adLayout = new FrameLayout(mcontext);

            if (serverExtras != null) {
                if (extrasAreValid(serverExtras)) {
                    appid = serverExtras.get(APP_ID).toString();
//					if (LOGS_ENABLED) {
//						Log.i("vserv", "loadInterstitial appid:: " + appid);
//					}
                    adSpace = serverExtras.containsKey(ADSPACE_NAME) ? serverExtras
                            .get(ADSPACE_NAME).toString()
                            : DEFAULT_ADSPACE_NAME;
                } else {
                    mInterstitialListener.onAdFailed(0);
                    return;
                }
            } else {
                mInterstitialListener.onAdFailed(0);
                return;
            }
// configure Flurry
            FlurryAgent.setLogEnabled(false);
            // init Flurry
            FlurryAgent.init(mcontext, appid);

            FlurryAgent.onStartSession(mcontext, appid);
            // get callbacks for ad events
            FlurryAds.setAdListener(this);
//			if (LOGS_ENABLED) {
//				Log.i("vserv", "set listener appid:: " + appid);
//			}
//			FlurryAgent.setLogEnabled(true);
//			FlurryAgent.setLogLevel(2);

            // FlurryAds.enableTestAds(true);
            if (!FlurryAds.isAdReady(adSpace)) {
//				if (LOGS_ENABLED) {
//					Log.i("vserv", "loadInterstitial fetch:: " + adSpace);
//				}
                FlurryAds.fetchAd(mcontext, adSpace, adLayout,
                        FlurryAdSize.FULLSCREEN);
            }

        } catch (Exception e) {
            if (mInterstitialListener != null) {
                mInterstitialListener.onAdFailed(0);
            }
            if (LOGS_ENABLED) {
                Log.i("vserv", "load Flurry Interstitial Exception:: " + e);
            }
            e.printStackTrace();
            return;
        }
    }

    @Override
    public void showAd() {
        try {
//			if (LOGS_ENABLED) {
//				Log.i("vserv", "show Flurry Interstitial:: ");
//			}
            if (FlurryAds.isAdReady(adSpace)) {
                FlurryAds.displayAd(mcontext, adSpace, adLayout);
            }
        } catch (Exception e) {
            e.printStackTrace();
            mInterstitialListener.onAdFailed(0);
        }
    }

    @Override
    public void onInvalidate() {
        try {
//			if (LOGS_ENABLED) {
//				Log.i("vserv", "onInvalidate Interstitial:: ");
//			}
            FlurryAds.removeAd(mcontext, adSpace, adLayout);
            FlurryAds.setAdListener(null);
            FlurryAgent.onEndSession(mcontext);
            mInterstitialListener = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean extrasAreValid(Map<String, Object> serverExtras) {
        return serverExtras.containsKey(APP_ID);
    }

    // private class InterstitialAdListener implements FlurryAdListener {
    /*
	 * Flurry AdListener implementation
	 */

    @Override
    public void onAdClicked(String arg0) {
        if (LOGS_ENABLED) {
            Log.d("vserv", "Flurry interstitial ad clicked." + arg0);
        }
        if (mInterstitialListener != null) {
            mInterstitialListener.onAdClicked();
        }

    }

    @Override
    public void onAdClosed(String arg0) {
        if (LOGS_ENABLED) {
            Log.d("vserv", "Flurry interstitial onAdClosed." + arg0);
        }
        if (mInterstitialListener != null) {
            mInterstitialListener.onAdDismissed();
        }
    }

    @Override
    public void onAdOpened(String arg0) {
        if (LOGS_ENABLED) {
            Log.d("vserv", "Flurry interstitial onAdOpened." + arg0);
        }
        if (mInterstitialListener != null) {
            mInterstitialListener.onAdShown();
        }

    }

    @Override
    public void onApplicationExit(String arg0) {
        if (LOGS_ENABLED) {
            Log.d("vserv", "Flurry interstitial onApplicationExit." + arg0);
        }
        if (mInterstitialListener != null) {
            mInterstitialListener.onLeaveApplication();
        }

    }

    @Override
    public void onRenderFailed(String arg0) {
        if (LOGS_ENABLED) {
            Log.d("vserv", "Flurry interstitial onRenderFailed." + arg0);
        }

    }

    @Override
    public void onRendered(String arg0) {
        if (LOGS_ENABLED) {
            Log.d("vserv", "Flurry interstitial onRendered." + arg0);
        }
//		if (mInterstitialListener != null) {
//			mInterstitialListener.onAdShown();
//		}

    }

    @Override
    public void onVideoCompleted(String arg0) {
        if (LOGS_ENABLED) {
            Log.d("vserv", "Flurry interstitial onVideoCompleted." + arg0);
        }

    }

    @Override
    public boolean shouldDisplayAd(String arg0, FlurryAdType arg1) {
        if (LOGS_ENABLED) {
            Log.d("vserv", "Flurry interstitial shouldDisplayAd." + arg0);
        }
        return true;
    }

    @Override
    public void spaceDidFailToReceiveAd(String arg0) {
        // if (LOGS_ENABLED) {
        Log.d("vserv", "Flurry interstitial spaceDidFailToReceiveAd." + arg0);
        // }
        if (mInterstitialListener != null) {
            mInterstitialListener.onAdFailed(0);
        }

    }

    @Override
    public void spaceDidReceiveAd(String arg0) {
        if (LOGS_ENABLED) {
            Log.d("vserv", "Flurry interstitial ad spaceDidReceiveAd." + arg0);
        }
        if (mInterstitialListener != null) {
            mInterstitialListener.onAdLoaded();
        }

    }

    public void onPause() {

    }

    public void onResume() {

    }

}