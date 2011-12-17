/**
 *  Copyright 2003-2007 Greg Luck
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package net.sf.jpam;

import org.eel.kitchen.pam.PamService;

public final class Pam
{
    private static final String JPAM_SHARED_LIBRARY_NAME = "jpam";

    public static final String DEFAULT_SERVICE_NAME
        = "net-sf-" + JPAM_SHARED_LIBRARY_NAME;

    private Pam()
    {
    }

    public static PamService getService()
        throws PamException
    {
        return getService(DEFAULT_SERVICE_NAME);
    }

    public static PamService getService(final String service)
        throws PamException
    {
        return new PamService(service);
    }
}
