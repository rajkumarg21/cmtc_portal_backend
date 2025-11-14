package com.cmtc.cms.service;

public interface CaptchaService {

	boolean verifyCaptcha(String recaptchaToken);

}
