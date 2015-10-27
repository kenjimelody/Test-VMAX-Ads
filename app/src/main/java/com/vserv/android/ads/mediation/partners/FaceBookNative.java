/**
 * Project      : Advert
 * Filename     : FaceBookNative.java
 * Author       : narendrap
 * Comments     :
 * Copyright    : Copyright  2014, VSERV
 */

package com.vserv.android.ads.mediation.partners;

import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.AdListener;
import com.facebook.ads.AdSettings;
import com.facebook.ads.NativeAd;
import com.facebook.ads.NativeAd.Rating;

/**
 * @author narendrap
 */
public class FaceBookNative extends VservCustomAd implements AdListener {

    private NativeAd nativeAd;
    private static final String PLACEMENT_ID_KEY = "placementid";
    private VservCustomNativeAdListener mNativeAdListener;
    public boolean LOGS_ENABLED = true;
    private VservCustomAdListener vservCustomAdListener;


    @Override
    public void loadAd(Context context,
                       VservCustomAdListener vservCustomAdListener,
                       Map<String, Object> localExtras, Map<String, Object> serverExtras) {

        try {
            if (LOGS_ENABLED) {
                Log.d("vserv", "Facebook loadAd .");
            }
            this.vservCustomAdListener = vservCustomAdListener;
            final String placementId;

            if (localExtras != null) {
                if (localExtras.containsKey("nativeListener")) {
                    if (LOGS_ENABLED) {
                        Log.i("Log", "nativeListener in localextras ");
                    }
                    mNativeAdListener = (VservCustomNativeAdListener) localExtras.get("nativeListener");


                }
            }
            if (extrasAreValid(serverExtras)) {
                placementId = serverExtras.get(PLACEMENT_ID_KEY).toString();
            } else {
                mNativeAdListener.onAdFailed("Placement id missing");
                return;
            }

            // AdSettings.addTestDevice("094748db9b2082c78822a0789b90274e"); // nexus 5 for test Ads



            nativeAd = new NativeAd(context, placementId);

            nativeAd.setAdListener(this);
            nativeAd.loadAd();
        } catch (Exception e) {
            if (mNativeAdListener != null) {
                mNativeAdListener.onAdFailed(e.getMessage());
            }
            e.printStackTrace();
            return;
        }
    }


    /* (non-Javadoc)
     * @see com.facebook.ads.AdListener#onAdClicked(com.facebook.ads.Ad)
     */
    @Override
    public void onAdClicked(Ad arg0) {
        Log.i("vserv", "fb onAdClicked");
        if (vservCustomAdListener != null) {

            vservCustomAdListener.onAdClicked();
        }
    }

    /* (non-Javadoc)
     * @see com.facebook.ads.AdListener#onAdLoaded(com.facebook.ads.Ad)
     */
    @Override
    public void onAdLoaded(Ad ad) {

        if (ad != nativeAd) {
            return;
        }
        nativeAd.unregisterView();
        String titleForAd = nativeAd.getAdTitle();
        String coverImageURL = nativeAd.getAdCoverImage().getUrl();
        int coverImageHeight = nativeAd.getAdCoverImage().getHeight();
        int coverImageWidth = nativeAd.getAdCoverImage().getWidth();
        String iconForAd = nativeAd.getAdIcon().getUrl();
        int iconAdHeight = nativeAd.getAdIcon().getHeight();
        int iconAdWidth = nativeAd.getAdIcon().getWidth();
        String socialContextForAd = nativeAd.getAdSocialContext();
        String titleForAdButton = nativeAd.getAdCallToAction();
        String textForAdBody = nativeAd.getAdBody();
        String appRatingForAd = "";
        Double rating = getDoubleRating(nativeAd.getAdStarRating());
        if (rating != null) {
            appRatingForAd = Double.toString(rating);
        }
        if (LOGS_ENABLED) {
            Log.d("vserv", "Title for Ad : " + titleForAd);
            Log.d("vserv", "coverImage URL : " + coverImageURL);
            Log.d("vserv", "socialContextForAd : " + socialContextForAd);
            Log.d("vserv", "titleForAdButton : " + titleForAdButton);
            Log.d("vserv", "textForAdBody : " + textForAdBody);
            Log.d("vserv", "appRatingForAd : " + appRatingForAd);
            Log.d("vserv", "iconForAd : " + iconForAd);
        }

        JSONObject fbJSON = new JSONObject();
        try {
            fbJSON.put("TITLE_TEXT", titleForAd);
            fbJSON.put("SOCIAL_CONTEXT", socialContextForAd);
            fbJSON.put("CALL_TO_ACTION", titleForAdButton);
            fbJSON.put("RATING", appRatingForAd);
            fbJSON.put("DESCRIPTION", textForAdBody);
            fbJSON.put("ICON", iconForAd);
            fbJSON.put("ICON_WIDTH", iconAdWidth);
            fbJSON.put("ICON_HEIGHT", iconAdHeight);
            fbJSON.put("COVER_IMAGE", coverImageURL);
            fbJSON.put("COVER_IMAGE_WIDTH", coverImageWidth);
            fbJSON.put("COVER_IMAGE_HEIGHT", coverImageHeight);

            Object[] objArray = new Object[]{fbJSON};
            mNativeAdListener.onAdLoaded(objArray);

        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }


    }

    /* (non-Javadoc)
     * @see com.facebook.ads.AdListener#onError(com.facebook.ads.Ad, com.facebook.ads.AdError)
     */
    @Override
    public void onError(final Ad ad, final AdError error) {
        if (error != null) {
            if (LOGS_ENABLED) {
                Log.d("vserv", "Facebook native ad failed to load. error: " + error.getErrorCode());
            }
            mNativeAdListener.onAdFailed(error.getErrorMessage());
        } else {
            mNativeAdListener.onAdFailed("No ad in inventory");
        }
    }


    /* (non-Javadoc)
     * @see com.vserv.android.ads.mediation.partners.VservCustomAd#showAd()
     */
    @Override
    public void showAd() {
    }

    /* (non-Javadoc)
     * @see com.vserv.android.ads.mediation.partners.VservCustomAd#onInvalidate()
     */
    @Override
    public void onInvalidate() {
        if (LOGS_ENABLED) {
            Log.i("vserv", "onInvalidate fb native : ");
        }
        try {
            if (nativeAd != null) {
                nativeAd.unregisterView();
                nativeAd.setAdListener(null);
                nativeAd.destroy();
                nativeAd = null;
                if (LOGS_ENABLED) {
                    Log.i("vserv", "onInvalidate fb native clear : ");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private boolean extrasAreValid(final Map<String, Object> serverExtras) {
        final String placementId = serverExtras.get(PLACEMENT_ID_KEY)
                .toString();
        return (placementId != null && placementId.length() > 0);
    }


    public void handleImpression(ViewGroup viewgroup,View view,List<View> listOfView) {
        if (LOGS_ENABLED) {
            Log.i("vserv", "handleImpressions fb: ");
        }
        if (nativeAd != null) {
            if (listOfView != null) {
                if (LOGS_ENABLED) {
                    Log.i("vserv", " registerViewForInteraction with list of views: " + listOfView.size());
                }
                nativeAd.registerViewForInteraction(view, listOfView);
            } else if(view !=null){
                if (LOGS_ENABLED) {
                    Log.i("vserv", " registerViewForInteraction with only view: ");
                }
                nativeAd.registerViewForInteraction(view);
            }
        }
    }


    private Double getDoubleRating(final Rating rating) {
        if (rating == null) {
            return null;
        }
        return rating.getValue() / rating.getScale();
    }

}
