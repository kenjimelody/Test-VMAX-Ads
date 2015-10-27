package com.melody.android.testvmax;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.vserv.android.ads.api.VservAdView;
import com.vserv.android.ads.common.VservAdListener;
import com.vserv.android.ads.wallet.WalletListener;

/**
 * Created by Thanisak Piyasaksiri on 10/27/15 AD.
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private VservAdView interstitialAdView;

    public VservAdListener mAdListener = new VservAdListener() {

        @Override
        public void didInteractWithAd(VservAdView adView) {
            Toast.makeText(MainActivity.this, "didInteractWithAd", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void adViewDidLoadAd(VservAdView adView) {

            Toast.makeText(MainActivity.this, "adViewDidLoadAd", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void willPresentOverlay(VservAdView adView) {

            Toast.makeText(MainActivity.this, "willPresentOverlay", Toast.LENGTH_SHORT).show();
        }

        @Override
        public VservAdView didFailedToLoadAd(String arg0) {
            Toast.makeText(MainActivity.this, "didFailedToLoadAd", Toast.LENGTH_SHORT).show();
            return null;
        }

        @Override
        public VservAdView didFailedToCacheAd(String Error) {
            Toast.makeText(MainActivity.this, "didFailedToCacheAd", Toast.LENGTH_SHORT).show();
            return null;
        }

        @Override
        public void willLeaveApp(VservAdView adView) {
            Toast.makeText(MainActivity.this, "willLeaveApp", Toast.LENGTH_SHORT).show();
        }
    };

    @Override
    public void onBackPressed() {

        if(interstitialAdView != null)
            interstitialAdView.onBackPressed();

        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        interstitialAdView = new VservAdView(this, "8063", VservAdView.UX_INTERSTITIAL);
        interstitialAdView.setAdListener(mAdListener);
        interstitialAdView.setZoneId("8063");
        interstitialAdView.setUxType(VservAdView.UX_INTERSTITIAL);
        interstitialAdView.setTimeOut(20);
        interstitialAdView.setTestDevices("8063");
        interstitialAdView.loadAd();

        ((TextView) findViewById(R.id.btn_bannerview)).setOnClickListener(this);
        ((TextView) findViewById(R.id.btn_nativebanner)).setOnClickListener(this);
        ((TextView) findViewById(R.id.btn_nativecontentstream)).setOnClickListener(this);
        ((TextView) findViewById(R.id.btn_incentofferwall)).setOnClickListener(this);
        ((TextView) findViewById(R.id.btn_incentrewardedvideo)).setOnClickListener(this);
        ((TextView) findViewById(R.id.btn_videoads)).setOnClickListener(this);
    }

    @Override
    protected void onPause() {

        super.onPause();

        if(interstitialAdView != null)
            interstitialAdView.onPause();
    }

    @Override
    protected void onResume() {

        super.onResume();

        if(interstitialAdView != null)
            interstitialAdView.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {

        Intent intent = new Intent(this, BannerActivity.class);
        switch (v.getId()) {
            case R.id.btn_bannerview:

                intent.putExtra("zoneid", "20846");
                intent.putExtra("type", VservAdView.UX_BANNER);
                startActivity(intent);
                break;

            case R.id.btn_nativebanner:

                intent.putExtra("zoneid", "010116d2");
                intent.putExtra("type", VservAdView.UX_NATIVE);
                intent.putExtra("format", "In-feed");
                startActivity(intent);
                break;

            case R.id.btn_nativecontentstream:

                intent.putExtra("zoneid", "V76062644");
                intent.putExtra("type", VservAdView.UX_NATIVE);
                intent.putExtra("format", "Content Stream");
                startActivity(intent);
                break;

            case R.id.btn_incentofferwall:

                intent.putExtra("zoneid", "ac19acf0");
                intent.putExtra("type", VservAdView.UX_INTERSTITIAL);
                intent.putExtra("format", "Offerwall");
                startActivity(intent);
                break;

            case R.id.btn_incentrewardedvideo:

                intent.putExtra("zoneid", "f2f24f2a");
                intent.putExtra("type", VservAdView.UX_INTERSTITIAL);
                intent.putExtra("format", "Rewarded Video");
                startActivity(intent);
                break;

            case R.id.btn_videoads:

                intent.putExtra("zoneid", "V762947e3");
                intent.putExtra("type", VservAdView.UX_INTERSTITIAL);
                intent.putExtra("format", "Interstitial");
                startActivity(intent);
                break;
            default:
                break;
        }
    }
}
