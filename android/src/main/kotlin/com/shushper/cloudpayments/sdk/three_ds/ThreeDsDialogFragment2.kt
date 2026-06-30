package com.shushper.cloudpayments.sdk.three_ds

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ImageButton
import androidx.core.view.isGone
import androidx.fragment.app.DialogFragment
import com.google.gson.JsonParser
import com.shushper.cloudpayments.R
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
// import com.shushper.cloudpayments.sdk.databinding.DialogCpsdkThreeDsBinding

import java.io.UnsupportedEncodingException
import java.net.URLEncoder
import java.util.*

class ThreeDsDialogFragment2 : DialogFragment() {

    interface ThreeDSDialogListener2 {
        fun onAuthorizationCompleted(md: String, paRes: String)
        fun onAuthorizationFailed(error: String?)
    }

    companion object {
        private const val POST_BACK_URL = "https://api.cloudpayments.ru/payments/get3dsData"
        private const val ARG_ACS_URL = "acs_url"
        private const val ARG_MD = "md"
        private const val ARG_PA_REQ = "pa_req"

        fun newInstance(acsUrl: String, paReq: String, md: String) = ThreeDsDialogFragment2().apply {
            arguments = Bundle().also {
                it.putString(ARG_ACS_URL, acsUrl)
                it.putString(ARG_MD, md)
                it.putString(ARG_PA_REQ, paReq)
            }
        }
    }

//    private var _binding: DialogCpsdkThreeDsBinding? = null

//    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
//        _binding = DialogCpsdkThreeDsBinding.inflate(inflater, container, false)
//        return binding.root
        return inflater.inflate(R.layout.dialog_cpsdk_three_ds, container, false)
    }

    override fun onDestroyView() {
        super.onDestroyView()
//        _binding = null
    }

    private val acsUrl by lazy {
        arguments?.getString(ARG_ACS_URL) ?: ""
    }

    private val md by lazy {
        arguments?.getString(ARG_MD) ?: ""
    }

    private val paReq by lazy {
        arguments?.getString(ARG_PA_REQ) ?: ""
    }

    private var listener: ThreeDSDialogListener2? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        isCancelable = false

        val webView = view.findViewById<WebView>(R.id.web_view)
        val closeButton = view.findViewById<ImageButton>(R.id.ic_close)
        webView.webViewClient = ThreeDsWebViewClient()
        webView.settings.domStorageEnabled = true
        webView.settings.javaScriptEnabled = true
        webView.settings.javaScriptCanOpenWindowsAutomatically = true
        webView.addJavascriptInterface(ThreeDsJavaScriptInterface(), "JavaScriptThreeDs")

        try {
            val params = StringBuilder()
                .append("PaReq=").append(URLEncoder.encode(paReq, "UTF-8"))
                .append("&MD=").append(URLEncoder.encode(md, "UTF-8"))
                .append("&TermUrl=").append(URLEncoder.encode(POST_BACK_URL, "UTF-8"))
                .toString()
            webView.postUrl(acsUrl, params.toByteArray())
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
        }

        closeButton.setOnClickListener {
            listener?.onAuthorizationFailed(null)
            dismiss()
        }
    }

    override fun onStart() {
        super.onStart()
        val window = dialog!!.window
        window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
    }

    private inner class ThreeDsWebViewClient : WebViewClient() {
        override fun onPageFinished(view: WebView, url: String) {
            Log.e("URL", "URL: $url")
            if (url.lowercase(Locale.getDefault()) == POST_BACK_URL.lowercase(Locale.getDefault())) {
                Log.d("URL", "POST_BACK_URL FOUND")
                view.isGone = true
                view.loadUrl("javascript:window.JavaScriptThreeDs.processHTML(document.getElementsByTagName('html')[0].innerHTML);")
            }
        }
    }

    internal inner class ThreeDsJavaScriptInterface {
        @JavascriptInterface
        fun processHTML(html: String) {
            Log.d("URL", "START PROCESSING HTML RESPONSE")
            val doc: Document = Jsoup.parse(html)
            val element: Element? = doc.select("body").first()
            Log.d("URL", "BODY ${element.toString()}")
            val jsonObject = JsonParser().parse(element?.ownText()).asJsonObject
            val paRes = jsonObject["PaRes"].asString
            Log.d("URL", "PARES VALUE $paRes")
            requireActivity().runOnUiThread {
                if (!paRes.isNullOrEmpty()) {
                    Log.d("URL", "SUCCESS LISTENER TRIGGERED, listener ${listener != null}")
                    listener?.onAuthorizationCompleted(md, paRes)
                } else {
                    Log.d("URL", "FAILURE LISTENER TRIGGERED")
                    listener?.onAuthorizationFailed(html ?: "")
                }
                dismissAllowingStateLoss()
            }
        }
    }

//    override fun onAttach(context: Context) {
//        super.onAttach(context)
//
//        listener = targetFragment as? ThreeDSDialogListener2
//        if (listener == null) {
//            listener = context as? ThreeDSDialogListener2
//        }
//    }
//
//    override fun onAttach(activity: Activity) {
//        super.onAttach(activity)
//
//        listener = targetFragment as? ThreeDSDialogListener2
//        if (listener == null) {
//            listener = activity as? ThreeDSDialogListener2
//        }
//    }

    fun setListener(listener: ThreeDSDialogListener2) {
        Log.e("URL", "Listener is set")
        this.listener = listener
    }
}