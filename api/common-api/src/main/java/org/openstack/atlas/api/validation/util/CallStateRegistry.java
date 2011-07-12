
package org.openstack.atlas.api.validation.util;

import java.lang.reflect.Method;
import java.util.Stack;

public class CallStateRegistry {
    private final ThreadLocal<LastKnownCall> threadLocalizedCallTracker;
    private final Stack<Method> lastKnownCalls;

    public Stack<Method> getLastKnownCalls() {
        return lastKnownCalls;
    }

    public CallStateRegistry() {
        threadLocalizedCallTracker = new ThreadLocal<LastKnownCall>();
        lastKnownCalls = new Stack<Method>();
    }

    public void registerCall(Method m) {
        threadLocalizedCallTracker.set(new LastKnownCall(m));
        lastKnownCalls.push(m);
    }

    public Method getLastKnownCall() {
        final LastKnownCall call = threadLocalizedCallTracker.get();
        threadLocalizedCallTracker.remove();

        return call != null && call.callInformation != null ? call.callInformation : null;
    }

    //TODO: Validate whether or not this is really a good idea. Encapsulation extreme stylez D:
    private static class LastKnownCall {
        public Method callInformation;

        public LastKnownCall(Method callInformation) {
            this.callInformation = callInformation;
        }
    }
}
