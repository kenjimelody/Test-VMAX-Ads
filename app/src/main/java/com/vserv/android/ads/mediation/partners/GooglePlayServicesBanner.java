package com.vserv.android.ads.mediation.partners;

import android.content.Context;
import android.location.Location;
import android.util.Log;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.AdRequest.Builder;

import java.util.Date;
import java.util.Map;

/*
 * Compatible with version latest version of the Google Play Services SDK.
 */

// Note: AdMob ads will now use this class as Google has deprecated the AdMob SDK.

public class GooglePlayServicesBanner extends VservCustomAd {
	/*
	 * These keys are intended for vserv internal use. Do not modify.
	 */
	private static final String AD_UNIT_ID_KEY = "adunitid";
	// private static final String AD_WIDTH_KEY = "adWidth";
	// private static final String AD_HEIGHT_KEY = "adHeight";
	// private static final String LOCATION_KEY = "location";

	private VservCustomAdListener mBannerListener;
	private AdView mGoogleAdView;
	public boolean LOGS_ENABLED = true;

	@Override
	public void loadAd(final Context context,
			final VservCustomAdListener customEventListener,
			final Map<String, Object> localExtras,
			final Map<String, Object> serverExtras) {
		try {
			// if (LOGS_ENABLED) {
			// Log.d("vserv", "Inside loadBanner ");
			// }
			mBannerListener = customEventListener;

			final String adUnitId;
			// final int adWidth;
			// final int adHeight;

			if (extrasAreValid(serverExtras)) {
				adUnitId = serverExtras.get(AD_UNIT_ID_KEY).toString();
				// if (LOGS_ENABLED) {
				// Log.d("vserv", "Inside loadBanner adUnitId " + adUnitId);
				// }
				// adWidth = Integer.parseInt(serverExtras.get(AD_WIDTH_KEY));
				// adHeight = Integer.parseInt(serverExtras.get(AD_HEIGHT_KEY));
			} else {
				mBannerListener.onAdFailed(0);
				return;
			}

			mGoogleAdView = new AdView(context);
			mGoogleAdView.setAdListener(new AdViewListener());
			mGoogleAdView.setAdUnitId(adUnitId);

			// final AdSize adSize = calculateAdSize(adWidth, adHeight);
			// if (adSize == null) {
			// mBannerListener.onBannerFailed(0);
			// return;
			// }

			mGoogleAdView.setAdSize(AdSize.SMART_BANNER);

			// final AdRequest adRequest = new AdRequest.Builder().build();
			final Builder adbuilder = new Builder();

			if (localExtras != null) {
				if (localExtras.containsKey("birthday")) {
					if (LOGS_ENABLED) {
						Log.i("vserv",
								"setBirthday : "
										+ (Date) localExtras.get("birthday"));
					}
					adbuilder.setBirthday((Date) localExtras.get("birthday"));
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

			mGoogleAdView.loadAd(adRequest);
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
	}

	@Override
	public void onInvalidate() {
		// Views.removeFromParent(mGoogleAdView);
		try {
			if (mGoogleAdView != null) {
				if (LOGS_ENABLED) {
					Log.i("vserv", "onInvalidate:: ");
				}
				mGoogleAdView.setAdListener(null);
				mGoogleAdView.destroy();
				mGoogleAdView = null;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private boolean extrasAreValid(Map<String, Object> serverExtras) {
		// try {
		// Integer.parseInt(serverExtras.get(AD_WIDTH_KEY));
		// Integer.parseInt(serverExtras.get(AD_HEIGHT_KEY));
		// } catch (NumberFormatException e) {
		// return false;
		// }

		return serverExtras.containsKey(AD_UNIT_ID_KEY);
	}

	// private AdSize calculateAdSize(int width, int height) {
	// // Use the smallest AdSize that will properly contain the adView
	// if (width <= BANNER.getWidth() && height <= BANNER.getHeight()) {
	// return BANNER;
	// } else if (width <= MEDIUM_RECTANGLE.getWidth()
	// && height <= MEDIUM_RECTANGLE.getHeight()) {
	// return MEDIUM_RECTANGLE;
	// } else if (width <= FULL_BANNER.getWidth()
	// && height <= FULL_BANNER.getHeight()) {
	// return FULL_BANNER;
	// } else if (width <= LEADERBOARD.getWidth()
	// && height <= LEADERBOARD.getHeight()) {
	// return LEADERBOARD;
	// } else {
	// return null;
	// }
	// }

	private class AdViewListener extends AdListener {
		/*
		 * Google Play Services AdListener implementation
		 */
		@Override
		public void onAdClosed() {

		}

		@Override
		public void onAdFailedToLoad(int errorCode) {
			if (LOGS_ENABLED) {
				Log.d("vserv", "Google Play Services banner ad failed to load."
						+ errorCode);
			}

			if (mBannerListener != null) {
				mBannerListener.onAdFailed(0);
			}
		}

		@Override
		public void onAdLeftApplication() {
			if (LOGS_ENABLED) {
				Log.d("vserv", "Google Play Services onAdLeftApplication.");
			}
			if (mBannerListener != null) {
				mBannerListener.onAdClicked();
			}
			if (mBannerListener != null) {
				mBannerListener.onLeaveApplication();
			}
		}

		@Override
		public void onAdLoaded() {
			if (LOGS_ENABLED) {
				Log.d("vserv",
						"Google Play Services banner ad loaded successfully. Showing ad...");
			}

			if (mBannerListener != null) {
				
				mBannerListener.onAdLoaded(mGoogleAdView);
			}
		}

		@Override
		public void onAdOpened() {
			if (LOGS_ENABLED) {
				Log.d("vserv", "Google Play Services banner ad clicked.");
			}
			if (mBannerListener != null) {
				mBannerListener.onAdShown();
			}
		}
	}
	
	public void onPause() {

	}

	public void onResume() {

	}

}