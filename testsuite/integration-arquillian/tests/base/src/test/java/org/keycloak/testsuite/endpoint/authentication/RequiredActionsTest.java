/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.keycloak.testsuite.endpoint.authentication;

import org.junit.Assert;
import org.junit.Test;
import org.keycloak.representations.idm.RequiredActionProviderRepresentation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:mstrukel@redhat.com">Marko Strukelj</a>
 */
public class RequiredActionsTest extends AbstractAuthenticationTest {

    @Test
    public void testRequiredActions() {
        List<RequiredActionProviderRepresentation> result = authMgmtResource.getRequiredActions();

        List<RequiredActionProviderRepresentation> expected = new ArrayList<>();
        addRequiredAction(expected, "CONFIGURE_TOTP", "Configure Totp", true, false, null);
        addRequiredAction(expected, "UPDATE_PASSWORD", "Update Password", true, false, null);
        addRequiredAction(expected, "UPDATE_PROFILE", "Update Profile", true, false, null);
        addRequiredAction(expected, "VERIFY_EMAIL", "Verify Email", true, false, null);
        addRequiredAction(expected, "terms_and_conditions", "Terms and Conditions", false, false, null);

        compareRequiredActions(expected, sort(result));

        RequiredActionProviderRepresentation forUpdate = newRequiredAction("VERIFY_EMAIL", "Verify Email", false, false, null);
        try {
            authMgmtResource.updateRequiredAction(forUpdate.getAlias(), forUpdate);
            Assert.fail("updateRequiredAction should fail due to null config");
        } catch (Exception ignored) {
        }

        forUpdate.setConfig(Collections.<String, String>emptyMap());
        authMgmtResource.updateRequiredAction(forUpdate.getAlias(), forUpdate);

        result = authMgmtResource.getRequiredActions();
        RequiredActionProviderRepresentation updated = findRequiredActionByAlias(forUpdate.getAlias(), result);

        Assert.assertNotNull("Required Action still there", updated);
        compareRequiredAction(forUpdate, updated);
    }


    private RequiredActionProviderRepresentation findRequiredActionByAlias(String alias, List<RequiredActionProviderRepresentation> list) {
        for (RequiredActionProviderRepresentation a: list) {
            if (alias.equals(a.getAlias())) {
                return a;
            }
        }
        return null;
    }

    private List<RequiredActionProviderRepresentation> sort(List<RequiredActionProviderRepresentation> list) {
        ArrayList<RequiredActionProviderRepresentation> sorted = new ArrayList<>(list);
        Collections.sort(sorted, new RequiredActionProviderComparator());
        return sorted;
    }

    private void compareRequiredActions(List<RequiredActionProviderRepresentation> expected, List<RequiredActionProviderRepresentation> actual) {
        Assert.assertNotNull("Actual null", actual);
        Assert.assertEquals("Required actions count", expected.size(), actual.size());

        Iterator<RequiredActionProviderRepresentation> ite = expected.iterator();
        Iterator<RequiredActionProviderRepresentation> ita = actual.iterator();
        while (ite.hasNext()) {
            compareRequiredAction(ite.next(), ita.next());
        }
    }

    private void compareRequiredAction(RequiredActionProviderRepresentation expected, RequiredActionProviderRepresentation actual) {
        Assert.assertEquals("alias - " + expected.getAlias(), expected.getAlias(), actual.getAlias());
        Assert.assertEquals("name - "  + expected.getAlias(), expected.getName(), actual.getName());
        Assert.assertEquals("enabled - "  + expected.getAlias(), expected.isEnabled(), actual.isEnabled());
        Assert.assertEquals("defaultAction - "  + expected.getAlias(), expected.isDefaultAction(), actual.isDefaultAction());
        Assert.assertEquals("config - " + expected.getAlias(), expected.getConfig() != null ? expected.getConfig() : Collections.emptyMap(), actual.getConfig());
    }

    private void addRequiredAction(List<RequiredActionProviderRepresentation> target, String alias, String name, boolean enabled, boolean defaultAction, Map conf) {
        target.add(newRequiredAction(alias, name, enabled, defaultAction, conf));
    }

    private RequiredActionProviderRepresentation newRequiredAction(String alias, String name, boolean enabled, boolean defaultAction, Map conf) {
        RequiredActionProviderRepresentation action = new RequiredActionProviderRepresentation();
        action.setAlias(alias);
        action.setName(name);
        action.setEnabled(enabled);
        action.setDefaultAction(defaultAction);
        action.setConfig(conf);
        return action;
    }

    private static class RequiredActionProviderComparator implements Comparator<RequiredActionProviderRepresentation> {
        @Override
        public int compare(RequiredActionProviderRepresentation o1, RequiredActionProviderRepresentation o2) {
            return o1.getAlias().compareTo(o2.getAlias());
        }
    }
}
