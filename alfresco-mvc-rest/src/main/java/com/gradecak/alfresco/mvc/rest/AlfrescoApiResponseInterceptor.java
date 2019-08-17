package com.gradecak.alfresco.mvc.rest;

import javax.servlet.http.HttpServletResponse;

import org.alfresco.rest.framework.resource.parameters.Params;
import org.alfresco.rest.framework.resource.parameters.Params.RecognizedParams;
import org.alfresco.rest.framework.webscripts.ResourceWebScriptHelper;
import org.springframework.core.MethodParameter;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.servlet.WebScriptServletRequest;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import com.gradecak.alfresco.mvc.webscript.DispatcherWebscript.WebscriptRequestWrapper;
import com.gradecak.alfresco.mvc.rest.annotation.AlfrescoRestResponse;
import com.gradecak.alfresco.mvc.webscript.LocalHttpServletResponse;

/**
 * 
 * class used to process the response with alfresco rest API behavior
 * only if the anootaion {@link AlfrescoRestResponse} is used
 */
@ControllerAdvice
public class AlfrescoApiResponseInterceptor implements ResponseBodyAdvice<Object> {

  private final ResourceWebScriptHelper webscriptHelper;
  private final boolean globalAlfrescoResponse;;

  public AlfrescoApiResponseInterceptor(final ResourceWebScriptHelper webscriptHelper) {
    this(webscriptHelper, false);
  }
  
  public AlfrescoApiResponseInterceptor(final ResourceWebScriptHelper webscriptHelper, final boolean globalAlfrescoResponse) {
    this.webscriptHelper = webscriptHelper;
	this.globalAlfrescoResponse = globalAlfrescoResponse;
  }

  @Override
  public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType, Class<? extends HttpMessageConverter<?>> selectedConverterType, ServerHttpRequest request,
      ServerHttpResponse response) {

	boolean useAlfrescoResponse = globalAlfrescoResponse;
	
	if(!useAlfrescoResponse) {
	  AlfrescoRestResponse methodAnnotation = returnType.getMethodAnnotation(AlfrescoRestResponse.class);
	  if(methodAnnotation == null){
	  	methodAnnotation = returnType.getContainingClass().getAnnotation(AlfrescoRestResponse.class);
	  }
	  
	  if(methodAnnotation != null) {
		  useAlfrescoResponse = true;
	  }
	}
		
	if(useAlfrescoResponse){
	    WebScriptServletRequest webScriptServletRequest = null;
	    if (response instanceof ServletServerHttpResponse) {
	      HttpServletResponse servletResponse = ((ServletServerHttpResponse) response).getServletResponse();
	      if (servletResponse instanceof LocalHttpServletResponse) {
	        WebscriptRequestWrapper localServletResponse = ((LocalHttpServletResponse) servletResponse).getRequestWrapper();
	        webScriptServletRequest = localServletResponse.getWebScriptServletRequest();
	      }
	    }

	    return webscriptHelper.processAdditionsToTheResponse(null, null, null, getDefaultParameters(webScriptServletRequest), body);
	}
	
    return body;
  }

  @Override
  public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
    return converterType.isAssignableFrom(MappingJackson2HttpMessageConverter.class);
  }

  static public Params getDefaultParameters(WebScriptRequest wsr) {
    if (wsr != null) {
      final RecognizedParams params = new AlfrescoRecognizedParamsExtractor().getRecognizedParams(wsr);
      return Params.valueOf(params, null, null, wsr);
    }
    Params parameters = Params.valueOf("", null, null);
    return parameters;
  }
}
