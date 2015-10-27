package com.vserv.android.ads.mediation.partners;

import android.content.Context;
import android.location.Location;
import android.util.Log;

import com.millennialmedia.android.MMAd;
import com.millennialmedia.android.MMAdView;
import com.millennialmedia.android.MMException;
import com.millennialmedia.android.MMRequest;
import com.millennialmedia.android.MMSDK;
import com.millennialmedia.android.RequestListener;

import java.util.Map;

/**
 * Compatible with version 5.3.0 of the Millennial Media SDK.
 */

public class MillennialMediaBanner extends VservCustomAd {
	private MMAdView mMillennialAdView;
	private VservCustomAdListener mBannerListener;
	public static final String APID_KEY = "appid";
	public static final String AD_WIDTH_KEY = "adWidth";
	public static final String AD_HEIGHT_KEY = "adHeight";
	public boolean LOGS_ENABLED = false;

	@Override
	public void loadAd(Context context, VservCustomAdListener customListener,
			Map<String, Object> localExtras, Map<String, Object> serverExtras) {
		try {
			if (LOGS_ENABLED) {
				Log.d("vserv", "Inside MillennialMediaBanner loadAd ");
			}
			mBannerListener = customListener;

			final String apid;
			final int width;
			final int height;
			if (extrasAreValid(serverExtras)) {
				apid = serverExtras.get(APID_KEY).toString();
				// width = Integer.parseInt(serverExtras.get(AD_WIDTH_KEY));
				// height = Integer.parseInt(serverExtras.get(AD_HEIGHT_KEY));
			} else {
				mBannerListener.onAdFailed(0);
				return;
			}

			MMSDK.initialize(context);

			mMillennialAdView = new MMAdView(context);
			mMillennialAdView
					.setListener(new MillennialBannerRequestListener());

			mMillennialAdView.setApid(apid);
			// mMillennialAdView.setWidth(width);
			// mMillennialAdView.setHeight(height);
			if (localExtras.containsKey("location")) {
				final Location location = (Location) localExtras
						.get("location");
				if (location != null) {
					MMRequest.setUserLocation(location);
				}
			}

			mMillennialAdView.setMMRequest(new MMRequest());
			mMillennialAdView.setId(MMSDK.getDefaultAdId());
			mMillennialAdView.getAd();
		} catch (Exception e) {
		}
	}

	@Override
	public void showAd() {

	}

	@Override
	public void onInvalidate() {
		if (LOGS_ENABLED) {
			Log.d("vserv", "Inside MillennialMediaBanner onInvalidate ");
		}
		if (mMillennialAdView != null) {
			mMillennialAdView.setListener(null);
		}

	}

	private boolean extrasAreValid(final Map<String, Object> serverExtras) {

		return serverExtras.containsKey(APID_KEY);
	}

	class MillennialBannerRequestListener implements RequestListener {
		@Override
		public void MMAdOverlayLaunched(final MMAd mmAd) {
			Log.d("vserv", "Millennial banner ad Launched.");

		}

		@Override
		public void MMAdOverlayClosed(final MMAd mmAd) {
			Log.d("vserv", "Millennial banner ad closed.");
		}

		@Override
		public void MMAdRequestIsCaching(final MMAd mmAd) {
			Log.d("vserv", "Millennial banner MMAdRequestIsCaching.");
		}

		@Override
		public void requestCompleted(final MMAd mmAd) {
			Log.d("vserv",
					"Millennial banner ad loaded successfully. Showing ad...");
			mBannerListener.onAdLoaded(mMillennialAdView);
		}

		@Override
		public void requestFailed(final MMAd mmAd, final MMException e) {
			if (e != null) {
				Log.d("vserv",
						"Millennial banner ad failed to load." + e.getMessage());
			} else {
				Log.d("vserv", "Millennial banner ad failed to load.");
			}
			mBannerListener.onAdFailed(0);
		}

		@Override
		public void onSingleTap(final MMAd mmAd) {
			Log.d("vserv", "Millennial banner onSingleTap.");
			mBannerListener.onAdClicked();
		}

	}
	
	public void onPause() {

	}

	public void onResume() {

	}


}