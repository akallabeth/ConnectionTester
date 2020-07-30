package com.github.akallabeth.connectiontester;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

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

    private void exMsg(Exception e) {
        String txt = "success!";
        if (e != null) {
            txt = getMsgTxt(e);
        }
        final String finalTxt = txt;
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                dialog.dismiss();
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
        getActivity().runOnUiThread(new Runnable() {
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
                    connection.connect();
                    try {
                    } finally {
                        connection.disconnect();
                    }
                } catch (Exception e) {
                    exMsg(e);
                }
                exMsg(null);
            }
        };
        try {
            executor.submit(task);
        } catch (Exception e) {
            exMsg(e);
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