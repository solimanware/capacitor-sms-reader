package ai.soliman.smsreader;

import android.Manifest;
import com.getcapacitor.JSObject;
import com.getcapacitor.PermissionState;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;
import com.getcapacitor.annotation.Permission;
import com.getcapacitor.annotation.PermissionCallback;
import java.util.ArrayList;
import org.json.JSONArray;

@CapacitorPlugin(name = "SMSInboxReader", permissions = { @Permission(strings = { Manifest.permission.READ_SMS }, alias = "sms") })
public class SMSInboxReaderPlugin extends Plugin {

    private SMSInboxReader implementation;

    @Override
    public void load() {
        implementation = new SMSInboxReader(getActivity());
    }

    private void requestSMSPermission(PluginCall call) {
        requestPermissionForAlias("sms", call, "permissionCallback");
    }

    /**
     * Checks the the given permission is granted or not
     * @return Returns true if the permission is granted and false if it is denied.
     */
    private boolean isSMSPermissionGranted() {
        return getPermissionState("sms") == PermissionState.GRANTED;
    }

    @PermissionCallback
    private void permissionCallback(PluginCall call) {
        if (!isSMSPermissionGranted()) {
            call.reject("Permission is required to access SMS.");
            return;
        }

        switch (call.getMethodName()) {
            case "getCount":
                getCount(call);
                break;
            case "getSMSList":
                getSMSList(call);
                break;
            case "getRawSMSList":
                getRawSMSList(call);
                break;
        }
    }

    @PluginMethod
    public void getRawSMSList(PluginCall call) {
        if (!isSMSPermissionGranted()) {
            requestSMSPermission(call);
        } else {
            GetSMSFilterInput filter = new GetSMSFilterInput(call.getObject("filter", new JSObject()));

            JSONArray rawSMSList = implementation.getRawSMSList(filter);

            JSObject ret = new JSObject();
            ret.put("rawSmsList", rawSMSList);
            call.resolve(ret);
        }
    }

    @PluginMethod
    public void getCount(PluginCall call) {
        if (!isSMSPermissionGranted()) {
            requestSMSPermission(call);
        } else {
            JSObject filters = call.getObject("filter", new JSObject());
            Integer count = implementation.getCount();
            JSObject ret = new JSObject();
            ret.put("count", count);
            call.resolve(ret);
        }
    }

    @PluginMethod
    public void getSMSList(PluginCall call) {
        if (!isSMSPermissionGranted()) {
            requestSMSPermission(call);
        } else {
            GetSMSProjectionInput projection = new GetSMSProjectionInput(call.getObject("projection", new JSObject()));
            GetSMSFilterInput filter = new GetSMSFilterInput(call.getObject("filter", new JSObject()));

            ArrayList<SMSObject> smsListObj = implementation.getSMSList(projection, filter);

            JSONArray smsList = new JSONArray();
            for (SMSObject sms : smsListObj) {
                smsList.put(sms.getJSONObject());
            }

            JSObject ret = new JSObject();
            ret.put("smsList", smsList);
            call.resolve(ret);
        }
    }
}
