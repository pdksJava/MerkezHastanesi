package com.pdks.webservice;

import javax.xml.ws.WebFault;

/**
 * This class was generated by Apache CXF 2.6.1 2024-02-29T09:04:09.130+02:00 Generated source version: 2.6.1
 */

@WebFault(name = "Exception", targetNamespace = "http://webService.pdks.com/")
public class Exception_Exception extends java.lang.Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5496803084063223170L;
	private com.pdks.webservice.Exception exception;

	public Exception_Exception() {
		super();
	}

	public Exception_Exception(String message) {
		super(message);
	}

	public Exception_Exception(String message, Throwable cause) {
		super(message, cause);
	}

	public Exception_Exception(String message, com.pdks.webservice.Exception exception) {
		super(message);
		this.exception = exception;
	}

	public Exception_Exception(String message, com.pdks.webservice.Exception exception, Throwable cause) {
		super(message, cause);
		this.exception = exception;
	}

	public com.pdks.webservice.Exception getFaultInfo() {
		return this.exception;
	}
}
