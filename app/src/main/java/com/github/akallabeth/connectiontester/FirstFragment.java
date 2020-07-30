package com.github.akallabeth.connectiontester;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import org.apache.commons.net.util.TrustManagerUtils;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;

public class FirstFragment extends Fragment {

    private ThreadPoolExecutor executor;
    private TextView result;
    private ProgressDialog dialog = null;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        // Inflate the layout for this fragment
        executor = new ThreadPoolExecutor(2, 8, 10000, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>(), new ThreadPoolExecutor.CallerRunsPolicy());
        return inflater.inflate(R.layout.fragment_first, container, false);
    }

    private void exMsg(Exception e, int code) {
        String txt = "success! http code " + String.valueOf(code);
        if (e != null) {
            txt = getMsgTxt(e);
        }
        final String finalTxt = txt;
        requireActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (dialog != null) {
                    dialog.dismiss();
                }
                dialog = null;
                result.setText(finalTxt);
                result.setVisibility(View.VISIBLE);
            }
        });

    }

    private String getMsgTxt(Exception e) {
        String lm = e.getLocalizedMessage();
        if (lm != null) {
            return lm;
        }

        String m = e.getMessage();
        if (m != null) {
            return m;
        }

        String c = e.toString();
        return c;
    }

    private void testConnect(final String urlString) {
        requireActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                dialog = new ProgressDialog(requireContext());
                dialog.setTitle("Connecting to");
                dialog.setMessage(urlString);
                dialog.show();
                result.setVisibility(View.GONE);
            }
        });
        Runnable task = new Runnable() {

            @Override
            public void run() {
                try {
                    URL url = new URL(urlString);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                    if (connection instanceof HttpsURLConnection) {
                        HttpsURLConnection ssl = (HttpsURLConnection) connection;

                        SSLContext sslCtx;
                        try {
                            TrustManager tm = TrustManagerUtils.getAcceptAllTrustManager();
                            HostnameVerifier hv = new HostnameVerifier() {
                                @Override
                                public boolean verify(String s, SSLSession sslSession) {
                                    return true;
                                }
                            };
                            TrustManager[] tms = new TrustManager[]{tm};
                            sslCtx = SSLContext.getInstance("TLS");
                            sslCtx.init(null, tms, null);

                            ssl.setHostnameVerifier(hv);
                            ssl.setSSLSocketFactory(sslCtx.getSocketFactory());
                        } catch (KeyManagementException | NoSuchAlgorithmException e) {
                            throw new IOException("SSLContext.getInstance(TLS) failed");
                        }
                    }

                    connection.connect();
                    try {
                        int responseCode = connection.getResponseCode();
                        exMsg(null, responseCode);
                    } finally {
                        connection.disconnect();
                    }
                } catch (Exception e) {
                    exMsg(e, -1);
                }
            }
        };
        try {
            executor.submit(task);
        } catch (Exception e) {
            exMsg(e, -1);
        }
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final Button button = view.findViewById(R.id.button_first);
        final EditText input = view.findViewById(R.id.url_input);
        result = view.findViewById(R.id.textView);

        button.setEnabled(!input.getText().toString().isEmpty());
        input.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                button.setEnabled(!editable.toString().isEmpty());
            }
        });
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                testConnect(input.getText().toString());
            }
        });
    }
}