package com.vserv.android.ads.mediation.partners;

import android.content.Context;
import android.location.Location;
import android.util.Log;

import com.millennialmedia.android.MMAd;
import com.millennialmedia.android.MMException;
import com.millennialmedia.android.MMInterstitial;
import com.millennialmedia.android.MMRequest;
import com.millennialmedia.android.MMSDK;
import com.millennialmedia.android.RequestListener;

import java.util.Map;

/**
 * Compatible with version 5.3.0 of the Millennial Media SDK.
 */

public class MillennialMediaInterstitial extends VservCustomAd {
	private MMInterstitial mMillennialInterstitial;
	private VservCustomAdListener mInterstitialListener;
	public static final String APID_KEY = "appid";
	public boolean LOGS_ENABLED = true;

	@Override
	public void loadAd(Context context, VservCustomAdListener customListener,
			Map<String, Object> localExtras, Map<String, Object> serverExtras) {
		try {
			// if (LOGS_ENABLED) {
			// Log.d("vserv", "Inside MillennialInterstitial loadAd ");
			// }
			mInterstitialListener = customListener;

			final String apid;
			if (extrasAreValid(serverExtras)) {
				apid = serverExtras.get(APID_KEY).toString();
			} else {
				mInterstitialListener.onAdFailed(0);
				return;
			}

			MMSDK.initialize(context);
			if (localExtras.containsKey("location")) {
				final Location location = (Location) localExtras
						.get("location");
				if (location != null) {
					MMRequest.setUserLocation(location);
				}
			}
			mMillennialInterstitial = new MMInterstitial(context);
			mMillennialInterstitial
					.setListener(new MillennialInterstitialRequestListener());
			mMillennialInterstitial.setMMRequest(new MMRequest());
			mMillennialInterstitial.setApid(apid);
			mMillennialInterstitial.fetch();
		} catch (Exception e) {
		}
	}

	@Override
	public void showAd() {
		// if (LOGS_ENABLED) {
		// Log.d("vserv", "Inside MillennialInterstitial showAd ");
		// }
		if (mMillennialInterstitial.isAdAvailable()) {
			mMillennialInterstitial.display();
		}

	}

	@Override
	public void onInvalidate() {
		// if (LOGS_ENABLED) {
		// Log.d("vserv", "Inside MillennialInterstitial onInvalidate ");
		// }
		if (mMillennialInterstitial != null) {
			mMillennialInterstitial.setListener(null);
		}

	}

	private boolean extrasAreValid(Map<String, Object> serverExtras) {
		return serverExtras.containsKey(APID_KEY);
	}

	class MillennialInterstitialRequestListener implements RequestListener {
		@Override
		public void MMAdOverlayLaunched(final MMAd mmAd) {
			if (LOGS_ENABLED) {
				Log.d("vserv", "Showing Millennial interstitial ad.");
			}
			mInterstitialListener.onAdShown();
		}

		@Override
		public void MMAdOverlayClosed(final MMAd mmAd) {
			if (LOGS_ENABLED) {
				Log.d("vserv", "Millennial interstitial ad dismissed.");
			}
			mInterstitialListener.onAdDismissed();
		}

		@Override
		public void MMAdRequestIsCaching(final MMAd mmAd) {
			if (LOGS_ENABLED) {
				Log.d("vserv", "Millennial interstitial MMAdRequestIsCaching.");
			}
		}

		@Override
		public void requestCompleted(final MMAd mmAd) {
			if (mMillennialInterstitial.isAdAvailable()) {
				if (LOGS_ENABLED) {
					Log.d("vserv",
							"Millennial interstitial ad loaded successfully.");
				}
				mInterstitialListener.onAdLoaded();
			} else {
				if (LOGS_ENABLED) {
					Log.d("vserv",
							"Millennial interstitial request completed, but no ad was available.");
				}
				mInterstitialListener.onAdFailed(0);
			}
		}

		@Override
		public void requestFailed(final MMAd mmAd, final MMException e) {
			if (mMillennialInterstitial == null || e == null) {
				if (LOGS_ENABLED) {
					Log.d("vserv", "Millennial interstitial ad failed to load.");
				}
				mInterstitialListener.onAdFailed(0);
			} else if (e.getCode() == MMException.CACHE_NOT_EMPTY
					&& mMillennialInterstitial.isAdAvailable()) {
				// requestFailed can be due to an ad already loaded or an ad
				// failed to load.
				if (LOGS_ENABLED) {
					Log.d("vserv",
							"Millennial interstitial loaded successfully from cache."
									+ e.getMessage());
				}
				mInterstitialListener.onAdLoaded();
			} else {
				if (LOGS_ENABLED) {
					Log.d("vserv", "Millennial interstitial ad failed to load."
							+ e.getMessage());
				}
				mInterstitialListener.onAdFailed(0);
			}
		}

		@Override
		public void onSingleTap(final MMAd mmAd) {
			if (LOGS_ENABLED) {
				Log.d("vserv", "Millennial interstitial clicked.");
			}
			mInterstitialListener.onAdClicked();
		}

	}

	public void onPause() {

	}

	public void onResume() {

	}

}