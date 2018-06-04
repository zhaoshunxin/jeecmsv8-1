<#--
<select><option></option></select>
-->
<#macro select2
	list value="" multiple="" headerKey="" headerValue="" listKey="" listValue="" listDeep="" headerButtom="false"
	label="" noHeight="false" required="false" colspan="" width="100" help="" helpPosition="2" colon=":" hasColon="true"
	id="" name="" class="" style="" size="" title="" disabled="" tabindex="" accesskey=""
	vld=""
	onclick="" ondblclick="" onmousedown="" onmouseup="" onmouseover="" onmousemove="" onmouseout="" onfocus="" onblur="" onkeypress="" onkeydown="" onkeyup="" onselect="" onchange=""
	>
<#include "control.ftl"/><#rt/>
<meta http-equiv="content-type" content="text/html; charset=UTF-8">
  <link rel="stylesheet" href="/r/cms/www/default/css/zTreeStyle.css" type="text/css">
<script type="text/javascript" src="/r/cms/www/default/js/jquery.ztree.core.js"></script>
  <SCRIPT type="text/javascript" >
  <!--
	var zTree;
	
	function zTreeOnClick(event, treeId, treeNode) {
		$("#channelId").val(treeNode.id);
	};	

	var setting = {
		view: {
			dblClickExpand: false,
			showLine: true,
			selectedMulti: false
		},
		data: {
			simpleData: {
				enable:true,
				idKey: "id",
				pIdKey: "pId",
				rootPId: ""
			}
		},
		callback: {
		onClick: zTreeOnClick
		}
	};
	var zNodes=[
		{id:0, pId:"", name:"所有栏目",open:true},
		<#list list as item>
			{id:${item[0].id}, pId:<#if item[1]??>${item[1].id}<#else>0</#if>, name:"${item[0][listValue]!}", value:"${item[0][listKey]}"},
		</#list>
	
	];
	$(document).ready(function(){
		var t = $("#tree");
		t = $.fn.zTree.init(t, setting, zNodes);

	});

  //-->
  </SCRIPT>
  <ul id="tree" class="ztree" style="width:260px; overflow:auto;"></ul>
  <input type="hidden" id="channelId" value="">
<#include "control-close.ftl"/><#rt/>
</#macro>
