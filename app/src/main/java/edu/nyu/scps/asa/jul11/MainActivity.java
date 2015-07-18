package edu.nyu.scps.asa.jul11;

import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

// table of contents

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ListView listView = (ListView)findViewById(R.id.listView);

        ArrayAdapter<CharSequence> arrayAdapter = ArrayAdapter.createFromResource(
                this,
                R.array.people_array,
                android.R.layout.simple_list_item_1
        );

        listView.setAdapter(arrayAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent;
                String upperName = (String) parent.getItemAtPosition(position);
                if (upperName.equals("Asa")) {
                    intent = new Intent(MainActivity.this, AsaActivity.class);
                } else {
                    intent = new Intent();
                    String lowerName = upperName.toLowerCase();
                    ComponentName componentName = new ComponentName(
                            "edu.nyu.scps." + lowerName + ".jul11",                          //name of package
                            "edu.nyu.scps." + lowerName + ".jul11." + upperName + "Activity" //name of class
                    );
                    intent.setComponent(componentName);
                }

                try {
                    startActivity(intent);
                } catch (ActivityNotFoundException activityNotFoundException) {
                    Toast toast = Toast.makeText(MainActivity.this, activityNotFoundException.toString(), Toast.LENGTH_LONG);
                    toast.show();
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
