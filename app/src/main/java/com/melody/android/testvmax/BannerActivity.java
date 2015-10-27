package com.melody.android.testvmax;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.vserv.android.ads.api.VservAdView;
import com.vserv.android.ads.common.VservAdListener;
import com.vserv.android.ads.nativeads.NativeAd;
import com.vserv.android.ads.nativeads.NativeAdConstants;
import com.vserv.android.ads.nativeview.AdType;
import com.vserv.android.ads.nativeview.NativeAdHelper;
import com.vserv.android.ads.reward.Offerwall;
import com.vserv.android.ads.reward.RewardVideo;
import com.vserv.android.ads.reward.RewardVideoDelegate;
import com.vserv.android.ads.wallet.Wallet;
import com.vserv.android.ads.wallet.WalletElement;
import com.vserv.android.ads.wallet.WalletListener;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Thanisak Piyasaksiri on 10/27/15 AD.
 */
public class BannerActivity extends AppCompatActivity {


    private int ads_type = 0;
    private String zone_id = "";

    private Bundle extras;
    private VservAdView adViewBanner;
    private VservAdView adNativeBanner;
    private VservAdView interstitialAdView;
    private ImageView ivNative;

    private String adFormat;

    private Wallet wallet;
    private WalletElement walletElementGold;
    private RewardVideo rewardedVideo;
    private Offerwall offerwall;

