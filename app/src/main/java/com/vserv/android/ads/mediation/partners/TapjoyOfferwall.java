package com.vserv.android.ads.mediation.partners;

import java.util.Hashtable;
import java.util.Map;

import com.tapjoy.TapjoyConnect;
import com.tapjoy.TapjoyConnectFlag;
import com.tapjoy.TapjoyConnectNotifier;
import com.tapjoy.TapjoyNotifier;
import com.tapjoy.TapjoySpendPointsNotifier;
import com.tapjoy.TapjoyVideoNotifier;
import com.tapjoy.TapjoyViewNotifier;
import com.vserv.android.ads.reward.Offerwall;
import com.vserv.android.ads.wallet.WalletListener;

import android.app.Activity;
import android.content.Context;

import android.util.Log;

/**
 * Compatible with version 10.2.2 of the Tapjoy SDK.
 */
public class TapjoyOfferwall extends VservCustomAd {

    private static final String TAG = "vserv";
    private static final String APP_ID = "appid";
    private static final String SECRET_KEY = "secretkey";
    private static final String NON_REWARDED_ID = "nonrewardedid";
    private VservCustomAdListener mVservCustomAdListener;
    private WalletListener walletListener;
    private TapjoyConnectionListener mTapjoyListener = null;
    public boolean LOGS_ENABLED = true;
    Context mContext = null;
    private String nonRewardedId;
    private String appId = null;
    private String secretKey = null;
    private boolean isTapPointsUpdated = false;
    private boolean isGetCurrencyCalled = false;
    private boolean isSpendCurrencyCalled = false;
    private boolean ispointsUpdatedSuccessfully = false;
    private int spendcurrency = 0;


    private static boolean isTapjoyRequested = false;
    private Offerwall offerwall;
    private long reward = 0;

