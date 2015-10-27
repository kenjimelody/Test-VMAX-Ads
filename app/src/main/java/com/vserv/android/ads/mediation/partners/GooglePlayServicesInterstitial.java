package com.vserv.android.ads.mediation.partners;

import android.content.Context;
import android.location.Location;
import android.util.Log;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdRequest.Builder;
import com.google.android.gms.ads.InterstitialAd;

import java.util.Date;
import java.util.Map;

/*
 * Compatible with latest version of the Google Play Services SDK.
 */

// Note: AdMob ads will now use this class as Google has deprecated the AdMob SDK.

public class GooglePlayServicesInterstitial extends VservCustomAd {
	/*
	 * These keys are intended for vserv internal use. Do not modify.
	 */
	private static final String AD_UNIT_ID_KEY = "adunitid";
	// private static final String LOCATION_KEY = "location";

	private VservCustomAdListener mInterstitialListener;
	private InterstitialAd mGoogleInterstitialAd;
	Context context;
	public boolean LOGS_ENABLED = true;

	@Override
	public void loadAd(final Context context,
			final VservCustomAdListener customEventListener,
			final Map<String, Object> localExtras,
			final Map<String, Object> serverExtras) {
		try {
			this.context = context;
			// if (LOGS_ENABLED) {
			// Log.i("vserv", "loadInterstitial:: ");
			// }
			mInterstitialListener = customEventListener;

			final String adUnitId;

			if (serverExtras != null) {
				if (extrasAreValid(serverExtras)) {
					adUnitId = serverExtras.get(AD_UNIT_ID_KEY).toString();
					// if (LOGS_ENABLED) {
					// Log.i("vserv", "loadInterstitial adUnitId:: "
					// + adUnitId);
					// }
				} else {
					mInterstitialListener.onAdFailed(0);
					return;
				}
			} else {
				mInterstitialListener.onAdFailed(0);
				return;
			}

			mGoogleInterstitialAd = new InterstitialAd(context);
			mGoogleInterstitialAd.setAdListener(new InterstitialAdListener());
			mGoogleInterstitialAd.setAdUnitId(adUnitId);

			final Builder adbuilder = new Builder();
			if (localExtras != null) {
				if (localExtras.containsKey("birthday")) {
					if (LOGS_ENABLED) {
						Log.i("vserv",
								"setBirthday : "
										+ (Date) localExtras.get("birthday"));
					}
					adbuilder.setBirthday((Date) localExtras.get("birthday"));
					// adbuilder.setBirthday(new GregorianCalendar(1989, 4,
					// 4).getTime());

				}
				if (localExtras.containsKey("gender")) {
					if (LOGS_ENABLED) {
						Log.i("vserv", "Gender : "
								+ localExtras.get("gender").toString());
					}
					if (localExtras.get("gender").toString()
							.equalsIgnoreCase("male")) {
						adbuilder.setGender(AdRequest.GENDER_MALE);

					} else if (localExtras.get("gender").toString()
							.equalsIgnoreCase("female")) {
						adbuilder.setGender(AdRequest.GENDER_FEMALE);

					} else if (localExtras.get("gender").toString()
							.equalsIgnoreCase("unknown")) {
						adbuilder.setGender(AdRequest.GENDER_UNKNOWN);

					}
				}
				if (localExtras.containsKey("location")) {
					if (LOGS_ENABLED) {
						Log.i("vserv",
								"location : "
										+ (Location) localExtras
												.get("location"));
					}
//					adbuilder.setLocation((Location) localExtras
//							.get("location"));

				}
			}
			final AdRequest adRequest = adbuilder.build();

			mGoogleInterstitialAd.loadAd(adRequest);
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
			// Log.i("vserv", "show Interstitial:: ");
			// }
			if (mGoogleInterstitialAd != null
					&& mGoogleInterstitialAd.isLoaded()) {

				mGoogleInterstitialAd.show();
			}
		} catch (Exception e) {
			e.printStackTrace();
			mInterstitialListener.onAdFailed(0);
		}
	}

	@Override
	public void onInvalidate() {
		try {
			// if (LOGS_ENABLED) {
			// Log.i("vserv", "onInvalidate Interstitial:: ");
			// }
			if (mGoogleInterstitialAd != null) {
				mGoogleInterstitialAd.setAdListener(null);
			}
			mGoogleInterstitialAd = null;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private boolean extrasAreValid(Map<String, Object> serverExtras) {
		return serverExtras.containsKey(AD_UNIT_ID_KEY);
	}

	private class InterstitialAdListener extends AdListener {
		/*
		 * Google Play Services AdListener implementation
		 */
		@Override
		public void onAdClosed() {
			if (LOGS_ENABLED) {
				Log.d("vserv",
						"Google Play Services interstitial ad dismissed.");
			}
			if (mInterstitialListener != null) {
				mInterstitialListener.onAdDismissed();
			}
		}

		@Override
		public void onAdFailedToLoad(int errorCode) {
			if (LOGS_ENABLED) {
				Log.d("vserv",
						"Google Play Services interstitial ad failed to load."
								+ errorCode);
			}

			if (mInterstitialListener != null) {
				mInterstitialListener.onAdFailed(0);
			}
			String message = String.format("onAdFailedToLoad (%s)",
					getErrorReason(errorCode));
			Log.d("vserv", message);
			// Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
		}

		@Override
		public void onAdLeftApplication() {
			if (LOGS_ENABLED) {
				Log.d("vserv", "Google Play Services interstitial ad clicked.");
			}
			if (mInterstitialListener != null) {
				mInterstitialListener.onAdClicked();
			}
			if (mInterstitialListener != null) {
				mInterstitialListener.onLeaveApplication();
			}
		}

		@Override
		public void onAdLoaded() {
			if (LOGS_ENABLED) {
				Log.d("vserv",
						"Google Play Services interstitial ad loaded successfully.");

			}
			if (mInterstitialListener != null) {
				mInterstitialListener.onAdLoaded();
			}
		}

		@Override
		public void onAdOpened() {
			if (LOGS_ENABLED) {
				Log.d("vserv", "Showing Google Play Services interstitial ad.");
			}
			if (mInterstitialListener != null) {
				mInterstitialListener.onAdShown();
			}
		}
	}

	private String getErrorReason(int errorCode) {
		String errorReason = "";
		switch (errorCode) {
		case AdRequest.ERROR_CODE_INTERNAL_ERROR:
			errorReason = "Internal error";
			break;
		case AdRequest.ERROR_CODE_INVALID_REQUEST:
			errorReason = "Invalid request";
			break;
		case AdRequest.ERROR_CODE_NETWORK_ERROR:
			errorReason = "Network Error";
			break;
		case AdRequest.ERROR_CODE_NO_FILL:
			errorReason = "No fill";
			break;
		}
		return errorReason;
	}

	public void onPause() {

	}

	public void onResume() {

	}

}