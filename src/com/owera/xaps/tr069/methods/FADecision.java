package com.owera.xaps.tr069.methods;

import java.util.List;

import com.owera.xaps.base.Log;
import com.owera.xaps.dbi.util.ProvisioningMessage;
import com.owera.xaps.dbi.util.ProvisioningMessage.ErrorResponsibility;
import com.owera.xaps.dbi.util.ProvisioningMessage.ProvStatus;
import com.owera.xaps.dbi.util.ProvisioningMode;
import com.owera.xaps.tr069.HTTPReqResData;
import com.owera.xaps.tr069.SessionData;
import com.owera.xaps.tr069.xml.Fault;

public class FADecision {
	public static void process(HTTPReqResData reqRes) {
		//		Fault fault = reqRes.getRequest().getFault();
		//		String errormsg = null;
		//		if (fault != null)
		//			errormsg = "The CPE reported faultcode " + fault.getFaultCode() + " and faultstring " + fault.getFaultString();
		//		Throwable throwable = reqRes.getThrowable();
		//		if (throwable != null) {
		//			if (errormsg == null)
		//				errormsg = "The ACS experienced an exception: " + throwable;
		//			else
		//				errormsg += " and the ACS experienced an exception: " + throwable;
		//		}
		//		Log.error(FADecision.class, errormsg);

		SessionData sessionData = reqRes.getSessionData();
		if (sessionData.getUnit().getProvisioningMode() == ProvisioningMode.REGULAR) {
			List<HTTPReqResData> reqResList = sessionData.getReqResList();
			if (reqResList != null && reqResList.size() >= 3) {
				HTTPReqResData prevReqRes = reqResList.get(reqResList.size() - 2);
				HTTPReqResData prev2ReqRes = reqResList.get(reqResList.size() - 3);
				String prevMethod = prevReqRes.getResponse().getMethod();
				if (prevMethod.equals(TR069Method.GET_PARAMETER_VALUES)) {
					String prev2Method = prev2ReqRes.getResponse().getMethod();
					if (!prev2Method.equals(TR069Method.GET_PARAMETER_VALUES)) {
						Log.warn(FADecision.class, "GPVres contained error, try once more and ask for all parameters");
						reqRes.getResponse().setMethod(TR069Method.GET_PARAMETER_VALUES);
						return;
					}
				}
			}
		}

		ProvisioningMessage pm = sessionData.getProvisioningMessage();
		Fault fault = reqRes.getRequest().getFault();
		pm.setErrorCode(new Integer(fault.getFaultCode()));
		pm.setErrorMessage(fault.getFaultString());
		pm.setErrorResponsibility(ErrorResponsibility.CLIENT);
		pm.setProvStatus(ProvStatus.ERROR);
		reqRes.getResponse().setMethod(TR069Method.EMPTY);
	}
}
