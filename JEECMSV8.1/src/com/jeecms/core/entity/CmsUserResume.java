package com.jeecms.core.entity;

import org.json.JSONException;
import org.json.JSONObject;

import com.jeecms.common.util.DateUtils;
import com.jeecms.core.entity.base.BaseCmsUserResume;



public class CmsUserResume extends BaseCmsUserResume {
	private static final long serialVersionUID = 1L;
	
	public JSONObject convertToJson() 
			throws JSONException{
		JSONObject json=new JSONObject();
		json.put("id", getId());
		json.put("resumeName", getResumeName());
		json.put("targetWorknature", getTargetWorknature());
		json.put("targetWorkplace", getTargetWorkplace());
		json.put("targetCategory", getTargetCategory());
		json.put("targetSalary", getTargetSalary());
		json.put("eduSchool", getEduSchool());
		if(getEduGraduation()!=null){
			json.put("eduGraduation", DateUtils.parseDateToDateStr(getEduGraduation()));
		}else{
			json.put("eduGraduation", "");
		}
		json.put("eduBack", getEduBack());
		json.put("eduDiscipline", getEduDiscipline());
		json.put("recentCompany", getRecentCompany());
		json.put("companyIndustry", getCompanyIndustry());
		json.put("companyScale", getCompanyScale());
		json.put("jobName", getJobName());
		if(getJobStart()!=null){
			json.put("jobStart", DateUtils.parseDateToDateStr(getJobStart()));
		}else{
			json.put("jobStart", "");
		}
		json.put("jobCategory", getJobCategory());
		json.put("subordinates", getSubordinates());
		json.put("jobDescription", getJobDescription());
		json.put("selfEvaluation", getSelfEvaluation());
		return json;
	}

/*[CONSTRUCTOR MARKER BEGIN]*/
	public CmsUserResume () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public CmsUserResume (java.lang.Integer id) {
		super(id);
	}

	/**
	 * Constructor for required fields
	 */
	public CmsUserResume (
		java.lang.Integer id,
		java.lang.String resumeName) {

		super (
			id,
			resumeName);
	}

/*[CONSTRUCTOR MARKER END]*/


}