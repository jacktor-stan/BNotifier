package com.jacktorscript.batterynotifier

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.billingclient.api.*
import com.android.billingclient.api.BillingFlowParams.ProductDetailsParams
import com.android.billingclient.api.QueryProductDetailsParams.Product
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.common.collect.ImmutableList
import com.jacktorscript.batterynotifier.iap.adapters.PremiumAdapter
import com.jacktorscript.batterynotifier.iap.interfaces.RecycleViewInterface
import com.jacktorscript.batterynotifier.core.Prefs
import com.jacktorscript.batterynotifier.core.PrefsConfig


class PremiumActivity : AppCompatActivity(), RecycleViewInterface {
    //var TAG = "TestINAPP"
    private var activity: Activity? = null
    private var prefs: Prefs? = null
    private var prefsConfig: PrefsConfig? = null
    private var billingClient: BillingClient? = null
    private var productDetailsList: MutableList<ProductDetails>? = null
    private var loadProducts: ProgressBar? = null
    private var recyclerView: RecyclerView? = null
    private var jacktorMsg: TextView? = null
    private var jacktorImg: ImageView? = null
    //private var toolbar: Toolbar? = null
    private var handler: Handler? = null
    private var btnRestoreFab: ExtendedFloatingActionButton? = null
    private var adapter: PremiumAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        //installSplashScreen()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_premium)
        initViews()

        // Atur warna status bar untuk android lollipop
        if (Build.VERSION.SDK_INT <= 22) {
            window.statusBarColor = ContextCompat.getColor(this, R.color.black)
        }

