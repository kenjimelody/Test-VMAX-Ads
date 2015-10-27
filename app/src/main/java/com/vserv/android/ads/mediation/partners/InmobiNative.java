/**
 * Project      : Advert
 * Filename     : InmobiNative.java
 * Author       : nidhib
 * Comments     :
 * Copyright    : Copyright  2014, VSERV
 */

package com.vserv.android.ads.mediation.partners;

import java.util.Iterator;
import java.util.List;
import java.util.Map;


import com.inmobi.commons.InMobi;
import com.inmobi.monetization.IMErrorCode;
import com.inmobi.monetization.IMNative;
import com.inmobi.monetization.IMNativeListener;


import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author nidhib
 */
public class InmobiNative extends VservCustomAd {

    private static final String PROPERTY_ID = "propertyid";

    private VservCustomNativeAdListener mnativeListener;

    public boolean LOGS_ENABLED = true;
    private Context context = null;
    public IMNative nativeAd = null;
    private JSONObject adJson = null;
    private VservCustomAdListener customListener;


    @Override
    public void loadAd(Context context, VservCustomAdListener customListener,
                       Map<String, Object> localExtras, Map<String, Object> serverExtras) {
        try {
            if (LOGS_ENABLED) {
                Log.d("vserv", "Inside load InmobiNative ");
            }
            this.customListener = customListener;
            this.context = context;
            final String propertyId;
            if (localExtras != null) {
                if (localExtras.containsKey("nativeListener")) {
                    if (LOGS_ENABLED) {
                        Log.i("Log", "nativeListener in localextras ");
                    }
                    mnativeListener = (VservCustomNativeAdListener) localExtras.get("nativeListener");
                }
            }

            if (extrasAreValid(serverExtras)) {
                propertyId = serverExtras.get(PROPERTY_ID).toString();
                if (LOGS_ENABLED) {
                    Log.d("vserv", "Inside loadBanner propertyId " + propertyId);
                }
                InMobi.initialize(context, propertyId);
                InMobi.setLogLevel(InMobi.LOG_LEVEL.DEBUG);
                nativeAd = new IMNative(propertyId, listener);
                nativeAd.loadAd();
            } else {
                mnativeListener.onAdFailed("Property Id missing");
                return;
            }

        } catch (Exception e) {
            if (mnativeListener != null) {
                mnativeListener.onAdFailed(e.getMessage());
            }
            e.printStackTrace();
            return;
        }

    }

    private boolean extrasAreValid(Map<String, Object> serverExtras) {

        return serverExtras.containsKey(PROPERTY_ID);
    }

    @Override
    public void showAd() {
        if (LOGS_ENABLED) {
            Log.d("vserv", "Inside show InmobiNative ");
        }

    }

    @Override
    public void onInvalidate() {
        if (LOGS_ENABLED) {
            Log.d("vserv", "Inside onInvalidate ");
        }
        try {
            if (nativeAd != null) {
                nativeAd.detachFromView();
                nativeAd = null;
                listener = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //Native Listeners
    IMNativeListener listener = new IMNativeListener() {

        @Override
        public void onNativeRequestSucceeded(IMNative nativeAd) {

            if (LOGS_ENABLED) {
                Log.d("vserv", "onNativeRequestSucceeded content: " + nativeAd.getContent());
            }
            Object[] objArray = new Object[0];
            JSONObject temp_json = new JSONObject();
            try {
                adJson = new JSONObject(String.valueOf(nativeAd.getContent()));
                if (adJson != null) {
                    Iterator<String> temp = adJson.keys();
                    while (temp.hasNext()) {
                        String keysTemp = temp.next();
                        try {
                            JSONObject panel = adJson.getJSONObject(keysTemp); // get key from list

                            temp_json.put(keysTemp, panel.getString("url"));
                            temp_json.put(keysTemp + "_HEIGHT", panel.getString("height"));
                            temp_json.put(keysTemp + "_WIDTH", panel.getString("width"));
                        } catch (JSONException e) {
                            Log.i("vserv", "temp_json JSONException : ");
                            String tempKey = adJson.getString(keysTemp);
                            temp_json.put(keysTemp, tempKey);
                        } catch (Exception e) {
                            Log.i("vserv", "temp_json exception : ");
                        }
                    }
                    Log.i("vserv", "temp_json : " + temp_json);


                }
                objArray = new Object[]{temp_json};
            } catch (JSONException e) {
                e.printStackTrace();
            }
            mnativeListener.onAdLoaded(objArray);

        }

        @Override
        public void onNativeRequestFailed(IMErrorCode errorCode) {

            if (errorCode != null) {
                if (LOGS_ENABLED) {
                    Log.d("vserv", "onNativeRequestFailed " + errorCode.toString());
                }
                mnativeListener.onAdFailed(errorCode.toString());
            } else {
                if (LOGS_ENABLED) {
                    Log.d("vserv", "onNativeRequestFailed No ad in inventory ");
                }
                mnativeListener.onAdFailed("No ad in inventory");
            }
        }


    };


    private int listViewCountCheck = -1;

    public void handleImpression(ViewGroup viewgroup, View view, List<View> listOfView) {
        try {
            if (LOGS_ENABLED) {
                Log.i("vserv", "handleImpressions inmobi: ");
            }

            if (nativeAd != null) {
                nativeAd.attachToView(viewgroup);
                if (listOfView != null) {
                    final String clickURL = adJson.getString("CLICK_URL");

                    if (clickURL != null && !clickURL.equals("")) {
                        for (int j = 0; j < listOfView.size(); j++) {
                            View localView = listOfView.get(j);
                            if (view.findViewById(localView.getId()) != null) {
                                listViewCountCheck = 1;
                            } else {
                                listViewCountCheck = 0;
                                break;
                            }
                        }
                        if (listViewCountCheck != 1) {
                            if (LOGS_ENABLED) {
                                Log.d("vserv", "Invalid view provided for registering click");
                            }
                            return;
                        }

                        for (int i = 0; i < listOfView.size(); i++) {
                            View localView = listOfView.get(i);
                            localView.setOnClickListener(new View.OnClickListener() {

                                @Override
                                public void onClick(View v) {
                                    if (customListener != null) {
                                        customListener.onAdClicked();
                                        nativeAd.handleClick(null);
                                    }
                                    Uri uri = Uri.parse(clickURL);
                                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                                    context.startActivity(intent);
                                }
                            });
                        }
                    }
                } else if (view != null) {

                    view.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (LOGS_ENABLED) {
                                Log.i("vserv", "handleImpressions inmobi onClick: " + view);
                            }
                            customListener.onAdClicked();
                            nativeAd.handleClick(null);
                            if (adJson != null) {
                                if (adJson.has("CLICK_URL")) {
                                    try {
                                        String t_value = adJson.getString("CLICK_URL");
                                        if (t_value != null) {

                                            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(t_value));
                                            browserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                            context.startActivity(browserIntent);
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }
                    });
                }

            }
        } catch (Exception e) {
        }
    }
}
