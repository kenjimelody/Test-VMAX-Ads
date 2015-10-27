package com.vserv.android.ads.mediation.partners;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.supersonic.mediationsdk.logger.SupersonicError;
import com.supersonic.mediationsdk.model.Placement;
import com.supersonic.mediationsdk.sdk.RewardedVideoListener;
import com.supersonic.mediationsdk.sdk.Supersonic;
import com.supersonic.mediationsdk.sdk.SupersonicFactory;
import com.vserv.android.ads.api.VservAdView;
import com.vserv.android.ads.reward.RewardVideo;
import com.vserv.android.ads.reward.RewardVideoDelegate;

import java.util.Map;

/**
 * Created by Narendra on 9/3/2015.
 */
public class SupersonicRewardedVideo extends VservCustomAd implements RewardedVideoListener {

    private Context mContext;
    private boolean LOGS_ENABLED = true;
    private VservCustomAdListener mVservCustomAdListener;
    private String appkey = null;
    private String TAG = "vserv";
    private String userId;
    private final String APPLICATION_KEY = "applicationkey";
    private final String PLACEMENT_NAME = "placement_name";
    private final String ADVID = "advid";
    private Supersonic mMediationAgent;
    private Placement placement;
    private String placementName;
    private static boolean isInitCalled;
    private RewardVideoDelegate rewardedVideoDelegate;
    private RewardVideo rewardedVideoAd;
    private long reward = 0;