        //Top App Bar
        val topAppBar = findViewById<View>(R.id.topAppBar) as MaterialToolbar
        setSupportActionBar(topAppBar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        if (prefsConfig!!.getPremium()  == 1) {
            title = getString(R.string.app_name) + " PRO"
            jacktorMsg?.text = getString(R.string.jacktor_msg_purchased)
            jacktorImg?.setImageResource(R.mipmap.jacktor_boop)
        }

        //Tombol back
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                startActivity(Intent(activity, MainActivity::class.java))
                finish()
            }
        })

        //Initialize a BillingClient with PurchasesUpdatedListener onCreate method
        billingClient = BillingClient.newBuilder(this)
            .enablePendingPurchases()
            .setListener { billingResult: BillingResult, list: List<Purchase>? ->
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && list != null) {
                    for (purchase in list) {
                        handlePurchase(purchase)
                    }
                }
            }.build()

        //start the connection after initializing the billing client
        establishConnection()


        //restore purchases
        btnRestoreFab?.setOnClickListener {
            //Log.d(TAG, "CLICKED RESTORE")
            restorePurchases()
            //showSnackbar(btnRestoreFab, getString(R.string.cant_refund), Snackbar.LENGTH_SHORT)
        }
    }

    fun establishConnection() {
        billingClient!!.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    // The BillingClient is ready. You can query purchases here.
                    showProducts()
                }
            }

            override fun onBillingServiceDisconnected() {
                // Try to restart the connection on the next request to
                // Google Play by calling the startConnection() method.
                establishConnection()
            }
        })
    }

    fun showProducts() {
        val productList = ImmutableList.of( //Product 1
            Product.newBuilder()
                .setProductId("upgrade_pro")
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        )
        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(productList)
            .build()
        billingClient!!.queryProductDetailsAsync(
            params
        ) { _: BillingResult?, prodDetailsList: List<ProductDetails>? ->
            // Process the result
            productDetailsList!!.clear()
            handler!!.postDelayed({
                loadProducts!!.visibility = View.INVISIBLE
                productDetailsList!!.addAll(prodDetailsList!!)
                //Log.d(TAG, productDetailsList!!.size.toString() + " number of products")
                adapter = PremiumAdapter(
                    applicationContext,
                    productDetailsList!!,
                    this as RecycleViewInterface, prefsConfig!!
                )
                recyclerView!!.setHasFixedSize(true)
                recyclerView!!.layoutManager = LinearLayoutManager(
                    this,
                    LinearLayoutManager.VERTICAL,
                    false
                )
                recyclerView!!.adapter = adapter
            }, 2000)
        }
    }

    private fun launchPurchaseFlow(productDetails: ProductDetails?) {
        val productDetailsParamsList = ImmutableList.of(
            ProductDetailsParams.newBuilder()
                .setProductDetails(productDetails!!)
                .build()
        )
        val billingFlowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(productDetailsParamsList)
            .build()
        billingClient!!.launchBillingFlow(activity!!, billingFlowParams)
    }

    private fun handlePurchase(purchases: Purchase) {
        if (!purchases.isAcknowledged) {
            billingClient!!.acknowledgePurchase(
                AcknowledgePurchaseParams
                    .newBuilder()
                    .setPurchaseToken(purchases.purchaseToken)
                    .build()
            ) { billingResult: BillingResult ->
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    //Setting setIsRemoveAd to true
                    // true - No ads
                    // false - showing ads.
                    prefsConfig!!.setPremium(1)
                    //  goBack();
                }
            }
            //Log.d(TAG, "Purchase Token: " + purchases.purchaseToken)
            //Log.d(TAG, "Purchase Time: " + purchases.purchaseTime)
            //og.d(TAG, "Purchase OrderID: " + purchases.orderId)
        }
    }

    override fun onResume() {
        super.onResume()
        billingClient!!.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.INAPP)
                .build()
        ) { billingResult: BillingResult, list: List<Purchase> ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                for (purchase in list) {
                    if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED && !purchase.isAcknowledged) {
                        handlePurchase(purchase)
                    }
                }
            }
        }
    }

    private fun restorePurchases() {
        billingClient = BillingClient.newBuilder(this).enablePendingPurchases()
            .setListener { _: BillingResult?, _: List<Purchase?>? -> }
            .build()
        val finalBillingClient: BillingClient = billingClient as BillingClient
        billingClient!!.startConnection(object : BillingClientStateListener {
            override fun onBillingServiceDisconnected() {
                establishConnection()
            }

            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    finalBillingClient.queryPurchasesAsync(
                        QueryPurchasesParams.newBuilder()
                            .setProductType(BillingClient.ProductType.INAPP).build()
                    ) { billingResult1: BillingResult, list: List<Purchase?> ->
                        if (billingResult1.responseCode == BillingClient.BillingResponseCode.OK) {
                            if (list.isNotEmpty()) {
                                prefsConfig?.setPremium(1) // set true to activate remove ad feature
                                showSnackbar(btnRestoreFab, getString(R.string.restore_purchases_success), Snackbar.LENGTH_SHORT)
                            } else {
                                //Log.d(TAG, "Oops, No purchase found.")
                                showSnackbar(btnRestoreFab, getString(R.string.restore_purchases_not_found), Snackbar.LENGTH_SHORT)
                                prefsConfig?.setPremium(0) // set false to de-activate remove ad feature
                            }
                        }
                    }
                }
            }
        })
    }


    private fun initViews() {
        activity = this
        handler = Handler(Looper.getMainLooper())
        prefs = Prefs(this)
        prefsConfig = PrefsConfig(this)
        productDetailsList = ArrayList()
        recyclerView = findViewById(R.id.recyclerview)
        btnRestoreFab = findViewById(R.id.fab)
        loadProducts = findViewById(R.id.loadProducts)
        jacktorMsg = findViewById(R.id.jacktorMsg)
        jacktorImg = findViewById(R.id.jacktorImg)
    }

    /*
    override fun onBackPressed() {
        super.onBackPressed()
        startActivity(Intent(activity, MainActivity::class.java))
        finish()
    }
    */



    private fun showSnackbar(view: View?, message: String?, duration: Int) {
        Snackbar.make(view!!, message!!, duration).show()
    }

    override fun onItemClick(pos: Int) {
        if (prefsConfig!!.getPremium() == 0) {
            showSnackbar(btnRestoreFab, getString(R.string.wait), Snackbar.LENGTH_LONG)
            launchPurchaseFlow(productDetailsList!![pos])
        } else {
            showSnackbar(btnRestoreFab, getString(R.string.already_purchased_sb), Snackbar.LENGTH_SHORT)
        }
    }
}