package hhx.group.foodhealth;

import com.microsoft.projectoxford.vision.contract.AnalyzeResult;

import java.util.List;

/**
 * Created by Enthalqy Huang on 2017/10/5.
 * Own AnalyzeResult that support the automatic parse of tag
 */

public class MyAnalyzeResult extends AnalyzeResult {
    public List<Tag> tags;

    public MyAnalyzeResult() {
        super();
    }
}
