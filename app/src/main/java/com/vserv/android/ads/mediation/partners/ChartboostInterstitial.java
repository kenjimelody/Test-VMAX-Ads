package com.vserv.android.ads.mediation.partners;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.chartboost.sdk.Chartboost;
import com.chartboost.sdk.ChartboostDelegate;
import com.chartboost.sdk.Libraries.CBLogging.Level;
import com.chartboost.sdk.Model.CBError.CBClickError;
import com.chartboost.sdk.Model.CBError.CBImpressionError;
import com.vserv.android.ads.api.VservAdView;
import com.vserv.android.ads.reward.RewardVideo;
import com.vserv.android.ads.reward.RewardVideoDelegate;

import java.util.*;

/*
 * Tested with Chartboost SDK 5.5.3.
 */
public class ChartboostInterstitial extends VservCustomAd {
	/*
	 * These keys are intended for Vserv internal use. Do not modify.
	 */
	public static final String APP_ID_KEY = "appid";
	public static final String APP_SIGNATURE_KEY = "appsignature";
	public static final String LOCATION_KEY = "location";
	public static final String LOCATION_DEFAULT = "Default";
	public static final String AD_TYPE = "adtype";
	private String appId;
	private String appSignature;
	private String location;
	private String adtype;

	private VservCustomAdListener mInterstitialListener;
	private Context context;
	public boolean LOGS_ENABLED = true;
	private static final String TAG = "vserv";
	private RewardVideoDelegate rewardedVideoDelegate;
	private RewardVideo rewardedVideoAd;
	private long reward = 0;

	/*
	 * Abstract methods from CustomEventInterstitial
	 */
	@Override
	public void loadAd(final Context context,
			final VservCustomAdListener customEventListener,
			final Map<String, Object> localExtras,
			final Map<String, Object> serverExtras) {

		try {
			location = LOCATION_DEFAULT;
			this.context = context;
			// if (LOGS_ENABLED) {
			// Log.i("vserv", "loadchartboostInterstitial:: ");
			// }
			mInterstitialListener = customEventListener;
			if (!(context instanceof Activity)) {
				Log.i("vserv", "not context instanceof Activity:: ");
				mInterstitialListener.onAdFailed(0);
				return;
			} else {
				Log.i("vserv", "context instanceof Activity:: ");
			}

			Activity activity = (Activity) context;

			if (extrasAreValid(serverExtras)) {
				setAppId(serverExtras.get(APP_ID_KEY).toString());
				setAppSignature(serverExtras.get(APP_SIGNATURE_KEY).toString());
				adtype = serverExtras.get(AD_TYPE).toString();
				setLocation(serverExtras.containsKey(LOCATION_KEY) ? serverExtras
						.get(LOCATION_KEY).toString() : LOCATION_DEFAULT);
			} else {
				mInterstitialListener.onAdFailed(0);
				return;
			}
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

			Chartboost.startWithAppId(activity, appId, appSignature);
			Chartboost.setLoggingLevel(Level.ALL);
			Chartboost.setDelegate(delegate);
			Chartboost.onCreate(activity);
			Chartboost.onStart(activity);
			Chartboost.setAutoCacheAds(false);
			Chartboost.setShouldRequestInterstitialsInFirstSession(true);

			if (adtype.equalsIgnoreCase("more apps")) {
				Chartboost.cacheMoreApps(location);
			} else if (adtype.equalsIgnoreCase("Rewarded video")) {
				Chartboost.cacheRewardedVideo(location);
			} else {
				Chartboost.cacheInterstitial(location);
			}
		} catch (Exception e) {
			mInterstitialListener.onAdFailed(0);
		}
	}

	@Override
	public void showAd() {
		// if (LOGS_ENABLED) {
		// Log.d("vserv", "Showing Chartboost interstitial ad.");
		// }
		try {
			if (adtype.equalsIgnoreCase("more apps")) {
				Chartboost.showMoreApps(location);
			} else if (adtype.equalsIgnoreCase("Rewarded video")) {
				Chartboost.showRewardedVideo(location);

			} else {
				Chartboost.showInterstitial(location);
			}

			if (Chartboost.onBackPressed()) {
				Log.d("vserv", "Chartboost.onBackPressed().");
				return;
			} else {
				Log.d("vserv", "not Chartboost.onBackPressed().");
			}

		} catch (Exception e) {
			mInterstitialListener.onAdFailed(0);
		}

	}

