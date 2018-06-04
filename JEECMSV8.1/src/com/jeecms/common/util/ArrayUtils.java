package com.jeecms.common.util;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.jeecms.cms.api.Constants;

/**
 * @author Tom
 */
public class ArrayUtils {
	public static Integer[] convertStrArrayToInt(String[]strArray){
		if(strArray!=null&&strArray.length>0){
			Integer array[]=new Integer[strArray.length];
			for(int i=0;i<strArray.length;i++){  
			    array[i]=Integer.parseInt(strArray[i]);
			}
			return array;
		}else{
			return null;
		}
	}
	
	public static Double[] convertStrArrayToDouble(Object[]strArray){
		if(strArray!=null&&strArray.length>0){
			Double array[]=new Double[strArray.length];
			for(int i=0;i<strArray.length;i++){  
			    array[i]=Double.parseDouble((String) strArray[i]);
			}
			return array;
		}else{
			return null;
		}
	}
	
	public static Integer[]parseStringToArray(String ids){
		if(StringUtils.isNotBlank(ids)){
			String[] idArray=ids.split(Constants.API_ARRAY_SPLIT_STR);
			Integer[] intIds=new Integer[idArray.length];
			for(int i=0;i<idArray.length;i++){
				if(StringUtils.isNumeric(idArray[i])){
					intIds[i]=Integer.parseInt(idArray[i]);
				}
			}
			return intIds;
		}else{
			return null;
		}
	}
	
	public static String[] removeStringArrayBlank(String[]array){
		if(array!=null){
			List<String> tmp = new ArrayList<String>();
			for(String str:array){
				if(StringUtils.isNotBlank(str)){
		
					tmp.add(str);
		
				}
			}
			return (String[]) tmp.toArray();
		}else{
			return null;
		}
	}
}
