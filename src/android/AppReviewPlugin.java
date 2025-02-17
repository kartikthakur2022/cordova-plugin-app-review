package by.chemerisuk.cordova;

import static com.google.android.gms.tasks.Tasks.await;
import static by.chemerisuk.cordova.support.ExecutionThread.WORKER;
import java.lang.reflect.Field;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;

import com.google.android.play.core.review.ReviewInfo;
import com.google.android.play.core.review.ReviewManager;
import com.google.android.play.core.review.ReviewManagerFactory;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaArgs;
import org.json.JSONException;
import org.json.JSONObject;

import by.chemerisuk.cordova.support.CordovaMethod;
import by.chemerisuk.cordova.support.ReflectiveCordovaPlugin;

public class AppReviewPlugin extends ReflectiveCordovaPlugin {
    @CordovaMethod(WORKER)
    private void requestReview(CallbackContext callbackContext) throws Exception {
        Activity activity = cordova.getActivity();
        ReviewManager manager = ReviewManagerFactory.create(activity);
        ReviewInfo reviewInfo = await(manager.requestReviewFlow());
        await(manager.launchReviewFlow(activity, reviewInfo));
        callbackContext.success();
    }

    @CordovaMethod
    protected void openStoreScreen(CordovaArgs args, CallbackContext callbackContext) throws JSONException {
        String packageName = args.getString(0);
        if (packageName == null) {
            packageName = cordova.getActivity().getPackageName();
        }
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + packageName));
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_NEW_DOCUMENT | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        cordova.getActivity().startActivity(intent);
        callbackContext.success();
    }

    @CordovaMethod
    protected void getDeviceOSVersion(CallbackContext callbackContext) throws Exception{
        try{
            JSONObject details = new JSONObject();
            details.put("version", Build.VERSION.RELEASE);
            details.put("apiLevel", Build.VERSION.SDK_INT);
            details.put("apiName", getNameForApiLevel(Build.VERSION.SDK_INT));
            callbackContext.success(details);
        } catch(Exception e ) {
            handleError("Exception occurred: ".concat(e.getMessage()), callbackContext);
        }
    }

     // https://stackoverflow.com/a/55946200/777265
    protected String getNameForApiLevel(int apiLevel) throws Exception{
        Field[] fields = Build.VERSION_CODES.class.getFields();
        String codeName = "UNKNOWN";
        for (Field field : fields) {
            if (field.getInt(Build.VERSION_CODES.class) == apiLevel) {
                codeName = field.getName();
            }
        }
        return codeName;
    }

    /**
     * Handles an error while executing a plugin API method  in the specified context.
     * Calls the registered Javascript plugin error handler callback.
     * @param errorMsg Error message to pass to the JS error handler
     */
    public void handleError(String errorMsg, CallbackContext context){
        try {
            // logError(errorMsg);
            context.error(errorMsg);
        } catch (Exception e) {
            // logError(e.toString());
        }
    }
}
