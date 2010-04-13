package com.jayway.concurrenttest.synchronizer;

import java.lang.reflect.Method;

public class ProxyState {
	
	private static Object lastTarget;
    private static Method lastMethod;
    private static Object[] lastArgs;
    
	public static Object getLastTarget() {
		return lastTarget;
	}
	public static void setLastTarget(Object lastTarget) {
		ProxyState.lastTarget = lastTarget;
	}
	public static Method getLastMethod() {
		return lastMethod;
	}
	public static void setLastMethod(Method lastMethod) {
		ProxyState.lastMethod = lastMethod;
	}
	public static Object[] getLastArgs() {
		return lastArgs;
	}
	public static void setLastArgs(Object[] lastArgs) {
		ProxyState.lastArgs = lastArgs;
	}
	
}
