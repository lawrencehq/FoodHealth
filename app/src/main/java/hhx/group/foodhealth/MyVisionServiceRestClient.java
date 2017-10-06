package hhx.group.foodhealth;

import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.microsoft.projectoxford.vision.VisionServiceRestClient;
import com.microsoft.projectoxford.vision.contract.AnalyzeResult;
import com.microsoft.projectoxford.vision.rest.VisionServiceException;
import com.microsoft.projectoxford.vision.rest.WebServiceRequest;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

/**
 * Created by Enthalqy Huang on 2017/10/5.
 * Own Clint, change base URL
 */

public class MyVisionServiceRestClient extends VisionServiceRestClient {

    private WebServiceRequest restCall = null;
    private Gson gson = new Gson();

    public MyVisionServiceRestClient(String subscriptKey) {
        super(subscriptKey);
        this.restCall = new WebServiceRequest(subscriptKey);
    }

    @Override
    public AnalyzeResult analyzeImage(InputStream stream, String[] visualFeatures) throws VisionServiceException, IOException {
        HashMap params = new HashMap();
        String features = TextUtils.join(",", visualFeatures);
        params.put("visualFeatures", features);
        String path = "https://australiaeast.api.cognitive.microsoft.com/vision/v1.0/analyze";
        String uri = WebServiceRequest.getUrl(path, params);
        params.clear();
        byte[] data = IOUtils.toByteArray(stream);
        params.put("data", data);
        String json = (String)this.restCall.request(uri, "POST", params, "application/octet-stream", false);
        Log.d("Debug", json);
        AnalyzeResult visualFeature = (AnalyzeResult)this.gson.fromJson(json, MyAnalyzeResult.class);
        return visualFeature;
    }
}