	private ChartboostDelegate delegate = new ChartboostDelegate() {

		@Override
		public boolean shouldRequestInterstitial(String location) {
			if (LOGS_ENABLED) {
				Log.i(TAG, "SHOULD REQUEST INTERSTITIAL '"
						+ (location != null ? location : "null"));
			}
			return true;
		}

		@Override
		public boolean shouldDisplayInterstitial(String location) {
			if (LOGS_ENABLED) {
				Log.i(TAG, "SHOULD DISPLAY INTERSTITIAL '"
						+ (location != null ? location : "null"));
			}
			return true;
		}

		@Override
		public void didCacheInterstitial(String location) {
			if (LOGS_ENABLED) {
				Log.i(TAG, "DID CACHE INTERSTITIAL '"
						+ (location != null ? location : "null"));
			}
			if (mInterstitialListener != null) {
				mInterstitialListener.onAdLoaded();
			}
		}

		@Override
		public void didFailToLoadInterstitial(String location,
				CBImpressionError error) {
			if (LOGS_ENABLED) {
				Log.i(TAG, "DID FAIL TO LOAD INTERSTITIAL '"
						+ (location != null ? location : "null") + " Error: "
						+ error.name());
			}
			if (mInterstitialListener != null) {
				mInterstitialListener.onAdFailed(0);
			}

		}

		@Override
		public void didDismissInterstitial(String location) {
			if (LOGS_ENABLED) {
				Log.i(TAG, "DID DISMISS INTERSTITIAL: "
						+ (location != null ? location : "null"));
			}
			if (mInterstitialListener != null) {
				mInterstitialListener.onAdDismissed();
			}
		}

		@Override
		public void didCloseInterstitial(String location) {
			if (LOGS_ENABLED) {
				Log.i(TAG, "DID CLOSE INTERSTITIAL: "
						+ (location != null ? location : "null"));
			}

		}

		@Override
		public void didClickInterstitial(String location) {
			if (LOGS_ENABLED) {
				Log.i(TAG, "DID CLICK INTERSTITIAL: "
						+ (location != null ? location : "null"));
			}
			if (mInterstitialListener != null) {
				mInterstitialListener.onAdClicked();
			}
		}

		@Override
		public void didDisplayInterstitial(String location) {
			if (LOGS_ENABLED) {
				Log.i(TAG, "DID DISPLAY INTERSTITIAL: "
						+ (location != null ? location : "null"));
			}
			if (mInterstitialListener != null) {
				mInterstitialListener.onAdShown();
			}
		}

		@Override
		public boolean shouldRequestMoreApps(String location) {
			if (LOGS_ENABLED) {
				Log.i(TAG, "SHOULD REQUEST MORE APPS: "
						+ (location != null ? location : "null"));
			}
			return true;
		}

		@Override
		public boolean shouldDisplayMoreApps(String location) {
			if (LOGS_ENABLED) {
				Log.i(TAG, "SHOULD DISPLAY MORE APPS: "
						+ (location != null ? location : "null"));
			}
			return true;
		}

		@Override
		public void didFailToLoadMoreApps(String location,
				CBImpressionError error) {
			if (LOGS_ENABLED) {
				Log.i(TAG, "DID FAIL TO LOAD MOREAPPS "
						+ (location != null ? location : "null") + " Error: "
						+ error.name());
			}
			if (mInterstitialListener != null) {
				mInterstitialListener.onAdFailed(0);
			}

		}

		@Override
		public void didCacheMoreApps(String location) {
			if (LOGS_ENABLED) {
				Log.i(TAG, "DID CACHE MORE APPS: "
						+ (location != null ? location : "null"));
			}
			if (mInterstitialListener != null) {
				mInterstitialListener.onAdLoaded();
			}
		}

		@Override
		public void didDismissMoreApps(String location) {
			if (LOGS_ENABLED) {
				Log.i(TAG, "DID DISMISS MORE APPS "
						+ (location != null ? location : "null"));
			}
			if (mInterstitialListener != null) {
				mInterstitialListener.onAdDismissed();
			}
		}

		@Override
		public void didCloseMoreApps(String location) {
			if (LOGS_ENABLED) {
				Log.i(TAG, "DID CLOSE MORE APPS: "
						+ (location != null ? location : "null"));
			}
		}

		@Override
		public void didClickMoreApps(String location) {
			if (LOGS_ENABLED) {
				Log.i(TAG, "DID CLICK MORE APPS: "
						+ (location != null ? location : "null"));
			}
			if (mInterstitialListener != null) {
				mInterstitialListener.onAdClicked();
			}
		}

		@Override
		public void didDisplayMoreApps(String location) {
			if (LOGS_ENABLED) {
				Log.i(TAG, "DID DISPLAY MORE APPS: "
						+ (location != null ? location : "null"));
			}
			if (mInterstitialListener != null) {
				mInterstitialListener.onAdShown();
			}
		}

		@Override
		public void didFailToRecordClick(String uri, CBClickError error) {
			if (LOGS_ENABLED) {
				Log.i(TAG,
						"DID FAILED TO RECORD CLICK "
								+ (uri != null ? uri : "null") + ", error: "
								+ error.name());
			}

		}

		@Override
		public void willDisplayVideo(String location) {
			Log.i(TAG, String.format("WILL DISPLAY VIDEO '%s", location));
		}

		@Override
		public void didCacheInPlay(String location) {
			if (LOGS_ENABLED) {
				Log.i(TAG, "DID CACHE INPLAY '"
						+ (location != null ? location : "null"));
			}
			if (mInterstitialListener != null) {
				mInterstitialListener.onAdLoaded();
			}
		}

		@Override
		public void didFailToLoadInPlay(String location, CBImpressionError error) {
			if (LOGS_ENABLED) {
				Log.i(TAG, "DID FAILED TO INPLAY '"
						+ (location != null ? location : "null") + ", error: "
						+ error.name());
			}
			if (mInterstitialListener != null) {
				mInterstitialListener.onAdFailed(0);
			}
		}

		public void didCacheRewardedVideo(String location) {
			if (LOGS_ENABLED) {
				Log.i(TAG, "DID CACHE RewardedVideo '"
						+ (location != null ? location : "null"));
			}
			if (mInterstitialListener != null) {
				mInterstitialListener.onAdLoaded();
			}
		};

		public void didClickRewardedVideo(String location) {
			if (LOGS_ENABLED) {
				Log.i(TAG, "DID CLICK RewardedVideo: "
						+ (location != null ? location : "null"));
			}
			if (mInterstitialListener != null) {
				mInterstitialListener.onAdClicked();
			}
		};

		public void didCloseRewardedVideo(String location) {
			if (LOGS_ENABLED) {
				Log.i(TAG, "DID CLOSE RewardedVideo: "
						+ (location != null ? location : "null"));
			}
		};

		public void didCompleteRewardedVideo(String location, int cbreward) {
			if (LOGS_ENABLED) {
				Log.i(TAG, "DID Complete RewardedVideo: "
						+ (location != null ? location : "null") + " Reward: "
						+ reward);
			}
			VservAdView.isVideoComplete=true;
			if (rewardedVideoAd != null) {
				Log.d("vserv", "CB onVideoViewComplete reward: " + reward);
				rewardedVideoAd.getWalletElement().awardVirtualCurrency(reward);
			}
			if (rewardedVideoDelegate != null) {
				Log.d("vserv", "CB onVideoViewComplete delegate: " );
				rewardedVideoDelegate.onRewardVideoCompleted(reward);
			}

		};

		public void didDismissRewardedVideo(String location) {
			if (LOGS_ENABLED) {
				Log.i(TAG, "DID DISMISS RewardedVideo: "
						+ (location != null ? location : "null"));
			}
			if (mInterstitialListener != null) {
				mInterstitialListener.onAdDismissed();
			}
		};

		public void didDisplayRewardedVideo(String location) {
			if (LOGS_ENABLED) {
				Log.i(TAG, "DID DISPLAY RewardedVideo: "
						+ (location != null ? location : "null"));
			}
			if (mInterstitialListener != null) {
				mInterstitialListener.onAdShown();
			}
		};

		public void didFailToLoadRewardedVideo(String location,
				CBImpressionError error) {

			if (LOGS_ENABLED) {
				Log.i(TAG, "DID FAILED TO Load rewarded video '"
						+ (location != null ? location : "null") + ", error: "
						+ (error != null ? error.name() : "null"));
			}
			if (mInterstitialListener != null) {
				mInterstitialListener.onAdFailed(0);
			}
		};

	};

	@Override
	public void onInvalidate() {
		// if (LOGS_ENABLED) {
		// Log.i("vserv", "onInvalidate Interstitial:: ");
		// }
		mInterstitialListener = null;
		if (Chartboost.onBackPressed()) {
			return;
		}

	}

	private void setAppId(String appId) {
		this.appId = appId;
	}

	private void setAppSignature(String appSignature) {
		this.appSignature = appSignature;
	}

	private void setLocation(String location) {
		this.location = location;
	}

	private boolean extrasAreValid(Map<String, Object> serverExtras) {
		return serverExtras.containsKey(APP_ID_KEY)
				&& serverExtras.containsKey(APP_SIGNATURE_KEY)
				&& serverExtras.containsKey(AD_TYPE);
	}

	public void onPause() {
		Chartboost.onPause((Activity) context);
	}

	public void onResume() {
		Chartboost.onResume((Activity) context);
	}

	public void onBackPressed() {
		// If an interstitial is on screen, close it. Otherwise continue as
		// normal.
		if (Chartboost.onBackPressed())
			return;

	}

}