    @Override
    public void loadAd(Context context, VservCustomAdListener vservCustomAdListener, Map<String, Object> localExtras, Map<String, Object> serverExtras) {

        try {
            if (LOGS_ENABLED) {
                Log.i("vserv", "loadAd supersonic");
            }
            mContext = context;

            mVservCustomAdListener = vservCustomAdListener;
            //userId = Settings.Secure.getString(mContext.getContentResolver(), Settings.Secure.ANDROID_ID);
            if (extrasAreValid(serverExtras)) {
                appkey = serverExtras.get(APPLICATION_KEY).toString();
                if (serverExtras.containsKey(PLACEMENT_NAME)) {
                    placementName = serverExtras.get(PLACEMENT_NAME).toString();
                }
                if (localExtras != null) {
                    if (localExtras.containsKey(ADVID)) {
                        userId = localExtras.get(ADVID).toString();
                    }

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
                loadSupersonicAd();
            } else {
                if ((mContext != null) && (mContext instanceof Activity)) {
                    ((Activity) mContext).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (mVservCustomAdListener != null) {
                                mVservCustomAdListener.onAdFailed(0);
                            }
                        }
                    });
                }
                return;
            }
        } catch (Exception e) {
            if (LOGS_ENABLED) {
                Log.i("vserv", "inside exception");
                e.printStackTrace();
            }
            if ((mContext != null) && (mContext instanceof Activity)) {
                ((Activity) mContext).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mVservCustomAdListener != null) {
                            if (LOGS_ENABLED) {
                                Log.i("vserv", "exception onAdFailed");
                            }
                            mVservCustomAdListener.onAdFailed(0);
                        }
                    }
                });
            }
        }
    }

    private void loadSupersonicAd() {
        if (mMediationAgent == null) {
            mMediationAgent = SupersonicFactory.getInstance();
            if (placementName != null && placementName.length() > 0) {
                placement = mMediationAgent.getPlacementInfo(placementName);
            }
        }
        mMediationAgent.setRewardedVideoListener(this);
        if (!isInitCalled) {
            mMediationAgent.initRewardedVideo((Activity) mContext, appkey, userId);
        } else {
            if (mMediationAgent.isRewardedVideoAvailable()) {
                if ((mContext != null) && (mContext instanceof Activity)) {
                    ((Activity) mContext).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (mVservCustomAdListener != null) {
                                mVservCustomAdListener.onAdLoaded();
                            }
                        }
                    });
                }
            } else {
                if ((mContext != null) && (mContext instanceof Activity)) {
                    ((Activity) mContext).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (mVservCustomAdListener != null) {
                                mVservCustomAdListener.onAdFailed(0);
                            }
                        }
                    });
                }
            }
        }
    }

    @Override
    public void showAd() {
        if (mMediationAgent != null) {
            if (placement != null) {
                mMediationAgent.showRewardedVideo(placement.getPlacementName());
            } else {
                mMediationAgent.showRewardedVideo();
            }
        } else {
            if ((mContext != null) && (mContext instanceof Activity)) {
                ((Activity) mContext).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mVservCustomAdListener != null) {
                            mVservCustomAdListener.onAdFailed(0);
                        }
                    }
                });
            }
        }
    }

    @Override
    public void onInvalidate() {
        if (mMediationAgent != null) {
            Log.i("vserv", "invalidate supersonic");
            mMediationAgent.release((Activity) mContext);
        }
    }

    boolean extrasAreValid(final Map<String, Object> serverExtras) {
       /* if(serverExtras.containsKey(PLACEMENT_NAME) && serverExtras.containsKey(APPLICATION_KEY))
        {
            return true;
        }
        return false;*/
        return serverExtras.containsKey(APPLICATION_KEY);
    }

    public void onResume() {
        if (LOGS_ENABLED) {
            Log.i("vserv", "onResume Supersonic");
        }
        if (mMediationAgent != null) {

            mMediationAgent.onResume((Activity) mContext);
        }
    }

    public void onPause() {
        if (LOGS_ENABLED) {
            Log.i("vserv", "onPause Supersonic");
        }
        if (mMediationAgent != null) {

            mMediationAgent.onPause((Activity) mContext);
        }
    }

    public void onDestroy() {
        if (LOGS_ENABLED)
            if (mMediationAgent != null) {
                mMediationAgent.release((Activity) mContext);
            }
    }

    @Override
    public void onRewardedVideoInitSuccess() {
        isInitCalled = true;
        if (mMediationAgent.isRewardedVideoAvailable()) {
            if ((mContext != null) && (mContext instanceof Activity)) {
                ((Activity) mContext).runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        if (mVservCustomAdListener != null) {
                            mVservCustomAdListener.onAdLoaded();
                        }
                    }
                });
            }
        } else {
            if ((mContext != null) && (mContext instanceof Activity)) {
                ((Activity) mContext).runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        if (mVservCustomAdListener != null) {
                            mVservCustomAdListener.onAdFailed(0);
                        }
                    }
                });
            }
        }
    }

    @Override
    public void onRewardedVideoInitFail(final SupersonicError supersonicError) {

        isInitCalled = false;
        if ((mContext != null) && (mContext instanceof Activity)) {
            ((Activity) mContext).runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    if (mVservCustomAdListener != null) {
                        final int errorCode = supersonicError.getErrorCode();
                        if ((mContext != null) && (mContext instanceof Activity)) {
                            ((Activity) mContext).runOnUiThread(new Runnable() {

                                @Override
                                public void run() {
                                    if (mVservCustomAdListener != null) {
                                        mVservCustomAdListener.onAdFailed(errorCode);
                                    }
                                }
                            });
                        }
                    }
                }
            });
        }
    }

    @Override
    public void onRewardedVideoAdOpened() {
        if ((mContext != null) && (mContext instanceof Activity)) {
            ((Activity) mContext).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mVservCustomAdListener != null) {
                        mVservCustomAdListener.onAdShown();
                    }
                }
            });
        }
    }

    @Override
    public void onRewardedVideoAdClosed() {
        if ((mContext != null) && (mContext instanceof Activity)) {
            ((Activity) mContext).runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    if (mVservCustomAdListener != null)
                        mVservCustomAdListener.onAdDismissed();
                }
            });
        }

    }

    @Override
    public void onVideoAvailabilityChanged(boolean available) {
    }

    @Override
    public void onVideoStart() {
    }

    @Override
    public void onVideoEnd() {
    }

    @Override
    public void onRewardedVideoAdRewarded(Placement placement) {
        String rewardName = placement.getRewardName();
        int rewardAmount = placement.getRewardAmount();

        VservAdView.isVideoComplete=true;
        if (rewardedVideoAd != null) {
            Log.d("vserv", "SuperSonic award: " + reward);
            rewardedVideoAd.getWalletElement().awardVirtualCurrency(reward);
        }
        if (rewardedVideoDelegate != null) {
            Log.d("vserv", "SuperSonic delegate: " );
            rewardedVideoDelegate.onRewardVideoCompleted(reward);
        }
    }

    @Override
    public void onRewardedVideoShowFail(final SupersonicError supersonicError) {
        if ((mContext != null) && (mContext instanceof Activity)) {
            ((Activity) mContext).runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    if (mVservCustomAdListener != null) {
                        int errorCode = supersonicError.getErrorCode();
                        if (errorCode == SupersonicError.ERROR_CODE_GENERIC) {
                            if (LOGS_ENABLED) {
                                Log.i("vserv", "generic error");
                            }
                        }
                        mVservCustomAdListener.onAdFailed(errorCode);
                    }
                }
            });
        }
    }
}