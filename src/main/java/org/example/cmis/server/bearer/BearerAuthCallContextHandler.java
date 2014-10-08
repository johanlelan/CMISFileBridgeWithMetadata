package org.example.cmis.server.bearer;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.chemistry.opencmis.commons.server.CallContext;
import org.apache.chemistry.opencmis.server.shared.CallContextHandler;

public class BearerAuthCallContextHandler implements CallContextHandler,
        Serializable {

    private static final long serialVersionUID = -7992600230829905177L;

    /**
     * Constructor.
     */
    public BearerAuthCallContextHandler() {
    }

    @Override
    public Map<String, String> getCallContextMap(HttpServletRequest request) {
        assert request != null;

        Map<String, String> result = null;

        String authHeader = request.getHeader("Authorization");
        if ((authHeader != null)
                && (authHeader.trim().toLowerCase(Locale.ENGLISH)
                        .startsWith("bearer "))) {
            int x = authHeader.lastIndexOf(' ');
            if (x == -1) {
                return result;
            }

            String credentials = null;
            try {
                credentials = authHeader.substring(x + 1);
            } catch (Exception e) {
                return result;
            }

            // extract user and password and add them to map
            result = new HashMap<String, String>();

            // Call Central Authentication Service to obtain user's information
            // TODO JLL : Call CAS

            result.put(CallContext.USERNAME, "test");
            result.put(CallContext.PASSWORD, credentials);
            
            result.put("AUTH.MODE", "bearer");
        }

        return result;
    }

}
