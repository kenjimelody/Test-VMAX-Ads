package com.vserv.android.ads.mediation.partners;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import android.content.Context;
import android.util.Log;

import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.AdSettings;
import com.facebook.ads.InterstitialAd;
import com.facebook.ads.InterstitialAdListener;

/*
 * Tested with facebook SDK 4.4.1.
 */

public class FaceBookInterstitial extends VservCustomAd implements
		InterstitialAdListener {
	private static final String PLACEMENT_ID_KEY = "placementid";

	private InterstitialAd mFacebookInterstitial;
	private VservCustomAdListener mInterstitialListener;
	public boolean LOGS_ENABLED = true;

	/**
	 * CustomEventInterstitial implementation
	 */

	@Override
	public void loadAd(final Context context,
			final VservCustomAdListener vservCustomAdListener,
			final Map<String, Object> localExtras,
			final Map<String, Object> serverExtras) {
		try {
			// if (LOGS_ENABLED) {
			// Log.d("vserv", "Facebook loadad inter.");
			// }

			mInterstitialListener = vservCustomAdListener;

			final String placementId;
			if (extrasAreValid(serverExtras)) {
				placementId = serverExtras.get(PLACEMENT_ID_KEY).toString();
			} else {
				mInterstitialListener.onAdFailed(0);
				return;
			}
			// AdSettings.addTestDevice("d81a16f98e1dc937a2ef0ec4a77eff88");

			mFacebookInterstitial = new InterstitialAd(context, placementId);

			mFacebookInterstitial.setAdListener(this);

			mFacebookInterstitial.loadAd();
		} catch (Exception e) {
			if (mInterstitialListener != null) {
				mInterstitialListener.onAdFailed(0);
			}
			e.printStackTrace();
			return;
		}
	}

	@Override
	public void showAd() {
		try {
			// if (LOGS_ENABLED) {
			// Log.d("vserv", "Facebook showad inter.");
			// }
			if (mFacebookInterstitial != null
					&& mFacebookInterstitial.isAdLoaded()) {

				mFacebookInterstitial.show();
			} else {
				if (LOGS_ENABLED) {
					Log.d("vserv",
							"Tried to show a Facebook interstitial ad before it finished loading. Please try again.");
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
			if (mFacebookInterstitial != null) {
				mFacebookInterstitial.setAdListener(null);
				mFacebookInterstitial.destroy();
				mFacebookInterstitial = null;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * InterstitialAdListener implementation
	 */

	@Override
	public void onAdLoaded(final Ad ad) {
		if (LOGS_ENABLED) {
			Log.d("vserv", "Facebook interstitial ad loaded successfully.");

		}
		mInterstitialListener.onAdLoaded();
	}

	@Override
	public void onError(final Ad ad, final AdError error) {
		if (LOGS_ENABLED) {
			Log.d("vserv", "Facebook interstitial ad failed to load. error: "
					+ error.getErrorCode());
		}
		if (error == AdError.NO_FILL) {
			mInterstitialListener.onAdFailed(1); // 1 No fill
		} else if (error == AdError.INTERNAL_ERROR) {
			mInterstitialListener.onAdFailed(2); // 2 Internal Error
		} else {
			mInterstitialListener.onAdFailed(0);
		}
	}

	@Override
	public void onInterstitialDisplayed(final Ad ad) {
		if (LOGS_ENABLED) {
			Log.d("vserv", "Showing Facebook interstitial ad.");
		}
		mInterstitialListener.onAdShown();
	}

	@Override
	public void onAdClicked(final Ad ad) {
		if (LOGS_ENABLED) {
			Log.d("vserv", "Facebook interstitial ad clicked.");
		}
		mInterstitialListener.onAdClicked();
	}

	@Override
	public void onInterstitialDismissed(final Ad ad) {
		if (LOGS_ENABLED) {
			Log.d("vserv", "Facebook interstitial ad dismissed.");
		}
		mInterstitialListener.onAdDismissed();
	}

	private boolean extrasAreValid(final Map<String, Object> serverExtras) {
		final String placementId = serverExtras.get(PLACEMENT_ID_KEY)
				.toString();
		return (placementId != null && placementId.length() > 0);
	}

	public void onPause() {

	}

	public void onResume() {

	}
}
