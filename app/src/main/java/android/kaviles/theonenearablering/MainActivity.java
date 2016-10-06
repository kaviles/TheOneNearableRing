package android.kaviles.theonenearablering;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.EstimoteSDK;
import com.estimote.sdk.Nearable;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private final static String TAG = MainActivity.class.getSimpleName();
//    private final static String TAG = "MainActivity";

    private BeaconManager beaconManager;
    private String scanId;

    private ArrayList<Nearable> nearableList;
    private EstimoteListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        EstimoteSDK.initialize(this, "kelvin-s-app-7i2", "3c1c62fa88e57ad3e1a4b8a392a08a7e");
        nearableList = new ArrayList<>();
        adapter = new EstimoteListAdapter(nearableList);

        ListView listView = (ListView) findViewById(R.id.listView);

        if (listView != null) {
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                    Nearable nearable = nearableList.get(position);

                    Intent intent = new Intent(MainActivity.this, ControllerActivity.class);
                    intent.putExtra("IDENTIFIER", nearable.identifier);

                    startActivity(intent);
                }
            });

            listView.setAdapter(adapter);
        }


        beaconManager = new BeaconManager(this);
        beaconManager.setNearableListener(new BeaconManager.NearableListener() {
            @Override
            public void onNearablesDiscovered(List<Nearable> nearables) {
                Log.d(TAG, "Discovered nearables: " + nearables);

                nearableList.clear();

                for (Nearable nearable : nearables) {
                    nearableList.add(nearable);
                }

                adapter.notifyDataSetChanged();
            }
        });
    }

//    @Override
//    protected void onStart() {
//        super.onStart();
//
//        beaconManager.connect(new BeaconManager.ServiceReadyCallback() {
//            @Override
//            public void onServiceReady() {
//                scanId = beaconManager.startNearableDiscovery();
//            }
//        });
//    }

    @Override
    protected void onResume() {
        super.onResume();
        beaconManager.connect(new BeaconManager.ServiceReadyCallback() {
            @Override
            public void onServiceReady() {
                scanId = beaconManager.startNearableDiscovery();
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();

        beaconManager.stopNearableDiscovery(scanId);
        beaconManager.disconnect();
    }

//    @Override
//    protected void onStop() {
//        super.onStop();
//
//        beaconManager.stopNearableDiscovery(scanId);
//    }

//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//
//        beaconManager.disconnect();
//    }

    private class EstimoteListAdapter extends BaseAdapter {

        ArrayList<Nearable> el;

        public EstimoteListAdapter(ArrayList<Nearable> estimoteList) {
            el = estimoteList;
        }

        @Override
        public int getCount() {
            return el.size();
        }

        @Override
        public Object getItem(int position) {
            return el.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            LayoutInflater inflater = getLayoutInflater();
            View row = inflater.inflate(R.layout.estimote_data_row, parent, false);

            TextView tv_identifier, tv_color, tv_type;
            tv_identifier = (TextView) row.findViewById(R.id.tv_identifier);
            tv_color = (TextView) row.findViewById(R.id.tv_color);
            tv_type = (TextView) row.findViewById(R.id.tv_type);

            tv_identifier.setText(el.get(position).identifier);
            tv_color.setText(el.get(position).color.text);
            tv_type.setText(el.get(position).type.text);

            return row;
        }
    }
}