    public VservAdListener mAdListener = new VservAdListener() {

        @Override
        public void didInteractWithAd(VservAdView adView) {
            Toast.makeText(BannerActivity.this, "didInteractWithAd", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void adViewDidLoadAd(VservAdView adView) {

            Toast.makeText(BannerActivity.this, "adViewDidLoadAd", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void willPresentOverlay(VservAdView adView) {

            Toast.makeText(BannerActivity.this, "willPresentOverlay", Toast.LENGTH_SHORT).show();
        }

        @Override
        public VservAdView didFailedToLoadAd(String arg0) {
            Toast.makeText(BannerActivity.this, "didFailedToLoadAd", Toast.LENGTH_SHORT).show();
            return null;
        }

        @Override
        public VservAdView didFailedToCacheAd(String Error) {
            Toast.makeText(BannerActivity.this, "didFailedToCacheAd", Toast.LENGTH_SHORT).show();
            return null;
        }

        @Override
        public void willLeaveApp(VservAdView adView) {
            Toast.makeText(BannerActivity.this, "willLeaveApp", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void adViewDidCacheAd(VservAdView adView) {

            if (adView != null) {

                if (adView.getUxType() == VservAdView.UX_NATIVE) {

                    NativeAd nativeAd = adView.getNativeAd();

                    if (adFormat.equals("In-feed")) {

                        adViewBanner.setVisibility(View.GONE);
                        ((LinearLayout) findViewById(R.id.ll_native_infeed)).setVisibility(View.VISIBLE);
                        ((LinearLayout) findViewById(R.id.ll_native_content)).setVisibility(View.GONE);

                        String title = nativeAd.getElementValue(NativeAdConstants.NativeAd_TITLE_TEXT);
                        String desc = nativeAd.getElementValue(NativeAdConstants.NativeAd_DESCRIPTION);
                        String cta = nativeAd.getElementValue(NativeAdConstants.NativeAd_CALL_TO_ACTION);
                        String price = nativeAd.getElementValue(NativeAdConstants.NativeAd_PRICE);
                        String ivURL = nativeAd.getElementValue(NativeAdConstants.NativeAd_ICON);

                        new ImageLoadTask(ivURL, ivNative).execute();
                        ((TextView) findViewById(R.id.titleNative)).setText(title);
                        ((TextView) findViewById(R.id.descNative)).setText(desc);
                        ((TextView) findViewById(R.id.ctaNative)).setText(cta);
                        ((TextView) findViewById(R.id.priceNative)).setText(price);

                        List<View> list = new ArrayList<View>();
                        list.add(((TextView) findViewById(R.id.ctaNative)));
                        list.add(((TextView) findViewById(R.id.titleNative)));
                        list.add(((TextView) findViewById(R.id.descNative)));
                        list.add(((TextView) findViewById(R.id.priceNative)));
                        nativeAd.registerViewForInteraction(adView, ((LinearLayout) findViewById(R.id.ll_native_infeed)), ((LinearLayout) findViewById(R.id.ll_native_infeed)), list);

                    } else if (adFormat.equals("Content Stream")) {

                        adViewBanner.setVisibility(View.GONE);
                        ((LinearLayout) findViewById(R.id.ll_native_infeed)).setVisibility(View.GONE);
                        ((LinearLayout) findViewById(R.id.ll_native_content)).setVisibility(View.VISIBLE);

                        String largeImage = nativeAd.getElementValue(NativeAdConstants.NativeAd_LARGE_IMAGE);
                        NativeAdHelper nativeAdHelper = new NativeAdHelper(BannerActivity.this, nativeAd, adView, AdType.CONTENT_STREAM);
                        nativeAdHelper.attachNativeAd(((LinearLayout) findViewById(R.id.ll_native_content)));
                        nativeAd.registerViewForInteraction(adView, ((LinearLayout) findViewById(R.id.ll_native_content)), ((LinearLayout) findViewById(R.id.ll_native_content)), null);
                    } else {

                        adViewBanner.setVisibility(View.GONE);
                        ((LinearLayout) findViewById(R.id.ll_native_infeed)).setVisibility(View.GONE);
                        ((LinearLayout) findViewById(R.id.ll_native_content)).setVisibility(View.GONE);
                    }
                }
            }
        }
    };

    public WalletListener vservWalletListener = new WalletListener() {

        @Override
        public void onUpdateVirtualCurrency(long vc) {

            Toast.makeText(BannerActivity.this, "onUpdateVirtualCurrency " + vc, Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onUpdateFailedVirtualCurrency(String errormsg) {

            Toast.makeText(BannerActivity.this, "onUpdateFailedVirtualCurrency : " + errormsg, Toast.LENGTH_SHORT).show();
        }
    };

    public RewardVideoDelegate rewardedVideoDelegate = new RewardVideoDelegate() {

        @Override
        public void onRewardVideoCompleted(long reward) {
            Log.i("vserv", "developer onRewardedVideoCompleted: " + reward);
        }

        @Override
        public void onRewardVideoInterrupted(String errorMessage) {
            Log.i("vserv", "developer onRewardedVideoInterrupted: " + errorMessage);
        }

        @Override
        public void onRewardVideoPlaybackError(String errorMessage) {
            Log.i("vserv", "developer onRewardVideoPlaybackError: " + errorMessage);
        }

        @Override
        public boolean handleImpressionCapPopup() {
            Log.i("vserv", "developer handleImpressionCapPopup: ");
            return true;
        }

        @Override
        public boolean handleShowPrePopup(String message, String title) {
            Log.i("vserv", "developer handleShowPrePopup: ");
            return true;

        }

        @Override
        public boolean handleShowPostPopup(String message, String title) {
            return true;
        }

        @Override
        public boolean handleAdInterruptedPopup(String message, String title) {
            return true;
        }

        @Override
        public boolean handleNoFillPopup(String message, String title) {
            return true;
        }
    };

    @Override
    public void onBackPressed() {

        if(adViewBanner != null)
            adViewBanner.onBackPressed();

        if(interstitialAdView != null)
            interstitialAdView.onBackPressed();

        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_banner);

        extras = getIntent().getExtras();
        ads_type = extras.getInt("type");
        zone_id = extras.getString("zoneid");

        if(extras.containsKey("format"))
            adFormat = extras.getString("format", "In-feed");

        adViewBanner = (VservAdView) findViewById(R.id.adViewBanner);
        ivNative = (ImageView) findViewById(R.id.ivNative);

        ((TextView) findViewById(R.id.label_zoneid)).setText("Zone ID: " + zone_id);
        ((TextView) findViewById(R.id.label_adstype)).setText("ADS Type: " + ads_type);
        ((TextView) findViewById(R.id.label_adsformat)).setText("ADS Format: " + adFormat);

        if(ads_type == VservAdView.UX_BANNER) {

            adViewBanner.setAdListener(mAdListener);
            adViewBanner.setZoneId(zone_id);
            adViewBanner.setUxType(VservAdView.UX_BANNER);
            adViewBanner.setTimeOut(20);
            adViewBanner.setRefresh(false);
            adViewBanner.setTestDevices(zone_id);
            adViewBanner.loadAd();

        } else if(ads_type == VservAdView.UX_NATIVE) {

            adViewBanner.setVisibility(View.GONE);

            adNativeBanner = new VservAdView(this, zone_id, VservAdView.UX_NATIVE);
            adNativeBanner.setAdListener(mAdListener);
            adNativeBanner.setTestDevices(zone_id);
            adNativeBanner.setRefresh(false);
            adNativeBanner.cacheAd();

        } else if(ads_type == VservAdView.UX_INTERSTITIAL) {

            adViewBanner.setVisibility(View.GONE);

            wallet = Wallet.getInstance(this);
            walletElementGold = wallet.addWalletElement("Gold Coins");
            walletElementGold.setWalletListener(vservWalletListener);

            rewardedVideo = new RewardVideo(this, walletElementGold);
            offerwall = new Offerwall(this, walletElementGold);

            rewardedVideo.setDelegate(rewardedVideoDelegate);

            if (adFormat.equals("Offerwall") || adFormat.equals("Rewarded Video") || adFormat.equals("Interstitial")) {

                interstitialAdView = new VservAdView(this, zone_id, VservAdView.UX_INTERSTITIAL);

                if (adFormat.equalsIgnoreCase("Offerwall"))
                    interstitialAdView.initOfferWall(offerwall);
                else if (adFormat.equalsIgnoreCase("Rewarded Video"))
                    interstitialAdView.initRewardedVideo(rewardedVideo);

                interstitialAdView.setAdListener(mAdListener);
                interstitialAdView.setZoneId(zone_id);
                interstitialAdView.setUxType(VservAdView.UX_INTERSTITIAL);
                interstitialAdView.setTimeOut(20);
                interstitialAdView.setTestDevices(zone_id);
                interstitialAdView.loadAd();
            }
        }
    }

    @Override
    protected void onPause() {

        super.onPause();

        if(adViewBanner != null)
            adViewBanner.onPause();

        if(interstitialAdView != null)
            interstitialAdView.onPause();
    }

    @Override
    protected void onResume() {

        super.onResume();

        if(adViewBanner != null)
            adViewBanner.onResume();

        if(interstitialAdView != null)
            interstitialAdView.onResume();
    }

    private class ImageLoadTask extends AsyncTask<Void, Void, Bitmap> {

        private String url;
        private ImageView imageView;

        public ImageLoadTask(String url, ImageView imageView) {
            this.url = url;
            this.imageView = imageView;
        }

        @Override
        protected Bitmap doInBackground(Void... params) {
            try {
                URL urlConnection = new URL(url);
                HttpURLConnection connection = (HttpURLConnection) urlConnection.openConnection();
                connection.setDoInput(true);
                connection.connect();
                InputStream input = connection.getInputStream();
                Bitmap myBitmap = BitmapFactory.decodeStream(input);
                return myBitmap;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            super.onPostExecute(result);
            imageView.setImageBitmap(result);
        }
    }
}
