

package com.vserv.android.ads.mediation.partners;

import java.util.Map;


import com.bee7.gamewall.GameWall;
import com.bee7.gamewall.GameWallLogic;
import com.bee7.gamewall.OnAvailableChangeListener;
import com.bee7.sdk.common.Reward;
import com.vserv.android.ads.reward.Offerwall;
import com.vserv.android.ads.wallet.WalletListener;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * @author nidhib
 */
public class Bee7Offerwall extends VservCustomAd implements
        OnAvailableChangeListener, GameWallLogic.RewardInterface {

    private static final String TAG = "vserv";
    private static final String APP_ID = "appid";
    private static final String APP_KEY = "appkey";
    private static final String VENDOR_ID = "vendorid";
    private VservCustomAdListener mVservCustomAdListener;
    public boolean LOGS_ENABLED = false;
    private Context mContext = null;
    private String vendorId = "";
    private String appId = null;
    private String appKey = null;


    private GameWall mGameWall;

    private Offerwall offerwall;

    @Override
    public void loadAd(Context context,
                       VservCustomAdListener vservCustomAdListener,
                       Map<String, Object> localExtras, Map<String, Object> serverExtras) {

        try {
            if (LOGS_ENABLED) {
                Log.i(TAG, "Bee7Offer loadad inter.");
            }
            mContext = context;
            mVservCustomAdListener = vservCustomAdListener;

            if (extrasAreValid(serverExtras)) {

                appKey = serverExtras.get(APP_KEY).toString();

                if (serverExtras.containsKey(VENDOR_ID)) {
                    vendorId = serverExtras.get(VENDOR_ID).toString();
                } else {
                    vendorId = "";
                }

                if (localExtras != null) {


                    if (localExtras.containsKey("offerwall")) {
                        offerwall = (Offerwall) localExtras.get("offerwall");

                    }

                }
                // Initialise Bee7 GameWall instance
                mGameWall = new GameWall((Activity) mContext, this, this,
                        appKey, vendorId, true);

                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
                prefs.edit().putString("bee7_api_key", appKey).commit();
                prefs.edit().putString("bee7_vendor_id", vendorId).commit();

                if (LOGS_ENABLED) {
                    Log.i(TAG, "gamewall initialised");
                }
            } else {
                if ((mContext != null) && (mContext instanceof Activity)) {
                    ((Activity) mContext).runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            if (mVservCustomAdListener != null)
                                mVservCustomAdListener.onAdFailed(0);
                        }
                    });
                }
                return;
            }

        } catch (Exception e) {

            e.printStackTrace();
            return;
        }
    }

    @Override
    public void showAd() {
        if (LOGS_ENABLED)
            Log.i(TAG, "bee7 showAd ");
        mGameWall.show();
    }

    @Override
    public void onInvalidate() {
        if (LOGS_ENABLED)
            Log.i(TAG, "onInvalidate ");
        mVservCustomAdListener = null;

        if (mGameWall != null) {
            if (!mGameWall.onBackPressed()) {
                return;
            }
        }
        mGameWall = null;
    }


    boolean extrasAreValid(final Map<String, Object> serverExtras) {
        return serverExtras.containsKey(APP_KEY);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.bee7.gamewall.GameWallLogic.RewardInterface#giveReward(com.bee7.sdk
     * .common.Reward)
     */
    @Override
    public void giveReward(Reward reward) {
        try {
            if (LOGS_ENABLED) {
                Log.d(TAG, "bee7 giveReward");

                Log.d(TAG, "Received reward: " + reward.toString());
                Log.d(TAG,
                        "Process received reward: "
                                + reward.getVirtualCurrencyAmount());
            }
            if (offerwall != null) {
                if (offerwall.getWalletElement() != null) {
                    offerwall.getWalletElement().awardVirtualCurrency(((long) reward.getVirtualCurrencyAmount()));
                }
            }


        } catch (Exception e) {
            if (LOGS_ENABLED) {
                e.printStackTrace();
            }
        }
    }

    public void onPause() {
        if (LOGS_ENABLED) {
            Log.d(TAG, "bee7 onPause");
        }
        /*
         * Call pause for Bee7 GameWall
		 */
        if (mGameWall != null) {
            mGameWall.pause();
        }
    }

    public void onResume() {
        if (LOGS_ENABLED) {
            Log.d(TAG, "bee7 onResume");
        }
        /*
         * Call resume for Bee7 GameWall
		 */

        if (mGameWall != null) {
            mGameWall.resume();
        }
    }

    public void onDestroy() {
        if (LOGS_ENABLED) {
            Log.d(TAG, "bee7 onDestroy");
        }
		/*
		 * Call destroy for Bee7 GameWall
		 */
        if (mGameWall != null) {
            mGameWall.destroy();

        }
    }


    public void onNewIntent(Intent intent) {
        if (LOGS_ENABLED) {
            Log.d(TAG, "bee7 onNewIntent");
        }
        if (mContext != null) {
            if (LOGS_ENABLED)
                Log.i("unity", "bee7 mContext is not null");
        } else {
            if (LOGS_ENABLED)
                Log.i("unity", "bee7 mContext is null");
        }
		/*
		 * Provide intent data to Bee7 GameWall in order to claim data
		 */
        if (mGameWall != null) {
            if (LOGS_ENABLED)
                Log.i("unity", "mGamewall is not null");
            mGameWall.onNewIntent(intent);
        }
    }

    public void onBackPressed() {
        if (LOGS_ENABLED) {
            Log.d(TAG, "bee7 onBackPressed");
        }
		/*
		 * Allow Bee7 GameWall to handle back press
		 */
        if (mGameWall != null) {
            if (!mGameWall.onBackPressed()) {
                return;
            }
        }
    }


    public void onConfigurationChanged() {
        if (LOGS_ENABLED) {
            Log.d(TAG, "bee7 onConfigurationChanged");
        }

		/*
		 * Allow Bee7 GameWall to handle orientation change. If app doesn't
		 * support orientation changes, remove this method.
		 */
        if (mGameWall != null) {
            mGameWall.updateView();
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.bee7.gamewall.OnAvailableChangeListener#onAvailableChange(boolean)
     */
    @Override
    public void onAvailableChange(boolean available) {

        if (available) {
            if (LOGS_ENABLED) {
                Log.d(TAG, "onAvailableChange if available");
            }

            // NOTE: Create impression event for GW button
            mGameWall.onGameWallButtonImpression();

            if ((mContext != null) && (mContext instanceof Activity)) {
                ((Activity) mContext).runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        if (mVservCustomAdListener != null)

                            mVservCustomAdListener.onAdLoaded();
                    }
                });
            }

        } else {
            if (LOGS_ENABLED) {
                Log.d(TAG, "onAvailableChange if not available");
            }
            if ((mContext != null) && (mContext instanceof Activity)) {
                ((Activity) mContext).runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        if (mVservCustomAdListener != null)
                            mVservCustomAdListener.onAdFailed(0);
                    }
                });
            }

        }

    }

}
