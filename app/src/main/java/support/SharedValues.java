package support;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;


public class SharedValues {

    Context context;

    public SharedValues(Context mContext)
    {
        context = mContext;
    }

    public void saveSharedPreference(String key, String value) {
        Log.i("Save", key + value);
        SharedPreferences pref = context.getSharedPreferences("myscm", Context.MODE_PRIVATE);
        SharedPreferences.Editor ed = pref.edit();
        ed.putString(key, value);
        ed.commit();
    }

    public String loadSharedPreferences_CompanyCode()
    {
        SharedPreferences pref = context.getSharedPreferences("myscm", Context.MODE_PRIVATE);
        String companyCode = pref.getString("companyCode", "");

        if (companyCode.equals("") || companyCode.equals(null))
        {
            return "";
        }
        else
        {
            return companyCode;
        }
    }

    public String loadSharedPreferences_ChangeCode() {
        SharedPreferences pref = context.getSharedPreferences("myscm", Context.MODE_PRIVATE);
        String companyCode = pref.getString("changeCode", "0");
        return companyCode;
    }
}