    @Override
    public void loadAd(Context context,
                       VservCustomAdListener vservCustomAdListener,
                       Map<String, Object> localExtras, Map<String, Object> serverExtras) {
        // TODO Auto-generated method stub
        try {
            if (LOGS_ENABLED) {
                Log.i(TAG, "TapjoyOffer loadad inter.");
            }
            mContext = context;
            mVservCustomAdListener = vservCustomAdListener;
            mTapjoyListener = new TapjoyConnectionListener();

            if (extrasAreValid(serverExtras)) {
                appId = serverExtras.get(APP_ID).toString();
                secretKey = serverExtras.get(SECRET_KEY).toString();

                if (serverExtras.containsKey(NON_REWARDED_ID)) {
                    nonRewardedId = serverExtras.get(NON_REWARDED_ID)
                            .toString();
                } else {
                    nonRewardedId = appId;
                }

                if (localExtras != null) {


                    if (localExtras.containsKey("offerwall")) {
                        offerwall = (Offerwall) localExtras.get("offerwall");

                    }

                }
                Hashtable<String, Object> connectFlags = new Hashtable<String, Object>();
                connectFlags.put(TapjoyConnectFlag.ENABLE_LOGGING, "true");

                TapjoyConnect.requestTapjoyConnect(context, appId, secretKey,
                        connectFlags, mTapjoyListener);

				/*
                 * @Override public void connectSuccess() { // TODO
				 * Auto-generated method stub if (LOGS_ENABLED) Log.i(TAG,
				 * "connectSuccess"); setTapjoyCallback(); if ((mContext !=
				 * null) && (mContext instanceof Activity)) { ((Activity)
				 * mContext) .runOnUiThread(new Runnable() {
				 * 
				 * @Override public void run() { if (mVservCustomAdListener !=
				 * null) { mVservCustomAdListener .onAdLoaded(); } } }); } else
				 * { if (mVservCustomAdListener != null) {
				 * mVservCustomAdListener.onAdLoaded(); } } }
				 * 
				 * @Override public void connectFail() { // TODO Auto-generated
				 * method stub if (LOGS_ENABLED) Log.i(TAG, "connectFail"); if
				 * ((mContext != null) && (mContext instanceof Activity)) {
				 * ((Activity) mContext) .runOnUiThread(new Runnable() {
				 * 
				 * @Override public void run() { if (mVservCustomAdListener !=
				 * null) { mVservCustomAdListener .onAdFailed(0); } } }); } else
				 * { if (mVservCustomAdListener != null) {
				 * mVservCustomAdListener.onAdFailed(0); } } return; } });
				 */
            } else {
                if ((mContext != null) && (mContext instanceof Activity)) {
                    ((Activity) mContext).runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            if (mVservCustomAdListener != null)
                                mVservCustomAdListener.onAdFailed(0);
                        }
                    });
                } else {
                    if (mVservCustomAdListener != null)
                        mVservCustomAdListener.onAdFailed(0);
                }
                return;
            }

        } catch (Exception e) {
            if ((mContext != null) && (mContext instanceof Activity)) {
                ((Activity) mContext).runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        if (mVservCustomAdListener != null) {
                            mVservCustomAdListener.onAdFailed(0);
                        }
                    }
                });
            } else {
                if (mVservCustomAdListener != null)
                    mVservCustomAdListener.onAdFailed(0);
            }
            e.printStackTrace();
            return;
        }
    }

    @Override
    public void showAd() {
        // TODO Auto-generated method stub
        if (nonRewardedId != null) {
            if (nonRewardedId == appId) {
                if (LOGS_ENABLED) {
                    Log.i(TAG, "TapjoyOffer Process Rewarded.");
                }
                TapjoyConnect.getTapjoyConnectInstance()
                        .showOffersWithCurrencyID(nonRewardedId, true);
            } else {
                if (LOGS_ENABLED) {
                    Log.i(TAG, "TapjoyOffer Process Non-Rewarded.");
                }
                TapjoyConnect.getTapjoyConnectInstance()
                        .showOffersWithCurrencyID(nonRewardedId, false);
            }
        }
    }

    @Override
    public void onInvalidate() {
        if (LOGS_ENABLED)
            Log.i(TAG, "onInvalidate ");
        if (mTapjoyListener != null)
            mTapjoyListener = null;

        if (mVservCustomAdListener != null)
            mVservCustomAdListener = null;

        if (walletListener != null && isTapPointsUpdated) {
            if (LOGS_ENABLED)
                Log.i(TAG, "onInvalidate incent destoryed ");
            walletListener = null;
        }

    }

    public void spendCurrency(int vc,
                              final WalletListener t_walletListener,
                              String t_appid, String t_secretkey, Context context) {
        if (LOGS_ENABLED) {
            Log.i(TAG, "tapjoy SpendCurrency."+vc);
        }
        try {
            mContext = context;
            spendcurrency = vc;
            TapjoyConnect tpconConnect = TapjoyConnect
                    .getTapjoyConnectInstance();
            if (tpconConnect != null) {
                if (LOGS_ENABLED)
                    Log.i(TAG, "tapjoy connected");
                TapjoyConnect.getTapjoyConnectInstance().spendTapPoints(vc,
                        new TapjoySpendPointsNotifier() {

                            @Override
                            public void getSpendPointsResponseFailed(
                                    final String arg0) {
                                isTapPointsUpdated = true;
                                if (LOGS_ENABLED)
                                    Log.i(TAG, "getSpendPointsResponseFailed "
                                            + arg0);
                                ((Activity) mContext)
                                        .runOnUiThread(new Runnable() {

                                            @Override
                                            public void run() {

                                                if (t_walletListener != null) {
                                                    t_walletListener.onUpdateFailedVirtualCurrency(arg0);
                                                } else {
                                                    if (offerwall != null && offerwall.getWalletElement() != null) {
                                                        offerwall.getWalletElement().awardVirtualCurrency(0);

                                                    }
                                                }

                                            }

                                        });

                            }

                            @Override
                            public void getSpendPointsResponse(
                                    String currencyName, final int pointTotal) {
                                isTapPointsUpdated = true;
                                if (LOGS_ENABLED)
                                    Log.i(TAG, "getSpendPointsResponse "
                                            + pointTotal);
                                ((Activity) mContext)
                                        .runOnUiThread(new Runnable() {

                                            @Override
                                            public void run() {
                                                if (t_walletListener != null) {
                                                    t_walletListener.onUpdateVirtualCurrency(spendcurrency);
                                                } else {
                                                    if (offerwall != null && offerwall.getWalletElement() != null) {
                                                        offerwall.getWalletElement().awardVirtualCurrency(spendcurrency);
                                                    }
                                                }
                                            }

                                        });

                            }
                        });
            } else {

                if (LOGS_ENABLED)
                    Log.i(TAG, "tapjoy not connected");
                isSpendCurrencyCalled = true;
                mTapjoyListener = new TapjoyConnectionListener();
                appId = t_appid;
                secretKey = t_secretkey;
                Hashtable<String, Object> connectFlags = new Hashtable<String, Object>();
                connectFlags.put(TapjoyConnectFlag.ENABLE_LOGGING, "true");

                TapjoyConnect.requestTapjoyConnect(mContext, appId, secretKey,
                        connectFlags, mTapjoyListener);

            }
        } catch (Exception e) {
        }

    }

    public void getCurrency(
            final WalletListener t_walletListener,
            String t_appid, String t_secretkey, Context context) {
        mContext = context;
        this.walletListener = t_walletListener;

        if (LOGS_ENABLED) {
            Log.i(TAG, "tapjoy getCurrency.");
            Log.i(TAG, "tapjoy isGetCurrencyCalled" + isGetCurrencyCalled);
        }
        try {

            TapjoyConnect tpconConnect = TapjoyConnect
                    .getTapjoyConnectInstance();
            if (tpconConnect != null) {
                if (LOGS_ENABLED)
                    Log.i(TAG, "tapjoy connected");
                TapjoyConnect.getTapjoyConnectInstance().getTapPoints(
                        new TapjoyNotifier() {

                            @Override
                            public void getUpdatePointsFailed(final String arg0) {
                                isTapPointsUpdated = true;
                                if (LOGS_ENABLED)
                                    Log.i(TAG,
                                            "tapjoy getCurrency getUpdatePointsFailed "
                                                    + arg0);
                                ((Activity) mContext)
                                        .runOnUiThread(new Runnable() {

                                            @Override
                                            public void run() {

                                                if (walletListener != null) {

                                                    walletListener
                                                            .onUpdateFailedVirtualCurrency(arg0);

                                                }
                                            }

                                        });
                            }

                            @Override
                            public void getUpdatePoints(String currencyName,
                                                        final int pointTotal) {
                                isTapPointsUpdated = true;
                                if (LOGS_ENABLED)
                                    Log.i(TAG,
                                            "tapjoy getCurrency getUpdatePoints "
                                                    + pointTotal);

                                spendCurrency(pointTotal,
                                        walletListener, appId,
                                        secretKey, mContext);

                            }
                        });
            } else {
                if (LOGS_ENABLED)
                    Log.i(TAG, "tapjoy not connected");
                if (!isTapjoyRequested) {
                    isTapjoyRequested = true;
                    isGetCurrencyCalled = true;
                    mTapjoyListener = new TapjoyConnectionListener();
                    appId = t_appid;
                    secretKey = t_secretkey;
                    Hashtable<String, Object> connectFlags = new Hashtable<String, Object>();
                    connectFlags.put(TapjoyConnectFlag.ENABLE_LOGGING, "true");

                    TapjoyConnect.requestTapjoyConnect(mContext, appId,
                            secretKey, connectFlags, mTapjoyListener);
                } else {
                    ((Activity) mContext).runOnUiThread(new Runnable() {

                        @Override
                        public void run() {

                            if (walletListener != null) {
                                walletListener
                                        .onUpdateFailedVirtualCurrency("Tapjoy not initialised yet");

                            }
                        }

                    });
                }

            }
        } catch (Exception e) {
        }

    }

    boolean extrasAreValid(final Map<String, Object> serverExtras) {
        String appId = serverExtras.get(APP_ID).toString();
        String secretKey = serverExtras.get(SECRET_KEY).toString();
        return ((appId != null && appId.length() > 0) && (secretKey != null && secretKey
                .length() > 0));
    }

    void setTapjoyCallback() {
        if (LOGS_ENABLED) {
            Log.i(TAG, "setTapjoyCallback.");
        }
        ispointsUpdatedSuccessfully = false;
        try {

            // Get notifications when Tapjoy views open or close.
            TapjoyConnect.getTapjoyConnectInstance().setTapjoyViewNotifier(
                    new TapjoyViewNotifier() {

                        @Override
                        public void viewWillOpen(int arg0) {
                            // TODO Auto-generated method stub
                            if (LOGS_ENABLED)
                                Log.i(TAG, "View is about to be shown");
                        }

                        @Override
                        public void viewWillClose(int arg0) {
                            // TODO Auto-generated method stub
                            if (LOGS_ENABLED)
                                Log.v(TAG, "Offerwall is about to go away");
                        }

                        @Override
                        public void viewDidOpen(int arg0) {
                            // TODO Auto-generated method stub
                            if (LOGS_ENABLED)
                                Log.i(TAG, "Offerwall has been shown");
                            if ((mContext != null)
                                    && (mContext instanceof Activity)) {
                                ((Activity) mContext)
                                        .runOnUiThread(new Runnable() {

                                            @Override
                                            public void run() {
                                                if (mVservCustomAdListener != null)
                                                    mVservCustomAdListener
                                                            .onAdShown();
                                            }
                                        });
                            } else {
                                if (mVservCustomAdListener != null)
                                    mVservCustomAdListener.onAdShown();
                            }
                        }

                        @Override
                        public void viewDidClose(int arg0) {

                            // Best Practice: We recommend calling getTapPoints
                            // as
                            // often as possible so the user�s balance is
                            // always
                            // up-to-date.
                            if (LOGS_ENABLED)
                                Log.i(TAG, "Offerwall has closed");

                            TapjoyConnect.getTapjoyConnectInstance()
                                    .getTapPoints(new TapjoyNotifier() {

                                        @Override
                                        public void getUpdatePointsFailed(
                                                final String arg0) {
                                            isTapPointsUpdated = true;
                                            if (!ispointsUpdatedSuccessfully) {
                                                if (LOGS_ENABLED)
                                                    Log.i(TAG,
                                                            "getUpdatePointsFailed "
                                                                    + arg0);
                                                ((Activity) mContext)
                                                        .runOnUiThread(new Runnable() {

                                                            @Override
                                                            public void run() {
                                                                if (offerwall != null && offerwall.getWalletElement() != null) {
                                                                    offerwall.getWalletElement().awardVirtualCurrency(0);

                                                                }

                                                            }

                                                        });
                                            }
                                        }

                                        @Override
                                        public void getUpdatePoints(
                                                String currencyName,
                                                final int pointTotal) {
                                            isTapPointsUpdated = true;
                                            ispointsUpdatedSuccessfully = true;
                                            if (LOGS_ENABLED)
                                                Log.i(TAG,
                                                        "getUpdatePoints 1= "
                                                                + currencyName
                                                                + "2= "
                                                                + pointTotal);

                                            if (offerwall != null && offerwall.getWalletElement() != null) {
                                                spendCurrency(pointTotal,
                                                        null,
                                                        appId, secretKey, mContext);
                                            }
                                            // ((Activity) mContext)
                                            // .runOnUiThread(new Runnable() {
                                            //
                                            // @Override
                                            // public void run() {
                                            // if (mVservCustomIncentListener !=
                                            // null) {
                                            // mVservCustomIncentListener
                                            // .onUpdateVirtualCurrency(pointTotal);
                                            // } else {
                                            // if (LOGS_ENABLED)
                                            //
                                            // Log.i(TAG,
                                            // "getUpdatePoints incent list null");
                                            // }
                                            // }
                                            // });
                                        }
                                    });
                            if ((mContext != null)
                                    && (mContext instanceof Activity)) {
                                ((Activity) mContext)
                                        .runOnUiThread(new Runnable() {

                                            @Override
                                            public void run() {
                                                if (mVservCustomAdListener != null)
                                                    mVservCustomAdListener
                                                            .onAdDismissed();
                                            }
                                        });
                            } else {
                                if (mVservCustomAdListener != null)
                                    mVservCustomAdListener.onAdDismissed();
                            }
                        }
                    });

            // Get notifications on video start, complete and error
            TapjoyConnect.getTapjoyConnectInstance().setVideoNotifier(
                    new TapjoyVideoNotifier() {

                        @Override
                        public void videoStart() {
                            if (LOGS_ENABLED)
                                Log.i(TAG, "video has started");
                        }

                        @Override
                        public void videoError(int statusCode) {
                            if (LOGS_ENABLED)
                                Log.i(TAG,
                                        "there was an error with the video: "
                                                + statusCode);
                        }

                        @Override
                        public void videoComplete() {
                            if (LOGS_ENABLED)
                                Log.i(TAG, "video has completed");

                            // Get notifications whenever Tapjoy currency is
                            // earned.
                            // TapjoyConnect.getTapjoyConnectInstance()
                            // .setEarnedPointsNotifier(
                            // new TapjoyEarnedPointsNotifier() {
                            // @Override
                            // public void earnedTapPoints(
                            // int amount) {
                            // if (LOGS_ENABLED)
                            // Log.i(TAG,
                            // "ammount earned+ "
                            // + amount);
                            // }
                            // });

                            // Best Practice: We recommend calling getTapPoints
                            // as
                            // often as possible so the user�s balance is
                            // always
                            // up-to-date.
                            TapjoyConnect.getTapjoyConnectInstance()
                                    .getTapPoints(new TapjoyNotifier() {

                                        @Override
                                        public void getUpdatePointsFailed(
                                                final String arg0) {

                                            isTapPointsUpdated = true;
                                            if (LOGS_ENABLED)
                                                Log.i(TAG,
                                                        "video getUpdatePointsFailed");
                                            ((Activity) mContext)
                                                    .runOnUiThread(new Runnable() {

                                                        @Override
                                                        public void run() {
                                                            if (offerwall != null && offerwall.getWalletElement() != null) {
                                                                offerwall.getWalletElement().awardVirtualCurrency(0);

                                                            }
                                                        }

                                                    });

                                        }

                                        @Override
                                        public void getUpdatePoints(
                                                String currencyName,
                                                final int pointTotal) {
                                            isTapPointsUpdated = true;
                                            if (LOGS_ENABLED)
                                                Log.i(TAG,
                                                        "video getUpdatePoints");
                                            if (offerwall != null && offerwall.getWalletElement() != null) {

                                                spendCurrency(pointTotal,
                                                        null,
                                                        appId, secretKey, mContext);
                                            }

                                        }
                                    });
                        }
                    });
        } catch (Exception exe) {
            if (LOGS_ENABLED)
                Log.i(TAG, "Exception in setTapjoyCallback");
            if ((mContext != null) && (mContext instanceof Activity)) {
                ((Activity) mContext).runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        if (mVservCustomAdListener != null)
                            mVservCustomAdListener.onAdFailed(0);
                    }
                });
            } else {
                if (mVservCustomAdListener != null)
                    mVservCustomAdListener.onAdFailed(0);
            }
            return;
        }
    }

    class TapjoyConnectionListener implements TapjoyConnectNotifier {

        @Override
        public void connectFail() {
            isTapPointsUpdated = true;
            if (LOGS_ENABLED)
                Log.i(TAG, "connectFail");

            isTapjoyRequested = false;

            if ((mContext != null) && (mContext instanceof Activity)) {
                ((Activity) mContext).runOnUiThread(new Runnable() {

                    @Override
                    public void run() {

                        if (offerwall != null && offerwall.getWalletElement() != null) {
                            offerwall.getWalletElement().awardVirtualCurrency(0);
                        }

                        if (mVservCustomAdListener != null) {
                            mVservCustomAdListener.onAdFailed(0);
                        }
                    }
                });
            }
            return;

        }

        @Override
        public void connectSuccess() {
            if (LOGS_ENABLED)
                Log.i(TAG, "connectSuccess");

            setTapjoyCallback();
            isTapjoyRequested = false;
            if (isGetCurrencyCalled) {
                isGetCurrencyCalled = false;
                getCurrency(walletListener, appId, secretKey,
                        mContext);
            }

            if ((mContext != null) && (mContext instanceof Activity)) {
                ((Activity) mContext).runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        if (mVservCustomAdListener != null) {
                            mVservCustomAdListener.onAdLoaded();
                        }
                    }
                });
            } else {
                if (mVservCustomAdListener != null) {
                    mVservCustomAdListener.onAdLoaded();
                }
            }

        }
    }


    public void onPause() {

    }

    public void onResume() {

    }

}
