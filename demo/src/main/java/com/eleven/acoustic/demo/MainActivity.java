package com.eleven.acoustic.demo;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.eleven.acoustic.receive.SignalReceiver;
import com.eleven.acoustic.receive.demodulate.DPSKSignalDemodulator;
import com.eleven.acoustic.receive.demodulate.OFDMSignalDemodulator;
import com.eleven.acoustic.send.SignalSender;
import com.eleven.acoustic.send.generate.DPSKSignalGenerator;
import com.eleven.acoustic.send.generate.OFDMSignalGenerator;

public class MainActivity extends AppCompatActivity {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    private final static int SAMPLE_RATE = 44100;
    private final static String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.

        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {

        }

    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        private int mSignalType;

        private SignalSender mSignalSender;
        private SignalReceiver mSignalReceiver;

        private EditText edit_signal;
        private Button btn_send;
        private Button btn_recv;

        private Context mContext;

        private boolean isReceiving;


        public PlaceholderFragment() {
            Log.i(TAG, "get default");
            mSignalSender = SignalSender.getDefault();
            mSignalReceiver = SignalReceiver.getDefault();
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public void onAttach(Context context) {
            super.onAttach(context);
            mContext = context;
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            Bundle args = this.getArguments();
            mSignalType = args.getInt(ARG_SECTION_NUMBER);
            isReceiving = false;
            switch (mSignalType) {
                case 1:
                    Log.i(TAG, "set dpsk");
                    mSignalSender.setSignalGenerator(new DPSKSignalGenerator());
                    mSignalReceiver.setSignalDemodulator(new DPSKSignalDemodulator());
                    mSignalReceiver.setOnReceivedListener(
                            new SignalReceiver.OnSignalReceivedListener() {
                                @Override
                                public void onReceived(int[] result) {
                                    StringBuilder sb = new StringBuilder();
                                    for (int a : result) {
                                        sb.append(a);
                                    }
                                    AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                                    builder.setMessage("接收到数据为：" + sb.toString());
                                    builder.setCancelable(false);
                                    builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.cancel();
                                        }
                                    });
                                    builder.create().show();
                                }

                                @Override
                                public void onNotReceived() {
                                    AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                                    builder.setMessage("未接收到数据");
                                    builder.setCancelable(false);
                                    builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.cancel();
                                        }
                                    });
                                    builder.create().show();
                                }
                            }
                    );
                    break;
                case 2:
                    Log.i(TAG, "set ofdm");
                    mSignalSender.setSignalGenerator(new OFDMSignalGenerator());
                    mSignalReceiver.setSignalDemodulator(new OFDMSignalDemodulator());
                    mSignalReceiver.setOnReceivedListener(
                            new SignalReceiver.OnSignalReceivedListener() {
                                @Override
                                public void onReceived(int[] result) {
                                    StringBuilder sb = new StringBuilder();
                                    for (int a : result) {
                                        sb.append(a);
                                    }
                                    AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                                    builder.setMessage("接收到数据为：" + sb.toString());
                                    builder.setCancelable(false);
                                    builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.cancel();
                                        }
                                    });
                                    builder.create().show();
                                }

                                @Override
                                public void onNotReceived() {
                                    AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                                    builder.setMessage("未接收到数据");
                                    builder.setCancelable(false);
                                    builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.cancel();
                                        }
                                    });
                                    builder.create().show();
                                }
                            }
                    );
                    break;

            }
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);

            edit_signal = (EditText) rootView.findViewById(R.id.signal);
            btn_send = (Button) rootView.findViewById(R.id.btn_send);
            btn_recv = (Button) rootView.findViewById(R.id.btn_recv);

            btn_send.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //boolean isSave;
                    String str_signal;
                    int[] num_signal;
                    switch (mSignalType) {
                        case 1 :
                            isReceiving = false;
                            str_signal = edit_signal.getText().toString();
                            num_signal = new int[str_signal.length()];
                            for (int i = 0; i < num_signal.length; i++) {
                                num_signal[i] = str_signal.charAt(i) - '0';
                            }
                            mSignalSender.sendSignal(num_signal);
                            break;
                        case 2 :
                            str_signal = edit_signal.getText().toString();
                            num_signal = new int[str_signal.length()];
                            for (int i = 0; i < num_signal.length; i++) {
                                num_signal[i] = str_signal.charAt(i) - '0';
                            }
                            mSignalSender.sendSignal(num_signal);
                            break;
                    }
                }
            });

            btn_recv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //boolean isSave = true;
                    if (!isReceiving) {
                        isReceiving = true;
                        btn_send.setEnabled(false);
                        btn_recv.setText("停止接收");
                        mSignalReceiver.startReceiving();
                    } else {
                        switch (mSignalType) {
                            case 1:
                                isReceiving = false;
                                btn_send.setEnabled(true);
                                btn_recv.setText(R.string.recv_signal);

                                mSignalReceiver.stopReceiving();
                                break;
                            case 2:
                                isReceiving = false;
                                btn_send.setEnabled(true);
                                btn_recv.setText(R.string.recv_signal);

                                mSignalReceiver.stopReceiving();
                                break;
                        }
                    }
                }
            });
            return rootView;
        }
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return PlaceholderFragment.newInstance(position + 1);
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "DPSK";
                case 1:
                    return "OFDM";
            }
            return null;
        }
    }
}
